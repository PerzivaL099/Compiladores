
public class TipoToken {
    private String nombre;

    public TipoToken(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {

        return nombre;
    }
}