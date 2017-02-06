package in.ravikalla.dbXmlCompare.xmlConvert.util;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.CommonUtil;
import in.ravikalla.dbXmlCompare.xmlConvert.HTMLUtility;
import in.ravikalla.dbXmlCompare.xmlConvert.dto.ExcelRowData;
import in.ravikalla.dbXmlCompare.xmlConvert.dto.XMLNode;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.pool.OracleDataSource;

public class Util_XMLConvert
{
    public static ExcelRowData readExcelRowData(final Sheet objSheet, final int intRowCount) {
        ExcelRowData objExcelRowData = null;
        final int intXMLStartCol = 2;
        Cell cellXMLElementName = null;
        Cell cellSQLQuery = null;
        Cell cellDBColName = null;
        String strXMLElementName = null;
        boolean isLastColEncountered = false;
        String strSQLQuery = null;
        String strDBCol = null;
        final Row objRow = objSheet.getRow(intRowCount);
        if (objRow != null) {
            for (int intColCtr = intXMLStartCol; intColCtr < 100 && !isLastColEncountered; ++intColCtr) {
                cellXMLElementName = objRow.getCell(intColCtr);
                if (cellXMLElementName != null && 3 != cellXMLElementName.getCellType()) {
                    if (1 == cellXMLElementName.getCellType()) {
                        strXMLElementName = cellXMLElementName.getStringCellValue().trim();
                    }
                    else if (cellXMLElementName.getCellType() == 0) {
                        strXMLElementName = CommonUtil.convertDoubleToString(cellXMLElementName.getNumericCellValue());
                    }
                    else if (4 == cellXMLElementName.getCellType()) {
                        strXMLElementName = new Boolean(cellXMLElementName.getBooleanCellValue()).toString();
                    }
                    if (!strXMLElementName.equals("")) {
                        cellSQLQuery = objRow.getCell(0);
                        cellDBColName = objRow.getCell(1);
                        if (cellSQLQuery != null && 1 == cellSQLQuery.getCellType()) {
                            strSQLQuery = cellSQLQuery.getStringCellValue().trim();
                        }
                        if (cellSQLQuery != null && cellSQLQuery.getCellType() == 0) {
                            strSQLQuery = String.valueOf(cellSQLQuery.getNumericCellValue());
                        }
                        if (cellDBColName != null && 1 == cellDBColName.getCellType()) {
                            strDBCol = cellDBColName.getStringCellValue().trim();
                        }
                        objExcelRowData = new ExcelRowData();
                        objExcelRowData.strXMLElementName = strXMLElementName;
                        objExcelRowData.intRow = intRowCount;
                        objExcelRowData.intCol = intColCtr;
                        objExcelRowData.strSQLQuery = strSQLQuery;
                        objExcelRowData.strDBCol = strDBCol;
                        isLastColEncountered = true;
                        System.out.println("strXMLElementName***->" + strXMLElementName);
                    }
                }
            }
        }
        return objExcelRowData;
    }
    
    public static XMLNode convertExcelRowToXMLNode(final ExcelRowData objExcelRowData_Temp) {
        final XMLNode objXMLNode = new XMLNode();
        objXMLNode.strElementName = objExcelRowData_Temp.strXMLElementName;
        objXMLNode.intElementType = 1;
        objXMLNode.strSQLQuery = objExcelRowData_Temp.strSQLQuery;
        if (objExcelRowData_Temp.strDBCol != null && objExcelRowData_Temp.strDBCol.length() > 0) {
            objXMLNode.strColumnName = objExcelRowData_Temp.strDBCol.toUpperCase();
        }
        return objXMLNode;
    }
    
    public static String getPreviousPath(final Sheet objSheet, final int intCurrentRowInxex, final int intCurrentColIndex) {
        String strPreviousPath = null;
        final StringBuffer strPreviousPath_Temp = new StringBuffer();
        Row objRow = null;
        Cell objCell = null;
        boolean isFirstColEncountered = false;
        String strCellValue_Temp = null;
        int intRowCtr = intCurrentRowInxex - 1;
        int intColCtr = intCurrentColIndex - 1;
        while (intRowCtr >= 1 && !isFirstColEncountered) {
            objRow = objSheet.getRow(intRowCtr);
            strCellValue_Temp = null;
            objCell = objRow.getCell(intColCtr);
            System.out.println("intRowCtr--->" + intRowCtr);
            System.out.println("intColCtr--->" + intColCtr);
            if (objCell != null && 1 == objCell.getCellType()) {
                strCellValue_Temp = objCell.getStringCellValue().trim();
            }
            System.out.println("path value-->" + strCellValue_Temp);
            if (strCellValue_Temp != null && !strCellValue_Temp.equalsIgnoreCase("")) {
                strPreviousPath_Temp.insert(0, "/" + strCellValue_Temp);
                if (--intColCtr < 2) {
                    isFirstColEncountered = true;
                }
            }
            --intRowCtr;
        }
        if (isFirstColEncountered) {
            strPreviousPath = strPreviousPath_Temp.toString();
        }
        return strPreviousPath;
    }
    
