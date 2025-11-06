package compilador.parser.statements;

import compilador.parser.ASTVisitor;
import compilador.parser.expressions.Expression;

/**
 * Representa la sentencia 'return'.
 * Hereda de Statement.
 */
public class ReturnStatement extends Statement {

    // El valor (expresión) que se devuelve. Puede ser null si la función es 'void'.
    public final Expression value; 

    public ReturnStatement(int line, Expression value) {
        super(line);
        this.value = value;
    }

    // Implementación del Patrón Visitor
    @Override
    public Object accept(ASTVisitor visitor) {
        // En la fase de generación de código, esto genera la instrucción RETURN.
        return visitor.visit(this); 
    }
}