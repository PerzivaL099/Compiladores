public class Comparacion extends Tupla{
    Token valor1, valor2, operador;

    public Comparacion(Token valor1, Token operador, Token valor2, int sv, int sf){
        super(sv, sf);
        this.valor1 = valor1;
        this.operador = operador;
        this.valor2 = valor2;
    }

    public String toString(){
        return "( " + super.toString() + ", [ " + valor1 + ", " + operador + ", " + valor2 + " ] ) ";
    }
}
