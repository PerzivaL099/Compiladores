package compilador.semantico;


import compilador.parser.ASTVisitor;

public class SemanticAnalyzer implements ASTVisitor {
    private final TablaSimbolos tablaSimbolos;

    public SemanticAnalyzer(){
        this.tablaSimbolos = new TablaSimbolos();
    }

    public void analyze(Program program){
        program.accept(this);
    }

    

}
