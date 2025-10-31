import java.util.ArrayList;

public class TablaSimbolos {
    private ArrayList<Simbolo> simbolos = new ArrayList<>();

    public void definir(Simbolo s) {
        simbolos.add(s);
        System.out.println("Símbolo definido: " + s);
    }

    public Simbolo resolver(String nombre){
        for(Simbolo s: simbolos){
            if(s.getNombre().equals(nombre)){
                return s;
            }
        }
        
        System.err.println("DEPURACIÓN: Símbolo '" + nombre + "' NO ENCONTRADO.");
        
        return null;
    }

    public ArrayList<Simbolo> getSimbolos() {
        return simbolos;
    }
}