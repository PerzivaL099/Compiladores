package compilador.parser.statements;

import compilador.parser.ASTNode; // Para thenBranch y elseBranch
import compilador.parser.ASTVisitor;
import compilador.parser.expressions.Expression; // Para la condición

public class IfStatement extends Statement {

    public final Expression condition;
    public final ASTNode thenBranch;
    public final ASTNode elseBranch; // Puede ser null

    public IfStatement(int line, Expression condition, ASTNode thenBranch, ASTNode elseBranch) {
        super(line);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}