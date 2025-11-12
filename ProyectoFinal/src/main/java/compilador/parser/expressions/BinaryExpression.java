package compilador.parser.expressions;

import compilador.parser.ASTVisitor;
import compilador.lexer.Token;

public class BinaryExpression extends Expression {

    public final Expression left;
    public final Token operator;
    public final Expression right;

    public BinaryExpression(int line, Token operator, Expression left, Expression right) {
        super(line);
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}