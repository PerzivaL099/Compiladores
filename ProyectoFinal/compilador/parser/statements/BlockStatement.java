package compilador.parser.statements;

import compilador.parser.ASTNode; 
import compilador.parser.ASTVisitor;
import java.util.List;
import java.util.ArrayList;

/**
 * Representa un bloque de código { ... }. Contiene una lista secuencial de sentencias.
 * Hereda de Statement.
 */
public class BlockStatement extends Statement {

    // La lista de todas las sentencias dentro del bloque
    public final List<ASTNode> statements;

    public BlockStatement(int line, List<ASTNode> statements) {
        super(line); // Llama al constructor de Statement (que llama a ASTNode)
        
        // Inicializar la lista por si el parser pasa 'null'
        this.statements = (statements != null) ? statements : new ArrayList<>();
    }
    
    // Sobrecarga del constructor para el parser cuando encuentra '{'
    public BlockStatement(int line) {
        super(line);
        this.statements = new ArrayList<>();
    }

    // Implementación del Patrón Visitor
    @Override
    @SuppressWarnings("rawtypes") // Opcional: Silenciar advertencia de ASTVisitor
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this); 
    }
    
    // Getter
    public List<ASTNode> getStatements() {
        return statements;
    }
}