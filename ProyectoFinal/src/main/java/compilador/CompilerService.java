package compilador;

import compilador.lexer.Lexer;
import compilador.parser.Parser;
import compilador.parser.declarations.Program;
import compilador.semantico.SemanticAnalyzer;
import compilador.intermedio.InterCodeGenerator;
import compilador.intermedio.Cuadrupla;
import compilador.generacion.CodeGenerator;
import compilador.generacion.DiagramGenerator;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
// Se elimina el import de LogService

public class CompilerService {

    // Se eliminó la instancia de LogService

    public CompilerService() {
        // No se necesita inicializar el LogService
    }

    // Se eliminó el parámetro userId de la firma del método
    public CompilerResult compile(String sourceCode) { 
        try {
            // FASES 1, 2, 3 (Análisis)
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
            
            // ======================================================
            // === LÓGICA DE ESCRITURA DE ARCHIVOS DE SALIDA (NUEVA) ===
            // ======================================================
            try {
                // Escribe el archivo Assembly (program.asm)
                try (FileWriter asmWriter = new FileWriter("output/program.asm")) {
                    asmWriter.write(asmResult);
                }

                // Escribe el archivo DOT (program_flow.dot)
                try (FileWriter dotWriter = new FileWriter("output/program_flow.dot")) {
                    dotWriter.write(dotResult);
                }
            } catch (IOException e) {
                // Importante: Si la escritura falla, lo registramos pero dejamos que la compilación continúe.
                System.err.println("Advertencia de I/O: No se pudieron guardar los archivos de salida en 'output/'. Verifique los permisos o la existencia de la carpeta. Detalle: " + e.getMessage());
            }
            // ======================================================

            // Se eliminó la llamada a logService.recordLog(...)

            return new CompilerResult(asmResult, dotResult);

        } catch (RuntimeException e) {
            return new CompilerResult(e.getMessage());
        } catch (Exception e) {
            return new CompilerResult("Error inesperado del servidor: " + e.getMessage());
        }
    }
}