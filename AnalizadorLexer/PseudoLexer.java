import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PseudoLexer {
    private ArrayList<TipoToken> tipos = new ArrayList<>(); // [cite: 107]
    private ArrayList<Token> tokens = new ArrayList<>(); // [cite: 107]

    public PseudoLexer() { // [cite: 108]
        // Se agregan los tipos de token con sus expresiones regulares
        // Nota: Las regex del PDF [cite: 110, 111, 119, 120, 121] están corregidas a una sintaxis Java válida.
        tipos.add(new TipoToken(TipoToken.NUMERO, "-?[0-9]+(\\.[0-9]+)?")); // [cite: 110]
        tipos.add(new TipoToken(TipoToken.CADENA, "\".*\"")); // [cite: 110]
        tipos.add(new TipoToken(TipoToken.OPARITMETICO, "[*/+-]")); // [cite: 111]
        tipos.add(new TipoToken(TipoToken.OPRELACIONAL, "(<=|>=|==|<|>|!=)")); // [cite: 111]
        tipos.add(new TipoToken(TipoToken.IGUAL, "=")); // [cite: 111]
        tipos.add(new TipoToken(TipoToken.COMA, ",")); // [cite: 112]
        tipos.add(new TipoToken(TipoToken.PARENTESISIZQ, "\\(")); // [cite: 113]
        tipos.add(new TipoToken(TipoToken.PARENTESISDER, "\\)")); // [cite: 113]
        tipos.add(new TipoToken(TipoToken.INICIOPROGRAMA, "inicio-programa")); // [cite: 114]
        tipos.add(new TipoToken(TipoToken.FINPROGRAMA, "fin-programa")); // [cite: 114]
        tipos.add(new TipoToken(TipoToken.LEER, "leer")); // [cite: 115]
        tipos.add(new TipoToken(TipoToken.ESCRIBIR, "escribir")); // [cite: 116]
        tipos.add(new TipoToken(TipoToken.SI, "si")); // [cite: 116]
        tipos.add(new TipoToken(TipoToken.ENTONCES, "entonces")); // [cite: 117]
        tipos.add(new TipoToken(TipoToken.FINSI, "fin-si")); // [cite: 117]
        tipos.add(new TipoToken(TipoToken.MIENTRAS, "mientras")); // [cite: 118]
        tipos.add(new TipoToken(TipoToken.FINMIENTRAS, "fin-mientras")); // [cite: 119]
        tipos.add(new TipoToken(TipoToken.VARIABLE, "[a-zA-Z_][a-zA-Z0-9_]*")); // [cite: 119]
        tipos.add(new TipoToken(TipoToken.ESPACIO, "[ \t\f\r\n]+")); // [cite: 120]
        tipos.add(new TipoToken(TipoToken.ERROR, "[^ \t\f\r\n]+")); // [cite: 121]
    } // [cite: 109]

    public ArrayList<Token> getTokens() { // [cite: 125]
        return tokens; // [cite: 126]
    } // [cite: 127]

    public void analizar(String entrada) throws LexicalException { // [cite: 128]
        StringBuffer er = new StringBuffer(); // [cite: 128]
        for (TipoToken tt : tipos) { // [cite: 129]
            er.append(String.format("|(?<%s>%s)", tt.getNombre(), tt.getPatron())); // [cite: 130]
        }
        Pattern p = Pattern.compile(new String(er.substring(1))); // [cite: 131]
        Matcher m = p.matcher(entrada); // [cite: 132]

        while (m.find()) { // [cite: 135]
            for (TipoToken tt : tipos) { // [cite: 136]
                if (m.group(TipoToken.ESPACIO) != null) { // [cite: 137]
                    continue; // [cite: 137]
                } else if (m.group(tt.getNombre()) != null) { // [cite: 138]
                    if (tt.getNombre().equals(TipoToken.ERROR)) { // [cite: 139]
                        LexicalException ex = new LexicalException(m.group(tt.getNombre())); // [cite: 139]
                        throw ex; // [cite: 139]
                    } // [cite: 140]
                    
                    String nombre = m.group(tt.getNombre()); // [cite: 141]
                    if (tt.getNombre().equals(TipoToken.CADENA)) { // [cite: 142]
                        nombre = nombre.substring(1, nombre.length() - 1); // [cite: 144]
                    } // [cite: 143]
                    
                    tokens.add(new Token(tt, nombre)); // [cite: 145]
                    break; // [cite: 146]
                } // [cite: 147]
            } // [cite: 148]
        } // [cite: 149]
    } // [cite: 133]
} // [cite: 134]