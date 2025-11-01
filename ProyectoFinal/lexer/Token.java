package lexer;

public class Token {
    public enum TokenType{

        IF, ELSE, WHILE, INT, BOOLEAN, TRUE, FALSE, RETURN,

        //Operadores
        PLUS, MINUS, MULT, DIV, ASSIGN,
        EQ, NEQ, LT, FT, GT, LTE, GTE,
        AND, OR, NOT,
        SEMICOLON, COMMA, LPAREN, RPAREN, LBRACE, RBRACE, 

        //Identificadores y Literales
        ID, INTEGER_LITERAL,

        //Fin de archivo
        EOF, ERROR
    }

    private final TokenType type;
    private final String lexeme;
    private final Object literalValue; // Valor para literales
    private final int line; //Reportar errores

    // Constructor
    public Token(TokenType type, String lexeme, Object literalValue, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literalValue = literalValue;
        this.line = line;
    }

    // Getters

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }   

    public Object getLiteralValue() {
        return literalValue;
    }

    public int getLine() {
        return line;
    }

   
    @Override
    public String toString() {
        return "<" + type + ", \"" + lexeme + "\"" + 
               (literalValue != null ? ", " + literalValue : "") + 
               ", line: " + line + ">";
    }

}
