package in.ravikalla.dbXmlCompare.xmlCompareUtil;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.CommonUtil;

public class ConvertXMLToFullPathInCSV
{
    public static void main(final String[] args) {
        String xmlStr = null;
        try {
            xmlStr = CommonUtil.readDataFromFile("resources/retrieveParticipantTransfersResponse1.xml");
        }
        catch (IOException e) {
            System.out.println("46 : IOException : " + e);
        }
        final String strFirstLevelOfRepeatingElementsWithSeparator = getFirstLevelOfRepeatingElements(xmlStr, xmlStr, ";");
        System.out.println(strFirstLevelOfRepeatingElementsWithSeparator);
        System.out.println("Completed!");
    }
    
    public static String writeKeyValuePairsOfXMLToCSV(final String strXMLData) {
        final StringBuffer strKeyValPairs = new StringBuffer();
        try {
            final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            final InputStream is = new ByteArrayInputStream(strXMLData.getBytes());
            final Document document = domFactory.newDocumentBuilder().parse(is);
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final XPathExpression expr = xpath.compile("//*[count(./descendant::*)=1]");
            final NodeList list = (NodeList)expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); ++i) {
                Node node = list.item(i);
                final StringBuilder path = new StringBuilder(node.getNodeName());
                final String value = node.getTextContent();
                for (node = node.getParentNode(); node.getNodeType() != 9; node = node.getParentNode()) {
                    path.insert(0, String.valueOf(node.getNodeName()) + '/');
                }
                strKeyValPairs.append("/").append((CharSequence)path).append(",").append(value).append("\n");
            }
        }
        catch (SAXException e) {
            System.out.println("ConvertXMLToFullPathInCSV : SAXException e : " + e);
        }
        catch (IOException e2) {
            System.out.println("ConvertXMLToFullPathInCSV : IOException e : " + e2);
        }
        catch (ParserConfigurationException e3) {
            System.out.println("ConvertXMLToFullPathInCSV : ParserConfigurationException e : " + e3);
        }
        catch (XPathExpressionException e4) {
            System.out.println("ConvertXMLToFullPathInCSV : XPathExpressionException e : " + e4);
        }
        return strKeyValPairs.toString();
    }
    
    public static String getFirstLevelOfRepeatingElements(final String strXMLData1, final String strXMLData2, final String strSeparator) {
        List<Node> lstResultNodes = null;
        List<String> lstResultNodeNames = new ArrayList<String>();
        final StringBuffer strResultNodeNames = new StringBuffer();
        try {
            final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            InputStream is = null;
            is = new ByteArrayInputStream(strXMLData1.getBytes());
            Document document = domFactory.newDocumentBuilder().parse(is);
            document.getDocumentElement().normalize();
            lstResultNodes = getFirstRepeatingAndLeafNodes(document);
            System.out.println("Size1 : Node : " + lstResultNodes.size());
            for (Node objNode : lstResultNodes) {
                final StringBuilder strNodePath = new StringBuilder(objNode.getNodeName());
                for (objNode = objNode.getParentNode(); objNode.getNodeType() != 9; objNode = objNode.getParentNode()) {
                    strNodePath.insert(0, String.valueOf(objNode.getNodeName()) + '/');
                }
                strNodePath.insert(0, '/');
                lstResultNodeNames = addStringToListIfNotAdded(strNodePath.toString(), lstResultNodeNames);
            }
            is = new ByteArrayInputStream(strXMLData2.getBytes());
            document = domFactory.newDocumentBuilder().parse(is);
            document.getDocumentElement().normalize();
            lstResultNodes = getFirstRepeatingAndLeafNodes(document);
            for (Node objNode : lstResultNodes) {
                final StringBuilder strNodePath = new StringBuilder(objNode.getNodeName());
                for (objNode = objNode.getParentNode(); objNode.getNodeType() != 9; objNode = objNode.getParentNode()) {
                    strNodePath.insert(0, String.valueOf(objNode.getNodeName()) + '/');
                }
                strNodePath.insert(0, '/');
                lstResultNodeNames = addStringToListIfNotAdded(strNodePath.toString(), lstResultNodeNames);
            }
            for (int i = 0; i < lstResultNodeNames.size(); ++i) {
                if (i == 0) {
                    strResultNodeNames.append(lstResultNodeNames.get(i));
                }
                else {
                    strResultNodeNames.append(strSeparator).append(lstResultNodeNames.get(i));
                }
            }
        }
        catch (SAXException e) {
            System.out.println("ConvertXMLToFullPathInCSV : SAXException e : " + e);
        }
        catch (IOException e2) {
            System.out.println("ConvertXMLToFullPathInCSV : IOException e : " + e2);
        }
        catch (ParserConfigurationException e3) {
            System.out.println("ConvertXMLToFullPathInCSV : ParserConfigurationException e : " + e3);
        }
        return strResultNodeNames.toString();
    }
    
    private static List<Node> getFirstRepeatingAndLeafNodes(final Node node) {
        final NodeList lstChildren = node.getChildNodes();
        final List<Node> lstRepeatingNodes = getRepeatedNodes(lstChildren);
        final List<Node> lstNonRepeatingNodes = getNonRepeatedNodes(lstChildren, lstRepeatingNodes);
        final List<Node> lstNonRepeatingLeafNodes = getNonRepeatingLeafNodes(lstNonRepeatingNodes);
        final List<Node> lstRepeatingAndLeafNodes = new ArrayList<Node>();
        lstRepeatingAndLeafNodes.addAll(lstRepeatingNodes);
        lstRepeatingAndLeafNodes.addAll(lstNonRepeatingLeafNodes);
        final List<Node> lstNonRepeatingWithoutLeafNodes = getNonRepeatingWithoutLeafNodes(lstNonRepeatingNodes, lstNonRepeatingLeafNodes);
        for (final Node objNonRepeatingNonLeafNode : lstNonRepeatingWithoutLeafNodes) {
            lstRepeatingAndLeafNodes.addAll(getFirstRepeatingAndLeafNodes(objNonRepeatingNonLeafNode));
        }
        return lstRepeatingAndLeafNodes;
    }
    
    private static List<Node> getNonRepeatingWithoutLeafNodes(final List<Node> lstNonRepeatingNodes, final List<Node> lstNonRepeatingLeafNodes) {
        final List<Node> lstNonRepeatingWithoutLeafNodes = new ArrayList<Node>();
        for (final Node objNonRepeatingNode : lstNonRepeatingNodes) {
            boolean blnNodeIsLeaf = false;
            for (final Node objNonRepeatingLeafNode : lstNonRepeatingLeafNodes) {
                if (objNonRepeatingNode.getNodeName().equals(objNonRepeatingLeafNode.getNodeName())) {
                    blnNodeIsLeaf = true;
                    break;
                }
            }
            if (!blnNodeIsLeaf) {
                lstNonRepeatingWithoutLeafNodes.add(objNonRepeatingNode);
            }
        }
        return lstNonRepeatingWithoutLeafNodes;
    }
    
    private static List<Node> getNonRepeatingLeafNodes(final List<Node> lstNonRepeatingNodes) {
        final List<Node> lstNonRepeatingLeafNode = new ArrayList<Node>();
        for (final Node objNonRepeatingNode : lstNonRepeatingNodes) {
            if (isLeafNode(objNonRepeatingNode)) {
                lstNonRepeatingLeafNode.add(objNonRepeatingNode);
            }
        }
        return lstNonRepeatingLeafNode;
    }
    
    private static boolean isLeafNode(final Node objNonRepeatingNode) {
        final NodeList lst = objNonRepeatingNode.getChildNodes();
        return lst.getLength() == 1 && lst.item(0).getNodeType() == 3;
    }
    
    private static List<Node> getRepeatedNodes(final NodeList lstChildren) {
        final List<Node> lstUnionOfNodes = new ArrayList<Node>();
        final List<Node> lstRepeatingNodes = new ArrayList<Node>();
        for (int i = 0; i < lstChildren.getLength(); ++i) {
            final Node objNode = lstChildren.item(i);
            if (objNode.getNodeType() == 1) {
                final String strNodeName_Temp = objNode.getNodeName();
                if (nodeExistsInList(strNodeName_Temp, lstUnionOfNodes)) {
                    lstRepeatingNodes.add(objNode);
                }
                else {
                    lstUnionOfNodes.add(objNode);
                }
            }
        }
        return lstRepeatingNodes;
    }
    
    private static List<Node> getNonRepeatedNodes(final NodeList objNodeList, final List<Node> lstRepeatingNodes) {
        final List<Node> lstNonRepeatedNodes = new ArrayList<Node>();
        Node objNodeFromEntireList = null;
        for (int intNodeList_Ctr = 0; intNodeList_Ctr < objNodeList.getLength(); ++intNodeList_Ctr) {
            objNodeFromEntireList = objNodeList.item(intNodeList_Ctr);
            if (objNodeFromEntireList.getNodeType() == 1) {
                boolean blnNodeExistInRepeatingList = false;
                for (final Node objNodeFromRepeatingList : lstRepeatingNodes) {
                    if (objNodeFromEntireList.getNodeName().equals(objNodeFromRepeatingList.getNodeName())) {
                        blnNodeExistInRepeatingList = true;
                        break;
                    }
                }
                if (!blnNodeExistInRepeatingList) {
                    lstNonRepeatedNodes.add(objNodeFromEntireList);
                }
            }
        }
        return lstNonRepeatedNodes;
    }
    
    private static boolean nodeExistsInList(final String strNodeName_Temp, final List<Node> lstUnionOfNodes) {
        boolean nodeExistsInList = false;
        for (final Node objNode_Temp : lstUnionOfNodes) {
            if (objNode_Temp.getNodeName().equals(strNodeName_Temp)) {
                nodeExistsInList = true;
                break;
            }
        }
        return nodeExistsInList;
    }
    
    public static void writeFileToDisk(final String strData, final String strFileName) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(strFileName), "utf-8"));
            writer.write(strData);
        }
        catch (IOException ex) {
            System.out.println("79 : IOException : " + ex);
            try {
                writer.close();
            }
            catch (Exception ex2) {
                System.out.println("84 : Exception : " + ex2);
            }
            return;
        }
        finally {
            try {
                writer.close();
            }
            catch (Exception ex2) {
                System.out.println("84 : Exception : " + ex2);
            }
        }
        try {
            writer.close();
        }
        catch (Exception ex2) {
            System.out.println("84 : Exception : " + ex2);
        }
    }
    
    private static List<String> addStringToListIfNotAdded(final String strToCheck, final List<String> lst) {
        int intElementPosToReplace = -1;
        boolean blnExists = false;
        int i = 0;
        while (i < lst.size()) {
            if (lst.get(i).indexOf(strToCheck) == 0) {
                blnExists = true;
                if (lst.get(i).length() > strToCheck.length()) {
                    intElementPosToReplace = i;
                    break;
                }
                break;
            }
            else {
                ++i;
            }
        }
        if (-1 != intElementPosToReplace) {
            lst.remove(intElementPosToReplace);
            if (!blnExists) {
                lst.add(strToCheck);
            }
        }
        else if (!blnExists) {
            lst.add(strToCheck);
        }
        return lst;
    }
}
