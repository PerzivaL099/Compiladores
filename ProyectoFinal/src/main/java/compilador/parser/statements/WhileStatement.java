package compilador.parser.statements;

import compilador.parser.ASTNode;
import compilador.parser.ASTVisitor;
import compilador.parser.expressions.Expression;

public class WhileStatement extends Statement {

    public final Expression condition;
    public final ASTNode body;

    public WhileStatement(int line, Expression condition, ASTNode body) {
        super(line);
        this.condition = condition;
        this.body = body;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}