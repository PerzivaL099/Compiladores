public class Escribir extends Tupla{
    Token cadena, variable;

    public Escribir(Token variableCadena, int sv, int sf){
        super(sv,sf);

        if(variableCadena.getTipo().getNombre().equals("CADENA"))
            cadena = variableCadena;
        else
            cadena = variableCadena;
    }

    public Escribir(Token cadena, Token variable, int sv, int sf){
        super(sv,sf);
        this.cadena = cadena;
        this.variable = variable;
    }

    @Override //Sobrescribir funcion heredada
    public String toString(){
        if(variable == null) {

            return "( " + super.toString() + " , [ " + cadena + " ] )";
        }
        else if (cadena == null) {

            return "( " + super.toString() + " , [ " + variable + " ] )";
        }
        else {

            return "( " + super.toString() + " , [ " + cadena + ", " + variable + " ] )";
        }
    }


}
