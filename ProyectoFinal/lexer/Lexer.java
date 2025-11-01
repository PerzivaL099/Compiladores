package lexer;

import java.util.HashMap;
import java.util.Map;

public class Lexer {
    private final String sourceCode;
    private int currentPosicion = 0;
    private int currentLine = 1;
    private char currentChar;

    //palabras reservadas
    private static final Map<String, Token.TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("if", Token.TokenType.IF);
        keywords.put("else", Token.TokenType.ELSE);
        keywords.put("while", Token.TokenType.WHILE);
        keywords.put("int", Token.TokenType.INT);
        keywords.put("boolean", Token.TokenType.BOOLEAN);
        keywords.put("true", Token.TokenType.TRUE);
        keywords.put("false", Token.TokenType.FALSE);
        keywords.put("return", Token.TokenType.RETURN);
    }

    public Lexer(String sourceCode) {
        this.sourceCode = sourceCode;
        if (!sourceCode.isEmpty()) {
            this.currentChar = sourceCode.charAt(0);
        } else {
            this.currentChar = '\0'; // Carácter nulo para archivo vacío
        }
    }

    // Método auxiliar para avanzar la posición y actualizar currentChar
    private void advance() {
        currentPosicion++;
        if (currentPosicion < sourceCode.length()) {
            currentChar = sourceCode.charAt(currentPosicion);
        } else {
            currentChar = '\0'; // Representa el EOF (End Of File)
        }
    }

    //Metodo para manejar espacios en blanco
    private void skipWhitespace(){
        while(Character.isWhitespace(currentChar) || currentChar == '\n' || currentChar == '\r'){
            if(currentChar == '\n'){
                currentLine++;
            }
            advance();
        }
    }

    // **MÉTODO PRINCIPAL: Entrega el siguiente token**
    public Token nextToken() {
        skipWhitespace();

        if (currentChar == '\0') {
            return new Token(Token.TokenType.EOF, "", null, currentLine);
        }

        // --- MANEJO DE IDENTIFICADORES Y PALABRAS CLAVE ---
        if (Character.isLetter(currentChar)) {
            return scanIdentifierOrKeyword();
        }

        // --- MANEJO DE NÚMEROS (LITERALES ENTEROS) ---
        if (Character.isDigit(currentChar)) {
            return scanNumber();
        }

        // --- MANEJO DE SÍMBOLOS Y OPERADORES DE UN CARÁCTER ---
        Token token = scanSingleCharacterSymbol();
        if (token != null) {
            return token;
        }

        // --- MANEJO DE ERRORES ---
        int startLine = currentLine;
        String errorLexeme = String.valueOf(currentChar);
        advance();
        System.err.println("Error Léxico en línea " + startLine + ": Caracter ilegal '" + errorLexeme + "'");
        return new Token(Token.TokenType.ERROR, errorLexeme, null, startLine);
    }

    // --- MÉTODOS DE ESCANEO ESPECÍFICOS ---

    private Token scanIdentifierOrKeyword() {
        int start = currentPosition;
        while (Character.isLetterOrDigit(currentChar)) {
            advance();
        }
        String lexeme = sourceCode.substring(start, currentPosition);
        
        // 1. Verificar si es una Palabra Clave
        Token.TokenType type = keywords.get(lexeme);

        // 2. Si no es Palabra Clave, es un Identificador
        if (type == null) {
            type = Token.TokenType.ID;
        }

        return new Token(type, lexeme, null, currentLine);
    }

    private Token scanNumber() {
        int start = currentPosition;
        while (Character.isDigit(currentChar)) {
            advance();
        }
        String lexeme = sourceCode.substring(start, currentPosition);
        
        // Intentar parsear el valor literal
        try {
            int value = Integer.parseInt(lexeme);
            return new Token(Token.TokenType.INTEGER_LITERAL, lexeme, value, currentLine);
        } catch (NumberFormatException e) {
            // Manejo de números demasiado grandes para 'int'
            System.err.println("Error Léxico en línea " + currentLine + ": Número fuera de rango.");
            return new Token(Token.TokenType.ERROR, lexeme, null, currentLine);
        }
    }

    private Token scanSingleCharacterSymbol() {
        int startLine = currentLine;
        char c = currentChar;
        
        switch (c) {
            case '+': advance(); return new Token(Token.TokenType.PLUS, "+", null, startLine);
            case '-': advance(); return new Token(Token.TokenType.MINUS, "-", null, startLine);
            case '*': advance(); return new Token(Token.TokenType.MULT, "*", null, startLine);
            case '/': advance(); return new Token(Token.TokenType.DIV, "/", null, startLine);
            case ';': advance(); return new Token(Token.TokenType.SEMICOLON, ";", null, startLine);
            case ',': advance(); return new Token(Token.TokenType.COMMA, ",", null, startLine);
            case '(': advance(); return new Token(Token.TokenType.LPAREN, "(", null, startLine);
            case ')': advance(); return new Token(Token.TokenType.RPAREN, ")", null, startLine);
            case '{': advance(); return new Token(Token.TokenType.LBRACE, "{", null, startLine);
            case '}': advance(); return new Token(Token.TokenType.RBRACE, "}", null, startLine);
            case '!': // ! o !=
                advance();
                if (currentChar == '=') {
                    advance(); return new Token(Token.TokenType.NEQ, "!=", null, startLine);
                }
                return new Token(Token.TokenType.NOT, "!", null, startLine);
            case '=': // = o ==
                advance();
                if (currentChar == '=') {
                    advance(); return new Token(Token.TokenType.EQ, "==", null, startLine);
                }
                return new Token(Token.TokenType.ASSIGN, "=", null, startLine);
            case '<': // < o <=
                advance();
                if (currentChar == '=') {
                    advance(); return new Token(Token.TokenType.LTE, "<=", null, startLine);
                }
                return new Token(Token.TokenType.LT, "<", null, startLine);
            case '>': // > o >=
                advance();
                if (currentChar == '=') {
                    advance(); return new Token(Token.TokenType.GTE, ">=", null, startLine);
                }
                return new Token(Token.TokenType.GT, ">", null, startLine);
            case '&': // &&
                advance();
                if (currentChar == '&') {
                    advance(); return new Token(Token.TokenType.AND, "&&", null, startLine);
                }
                break; // Error si es solo un &
            case '|': // ||
                advance();
                if (currentChar == '|') {
                    advance(); return new Token(Token.TokenType.OR, "||", null, startLine);
                }
                break; // Error si es solo un |
        }
        return null; // No es un token de un solo carácter conocido.
    }
}
