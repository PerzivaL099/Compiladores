package compilador;

//Almacena resultados
public class CompilerResult{
    private final String asmCode;
    private final String dotCode;
    private final String error;

    //Constructor para exito
    public CompilerResult(String asmCode, String dotCode){
        this.asmCode = asmCode;
        this.dotCode = dotCode;
        this.error = null;
    }

    //Constructor para error
    public CompilerResult(String error){
        this.asmCode = null;
        this.dotCode = null;
        this.error = error;
    }

    //Getters
    public String getAsmCode(){return asmCode;}
    public String getDotCode(){return dotCode;}
    public String getError(){return error;}
}