package parser;

public abstract class ASTNode {
    protected int line;
    
    public ASTNode(int line) {
        this.line = line;
    }
}
