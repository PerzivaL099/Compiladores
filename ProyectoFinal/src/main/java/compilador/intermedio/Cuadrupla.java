package compilador.intermedio;

public class Cuadrupla{

    //Operadores de tipo SUM, ASSIGN, JUMPIF, GOTO, RETURN
    public final String  operator;
    public final String operand1;
    public final String operand2;
    public final String result;

    //Constructor
    public Cuadrupla(String operator, String operand1, String operand2, String result){
        this.operator = operator;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.result = result;
    }

    @Override
    public String toString(){
        //Formato
        return String.format("[%s, %s, %s, %s]", 
                             operator, 
                             (operand1 == null ? "null" : operand1), 
                             (operand2 == null ? "null" : operand2), 
                             (result == null ? "null" : result));
    }
}