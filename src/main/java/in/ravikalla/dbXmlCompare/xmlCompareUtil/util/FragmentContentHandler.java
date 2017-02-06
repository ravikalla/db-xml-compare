package in.ravikalla.dbXmlCompare.xmlCompareUtil.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class FragmentContentHandler extends DefaultHandler
{
    private String xPath;
    private XMLReader xmlReader;
    private FragmentContentHandler parent;
    private StringBuilder characters;
    private Map<String, Integer> elementNameCount;
    public static Map<String, String> linkedHashMap;
    public static Map<String, String> linkedHashMapValueAndXpaths;
    
    static {
        FragmentContentHandler.linkedHashMap = new LinkedHashMap<String, String>();
        FragmentContentHandler.linkedHashMapValueAndXpaths = new LinkedHashMap<String, String>();
    }
    
    public FragmentContentHandler(final XMLReader xmlReader) {
        this.xPath = "";
        this.characters = new StringBuilder();
        this.elementNameCount = new HashMap<String, Integer>();
        this.xmlReader = xmlReader;
    }
    
    private FragmentContentHandler(final String xPath, final XMLReader xmlReader, final FragmentContentHandler parent) {
        this(xmlReader);
        this.xPath = xPath;
        this.parent = parent;
    }
    
    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
        Integer count = this.elementNameCount.get(qName);
        if (count == null) {
            count = 1;
        }
        else {
            ++count;
        }
        this.elementNameCount.put(qName, count);
        final String childXPath = String.valueOf(this.xPath) + "/" + qName + "[" + count + "]";
        for (int attsLength = atts.getLength(), x = 0; x < attsLength; ++x) {}
        final FragmentContentHandler child = new FragmentContentHandler(childXPath, this.xmlReader, this);
        this.xmlReader.setContentHandler(child);
    }
    
    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        final String value = this.characters.toString().trim();
        if (value.length() > 0) {
            FragmentContentHandler.linkedHashMap.put(this.xPath.toUpperCase(), value);
            FragmentContentHandler.linkedHashMapValueAndXpaths.put(value, this.xPath.toUpperCase());
        }
        this.xmlReader.setContentHandler(this.parent);
    }
    
    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        this.characters.append(ch, start, length);
    }
}
