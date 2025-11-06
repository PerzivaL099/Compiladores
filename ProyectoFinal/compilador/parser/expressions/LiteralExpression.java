package compilador.parser.expressions;

import compilador.parser.ASTVisitor;

public class LiteralExpression extends Expression {

    public final Object value; // Puede ser Integer o Boolean

    public LiteralExpression(int line, Object value) {
        super(line);
        this.value = value;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}