
public class Simbolo {
    protected String nombre;
    public Simbolo(String nombre) {
        this.nombre = nombre;
    }
    @Override
    public String toString() {
        return "Simbolo<" + nombre + ">";
    }
}