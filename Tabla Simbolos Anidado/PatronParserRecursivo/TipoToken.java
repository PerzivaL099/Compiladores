package PatronParserRecursivo;

public enum TipoToken {
    // Keywords - Order matters! Longer keywords first to avoid partial matches
    INICIOPROGRAMA("inicio-programa"),
    FINPROGRAMA("fin-programa"),
    DECLARACIONVARIABLES("variables"),
    FINMIENTRAS("fin-mientras"),
    FINSI("fin-si"),
    ENTONCES("entonces"),
    MIENTRAS("mientras"),
    ESCRIBIR("escribir"),
    LEER("leer"),
    SI("si"),
    
    // Operators - Multi-character operators first
    OPRELACIONAL("<=|>=|==|!=|<|>"),
    OPARITMETICO("[+\\-*/]"),
    IGUAL("="),
    
    // Symbols and punctuation
    PARENTESISIZQ("\\("),
    PARENTESISDER("\\)"),
    DOSPUNTOS(":"),
    COMA(","),
    
    // Literals and identifiers
    CADENA("\"[^\"]*\""),
    NUMERO("\\d+(\\.\\d+)?"),
    VARIABLE("[a-zA-Z_][a-zA-Z0-9_]*"),
    
    // Special tokens
    WHITESPACE("\\s+"),
    NEWLINE("\\r?\\n"),
    COMMENT("//.*"),
    ERROR(".");

    private final String pattern;

    TipoToken(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public String getNombre() {
        return this.name();
    }

    /**
     * Check if this token type is a keyword
     */
    public boolean isKeyword() {
        switch (this) {
            case INICIOPROGRAMA:
            case FINPROGRAMA:
            case DECLARACIONVARIABLES:
            case FINMIENTRAS:
            case FINSI:
            case ENTONCES:
            case MIENTRAS:
            case ESCRIBIR:
            case LEER:
            case SI:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if this token type is an operator
     */
    public boolean isOperator() {
        switch (this) {
            case OPRELACIONAL:
            case OPARITMETICO:
            case IGUAL:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if this token type is a literal
     */
    public boolean isLiteral() {
        switch (this) {
            case CADENA:
            case NUMERO:
            case VARIABLE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if this token should be ignored during parsing
     */
    public boolean shouldIgnore() {
        switch (this) {
            case WHITESPACE:
            case NEWLINE:
            case COMMENT:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get a human-readable description of the token type
     */
    public String getDescription() {
        switch (this) {
            case INICIOPROGRAMA: return "Inicio de programa";
            case FINPROGRAMA: return "Fin de programa";
            case DECLARACIONVARIABLES: return "Declaración de variables";
            case LEER: return "Instrucción de lectura";
            case ESCRIBIR: return "Instrucción de escritura";
            case SI: return "Condicional si";
            case ENTONCES: return "Entonces";
            case FINSI: return "Fin de condicional";
            case MIENTRAS: return "Bucle mientras";
            case FINMIENTRAS: return "Fin de bucle";
            case IGUAL: return "Operador de asignación";
            case OPARITMETICO: return "Operador aritmético";
            case OPRELACIONAL: return "Operador relacional";
            case PARENTESISIZQ: return "Paréntesis izquierdo";
            case PARENTESISDER: return "Paréntesis derecho";
            case DOSPUNTOS: return "Dos puntos";
            case COMA: return "Coma";
            case VARIABLE: return "Variable";
            case NUMERO: return "Número";
            case CADENA: return "Cadena de texto";
            case WHITESPACE: return "Espacio en blanco";
            case NEWLINE: return "Nueva línea";
            case COMMENT: return "Comentario";
            case ERROR: return "Token no reconocido";
            default: return "Desconocido";
        }
    }
}