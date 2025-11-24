package compilador;

//Almacena resultados
public class CompilerResult {
    private final boolean success;      // ⭐ AGREGADO
    private final String asmCode;
    private final String dotCode;
    private final String error;

    // Constructor para éxito
    public CompilerResult(String asmCode, String dotCode) {
        this.success = true;            // ⭐ AGREGADO
        this.asmCode = asmCode;
        this.dotCode = dotCode;
        this.error = null;
    }

    // Constructor para error
    public CompilerResult(String error) {
        this.success = false;           // ⭐ AGREGADO
        this.asmCode = "";
        this.dotCode = "";
        this.error = error;
    }

    // Getters
    public boolean isSuccess() {        // ⭐ AGREGADO
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