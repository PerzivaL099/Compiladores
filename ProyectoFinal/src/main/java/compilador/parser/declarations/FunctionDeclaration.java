package compilador.parser.declarations;

import compilador.parser.ASTNode;
import compilador.parser.ASTVisitor;
import compilador.parser.statements.BlockStatement;

import java.util.List;

/**
 * Representa la declaración de una función en el código fuente.
 */
public class FunctionDeclaration extends ASTNode {

    public final String returnType;
    public final String id;
    public final List<Parameter> parameters;
    public final BlockStatement body;

    public FunctionDeclaration(int line, String returnType, String id, List<Parameter> parameters, BlockStatement body) {
        super(line);
        this.returnType = returnType;
        this.id = id;
        this.parameters = parameters;
        this.body = body;
    }

    // Implementación del Patrón Visitor
    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
    
    // Getters para acceder a los campos (opcional, pero buena práctica)
    public String getId() {
        return id;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public BlockStatement getBody() {
        return body;
    }
}