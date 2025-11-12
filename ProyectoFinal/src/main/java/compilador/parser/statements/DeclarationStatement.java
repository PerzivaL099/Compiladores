package compilador.parser.statements;

import compilador.parser.ASTVisitor;
import compilador.parser.expressions.Expression;

public class DeclarationStatement extends Statement {

    public final String type;
    public final String id;
    public final Expression initialValue; // Puede ser null

    public DeclarationStatement(int line, String type, String id, Expression initialValue) {
        super(line);
        this.type = type;
        this.id = id;
        this.initialValue = initialValue;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}