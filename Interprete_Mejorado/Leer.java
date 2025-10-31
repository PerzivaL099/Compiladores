import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Leer extends Tupla {
    Token variable;

    public Leer (Token variable, int sv, int sf) {
        super (sv, sf);
        this.variable = variable;
    }

    public String toString() {
        return "(" + super.toString() + ", [" + variable + "])";
    }

    public int ejecutar (TablaSimbolos ts) {
        String valor = "0.0";
        
        
        System.out.print("Da un valor para " + variable.getNombre() + " ");
        BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in));
        
        try {
            valor = entrada.readLine();
        } catch (IOException ex) {}
        
        
        Simbolo s = ts.resolver(variable.getNombre());
        
        if (s == null) {
            
            System.err.println("ERROR DE EJECUCIÓN CRÍTICO: La variable '" + variable.getNombre() + "' no se ha podido resolver en la Tabla de Símbolos.");
            
            return -1; 
        }

        
        Variable v = (Variable) s; 
        
        try {
            v.setValor (Float.parseFloat(valor));
        } catch (NumberFormatException exception) {
            System.out.println("Error: Número inválido");

        }
        
        return saltoVerdadero;
    }
}