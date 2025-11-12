package compilador.generacion;

import compilador.intermedio.Cuadrupla;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Traduce lista de cuadruplas a codigo ensamblador
public class CodeGenerator {
    private final List<Cuadrupla> intermediateCode;
    private final List<String> assemblyCode =  new ArrayList<>();

    //Mapeo simple de variables a posiciones de pila
    private final Map<String, Integer> stackOffset = new HashMap<>();
    private int currentOffset = 0;

    // Simulación de Registros (para operaciones temporales)
    private static final String REG_T1 = "R1"; 
    private static final String REG_T2 = "R2"; 
    private static final String REG_RESULT = "R0"; // Registro de resultado/retorno

    public CodeGenerator(List<Cuadrupla> intermediateCode) {
        this.intermediateCode = intermediateCode;
    }

    public List<String> generate() {
        // 1. Inicializar la sección de datos y texto (simulado)
        assemblyCode.add(".DATA");
        assemblyCode.add("\t; Variables globales (si las hubiera)");
        assemblyCode.add(".TEXT");
        assemblyCode.add("\tJUMP main"); // Asumimos un 'main'

        // 2. Procesar cada cuádrupla
        for (Cuadrupla cuad : intermediateCode) {
            translateCuadrupla(cuad);
        }

        return assemblyCode;
    }

    private void translateCuadrupla(Cuadrupla cuad) {
        // Añadir la cuádrupla como comentario para referencia
        assemblyCode.add("\n; " + cuad.toString());

        switch (cuad.operator) {
            case "ASSIGN":
                translateAssign(cuad);
                break;
            case "+":
            case "-":
            case "*":
            case "/":
                translateArithmetic(cuad);
                break;
            
            // --- INICIO DE CORRECCIONES ---
            
            // 1. AÑADIDO: Manejo de Operadores Relacionales
            case "<":
            case ">":
            case "<=":
            case ">=":
            case "==":
            case "!=":
                translateRelational(cuad);
                break;

            // 2. AÑADIDO: Manejo de Llamadas a Función (PARAM)
            case "PARAM":
                translateParam(cuad);
                break;
                
            // 3. AÑADIDO: Manejo de Llamadas a Función (CALL)
            case "CALL":
                translateCall(cuad);
                break;

            // 4. AÑADIDO: Manejo de Directivas (no generan código)
            case "PARAM_IN":
            case "END_FUNCTION":
                assemblyCode.add("\t; (Directiva: " + cuad.operator + ")");
                break;
                
            // --- FIN DE CORRECCIONES ---

            case "JUMPIF_FALSE":
                translateJumpIfFalse(cuad);
                break;
            case "GOTO":
                assemblyCode.add("\tJUMP " + cuad.operand1);
                break;
            case "LABEL":
                assemblyCode.add(cuad.operand1 + ":"); // Define una etiqueta
                break;
            case "RETURN":
                translateReturn(cuad);
                break;
            default:
                assemblyCode.add("\t; ERROR: Cuádrupla no implementada: " + cuad.operator);
        }
    }
    
    // ----------------------------------------------------------------
    // MÉTODOS DE TRADUCCIÓN (Existentes)
    // ----------------------------------------------------------------

    private void translateAssign(Cuadrupla cuad) {
        String srcRegister = loadValue(cuad.operand1, REG_T1);
        storeValue(srcRegister, cuad.result);
    }

    private void translateArithmetic(Cuadrupla cuad) {
        String reg1 = loadValue(cuad.operand1, REG_T1);
        String reg2 = loadValue(cuad.operand2, REG_T2);

        String opCode;
        switch (cuad.operator) {
            case "+": opCode = "ADD"; break;
            case "-": opCode = "SUB"; break;
            case "*": opCode = "MUL"; break;
            case "/": opCode = "DIV"; break;
            default: opCode = "NOP";
        }

        assemblyCode.add(String.format("\t%s %s, %s, %s", opCode, REG_RESULT, reg1, reg2));
        storeValue(REG_RESULT, cuad.result);
    }
    
    private void translateJumpIfFalse(Cuadrupla cuad) {
        String condReg = loadValue(cuad.operand1, REG_T1);
        assemblyCode.add(String.format("\tCMP %s, #0", condReg)); 
        assemblyCode.add(String.format("\tJUMPEQ %s", cuad.result));
    }
    
    private void translateReturn(Cuadrupla cuad) {
        loadValue(cuad.operand1, REG_RESULT);
        assemblyCode.add("\tRET");
    }

    // ----------------------------------------------------------------
    // MÉTODOS DE TRADUCCIÓN (NUEVOS)
    // ----------------------------------------------------------------

