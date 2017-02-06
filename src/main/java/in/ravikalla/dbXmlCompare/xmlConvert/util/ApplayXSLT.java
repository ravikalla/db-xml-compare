package in.ravikalla.dbXmlCompare.xmlConvert.util;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public class ApplayXSLT
{
    public static void main(final String[] argv) {
        try {
            final File stylesheet = new File("C:/UI-XML/XSLTFile.xml");
            final File datafile = new File("C:/UI-XML/Data.xml");
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document document = builder.parse(datafile);
            final StreamSource stylesource = new StreamSource(stylesheet);
            TransformerFactory.newInstance().newTransformer(stylesource);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
