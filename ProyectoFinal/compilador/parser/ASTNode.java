package compilador.parser;

/**
 * Clase base abstracta para todos los nodos del AST.
 * Define la estructura mínima y el método ACCEPT para el Patrón Visitor.
 */
public abstract class ASTNode {
    protected int line;
    
    public ASTNode(int line) {
        this.line = line;
    }
    
    // Método abstracto obligatorio para el Patrón Visitor.
    // OBLIGA a todas las subclases (IfStatement, BinaryExpression, etc.) 
    // a implementar su propia lógica de aceptación.
    public abstract Object accept(ASTVisitor visitor); 
    
    // Getter
    public int getLine() {
        return line;
    }
}