package compilador.parser.expressions;

import compilador.parser.ASTNode; // Importa el padre


public abstract class Expression extends ASTNode { 
    
    public Expression(int line) {
        super(line); // Llama al constructor del padre
    }
}