    /**
     * Traduce cuádruplas relacionales (ej. <, ==) a código ensamblador.
     * El resultado (tX) será 1 si es verdadero, 0 si es falso.
     */
    private void translateRelational(Cuadrupla cuad) {
        // Cuádrupla: (OP, op1, op2, res) -> res = (op1 OP op2)
        String reg1 = loadValue(cuad.operand1, REG_T1);
        String reg2 = loadValue(cuad.operand2, REG_T2);

        String jumpInstruction;
        switch (cuad.operator) {
            case "<": jumpInstruction = "JUMPLT"; break; // Jump if Less Than
            case ">": jumpInstruction = "JUMPGT"; break; // Jump if Greater Than
            case "==": jumpInstruction = "JUMPEQ"; break; // Jump if Equal
            case "!=": jumpInstruction = "JUMPNE"; break; // Jump if Not Equal
            case "<=": jumpInstruction = "JUMPLE"; break; // Jump if Less/Equal
            case ">=": jumpInstruction = "JUMPGE"; break; // Jump if Greater/Equal
            default: jumpInstruction = "JUMP"; // No debería ocurrir
        }

        // Crear etiquetas únicas para el flujo
        String labelTrue = "L_TRUE_" + (currentOffset); 
        String labelEnd = "L_END_" + (currentOffset);

        // 1. Comparar los dos registros
        assemblyCode.add(String.format("\tCMP %s, %s", reg1, reg2));
        // 2. Saltar a la etiqueta "True" si la condición se cumple
        assemblyCode.add(String.format("\t%s %s", jumpInstruction, labelTrue));
        
        // 3. Rama "False": Cargar 0 (falso) en el resultado
        assemblyCode.add(String.format("\tLOADI %s, #0", REG_RESULT));
        assemblyCode.add(String.format("\tJUMP %s", labelEnd));
        
        // 4. Rama "True": Cargar 1 (verdadero) en el resultado
        assemblyCode.add(labelTrue + ":");
        assemblyCode.add(String.format("\tLOADI %s, #1", REG_RESULT));
        
        // 5. Fin
        assemblyCode.add(labelEnd + ":");
        // 6. Almacenar el resultado (1 o 0) en la variable temporal 'res'
        storeValue(REG_RESULT, cuad.result);
    }

    /**
     * Traduce la cuádrupla PARAM (simula empujar un argumento a la pila).
     */
    private void translateParam(Cuadrupla cuad) {
        // Cuádrupla: (PARAM, op1, null, null)
        String reg = loadValue(cuad.operand1, REG_T1);
        // Simula empujar el parámetro a la pila de la función llamada
        assemblyCode.add(String.format("\tPUSH %s", reg)); 
    }

    /**
     * Traduce la cuádrupla CALL (salto a función).
     */
    private void translateCall(Cuadrupla cuad) {
        // Cuádrupla: (CALL, "factorial", "1", "t2")
        String functionName = cuad.operand1;
        String numArgs = cuad.operand2; // No lo usamos, pero es bueno saberlo
        String resultTemp = cuad.result;

        assemblyCode.add(String.format("\tCALL %s", functionName));
        
        // Guardar el resultado (que la función dejó en R0)
        storeValue(REG_RESULT, resultTemp);
    }

    // ----------------------------------------------------------------
    // MÉTODOS DE MANEJO DE MEMORIA (SIMULADO)
    // ----------------------------------------------------------------
    
    // Mapea y devuelve la instrucción de carga
    private String loadValue(String operand, String targetReg) {
        try {
            // Si es un literal numérico
            int value = Integer.parseInt(operand);
            assemblyCode.add(String.format("\tLOADI %s, #%d", targetReg, value)); // LOADI: Carga Inmediata
            return targetReg;
        } catch (NumberFormatException e) {
            // Si es "true" o "false" (Manejado como 1 y 0)
            if (operand.equals("true")) {
                 assemblyCode.add(String.format("\tLOADI %s, #1", targetReg));
                 return targetReg;
            }
            if (operand.equals("false")) {
                 assemblyCode.add(String.format("\tLOADI %s, #0", targetReg));
                 return targetReg;
            }
            
            // Si es una variable (ID o Temporal tX)
            if (!stackOffset.containsKey(operand)) {
                stackOffset.put(operand, currentOffset);
                currentOffset += 4; // Avanzar 4 bytes
            }
            int offset = stackOffset.get(operand);
            assemblyCode.add(String.format("\tLOAD %s, [SP + %d]", targetReg, offset)); // LOAD: Carga desde Stack Pointer (SP)
            return targetReg;
        }
    }
    
    // Mapea y devuelve la instrucción de almacenamiento
    private void storeValue(String sourceReg, String targetVar) {
        if (!stackOffset.containsKey(targetVar)) {
            stackOffset.put(targetVar, currentOffset);
            currentOffset += 4;
        }
        int offset = stackOffset.get(targetVar);
        assemblyCode.add(String.format("\tSTORE %s, [SP + %d]", sourceReg, offset)); // STORE: Almacena en Stack Pointer (SP)
    }
}