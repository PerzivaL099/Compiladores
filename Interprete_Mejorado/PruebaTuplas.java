
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


public class PruebaTuplas {


    public static String leerPrograma(String rutaArchivo) throws java.io.IOException {

        if (!Files.exists(Paths.get(rutaArchivo))) {
            System.out.println("Advertencia: No se encontró 'ejemplo.alg'. Usando programa de prueba interno.");
            return "inicio-programa\n" +
                    "leer numeroDeElementos\n" +
                    "promedio = 0\n" +
                    "i = 0\n" +
                    "mientras (i < numeroDeElementos)\n" +
                    "fin-mientras\n" +
                    "fin-programa\n";
        }
        return new String(Files.readAllBytes(Paths.get(rutaArchivo)));
    }

    public static void main(String[] arg) {

        try {

            String entrada = leerPrograma("ejemplo.alg");
            System.out.println("--- Contenido de ejemplo.alg ---");
            System.out.println(entrada);
            System.out.println("---------------------------------");

            PseudoLexer lexer = new PseudoLexer();
            lexer.analizar(entrada);

            System.out.println("\n*** Análisis léxico ***\n");
            for (Token t: lexer.getTokens()) {
                System.out.println(t);
            }

            System.out.println("\n*** Análisis sintáctico ***\n");
            TablaSimbolos ts = new TablaSimbolos();
            PseudoGenerador generador = new PseudoGenerador(lexer.getTokens());
            PseudoParser parser = new PseudoParser(ts, generador);
            parser.analizar(lexer);

            System.out.println("\n*** Tabla de símbolos ***\n");
            for (Simbolo s: ts.getSimbolos()) {
                System.out.println(s);
            }

            System.out.println("\n*** Tuplas generadas ***\n");
            ArrayList<Tupla> tuplas = generador.getTuplas();
            for (int i = 0; i < tuplas.size(); i++) {

                System.out.println("(" + i + ") " + tuplas.get(i));
            }

        } catch (Exception e) {
            System.err.println("\n--- ERROR ---");
            e.printStackTrace();
        }
    }
}