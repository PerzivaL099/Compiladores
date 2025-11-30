package PatronParserRecursivo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PruebaTablaSimbolos {
    
    public static void main(String[] args) {
        String rutaArchivo = "C:\\Users\\mario\\OneDrive\\Desktop\\Codigos\\ProyeectosCompiladores\\Compiladores\\Tabla Simbolos Anidado\\PatronParserRecursivo\\ejemplo.alg"; 
        String entrada = leerPrograma(rutaArchivo);
        
        if (entrada.isEmpty()) {
            System.err.println("No se pudo leer el archivo, usando ejemplo interno.");
            testWithHardcodedExample(); 
            return;
        }
        
        ejecutarPrueba(entrada);
    }

    private static void ejecutarPrueba(String entrada) {
        System.out.println("Contenido del archivo:");
        System.out.println(entrada);
        System.out.println("=".repeat(60));

        PseudoLexer lexer = new PseudoLexer();
        lexer.tokenize(entrada); 

        // --- [NUEVO] IMPRESIÓN DE TOKENS ---
        System.out.println("\n*** Análisis Léxico (Lista de Tokens) ***");
        for (Token t : lexer.getTokens()) {
            // Formato: TIPO: Lexema
            System.out.println(t.getTipo().getNombre() + ": " + t.getLexema());
        }
        System.out.println("*****************************************\n");
        // -----------------------------------

        if (lexer.hasErrors()) {
            System.err.println("¡Errores léxicos!");
            return;
        }

        System.out.println("Análisis léxico correcto.");

        System.out.println("\n=== ANALISIS SINTACTICO Y DE ALCANCE ===");
        try {
            PseudoParser parser = new PseudoParser();
            parser.analizar(lexer); 

            System.out.println("\n*** Estado Final del Scope ***\n");
            
            Scope alcanceFinal = parser.getCurrentScope();
            
            if (alcanceFinal instanceof BaseScope) {
                BaseScope scopeBase = (BaseScope) alcanceFinal;
                System.out.println("Scope actual: " + scopeBase.getScopeName());
                System.out.println("Símbolos en este nivel:");
                
                for (Simbolo s : scopeBase.symbols.values()) {
                    System.out.println(" - " + s);
                }
            } else {
                System.out.println("Scope final: " + alcanceFinal);
            }

        } catch (SyntaxException e) {
            System.err.println("Error de SINTAXIS: " + e.getDetailedMessage());
        } catch (SemanticException e) {
            System.err.println("Error SEMANTICO: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String leerPrograma(String nombre) {
        // ... (Tu código de lectura de archivo se queda igual)
        String entrada = "";
        try {
            FileReader reader = new FileReader(new File(nombre));
            int caracter;
            while ((caracter = reader.read()) != -1) entrada += (char) caracter;
            reader.close();
            return entrada;
        } catch (IOException e) { return ""; }
    }
    
    public static void testWithHardcodedExample() {
        // ... (Tu código de ejemplo se queda igual)
        String ejemplo = "inicio-programa variables: x i = 10 fin-programa";
        ejecutarPrueba(ejemplo);
    }
}