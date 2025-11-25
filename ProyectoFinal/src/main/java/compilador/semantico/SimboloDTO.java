package compilador.semantico;



public class SimboloDTO { 
    
    private final String name;
    private final String type;
    private final String scope; 
    private final String address; 
    
    public SimboloDTO(String name, String type, String scope, String address) {
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.address = address;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public String getScope() { return scope; }
    public String getAddress() { return address; }
}