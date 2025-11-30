package PatronParserRecursivo;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PseudoLexer {
    private ArrayList<Token> tokens;
    private String input;
    private Pattern pattern;
    private int currentLine;
    private int currentColumn;

    public PseudoLexer() {
        tokens = new ArrayList<>();
        buildPattern();
        currentLine = 1;
        currentColumn = 1;
    }

    private void buildPattern() {
        StringBuilder patternBuilder = new StringBuilder();
        
        // Build the combined pattern with named groups
        for (TipoToken tokenType : TipoToken.values()) {
            if (patternBuilder.length() > 0) {
                patternBuilder.append("|");
            }
            // Use valid group names (replace hyphens with underscores)
            String groupName = tokenType.name();
            patternBuilder.append("(?<").append(groupName).append(">")
                         .append(tokenType.getPattern()).append(")");
        }
        
        pattern = Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE);
    }

    public void tokenize(String input) {
        this.input = input;
        tokens.clear();
        currentLine = 1;
        currentColumn = 1;
        
        if (input == null || input.trim().isEmpty()) {
            System.out.println("Entrada vacía - no hay tokens para generar");
            return;
        }
        
        Matcher matcher = pattern.matcher(input);
        
        while (matcher.find()) {
            TipoToken tokenType = null;
            String tokenValue = matcher.group();
            
            // Find which token type matched
            for (TipoToken type : TipoToken.values()) {
                String groupName = type.name();
                if (matcher.group(groupName) != null) {
                    tokenType = type;
                    break;
                }
            }
            
            // Calculate position
            int tokenLine = currentLine;
            int tokenColumn = matcher.start() - getLineStart(input, matcher.start()) + 1;
            
            // Handle different token types
            if (tokenType != null) {
                switch (tokenType) {
                    case WHITESPACE:
                        // Skip whitespace but update position
                        updatePosition(tokenValue);
                        break;
                        
                    case NEWLINE:
                        // Skip newlines but update line counter
                        currentLine++;
                        currentColumn = 1;
                        break;
                        
                    case COMMENT:
                        // Skip comments
                        updatePosition(tokenValue);
                        break;
                        
                    case ERROR:
                        // Handle error tokens
                        System.err.println("Error: Carácter no reconocido '" + tokenValue + 
                                         "' en línea " + tokenLine + ", columna " + tokenColumn);
                        tokens.add(new Token(tokenType, tokenValue, tokenLine, tokenColumn));
                        updatePosition(tokenValue);
                        break;
                        
                    default:
                        // Add valid tokens
                        tokens.add(new Token(tokenType, tokenValue, tokenLine, tokenColumn));
                        updatePosition(tokenValue);
                        break;
                }
            }
        }
        
        System.out.println("Tokenización completada. Total de tokens válidos: " + tokens.size());
    }

    private void updatePosition(String tokenValue) {
        for (char c : tokenValue.toCharArray()) {
            if (c == '\n') {
                currentLine++;
                currentColumn = 1;
            } else {
                currentColumn++;
            }
        }
    }

    private int getLineStart(String text, int position) {
        int lineStart = 0;
        for (int i = 0; i < position; i++) {
            if (text.charAt(i) == '\n') {
                lineStart = i + 1;
            }
        }
        return lineStart;
    }

    public ArrayList<Token> getTokens() {
        return new ArrayList<>(tokens); // Return a copy to prevent external modification
    }

    public void printTokens() {
        System.out.println("\n=== TOKENS GENERADOS ===");
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            System.out.printf("%3d: %-20s %-20s L:%-3d C:%-3d%n", 
                            i + 1, 
                            token.getTipo().getNombre(), 
                            "'" + token.getNombre() + "'",
                            token.getLinea(),
                            token.getColumna());
        }
        System.out.println("=".repeat(60) + "\n");
    }

    public void printTokensWithDescription() {
        System.out.println("\n=== TOKENS CON DESCRIPCIÓN ===");
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            System.out.printf("%3d: %-15s %-15s %-25s L:%-3d C:%-3d%n", 
                            i + 1, 
                            token.getTipo().getNombre(), 
                            "'" + token.getNombre() + "'",
                            token.getTipo().getDescription(),
                            token.getLinea(),
                            token.getColumna());
        }
        System.out.println("=".repeat(80) + "\n");
    }

    public void printStatistics() {
        System.out.println("\n=== ESTADÍSTICAS DE TOKENS ===");
        int[] counts = new int[TipoToken.values().length];
        
        for (Token token : tokens) {
            counts[token.getTipo().ordinal()]++;
        }
        
        System.out.println("Tipo de Token          | Cantidad | Descripción");
        System.out.println("-".repeat(60));
        
        for (int i = 0; i < TipoToken.values().length; i++) {
            if (counts[i] > 0) {
                TipoToken tipo = TipoToken.values()[i];
                System.out.printf("%-20s | %8d | %s%n", 
                                tipo.getNombre(), 
                                counts[i], 
                                tipo.getDescription());
            }
        }
        System.out.println("=".repeat(60) + "\n");
    }

    // Utility methods
    public boolean hasTokens() {
        return !tokens.isEmpty();
    }

    public int getTokenCount() {
        return tokens.size();
    }

    public Token getTokenAt(int index) {
        if (index >= 0 && index < tokens.size()) {
            return tokens.get(index);
        }
        return null;
    }

    public boolean hasErrors() {
        return tokens.stream().anyMatch(token -> token.getTipo() == TipoToken.ERROR);
    }

    public ArrayList<Token> getErrorTokens() {
        ArrayList<Token> errorTokens = new ArrayList<>();
        for (Token token : tokens) {
            if (token.getTipo() == TipoToken.ERROR) {
                errorTokens.add(token);
            }
        }
        return errorTokens;
    }

    // Clear all tokens
    public void clear() {
        tokens.clear();
        currentLine = 1;
        currentColumn = 1;
    }

    // Test method
    public static void main(String[] args) {
        PseudoLexer lexer = new PseudoLexer();
        
        String testCode = """
            inicio-programa
            variables: x y resultado
            leer x
            leer y
            resultado = x + y
            escribir "La suma es: ", resultado
            si (resultado > 10) entonces
                escribir "Es mayor que 10"
            fin-si
            mientras (x < 5)
                x = x + 1
                escribir "x vale: ", x
            fin-mientras
            fin-programa
            """;
        
        System.out.println("=== PRUEBA DEL LEXER ===");
        System.out.println("Código a analizar:");
        System.out.println(testCode);
        System.out.println("\n" + "=".repeat(50));
        
        lexer.tokenize(testCode);
        lexer.printTokensWithDescription();
        lexer.printStatistics();
        
        if (lexer.hasErrors()) {
            System.out.println("\n¡ATENCIÓN! Se encontraron errores:");
            for (Token errorToken : lexer.getErrorTokens()) {
                System.out.println("  " + errorToken);
            }
        } else {
            System.out.println("\n✓ No se encontraron errores léxicos");
        }
    }
}