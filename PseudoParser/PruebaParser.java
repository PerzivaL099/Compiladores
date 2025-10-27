import java.io.FileReader;
import java.io.IOException;

public class PruebaParser { 
    
    public static void main(String[] arg) throws LexicalException, SyntaxException { 
        String entrada = leerPrograma("inicio-programa.txt"); 
        
        // 1. Ejecutar el análisis léxico
        PseudoLexer lexer = new PseudoLexer(); 
        lexer.analizar(entrada); 
        
        System.out.println("*** Análisis léxico ***\n"); 
        for (Token t : lexer.getTokens()) { 
            System.out.println(t); 
        } 

        // 2. Ejecutar el análisis sintáctico
        System.out.println("\n*** Análisis sintáctico ***\n"); 
        PseudoParser parser = new PseudoParser(); 
        parser.analizar(lexer); 
    } 

    /**
     * Lee el contenido de un archivo de texto y lo devuelve como un String.
     */
    private static String leerPrograma(String nombre) { 
        String entrada = ""; 
        try { 
            FileReader reader = new FileReader(nombre); 
            int caracter; 
            while ((caracter = reader.read()) != -1) { 
                entrada += (char) caracter; 
            }
            reader.close(); 
            return entrada; 
        } catch (IOException e) { 
            e.printStackTrace();
            return ""; 
        } 
    } 
}