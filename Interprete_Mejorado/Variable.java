public class Variable extends Simbolo{

    private float valor = 0;

   // Cambia 'Tipo' por 'TipoToken'
    public Variable(String nombre, TipoToken tipo) {
        // Llama al constructor de Simbolo (ver paso 2)
        super(nombre, tipo); 
    }
    
    public void setValor(float valor){
        this.valor = valor;
    }

    public float getValor(){
        return this.valor;
    }

}
