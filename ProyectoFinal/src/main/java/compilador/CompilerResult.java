package compilador;

import java.util.List;
// Asegúrate de que esta clase SimboloDTO exista en tu proyecto para la serialización
// Si la defines en un paquete diferente, ajusta el import.
import compilador.semantico.SimboloDTO; 

//Almacena resultados
public class CompilerResult {
    private final boolean success;
    private final String asmCode;
    private final String dotCode;
    private final String error;
    
    // ⭐ NUEVO CAMPO PARA LA TABLA DE SÍMBOLOS ⭐
    private final List<SimboloDTO> symbolTable; 

    // Constructor para éxito
    public CompilerResult(String asmCode, String dotCode, List<SimboloDTO> symbolTable) {
        this.success = true;
        this.asmCode = asmCode;
        this.dotCode = dotCode;
        this.error = null;
        this.symbolTable = symbolTable; // ⭐ ASIGNACIÓN DE LA TABLA ⭐
    }

    // Constructor para error
    public CompilerResult(String error) {
        this.success = false;
        this.asmCode = "";
        this.dotCode = "";
        this.error = error;
        this.symbolTable = null; // No hay tabla en caso de error
    }

    // Getters (necesarios para que GSON/Jackson serialice correctamente)
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
    
    // ⭐ GETTER PARA LA TABLA DE SÍMBOLOS ⭐
    public List<SimboloDTO> getSymbolTable() {
        return symbolTable;
    }
}