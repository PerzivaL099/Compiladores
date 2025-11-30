package PatronParserRecursivo;

public class Token {
    private TipoToken tipo;
    private String nombre; // Esto guarda el lexema (ej: "suma", "10")
    private int linea;
    private int columna;

    public Token(TipoToken tipo, String nombre, int linea, int columna) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.linea = linea;
        this.columna = columna;
    }

    // --- ESTE ES EL MÉTODO QUE TE FALTABA ---
    // El parser llama a getLexema(), así que le devolvemos 'nombre'
    public String getLexema() {
        return nombre;
    }
    // ----------------------------------------

    // Getters
    public TipoToken getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    // Setters (if needed)
    public void setTipo(TipoToken tipo) {
        this.tipo = tipo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setLinea(int linea) {
        this.linea = linea;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }

    @Override
    public String toString() {
        // Modificado ligeramente para mostrar el lexema claramente
        return String.format("Token [%s] Lexema: '%s' (Línea: %d)", 
                           tipo.getNombre(), nombre, linea);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Token token = (Token) obj;
        return linea == token.linea && 
               columna == token.columna && 
               tipo == token.tipo && 
               nombre.equals(token.nombre);
    }

    @Override
    public int hashCode() {
        int result = tipo.hashCode();
        result = 31 * result + nombre.hashCode();
        result = 31 * result + linea;
        result = 31 * result + columna;
        return result;
    }
}