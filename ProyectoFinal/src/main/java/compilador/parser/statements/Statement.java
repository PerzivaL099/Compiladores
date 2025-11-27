package compilador.parser.statements;

import compilador.parser.ASTNode; // Importa el padre (ASTNode)

/**
 * Clase base abstracta para todas las Sentencias (Statements).
 * Debe heredar de ASTNode.
 */
public abstract class Statement extends ASTNode { 

    // Constructor que llama al padre (ASTNode)
    public Statement(int line) {
        super(line); 
    }
    
    
}