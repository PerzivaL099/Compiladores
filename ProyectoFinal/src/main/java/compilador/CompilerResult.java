package compilador;

//Almacena resultados
public class CompilerResult {
    private final boolean success;      
    private final String asmCode;
    private final String dotCode;
    private final String error;

    // Constructor para éxito
    public CompilerResult(String asmCode, String dotCode) {
        this.success = true;            
        this.asmCode = asmCode;
        this.dotCode = dotCode;
        this.error = null;
    }

    // Constructor para error
    public CompilerResult(String error) {
        this.success = false;           
        this.asmCode = "";
        this.dotCode = "";
        this.error = error;
    }

    // Getters
    public boolean isSuccess() {        
        return success;
    }

    public String getAsmCode() {
        return asmCode;
    }

    public String getDotCode() {
        return dotCode;
    }

    public String getError() {
        return error;
    }
}