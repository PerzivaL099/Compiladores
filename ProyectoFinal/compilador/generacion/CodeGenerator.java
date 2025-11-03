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
            // ... (Otros operadores)
            default:
                assemblyCode.add("\t; ERROR: Cuádrupla no implementada: " + cuad.operator);
        }
    }
    
    // ----------------------------------------------------------------
    // EJEMPLOS DE TRADUCCIÓN
    // ----------------------------------------------------------------

    private void translateAssign(Cuadrupla cuad) {
        // Cuádrupla: (ASSIGN, op1, null, res) -> res = op1
        
        // 1. Cargar el valor (op1) a un registro temporal
        String srcRegister = loadValue(cuad.operand1, REG_T1);

        // 2. Almacenar el valor del registro en la ubicación de la variable (res)
        storeValue(srcRegister, cuad.result);
    }

    private void translateArithmetic(Cuadrupla cuad) {
        // Cuádrupla: (OP, op1, op2, res) -> res = op1 OP op2

        // 1. Cargar operandos a registros
        String reg1 = loadValue(cuad.operand1, REG_T1);
        String reg2 = loadValue(cuad.operand2, REG_T2);

        String opCode;
        switch (cuad.operator) {
            case "+": opCode = "ADD"; break;
            case "-": opCode = "SUB"; break;
            case "*": opCode = "MUL"; break;
            case "/": opCode = "DIV"; break;
            default: opCode = "NOP"; // No Operation
        }

        // 2. Realizar la operación y dejar el resultado en REG_RESULT
        assemblyCode.add(String.format("\t%s %s, %s, %s", opCode, REG_RESULT, reg1, reg2));

        // 3. Almacenar el resultado (REG_RESULT) en la variable 'res' de la cuádrupla (que suele ser un temporal tX)
        storeValue(REG_RESULT, cuad.result);
    }
    
    private void translateJumpIfFalse(Cuadrupla cuad) {
        // Cuádrupla: (JUMPIF_FALSE, cond, null, L_TARGET) -> IF cond==FALSE GOTO L_TARGET

        // 1. Cargar la condición a un registro
        String condReg = loadValue(cuad.operand1, REG_T1);

        // 2. Generar la instrucción de salto condicional (simulado: verifica si es 0/False)
        assemblyCode.add(String.format("\tCMP %s, #0", condReg)); // Compara el registro con cero
        assemblyCode.add(String.format("\tJUMPEQ %s", cuad.result)); // Si es igual a cero (falso), salta a la etiqueta
    }
    
    private void translateReturn(Cuadrupla cuad) {
        // 1. Cargar el valor de retorno al registro de resultado estándar (R0)
        loadValue(cuad.operand1, REG_RESULT);
        
        // 2. Simular el salto al final de la función para la limpieza (ya manejado por GOTO en InterCodeGenerator)
        assemblyCode.add("\tRET"); // Instrucción de retorno simulada
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
            // Si es una variable (ID o Temporal tX)
            if (!stackOffset.containsKey(operand)) {
                // Asignar una nueva posición en el stack (para variables nuevas/temporales)
                stackOffset.put(operand, currentOffset);
                currentOffset += 4; // Avanzar 4 bytes (simulación de enteros de 32 bits)
            }
            int offset = stackOffset.get(operand);
            assemblyCode.add(String.format("\tLOAD %s, [SP + %d]", targetReg, offset)); // LOAD: Carga desde Stack Pointer (SP)
            return targetReg;
        }
    }
    
    // Mapea y devuelve la instrucción de almacenamiento
    private void storeValue(String sourceReg, String targetVar) {
        if (!stackOffset.containsKey(targetVar)) {
             // Asignar una nueva posición si la variable es nueva (como temporales)
            stackOffset.put(targetVar, currentOffset);
            currentOffset += 4;
        }
        int offset = stackOffset.get(targetVar);
        assemblyCode.add(String.format("\tSTORE %s, [SP + %d]", sourceReg, offset)); // STORE: Almacena en Stack Pointer (SP)
    }
}
