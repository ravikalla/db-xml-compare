package in.ravikalla.dbXmlCompare.xmlCompareUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import in.ravikalla.dbXmlCompare.xmlCompareUtil.dto.MappingDataDTO;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.dto.XMLDataConverterResultDTO;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.dto.XMLToXMLComparisonResultsHolderDTO;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.CommonUtil;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.SimpleNamespaceContext;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.excel.ExcelCommon;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.excel.ExcelUtil;
import in.ravikalla.dbXmlCompare.xmlConvert.util.Util_XMLConvert;

public class XMLDataConverter
{
    public static DocumentBuilder builder;
    
    static {
        XMLDataConverter.builder = null;
    }
    
    public static void main(final String[] args) {
        final String strXMLFileName = "C:\\Web Services Automation\\Tests\\strComparisionResultsFile\\Response.xml";
        String xmlStr = null;
        try {
            xmlStr = CommonUtil.readDataFromFile(strXMLFileName);
            getXPathElementsData(xmlStr);
        }
        catch (IOException e) {
            System.out.println("56 : IOException : " + e);
        }
        catch (SAXException e2) {
            System.out.println("58 : SAXException : " + e2);
        }
        catch (ParserConfigurationException e3) {
            System.out.println("60 : ParserConfigurationException : " + e3);
        }
        System.out.println("Completed!");
    }
    
    public static void saveXMLDataToExcelFile(final String xmlStr, final String strMappingDocFileName, final String strOPFileName) {
        final XMLDataConverterResultDTO objXMLDataConverterResultDTO = convertDataFromXML(xmlStr, strMappingDocFileName);
        writeFileToDisk(objXMLDataConverterResultDTO.mapResponseXMLData, strOPFileName);
    }
    
    public static XMLDataConverterResultDTO convertDataFromXML(final String xmlStr, final String strMappingDocument) {
        XMLDataConverterResultDTO objXMLDataConverterResultDTO = null;
        Map<String, List<String>> mapResponseXMLData = null;
        MappingDataDTO objMappingData = new MappingDataDTO();
        try {
            objMappingData = readDataFromExcel(strMappingDocument, objMappingData);
            System.out.println("79 : " + (objMappingData.mapCursorRepeatableElement == null) + " : " + (objMappingData.mapCursorSpecificElements == null));
            if (objMappingData.mapCursorRepeatableElement != null && objMappingData.mapCursorSpecificElements != null) {
                mapResponseXMLData = getXPathElementsData(xmlStr, objMappingData);
                objXMLDataConverterResultDTO = new XMLDataConverterResultDTO();
                objXMLDataConverterResultDTO.mapResponseXMLData = mapResponseXMLData;
                objXMLDataConverterResultDTO.mapElementToDB = objMappingData.mapElementToDB;
            }
        }
        catch (ParserConfigurationException e) {
            System.out.println("36 : ParserConfigurationException : " + e);
        }
        catch (SAXException e2) {
            System.out.println("38 : SAXException : " + e2);
        }
        catch (IOException e3) {
            System.out.println("40 : IOException : " + e3);
        }
        return objXMLDataConverterResultDTO;
    }
    
