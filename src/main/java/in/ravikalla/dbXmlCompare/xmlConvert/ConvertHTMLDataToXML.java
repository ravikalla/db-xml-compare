package in.ravikalla.dbXmlCompare.xmlConvert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import in.ravikalla.dbXmlCompare.xmlCompareUtil.StoredProc;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.excel.ExcelCommon;
import in.ravikalla.dbXmlCompare.xmlConvert.dto.ExcelRowData;
import in.ravikalla.dbXmlCompare.xmlConvert.dto.XMLNode;
import in.ravikalla.dbXmlCompare.xmlConvert.util.Util_XMLConvert;

public class ConvertHTMLDataToXML
{
    public static void main(final String[] args) {
        try {
            System.out.println("ReadExcelAndConvertToXML---->" + testReadExcelAndConvertToXML());
        }
        catch (Exception e) {
            System.out.println("31 : ConvertSQLToXML.main(...) : Exception : " + e);
        }
    }
    
    private static String testReadExcelAndConvertToXML() {
        final String strExcelFileName = "C:/UI-XML/ExcelMappingConfigFileTemplate.xls";
        final String strParams = "1:true;0:false;Hi, Ravi:Hi, Kalla";
        final String strXML = readExcelAndConvertToXML(strExcelFileName, "Sheet1", strParams, "C:/UI-XML/Data.htm");
        return strXML;
    }
    
    public static String readExcelAndConvertToXML(final String strExcelFileName, final String strSheetName, final String strExternalParameters, final String strHtmlFilePath) {
        System.out.println("Start : ConvertSQLToXML.readExcelAndConvertToXML(...)");
        String strXML = null;
        Map<String, String> mapExternalParameters = null;
        try {
            final XMLNode objXMLNodeTree = generateXMLNodeTreeFromConfig(strExcelFileName, strSheetName);
            mapExternalParameters = Util_XMLConvert.convertExternalParametersDataToMap(strExternalParameters);
            final LinkedHashMap<String, String> linkedHashMap = HTMLUtility.getXPathsAndValues(strHtmlFilePath);
            final List<XMLNode> lstPopulatedXMLNodes = Util_XMLConvert.parseAllNodesAndFromHtmlData(objXMLNodeTree, mapExternalParameters, linkedHashMap, strHtmlFilePath);
            if (lstPopulatedXMLNodes != null && lstPopulatedXMLNodes.size() == 1) {
                final XMLNode objXMLNode = lstPopulatedXMLNodes.get(0);
                System.out.println("50 : Converted XMLNode to string : ConvertSQLToXML.readExcelAndConvertToXML(...)");
                strXML = Util_XMLConvert.convertCustomXMLNodesToXML(objXMLNode);
            }
            System.out.println("53 : Created XML string : ConvertSQLToXML.readExcelAndConvertToXML(...)");
        }
        catch (ParserConfigurationException e) {
            System.out.println("53 : ConvertSQLToXML.readExcelAndConvertToXML(...) : ParserConfigurationException : " + e);
        }
        catch (TransformerFactoryConfigurationError e2) {
            System.out.println("55 : ConvertSQLToXML.readExcelAndConvertToXML(...) : TransformerFactoryConfigurationError : " + e2);
        }
        catch (TransformerException e3) {
            System.out.println("57 : ConvertSQLToXML.readExcelAndConvertToXML(...) : TransformerException : " + e3);
        }
        catch (ClassNotFoundException e4) {
            System.out.println("59 : ConvertSQLToXML.readExcelAndConvertToXML(...) : ClassNotFoundException : " + e4);
        }
        catch (SQLException e5) {
            System.out.println("61 : ConvertSQLToXML.readExcelAndConvertToXML(...) : SQLException : " + e5);
            e5.printStackTrace();
        }
        catch (Exception e6) {
            System.out.println("63 : ConvertSQLToXML.readExcelAndConvertToXML(...) : Exception : " + e6);
        }
        System.out.println("End : ConvertSQLToXML.readExcelAndConvertToXML(...)");
        return strXML;
    }
    
