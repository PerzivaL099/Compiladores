package compilador.parser.expressions;

import compilador.parser.ASTNode; // Importa el padre

/**
 * Clase base abstracta para todas las Expresiones.
 * Hereda de ASTNode.
 */
public abstract class Expression extends ASTNode { 
    
    public Expression(int line) {
        super(line); // Llama al constructor del padre
    }
}