package compilador.generacion;

import compilador.parser.ASTVisitor;
import compilador.parser.ASTNode;
import compilador.parser.declarations.*; 
import compilador.parser.statements.*;
import compilador.parser.expressions.*;

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
        dotCode.append("\tnode [shape=box];\n"); // Nodos por defecto: Rectángulos
        
        program.accept(this);

        dotCode.append("}\n");

        // 3. Escribir el código DOT a un archivo
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(dotCode.toString());
        }
        System.out.println("Diagrama DOT generado en: " + filename);
        // Nota: El usuario deberá ejecutar Graphviz por separado para renderizar el PNG/SVG.
    }
    
    // Método auxiliar para obtener un nombre único de nodo DOT
    private String getNodeName(ASTNode node) {
        if (!nodeNames.containsKey(node)) {
            String name = "N" + (nodeCounter++);
            nodeNames.put(node, name);
            return name;
        }
        return nodeNames.get(node);
    }
    
    // Método auxiliar para definir un nodo DOT
    private void defineNode(String name, String label, String shape) {
        dotCode.append(String.format("\t%s [label=\"%s\", shape=%s];\n", name, label.replace("\"", "'"), shape));
    }
    
    // Método auxiliar para definir una arista/conexión
    private void defineEdge(String from, String to, String label) {
        dotCode.append(String.format("\t%s -> %s [label=\"%s\"];\n", from, to, label));
    }

    // =================================================================
    // I. NODOS DE DECLARACIÓN (Solo definen estructura de alto nivel)
    // =================================================================

    @Override
    public Object visit(Program node) {
        // Un nodo inicial de programa
        String startNode = "START";
        defineNode(startNode, "PROGRAM START", "oval");

        String lastNode = startNode;
        for (FunctionDeclaration func : node.functions) {
            String funcNode = (String) func.accept(this);
            // Opcional: conectar funciones, pero generalmente se dibujan separadas
            if (funcNode != null) {
                defineEdge(lastNode, funcNode, "Define " + func.id);
                lastNode = funcNode;
            }
        }
        // Nodo final
        defineNode("END", "PROGRAM END", "oval");
        defineEdge(lastNode, "END", "");
        return null;
    }

    @Override
    public Object visit(FunctionDeclaration node) {
        String funcStart = getNodeName(node);
        defineNode(funcStart, "FUNCTION: " + node.id + " (" + node.returnType + ")", "invhouse"); // Símbolo de inicio

        String bodyStart = (String) node.body.accept(this);
        defineEdge(funcStart, bodyStart, "Entry");

        // El nodo final de la función se definirá en el return o al final del bloque
        return funcStart;
    }
    
    // ... (visit(Parameter node) y otros auxiliares devuelven null o se manejan internamente)
    
    // =================================================================
    // II. NODOS DE SENTENCIAS (FLUJO DE CONTROL)
    // =================================================================

    // Nota: El parser solo conecta sentencias secuenciales. El generador debe manejar la secuencia.
    
    @Override
    public Object visit(BlockStatement node) {
        if (node.statements.isEmpty()) {
            return null; // No hay nada que dibujar
        }
        
        String startNode = getNodeName(node.statements.get(0));
        String lastNode = startNode;
        
        // Conectar sentencias secuencialmente
        for (int i = 0; i < node.statements.size(); i++) {
            ASTNode currentStmt = node.statements.get(i);
            String currentNode = getNodeName(currentStmt);

            // La lógica del visit(currentStmt) crea el nodo si no existe
            currentStmt.accept(this); 
            
            if (i > 0) {
                // Conectar el nodo anterior con el actual (secuencia)
                defineEdge(lastNode, currentNode, "");
            }
            lastNode = currentNode;
        }
        return startNode; // Devuelve el primer nodo del bloque
    }

    @Override
    public Object visit(AssignmentStatement node) {
        String name = getNodeName(node);
        // Asignación: rectangulo de proceso
        String label = String.format("%s = %s", node.id, node.value.toString()); 
        defineNode(name, label, "box"); 
        return name;
    }
    
    @Override
    public Object visit(IfStatement node) {
        String condNode = getNodeName(node);
        // Condicional: rombo de decisión
        defineNode(condNode, node.condition.toString(), "diamond"); 

        String thenStart = (String) node.thenBranch.accept(this);
        String thenEnd = getNodeName(node.thenBranch); // Asumimos que el último nodo es el que devuelve el visit

        defineEdge(condNode, thenStart, "True");
        
        // Nodo de confluencia (donde se une el IF/ELSE)
        String joinNode = getNodeName(node) + "_JOIN"; 
        defineNode(joinNode, "", "circle"); 
        
        // La rama True salta al nodo de confluencia
        defineEdge(thenEnd, joinNode, "");

        if (node.elseBranch != null) {
            String elseStart = (String) node.elseBranch.accept(this);
            String elseEnd = getNodeName(node.elseBranch);

            defineEdge(condNode, elseStart, "False");
            defineEdge(elseEnd, joinNode, "");
        } else {
            // Si no hay ELSE, la rama False salta directamente al JOIN
            defineEdge(condNode, joinNode, "False");
        }
        
        return condNode; // Devuelve el nodo de inicio del IF
    }

    // ... (El resto de métodos visit deben implementarse para definir los nodos
    // aunque la lógica de conexión secuencial se maneja en visit(BlockStatement))
    
    // Los métodos visit de expresiones (BinaryExpression, etc.) generalmente
    // solo devuelven una representación en cadena para usarse en las etiquetas.

    // Métodos stub (requeridos por ASTVisitor)
    @Override public Object visit(DeclarationStatement node) { return getNodeName(node); }
    @Override public Object visit(WhileStatement node) { /* Necesita lógica de loop */ return getNodeName(node); }
    @Override public Object visit(ReturnStatement node) { String name = getNodeName(node); defineNode(name, "RETURN", "box"); return name; }
    @Override public Object visit(BinaryExpression node) { return node.operator.getLexeme(); }
    @Override public Object visit(UnaryExpression node) { return node.operator.getLexeme(); }
    @Override public Object visit(FunctionCall node) { String name = getNodeName(node); defineNode(name, "CALL " + node.id, "box"); return name; }
    @Override public Object visit(VariableAccess node) { return node.id; }
    @Override public Object visit(LiteralExpression node) { return node.value.toString(); }
    @Override public Object visit(Parameter node) { return null; }

    @Override
    public Object visit(compilador.parser.Program node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
}