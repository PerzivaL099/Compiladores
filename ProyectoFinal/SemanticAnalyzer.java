package semantico;


import compilador.parser.ASTVisitor;
import semantico.TablaSimbolos;

public class SemanticAnalyzer implements ASTVisitor {
    private final TablaSimbolos tablaSimbolos;

    public SemanticAnalyzer(){
        this.tablaSimbolos = new TablaSimbolos();
    }

    public void analyze(Program program){
        program.accept(this);
    }

    

}