    private static MappingDataDTO readDataFromExcel(final String strInputFile, MappingDataDTO objMappingData) {
        System.out.println("121 : XMLDataConverter.readDataFromExcel " + strInputFile);
        try {
            final Workbook wb = ExcelCommon.getWorkBook(strInputFile);
            Sheet sheet = wb.getSheet("Sheet1");
            objMappingData = ExcelUtil.getDataFromSheet(sheet, objMappingData);
            sheet = wb.getSheet("LOOKUP");
            if (sheet != null) {
                objMappingData = ExcelUtil.getLookupInfoFromSheet(sheet, objMappingData);
            }
            sheet = wb.getSheet("FORMAT_FOR_COMPARISON");
            if (sheet != null) {
                objMappingData = ExcelUtil.getFormatInfoFromSheet(sheet, objMappingData);
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException : " + e);
        }
        catch (InvalidFormatException e2) {
            System.out.println("InvalidFormatException : " + e2);
        }
        catch (IOException e3) {
            System.out.println("IOException : " + e3);
        }
        return objMappingData;
    }
    
    public static void writeFileToDisk(final Map<String, List<String>> mapStoredProcData, final String strFileName) {
        final HSSFWorkbook workbook = new HSSFWorkbook();
        try {
            for (final Map.Entry<String, List<String>> entry : mapStoredProcData.entrySet()) {
                final String strCursorName = entry.getKey();
                final List<String> lstCursorData = entry.getValue();
                final HSSFSheet sheet = workbook.createSheet(strCursorName);
                short intRowCnt = 0;
                for (final String strCursorLineData : lstCursorData) {
                    final String[] arrCursorLineElements = strCursorLineData.split("\\|");
                    final HSSFSheet hssfSheet = sheet;
                    final short n = intRowCnt;
                    intRowCnt = (short)(n + 1);
                    final HSSFRow rowhead = hssfSheet.createRow((int)n);
                    for (int intColCnt = 0; intColCnt < arrCursorLineElements.length; ++intColCnt) {
                        rowhead.createCell(intColCnt).setCellValue(arrCursorLineElements[intColCnt]);
                    }
                }
            }
            final FileOutputStream fileOut = new FileOutputStream(strFileName);
            workbook.write((OutputStream)fileOut);
            fileOut.close();
            System.out.println("Your excel file has been generated!");
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
    }
    
    private static Map<String, List<String>> getXPathElementsData(final String xmlStr, final MappingDataDTO objMappingData) throws SAXException, IOException, ParserConfigurationException {
        List<String> lstDataRows = null;
        List<String> lstSpecificElement = null;
        Map<String, String> mapDBCol = null;
        Map<String, List<String>> mapResponseXMLData = null;
        for (final Map.Entry<String, String> entryCursorRepeatableElement : objMappingData.mapCursorRepeatableElement.entrySet()) {
            if (mapResponseXMLData == null) {
                mapResponseXMLData = new HashMap<String, List<String>>();
            }
            lstSpecificElement = objMappingData.mapCursorSpecificElements.get(entryCursorRepeatableElement.getKey());
            mapDBCol = objMappingData.mapElementToDB.get(entryCursorRepeatableElement.getKey());
            final Object[] arrObjSpecificElement = lstSpecificElement.toArray();
            final String[] arrStrSpecificElement = Arrays.copyOf(arrObjSpecificElement, arrObjSpecificElement.length, (Class<? extends String[]>)String[].class);
            lstDataRows = getXPathElementsData(xmlStr, entryCursorRepeatableElement.getKey(), entryCursorRepeatableElement.getValue(), arrStrSpecificElement, mapDBCol, objMappingData);
            mapResponseXMLData.put(entryCursorRepeatableElement.getKey(), lstDataRows);
        }
        return mapResponseXMLData;
    }
    
    private static List<String> getXPathElementsData(String xmlStr, final String strCursorName, final String strRepeatableElement, final String[] arrSpecificElement, final Map<String, String> mapDBCol, final MappingDataDTO objMappingData) throws SAXException, IOException, ParserConfigurationException {
        String strElementData = null;
        System.out.println("227 : " + strRepeatableElement + " : " + arrSpecificElement.length + " : " + xmlStr);
        DocumentBuilder builder;
        Document doc;
        for (builder = getDocumentBuilder(), doc = null, doc = builder.parse(new InputSource(new StringReader(xmlStr))); containXMLNSAttribute(doc); doc = builder.parse(new InputSource(new StringReader(xmlStr)))) {
            xmlStr = removeXMLNSAttribute(xmlStr);
        }
        final XPathFactory xpathFactory = XPathFactory.newInstance();
        final XPath xpath = xpathFactory.newXPath();
        final Map<String, String> prefMap = getAttributeMap(doc);
        final SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
        xpath.setNamespaceContext(namespaces);
        final List<String> lstRowsWithXMLData = new ArrayList<String>();
        StringBuffer strRowData = null;
        try {
            System.out.println("293 : Repeatable element : " + strRepeatableElement);
            XPathExpression expr = xpath.compile("count(" + strRepeatableElement + ")");
            final Double dblElementCount = (Double)expr.evaluate(doc, XPathConstants.NUMBER);
            System.out.println("298 : " + dblElementCount + " : " + strRepeatableElement);
            final long lngElementCount = dblElementCount.longValue();
            strRowData = new StringBuffer();
            for (final Map.Entry<String, String> entryDBCol : mapDBCol.entrySet()) {
                if (strRowData.length() != 0) {
                    strRowData.append("|");
                }
                strRowData.append(entryDBCol.getKey());
            }
            lstRowsWithXMLData.add(strRowData.toString());
            for (int intElementsCtr = 1; intElementsCtr <= lngElementCount; ++intElementsCtr) {
                strRowData = new StringBuffer();
                for (final String strSpecificElement : arrSpecificElement) {
                    if (strRowData.length() != 0) {
                        strRowData.append("|");
                    }
                    expr = xpath.compile(String.valueOf(strRepeatableElement) + "[" + intElementsCtr + "]/" + strSpecificElement + "/text()");
                    final NodeList nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
                    for (int intResultCtr = 0; intResultCtr < nodes.getLength(); ++intResultCtr) {
                        strElementData = nodes.item(intResultCtr).getNodeValue();
                        strElementData = getLookupValue(strElementData, String.valueOf(strCursorName) + "|" + strSpecificElement, objMappingData);
                        strRowData.append(strElementData);
                    }
                }
                lstRowsWithXMLData.add(strRowData.toString());
            }
        }
        catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return lstRowsWithXMLData;
    }
    
    public static Map<String, String> getAttributeMap(final Document doc) {
        final Map<String, String> mapAttributes = new HashMap<String, String>();
        final NodeList nodeList = doc.getElementsByTagName("*");
        for (int i = 0; i < nodeList.getLength(); ++i) {
            final Node node = nodeList.item(i);
            if (node.getNodeType() == 1) {
                final NamedNodeMap mapNamedNode = node.getAttributes();
                for (int intNamedNodeSize = mapNamedNode.getLength(), intCtr = 0; intCtr < intNamedNodeSize; ++intCtr) {
                    final String strNodeName = mapNamedNode.item(intCtr).getNodeName();
                    final String[] arrNameSpace = strNodeName.toLowerCase().split(":");
                    if (arrNameSpace.length > 1 && arrNameSpace[0].trim().toLowerCase().equalsIgnoreCase("xmlns")) {
                        String strNameSpaceKey = null;
                        if (arrNameSpace.length > 1) {
                            strNameSpaceKey = arrNameSpace[1];
                        }
                        else {
                            strNameSpaceKey = "";
                        }
                        final String strNameSpaceValue = mapNamedNode.item(intCtr).getNodeValue();
                        mapAttributes.put(strNameSpaceKey, strNameSpaceValue);
                    }
                }
            }
        }
        return mapAttributes;
    }
    
    private static String removeXMLNSAttribute(String strXML) {
        int intXMLNSIndex = strXML.indexOf("xmlns");
        boolean blnEualToStarted = false;
        boolean blnFirstQuoteStarted = false;
        StringBuffer strTempStr = new StringBuffer("xmlns");
        for (int intCtr = intXMLNSIndex + "xmlns".length(); intCtr < strXML.length(); ++intCtr) {
            final char charTemp = strXML.charAt(intCtr);
            strTempStr.append(charTemp);
            if (charTemp != ' ' && charTemp != '\n') {
                if (charTemp != '\t') {
                    if (charTemp == '=') {
                        blnEualToStarted = true;
                    }
                    else if ((blnEualToStarted && charTemp == '\'') || charTemp == '\"') {
                        if (blnFirstQuoteStarted) {
                            blnEualToStarted = false;
                            blnFirstQuoteStarted = false;
                            strXML = strXML.replace(strTempStr.toString(), "");
                            break;
                        }
                        blnFirstQuoteStarted = true;
                    }
                    else if (!blnEualToStarted || !blnFirstQuoteStarted) {
                        strTempStr = new StringBuffer("xmlns");
                        intXMLNSIndex = strXML.indexOf("xmlns", intXMLNSIndex + 1);
                        intCtr = intXMLNSIndex + "xmlns".length();
                        --intCtr;
                        if (intXMLNSIndex < 0) {
                            break;
                        }
                        blnEualToStarted = false;
                        blnFirstQuoteStarted = false;
                    }
                }
            }
        }
        return strXML;
    }
    
    public static XMLToXMLComparisonResultsHolderDTO compareXPathElementsData_WithChildElements(final String xmlStr1, final String xmlStr2, final String strIterativeElement, final String[] arrElementsToExclude, final String strPrimaryKeyElement, String strTrimElements) throws SAXException, IOException, ParserConfigurationException {
        System.out.println("Start : XMLDataConverter.compareXPathElementsData_WithChildElements(...)");
        final List<String> lstMismatchedDataForCSV = new ArrayList<String>();
        final List<String> lstMatchedDataForCSV = new ArrayList<String>();
        List<String> lstNodeInformation_Temp = null;
        int intTempNode2Position = -1;
        final Map<Integer, Integer> mapMatchedNodePositions = new HashMap<Integer, Integer>();
        NodeList nodeListRoot1 = null;
        NodeList nodeListRoot2 = null;
        Node node1 = null;
        System.out.println("290 : " + strIterativeElement);
        final XPathFactory xpathFactory = XPathFactory.newInstance();
        final XPath xpath1 = xpathFactory.newXPath();
        final XPath xpath2 = xpathFactory.newXPath();
        final DocumentBuilder builder = getDocumentBuilder();
        final Document doc1 = builder.parse(new InputSource(new StringReader(xmlStr1)));
        final Document doc2 = builder.parse(new InputSource(new StringReader(xmlStr2)));
        doc1.getDocumentElement().normalize();
        doc2.getDocumentElement().normalize();
        String strCaseSensitveValues = "";
        Map<String, String> mapCaseSensitveValues = new HashMap<String, String>();
        try {
            if (strTrimElements != null && strTrimElements.split("&&").length > 1) {
                strCaseSensitveValues = strTrimElements.split("&&")[1];
                strTrimElements = strTrimElements.split("&&")[0];
                if (strTrimElements != null && strTrimElements.equalsIgnoreCase("")) {
                    strTrimElements = null;
                }
                System.out.println("case values-->" + strCaseSensitveValues);
                System.out.println("strTrimElements-->" + strTrimElements);
                mapCaseSensitveValues = Util_XMLConvert.convertTrimmableElementsToMap(strCaseSensitveValues, ";", ",");
                System.out.println("map siae for case senitive--->" + mapCaseSensitveValues.size());
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        if (strTrimElements != null && !strTrimElements.trim().equalsIgnoreCase("") && strTrimElements.split(",")[1].equalsIgnoreCase(";")) {
            strTrimElements = null;
        }
        final Map<String, String> mapTrimElements = Util_XMLConvert.convertTrimmableElementsToMap(strTrimElements, ";", ",");
        final NodeList lstChildNodeList1 = doc1.getChildNodes();
        Util_XMLConvert.trimValueIfApplicable(lstChildNodeList1, mapTrimElements);
        final NodeList lstChildNodeList2 = doc2.getChildNodes();
        Util_XMLConvert.trimValueIfApplicable(lstChildNodeList2, mapTrimElements);
        final Map<String, String> prefMap1 = getAttributeMap(doc1);
        final SimpleNamespaceContext namespaces1 = new SimpleNamespaceContext(prefMap1);
        xpath1.setNamespaceContext(namespaces1);
        final Map<String, String> prefMap2 = getAttributeMap(doc2);
        final SimpleNamespaceContext namespaces2 = new SimpleNamespaceContext(prefMap2);
        xpath2.setNamespaceContext(namespaces2);
        try {
            XPathExpression expr1 = xpath1.compile("count(" + strIterativeElement + ")");
            final int intElementCount1 = (int)expr1.evaluate(doc1, XPathConstants.NUMBER);
            XPathExpression expr2 = xpath2.compile("count(" + strIterativeElement + ")");
            final int intElementCount2 = (int)expr2.evaluate(doc2, XPathConstants.NUMBER);
            System.out.println("313 : " + intElementCount1 + " : " + intElementCount2 + " : " + strIterativeElement);
            expr2 = xpath2.compile(strIterativeElement);
            nodeListRoot2 = (NodeList)expr2.evaluate(doc2, XPathConstants.NODESET);
            System.out.println("347 : " + nodeListRoot2.getLength());
            for (int intElementsCtr1 = 0; intElementsCtr1 < intElementCount1; ++intElementsCtr1) {
                expr1 = xpath1.compile(String.valueOf(strIterativeElement) + "[" + (intElementsCtr1 + 1) + "]");
                nodeListRoot1 = (NodeList)expr1.evaluate(doc1, XPathConstants.NODESET);
                node1 = null;
                if (nodeListRoot1.getLength() > 0) {
                    node1 = nodeListRoot1.item(0);
                }
                intTempNode2Position = -1;
                if (node1 != null) {
                    if (eligibleNodeForValidation(node1, arrElementsToExclude)) {
                        intTempNode2Position = getPositionOfMatchingNodeFromList(node1, nodeListRoot2, mapMatchedNodePositions, arrElementsToExclude, mapCaseSensitveValues);
                        if (node1.getNodeName().equals("StatusText")) {
                            System.out.println("XMLDataConverter Node Names : " + node1.getTextContent());
                        }
                        System.out.println("362 : MatchingNodes : " + intElementsCtr1 + " : " + intTempNode2Position);
                    }
                    if (-1 != intTempNode2Position) {
                        mapMatchedNodePositions.put(new Integer(intElementsCtr1), new Integer(intTempNode2Position));
                    }
                }
            }
            expr1 = xpath1.compile(strIterativeElement);
            nodeListRoot1 = (NodeList)expr1.evaluate(doc1, XPathConstants.NODESET);
            final List<String> lstMatchedElementPositions_ColonSeparated = findMatchedNodePositionsInXML1AndXML2(mapMatchedNodePositions, intElementCount1);
            final List<String> lstMismatchedElementPositions_ColonSeparated = findMismatchedNodePositionsInXML1AndXML2(mapMatchedNodePositions, intElementCount1, intElementCount2);
            for (final String strMismatchedPosition_ColunSeparated : lstMismatchedElementPositions_ColonSeparated) {
                lstNodeInformation_Temp = getNodeInformationForMismatchedData(strMismatchedPosition_ColunSeparated, nodeListRoot1, nodeListRoot2, arrElementsToExclude);
                if (lstNodeInformation_Temp != null && lstNodeInformation_Temp.size() > 0) {
                    lstMismatchedDataForCSV.add(",,,");
                    lstMismatchedDataForCSV.addAll(lstNodeInformation_Temp);
                }
            }
            for (final String strMatchedPosition_ColunSeparated : lstMatchedElementPositions_ColonSeparated) {
                lstNodeInformation_Temp = getNodeInformationForMatchedData(strMatchedPosition_ColunSeparated, nodeListRoot1);
                if (lstNodeInformation_Temp != null && lstNodeInformation_Temp.size() > 0) {
                    lstMatchedDataForCSV.add(",,,");
                    lstMatchedDataForCSV.addAll(lstNodeInformation_Temp);
                }
            }
        }
        catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        System.out.println("End : XMLDataConverter.compareXPathElementsData_WithChildElements(...)");
        final XMLToXMLComparisonResultsHolderDTO objXMLToXMLComparisonResultsHolderDTO = new XMLToXMLComparisonResultsHolderDTO();
        objXMLToXMLComparisonResultsHolderDTO.lstMatchedDataForCSV = lstMatchedDataForCSV;
        objXMLToXMLComparisonResultsHolderDTO.lstMismatchedDataForCSV = lstMismatchedDataForCSV;
        return objXMLToXMLComparisonResultsHolderDTO;
    }
    
    private static boolean containXMLNSAttribute(final Document doc) {
        System.out.println("388 : containXMLNSAttribute(...)");
        final NodeList nodeList = doc.getElementsByTagName("*");
        for (int i = 0; i < nodeList.getLength(); ++i) {
            final Element ele = (Element)nodeList.item(i);
            if (ele.hasAttribute("xmlns")) {
                System.out.println("485 : Attribute identififed");
                return true;
            }
        }
        return false;
    }
    
    public static void getXPathElementsData(String xmlStr) throws SAXException, IOException, ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc;
        for (builder = factory.newDocumentBuilder(), doc = null, doc = builder.parse(new InputSource(new StringReader(xmlStr))); containXMLNSAttribute(doc); doc = builder.parse(new InputSource(new StringReader(xmlStr)))) {
            xmlStr = removeXMLNSAttribute(xmlStr);
            System.out.println("532 : " + xmlStr);
        }
        final XPathFactory xpathFactory = XPathFactory.newInstance();
        final XPath xpath = xpathFactory.newXPath();
        final Map<String, String> prefMap = getAttributeMap(doc);
        final SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
        xpath.setNamespaceContext(namespaces);
        StringBuffer strRowData = null;
        try {
            strRowData = new StringBuffer();
            if (strRowData.length() != 0) {
                strRowData.append(",");
            }
            final XPathExpression expr = xpath.compile("/ResponseStatus/Status/text()");
            final NodeList nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            System.out.println("424 : Nodes Length : " + nodes.getLength());
            for (int intResultCtr = 0; intResultCtr < nodes.getLength(); ++intResultCtr) {
                strRowData.append("," + nodes.item(intResultCtr).getNodeValue() + " : " + nodes.item(intResultCtr).getNodeType() + " : " + nodes.item(intResultCtr).getNodeName() + "\n");
            }
            System.out.println("387 : Row Data : " + strRowData.toString());
            System.out.println("388 : " + xmlStr);
        }
        catch (XPathExpressionException e) {
            System.out.println("XPathExpressionException e : " + e);
        }
    }
    
    public static String getStringFromDoc(final Document doc) {
        String strStringFromFile = "";
        try {
            final DOMSource domSource = new DOMSource(doc);
            final StringWriter writer = new StringWriter();
            final StreamResult result = new StreamResult(writer);
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            writer.flush();
            strStringFromFile = writer.toString();
        }
        catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
        return strStringFromFile;
    }
    
    public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        if (XMLDataConverter.builder == null) {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            XMLDataConverter.builder = factory.newDocumentBuilder();
        }
        return XMLDataConverter.builder;
    }
    
    public static int getPositionOfMatchingNodeFromList(final Node node1, final NodeList nodeListRoot2, final Map<Integer, Integer> mapMatchedNodePositions, final String[] arrElementsToExclude, final Map<String, String> mapIgnoreCaseSensitveValues) {
        int intPositionOfMatchingNodeFromList = -1;
        for (int intNodePositionInLst2 = 0; intNodePositionInLst2 < nodeListRoot2.getLength(); ++intNodePositionInLst2) {
            if (!isNumberPresentInMapValue(intNodePositionInLst2, mapMatchedNodePositions) && eligibleNodeForValidation(nodeListRoot2.item(intNodePositionInLst2), arrElementsToExclude)) {
                System.out.println("1 Node Names : " + node1.getNodeName() + " : " + node1.getTextContent() + " : " + nodeListRoot2.item(intNodePositionInLst2).getNodeName() + " : " + nodeListRoot2.item(intNodePositionInLst2).getTextContent());
                if (equal(node1, nodeListRoot2.item(intNodePositionInLst2), arrElementsToExclude, mapIgnoreCaseSensitveValues)) {
                    intPositionOfMatchingNodeFromList = intNodePositionInLst2;
                    System.out.println("3 Node Names : " + intPositionOfMatchingNodeFromList + " : " + node1.getNodeName() + " : " + node1.getTextContent() + " : " + nodeListRoot2.item(intNodePositionInLst2).getNodeName() + " : " + nodeListRoot2.item(intNodePositionInLst2).getTextContent());
                    break;
                }
            }
        }
        return intPositionOfMatchingNodeFromList;
    }
    
    public static boolean isNumberPresentInMapValue(final int integerToMatch, final Map<Integer, Integer> mapMatchedNodePositions) {
        for (final Map.Entry<Integer, Integer> entry : mapMatchedNodePositions.entrySet()) {
            if (entry.getValue() == integerToMatch) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean equal(final Node node1, final Node node2, final String[] arrElementsToExclude, final Map<String, String> mapIgnoreCaseSensitive) {
        if (node1.getNodeName().equals("StatusText") && node2.getNodeName().equals("StatusText")) {
            System.out.println("XMLDataConverter : " + node1.getNodeName() + " : " + node1.getTextContent() + " : " + node1.getNodeType() + " : " + node2.getNodeName() + " : " + node2.getTextContent() + " : " + node2.getNodeType());
        }
        System.out.println("mapIgnoreCaseSensitive size-------->" + mapIgnoreCaseSensitive);
        boolean isEqual = true;
        final List<Integer> lstMatchedPositionsInSecondList = new ArrayList<Integer>();
        if (node1.getNodeName().equals("masterProperties")) {
            System.out.println(node1.getNodeName());
        }
        if (node1.getNodeType() == node2.getNodeType() && node1.getNodeName().equals(node2.getNodeName())) {
            if (node1.getNodeType() == 3 || node1.getNodeType() == 2) {
                String strYesNo = "No";
                if (mapIgnoreCaseSensitive != null) {
                    final String strpath = Util_XMLConvert.getCompletePathForANode(node1).substring(0, Util_XMLConvert.getCompletePathForANode(node1).lastIndexOf("/"));
                    strYesNo = mapIgnoreCaseSensitive.get(strpath);
                }
                if (strYesNo != null && strYesNo.equalsIgnoreCase("Yes")) {
                    isEqual = node1.getTextContent().trim().equalsIgnoreCase(node2.getTextContent().trim());
                }
                else if (strYesNo != null && (strYesNo.startsWith("<") || strYesNo.startsWith(">") || strYesNo.startsWith("Value") || strYesNo.startsWith("Between"))) {
                    final String[] eqaulityCond = strYesNo.split(",");
                    if (node2.getTextContent() != null && !node2.getTextContent().trim().equals("")) {
                        final String s;
                        switch (s = eqaulityCond[0]) {
                            case "<": {
                                final Double nodeVal = Double.parseDouble(node2.getTextContent().trim());
                                final Double inputVal = Double.parseDouble(eqaulityCond[1].trim());
                                isEqual = (nodeVal < inputVal);
                                break;
                            }
                            case ">": {
                                final Double nodeVal2 = Double.parseDouble(node2.getTextContent().trim());
                                final Double inputVal2 = Double.parseDouble(eqaulityCond[1].trim());
                                isEqual = (nodeVal2 > inputVal2);
                                break;
                            }
                            case "Value": {
                                final String nodeVal3 = node2.getTextContent().trim();
                                final String inputVal3 = eqaulityCond[1].trim();
                                isEqual = nodeVal3.equals(inputVal3);
                                break;
                            }
                            case "Between": {
                                final Double nodeVal4 = Double.parseDouble(node2.getTextContent().trim());
                                final Double inputVal4 = Double.parseDouble(eqaulityCond[1].trim());
                                final Double inputVal5 = Double.parseDouble(eqaulityCond[2].trim());
                                if (nodeVal4 > inputVal4 && nodeVal4 < inputVal5) {
                                    isEqual = true;
                                    break;
                                }
                                isEqual = false;
                                break;
                            }
                            default:
                                break;
                        }
                    }
                }
                else {
                    isEqual = node1.getTextContent().trim().equals(node2.getTextContent().trim());
                }
            }
            else {
                final List<Node> lst1 = getChildrenWithoutTextNodesIfComplex(node1, arrElementsToExclude);
                final List<Node> lst2 = getChildrenWithoutTextNodesIfComplex(node2, arrElementsToExclude);
                System.out.println("604 : XMLDataConverter.equal(...) : " + lst1.size() + " : " + lst2.size());
                if (lst1.size() == lst2.size() && lst1.isEmpty() && lst2.isEmpty()) {
                    final NodeList childNodes1 = node1.getChildNodes();
                    final NodeList childNodes2 = node2.getChildNodes();
                    if (childNodes1 != null && childNodes1.getLength() > 0 && childNodes2 != null && childNodes2.getLength() > 0) {
                        System.out.println("CHILD Nodes1 : " + childNodes1.getLength() + "CHILD Nodes2 : " + childNodes2.getLength());
                        if (childNodes1.getLength() != childNodes2.getLength()) {
                            isEqual = false;
                        }
                    }
                    else {
                        final int intNodeListLen = childNodes1.getLength();
                        String strYesNo2 = "No";
                        if (mapIgnoreCaseSensitive != null) {
                            final String strpath2 = Util_XMLConvert.getCompletePathForANode(node1).substring(0, Util_XMLConvert.getCompletePathForANode(node1).lastIndexOf("/"));
                            strYesNo2 = mapIgnoreCaseSensitive.get(strpath2);
                        }
                        if (strYesNo2 != null && strYesNo2.equalsIgnoreCase("Yes")) {
                            isEqual = node1.getTextContent().trim().equalsIgnoreCase(node2.getTextContent().trim());
                        }
                        else if (strYesNo2 != null && (strYesNo2.startsWith("<") || strYesNo2.startsWith(">") || strYesNo2.startsWith("Value") || strYesNo2.startsWith("Between"))) {
                            final String[] eqaulityCond2 = strYesNo2.split(",");
                            if (node1.getTextContent() != null && !node1.getTextContent().trim().equals("")) {
                                final String s2;
                                switch (s2 = eqaulityCond2[0]) {
                                    case "<": {
                                        final Double nodeVal5 = Double.parseDouble(node2.getTextContent().trim());
                                        final Double inputVal6 = Double.parseDouble(eqaulityCond2[1].trim());
                                        isEqual = (nodeVal5 < inputVal6);
                                        break;
                                    }
                                    case ">": {
                                        final Double nodeVal6 = Double.parseDouble(node2.getTextContent().trim());
                                        final Double inputVal7 = Double.parseDouble(eqaulityCond2[1].trim());
                                        isEqual = (nodeVal6 > inputVal7);
                                        break;
                                    }
                                    case "Value": {
                                        final String nodeVal7 = node2.getTextContent().trim();
                                        final String inputVal8 = eqaulityCond2[1].trim();
                                        isEqual = nodeVal7.equals(inputVal8);
                                        break;
                                    }
                                    case "Between": {
                                        final Double nodeVal8 = Double.parseDouble(node2.getTextContent().trim());
                                        final Double inputVal9 = Double.parseDouble(eqaulityCond2[1].trim());
                                        final Double inputVal10 = Double.parseDouble(eqaulityCond2[2].trim());
                                        if (nodeVal8 > inputVal9 && nodeVal8 < inputVal10) {
                                            isEqual = true;
                                            break;
                                        }
                                        isEqual = false;
                                        break;
                                    }
                                    default:
                                        break;
                                }
                            }
                        }
                        else {
                            isEqual = node1.getTextContent().trim().equals(node2.getTextContent().trim());
                        }
                    }
                }
                if (lst1.size() != lst2.size()) {
                    isEqual = false;
                }
                else {
                    Node node1_child = null;
                    Node node2_child = null;
                    int intDoNotCompareElementsCnt = 0;
                    for (int node1Ctr = 0; node1Ctr < lst1.size(); ++node1Ctr) {
                        node1_child = lst1.get(node1Ctr);
                        if (eligibleNodeForValidation(node1_child, arrElementsToExclude)) {
                            boolean blnNodeMatchFound = false;
                            for (int node2Ctr = 0; node2Ctr < lst2.size(); ++node2Ctr) {
                                node2_child = lst2.get(node2Ctr);
                                if (eligibleNodeForValidation(node2_child, arrElementsToExclude)) {
                                    if (!isAlreadyMatchedNode(lstMatchedPositionsInSecondList, node2Ctr)) {
                                        if (equal(node1_child, node2_child, arrElementsToExclude, mapIgnoreCaseSensitive)) {
                                            lstMatchedPositionsInSecondList.add(new Integer(node2Ctr));
                                            blnNodeMatchFound = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (!blnNodeMatchFound) {
                                isEqual = false;
                                break;
                            }
                        }
                        else {
                            ++intDoNotCompareElementsCnt;
                        }
                    }
                    if (lstMatchedPositionsInSecondList.size() + intDoNotCompareElementsCnt != lst1.size()) {
                        isEqual = false;
                    }
                }
            }
        }
        else {
            isEqual = false;
        }
        if (node1.getNodeName().equals("StatusText") && node2.getNodeName().equals("StatusText")) {
            System.out.println("XMLDataConverter : isEqual : " + isEqual + " : " + node1.getNodeName() + " : " + node1.getTextContent() + " : " + node2.getNodeName() + " : " + node2.getTextContent());
        }
        System.out.println("@@@@@@@@equal value--->" + isEqual);
        System.out.println("node1.getNodeName()--->" + node1.getTextContent() + "-");
        System.out.println("node2.getNodeName()--->" + node2.getTextContent() + "-");
        final String str1 = node1.getTextContent().replaceAll("\\s*", "");
        final String str2 = node2.getTextContent().replaceAll("\\s*", "");
        System.out.println("str compare--->" + str1.equalsIgnoreCase(str2));
        return isEqual;
    }
    
    private static boolean eligibleNodeForValidation(final Node objNodeToCheck, final String[] arrElementsToExclude) {
        boolean isEligibleForValidation = true;
        if (arrElementsToExclude != null && arrElementsToExclude.length > 0) {
            final String strElementPath = getCompletePathForANode(objNodeToCheck);
            for (final String strTempElementToExclude : arrElementsToExclude) {
                if (strTempElementToExclude.equals(strElementPath)) {
                    isEligibleForValidation = false;
                    break;
                }
            }
        }
        return isEligibleForValidation;
    }
    
    public static boolean isAlreadyMatchedNode(final List<Integer> lstMatchedPositions, final int nodeCtr) {
        boolean isAlreadyMatchedNode = false;
        for (final Integer intTempPosition : lstMatchedPositions) {
            if (intTempPosition == nodeCtr) {
                isAlreadyMatchedNode = true;
                break;
            }
        }
        return isAlreadyMatchedNode;
    }
    
    public static List<String> findMismatchedNodePositionsInXML1AndXML2(final Map<Integer, Integer> mapMatchedNodePositions, final int intElementCount1, final int intElementCount2) {
        final List<String> lstMismatchedNodePositionsInXML1AndXML2 = new ArrayList<String>();
        final List<String> lstMismatchedPositionsInXML1 = new ArrayList<String>();
        final List<String> lstMismatchedPositionsInXML2 = new ArrayList<String>();
        String strPosition1 = null;
        String strPosition2 = null;
        for (int intTempPosition = 0; intTempPosition < intElementCount1 || intTempPosition < intElementCount2; ++intTempPosition) {
            boolean blnElementFoundInXML1 = false;
            boolean blnElementFoundInXML2 = false;
            for (final Map.Entry<Integer, Integer> objEntry : mapMatchedNodePositions.entrySet()) {
                if (objEntry.getKey() == intTempPosition) {
                    blnElementFoundInXML1 = true;
                }
                if (objEntry.getValue() == intTempPosition) {
                    blnElementFoundInXML2 = true;
                }
            }
            if (!blnElementFoundInXML1 && intTempPosition < intElementCount1) {
                lstMismatchedPositionsInXML1.add(new Integer(intTempPosition).toString());
                System.out.println("---1 : " + intTempPosition);
            }
            if (!blnElementFoundInXML2 && intTempPosition < intElementCount2) {
                lstMismatchedPositionsInXML2.add(new Integer(intTempPosition).toString());
                System.out.println("---2 : " + intTempPosition);
            }
        }
        for (int intTempPosition = 0; intTempPosition < lstMismatchedPositionsInXML1.size(); ++intTempPosition) {
            strPosition1 = lstMismatchedPositionsInXML1.get(intTempPosition);
            System.out.println("701 : Mismatched elements : " + strPosition1 + ": ");
            lstMismatchedNodePositionsInXML1AndXML2.add(String.valueOf(strPosition1) + ": ");
        }
        for (int intTempPosition = 0; intTempPosition < lstMismatchedPositionsInXML2.size(); ++intTempPosition) {
            strPosition2 = lstMismatchedPositionsInXML2.get(intTempPosition);
            System.out.println("706 : Mismatched elements : :" + strPosition2);
            lstMismatchedNodePositionsInXML1AndXML2.add(" :" + strPosition2);
        }
        return lstMismatchedNodePositionsInXML1AndXML2;
    }
    
    public static List<String> findMatchedNodePositionsInXML1AndXML2(final Map<Integer, Integer> mapMatchedNodePositions, final int intElementCount1) {
        final List<String> lstMatchedNodePositionsInXML1 = new ArrayList<String>();
        final List<String> lstMatchedPositionsInXML1 = new ArrayList<String>();
        String strPosition1 = null;
        for (int intTempPosition = 0; intTempPosition < intElementCount1; ++intTempPosition) {
            boolean blnElementFoundInXML1 = false;
            for (final Map.Entry<Integer, Integer> objEntry : mapMatchedNodePositions.entrySet()) {
                if (objEntry.getKey() == intTempPosition) {
                    blnElementFoundInXML1 = true;
                }
            }
            if (blnElementFoundInXML1 && intTempPosition < intElementCount1) {
                lstMatchedPositionsInXML1.add(new Integer(intTempPosition).toString());
            }
        }
        for (int intTempPosition = 0; intTempPosition < lstMatchedPositionsInXML1.size(); ++intTempPosition) {
            if (intTempPosition >= lstMatchedPositionsInXML1.size()) {
                strPosition1 = " ";
            }
            else {
                strPosition1 = lstMatchedPositionsInXML1.get(intTempPosition);
            }
            System.out.println("731 : Matched elements : " + strPosition1);
            lstMatchedNodePositionsInXML1.add(strPosition1);
        }
        return lstMatchedNodePositionsInXML1;
    }
    
    public static List<String> getNodeInformationForMismatchedData(final String strMismatchedPosition_ColumnSeparated, final NodeList nodeListRoot1, final NodeList nodeListRoot2, final String[] arrElementsToExclude) {
        List<String> lstResult = new ArrayList<String>();
        final List<String> lstNode1Data = new ArrayList<String>();
        final List<String> lstNode2Data = new ArrayList<String>();
        final String[] arrMismatchedPositions = strMismatchedPosition_ColumnSeparated.split(":");
        int intElementPosition1 = -1;
        int intElementPosition2 = -1;
        if (!arrMismatchedPositions[0].trim().equals("")) {
            intElementPosition1 = Integer.parseInt(arrMismatchedPositions[0].trim());
            if (eligibleNodeForValidation(nodeListRoot1.item(intElementPosition1), arrElementsToExclude)) {
                lstNode1Data.addAll(getListOfCSVDataRows(nodeListRoot1.item(intElementPosition1)));
            }
        }
        if (!arrMismatchedPositions[1].trim().equals("")) {
            intElementPosition2 = Integer.parseInt(arrMismatchedPositions[1].trim());
            if (eligibleNodeForValidation(nodeListRoot2.item(intElementPosition2), arrElementsToExclude)) {
                lstNode2Data.addAll(getListOfCSVDataRows(nodeListRoot2.item(intElementPosition2)));
            }
        }
        lstResult = mergeMismatchedListsInCSVFormat(lstNode1Data, lstNode2Data);
        return lstResult;
    }
    
    public static List<String> getNodeInformationForMatchedData(final String strMatchedPosition_ColumnSeparated, final NodeList nodeListRoot1) {
        List<String> lstResult = new ArrayList<String>();
        final List<String> lstNode1Data = new ArrayList<String>();
        int intElementPosition1 = -1;
        if (!strMatchedPosition_ColumnSeparated.trim().equals("")) {
            intElementPosition1 = Integer.parseInt(strMatchedPosition_ColumnSeparated.trim());
            lstNode1Data.addAll(getListOfCSVDataRows(nodeListRoot1.item(intElementPosition1)));
        }
        lstResult = mergeMatchedListsInCSVFormat(lstNode1Data);
        return lstResult;
    }
    
    public static List<String> getListOfCSVDataRows(final Node objNode) {
        final List<String> lstDataRows = new ArrayList<String>();
        if (isSimpleElement(objNode)) {
            lstDataRows.add(getCompletePathAndDataInCSVFormat(objNode));
        }
        else {
            final NodeList lstChildNodes = objNode.getChildNodes();
            for (int i = 0; i < lstChildNodes.getLength(); ++i) {
                if (lstChildNodes.item(i).getNodeType() != 3) {
                    lstDataRows.addAll(getListOfCSVDataRows(lstChildNodes.item(i)));
                }
            }
        }
        return lstDataRows;
    }
    
    private static String getCompletePathAndDataInCSVFormat(final Node objNode) {
        String strCompletePathAndDataInCSVFormat = null;
        final String strData = objNode.getTextContent();
        final String strCompletePath = getCompletePathForANode(objNode);
        strCompletePathAndDataInCSVFormat = String.valueOf(strCompletePath) + "," + strData;
        return strCompletePathAndDataInCSVFormat;
    }
    
    private static String getCompletePathForANode(final Node objNode) {
        String strCompletePathForNode = objNode.getNodeName();
        Node tempNode = objNode;
        while (tempNode.getParentNode() != null) {
            tempNode = tempNode.getParentNode();
            if (tempNode.getNodeName().equals("#document")) {
                strCompletePathForNode = "/" + strCompletePathForNode;
            }
            else {
                strCompletePathForNode = String.valueOf(tempNode.getNodeName()) + "/" + strCompletePathForNode;
            }
        }
        return strCompletePathForNode;
    }
    
    private static boolean isSimpleElement(final Node objNode) {
        final NodeList lstChildNodes = objNode.getChildNodes();
        boolean isSimpleElement = true;
        for (int i = 0; i < lstChildNodes.getLength(); ++i) {
            if (lstChildNodes.item(i).getNodeType() != 3) {
                isSimpleElement = false;
            }
        }
        return isSimpleElement;
    }
    
    private static List<String> mergeMismatchedListsInCSVFormat(final List<String> lstNode1Data, final List<String> lstNode2Data) {
        final List<String> lstResults = new ArrayList<String>();
        final int intLst1Size = lstNode1Data.size();
        final int intLst2Size = lstNode2Data.size();
        String str1 = null;
        String str2 = null;
        str2 = " , ";
        for (int i = 0; i < intLst1Size; ++i) {
            str1 = lstNode1Data.get(i);
            lstResults.add(String.valueOf(str1) + "," + str2);
        }
        str1 = " , ";
        for (int i = 0; i < intLst2Size; ++i) {
            str2 = lstNode2Data.get(i);
            lstResults.add(String.valueOf(str1) + "," + str2);
        }
        return lstResults;
    }
    
    private static List<String> mergeMatchedListsInCSVFormat(final List<String> lstNode1Data) {
        final List<String> lstResults = new ArrayList<String>();
        final int intLst1Size = lstNode1Data.size();
        String str1 = null;
        final String str2 = " , ";
        for (int i = 0; i < intLst1Size; ++i) {
            str1 = lstNode1Data.get(i);
            final String[] strArray = str1.split(",");
            System.out.println("str1 value-->" + strArray.length);
            if (strArray.length > 1) {
                lstResults.add(String.valueOf(str1) + "," + strArray[1]);
            }
            else {
                lstResults.add(String.valueOf(str1) + ",");
            }
        }
        return lstResults;
    }
    
    public static String getLookupValue(String strElementData, final String strRowID_WithSpecificElement, final MappingDataDTO objMappingData) {
        if (objMappingData.mapDataSheetLookupForConversion != null) {
            final String strLookupName = objMappingData.mapDataSheetLookupForConversion.get(strRowID_WithSpecificElement);
            System.out.println("154 : CommonUtil.getlookupValue(...)" + strLookupName);
            if (objMappingData.mapWSLookup != null) {
                final Map<String, String> mapLOVForMapName = objMappingData.mapWSLookup.get(strRowID_WithSpecificElement);
                if (mapLOVForMapName != null) {
                    final String strLookupValue = mapLOVForMapName.get(strElementData);
                    if (strLookupValue != null && strElementData.trim().length() > 0) {
                        strElementData = strLookupValue;
                    }
                }
            }
        }
        return strElementData;
    }
    
    public static List<String> readContentToList(final String strConfigTextFile) {
        final List<String> lst = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(strConfigTextFile));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                lst.add(line);
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("184 : CommonUtil.readContentToList(...) : FileNotFoundException : " + e);
        }
        catch (IOException e2) {
            System.out.println("186 : CommonUtil.readContentToList(...) : IOException : " + e2);
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                }
                catch (IOException e3) {
                    System.out.println("192 : CommonUtil.readContentToList(...) : IOException : " + e3);
                }
            }
        }
        if (br != null) {
            try {
                br.close();
            }
            catch (IOException e3) {
                System.out.println("192 : CommonUtil.readContentToList(...) : IOException : " + e3);
            }
        }
        return lst;
    }
    
    public static void printResultsToFile(final String strComparisonResultsFile, final List<String> lstPassedCSVData, final List<String> lstFailedCSVData) {
        final HSSFWorkbook workbook = new HSSFWorkbook();
        try {
            HSSFSheet sheet = workbook.createSheet("MatchedData");
            if (lstPassedCSVData != null) {
                short intRowCnt = 0;
                for (final String strPassedCSVROWData : lstPassedCSVData) {
                    final String[] arrPassedCSVRowData = strPassedCSVROWData.split(",");
                    final HSSFSheet hssfSheet = sheet;
                    final short n = intRowCnt;
                    intRowCnt = (short)(n + 1);
                    final HSSFRow rowhead = hssfSheet.createRow((int)n);
                    for (int intColCnt = 0; intColCnt < arrPassedCSVRowData.length; ++intColCnt) {
                        rowhead.createCell(intColCnt).setCellValue(arrPassedCSVRowData[intColCnt]);
                    }
                }
            }
            sheet = workbook.createSheet("MismatchedData");
            if (lstFailedCSVData != null) {
                short intRowCnt = 0;
                for (final String strFailedCSVROWData : lstFailedCSVData) {
                    final String[] arrFailedCSVRowData = strFailedCSVROWData.split(",");
                    final HSSFSheet hssfSheet2 = sheet;
                    final short n2 = intRowCnt;
                    intRowCnt = (short)(n2 + 1);
                    final HSSFRow rowhead = hssfSheet2.createRow((int)n2);
                    for (int intColCnt = 0; intColCnt < arrFailedCSVRowData.length; ++intColCnt) {
                        rowhead.createCell(intColCnt).setCellValue(arrFailedCSVRowData[intColCnt]);
                    }
                }
            }
            final FileOutputStream fileOut = new FileOutputStream(strComparisonResultsFile);
            workbook.write((OutputStream)fileOut);
            fileOut.close();
            System.out.println("Your results file has been generated!");
        }
        catch (Exception ex) {
            System.out.println("923 : " + ex);
        }
    }
    
    public static List<Node> getChildrenWithoutTextNodesIfComplex(final Node objNodes, final String[] arrElementsToExclude) {
        final NodeList childNodes = objNodes.getChildNodes();
        final int intNodeListLen = childNodes.getLength();
        final List<Node> lstChildrenWithoutTextNodes = new ArrayList<Node>();
        boolean isComplex = false;
        for (int i = 0; i < intNodeListLen; ++i) {
            if (childNodes.item(i).getNodeType() == 1) {
                isComplex = true;
                break;
            }
        }
        for (int i = 0; i < intNodeListLen; ++i) {
            if (isComplex) {
                if (childNodes.item(i).getNodeType() != 3) {
                    final String str = childNodes.item(i).getTextContent();
                    if (str != null && !str.equalsIgnoreCase("") && eligibleNodeForValidation(childNodes.item(i), arrElementsToExclude)) {
                        lstChildrenWithoutTextNodes.add(childNodes.item(i));
                    }
                }
            }
            else if (eligibleNodeForValidation(childNodes.item(i), arrElementsToExclude)) {
                lstChildrenWithoutTextNodes.add(childNodes.item(i));
            }
        }
        return lstChildrenWithoutTextNodes;
    }
}
