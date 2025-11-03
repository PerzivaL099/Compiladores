package compilador.parser.declarations;


import compilador.parser.ASTNode;
import compilador.parser.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class Program extends ASTNode {
    // Lista de todas las funciones definidas en el programa MiniJava
    public final List<FunctionDeclaration> functions;

    public Program(int line) {
        super(line);
        this.functions = new ArrayList<>();
    }

    public void addFunction(FunctionDeclaration func) {
        this.functions.add(func);
    }
    
    // Implementación del Patrón Visitor para que el compilador pueda recorrer el programa.
    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
    
    // Método auxiliar (Getter, si es necesario)
    public List<FunctionDeclaration> getFunctions() {
        return functions;
    }


    
}
