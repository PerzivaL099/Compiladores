package compilador;

// Importa SparkJava y utilidades
import compilador.lexer.Lexer;
import compilador.parser.Parser;
import compilador.parser.declarations.Program;
import compilador.semantico.SemanticAnalyzer;
import compilador.intermedio.InterCodeGenerator;
import compilador.intermedio.Cuadrupla;
import compilador.generacion.CodeGenerator;
import compilador.generacion.DiagramGenerator;
import java.util.List;
import compilador.semantico.LogService; // Importa el servicio de log

public class CompilerService {

    // Instancia del servicio de log (Necesita que la clase exista)
    private final LogService logService; 

    public CompilerService() {
        // Inicializar el servicio de log en el constructor
        this.logService = new LogService(); 
    }

    /**
     * Compila el código fuente y registra la actividad si es exitosa.
     * @param sourceCode El código MiniJava a compilar.
     * @param userId El ID del usuario autenticado (se obtiene de la sesión de Spark).
     */
    public CompilerResult compile(String sourceCode, String userId) {
        try {
            // FASE 1 - 3 (Análisis)
            Lexer lexer = new Lexer(sourceCode);
            Parser parser = new Parser(lexer);
            Program ast = parser.parseProgram();
            SemanticAnalyzer semAnalyzer = new SemanticAnalyzer();
            semAnalyzer.analyze(ast);

            // FASE 4 (Intermedio)
            InterCodeGenerator icg = new InterCodeGenerator();
            icg.generate(ast);
            List<Cuadrupla> cuadruples = icg.getCode();

            // FASE 5 & 6 (Generación)
            CodeGenerator asmGenerator = new CodeGenerator(cuadruples);
            List<String> assemblyList = asmGenerator.generate();
            String asmResult = String.join("\n", assemblyList);

            DiagramGenerator diagramGenerator = new DiagramGenerator();
            String dotResult = diagramGenerator.generateDotString(ast); 

            // --- TAREA D: REGISTRO DE LOG ---
            // Solo registramos si la compilación fue exitosa (no hubo RuntimeException)
            logService.recordLog(userId, sourceCode, asmResult, dotResult); 
            // ---------------------------------

            return new CompilerResult(asmResult, dotResult);

        } catch (RuntimeException e) {
            // Captura errores de compilación (léxico, sintáctico, semántico)
            return new CompilerResult(e.getMessage());
        } catch (Exception e) {
            // Captura errores inesperados (p. ej., problemas de IO en LogService)
            return new CompilerResult("Error inesperado del servidor: " + e.getMessage());
        }
    }
}