    public static XMLNode getParentNode(final XMLNode objXMLCurrentNode, final String[] arrPreviousPathElementNames, final int intPositionInPreviousPath) throws Exception {
        XMLNode objParentXMLNode = null;
        if (arrPreviousPathElementNames[intPositionInPreviousPath].equals(objXMLCurrentNode.strElementName)) {
            if (arrPreviousPathElementNames.length == intPositionInPreviousPath + 1) {
                objParentXMLNode = objXMLCurrentNode;
            }
            else {
                final List<XMLNode> lstChildNodes = objXMLCurrentNode.lstChildNodes;
                if (lstChildNodes != null) {
                    boolean blnParentFound = false;
                    for (final XMLNode objXMLNode_Temp : lstChildNodes) {
                        if (objXMLNode_Temp.strElementName.equals(arrPreviousPathElementNames[intPositionInPreviousPath + 1])) {
                            blnParentFound = true;
                            objParentXMLNode = getParentNode(objXMLNode_Temp, arrPreviousPathElementNames, intPositionInPreviousPath + 1);
                            break;
                        }
                    }
                    if (!blnParentFound) {
                        throw new Exception("118 : Parent not found while inserting in tree : Util_XMLConvert.getParentNode(...) : intPositionInPreviousPath : " + intPositionInPreviousPath);
                    }
                }
            }
            return objParentXMLNode;
        }
        throw new Exception("Incorrect path to insert into the tree : Util_XMLConvert.getParentNode(...) : intPositionInPreviousPath : " + intPositionInPreviousPath);
    }
    
    public static List<XMLNode> parseAllNodesAndExecuteQueries(final XMLNode objXMLNodeTree, final Map<String, String> mapParamsFromParent, final Connection conn, final Map<Integer, Connection> connectionsMap) {
        String strChildElementValue_Temp = null;
        List<Map<String, String>> lstRowResults = null;
        final List<XMLNode> lstPopulatedCurrentNodes = new ArrayList<XMLNode>();
        List<XMLNode> lstChildXMLNodes_Temp = null;
        Map<String, String> mapRowResult_Temp = null;
        XMLNode objClonedXMLNode = null;
        XMLNode objClonedChildXMLNode = null;
        List<XMLNode> lstClonedNodes = null;
        List<XMLNode> lstChildNodes = null;
        if (objXMLNodeTree.strSQLQuery != null && !objXMLNodeTree.strSQLQuery.equalsIgnoreCase("")) {
            System.out.println("Found query : " + objXMLNodeTree.strSQLQuery);
            lstRowResults = getRowListFromDB(objXMLNodeTree.strSQLQuery, conn, mapParamsFromParent, connectionsMap);
        }
        if (lstRowResults == null && mapParamsFromParent != null) {
            lstRowResults = new ArrayList<Map<String, String>>();
            lstRowResults.add(mapParamsFromParent);
        }
        if (objXMLNodeTree.strColumnName != null && objXMLNodeTree.strColumnName.length() > 0) {
            if (lstRowResults != null && lstRowResults.size() > 0) {
                for (final Map<String, String> mapRowResult : lstRowResults) {
                    mapRowResult_Temp = new LinkedHashMap<String, String>();
                    mapRowResult_Temp.putAll(mapRowResult);
                    objClonedXMLNode = objXMLNodeTree.clone();
                    strChildElementValue_Temp = mapRowResult_Temp.get(objXMLNodeTree.strColumnName.toUpperCase());
                    if (strChildElementValue_Temp != null) {
                        objClonedXMLNode.strElementValue = strChildElementValue_Temp;
                    }
                    lstPopulatedCurrentNodes.add(objClonedXMLNode);
                }
            }
        }
        else {
            lstClonedNodes = new ArrayList<XMLNode>();
            if (lstRowResults != null && lstRowResults.size() > 0) {
                for (final Map<String, String> mapRowResult : lstRowResults) {
                    mapRowResult_Temp = new LinkedHashMap<String, String>();
                    if (mapRowResult != null) {
                        mapRowResult_Temp.putAll(mapRowResult);
                    }
                    if (mapParamsFromParent != null) {
                        mapRowResult_Temp.putAll(mapParamsFromParent);
                    }
                    objClonedXMLNode = objXMLNodeTree.clone();
                    lstChildNodes = objClonedXMLNode.lstChildNodes;
                    lstChildXMLNodes_Temp = new ArrayList<XMLNode>();
                    for (final XMLNode objChildXMLNode : lstChildNodes) {
                        System.out.println("element name(((((((((((((((-->" + objChildXMLNode.strElementName);
                        objClonedChildXMLNode = objChildXMLNode.clone();
                        lstChildXMLNodes_Temp.addAll(parseAllNodesAndExecuteQueries(objClonedChildXMLNode, mapRowResult_Temp, conn, connectionsMap));
                    }
                    objClonedXMLNode.lstChildNodes = lstChildXMLNodes_Temp;
                    lstClonedNodes.add(objClonedXMLNode);
                }
            }
            lstPopulatedCurrentNodes.addAll(lstClonedNodes);
        }
        return lstPopulatedCurrentNodes;
    }
    
