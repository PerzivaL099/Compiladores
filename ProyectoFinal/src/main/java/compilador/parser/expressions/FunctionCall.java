package compilador.parser.expressions;

import compilador.parser.ASTVisitor;
import java.util.List;
import java.util.ArrayList;

/**
 * Representa la llamada a una función (ej. func(a, b)).
 * Hereda de Expression.
 */
public class FunctionCall extends Expression {

    public final String id;
    public final List<Expression> arguments;

    public FunctionCall(int line, String id, List<Expression> arguments) {
        super(line);
        this.id = id;
        this.arguments = (arguments != null) ? arguments : new ArrayList<>();
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}