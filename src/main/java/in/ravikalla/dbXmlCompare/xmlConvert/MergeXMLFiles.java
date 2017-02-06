package in.ravikalla.dbXmlCompare.xmlConvert;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import in.ravikalla.dbXmlCompare.xmlCompareUtil.CompareXMLAndXML;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.XMLDataConverter;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.CommonUtil;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.SimpleNamespaceContext;
import in.ravikalla.dbXmlCompare.xmlConvert.util.Util_XMLConvert;

public class MergeXMLFiles
{
    public static void main(final String[] args) {
        final String strMergedXML = mergeXMLFiles("resources/test1.xml;resources/test2.xml", "/ContactResponse/ContactResp/Contacts/Contact");
        System.out.println("Merged XML :\n" + strMergedXML);
    }
    
    public static String mergeXMLFiles(final String strFileNames, final String strIterativeElement) {
        final String[] arrFileNames = strFileNames.split(";");
        String strMainXML = null;
        String strTempXML = null;
        try {
            final DocumentBuilder builder = XMLDataConverter.getDocumentBuilder();
            final Document docMaster = builder.newDocument();
            final Element objRootElement = docMaster.createElement("RootElement");
            Document doc = null;
            Map<String, String> prefMap = null;
            SimpleNamespaceContext namespaces = null;
            final XPathFactory xpathFactory = XPathFactory.newInstance();
            final XPath xpath = xpathFactory.newXPath();
            NodeList nodeListRoot = null;
            Node node1 = null;
            Node node2 = null;
            final int intLastIndex = strIterativeElement.lastIndexOf("/");
            final String strNodeName = strIterativeElement.substring(intLastIndex);
            String[] array;
            for (int length = (array = arrFileNames).length, i = 0; i < length; ++i) {
                final String strFileName = array[i];
                strTempXML = CommonUtil.readDataFromFile(strFileName);
                strTempXML = CompareXMLAndXML.removeXmlStringNamespaceAndPreamble(strTempXML);
                strTempXML = CompareXMLAndXML.removeSOAPENVPrefix(strTempXML);
                doc = builder.parse(new InputSource(new StringReader(strTempXML)));
                doc.getDocumentElement().normalize();
                prefMap = XMLDataConverter.getAttributeMap(doc);
                namespaces = new SimpleNamespaceContext(prefMap);
                xpath.setNamespaceContext(namespaces);
                XPathExpression expr = xpath.compile("count(" + strIterativeElement + ")");
                for (int intElementCount = (int)expr.evaluate(doc, XPathConstants.NUMBER), intElementsCtr = 0; intElementsCtr < intElementCount; ++intElementsCtr) {
                    expr = xpath.compile(String.valueOf(strIterativeElement) + "[" + (intElementsCtr + 1) + "]");
                    nodeListRoot = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
                    node1 = null;
                    if (nodeListRoot.getLength() > 0) {
                        node1 = nodeListRoot.item(0);
                        node2 = Util_XMLConvert.clone(node1, docMaster);
                        objRootElement.appendChild(node2);
                    }
                }
            }
            docMaster.appendChild(objRootElement);
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            final DOMSource source = new DOMSource(docMaster);
            final StreamResult result = new StreamResult(new StringWriter());
            transformer.transform(source, result);
            strMainXML = result.getWriter().toString();
        }
        catch (IOException e) {
            System.out.println("18 : MergeXMLFiles.mergeXMLFiles(...) : IOException e : " + e);
        }
        catch (ParserConfigurationException e2) {
            System.out.println("31 : MergeXMLFiles.mergeXMLFiles(...) : ParserConfigurationException e : " + e2);
        }
        catch (SAXException e3) {
            System.out.println("34 : MergeXMLFiles.mergeXMLFiles(...) : SAXException e : " + e3);
        }
        catch (XPathExpressionException e4) {
            System.out.println("53 : MergeXMLFiles.mergeXMLFiles(...) : XPathExpressionException e : " + e4);
        }
        catch (TransformerConfigurationException e5) {
            System.out.println("82 : MergeXMLFiles.mergeXMLFiles(...) : TransformerConfigurationException e : " + e5);
        }
        catch (TransformerFactoryConfigurationError e6) {
            System.out.println("85 : MergeXMLFiles.mergeXMLFiles(...) : TransformerFactoryConfigurationError e : " + e6);
        }
        catch (TransformerException e7) {
            System.out.println("87 : MergeXMLFiles.mergeXMLFiles(...) : TransformerException e : " + e7);
        }
        return strMainXML;
    }
}
