
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PseudoLexer {

    private ArrayList<Token> tokens = new ArrayList<>();
    private String entrada;
    private int pos = 0;
    private char charActual;


    private static final Map<String, TipoToken> PALABRAS_CLAVE;

    static {
        PALABRAS_CLAVE = new HashMap<>();
        // Palabras clave del programa
        PALABRAS_CLAVE.put("inicio-programa", new TipoToken("INICIOPROGRAMA"));
        PALABRAS_CLAVE.put("fin-programa", new TipoToken("FINPROGRAMA"));

        PALABRAS_CLAVE.put("leer", new TipoToken("LEER"));
        PALABRAS_CLAVE.put("escribir", new TipoToken("ESCRIBIR"));

        PALABRAS_CLAVE.put("mientras", new TipoToken("MIENTRAS"));
        PALABRAS_CLAVE.put("fin-mientras", new TipoToken("FINMIENTRAS"));

        PALABRAS_CLAVE.put("si", new TipoToken("SI"));
        PALABRAS_CLAVE.put("entonces", new TipoToken("ENTONCES"));
        PALABRAS_CLAVE.put("fin-si", new TipoToken("FINSI"));
    }


    private void avanzar() {
        pos++;
        if (pos < entrada.length()) {
            charActual = entrada.charAt(pos);
        } else {

            charActual = '\0';
        }
    }


    private void saltarEspacios() {
        while (charActual != '\0' && Character.isWhitespace(charActual)) {
            avanzar();
        }
    }


    private Token leerNumero() {
        StringBuilder lexema = new StringBuilder();
        while (charActual != '\0' && Character.isDigit(charActual)) {
            lexema.append(charActual);
            avanzar();
        }

        if (charActual == '.') {
            lexema.append(charActual);
            avanzar();
            while (charActual != '\0' && Character.isDigit(charActual)) {
                lexema.append(charActual);
                avanzar();
            }
        }
        return new Token(new TipoToken("NUMERO"), lexema.toString());
    }


    private Token leerCadena() {
        StringBuilder lexema = new StringBuilder();
        lexema.append('"');
        avanzar();

        while (charActual != '\0' && charActual != '"') {
            lexema.append(charActual);
            avanzar();
        }

        if (charActual == '"') {
            lexema.append('"');
            avanzar();
        } else {

            System.err.println("Error Léxico: Cadena no cerrada.");
        }
        return new Token(new TipoToken("CADENA"), lexema.toString());
    }


    private Token leerIdentificadorOPalabraClave() {
        StringBuilder lexema = new StringBuilder();

        while (charActual != '\0' && (Character.isLetterOrDigit(charActual) || charActual == '-' || charActual == '_')) {
            lexema.append(charActual);
            avanzar();
        }

        String lexemaStr = lexema.toString();

        // Comprobar si es una palabra clave
        TipoToken tipo = PALABRAS_CLAVE.get(lexemaStr);
        if (tipo != null) {
            return new Token(tipo, lexemaStr); // Es una palabra clave
        }


        return new Token(new TipoToken("VARIABLE"), lexemaStr);
    }


    public void analizar(String entrada) {
        this.entrada = entrada;
        this.pos = 0;
        this.tokens = new ArrayList<>(); // Reiniciar la lista de tokens

        if (entrada == null || entrada.isEmpty()) {
            return;
        }

        this.charActual = entrada.charAt(pos);

        System.out.println("--- Analizando Léxicamente (REAL) ---");

        while (charActual != '\0') {
            // 1. Ignorar espacios
            if (Character.isWhitespace(charActual)) {
                saltarEspacios();
                continue;
            }

            // 2. Identificadores y Palabras Clave

            if (Character.isLetter(charActual) || charActual == '_') {
                tokens.add(leerIdentificadorOPalabraClave());
                continue;
            }

            // 3. Números
            if (Character.isDigit(charActual)) {
                tokens.add(leerNumero());
                continue;
            }

            // 4. Cadenas
            if (charActual == '"') {
                tokens.add(leerCadena());
                continue;
            }

            // 5. Símbolos de un solo caracter
            switch (charActual) {
                case '=':
                    tokens.add(new Token(new TipoToken("IGUAL"), "="));
                    avanzar();
                    break;
                case '+':
                    tokens.add(new Token(new TipoToken("OPARITMETICO"), "+"));
                    avanzar();
                    break;
                case '/':
                    tokens.add(new Token(new TipoToken("OPARITMETICO"), "/"));
                    avanzar();
                    break;

                case '<':
                    tokens.add(new Token(new TipoToken("OPRELACIONAL"), "<"));
                    avanzar();
                    break;


                case '(':
                    tokens.add(new Token(new TipoToken("PARENTESISIZQ"), "("));
                    avanzar();
                    break;
                case ')':
                    tokens.add(new Token(new TipoToken("PARENTESISDER"), ")"));
                    avanzar();
                    break;
                case ',':
                    tokens.add(new Token(new TipoToken("COMA"), ","));
                    avanzar();
                    break;

                default:

                    System.err.println("Error Léxico: Caracter no reconocido '" + charActual + "' en la posición " + pos);
                    avanzar();
            }
        }

        System.out.println("--- Análisis Léxico Real Terminado ---");
    }


      //Devuelve la lista de tokens generada.

    public ArrayList<Token> getTokens() {
        return tokens;
    }
}