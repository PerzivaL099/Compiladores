package compilador.semantico;

import java.util.List;

import compilador.parser.ASTNode;
import compilador.parser.ASTVisitor;
// Importar todos los nodos del AST (¡incluyendo Program!)
import compilador.parser.declarations.*;
import compilador.parser.expressions.*;
import compilador.parser.statements.*;

/**
 * Fase 3: Analizador Semántico.
 * Implementa ASTVisitor para recorrer el AST y realizar:
 * 1. Chequeo de Tipos (Type Checking).
 * 2. Chequeo de Ámbitos/Declaraciones (Scope/Declaration Checking).
 */
public class SemanticAnalyzer implements ASTVisitor {

    private final TablaSimbolos tablaSimbolos;
    
    // Almacena el tipo de retorno esperado de la función actual
    private String currentFunctionReturnType = "void"; 

    public SemanticAnalyzer() {
        this.tablaSimbolos = new TablaSimbolos();
    }
    
    // Método principal llamado desde Main.java
    // Este método ahora es válido porque "Program" está importado.
    public void analyze(Program ast) {
        System.out.println("-> Analizando Semánticamente...");
        ast.accept(this);
    }
    
    // --- Utilidades de Reporte de Errores ---

    private void reportError(int line, String message) {
        // Lanza una excepción para detener la compilación ante un error lógico
        throw new RuntimeException("[Error Semántico en línea " + line + "] " + message);
    }
    
    

    // =================================================================
    // I. NODOS DE DECLARACIÓN
    // =================================================================

    @Override
    public Object visit(Program node) {
        // 1. Primera pasada: registrar todas las firmas de funciones
        for (FunctionDeclaration func : node.functions) {
            
            tablaSimbolos.declareFunction(func);
        }
        
        // 2. Segunda pasada: visitar el cuerpo de cada función
        for (FunctionDeclaration func : node.functions) {
            this.currentFunctionReturnType = func.returnType;
            tablaSimbolos.openScope(func.id); // Abrir ámbito de función
            
            // Declarar parámetros en este nuevo ámbito
            for (Parameter param : func.parameters) {
                if (tablaSimbolos.isDeclaredInCurrentScope(param.id)) {
                    reportError(param.getLine(), "El parámetro '" + param.id + "' ya está definido.");
                }
                tablaSimbolos.declareVariable(param.id, param.type);
            }
            
            // Visitar el cuerpo
            func.body.accept(this);
            
            tablaSimbolos.closeScope(); // Cerrar ámbito de función
        }
        this.currentFunctionReturnType = "void";
        return null;
    }

    @Override
    public Object visit(FunctionDeclaration node) {
        // La lógica principal se maneja en visit(Program)
        return null; 
    }

    @Override
    public Object visit(Parameter node) {
        // La lógica principal se maneja en visit(Program)
        return null;
    }
    
    // =================================================================
    // II. NODOS DE SENTENCIAS (FLUJO DE CONTROL)
    // =================================================================

    @Override
    public Object visit(BlockStatement node) {
        tablaSimbolos.openScope(); // Abrir ámbito para { ... }
        for (ASTNode statement : node.statements) {
            statement.accept(this);
        }
        tablaSimbolos.closeScope(); // Cerrar ámbito
        return null;
    }

    @Override
    public Object visit(DeclarationStatement node) {
        int line = node.getLine();
        
        if (tablaSimbolos.isDeclaredInCurrentScope(node.id)) {
            reportError(line, "La variable '" + node.id + "' ya está definida en este ámbito.");
        }
        
        // Chequeo de tipo si hay inicialización
        if (node.initialValue != null) {
            String exprType = (String) node.initialValue.accept(this); // Visita la expresión
            if (!exprType.equals(node.type)) {
                reportError(line, "Tipos incompatibles. No se puede inicializar '" + node.type + "' con un valor de tipo '" + exprType + "'.");
            }
        }
        
        tablaSimbolos.declareVariable(node.id, node.type); // Declarar la variable
        return null;
    }
    
    @Override
    public Object visit(AssignmentStatement node) {
        int line = node.getLine();
        
        // 1. Verificar que la variable exista
        String varType = tablaSimbolos.lookupVariable(node.id);
        if (varType == null) {
            reportError(line, "La variable '" + node.id + "' no ha sido declarada.");
        }
        
        // 2. Verificar el tipo de la expresión
        String exprType = (String) node.value.accept(this);
        
        // 3. Verificar compatibilidad
        if (!exprType.equals(varType)) {
            reportError(line, "Tipos incompatibles. No se puede asignar '" + exprType + "' a la variable '" + node.id + "' de tipo '" + varType + "'.");
        }
        return null;
    }

