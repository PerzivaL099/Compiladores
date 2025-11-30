package PatronParserRecursivo;

public class LocalScope extends BaseScope {
    public LocalScope(Scope parent) {
        super(parent); // Debe tener un padre
    }
    public String getScopeName() { return "local"; }
}