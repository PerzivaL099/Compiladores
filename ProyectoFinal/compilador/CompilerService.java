package compilador;

// Importa todas tus fases
import compilador.lexer.Lexer;
import compilador.parser.Parser;
import compilador.parser.declarations.Program;
import compilador.semantico.SemanticAnalyzer;
import compilador.intermedio.InterCodeGenerator;
import compilador.intermedio.Cuadrupla;
import compilador.generacion.CodeGenerator;
import compilador.generacion.DiagramGenerator;
import java.util.List;

public class CompilerService {

    // Este método REEMPLAZA la lógica de tu antiguo Main.java
    public CompilerResult compile(String sourceCode) {
        try {
            // FASE 1: Léxico
            Lexer lexer = new Lexer(sourceCode);

            // FASE 2: Sintáctico (AST)
            Parser parser = new Parser(lexer);
            Program ast = parser.parseProgram();

            // FASE 3: Semántico
            SemanticAnalyzer semAnalyzer = new SemanticAnalyzer();
            semAnalyzer.analyze(ast);

            // FASE 4: Código Intermedio
            InterCodeGenerator icg = new InterCodeGenerator();
            icg.generate(ast);
            List<Cuadrupla> cuadruples = icg.getCode();

            // FASE 5: Generación de Ensamblador
            CodeGenerator asmGenerator = new CodeGenerator(cuadruples);
            List<String> assemblyList = asmGenerator.generate();
            String asmResult = String.join("\n", assemblyList); // Convertir a un solo String

            // FASE 6: Generación de Diagrama
            DiagramGenerator diagramGenerator = new DiagramGenerator();
            // Necesitarás un getter en DiagramGenerator para el string DOT
            String dotResult = diagramGenerator.generateDotString(ast); 

            // ¡ÉXITO!
            return new CompilerResult(asmResult, dotResult);

        } catch (RuntimeException e) {
            // ¡ERROR!
            return new CompilerResult(e.getMessage());
        } catch (Exception e) {
            return new CompilerResult("Error inesperado del servidor: " + e.getMessage());
        }
    }
}