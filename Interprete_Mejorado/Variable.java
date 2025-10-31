public class Variable extends Simbolo{

    private float valor = 0;

   
    public Variable(String nombre, TipoToken tipo) {
        
        super(nombre, tipo); 
    }
    
    public void setValor(float valor){
        this.valor = valor;
    }

    public float getValor(){
        return this.valor;
    }

}
