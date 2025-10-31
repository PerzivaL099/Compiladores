public class Simbolo {
    protected String nombre;
    protected TipoToken tipo; // Campo usado por Variable, mantenido aquí por jerarquía

    
    public Simbolo(String nombre, TipoToken tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }
    
    
    public Simbolo(String nombre) {
        this.nombre = nombre;
        // El tipo se puede dejar como null o inicializar con un valor por defecto
        this.tipo = null; 
    }

    public String getNombre() {
        return nombre;
    }

    public TipoToken getTipo() {
        return tipo;
    }
    
    @Override
    public String toString() {
        return "Simbolo<" + nombre + ">";
    }
}