package PatronParserRecursivo;

import java.util.ArrayList;

public class PseudoParser {
    private ArrayList<Token> tokens;
    private int indiceToken = 0;
    private SyntaxException syntaxEx;
    
    // [CAMBIO] Reemplazamos TablaSimbolos por Scope
    private Scope currentScope; 
    private TipoIncorporado tipoReal; 

    // Método para obtener el scope actual (para pruebas)
    public Scope getCurrentScope(){
        return currentScope;
    }

    public void analizar(PseudoLexer lexer) throws SyntaxException, SemanticException {
        tokens = lexer.getTokens();
        indiceToken = 0;
        syntaxEx = null;

        // [CAMBIO] Inicializamos el Scope Global
        currentScope = new GlobalScope();
        
        // Definimos el tipo base "real" en el scope global
        tipoReal = new TipoIncorporado("real");
        currentScope.define(tipoReal);

        if (Programa()) {
            if (indiceToken == tokens.size()) {
                System.out.println("Análisis Sintáctico y Semántico Correcto");
                return;
            }
        }
        throw syntaxEx;
    }

    // <Programa>
    private boolean Programa() throws SemanticException {
        if (match("INICIOPROGRAMA")) {
            if (DeclaracionVariables()) {
                if (Enunciados()) {
                    if (match("FINPROGRAMA")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // <DeclaracionVariables>
    private boolean DeclaracionVariables() throws SemanticException {
        int indiceAux = indiceToken;
        if (match("DECLARACIONVARIABLES")) {
            if (match("DOSPUNTOS")) {
                if (ListaVariables()) {
                    return true;
                }
            }
        }
        indiceToken = indiceAux;
        return true; 
    }

    // <ListaVariables>
    private boolean ListaVariables() throws SemanticException {
        if (indiceToken < tokens.size() && tokens.get(indiceToken).getTipo().getNombre().equals("VARIABLE")) {
            String nombreVar = tokens.get(indiceToken).getLexema();
            
            // [CAMBIO] Definimos en el currentScope
            // Nota: Para verificar duplicados locales, podríamos checar antes currentScope.resolve
            try {
                // Aquí podrías validar si ya existe SOLO en el scope actual antes de definir
                Variable v = new Variable(nombreVar, tipoReal);
                currentScope.define(v); 
            } catch (Exception e) {
                throw new SemanticException(e.getMessage());
            }

            match("VARIABLE");

            if (match("COMA")) {
                return ListaVariables();
            }
            return true;
        }
        return false;
    }

    // <Enunciados>
    private boolean Enunciados() throws SemanticException {
        if (Enunciado()) {
            Enunciados(); 
            return true;
        }
        return true; 
    }

    // <Enunciado>
    private boolean Enunciado() throws SemanticException {
        int indiceAux = indiceToken;
        if (indiceToken >= tokens.size()) return false;
        
        // Asignacion
        if(tokens.get(indiceToken).getTipo().getNombre().equals("VARIABLE")) {
            if (Asignacion()) return true;
        }
        // Leer
        indiceToken = indiceAux;
        if (indiceToken < tokens.size() && tokens.get(indiceToken).getTipo().getNombre().equals("LEER")) {
            if (Leer()) return true;
        }
        // Escribir
        indiceToken = indiceAux;
        if (indiceToken < tokens.size() && tokens.get(indiceToken).getTipo().getNombre().equals("ESCRIBIR")) {
            if (Escribir()) return true;
        }
        // Si
        indiceToken = indiceAux;
        if (indiceToken < tokens.size() && tokens.get(indiceToken).getTipo().getNombre().equals("SI")) {
            if (Si()) return true;
        }
        // Mientras
        indiceToken = indiceAux;
        if (indiceToken < tokens.size() && tokens.get(indiceToken).getTipo().getNombre().equals("MIENTRAS")) {
            if (Mientras()) return true;
        }
        
        indiceToken = indiceAux;
        return false;
    }

    // <Asignacion> -> VARIABLE = <Expresion>
    private boolean Asignacion() throws SemanticException {
        int indiceAux = indiceToken;
        if (indiceToken < tokens.size() && tokens.get(indiceToken).getTipo().getNombre().equals("VARIABLE")) {
            String nombreVar = tokens.get(indiceToken).getLexema();
            
            // [CAMBIO] Usamos currentScope.resolve() para buscar global o localmente
            if (currentScope.resolve(nombreVar) == null) {
                throw new SemanticException("Error Semántico: La variable '" + nombreVar + "' no ha sido declarada.");
            }

            match("VARIABLE");
            if (match("IGUAL")) {
                if (Expresion()) return true;
            }
        }
        indiceToken = indiceAux;
        return false;
    }

    // ... Expresion y Valor usan la misma lógica de currentScope.resolve ...
    private boolean Expresion() throws SemanticException {
        int indiceAux = indiceToken;
        if (Valor()) {
            if (match("OPARITMETICO")) {
                if (Valor()) return true;
            } else {
                return true; 
            }
        }
        indiceToken = indiceAux;
        return false;
    }

    private boolean Valor() throws SemanticException {
        if (indiceToken < tokens.size() && tokens.get(indiceToken).getTipo().getNombre().equals("VARIABLE")) {
            String nombreVar = tokens.get(indiceToken).getLexema();
            // [CAMBIO] Validación de alcance
            if (currentScope.resolve(nombreVar) == null) {
                throw new SemanticException("Error Semántico: Uso de variable no declarada '" + nombreVar + "'.");
            }
            return match("VARIABLE");
        }
        return match("NUMERO");
    }

    // ... Leer y Escribir también ...
    private boolean Leer() throws SemanticException {
        int indiceAux = indiceToken;
        if (match("LEER")) {
            if (indiceToken < tokens.size() && tokens.get(indiceToken).getTipo().getNombre().equals("VARIABLE")) {
                String nombreVar = tokens.get(indiceToken).getLexema();
                if (currentScope.resolve(nombreVar) == null) {
                    throw new SemanticException("Error Semántico: Variable no declarada '" + nombreVar + "'.");
                }
                match("VARIABLE");
                return true;
            }
        }
        indiceToken = indiceAux;
        return false;
    }

    private boolean Escribir() throws SemanticException {
        int indiceAux = indiceToken;
        // ESCRIBIR CADENA COMA VARIABLE
        if (match("ESCRIBIR")) {
            if (match("CADENA")) {
                if (match("COMA")) {
                    if (indiceToken < tokens.size() && tokens.get(indiceToken).getTipo().getNombre().equals("VARIABLE")) {
                        String nombreVar = tokens.get(indiceToken).getLexema();
                        if (currentScope.resolve(nombreVar) == null) {
                            throw new SemanticException("Error Semántico: Variable no declarada '" + nombreVar + "'.");
                        }
                        match("VARIABLE");
                        return true;
                    }
                }
            }
        }
        // ... (resto de lógica Escribir igual, usando resolve) ...
        indiceToken = indiceAux;
        // ESCRIBIR CADENA
        if (match("ESCRIBIR")) {
             if (match("CADENA")) return true;
        }
        
        indiceToken = indiceAux;
        // ESCRIBIR VARIABLE
        if (match("ESCRIBIR")) {
             if (indiceToken < tokens.size() && tokens.get(indiceToken).getTipo().getNombre().equals("VARIABLE")) {
                String nombreVar = tokens.get(indiceToken).getLexema();
                if (currentScope.resolve(nombreVar) == null) {
                    throw new SemanticException("Error Semántico: Variable no declarada '" + nombreVar + "'.");
                }
                match("VARIABLE");
                return true;
             }
        }
        
        indiceToken = indiceAux;
        return false;
    }

    // [CAMBIO IMPORTANTE] <Si> con Scope Anidado
    private boolean Si() throws SemanticException {
        int indiceAux = indiceToken;

        if (match("SI")) {
            if (Condicion()) {
                if (match("ENTONCES")) {
                    
                    // [PUSH] Creamos un nuevo scope local
                    System.out.println(">>> Entrando a Scope LOCAL (SI)");
                    currentScope = new LocalScope(currentScope);
                    
                    if (Enunciados()) {
                        if (match("FINSI")) {
                            // [POP] Regresamos al scope anterior
                            System.out.println("<<< Saliendo de Scope LOCAL (SI)");
                            currentScope = currentScope.getEnclosingScope();
                            return true;
                        }
                    }
                }
            }
        }
        indiceToken = indiceAux;
        return false;
    }

    // [CAMBIO IMPORTANTE] <Mientras> con Scope Anidado
    private boolean Mientras() throws SemanticException {
        int indiceAux = indiceToken;

        if (match("MIENTRAS")) {
            if (Condicion()) {
                
                // [PUSH] Creamos un nuevo scope local
                System.out.println(">>> Entrando a Scope LOCAL (MIENTRAS)");
                currentScope = new LocalScope(currentScope);
                
                if (Enunciados()) {
                    if (match("FINMIENTRAS")) {
                        // [POP] Regresamos al scope anterior
                        System.out.println("<<< Saliendo de Scope LOCAL (MIENTRAS)");
                        currentScope = currentScope.getEnclosingScope();
                        return true;
                    }
                }
            }
        }
        indiceToken = indiceAux;
        return false;
    }

    // <Condicion> (Igual que antes)
    private boolean Condicion() throws SemanticException {
        int indiceAux = indiceToken;
        if(match("PARENTESISIZQ")) {
            if (Valor()) {
                if (match("OPRELACIONAL")) {
                    if (Valor()) {
                        if(match("PARENTESISDER")) return true;
                    }
                }
            }
        }
        indiceToken = indiceAux;
        return false;
    }

    private boolean match(String nombre){
        if (indiceToken >= tokens.size()) {
            if (syntaxEx == null)
                syntaxEx = new SyntaxException(nombre, "EOF");
            return false;
        }
        if (tokens.get(indiceToken).getTipo().getNombre().equals(nombre)){
            indiceToken++;
            return true;
        }
        if (syntaxEx == null)
            syntaxEx = new SyntaxException(nombre, tokens.get(indiceToken).getTipo().getNombre());
        return false;
    }
}