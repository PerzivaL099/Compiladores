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

    // --- INICIO DE CORRECCIÓN 1: Mover variables de estado ---
    // Se mueven de 'final' para poder reiniciarlas en cada llamada
    private StringBuilder dotCode;
    private int nodeCounter;
    private Map<ASTNode, String[]> nodeRegistry;
    // --- FIN DE CORRECCIÓN 1 ---

    // --- INICIO DE CORRECCIÓN 2: Nuevo método para el Servidor Web ---
    /**
     * Genera el código DOT como un String, sin escribir a disco.
     * Este método es llamado por CompilerService.
     * @param program El nodo raíz del AST
     * @return El código DOT como un String.
     */
    public String generateDotString(Program program) {
        // Reiniciar el estado para cada compilación (¡CRUCIAL para un servidor!)
        this.dotCode = new StringBuilder();
        this.nodeCounter = 0;
        this.nodeRegistry = new HashMap<>();

        // Lógica de generación (movida desde el método 'generate')
        dotCode.append("digraph FlowChart {\n");
        dotCode.append("\trankdir=TB; // De Arriba a Abajo\n");
        dotCode.append("\tnode [shape=box, style=\"rounded\"];\n"); 
        
        // Iniciar la visita
        program.accept(this);

        dotCode.append("}\n");
        
        return dotCode.toString();
    }
    // --- FIN DE CORRECCIÓN 2 ---

    // --- INICIO DE CORRECCIÓN 3: Refactorizar método antiguo ---
    /**
     * Genera el código DOT y lo escribe a un archivo.
     * (Usado por el Main.java de línea de comandos original)
     */
    public void generate(Program program, String filename) throws IOException {
        // Ahora llama al nuevo método para obtener el string
        String dotResult = this.generateDotString(program);
        
        // Escribe el string resultante al archivo
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(dotResult);
        }
        
        // (La salida de consola se movió a Main.java, pero puede quedar aquí)
    }
    // --- FIN DE CORRECCIÓN 3 ---
    
    // --- Utilidades DOT ---
    
    private String getNextNodeName() {
        return "N" + (nodeCounter++);
    }
    
    private void defineNode(String name, String label, String shape, String color) {
        label = label.replace("\"", "'").replace("\n", "\\n").replace(";", ""); 
        dotCode.append(String.format("\t%s [label=\"%s\", shape=%s, color=\"%s\"];\n", name, label, shape, color));
    }
    
    private void defineEdge(String from, String to, String label) {
        dotCode.append(String.format("\t%s -> %s [label=\"%s\"];\n", from, to, label));
    }

    // --- MÉTODOS DE VISITA (Sin cambios) ---
    // (Tu implementación completa de todos los métodos 'visit' va aquí)

    @Override
    public Object visit(Program node) {
        String startNode = getNextNodeName();
        defineNode(startNode, "START", "oval", "black");
        
        String lastNode = startNode;
        for (FunctionDeclaration func : node.functions) {
            String funcStart = (String) func.accept(this);
            defineEdge(lastNode, funcStart, "");
            
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
        
        String bodyStart = (String) node.body.accept(this);
        defineEdge(funcName, bodyStart, "Entry");
        
        return funcName;
    }

    @Override
    public Object visit(Parameter node) { return null; }
    
    @Override
    public Object visit(BlockStatement node) {
        String startNode = getNextNodeName();
        String endNode = getNextNodeName();
        nodeRegistry.put(node, new String[]{startNode, endNode});
        
        defineNode(startNode, "BlockStart", "point", "white");
        defineNode(endNode, "BlockEnd", "point", "white");

        String lastNode = startNode;
        
        for (ASTNode stmt : node.statements) {
            String stmtStart = (String) stmt.accept(this);
            defineEdge(lastNode, stmtStart, "");
            lastNode = nodeRegistry.get(stmt)[1];
        }
        
        defineEdge(lastNode, endNode, "");
        return startNode;
    }

    @Override
    public Object visit(DeclarationStatement node) {
        String name = getNextNodeName();
        String label = node.type + " " + node.id + 
                       (node.initialValue != null ? " = ..." : "");
        defineNode(name, label, "box", "green");
        
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

        String joinNode = getNextNodeName();
        defineNode(joinNode, "", "point", "white"); 
        
        nodeRegistry.put(node, new String[]{condNode, joinNode});

        String thenStart = (String) node.thenBranch.accept(this);
        String thenEnd = nodeRegistry.get(node.thenBranch)[1];
        defineEdge(condNode, thenStart, "True");
        defineEdge(thenEnd, joinNode, "");

        if (node.elseBranch != null) {
            String elseStart = (String) node.elseBranch.accept(this);
            String elseEnd = nodeRegistry.get(node.elseBranch)[1];
            defineEdge(condNode, elseStart, "False");
            defineEdge(elseEnd, joinNode, "");
        } else {
            defineEdge(condNode, joinNode, "False");
        }
        
        return condNode;
    }

    @Override
    public Object visit(WhileStatement node) {
        String condNode = getNextNodeName();
        defineNode(condNode, "WHILE: ...", "diamond", "red"); 

        String joinNode = getNextNodeName(); 
        defineNode(joinNode, "", "point", "white");
        
        nodeRegistry.put(node, new String[]{condNode, joinNode});

        String bodyStart = (String) node.body.accept(this);
        String bodyEnd = nodeRegistry.get(node.body)[1];
        
        defineEdge(condNode, bodyStart, "True");
        defineEdge(bodyEnd, condNode, "Loop"); 
        defineEdge(condNode, joinNode, "False"); 
        
        return condNode; 
    }

    @Override
    public Object visit(ReturnStatement node) {
        String name = getNextNodeName();
        defineNode(name, "RETURN ...", "box", "orange");
        nodeRegistry.put(node, new String[]{name, name});
        return name;
    }

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