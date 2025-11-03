package compilador;

import compilador.lexer.Lexer;
import compilador.parser.Parser;
//import compilador.parser.declarations.Program;
import compilador.semantico.SemanticAnalyzer;
import compilador.intermedio.InterCodeGenerator;
import compilador.intermedio.Cuadrupla;
import compilador.generacion.CodeGenerator;
import compilador.generacion.DiagramGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    private static final String SOURCE_FILE = "input/test.mj"; // MiniJava Source
    private static final String OUTPUT_ASM = "output/program.asm";
    private static final String OUTPUT_DOT = "output/program_flow.dot";

    public static void main(String[] args) {
        System.out.println("--- COMPILADOR MINIJAVA INICIADO ---");
        
        try {
            // 1. CARGAR CÓDIGO FUENTE
            String sourceCode = new String(Files.readAllBytes(Paths.get(SOURCE_FILE)));
            
            // =========================================================
            // FASE 1: ANÁLISIS LÉXICO (Lexer)
            // =========================================================
            System.out.println("\n[1/6] Iniciando Análisis Léxico...");
            Lexer lexer = new Lexer(sourceCode);
            // El lexer es consumido internamente por el parser

            // =========================================================
            // FASE 2: ANÁLISIS SINTÁCTICO (Parser)
            // =========================================================
            System.out.println("[2/6] Iniciando Análisis Sintáctico y construcción del AST...");
            Parser parser = new Parser(lexer);
            Program ast = parser.parseProgram();
            System.out.println("      -> AST construido exitosamente.");

            // =========================================================
            // FASE 3: ANÁLISIS SEMÁNTICO (SemanticAnalyzer)
            // =========================================================
            System.out.println("[3/6] Iniciando Análisis Semántico (Tabla de Símbolos, Tipos)...");
            SemanticAnalyzer semAnalyzer = new SemanticAnalyzer();
            semAnalyzer.analyze(ast);
            System.out.println("      -> Verificación Semántica completada sin errores.");

            // =========================================================
            // FASE 4: CÓDIGO INTERMEDIO (InterCodeGenerator)
            // =========================================================
            System.out.println("[4/6] Iniciando Generación de Cuádruplas...");
            InterCodeGenerator icg = new InterCodeGenerator();
            icg.generate(ast);
            List<Cuadrupla> cuadruples = icg.getCode();
            System.out.println("      -> Generadas " + cuadruples.size() + " cuádruplas.");
            // Opcional: imprimir las cuádruplas para depuración
            // cuadruples.forEach(System.out::println); 

            // =========================================================
            // FASE 5: GENERACIÓN DE CÓDIGO FINAL (Ensamblador)
            // =========================================================
            System.out.println("[5/6] Iniciando Generación de Código Ensamblador...");
            CodeGenerator asmGenerator = new CodeGenerator(cuadruples);
            List<String> assembly = asmGenerator.generate();
            Files.write(Paths.get(OUTPUT_ASM), assembly);
            System.out.println("      -> Ensamblador escrito en: " + OUTPUT_ASM);

            // =========================================================
            // FASE 6: GENERACIÓN DE DIAGRAMAS (DiagramGenerator) - ¡EXTENSIÓN!
            // =========================================================
            System.out.println("[6/6] Iniciando Generación de Diagrama de Flujo (DOT)...");
            DiagramGenerator diagramGenerator = new DiagramGenerator();
            diagramGenerator.generate(ast, OUTPUT_DOT);
            System.out.println("      -> Archivo DOT escrito en: " + OUTPUT_DOT);
            
            System.out.println("\n--- COMPILACIÓN EXITOSA ---");

        } catch (IOException e) {
            System.err.println("\n[ERROR FATAL] Problema de Archivo (Verifica que 'input/test.mj' exista): " + e.getMessage());
        } catch (RuntimeException e) {
            // Capturar errores léxicos, sintácticos o semánticos lanzados por las fases
            System.err.println("\n--- ERROR EN LA COMPILACIÓN ---");
            System.err.println("Error: " + e.getMessage());
        }
    }
}