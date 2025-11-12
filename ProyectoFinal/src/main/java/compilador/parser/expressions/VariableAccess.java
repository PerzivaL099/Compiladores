package compilador.parser.expressions;

import compilador.parser.ASTVisitor;

public class VariableAccess extends Expression {

    public final String id;

    public VariableAccess(int line, String id) {
        super(line);
        this.id = id;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}