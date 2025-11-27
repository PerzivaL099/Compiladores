package compilador.parser.declarations;

import compilador.parser.ASTNode;
import compilador.parser.ASTVisitor;

/**
 * Representa la declaración de un parámetro en la firma de una función.
 */
public class Parameter extends ASTNode {

    public final String type;
    public final String id;

    public Parameter(int line, String type, String id) {
        super(line);
        this.type = type;
        this.id = id;
    }

    // Implementación del Patrón Visitor
    @Override
    public Object accept(ASTVisitor visitor) {
        
        return visitor.visit(this); 
    }
    
    // Getters
    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }
}
