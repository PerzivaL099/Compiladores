public class Asignacion extends Tupla {
    Token variable, valor1, valor2, operador;

    public Asignacion(Token variable, Token valor, int sv, int sf){
        super(sv, sf);
        this.variable = variable;
        this.valor1 = valor;
    }


    public Asignacion(Token variable, Token valor1, Token operador, Token valor2, int sv, int sf){ // <-- Acepta 4 tokens
        super(sv, sf);
        this.variable = variable;
        this.valor1 = valor1;
        this.operador = operador;
        this.valor2 = valor2;
    }

    @Override
    public String toString() {
        // Caso 1: Asignación simple (ej: i = 0)
        if (operador == null) {
            return "( " + super.toString() + " , [ " + variable + ", " + valor1 + " ] )";
        }
        // Caso 2: Asignación compleja (ej: i = i + 1)
        else {

            return "( " + super.toString() + " , [ " + variable + ", " + valor1 + ", " + operador + ", " + valor2 + " ] )";
        }
    }
}
