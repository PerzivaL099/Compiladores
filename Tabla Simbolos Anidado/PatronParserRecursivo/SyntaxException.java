package PatronParserRecursivo;

public class SyntaxException extends Exception {
    private String expected;
    private String found;
    private int line;
    private int column;

    // Basic constructor
    public SyntaxException(String expected, String found) {
        super(buildMessage(expected, found, -1, -1));
        this.expected = expected;
        this.found = found;
        this.line = -1;
        this.column = -1;
    }

    // Constructor with position information
    public SyntaxException(String expected, String found, int line, int column) {
        super(buildMessage(expected, found, line, column));
        this.expected = expected;
        this.found = found;
        this.line = line;
        this.column = column;
    }

    // Constructor with custom message
    public SyntaxException(String message) {
        super(message);
        this.expected = "";
        this.found = "";
        this.line = -1;
        this.column = -1;
    }

    // Constructor with token information
    public SyntaxException(String expected, Token foundToken) {
        super(buildMessage(expected, foundToken.getTipo().getNombre(), 
                          foundToken.getLinea(), foundToken.getColumna()));
        this.expected = expected;
        this.found = foundToken.getTipo().getNombre();
        this.line = foundToken.getLinea();
        this.column = foundToken.getColumna();
    }

    private static String buildMessage(String expected, String found, int line, int column) {
        StringBuilder message = new StringBuilder();
        message.append("Error de sintaxis: ");
        message.append("Se esperaba '").append(expected).append("'");
        message.append(" pero se encontró '").append(found).append("'");
        
        if (line > 0 && column > 0) {
            message.append(" en línea ").append(line).append(", columna ").append(column);
        }
        
        return message.toString();
    }

    // Getters
    public String getExpected() {
        return expected;
    }

    public String getFound() {
        return found;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    // Check if position information is available
    public boolean hasPosition() {
        return line > 0 && column > 0;
    }

    // Get a detailed error message
    public String getDetailedMessage() {
        StringBuilder detail = new StringBuilder();
        detail.append("=== ERROR DE SINTAXIS ===\n");
        detail.append("Esperado: ").append(expected).append("\n");
        detail.append("Encontrado: ").append(found).append("\n");
        
        if (hasPosition()) {
            detail.append("Posición: Línea ").append(line).append(", Columna ").append(column).append("\n");
        }
        
        detail.append("Mensaje: ").append(getMessage()).append("\n");
        detail.append("========================");
        
        return detail.toString();
    }

    // Get suggested fix (basic implementation)
    public String getSuggestion() {
        if (found.equals("EOF")) {
            return "El programa terminó inesperadamente. Verifica que todas las estructuras estén cerradas correctamente.";
        }
        
        switch (expected) {
            case "FINPROGRAMA":
                return "¿Olvidaste agregar 'fin-programa' al final?";
            case "FINSI":
                return "¿Olvidaste cerrar la estructura 'si' con 'fin-si'?";
            case "FINMIENTRAS":
                return "¿Olvidaste cerrar la estructura 'mientras' con 'fin-mientras'?";
            case "ENTONCES":
                return "Después de la condición 'si' debe ir 'entonces'";
            case "PARENTESISDER":
                return "¿Falta un paréntesis de cierre ')'?";
            case "PARENTESISIZQ":
                return "¿Falta un paréntesis de apertura '('?";
            case "VARIABLE":
                return "Se esperaba el nombre de una variable";
            case "NUMERO":
                return "Se esperaba un número";
            default:
                return "Revisa la sintaxis en esta posición";
        }
    }

    @Override
    public String toString() {
        return getDetailedMessage();
    }
}