    private static XMLNode generateXMLNodeTreeFromConfig(final String strExcelFileName, final String strSheetName) throws Exception {
        System.out.println("Start : ConvertSQLToXML.generateXMLNodeTreeFromConfig(...)");
        boolean isFileEnded = false;
        ExcelRowData objExcelRowData_Temp = null;
        XMLNode objXMLRootNode = null;
        XMLNode objXMLNode_Temp = null;
        XMLNode objParentXMLNode = null;
        String strPreviousPath = null;
        try {
            final Workbook wb = ExcelCommon.getWorkBook(strExcelFileName);
            final Sheet objSheet = wb.getSheet(strSheetName);
            if (objSheet == null) {
                throw new Exception("Sheet name not present : " + strSheetName);
            }
            System.out.println(objSheet.getLastRowNum());
            int intRowCount = 1;
            while (!isFileEnded) {
                System.out.println("intRowCount-------------->" + intRowCount);
                objExcelRowData_Temp = Util_XMLConvert.readExcelRowData(objSheet, intRowCount);
                if (objExcelRowData_Temp == null) {
                    isFileEnded = true;
                }
                else {
                    objXMLNode_Temp = Util_XMLConvert.convertExcelRowToXMLNode(objExcelRowData_Temp);
                    if (objXMLRootNode == null) {
                        objXMLRootNode = objXMLNode_Temp;
                    }
                    else {
                        System.out.println("current node element--->" + objXMLNode_Temp.strElementName);
                        strPreviousPath = Util_XMLConvert.getPreviousPath(objSheet, objExcelRowData_Temp.intRow, objExcelRowData_Temp.intCol);
                        System.out.println("strPreviousPath--->" + strPreviousPath);
                        final String[] arrPreviousPathElementNames = strPreviousPath.split("/");
                        if (arrPreviousPathElementNames.length > 1) {
                            if (!arrPreviousPathElementNames[1].equals(objXMLRootNode.strElementName)) {
                                throw new Exception("Incorrect path to insert into the tree : " + strPreviousPath);
                            }
                            objParentXMLNode = Util_XMLConvert.getParentNode(objXMLRootNode, arrPreviousPathElementNames, 1);
                            objParentXMLNode.lstChildNodes.add(objXMLNode_Temp);
                        }
                        else {
                            System.out.println("Repeated root element encountered in excel sheet at Row# " + intRowCount);
                        }
                    }
                }
                ++intRowCount;
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("39 : ConvertSQLToXML.generateXMLNodeTreeFromConfig(...) : FileNotFoundException : " + e);
        }
        catch (InvalidFormatException e2) {
            System.out.println("41 : ConvertSQLToXML.generateXMLNodeTreeFromConfig(...) : InvalidFormatException : " + e2);
        }
        catch (IOException e3) {
            System.out.println("43 : ConvertSQLToXML.generateXMLNodeTreeFromConfig(...) : IOException : " + e3);
        }
        catch (Exception e4) {
            System.out.println("69 : ConvertSQLToXML.generateXMLNodeTreeFromConfig(...) : Exception : " + e4);
            e4.printStackTrace();
            throw e4;
        }
        System.out.println("End : ConvertSQLToXML.generateXMLNodeTreeFromConfig(...)");
        return objXMLRootNode;
    }
    
    public static void printParametersOfXMLToXMLComparison(final String strExcelFileName, final String strSheetName, final String strDBConnURL, final String strDBUID, final String strDBPWD, final String strExternalParameters) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(String.valueOf(strExcelFileName) + "_Params");
            out.println(String.valueOf(strExcelFileName) + "\n" + strSheetName + "\n" + strDBConnURL + "\n" + strDBUID + "\n" + strDBPWD + "\n" + strExternalParameters);
        }
        catch (FileNotFoundException e) {
            System.out.println("315 : printParametersOfXMLToXMLComparison(...) : FileNotFoundException : " + e);
            return;
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
        if (out != null) {
            out.close();
        }
    }
    