    @Override
    public Object visit(IfStatement node) {
        String conditionType = (String) node.condition.accept(this);
        if (!conditionType.equals("boolean")) {
            reportError(node.getLine(), "La condición del IF debe ser de tipo boolean, pero se encontró '" + conditionType + "'.");
        }
        
        node.thenBranch.accept(this);
        if (node.elseBranch != null) {
            node.elseBranch.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(WhileStatement node) {
        String conditionType = (String) node.condition.accept(this);
        if (!conditionType.equals("boolean")) {
            reportError(node.getLine(), "La condición del WHILE debe ser de tipo boolean, pero se encontró '" + conditionType + "'.");
        }
        
        node.body.accept(this);
        return null;
    }
    
    @Override
    public Object visit(ReturnStatement node) {
        String returnExprType = (String) node.value.accept(this);
        
        if (!returnExprType.equals(this.currentFunctionReturnType)) {
            reportError(node.getLine(), "Tipo de retorno incompatible. Se esperaba '" + this.currentFunctionReturnType + "' pero se encontró '" + returnExprType + "'.");
        }
        return null;
    }

    // =================================================================
    // III. NODOS DE EXPRESIÓN (Devuelven el tipo como String)
    // =================================================================

    @Override
    public Object visit(BinaryExpression node) {
        int line = node.getLine();
        String leftType = (String) node.left.accept(this);
        String rightType = (String) node.right.accept(this);
        String op = node.operator.getLexeme();
        
        switch (op) {
            case "+": case "-": case "*": case "/":
                if (!leftType.equals("int") || !rightType.equals("int")) {
                    reportError(line, "Los operandos de '" + op + "' deben ser de tipo int.");
                }
                return "int";
            
            case "&&": case "||":
                if (!leftType.equals("boolean") || !rightType.equals("boolean")) {
                    reportError(line, "Los operandos de '" + op + "' deben ser de tipo boolean.");
                }
                return "boolean";
            
            case "==": case "!=":
                if (!leftType.equals(rightType)) {
                    reportError(line, "Los operandos de '" + op + "' deben ser del mismo tipo.");
                }
                return "boolean"; // El resultado de la comparación es siempre boolean
            
            case "<": case ">": case "<=": case ">=":
                if (!leftType.equals("int") || !rightType.equals("int")) {
                    reportError(line, "Los operandos de '" + op + "' deben ser de tipo int.");
                }
                return "boolean";
            
            default:
                reportError(line, "Operador binario desconocido: " + op);
                return null;
        }
    }

    @Override
    public Object visit(UnaryExpression node) {
        int line = node.getLine();
        String op = node.operator.getLexeme();
        String operandType = (String) node.operand.accept(this);
        
        if (op.equals("!")) {
            if (!operandType.equals("boolean")) {
                reportError(line, "El operador '!' solo se aplica a booleanos.");
            }
            return "boolean";
        }
        
        if (op.equals("-")) {
            if (!operandType.equals("int")) {
                reportError(line, "El operador unario '-' solo se aplica a int.");
            }
            return "int";
        }
        
        reportError(line, "Operador unario desconocido: " + op);
        return null;
    }
    
    @Override
    public Object visit(FunctionCall node) {
        int line = node.getLine();
        FunctionDeclaration funcDecl = tablaSimbolos.lookupFunction(node.id);
        
        if (funcDecl == null) {
            reportError(line, "La función '" + node.id + "' no ha sido declarada.");
        }
        
        if (funcDecl.parameters.size() != node.arguments.size()) {
            reportError(line, "Llamada a '" + node.id + "' requiere " + funcDecl.parameters.size() + " argumentos, pero se proveyeron " + node.arguments.size() + ".");
        }
        
        for (int i = 0; i < node.arguments.size(); i++) {
            String argType = (String) node.arguments.get(i).accept(this);
            String expectedType = funcDecl.parameters.get(i).type;
            if (!argType.equals(expectedType)) {
                reportError(line, "Argumento #" + (i + 1) + " de '" + node.id + "': se esperaba '" + expectedType + "' pero se encontró '" + argType + "'.");
            }
        }
        
        return funcDecl.returnType; // El tipo de la llamada es el tipo de retorno de la función
    }

    @Override
    public Object visit(VariableAccess node) {
        String varType = tablaSimbolos.lookupVariable(node.id);
        if (varType == null) {
            reportError(node.getLine(), "La variable '" + node.id + "' no ha sido declarada.");
        }
        return varType;
    }

    @Override
    public Object visit(LiteralExpression node) {
        if (node.value instanceof Integer) {
            return "int";
        }
        if (node.value instanceof Boolean) {
            return "boolean";
        }
        reportError(node.getLine(), "Tipo de literal desconocido.");
        return null;
    }
}