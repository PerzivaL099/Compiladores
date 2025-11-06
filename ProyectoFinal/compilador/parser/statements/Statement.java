package compilador.parser.statements;

import compilador.parser.ASTNode; // <-- Necesitas importar el padre

/**
 * Clase base abstracta para todos los nodos que representan Sentencias.
 * Hereda de ASTNode.
 */
public abstract class Statement extends ASTNode { 

    public Statement(int line) {
        // Llama al constructor de la clase padre (ASTNode)
        super(line); 
    }
    
    // El método accept() NO es necesario aquí, ya que los nodos concretos
    // (IfStatement, AssignmentStatement) lo implementarán directamente para sus tipos.
}