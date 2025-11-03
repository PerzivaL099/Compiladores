package compilador.intermedio;

import compilador.parser.ASTVisitor;
// Usamos el wildcard para importaciones más limpias (según nuestra discusión)
import compilador.parser.declarations.*; 
import compilador.parser.statements.*;
import compilador.parser.expressions.*;
import compilador.parser.ASTNode; // Para tipos genéricos como then/else/body

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class InterCodeGenerator implements ASTVisitor {

    private final List<Cuadrupla> code = new ArrayList<>();
    private int tempCounter = 0;
    private int labelCounter = 0;
    
    // Pila para manejar el retorno de funciones (etiquetas de retorno)
    private final Stack<String> returnLabels = new Stack<>(); 

    // Métodos auxiliares para nombres únicos
    private String newTemp() { return "t" + (tempCounter++); }
    private String newLabel() { return "L" + (labelCounter++); }

    private void emit(String op, String arg1, String arg2, String res) {
        code.add(new Cuadrupla(op, arg1, arg2, res));
    }

    public List<Cuadrupla> getCode() {
        return code;
    }

    public void generate(Program program) {
        program.accept(this);
    }
    
    // =================================================================
    // I. NODOS DE DECLARACIÓN
    // =================================================================

    @Override
    public Object visit(Program node) {
        // Generación de código para cada función
        for (FunctionDeclaration func : node.functions) {
            func.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(FunctionDeclaration node) {
        // 1. Etiqueta de inicio de función
        emit("LABEL", node.id, "null", "null");
        
        // 2. Etiqueta de retorno y push a la pila
        String returnLabel = newLabel();
        returnLabels.push(returnLabel);
        
        // 3. Declaración de parámetros (opcional, pero ayuda al seguimiento)
        int paramCount = 0;
        for (Parameter param : node.parameters) {
            // Asignar parámetros de entrada a variables locales/temporales
            emit("PARAM_IN", String.valueOf(paramCount++), "null", param.id);
        }

        // 4. Visitar el cuerpo
        node.body.accept(this);
        
        // 5. Etiqueta de retorno y limpieza
        returnLabels.pop();
        emit("LABEL", returnLabel, "null", "null");
        emit("END_FUNCTION", "null", "null", "null");
        
        return null;
    }
    
    // No genera cuádrupla, ya que solo es parte de FunctionDeclaration
    @Override
    public Object visit(Parameter node) { return null; }

    // =================================================================
    // II. NODOS DE SENTENCIAS (FLUJO DE CONTROL)
    // =================================================================

    @Override
    public Object visit(BlockStatement node) {
        for (ASTNode stmt : node.statements) {
            stmt.accept(this);
        }
        return null;
    }
    
    @Override
    public Object visit(DeclarationStatement node) {
        if (node.initialValue != null) {
            // Si hay inicialización: ID = <Expression>
            String result = (String) node.initialValue.accept(this);
            // Cuádrupla de asignación: (ASSIGN, resultado_expr, null, ID)
            emit("ASSIGN", result, "null", node.id);
        }
        // Si no hay inicialización, no se genera código intermedio.
        return null;
    }

    @Override
    public Object visit(AssignmentStatement node) {
        // 1. Generar código para la expresión (lado derecho)
        String result = (String) node.value.accept(this);
        
        // 2. Cuádrupla de asignación: (ASSIGN, resultado_expr, null, ID)
        emit("ASSIGN", result, "null", node.id);
        return null;
    }
    
    @Override
    public Object visit(IfStatement node) {
        String conditionResult = (String) node.condition.accept(this);
        String labelFalse = newLabel();
        String labelEnd = newLabel();

        // JUMPIF_FALSE t_cond, null, L_FALSE
        emit("JUMPIF_FALSE", conditionResult, "null", labelFalse);

        // Bloque THEN
        node.thenBranch.accept(this);
        
        // Si hay ELSE, saltar el bloque ELSE
        if (node.elseBranch != null) {
            emit("GOTO", labelEnd, "null", "null");
        }
        
        // Etiqueta ELSE/FIN del IF (si no hay ELSE, es el fin)
        emit("LABEL", labelFalse, "null", "null");
        
        // Bloque ELSE
        if (node.elseBranch != null) {
            node.elseBranch.accept(this);
            // Etiqueta de fin solo si había ELSE
            emit("LABEL", labelEnd, "null", "null"); 
        }
        
        return null;
    }

    @Override
    public Object visit(WhileStatement node) {
        String labelStart = newLabel();
        String labelEnd = newLabel();

        // 1. Etiqueta de inicio del ciclo
        emit("LABEL", labelStart, "null", "null"); 

        // 2. Generar código para la condición
        String conditionResult = (String) node.condition.accept(this);

        // 3. JUMP al final si la condición es falsa
        // JUMPIF_FALSE t_cond, null, L_END
        emit("JUMPIF_FALSE", conditionResult, "null", labelEnd);

        // 4. Cuerpo del ciclo
        node.body.accept(this);
        
        // 5. GOTO de vuelta al inicio
        emit("GOTO", labelStart, "null", "null");
        
        // 6. Etiqueta de fin del ciclo
        emit("LABEL", labelEnd, "null", "null");
        
        return null;
    }

    @Override
    public Object visit(ReturnStatement node) {
        // 1. Visitar la expresión de retorno
        String result = (String) node.value.accept(this);
        
        // 2. Cuádrupla de RETORNO (el ensamblador usará esto para limpiar la pila)
        emit("RETURN", result, "null", "null");
        
        // 3. GOTO a la etiqueta de fin de función (limpieza de stack, etc.)
        if (!returnLabels.isEmpty()) {
            emit("GOTO", returnLabels.peek(), "null", "null");
        }
        
        return null;
    }

    // =================================================================
    // III. NODOS DE EXPRESIONES (Devuelven el identificador del resultado)
    // =================================================================

    @Override
    public Object visit(BinaryExpression node) {
        // 1. Visitar lados izquierdo y derecho
        String leftResult = (String) node.left.accept(this); 
        String rightResult = (String) node.right.accept(this);

        // 2. Generar nuevo temporal para el resultado
        String resultTemp = newTemp();
        
        // 3. Emitir cuádrupla: (OP, op1, op2, res)
        emit(node.operator.getLexeme(), leftResult, rightResult, resultTemp);
        
        return resultTemp; 
    }
    
    @Override
    public Object visit(UnaryExpression node) {
        // 1. Visitar el operando
        String operandResult = (String) node.operand.accept(this);
        
        // 2. Generar temporal
        String resultTemp = newTemp();
        
        // 3. Emitir cuádrupla: (OP, operando, null, res)
        emit(node.operator.getLexeme(), operandResult, "null", resultTemp);
        
        return resultTemp; 
    }
    
    @Override
    public Object visit(FunctionCall node) {
        // 1. Generar cuádruplas para los argumentos
        for (Expression arg : node.arguments) {
            String argResult = (String) arg.accept(this);
            // PARAM arg_result, null, null, null
            emit("PARAM", argResult, "null", "null"); 
        }
        
        // 2. Generar temporal para almacenar el valor de retorno
        String resultTemp = newTemp();
        
        // 3. Cuádrupla de llamada: (CALL, num_args, null, resultado)
        emit("CALL", String.valueOf(node.arguments.size()), "null", resultTemp);
        
        return resultTemp;
    }
    
    @Override
    public Object visit(VariableAccess node) {
        // No se genera código; simplemente se devuelve el nombre de la variable
        return node.id; 
    }
    
    @Override
    public Object visit(LiteralExpression node) {
        // No se genera código; se devuelve el valor literal como String
        return node.value.toString(); 
    }
}