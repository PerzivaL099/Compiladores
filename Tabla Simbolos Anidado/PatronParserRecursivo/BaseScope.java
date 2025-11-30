package PatronParserRecursivo;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseScope implements Scope{
    Scope enclosingScope;
    Map<String, Simbolo> symbols = new HashMap<String, Simbolo>();

    public BaseScope(Scope enclosingScope){
        this.enclosingScope = enclosingScope;
    }

    public Simbolo resolve(String name){
        Simbolo s = symbols.get(name);
        if (s != null) return s;

        if(enclosingScope != null) return enclosingScope.resolve(name);
        return null;
    }

    public void define(Simbolo sym){
        symbols.put(sym.getNombre(), sym);
    }

    public Scope getEnclosingScope(){
        return enclosingScope;
    }
    
}
