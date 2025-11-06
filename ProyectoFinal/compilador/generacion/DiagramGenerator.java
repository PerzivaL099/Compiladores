package compilador.generacion;

import compilador.parser.ASTVisitor;
import compilador.parser.ASTNode;
// Importar todos los nodos AST
import compilador.parser.declarations.*; 
import compilador.parser.expressions.*;
import compilador.parser.statements.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Genera código DOT (Graphviz) a partir del AST para crear Diagramas de Flujo.
 * Implementa todos los métodos de ASTVisitor.
 */
@SuppressWarnings("rawtypes") // Suprimir advertencias de tipo crudo
public class DiagramGenerator implements ASTVisitor {

    private final StringBuilder dotCode = new StringBuilder();
    private int nodeCounter = 0;
    
    // Almacena el nombre del nodo de INICIO y FIN de un bloque de sentencias
    // <Key: Nodo AST (ej. IfStatement), Value: [Nodo_Inicio, Nodo_Fin]>
    private final Map<ASTNode, String[]> nodeRegistry = new HashMap<>();

    public void generate(Program program, String filename) throws IOException {
        dotCode.append("digraph FlowChart {\n");
        dotCode.append("\trankdir=TB; // De Arriba a Abajo\n");
        dotCode.append("\tnode [shape=box, style=\"rounded\"];\n"); 
        
        // Iniciar la visita
        program.accept(this);

        dotCode.append("}\n");

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(dotCode.toString());
        }
    }
    
    // --- Utilidades DOT ---
    
    private String getNextNodeName() {
        return "N" + (nodeCounter++);
    }
    
    // Define el nodo en DOT
    private void defineNode(String name, String label, String shape, String color) {
        label = label.replace("\"", "'").replace("\n", "\\n").replace(";", ""); 
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
        String startNode = getNextNodeName();
        defineNode(startNode, "START", "oval", "black");
        
        String lastNode = startNode;
        for (FunctionDeclaration func : node.functions) {
            String funcStart = (String) func.accept(this);
            defineEdge(lastNode, funcStart, "");
            
            // Obtenemos el nodo final del cuerpo de la función
            String funcEnd = nodeRegistry.get(func.body)[1];
            lastNode = funcEnd; 
        }
        
        String endNode = getNextNodeName();
        defineNode(endNode, "END", "oval", "black");
        defineEdge(lastNode, endNode, "");
        return null;
    }

    @Override
    public Object visit(FunctionDeclaration node) {
        String funcName = getNextNodeName();
        defineNode(funcName, "FUNCIÓN: " + node.id, "invhouse", "blue");
        
        // Visitamos el cuerpo (que es un BlockStatement)
        String bodyStart = (String) node.body.accept(this);
        defineEdge(funcName, bodyStart, "Entry");
        
        return funcName; // Devolvemos el nombre del nodo de INICIO de esta función
    }

    @Override
    public Object visit(Parameter node) { return null; } // No se dibujan
    
    // =================================================================
    // II. SENTENCIAS (FLUJO Y SECUENCIA)
    // =NOTAS: 
    // - Los métodos visit(Sentencia) DEBEN devolver el nombre del NODO DE INICIO.
    // - También DEBEN registrar su NODO DE FIN en nodeRegistry.
    // =================================================================

    @Override
    public Object visit(BlockStatement node) {
        String startNode = getNextNodeName();
        String endNode = getNextNodeName();
        
        // Registra el inicio y el fin del bloque
        nodeRegistry.put(node, new String[]{startNode, endNode});
        
        // Nodo "fantasma" de inicio de bloque
        defineNode(startNode, "BlockStart", "point", "white");
        defineNode(endNode, "BlockEnd", "point", "white");

        String lastNode = startNode;
        
        for (ASTNode stmt : node.statements) {
            String stmtStart = (String) stmt.accept(this); // Visita y obtiene la etiqueta de inicio
            defineEdge(lastNode, stmtStart, "");
            
            // Obtenemos el nodo final de la sentencia que acabamos de visitar
            lastNode = nodeRegistry.get(stmt)[1];
        }
        
        // Conectar el último nodo del bloque al nodo final del bloque
        defineEdge(lastNode, endNode, "");
        
        return startNode; // Devuelve el nodo de INICIO
    }

    @Override
    public Object visit(DeclarationStatement node) {
        String name = getNextNodeName();
        String label = node.type + " " + node.id + 
                       (node.initialValue != null ? " = ..." : "");
        defineNode(name, label, "box", "green");
        
        // El inicio y el fin son el mismo nodo
        nodeRegistry.put(node, new String[]{name, name}); 
        return name;
    }

    @Override
    public Object visit(AssignmentStatement node) {
        String name = getNextNodeName();
        String label = String.format("%s = ...", node.id); 
        defineNode(name, label, "box", "black");
        
        nodeRegistry.put(node, new String[]{name, name});
        return name;
    }
    
    @Override
    public Object visit(IfStatement node) {
        String condNode = getNextNodeName();
        defineNode(condNode, "IF: ...", "diamond", "red"); 

        String joinNode = getNextNodeName(); // Punto de confluencia
        defineNode(joinNode, "", "point", "white"); 
        
        nodeRegistry.put(node, new String[]{condNode, joinNode});

        // Rama THEN
        String thenStart = (String) node.thenBranch.accept(this);
        String thenEnd = nodeRegistry.get(node.thenBranch)[1];
        defineEdge(condNode, thenStart, "True");
        defineEdge(thenEnd, joinNode, "");

        // Rama ELSE
        if (node.elseBranch != null) {
            String elseStart = (String) node.elseBranch.accept(this);
            String elseEnd = nodeRegistry.get(node.elseBranch)[1];
            defineEdge(condNode, elseStart, "False");
            defineEdge(elseEnd, joinNode, "");
        } else {
            defineEdge(condNode, joinNode, "False");
        }
        
        return condNode; // Devuelve el nodo de inicio del IF
    }

    @Override
    public Object visit(WhileStatement node) {
        String condNode = getNextNodeName();
        defineNode(condNode, "WHILE: ...", "diamond", "red"); 

        String joinNode = getNextNodeName(); // Nodo de salida (si es Falso)
        defineNode(joinNode, "", "point", "white");
        
        nodeRegistry.put(node, new String[]{condNode, joinNode});

        // Visitar cuerpo
        String bodyStart = (String) node.body.accept(this);
        String bodyEnd = nodeRegistry.get(node.body)[1];
        
        // Conexiones de flujo
        defineEdge(condNode, bodyStart, "True");
        defineEdge(bodyEnd, condNode, "Loop"); // Bucle de vuelta a la condición
        defineEdge(condNode, joinNode, "False"); // Salida del bucle
        
        return condNode; 
    }

    @Override
    public Object visit(ReturnStatement node) {
        String name = getNextNodeName();
        defineNode(name, "RETURN ...", "box", "orange");
        nodeRegistry.put(node, new String[]{name, name});
        return name;
    }

    // =================================================================
    // III. EXPRESIONES (No se dibujan, solo se implementa el stub)
    // =================================================================

    @Override
    public Object visit(BinaryExpression node) { return null; }
    @Override
    public Object visit(UnaryExpression node) { return null; }
    @Override
    public Object visit(FunctionCall node) { return null; }
    @Override
    public Object visit(VariableAccess node) { return null; }
    @Override
    public Object visit(LiteralExpression node) { return null; }
}