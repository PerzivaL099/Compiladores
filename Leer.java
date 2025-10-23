public class Leer extends Tupla{
    Token variable;

    public Leer(Token variable, int sv, int sf){
        super(sv,sf);
        this.variable = variable;
    }

    public String toString(){
        return "(" + super.toString() + ", [" + variable + " ] )";
    }
}
