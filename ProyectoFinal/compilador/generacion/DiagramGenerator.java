package compilador.generacion;

import compilador.parser.ASTVisitor;
import compilador.parser.ASTNode;
import compilador.parser.declarations.*; 
import compilador.parser.expressions.*;
import compilador.parser.statements.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Genera código DOT (Graphviz) a partir del AST para crear Diagramas de Flujo.
 */
public class DiagramGenerator implements ASTVisitor {

    private final StringBuilder dotCode = new StringBuilder();
    private int nodeCounter = 0;
    private final Map<ASTNode, String> nodeNames = new HashMap<>();

    public void generate(Program program, String filename) throws IOException {
        dotCode.append("digraph FlowChart {\n");
        dotCode.append("\trankdir=TB; // Top to Bottom\n");
        dotCode.append("\tnode [shape=box, style=\"rounded\"];\n"); 
        
        program.accept(this);

        dotCode.append("}\n");

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(dotCode.toString());
        }
        System.out.println("      -> Diagrama DOT generado en: " + filename);
    }
    
    // --- Utilidades DOT ---
    
    // Obtiene o crea un nombre de nodo único
    private String getNodeName(ASTNode node) {
        if (!nodeNames.containsKey(node)) {
            String name = "N" + (nodeCounter++);
            nodeNames.put(node, name);
            return name;
        }
        return nodeNames.get(node);
    }
    
    // Define el nodo en DOT
    private void defineNode(String name, String label, String shape, String color) {
        // Sanitizar label para DOT
        label = label.replace("\"", "'").replace("\n", "\\n").replace(";", "\\;"); 
        dotCode.append(String.format("\t%s [label=\"%s\", shape=%s, color=\"%s\"];\n", name, label, shape, color));
    }
    
    // Define una conexión/arista en DOT
    private void defineEdge(String from, String to, String label) {
        dotCode.append(String.format("\t%s -> %s [label=\"%s\"];\n", from, to, label));
    }

    // --- MÉTODOS DE VISITA (Lógica de Dibujo) ---

    // =================================================================
    // I. DECLARACIONES
    // =================================================================
    
    @Override
    public Object visit(Program node) {
        // Dibujamos START/END si solo compilamos una función principal (main).
        // Si hay múltiples funciones, las visitamos secuencialmente.
        String lastNode = "START_PROGRAM";
        defineNode(lastNode, "START", "oval", "black");
        
        for (FunctionDeclaration func : node.functions) {
            String funcStart = (String) func.accept(this);
            defineEdge(lastNode, funcStart, "Define");
            lastNode = funcStart; // Usamos la etiqueta de inicio de la función como el último nodo visitado.
        }
        
        // Simular el nodo final
        String endNode = "END_PROGRAM";
        defineNode(endNode, "END", "oval", "black");
        defineEdge(lastNode, endNode, "Fin");
        
        return null;
    }

    @Override
    public Object visit(FunctionDeclaration node) {
        String funcName = node.id;
        defineNode(funcName, "FUNCIÓN: " + funcName + "(" + node.returnType + ")", "invhouse", "blue");
        
        // Asume que el cuerpo siempre es un BlockStatement
        node.body.accept(this); 
        
        return funcName; // Devolvemos el nombre de la función (etiqueta de inicio)
    }

    @Override
    public Object visit(Parameter node) { return null; }
    
    // =================================================================
    // II. SENTENCIAS (FLUJO Y SECUENCIA)
    // =================================================================

    @Override
    public Object visit(BlockStatement node) {
        String startNode = getNodeName(node);
        // Definición inicial del nodo
        
        String lastNode = startNode;
        String nextNode = null;

        for (int i = 0; i < node.statements.size(); i++) {
            ASTNode currentStmt = node.statements.get(i);
            String currentNode = (String) currentStmt.accept(this); // Visita y obtiene la etiqueta de inicio

            if (i > 0) {
                // Conectar el nodo anterior (o el fin del bloque anterior) al inicio del nodo actual
                defineEdge(lastNode, currentNode, "");
            }
            
            // Si el nodo actual no es un IF o WHILE, el fin es su propio inicio.
            // (La complejidad de manejo de fin de IF/WHILE se deja para el visit de esos nodos)
            lastNode = currentNode;
        }
        return startNode;
    }

    @Override
    public Object visit(DeclarationStatement node) {
        String name = getNodeName(node);
        String label = node.type + " " + node.id + 
                       (node.initialValue != null ? " = " + node.initialValue.toString() : "");
        defineNode(name, label, "box", "green");
        return name;
    }

    @Override
    public Object visit(AssignmentStatement node) {
        String name = getNodeName(node);
        String label = String.format("%s = %s", node.id, node.value.toString()); 
        defineNode(name, label, "box", "black");
        return name;
    }
    
    @Override
    public Object visit(IfStatement node) {
        String condNode = getNodeName(node);
        defineNode(condNode, "IF: " + node.condition.toString(), "diamond", "red"); 

        String joinNode = getNodeName(node) + "_JOIN"; 
        defineNode(joinNode, "", "circle", "black"); // Punto de confluencia

        // Rama THEN
        String thenStart = (String) node.thenBranch.accept(this);
        defineEdge(condNode, thenStart, "True");
        defineEdge(thenStart, joinNode, ""); // Conecta el final del THEN al JOIN

        // Rama ELSE (si existe)
        if (node.elseBranch != null) {
            String elseStart = (String) node.elseBranch.accept(this);
            defineEdge(condNode, elseStart, "False");
            defineEdge(elseStart, joinNode, ""); // Conecta el final del ELSE al JOIN
        } else {
            // Si no hay ELSE, la rama False salta directamente al JOIN
            defineEdge(condNode, joinNode, "False");
        }
        
        return condNode; // Devuelve el nodo de inicio del IF para la secuencia
    }

    @Override
    public Object visit(WhileStatement node) {
        String condNode = getNodeName(node);
        defineNode(condNode, "WHILE: " + node.condition.toString(), "diamond", "red"); 

        // Visitar cuerpo
        String bodyStart = (String) node.body.accept(this);
        
        // Conexiones de flujo
        defineEdge(condNode, bodyStart, "True");
        defineEdge(bodyStart, condNode, "Loop"); // Bucle de vuelta a la condición
        
        // Necesitamos un nodo de salida. Simplemente devolvemos el nodo condición.
        // El nodo que siga al WHILE se conectará al nodo que sigue al nodo condNode.
        
        return condNode; 
    }

    @Override
    public Object visit(ReturnStatement node) {
        String name = getNodeName(node);
        defineNode(name, "RETURN " + node.value.toString(), "box", "orange");
        return name;
    }

    // =================================================================
    // III. EXPRESIONES (Devuelven el identificador del resultado como String, no dibujan nodos)
    // =================================================================

    @Override
    public Object visit(BinaryExpression node) { return node.left.toString() + node.operator.getLexeme() + node.right.toString(); }
    @Override
    public Object visit(UnaryExpression node) { return node.operator.getLexeme() + node.operand.toString(); }
    @Override
    public Object visit(FunctionCall node) { return "CALL " + node.id + "(...)"; }
    @Override
    public Object visit(VariableAccess node) { return node.id; }
    @Override
    public Object visit(LiteralExpression node) { return node.value.toString(); }
}