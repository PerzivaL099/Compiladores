import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;


public class PruebaInterprete {
    
    public static void main(String[] arg) throws LexicalException, SyntaxException, IOException {
        
        
        String entrada = leerPrograma("ejemplo-alg"); 
        
        PseudoLexer lexer = new PseudoLexer();
        lexer.analizar (entrada);
        
        System.out.println("*** Análisis léxico ***\n");
        for (Token t: lexer.getTokens())
            System.out.println(t);
        
        System.out.println("\n*** Análisis sintáctico ***\n");
        
        TablaSimbolos ts = new TablaSimbolos();
        
        TipoToken tipoReal = new TipoToken("real"); 

        for (Token t : lexer.getTokens()) {
            // Verifica que el token sea de tipo VARIABLE y que no esté ya definido
            if (t.getTipo().getNombre().equals("VARIABLE") && ts.resolver(t.getNombre()) == null) {
                
                // Crea el objeto Variable usando el constructor que corregimos
                Variable nuevaVar = new Variable(t.getNombre(), tipoReal);
                
                // Insértala en la Tabla de Símbolos
                ts.definir(nuevaVar);
            }
        }
        System.out.println("\n*** Inicialización de Variables Terminada ***\n");
        // -------------------------------------------------------------------
        
        PseudoGenerador generador = new PseudoGenerador (lexer.getTokens());
        PseudoParser parser = new PseudoParser (ts, generador);
        parser.analizar (lexer);
        
        System.out.println("\n*** Tabla de simbolos ***\n");
        for (Simbolo s: ts.getSimbolos())
            System.out.println(s);
        
        System.out.println("\n*** Tuplas generadas ***\n");
        for (Tupla t: generador.getTuplas()) {
            System.out.println(t);
        }
        
        System.out.println("\n*** Ejecucion del programa ***\n");
        
        PseudoInterprete interprete = new PseudoInterprete (ts);
        interprete.interpretar (generador.getTuplas());
    }
    
   
    /**
     * Lee todo el contenido de un archivo de texto y lo devuelve como una String.
     * @param nombreArchivo El nombre del archivo a leer (ej: "ejemplo-alg").
     * @return El contenido completo del archivo.
     * @throws IOException Si el archivo no existe o hay un error de lectura.
     */
    private static String leerPrograma(String nombreArchivo) throws IOException {
        StringBuilder contenido = new StringBuilder();
        
        
        try (BufferedReader reader = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            
            while ((linea = reader.readLine()) != null) {
                contenido.append(linea).append("\n");
            }
        } 
        
        return contenido.toString();
    }
}