import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Test
{
    public static void main(final String[] args) throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(new InputSource(new FileInputStream("C:\\HTML TEST\\test.htm")));
        final XPathFactory xpathFactory = XPathFactory.newInstance();
        final XPath xpathE = xpathFactory.newXPath();
        final String xpath = "/html[1]/body[1]/div[2]/div[2]/div[2]/section[2]/div[1]/div[1]/div[1]/div[2]/div[1]/section[1]/div[1]/div[2]/ul[1]/li[1]/div[2]/div[1]/div[2]/div[1]/table[1]/tbody[1]/tr[1]/td[1]";
        final String data = getElementDataById("C:\\HTML TEST\\test.htm", xpath);
        System.out.println(data);
    }
    
    private static String getElementDataById(final String htmlFileName, final String xpath) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException {
        String name = null;
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(new InputSource(new FileInputStream(htmlFileName)));
            final XPathFactory xpathFactory = XPathFactory.newInstance();
            final XPath xpathE = xpathFactory.newXPath();
            final XPathExpression expr = xpathE.compile(xpath);
            name = (String)expr.evaluate(doc, XPathConstants.STRING);
        }
        catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return name;
    }
}
