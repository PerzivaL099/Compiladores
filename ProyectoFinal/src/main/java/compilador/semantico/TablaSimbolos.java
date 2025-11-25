package compilador.semantico;

import java.util.Stack;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection; 
import java.util.ArrayList; 
// Importar los nodos AST que necesitamos almacenar
import compilador.parser.declarations.FunctionDeclaration;
import compilador.parser.declarations.Parameter;

/**
 * Estructura de datos para la Tabla de Símbolos.
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

    // ⭐ CLASE INTERNA CORREGIDA ⭐
    /**
     * Representa un símbolo (variable) en la tabla.
     */
    public class Simbolo {
        public final String id;
        public final String type; // "int", "boolean"
        public final String kind; // "variable"
        // ⭐ NUEVOS CAMPOS AÑADIDOS ⭐
        public final String scope;
        public final String address; 

        // ⭐ CONSTRUCTOR CORREGIDO: Acepta todos los campos para el DTO ⭐
        public Simbolo(String id, String type, String kind, String scope, String address) {
            this.id = id;
            this.type = type;
            this.kind = kind;
            this.scope = scope;
            this.address = address;
        }

        // Getters para el mapeo a SimboloDTO
        public String getName() { return id; }
        public String getType() { return type; }
        public String getScope() { return scope; }
        public String getAddress() { return address; }
    }
    
    
    /**
     * Devuelve todos los símbolos de todos los ámbitos (aplanado) para la serialización.
     */
    public Collection<Simbolo> getAllSimbolos() {
        Collection<Simbolo> allSimbolos = new ArrayList<>();
        // Recorre todos los ámbitos y añade sus símbolos a una lista plana
        for (Map<String, Simbolo> scope : scopes) {
            allSimbolos.addAll(scope.values());
        }
        return allSimbolos;
    }

    // --- MÉTODOS DE MANEJO DE ÁMBITOS ---

    public void openScope() {
        scopes.push(new HashMap<>());
    }
    
    public void openScope(String scopeName) {
        scopes.push(new HashMap<>());
    }

    public void closeScope() {
        if (!scopes.isEmpty()) {
            scopes.pop();
        }
    }
    
    // --- MÉTODOS DE DECLARACIÓN ---

    // ⭐ MÉTODO CORREGIDO: Llama al constructor de Simbolo con datos completos ⭐
    public void declareVariable(String id, String type) {
        if (scopes.isEmpty()) return;
        
        // 1. Obtener Ámbito y Dirección (Lógica de Generación de Código y Semántica)
        String currentScope = getCurrentScopeName();
        
        // Asignación de Dirección: Usamos el tamaño actual del ámbito como offset
        int offset = scopes.peek().size() * 4; // Asumimos 4 bytes por variable
        String address = "FP + " + offset;
        
        // ⭐ LLAMADA AL CONSTRUCTOR CORREGIDO ⭐
        scopes.peek().put(id, new Simbolo(id, type, "variable", currentScope, address));
    }

    public void declareFunction(FunctionDeclaration funcNode) {
        if (functions.containsKey(funcNode.id)) {
            return;
        }
        functions.put(funcNode.id, funcNode);
    }

    // --- MÉTODOS DE BÚSQUEDA ---

    public boolean isDeclaredInCurrentScope(String id) {
        if (scopes.isEmpty()) return false;
        return scopes.peek().containsKey(id);
    }

    public String lookupVariable(String id) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Map<String, Simbolo> scope = scopes.get(i);
            if (scope.containsKey(id)) {
                Simbolo s = scope.get(id);
                if (s.kind.equals("variable")) {
                    return s.type;
                }
            }
        }
        return null;
    }

    public FunctionDeclaration lookupFunction(String id) {
        return functions.get(id);
    }
    
    // Método auxiliar para el Simbolo (simple, asume solo dos niveles)
    private String getCurrentScopeName() {
        return scopes.size() == 1 ? "global" : "local";
    }
}