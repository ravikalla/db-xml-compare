package in.ravikalla.dbXmlCompare.xmlConvert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import in.ravikalla.dbXmlCompare.xmlCompareUtil.StoredProc;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.excel.ExcelCommon;
import in.ravikalla.dbXmlCompare.xmlConvert.dto.ExcelRowData;
import in.ravikalla.dbXmlCompare.xmlConvert.dto.XMLNode;
import in.ravikalla.dbXmlCompare.xmlConvert.util.Util_XMLConvert;

public class ConvertSQLToXML
{
    public static void main(final String[] args) {
        final String strExcelFileName = "C:/Data/1.xls";
        final String strParams = "ListID:12345;ClientID:12345";
        try {
            System.out.println("ReadExcelAndConvertToXML---->" + testReadExcelAndConvertToXML());
        }
        catch (Exception e) {
            System.out.println("31 : ConvertSQLToXML.main(...) : Exception : " + e);
        }
        System.out.println("End : ConvertSQLToXML.main(...)");
    }
    
    private static String testReadExcelAndConvertToXML() {
        final String strExcelFileName = "C:/data/sqldata/GET_clients_{client-id}_products_{product-id}_feature-classes.xls";
        final String strParams = "ClientID:064157;ProductID:48-1;ResponseStatus:SUCCESS;ResponseStatusText:SUCCESS;ResponseMessageCode:SUCCESS;ResponseMessageType:App1;ResponseMessageText:SUCCESS";
        final String strXML = readExcelAndConvertToXML(strExcelFileName, "Sheet1", "jdbc:oracle:thin:@//<ServerIP>:<PORT>/<DBName>", "<ID>", "<PWD>", strParams);
        return strXML;
    }
    
    private static String readDBMappingFileAndConvertToXML() {
        final String strStoredProc = "<SchemaName>.<PKG NAME>.<SP NAME>(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        final String strDatatypesList = "CURSOR,CURSOR,CURSOR,CURSOR,CURSOR,CURSOR,CURSOR,CURSOR,CURSOR,CURSOR,CURSOR,CURSOR,CURSOR,CURSOR,STRING,STRING,NUMBER,CURSOR";
        final String inParametersPositions = "1:123456,2:TEST_VAL,3:Y,22:N";
        final String outParameterPositions = "4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21";
        final String userName = "<U_ID>";
        final String password = "<PWD>";
        final String strConnectionString = "jdbc:oracle:thin:@//<IP>:<PORT>/<DB>";
        final String strXML = convertDBStoredProcedureToXML("C:/Coding/DBProcedureMapping.xls", "Sheet1", strConnectionString, userName, password, strStoredProc, strDatatypesList, inParametersPositions, outParameterPositions);
        return strXML;
    }
    
    public static Map<Integer, Connection> getConnectionsMapFromExcelFile(final String strExcelFileName, final String strSheetName, final String strDbPasswords) {
        final Map<Integer, Connection> connectionsMap = new HashMap<Integer, Connection>();
        final Row objRow = null;
        final Cell objCell = null;
        Connection conn = null;
        try {
            final Workbook wb = ExcelCommon.getWorkBook(strExcelFileName);
            final Sheet objSheet = wb.getSheet(strSheetName);
            if (objSheet == null) {
                throw new Exception("Sheet name not present : " + strSheetName);
            }
            objSheet.iterator();
            final Iterator<Row> rowIterator = (Iterator<Row>)objSheet.iterator();
            final String[] passwords = strDbPasswords.split("--");
            int i = 1;
            while (rowIterator.hasNext()) {
                final Row row = rowIterator.next();
                if (row.getRowNum() != 0) {
                    final int DBNo = (int)row.getCell(0).getNumericCellValue();
                    final String strDBConnURL = row.getCell(1).getStringCellValue().toString();
                    final String strDBUID = row.getCell(2).getStringCellValue().toString();
                    String strDBPWD = row.getCell(3).getStringCellValue().toString();
                    if (strDBPWD == null) {
                        strDBPWD = passwords[i];
                        System.out.println("Passwords getting from parameters" + passwords[i]);
                    }
                    else if (strDBPWD.equalsIgnoreCase("")) {
                        strDBPWD = passwords[i];
                        System.out.println(" in empty Passwords getting from parameters" + passwords[i]);
                    }
                    System.out.println("length-->" + strDBConnURL.length());
                    if (strDBConnURL.equalsIgnoreCase("jdbc:oracle:thin:@//<IP>:<PORT>/<DB>")) {
                        System.out.println("true");
                    }
                    System.out.println("DBNo-->" + DBNo);
                    System.out.println("strDBUID-->" + strDBUID);
                    System.out.println("strDBPWD-->" + strDBPWD);
                    System.out.println("strDBConnURL-->" + strDBConnURL);
                    conn = Util_XMLConvert.getDBConnection(strDBConnURL, strDBUID, strDBPWD);
                    System.out.println("conn--->" + conn);
                    connectionsMap.put(DBNo, conn);
                    ++i;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return connectionsMap;
    }
    
    public static String readExcelAndConvertToXML(final String strExcelFileName, String strSheetName, final String strDBConnURL, final String strDBUID, String strDBPWD, final String strExternalParameters) {
        System.out.println("Start : ConvertSQLToXML.readExcelAndConvertToXML(...)");
        printParametersOfXMLToXMLComparison(strExcelFileName, strSheetName, strDBConnURL, strDBUID, strDBPWD, strExternalParameters);
        String strXML = null;
        Connection conn = null;
        Map<String, String> mapExternalParameters = null;
        try {
            final String[] strSheetNamesArray = strSheetName.split(";");
            Map<Integer, Connection> dbConnectionMap = null;
            if (strSheetNamesArray.length > 1) {
                dbConnectionMap = getConnectionsMapFromExcelFile(strExcelFileName, strSheetNamesArray[1], strDBPWD);
                strSheetName = strSheetNamesArray[0];
                strDBPWD = strDBPWD.split("--")[0];
                System.out.println("password-->" + strDBPWD);
            }
            final XMLNode objXMLNodeTree = generateXMLNodeTreeFromConfig(strExcelFileName, strSheetName);
            System.out.println("43 : Created XMLNodeTree : ConvertSQLToXML.readExcelAndConvertToXML(...)");
            conn = Util_XMLConvert.getDBConnection(strDBConnURL, strDBUID, strDBPWD);
            System.out.println("45 : Created DB Connection : ConvertSQLToXML.readExcelAndConvertToXML(...)");
            mapExternalParameters = Util_XMLConvert.convertExternalParametersToMap(strExternalParameters);
            final List<XMLNode> lstPopulatedXMLNodes = Util_XMLConvert.parseAllNodesAndExecuteQueries(objXMLNodeTree, mapExternalParameters, conn, dbConnectionMap);
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
            e5.printStackTrace();
        }
        catch (Exception e6) {
            System.out.println("63 : ConvertSQLToXML.readExcelAndConvertToXML(...) : Exception : " + e6);
        }
        finally {
            if (conn != null) {
                try {
                    conn.close();
                }
                catch (SQLException e7) {
                    System.out.println("67 : ConvertSQLToXML.readExcelAndConvertToXML(...) : Issue while creating DB connection : SQLException : " + e7);
                }
            }
        }
        if (conn != null) {
            try {
                conn.close();
            }
            catch (SQLException e7) {
                System.out.println("67 : ConvertSQLToXML.readExcelAndConvertToXML(...) : Issue while creating DB connection : SQLException : " + e7);
            }
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
}