    public static String convertDBStoredProcedureToXML(final String strExcelFileName, final String strSheetName, final String strDBConnURL, final String strDBUID, final String strDBPWD, final String strStoredProc, final String strDatatypesList, final String inParametersPositions, final String outParameterPositions) {
        System.out.println("Start : ConvertSQLToXML.readExcelAndConvertToXML(...)");
        final Map<Integer, Object> hashMap = StoredProc.getStoredProcData(strStoredProc, strDatatypesList, inParametersPositions, outParameterPositions, strDBConnURL, strDBUID, strDBPWD);
        String strXML = null;
        final Connection conn = null;
        final Map<String, String> mapExternalParameters = null;
        try {
            final XMLNode objXMLNodeTree = generateXMLNodeTreeFromConfig(strExcelFileName, strSheetName);
            System.out.println("43 : Created XMLNodeTree : ConvertSQLToXML.readExcelAndConvertToXML(...)");
            final List<XMLNode> lstPopulatedXMLNodes = Util_XMLConvert.parseAllNodesAndFillNodeDataFromDatabase(objXMLNodeTree, null, conn, hashMap);
            System.out.println("47 : Created list of populated XML nodes : ConvertSQLToXML.readExcelAndConvertToXML(...) : " + lstPopulatedXMLNodes.size());
            if (lstPopulatedXMLNodes != null && lstPopulatedXMLNodes.size() == 1) {
                final XMLNode objXMLNode = lstPopulatedXMLNodes.get(0);
                System.out.println("50 : Converted XMLNode to string : ConvertSQLToXML.readExcelAndConvertToXML(...)");
                strXML = Util_XMLConvert.convertCustomXMLNodesToXML(objXMLNode);
            }
            System.out.println("53 : Created XML string : ConvertSQLToXML.readExcelAndConvertToXML(...)");
        }
        catch (ParserConfigurationException e) {
            System.out.println("53 : ConvertSQLToXML.readExcelAndConvertToXML(...) : ParserConfigurationException : " + e);
        }
        catch (TransformerFactoryConfigurationError e2) {
            System.out.println("55 : ConvertSQLToXML.readExcelAndConvertToXML(...) : TransformerFactoryConfigurationError : " + e2);
        }
        catch (TransformerException e3) {
            System.out.println("57 : ConvertSQLToXML.readExcelAndConvertToXML(...) : TransformerException : " + e3);
        }
        catch (ClassNotFoundException e4) {
            System.out.println("59 : ConvertSQLToXML.readExcelAndConvertToXML(...) : ClassNotFoundException : " + e4);
        }
        catch (SQLException e5) {
            System.out.println("61 : ConvertSQLToXML.readExcelAndConvertToXML(...) : SQLException : " + e5);
        }
        catch (Exception e6) {
            e6.printStackTrace();
            System.out.println("63 : ConvertSQLToXML.readExcelAndConvertToXML(...) : Exception : " + e6);
        }
        finally {
            if (conn != null) {
                try {
                    conn.close();
                }
                catch (SQLException e7) {
                    e7.printStackTrace();
                    System.out.println("67 : ConvertSQLToXML.readExcelAndConvertToXML(...) : Issue while creating DB connection : SQLException : " + e7);
                }
            }
        }
        if (conn != null) {
            try {
                conn.close();
            }
            catch (SQLException e7) {
                e7.printStackTrace();
                System.out.println("67 : ConvertSQLToXML.readExcelAndConvertToXML(...) : Issue while creating DB connection : SQLException : " + e7);
            }
        }
        System.out.println("End : ConvertSQLToXML.readExcelAndConvertToXML(...)");
        return strXML;
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
