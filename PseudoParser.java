import java.util.ArrayList;


public class PseudoParser {

    private ArrayList<Token> tokens;
    private int indiceToken = 0;
    private SyntaxException ex;
    private TablaSimbolos ts;
    private TipoIncorporado real;
    private PseudoGenerador generador;



    public PseudoParser(TablaSimbolos ts, PseudoGenerador generador) {
        this.ts = ts;
        this.generador = generador;
    }


    public void analizar(PseudoLexer lexer) throws SyntaxException{
        tokens = lexer.getTokens();

        real = new TipoIncorporado("real");
        ts.definir(real);

        if(Programa()){
            if(indiceToken == tokens.size()) {
                System.out.println("\nLa sintaxis del programa es correcta");
                return;
            }
        }
        throw ex;
    }


    private boolean Programa() {
        if(match("INICIOPROGRAMA"))
            if(Enunciados())
                if(match("FINPROGRAMA")){
                    generador.crearTuplaFinPrograma();
                    return true;
                }
        return false;
    }

    private boolean Asignacion(){
        int indiceAux = indiceToken;

        if(match("VARIABLE"))
            if(match("IGUAL"))
                if(Expresion()){
                    generador.crearTuplaAsignacion(indiceAux, indiceToken);
                    return true;
                }
        indiceToken = indiceAux;
        return false;
    }

    private boolean Leer(){
        int indiceAux = indiceToken;

        if(match("LEER"))
            if(match("VARIABLE")){
                generador.crearTuplaLeer(indiceAux+1);
                return true;
            }

        indiceToken = indiceAux;
        return false;
    }

    private boolean Escribir(){
        int indiceAux = indiceToken;


        if(match("ESCRIBIR"))
            if(match("CADENA"))
                if(match("COMA"))
                    if(match("VARIABLE")){
                        generador.crearTuplaEscribir(indiceAux+1, indiceToken);
                        return true;
                    }

        indiceToken = indiceAux;


        if(match("ESCRIBIR"))
            if(match("CADENA")){
                generador.crearTuplaEscribir(indiceAux+1, indiceToken);
                return true;
            }
        indiceToken = indiceAux;


        if(match("ESCRIBIR"))
            if(match("VARIABLE")){
                generador.crearTuplaEscribir(indiceAux+1, indiceToken);
                return true;
            }

        indiceToken = indiceAux;
        return false;
    }

    private boolean Si(){
        int indiceAux = indiceToken;
        int indiceTupla = generador.getTuplas().size();

        if(match("SI"))
            if(Comparacion())
                if(match("ENTONCES"))
                    if(Enunciados())
                        if(match("FINSI")){
                            generador.conectarSi(indiceTupla);
                            return true;
                        }

        indiceToken = indiceAux;
        return false;
    }

    private boolean Mientras(){
        int indiceAux = indiceToken;
        int indiceTupla = generador.getTuplas().size();

        if (match("MIENTRAS"))
            if(Comparacion())
                if(Enunciados())
                    if(match("FINMIENTRAS")){
                        generador.conectarMientras(indiceTupla);
                        return true;
                    }
        indiceToken = indiceAux;
        return false;
    }

    private boolean Comparacion(){
        int indiceAux = indiceToken;


        if(match("PARENTESISIZQ"))
            if(Valor())
                if(match("OPRELACIONAL"))
                    if(Valor())
                        if(match("PARENTESISDER")){
                            generador.crearTuplaComparacion(indiceAux+1);
                            return true;
                        }
        indiceToken = indiceAux;
        return false;
    }





    private boolean match(String tipoEsperado) {
        if (indiceToken >= tokens.size()) {

            ex = new SyntaxException("Error: Se esperaba '" + tipoEsperado + "' pero se encontró el fin del programa.");
            return false;
        }

        Token tokenActual = tokens.get(indiceToken);
        if (tokenActual.getTipo().getNombre().equals(tipoEsperado)) {
            indiceToken++;
            return true;
        } else {

            ex = new SyntaxException("Error en la línea " + 0 + ": Se esperaba '" + tipoEsperado + "' pero se encontró '" + tokenActual.getLexema() + "'.");
            return false;
        }
    }



    private String verTipoActual() {
        if (indiceToken >= tokens.size()) {
            return "EOF";
        }
        return tokens.get(indiceToken).getTipo().getNombre();
    }




    private boolean Enunciados() {
        if (!Enunciado()) {
            return false;
        }


        while (true) {
            String tipo = verTipoActual();

            if (tipo.equals("LEER") || tipo.equals("ESCRIBIR") || tipo.equals("VARIABLE") || tipo.equals("SI") || tipo.equals("MIENTRAS")) {
                if (!Enunciado()) {
                    return false;
                }
            } else {

                break;
            }
        }
        return true;
    }




    private boolean Enunciado() {
        String tipo = verTipoActual();

        if (tipo.equals("LEER")) {
            return Leer();
        } else if (tipo.equals("ESCRIBIR")) {
            return Escribir();
        } else if (tipo.equals("VARIABLE")) {
            return Asignacion();
        } else if (tipo.equals("SI")) {
            return Si();
        } else if (tipo.equals("MIENTRAS")) {
            return Mientras();
        }


        ex = new SyntaxException("Error: Se esperaba un enunciado (LEER, ESCRIBIR, VARIABLE, SI, MIENTRAS) pero se encontró '" + verTipoActual() + "'.");
        return false;
    }




    private boolean Expresion() {
        if (!Valor()) {
            return false;
        }


        if (verTipoActual().equals("OPARITMETICO")) {
            if (!match("OPARITMETICO")) {
                return false;
            }
            if (!Valor()) {
                ex = new SyntaxException("Error: Se esperaba VARIABLE o NUMERO después del operador aritmético.");
                return false;
            }
        }
        return true;
    }




    private boolean Valor() {
        if (verTipoActual().equals("VARIABLE")) {
            return match("VARIABLE");
        } else if (verTipoActual().equals("NUMERO")) {
            return match("NUMERO");
        }

        ex = new SyntaxException("Error: Se esperaba VARIABLE o NUMERO pero se encontró '" + verTipoActual() + "'.");
        return false;
    }
}