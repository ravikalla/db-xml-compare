package in.ravikalla.dbXmlCompare.xmlCompareUtil.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class SimpleNamespaceContext implements NamespaceContext
{
    private final Map<String, String> PREF_MAP;
    
    public SimpleNamespaceContext(final Map<String, String> prefMap) {
        (this.PREF_MAP = new HashMap<String, String>()).putAll(prefMap);
    }
    
    @Override
    public String getNamespaceURI(final String prefix) {
        return this.PREF_MAP.get(prefix);
    }
    
    @Override
    public String getPrefix(final String uri) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Iterator getPrefixes(final String uri) {
        throw new UnsupportedOperationException();
    }
}
