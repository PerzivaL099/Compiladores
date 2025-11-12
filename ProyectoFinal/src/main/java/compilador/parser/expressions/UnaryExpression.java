package compilador.parser.expressions;

import compilador.parser.ASTVisitor;
import compilador.lexer.Token;

public class UnaryExpression extends Expression {

    public final Token operator;
    public final Expression operand;

    public UnaryExpression(int line, Token operator, Expression operand) {
        super(line);
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}