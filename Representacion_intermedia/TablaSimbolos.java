
import java.util.ArrayList;

public class TablaSimbolos {
    private ArrayList<Simbolo> simbolos = new ArrayList<>();

    public void definir(Simbolo s) {
        simbolos.add(s);
        System.out.println("Símbolo definido: " + s);
    }

    public ArrayList<Simbolo> getSimbolos() {
        return simbolos;
    }
}