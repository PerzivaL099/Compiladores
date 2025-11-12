package compilador.parser;

/**
 * Clase base abstracta para todos los nodos del AST.
 */
@SuppressWarnings("rawtypes") // Suprime la advertencia del Visitor
public abstract class ASTNode {
    
    protected int line;
    
    public ASTNode(int line) {
        this.line = line;
    }
    
    // Método abstracto obligatorio para el Patrón Visitor.
    public abstract Object accept(ASTVisitor visitor); 
    
    public int getLine() {
        return line;
    }
}