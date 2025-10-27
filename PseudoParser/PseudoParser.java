import java.util.ArrayList;;

public class PseudoParser {
    private ArrayList<Token> tokens;
    private int indiceToken = 0;
    private SyntaxException ex;

    public void analizar(PseudoLexer lexer) throws SyntaxException{
        tokens = lexer.getTokens();

        if(Programa()){
            if(indiceToken == tokens.size()){
                System.out.println("\nLa sintaxis del programa es correcta");
                return;
            }
        }
        throw ex;
    }

    private boolean Programa(){
        if(match("INICIOPROGRAMA"))
            if(Enunciados())
                if(match("FINPROGRAMA"))
                    return true;

        return false;
    }

    private boolean Enunciados(){
        int indiceAux = indiceToken;

        if(Enunciado()){
            while(Enunciado());
            return true;
        }

        indiceToken = indiceAux;
        return false;
    }

   // <Enunciado> -> <Asignacion> | <Leer> | <Escribir> | <Si> | <Mientras>
    private boolean Enunciado() {
        int indiceAux = indiceToken;

        // --- INICIO DE LA CORRECCIÓN ---
        // Comprobar si el token actual es el fin del programa
        if (indiceToken < tokens.size() && 
            tokens.get(indiceToken).getTipo().getNombre().equals("FINPROGRAMA")) {
            return false; // No es un enunciado, es el fin
        }
        // --- FIN DE LA CORRECCIÓN ---


        if (tokens.get(indiceToken).getTipo().getNombre().equals("VARIABLE")) {
            if (Asignacion())
                return true;
        }
        
        indiceToken = indiceAux;
        if (tokens.get(indiceToken).getTipo().getNombre().equals("LEER")) {
            if (Leer())
                return true;
        }

        indiceToken = indiceAux;
        if (tokens.get(indiceToken).getTipo().getNombre().equals("ESCRIBIR")) {
            if (Escribir())
                return true;
        }

        indiceToken = indiceAux;
        if (tokens.get(indiceToken).getTipo().getNombre().equals("SI")) {
            if (Si())
                return true;
        }
        
        indiceToken = indiceAux;
        if (tokens.get(indiceToken).getTipo().getNombre().equals("MIENTRAS")) {
            if (Mientras())
                return true;
        }
        
        indiceToken = indiceAux;
        return false;
    }

    private boolean Asignacion(){
        int indiceAux = indiceToken;

        if(match("VARIABLE"))
            if(match("IGUAL"))
                if(Expresion())
                    return true;
        
        indiceToken = indiceAux;
        return false;
    }

    private boolean Expresion(){
        int indiceAux = indiceToken;

        if(Valor())
            if(match("OPARIMETICO"))
                if(Valor())
                    return true;
        
        indiceToken = indiceAux;
        if(Valor())
            return true;

        indiceToken = indiceAux;
        return false;
            
    }

    // <Valor> -> VARIABLE | NUMERO
    private boolean Valor() {
        if (match("VARIABLE") || match("NUMERO")) { 
            return true; 
        }
        return false; 
    } 

    // <Leer> -> leer VARIABLE
    private boolean Leer() {
        int indiceAux = indiceToken; 
        if (match("LEER")) { 
            if (match("VARIABLE")) { 
                return true; 
            }
        }
        indiceToken = indiceAux; 
        return false; 
    } 

    // <Escribir> -> escribir CADENA, VARIABLE | escribir CADENA | escribir VARIABLE
    private boolean Escribir() {
        int indiceAux = indiceToken; 
        if (match("ESCRIBIR")) { 
            if (match("CADENA")) { 
                if (match("COMA")) { 
                    if (match("VARIABLE")) { 
                        return true; 
                    }
                }
            }
        } 
        //Regla 2
        indiceToken = indiceAux; 
        if (match("ESCRIBIR")) { 
            if (match("CADENA")) { 
                return true; 
            }
        }
        //Regla 3
        indiceToken = indiceAux; 
        if (match("ESCRIBIR")) { 
            if (match("VARIABLE")) { 
                return true; 
            }
        }
        indiceToken = indiceAux; 
        return false; 
    }

    // <Si> -> si <Comparacion> entonces <Enunciados> fin-si
    private boolean Si() {
        int indiceAux = indiceToken; 
        if (match("SI")) { 
            if (Comparacion()) { 
                if (match("ENTONCES")) { 
                    if (Enunciados()) { 
                        if (match("FINSI")) { 
                            return true; 
                        }
                    }
                }
            }
        }
        indiceToken = indiceAux; 
        return false; 
    } 

    // <Mientras> -> mientras <Comparacion> <Enunciados> fin-mientras
    private boolean Mientras() {
        int indiceAux = indiceToken; 
        if (match("MIENTRAS")) { 
            if (Comparacion()) { 
                if (Enunciados()) { 
                    if (match("FINMIENTRAS")) { 
                        return true; 
                    }
                }
            }
        }
        indiceToken = indiceAux; 
        return false; 
    } 

    // <Comparacion> -> ( <Valor> <Operador relacional> <Valor> )
    private boolean Comparacion() {
        int indiceAux = indiceToken; 
        if (match("PARENTESISIZQ")) { 
            if (Valor()) { 
                if (match("OPRELACIONAL")) { 
                    if (Valor()) { 
                        if (match("PARENTESISDER")) { 
                            return true; 
                        }
                    }
                }
            }
        }
        indiceToken = indiceAux; 
        return false; 
    } 

    // Método de ayuda para consumir tokens
    private boolean match(String nombre) {
        if (tokens.get(indiceToken).getTipo().getNombre().equals(nombre)) { 
            System.out.println(nombre + ": " + tokens.get(indiceToken).getNombre()); 
            indiceToken++; 
            return true;
        } 
        if (ex == null) { 
            ex = new SyntaxException(nombre, tokens.get(indiceToken).getTipo().getNombre()); 
        }
        return false; 
    } 
}


