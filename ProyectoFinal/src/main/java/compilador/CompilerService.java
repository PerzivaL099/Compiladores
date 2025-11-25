package compilador;

import compilador.lexer.Lexer;
import compilador.parser.Parser;
import compilador.parser.declarations.Program;
import compilador.semantico.SemanticAnalyzer;
import compilador.intermedio.InterCodeGenerator;
import compilador.intermedio.Cuadrupla;
import compilador.generacion.CodeGenerator;
import compilador.generacion.DiagramGenerator;
import compilador.semantico.TablaSimbolos; 
import compilador.semantico.SimboloDTO; 

import java.util.List;
import java.util.ArrayList; 
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection; // ⭐ IMPORTACIÓN FALTANTE: para manejar la colección de símbolos internos ⭐

// Se elimina el import de LogService

public class CompilerService {

    public CompilerService() { }

    public CompilerResult compile(String sourceCode) { 
        
        List<SimboloDTO> simbolosParaFront = new ArrayList<>(); 
        
        try {
            // FASES 1, 2, 3 (Análisis)
            Lexer lexer = new Lexer(sourceCode);
            Parser parser = new Parser(lexer);
            Program ast = parser.parseProgram();
            SemanticAnalyzer semAnalyzer = new SemanticAnalyzer();
            semAnalyzer.analyze(ast);

            // ⭐ PASO 3.1 & 3.2: RECUPERACIÓN Y MAPEO DE LA TABLA DE SÍMBOLOS ⭐
            TablaSimbolos tablaSimbolos = semAnalyzer.getTablaSimbolos(); 
            
            if (tablaSimbolos != null) {
                // Obtenemos la colección de Simbolos (clase anidada)
                // Usamos la clase anidada Simbolo de TablaSimbolos.java
                Collection<TablaSimbolos.Simbolo> simbolosInternos = tablaSimbolos.getAllSimbolos();
                
                // Mapeamos a DTO (la clase de transferencia de datos)
                for (TablaSimbolos.Simbolo simbolo : simbolosInternos) {
                    simbolosParaFront.add(new SimboloDTO(
                        simbolo.getName(), 
                        simbolo.getType(), 
                        simbolo.getScope(), 
                        simbolo.getAddress() 
                    ));
                }
            }
            System.out.println("DEBUG: Símbolos mapeados para frontend: " + simbolosParaFront.size());
            // -------------------------------------------------------------

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
                System.err.println("Advertencia de I/O: No se pudieron guardar los archivos de salida en 'output/'. Verifique los permisos o la existencia de la carpeta. Detalle: " + e.getMessage());
            }
            // ======================================================

            // Se eliminó la llamada a logService.recordLog(...)

            // ⭐ LLAMAR AL NUEVO CONSTRUCTOR CON LA TABLA DE SÍMBOLOS ⭐
            return new CompilerResult(asmResult, dotResult, simbolosParaFront);

        } catch (RuntimeException e) {
            // ⭐ USAR EL CONSTRUCTOR DE ERROR ⭐
            return new CompilerResult(e.getMessage());
        } catch (Exception e) {
            // ⭐ USAR EL CONSTRUCTOR DE ERROR ⭐
            return new CompilerResult("Error inesperado del servidor: " + e.getMessage());
        }
    }
}