package compilador.semantico;

import java.util.Stack;
import java.util.HashMap;
import java.util.Map;

/**
 * Representa la Tabla de Símbolos, gestionando diferentes ámbitos (scopes).
 */
public class TablaSimbolos {

    // La pila de mapas simula los ámbitos anidados.
    // El mapa superior de la pila es el ámbito actual.
    private final Stack<Map<String, Simbolo>> scopes;

    public TablaSimbolos() {
        this.scopes = new Stack<>();
        // Iniciar con el ámbito global
        pushScope(); 
    }

    /**
     * Entra en un nuevo ámbito (e.g., inicio de función, bloque IF/WHILE).
     */
    public void pushScope() {
        scopes.push(new HashMap<>());
    }

    /**
     * Sale del ámbito actual.
     */
    public void popScope() {
        if (!scopes.isEmpty()) {
            scopes.pop();
        }
    }

    /**
     * Agrega un nuevo símbolo al ámbito actual.
     * @return true si el símbolo fue agregado, false si ya existe en el ámbito.
     */
    public boolean addSymbol(String id, String type, String kind) {
        if (scopes.isEmpty()) return false;
        
        Map<String, Simbolo> currentScope = scopes.peek();
        if (currentScope.containsKey(id)) {
            return false; // Error: Símbolo ya declarado en este ámbito
        }
        
        currentScope.put(id, new Simbolo(id, type, kind));
        return true;
    }

    /**
     * Busca un símbolo, empezando por el ámbito más interno hasta el global.
     * @return El objeto Simbolo o null si no se encuentra.
     */
    public Simbolo findSymbol(String id) {
        // Itera la pila de arriba a abajo (del ámbito actual al global)
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(id)) {
                return scopes.get(i).get(id);
            }
        }
        return null; // No encontrado
    }
}

/**
 * Clase interna o separada que representa la información de un identificador.
 */
class Simbolo {
    public final String id;
    public final String type; // ej. "int", "boolean" o tipo de retorno de función
    public final String kind; // ej. "variable", "funcion"

    public Simbolo(String id, String type, String kind) {
        this.id = id;
        this.type = type;
        this.kind = kind;
    }
}