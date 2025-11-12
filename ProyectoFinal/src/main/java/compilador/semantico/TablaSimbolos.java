package compilador.semantico;

import java.util.Stack;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
// Importar los nodos AST que necesitamos almacenar
import compilador.parser.declarations.FunctionDeclaration;
import compilador.parser.declarations.Parameter;

/**
 * Estructura de datos para la Tabla de Símbolos.
 * Gestiona ámbitos (scopes) usando una pila de mapas para variables
 * y un mapa global para funciones.
 */
public class TablaSimbolos {

    // Pila de mapas para variables (maneja ámbitos locales y globales)
    private final Stack<Map<String, Simbolo>> scopes;
    
    // Mapa global solo para firmas de funciones
    private final Map<String, FunctionDeclaration> functions;

    public TablaSimbolos() {
        this.scopes = new Stack<>();
        this.functions = new HashMap<>();
        openScope(); // Inicia el ámbito global
    }

    // --- MÉTODOS DE MANEJO DE ÁMBITOS ---

    /**
     * Abre un nuevo ámbito (al entrar a un bloque {} o función).
     * (Requerido por SemanticAnalyzer)
     */
    public void openScope() {
        scopes.push(new HashMap<>());
    }
    
    /**
     * Sobrecarga para nombrar un ámbito (útil para funciones).
     * (Requerido por SemanticAnalyzer)
     */
    public void openScope(String scopeName) {
        scopes.push(new HashMap<>());
    }

    /**
     * Cierra el ámbito actual (al salir de un bloque {} o función).
     * (Requerido por SemanticAnalyzer)
     */
    public void closeScope() {
        if (!scopes.isEmpty()) {
            scopes.pop();
        }
    }

    // --- MÉTODOS DE DECLARACIÓN (Requeridos por SemanticAnalyzer) ---

    /**
     * Declara una variable en el ámbito actual.
     * (Requerido por SemanticAnalyzer)
     */
    public void declareVariable(String id, String type) {
        if (scopes.isEmpty()) return;
        // Asume que SemanticAnalyzer ya verificó 'isDeclaredInCurrentScope'
        scopes.peek().put(id, new Simbolo(id, type, "variable"));
    }

    /**
     * Declara una función guardando su nodo AST completo.
     * (Requerido por SemanticAnalyzer)
     */
    public void declareFunction(FunctionDeclaration funcNode) {
        if (functions.containsKey(funcNode.id)) {
            // El error de "función ya declarada" lo maneja el SemanticAnalyzer
            return;
        }
        functions.put(funcNode.id, funcNode);
    }


    // --- MÉTODOS DE BÚSQUEDA (Requeridos por SemanticAnalyzer) ---

    /**
     * Verifica si un ID ya está declarado EN EL ÁMBITO ACTUAL.
     * (Requerido por SemanticAnalyzer)
     */
    public boolean isDeclaredInCurrentScope(String id) {
        if (scopes.isEmpty()) return false;
        return scopes.peek().containsKey(id);
    }

    /**
     * Busca una variable en todos los ámbitos, desde el actual hasta el global.
     * (Requerido por SemanticAnalyzer)
     * @return El tipo (String) de la variable, o null si no se encuentra.
     */
    public String lookupVariable(String id) {
        // Itera la pila de arriba a abajo (del ámbito actual al global)
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Map<String, Simbolo> scope = scopes.get(i);
            if (scope.containsKey(id)) {
                Simbolo s = scope.get(id);
                // Asegurarse de que es una variable y no otro tipo de símbolo
                if (s.kind.equals("variable")) {
                    return s.type;
                }
            }
        }
        return null; // No se encontró
    }

    /**
     * Busca una función en el mapa global de funciones.
     * (Requerido por SemanticAnalyzer)
     * @return El nodo FunctionDeclaration, o null si no se encuentra.
     */
    public FunctionDeclaration lookupFunction(String id) {
        return functions.get(id);
    }
    
    // --- CLASE INTERNA SIMBOLO ---
    /**
     * Representa un símbolo (variable) en la tabla.
     * (Movido aquí para encapsulación)
     */
    private class Simbolo {
        public final String id;
        public final String type; // "int", "boolean"
        public final String kind; // "variable"

        public Simbolo(String id, String type, String kind) {
            this.id = id;
            this.type = type;
            this.kind = kind;
        }
    }
}