    private static List<Map<String, String>> getRowListFromDB(String strSQLQuery, Connection conn, final Map<String, String> mapParamsFromParent, final Map<Integer, Connection> connectionsMap) {
        List<Map<String, String>> lstRowDataFromDB = null;
        Map<String, String> mapRowData = null;
        List<String> lstDBColumnNames = null;
        PreparedStatement objPreparedStmt = null;
        ResultSet objResultSet = null;
        try {
            try {
                final String[] splitQuery = strSQLQuery.split("===");
                if (splitQuery.length > 1) {
                    strSQLQuery = splitQuery[1];
                    conn = connectionsMap.get(Integer.parseInt(splitQuery[0]));
                    System.out.println("Connection Object form map----******--->" + conn);
                    System.out.println("splitQuery[1]----******--->" + splitQuery[1]);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            strSQLQuery = replaceColumnNamesWithData(strSQLQuery, mapParamsFromParent);
            System.out.println("233 : SQL Query" + strSQLQuery);
            objPreparedStmt = conn.prepareStatement(strSQLQuery);
            objResultSet = objPreparedStmt.executeQuery();
            while (objResultSet.next()) {
                System.out.println("Data Available--------------?");
                lstDBColumnNames = getDBColumnNames(objResultSet);
                if (lstRowDataFromDB == null) {
                    lstRowDataFromDB = new ArrayList<Map<String, String>>();
                }
                mapRowData = convertResultSetRowToMap(objResultSet, lstDBColumnNames);
                lstRowDataFromDB.add(mapRowData);
            }
        }
        catch (SQLException e) {
            System.out.println("157 : Util_XMLConvert.getRowListFromDB(...) : " + e);
            if (objPreparedStmt != null) {
                try {
                    objPreparedStmt.close();
                }
                catch (SQLException e2) {
                    System.out.println("228 : Util_XMLConvert.getRowListFromDB(...) : Error while closing PreparedStatement : SQLException : " + e2);
                }
            }
            if (objResultSet != null) {
                try {
                    objResultSet.close();
                }
                catch (SQLException e2) {
                    System.out.println("235 : Util_XMLConvert.getRowListFromDB(...) : Error while closing ResultSet : SQLException : " + e2);
                }
                return lstRowDataFromDB;
            }
            return lstRowDataFromDB;
        }
        finally {
            if (objPreparedStmt != null) {
                try {
                    objPreparedStmt.close();
                }
                catch (SQLException e2) {
                    System.out.println("228 : Util_XMLConvert.getRowListFromDB(...) : Error while closing PreparedStatement : SQLException : " + e2);
                }
            }
            if (objResultSet != null) {
                try {
                    objResultSet.close();
                }
                catch (SQLException e2) {
                    System.out.println("235 : Util_XMLConvert.getRowListFromDB(...) : Error while closing ResultSet : SQLException : " + e2);
                }
            }
        }
        if (objPreparedStmt != null) {
            try {
                objPreparedStmt.close();
            }
            catch (SQLException e2) {
                System.out.println("228 : Util_XMLConvert.getRowListFromDB(...) : Error while closing PreparedStatement : SQLException : " + e2);
            }
        }
        if (objResultSet != null) {
            try {
                objResultSet.close();
            }
            catch (SQLException e2) {
                System.out.println("235 : Util_XMLConvert.getRowListFromDB(...) : Error while closing ResultSet : SQLException : " + e2);
            }
        }
        return lstRowDataFromDB;
    }
    
    private static String replaceColumnNamesWithData(String strSQLQuery, final Map<String, String> mapParamsFromParent) {
        if (mapParamsFromParent != null) {
            final List<String> lstColumnNames = identifyColumnNamesInQuery(strSQLQuery);
            if (lstColumnNames != null) {
                final StringBuilder strSQLQuery_Builder = new StringBuilder(strSQLQuery);
                final StringBuilder strUpperSQLQuery_Builder = new StringBuilder(strSQLQuery.toUpperCase());
                for (final String strColumnName : lstColumnNames) {
                    int intColIndex = 0;
                    while (true) {
                        final String strUpperColumnName = "{{" + strColumnName.toUpperCase() + "}}";
                        intColIndex = strUpperSQLQuery_Builder.indexOf(strUpperColumnName, intColIndex);
                        if (intColIndex == -1) {
                            break;
                        }
                        strSQLQuery_Builder.replace(intColIndex, intColIndex + strUpperColumnName.length(), strUpperColumnName);
                        ++intColIndex;
                    }
                }
                strSQLQuery = strSQLQuery_Builder.toString();
                for (String strColumnName : lstColumnNames) {
                    strColumnName = strColumnName.toUpperCase();
                    final String strValue = mapParamsFromParent.get(strColumnName);
                    if (strValue != null) {
                        strSQLQuery = strSQLQuery.replace("{{" + strColumnName + "}}", strValue);
                    }
                }
            }
        }
        return strSQLQuery;
    }
    
    private static List<String> identifyColumnNamesInQuery(final String strSQLQuery) {
        final List<String> lstColumnNames = new ArrayList<String>();
        int intStartIndex = 0;
        int intEndIndex = 0;
        intStartIndex = strSQLQuery.indexOf("{{", intStartIndex);
        intEndIndex = strSQLQuery.indexOf("}}", intEndIndex);
        String strTempColName = null;
        while (intStartIndex != -1 && intEndIndex != -1) {
            strTempColName = strSQLQuery.substring(intStartIndex + 2, intEndIndex);
            if (strTempColName != null) {
                lstColumnNames.add(strTempColName);
            }
            intStartIndex = strSQLQuery.indexOf("{{", intStartIndex + 1);
            intEndIndex = strSQLQuery.indexOf("}}", intEndIndex + 1);
        }
        return lstColumnNames;
    }
    
    private static Map<String, String> convertResultSetRowToMap(final ResultSet objResultSet, final List<String> lstDBColumnNames) throws SQLException {
        final Map<String, String> mapRowData = new LinkedHashMap<String, String>();
        for (final String strDBColName : lstDBColumnNames) {
            mapRowData.put(strDBColName, objResultSet.getString(strDBColName));
            System.out.println("Column names--->" + strDBColName + " column value--->" + objResultSet.getString(strDBColName));
        }
        return mapRowData;
    }
    
    private static List<String> getDBColumnNames(final ResultSet objResultSet) throws SQLException {
        final List<String> lstColumnNames = new ArrayList<String>();
        final ResultSetMetaData objResultSetMetaData = objResultSet.getMetaData();
        for (int intColCount = objResultSetMetaData.getColumnCount(), i = 1; i <= intColCount; ++i) {
            lstColumnNames.add(objResultSetMetaData.getColumnName(i));
            System.out.println("Column names--->" + objResultSetMetaData.getColumnName(i));
        }
        return lstColumnNames;
    }
    
    public static Connection getDBConnection(final String dbConnectionString, final String dbUserId, final String dbPwd) throws ClassNotFoundException, SQLException {
        final String driver = "oracle.jdbc.driver.OracleDriver";
        System.out.println("dbConnectionString:" + dbConnectionString);
        System.out.println("dbUserId:" + dbUserId);
        System.out.println("dbPwd:" + dbPwd);
        Class.forName(driver);
        final OracleDataSource ods = new OracleDataSource();
        ods.setURL(dbConnectionString);
        ods.setUser(dbUserId);
        ods.setPassword(dbPwd);
        final Connection conn = ods.getConnection();
        System.out.println("test connection-->" + conn);
        return conn;
    }
    
    public static String convertCustomXMLNodesToXML(final XMLNode objXMLNode) throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        final Document doc = docBuilder.newDocument();
        final Element objElement = convertCustomXMLNodesToElement(doc, objXMLNode);
        doc.appendChild(objElement);
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final DOMSource source = new DOMSource(doc);
        final StreamResult result = new StreamResult(new StringWriter());
        transformer.transform(source, result);
        return result.getWriter().toString();
    }
    
    public static Element convertCustomXMLNodesToElement(final Document doc, final XMLNode objXMLNode) {
        final Element objElement = doc.createElement(objXMLNode.strElementName);
        objElement.appendChild(doc.createTextNode(objXMLNode.strElementValue));
        if (objXMLNode.lstChildNodes != null && objXMLNode.lstChildNodes.size() > 0) {
            for (final XMLNode objChildXMLNode : objXMLNode.lstChildNodes) {
                objElement.appendChild(convertCustomXMLNodesToElement(doc, objChildXMLNode));
            }
        }
        return objElement;
    }
    
    public static Map<String, String> convertExternalParametersToMap(final String strExternalParameters) {
        final Map<String, String> mapExternalParameters = new HashMap<String, String>();
        if (strExternalParameters != null && strExternalParameters.trim().length() > 0) {
            final String[] arrExternalParameters = strExternalParameters.split(";");
            String[] array;
            for (int length = (array = arrExternalParameters).length, i = 0; i < length; ++i) {
                final String strParameterKeyValuePair = array[i];
                final String[] arrParameterKeyValuePair = strParameterKeyValuePair.split(":");
                if (arrParameterKeyValuePair != null && arrParameterKeyValuePair.length > 1) {
                    mapExternalParameters.put(arrParameterKeyValuePair[0].toUpperCase(), arrParameterKeyValuePair[1]);
                }
            }
        }
        return mapExternalParameters;
    }
    
    public static Map<String, String> convertTrimmableElementsToMap(final String strTrimElements, final String strElementsSeparator, final String strValueSeparator) {
        final Map<String, String> mapTrimmableElements = new HashMap<String, String>();
        if (strTrimElements != null && strTrimElements.trim().length() > 0) {
            final String[] arrElements = strTrimElements.split(strElementsSeparator);
            if (arrElements != null && arrElements.length > 0) {
                String[] array;
                for (int length = (array = arrElements).length, i = 0; i < length; ++i) {
                    final String strElementValuePairs = array[i];
                    if (strElementValuePairs != null && strElementValuePairs.length() > 0) {
                        final String[] arrElementValuePairs = strElementValuePairs.split(strValueSeparator);
                        if (arrElementValuePairs != null && arrElementValuePairs.length > 0) {
                            if (arrElementValuePairs[1] != null && arrElementValuePairs[1].equalsIgnoreCase("Between")) {
                                mapTrimmableElements.put(arrElementValuePairs[0], String.valueOf(arrElementValuePairs[1]) + "," + arrElementValuePairs[2] + "," + arrElementValuePairs[3]);
                            }
                            else if (arrElementValuePairs[1] != null && (arrElementValuePairs[1].equals("<") || arrElementValuePairs[1].equals(">") || arrElementValuePairs[1].equals("Value"))) {
                                mapTrimmableElements.put(arrElementValuePairs[0], String.valueOf(arrElementValuePairs[1]) + "," + arrElementValuePairs[2]);
                            }
                            else {
                                mapTrimmableElements.put(arrElementValuePairs[0], arrElementValuePairs[1]);
                            }
                        }
                    }
                }
            }
        }
        return mapTrimmableElements;
    }
    
    public static void trimValueIfApplicable(final NodeList lstChildNodeList, final Map<String, String> mapTrimElements) {
        if (lstChildNodeList != null && lstChildNodeList.getLength() > 0) {
            final int intChildrenLength = lstChildNodeList.getLength();
            Node objChildNode = null;
            for (int intChildCtr = 0; intChildCtr < intChildrenLength; ++intChildCtr) {
                objChildNode = lstChildNodeList.item(intChildCtr);
                final String strCompletePath = getCompletePathForANode(objChildNode);
                if (strCompletePath != null && strCompletePath.length() > 0) {
                    final String strValue = mapTrimElements.get(strCompletePath);
                    if (strValue != null && strValue.length() > 0) {
                        final int intValueLength = Integer.parseInt(strValue);
                        String strTextContent = objChildNode.getTextContent();
                        if (strTextContent.length() >= intValueLength) {
                            strTextContent = strTextContent.substring(0, intValueLength);
                            objChildNode.setTextContent(strTextContent);
                        }
                    }
                }
                trimValueIfApplicable(objChildNode.getChildNodes(), mapTrimElements);
            }
        }
    }
    
    public static String getCompletePathForANode(final Node objNode) {
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
    
    public static Node clone(final Node node1, final Document docMaster) {
        final Element objElement = docMaster.createElement(node1.getNodeName());
        final NodeList lstChildNodes = node1.getChildNodes();
        Node tempNode = null;
        for (int intChildCtr = 0; intChildCtr < lstChildNodes.getLength(); ++intChildCtr) {
            tempNode = lstChildNodes.item(intChildCtr);
            if (tempNode.getNodeType() == 3) {
                objElement.appendChild(docMaster.createTextNode(tempNode.getTextContent()));
            }
            else if (tempNode.getNodeType() == 1) {
                objElement.appendChild(clone(tempNode, docMaster));
            }
        }
        return objElement;
    }
    
    public static List<XMLNode> parseAllNodesAndFillNodeDataFromDatabase(final XMLNode objXMLNodeTree, final Map<String, String> mapParamsFromParent, final Connection conn, final Map<Integer, Object> hashMap) {
        System.out.println("Start : Util_XMLConvert.parseAllNodesAndExecuteQueries(...)");
        String strChildElementValue_Temp = null;
        List<Map<String, String>> lstRowResults = null;
        final List<XMLNode> lstPopulatedCurrentNodes = new ArrayList<XMLNode>();
        List<XMLNode> lstChildXMLNodes_Temp = null;
        Map<String, String> mapRowResult_Temp = null;
        XMLNode objClonedXMLNode = null;
        XMLNode objClonedChildXMLNode = null;
        List<XMLNode> lstClonedNodes = null;
        List<XMLNode> lstChildNodes = null;
        System.out.println("objXMLNodeTree.strSQLQuery--->" + objXMLNodeTree.strSQLQuery);
        if (objXMLNodeTree.strSQLQuery != null) {
            lstRowResults = getRowListFromDBResultSet(objXMLNodeTree.strSQLQuery, hashMap);
            System.out.println("lstRowResults size--->" + lstRowResults.size());
        }
        if (lstRowResults == null && mapParamsFromParent != null) {
            lstRowResults = new ArrayList<Map<String, String>>();
            lstRowResults.add(mapParamsFromParent);
        }
        System.out.println("objXMLNodeTree.strColumnName--->" + objXMLNodeTree.strColumnName);
        System.out.println("objXMLNodeTree.Element--->" + objXMLNodeTree.strElementName);
        if (objXMLNodeTree.strColumnName != null && objXMLNodeTree.strColumnName.length() > 0) {
            if (lstRowResults != null && lstRowResults.size() > 0) {
                for (final Map<String, String> mapRowResult : lstRowResults) {
                    mapRowResult_Temp = new LinkedHashMap<String, String>();
                    mapRowResult_Temp.putAll(mapRowResult);
                    if (mapParamsFromParent != null) {
                        mapRowResult_Temp.putAll(mapParamsFromParent);
                    }
                    objClonedXMLNode = objXMLNodeTree.clone();
                    strChildElementValue_Temp = mapRowResult_Temp.get(objXMLNodeTree.strColumnName);
                    if (strChildElementValue_Temp != null) {
                        objClonedXMLNode.strElementValue = strChildElementValue_Temp;
                    }
                    lstPopulatedCurrentNodes.add(objClonedXMLNode);
                }
            }
        }
        else {
            lstClonedNodes = new ArrayList<XMLNode>();
            System.out.println("lstRowResults*********" + lstRowResults);
            if (lstRowResults != null && lstRowResults.size() > 0) {
                for (final Map<String, String> mapRowResult : lstRowResults) {
                    mapRowResult_Temp = new LinkedHashMap<String, String>();
                    if (mapRowResult != null) {
                        mapRowResult_Temp.putAll(mapRowResult);
                    }
                    if (mapParamsFromParent != null) {
                        mapRowResult_Temp.putAll(mapParamsFromParent);
                    }
                    objClonedXMLNode = objXMLNodeTree.clone();
                    lstChildNodes = objClonedXMLNode.lstChildNodes;
                    lstChildXMLNodes_Temp = new ArrayList<XMLNode>();
                    for (final XMLNode objChildXMLNode : lstChildNodes) {
                        objClonedChildXMLNode = objChildXMLNode.clone();
                        lstChildXMLNodes_Temp.addAll(parseAllNodesAndFillNodeDataFromDatabase(objClonedChildXMLNode, mapRowResult_Temp, conn, hashMap));
                    }
                    objClonedXMLNode.lstChildNodes = lstChildXMLNodes_Temp;
                    lstClonedNodes.add(objClonedXMLNode);
                }
            }
            lstPopulatedCurrentNodes.addAll(lstClonedNodes);
        }
        System.out.println("End : Util_XMLConvert.parseAllNodesAndExecuteQueries(...)");
        return lstPopulatedCurrentNodes;
    }
    
    private static List<Map<String, String>> getRowListFromDBResultSet(String outParamPosition, final Map<Integer, Object> hashMap) {
        List<Map<String, String>> lstRowDataFromDB = null;
        Map<String, String> mapRowData = null;
        List<String> lstDBColumnNames = null;
        final PreparedStatement objPreparedStmt = null;
        OracleResultSet objResultSet = null;
        String strResultValue = null;
        String dbColumn = "";
        try {
            System.out.println("outParamPosition &&&&&&&&&&--->" + outParamPosition);
            final String[] outParamPositions = outParamPosition.split(":");
            if (outParamPositions.length == 2) {
                outParamPosition = outParamPositions[0];
                dbColumn = outParamPositions[1];
            }
            final Integer intValue = (int)Double.parseDouble(outParamPosition);
            final Object obj = hashMap.get(intValue);
            System.out.println("obj &444444444444&&&&&&&&&--->" + obj);
            if (obj instanceof ResultSet) {
                System.out.println("obj &44444444455555555555444&&&&&&&&&--->" + obj);
                objResultSet = (OracleResultSet)obj;
                while (objResultSet.next()) {
                    lstDBColumnNames = getDBColumnNames((ResultSet)objResultSet);
                    if (lstRowDataFromDB == null) {
                        lstRowDataFromDB = new ArrayList<Map<String, String>>();
                    }
                    mapRowData = convertResultSetRowToMap((ResultSet)objResultSet, lstDBColumnNames);
                    lstRowDataFromDB.add(mapRowData);
                }
            }
            else {
                strResultValue = obj.toString();
                mapRowData = new HashMap<String, String>();
                mapRowData.put(dbColumn, strResultValue);
                System.out.println("Status Date-------*********----->" + dbColumn + " :strResultValue   " + strResultValue);
                lstRowDataFromDB = new ArrayList<Map<String, String>>();
                lstRowDataFromDB.add(mapRowData);
            }
        }
        catch (SQLException e) {
            System.out.println("157 : Util_XMLConvert.getRowListFromDB(...) : " + e);
            if (objPreparedStmt != null) {
                try {
                    objPreparedStmt.close();
                }
                catch (SQLException e2) {
                    System.out.println("228 : Util_XMLConvert.getRowListFromDB(...) : Error while closing PreparedStatement : SQLException : " + e2);
                }
            }
            if (objResultSet != null) {
                try {
                    objResultSet.close();
                }
                catch (SQLException e2) {
                    System.out.println("235 : Util_XMLConvert.getRowListFromDB(...) : Error while closing ResultSet : SQLException : " + e2);
                }
                return lstRowDataFromDB;
            }
            return lstRowDataFromDB;
        }
        finally {
            if (objPreparedStmt != null) {
                try {
                    objPreparedStmt.close();
                }
                catch (SQLException e2) {
                    System.out.println("228 : Util_XMLConvert.getRowListFromDB(...) : Error while closing PreparedStatement : SQLException : " + e2);
                }
            }
            if (objResultSet != null) {
                try {
                    objResultSet.close();
                }
                catch (SQLException e2) {
                    System.out.println("235 : Util_XMLConvert.getRowListFromDB(...) : Error while closing ResultSet : SQLException : " + e2);
                }
            }
        }
        if (objPreparedStmt != null) {
            try {
                objPreparedStmt.close();
            }
            catch (SQLException e2) {
                System.out.println("228 : Util_XMLConvert.getRowListFromDB(...) : Error while closing PreparedStatement : SQLException : " + e2);
            }
        }
        if (objResultSet != null) {
            try {
                objResultSet.close();
            }
            catch (SQLException e2) {
                System.out.println("235 : Util_XMLConvert.getRowListFromDB(...) : Error while closing ResultSet : SQLException : " + e2);
            }
        }
        return lstRowDataFromDB;
    }
    
    private static List<Map<String, String>> getRowListFromHTML(final String strSQLQuery, final Map<String, String> mapParamsFromParent, final String strHtmlFilePath) {
        List<Map<String, String>> lstRowDataFromDB = new ArrayList<Map<String, String>>();
        lstRowDataFromDB = HTMLUtility.getRecordsFromTable(strHtmlFilePath, strSQLQuery.substring(0, strSQLQuery.indexOf(",")), strSQLQuery.substring(strSQLQuery.indexOf(",") + 1, strSQLQuery.length()), mapParamsFromParent);
        return lstRowDataFromDB;
    }
    
    public static List<XMLNode> parseAllNodesAndFromHtmlData(final XMLNode objXMLNodeTree, final Map<String, String> mapParamsFromParent, final Map<String, String> linkedHashMap, final String strhtmlFilePath) {
        String strChildElementValue_Temp = null;
        List<Map<String, String>> lstRowResults = null;
        final List<XMLNode> lstPopulatedCurrentNodes = new ArrayList<XMLNode>();
        List<XMLNode> lstChildXMLNodes_Temp = null;
        Map<String, String> mapRowResult_Temp = null;
        XMLNode objClonedXMLNode = null;
        XMLNode objClonedChildXMLNode = null;
        List<XMLNode> lstClonedNodes = null;
        List<XMLNode> lstChildNodes = null;
        if (objXMLNodeTree.strSQLQuery != null && !objXMLNodeTree.strSQLQuery.equalsIgnoreCase("")) {
            System.out.println("Found query : " + objXMLNodeTree.strSQLQuery);
            lstRowResults = getRowListFromHTML(objXMLNodeTree.strSQLQuery, mapParamsFromParent, strhtmlFilePath);
            System.out.println(lstRowResults.size());
        }
        if (lstRowResults == null && mapParamsFromParent != null) {
            lstRowResults = new ArrayList<Map<String, String>>();
            lstRowResults.add(mapParamsFromParent);
        }
        if (objXMLNodeTree.strColumnName != null && objXMLNodeTree.strColumnName.length() > 0) {
            if (lstRowResults != null && lstRowResults.size() > 0) {
                for (final Map<String, String> mapRowResult : lstRowResults) {
                    mapRowResult_Temp = new LinkedHashMap<String, String>();
                    mapRowResult_Temp.putAll(mapRowResult);
                    if (mapParamsFromParent != null) {
                        mapRowResult_Temp.putAll(mapParamsFromParent);
                    }
                    objClonedXMLNode = objXMLNodeTree.clone();
                    System.out.println("objXMLNodeTree.strColumnName.toUpperCase()------------->" + objXMLNodeTree.strColumnName);
                    strChildElementValue_Temp = mapRowResult_Temp.get(objXMLNodeTree.strColumnName);
                    System.out.println("strChildElementValue_Temp------------->" + strChildElementValue_Temp);
                    if (strChildElementValue_Temp == null || strChildElementValue_Temp.equalsIgnoreCase("")) {
                        strChildElementValue_Temp = linkedHashMap.get(objXMLNodeTree.strColumnName);
                    }
                    strChildElementValue_Temp = HTMLUtility.getTransmissionData(mapParamsFromParent, strChildElementValue_Temp);
                    System.out.println("strChildElementValue_Temp------------->" + strChildElementValue_Temp);
                    System.out.println("linkedHashMap size------------->" + linkedHashMap.size());
                    if (strChildElementValue_Temp != null) {
                        objClonedXMLNode.strElementValue = strChildElementValue_Temp;
                    }
                    lstPopulatedCurrentNodes.add(objClonedXMLNode);
                }
            }
        }
        else {
            lstClonedNodes = new ArrayList<XMLNode>();
            if (lstRowResults != null && lstRowResults.size() > 0) {
                for (final Map<String, String> mapRowResult : lstRowResults) {
                    mapRowResult_Temp = new LinkedHashMap<String, String>();
                    if (mapRowResult != null) {
                        mapRowResult_Temp.putAll(mapRowResult);
                    }
                    if (mapParamsFromParent != null) {
                        mapRowResult_Temp.putAll(mapParamsFromParent);
                    }
                    objClonedXMLNode = objXMLNodeTree.clone();
                    lstChildNodes = objClonedXMLNode.lstChildNodes;
                    lstChildXMLNodes_Temp = new ArrayList<XMLNode>();
                    for (final XMLNode objChildXMLNode : lstChildNodes) {
                        System.out.println("element name(((((((((((((((-->" + objChildXMLNode.strElementName);
                        objClonedChildXMLNode = objChildXMLNode.clone();
                        lstChildXMLNodes_Temp.addAll(parseAllNodesAndFromHtmlData(objClonedChildXMLNode, mapRowResult_Temp, linkedHashMap, strhtmlFilePath));
                    }
                    objClonedXMLNode.lstChildNodes = lstChildXMLNodes_Temp;
                    lstClonedNodes.add(objClonedXMLNode);
                }
            }
            lstPopulatedCurrentNodes.addAll(lstClonedNodes);
        }
        return lstPopulatedCurrentNodes;
    }
    
    public static Map<String, String> convertExternalParametersDataToMap(final String strExternalParameters) {
        final Map<String, String> mapExternalParameters = new HashMap<String, String>();
        if (strExternalParameters != null && strExternalParameters.trim().length() > 0) {
            final String[] arrExternalParameters = strExternalParameters.split(";");
            String[] array;
            for (int length = (array = arrExternalParameters).length, i = 0; i < length; ++i) {
                final String strParameterKeyValuePair = array[i];
                final String[] arrParameterKeyValuePair = strParameterKeyValuePair.split(":");
                if (arrParameterKeyValuePair != null && arrParameterKeyValuePair.length > 1) {
                    mapExternalParameters.put(arrParameterKeyValuePair[0], arrParameterKeyValuePair[1]);
                }
            }
        }
        return mapExternalParameters;
    }
}
