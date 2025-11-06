package compilador.parser.expressions;

import compilador.parser.ASTNode;

/**
 * Clase base abstracta para todos los nodos que representan Expresiones (valores computables).
 */
public abstract class Expression extends ASTNode { 
    
    public Expression(int line) {
        super(line);
    }
}