package compilador.parser.statements;

import compilador.parser.ASTVisitor;
import compilador.parser.expressions.Expression;

public class AssignmentStatement extends Statement {

    public final String id;
    public final Expression value;

    public AssignmentStatement(int line, String id, Expression value) {
        super(line);
        this.id = id;
        this.value = value;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}