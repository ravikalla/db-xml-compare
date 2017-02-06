package in.ravikalla.dbXmlCompare.xmlCompareUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import in.ravikalla.dbXmlCompare.xmlCompareUtil.dto.XMLDataConverterResultDTO;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.CommonUtil;

public class CompareXMLAndDBData {
    public static void main(final String[] args) {
        final String strXMLFileName = "C:\\Users\\kalla\\Desktop\\temp\\Response_1.xml";
        final String strMapDocFileName = "C:\\Users\\kalla\\Desktop\\temp\\Mapping_1.xls";
        final String strSQLQuery = "Select DUEDATE, EVALUATIONFACEAMT, FOLLOWUPCOUNT, FORMNO, FULFILLEDDATE, HISTORYDURATION, RECEIVEDDATE, REQUESTEDDATE, REQUIREMENTINFOID, SEQUENCE, STATUSDATE from TCLIFE_ODS_N_ST2.VW_ACORD_UNDRWRTNG_REQUIRMNT where CASENUMBER = 'C027791'";
        final String userName = "<U_ID>";
        final String password = "<PWD>";
        final String strConnectionString = "jdbc:oracle:thin:@//<IP>:<PORT>/<DB>";
        final String strComparisonResultsFile = "C:\\Users\\kalla\\Desktop\\temp\\Results_1.xls";
        final String strXMLOPFileName = "C:\\Users\\kalla\\Desktop\\temp\\XML_OP_1.xls";
        final String strSQLQueryOPFileName = "C:\\Users\\kalla\\Desktop\\temp\\SQL_OP_1.xls";
        String xmlStr = null;
        try {
            xmlStr = CommonUtil.readDataFromFile(strXMLFileName);
        }
        catch (IOException e) {
            System.out.println("46 : IOException : " + e);
        }
        final boolean blnCompareResult = compareExcelAndQuery_WriteResults(strComparisonResultsFile, xmlStr, strMapDocFileName, strSQLQuery, userName, password, strConnectionString, strXMLOPFileName, strSQLQueryOPFileName, "REQUIREMENTINFOID", "RequirementInfoUniqueID");
        System.out.println("Compare Result : " + blnCompareResult);
    }
    
    public static boolean compareExcelAndQuery_WriteResults(final String strComparisonResultsFile, final String xmlStr, final String strMapDocFileName, final String strSQLQuery, final String userName, final String password, final String strConnectionString, final String strXMLOPFileName, final String strSQLOPFileName, final String strDBColName, final String strXMLElementName) {
        printParameters(strComparisonResultsFile, xmlStr, strMapDocFileName, strSQLQuery, userName, password, strConnectionString, strXMLOPFileName, strSQLOPFileName);
        final XMLDataConverterResultDTO objXMLDataConverterResultDTO = XMLDataConverter.convertDataFromXML(xmlStr, strMapDocFileName);
        if (strXMLOPFileName != null && !strXMLOPFileName.equals("") && objXMLDataConverterResultDTO != null) {
            XMLDataConverter.writeFileToDisk(objXMLDataConverterResultDTO.mapResponseXMLData, strXMLOPFileName);
        }
        final Map<String, List<String>> mapStoredProcData = SQLQuery.getSQLQueryData(strSQLQuery, strConnectionString, userName, password, objXMLDataConverterResultDTO.mapElementToDB);
        if (strSQLOPFileName != null && !strSQLOPFileName.equals("")) {
            StoredProc.writeFileToDisk(mapStoredProcData, strSQLOPFileName);
        }
        final Map<String, List<String>> mapComparisonResults = compareMappingDocuments(objXMLDataConverterResultDTO.mapResponseXMLData, mapStoredProcData, strDBColName, strXMLElementName);
        boolean blnDifferencesExist = false;
        if (mapComparisonResults != null) {
            printComparisonResultsToFile(strComparisonResultsFile, mapComparisonResults);
            blnDifferencesExist = true;
            System.out.println("Data doesn't match");
        }
        return !blnDifferencesExist;
    }
    
    public static boolean compareAndWriteResults(final String strComparisonResultsFile, final String xmlStr, final String strMapDocFileName, final String strStoredProc, final String strDatatypesList, final String userName, final String password, final String strConnectionString, final String strXMLOPFileName, final String strSPOPFileName, final String strDBColName, final String strXMLElementName) {
        final XMLDataConverterResultDTO objXMLDataConverterResultDTO = XMLDataConverter.convertDataFromXML(xmlStr, strMapDocFileName);
        if (strXMLOPFileName != null && !strXMLOPFileName.equals("")) {
            XMLDataConverter.writeFileToDisk(objXMLDataConverterResultDTO.mapResponseXMLData, strXMLOPFileName);
        }
        final Map<String, List<String>> mapStoredProcData = StoredProc.getStoredProcData(strStoredProc, strDatatypesList, strConnectionString, userName, password, objXMLDataConverterResultDTO.mapElementToDB);
        if (strSPOPFileName != null && !strSPOPFileName.equals("")) {
            StoredProc.writeFileToDisk(mapStoredProcData, strSPOPFileName);
        }
        final Map<String, List<String>> mapComparisonResults = compareMappingDocuments(objXMLDataConverterResultDTO.mapResponseXMLData, mapStoredProcData, strDBColName, strXMLElementName);
        boolean blnDifferencesExist = false;
        if (mapComparisonResults != null) {
            printComparisonResultsToFile(strComparisonResultsFile, mapComparisonResults);
            blnDifferencesExist = true;
            System.out.println("Data doesn't match");
        }
        return blnDifferencesExist;
    }
    
    private static void printComparisonResultsToFile(final String strComparisonResultsFile, final Map<String, List<String>> mapComparisonResults) {
        final HSSFWorkbook workbook = new HSSFWorkbook();
        try {
            for (final Map.Entry<String, List<String>> entry : mapComparisonResults.entrySet()) {
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
            final FileOutputStream fileOut = new FileOutputStream(strComparisonResultsFile);
            workbook.write((OutputStream)fileOut);
            fileOut.close();
            System.out.println("Your results file has been generated!");
        }
        catch (Exception ex) {
            System.out.println("96 : " + ex);
        }
    }
    
    private static Map<String, List<String>> compareMappingDocuments(final Map<String, List<String>> mapResponseXMLData, final Map<String, List<String>> mapStoredProcData, final String strDBColName, final String strXMLElementName) {
        final Map<String, List<String>> mapComparisonResults = new LinkedHashMap<String, List<String>>();
        List<String> lstTempComparisonPassResults = null;
        List<String> lstTempComparisonFailResults = null;
        for (final Map.Entry<String, List<String>> entryResponseXMLData : mapResponseXMLData.entrySet()) {
            lstTempComparisonPassResults = new ArrayList<String>();
            lstTempComparisonFailResults = new ArrayList<String>();
            final List<String> lstXMLData = entryResponseXMLData.getValue();
            final List<String> lstDBData = mapStoredProcData.get(entryResponseXMLData.getKey());
            if (lstXMLData == null || lstXMLData.size() == 0) {
                lstTempComparisonFailResults.add("No data in XML for : " + entryResponseXMLData.getKey());
                mapComparisonResults.put(String.valueOf(entryResponseXMLData.getKey()) + "_Fail", lstTempComparisonFailResults);
            }
            else if (lstDBData == null || lstDBData.size() == 0) {
                lstTempComparisonFailResults.add("No data in DB for : " + entryResponseXMLData.getKey());
                mapComparisonResults.put(String.valueOf(entryResponseXMLData.getKey()) + "_Fail", lstTempComparisonFailResults);
            }
            else {
                lstTempComparisonPassResults = compareAndGetList(lstXMLData, lstDBData, strDBColName, strXMLElementName, "CHECK_MATCHING_DATA");
                lstTempComparisonFailResults = compareAndGetList(lstXMLData, lstDBData, strDBColName, strXMLElementName, "CHECK_MISMATCHING_DATA");
                mapComparisonResults.put(String.valueOf(entryResponseXMLData.getKey()) + "_Match", lstTempComparisonPassResults);
                mapComparisonResults.put(String.valueOf(entryResponseXMLData.getKey()) + "_Mismatch", lstTempComparisonFailResults);
            }
        }
        return mapComparisonResults;
    }
    
    private static List<String> compareAndGetList(final List<String> lstXMLData, final List<String> lstDBData) {
        final List<String> lstTempComparisonResults = new ArrayList<String>();
        final String strHeader = lstXMLData.get(0);
        lstTempComparisonResults.add("ComparisonResult|" + strHeader);
        for (final String strXMLData : lstXMLData) {
            int intMatchCount = 0;
            for (final String strDBData : lstDBData) {
                if (strDBData.equals(strXMLData)) {
                    ++intMatchCount;
                }
            }
            if (intMatchCount == 0) {
                lstTempComparisonResults.add("Match not found in DB|" + strXMLData);
            }
            if (intMatchCount > 1 && intMatchCount != getEntryCountOfStringInList(strXMLData, lstXMLData)) {
                lstTempComparisonResults.add("Incorrect number of entries(" + intMatchCount + ") in DB|" + strXMLData);
            }
        }
        for (final String strDBData2 : lstDBData) {
            int intMatchCount = 0;
            for (final String strXMLData2 : lstXMLData) {
                if (strXMLData2.equals(strDBData2)) {
                    ++intMatchCount;
                }
            }
            if (intMatchCount == 0) {
                lstTempComparisonResults.add("Match not found in XML|" + strDBData2);
            }
            if (intMatchCount > 1 && intMatchCount != getEntryCountOfStringInList(strDBData2, lstDBData)) {
                lstTempComparisonResults.add("Incorrect number of entries(" + intMatchCount + ") in XML|" + intMatchCount);
            }
        }
        return lstTempComparisonResults;
    }
    
    private static List<String> compareAndGetList(final List<String> lstXMLData, final List<String> lstDBData, final String strDBColName, final String strXMLElementName, final String strComparisonType) {
        final List<String> lstXMLData_Temp = new ArrayList<String>();
        final List<String> lstDBData_Temp = new ArrayList<String>();
        lstXMLData_Temp.addAll(lstXMLData);
        lstDBData_Temp.addAll(lstDBData);
        final List<String> compareList = new ArrayList<String>();
        final String strHeader = lstXMLData_Temp.get(0);
        compareList.add("ColumnName|WS Value|DB Value|ComparisonResult");
        final String[] arrHeaderCols = strHeader.split("\\|");
        int intXMLPrimaryKeyIndex = -1;
        for (int intHeaderColCtr = 0; intHeaderColCtr < arrHeaderCols.length; ++intHeaderColCtr) {
            if (arrHeaderCols[intHeaderColCtr].equals(strXMLElementName)) {
                intXMLPrimaryKeyIndex = intHeaderColCtr;
            }
        }
        for (int intXMLRowCtr = 1; intXMLRowCtr < lstXMLData_Temp.size(); ++intXMLRowCtr) {
            final String strXMLData = lstXMLData_Temp.get(intXMLRowCtr);
            int intMatchCount = 0;
            final String[] arrXMLData = strXMLData.split("\\|");
            for (int intDBRowCtr = 1; intDBRowCtr < lstDBData_Temp.size(); ++intDBRowCtr) {
                final String[] arrDBData = lstDBData_Temp.get(intDBRowCtr).split("\\|");
                final List<String> lstComparedColumnsOfRow = CommonUtil.getComparedColumnsInRow(arrDBData, arrXMLData, arrHeaderCols, intXMLPrimaryKeyIndex);
                if (lstComparedColumnsOfRow != null && lstComparedColumnsOfRow.size() > 0) {
                    ++intMatchCount;
                    if (CommonUtil.isMatchedRow(lstComparedColumnsOfRow)) {
                        if ("CHECK_MATCHING_DATA".equals(strComparisonType)) {
                            compareList.addAll(lstComparedColumnsOfRow);
                        }
                    }
                    else if ("CHECK_MISMATCHING_DATA".equals(strComparisonType)) {
                        compareList.addAll(lstComparedColumnsOfRow);
                    }
                    lstDBData_Temp.remove(intDBRowCtr--);
                    lstXMLData_Temp.remove(intXMLRowCtr--);
                    System.out.println("245 : " + lstXMLData_Temp.size() + " : " + lstDBData_Temp.size() + " : " + intDBRowCtr + " : " + lstDBData_Temp.get(intDBRowCtr));
                    break;
                }
            }
            if (intMatchCount == 0 && "CHECK_MISMATCHING_DATA".equals(strComparisonType)) {
                compareList.addAll(CommonUtil.convertedListOfXMLRowToCols(arrHeaderCols, strXMLData));
            }
        }
        System.out.println("254 : " + lstXMLData_Temp.size() + " : " + lstDBData_Temp.size());
        for (int intDBRowCtr2 = 1; intDBRowCtr2 < lstDBData_Temp.size(); ++intDBRowCtr2) {
            System.out.println("256 : " + lstXMLData_Temp.size() + " : " + lstDBData_Temp.size());
            final String strDBData = lstDBData_Temp.get(intDBRowCtr2);
            int intMatchCount = 0;
            final String[] arrDBData2 = strDBData.split("\\|");
            for (int intXMLRowCtr2 = 1; intXMLRowCtr2 < lstXMLData_Temp.size(); ++intXMLRowCtr2) {
                final String[] arrXMLData2 = lstXMLData_Temp.get(intXMLRowCtr2).split("\\|");
                final List<String> lstComparedColumnsOfRow = CommonUtil.getComparedColumnsInRow(arrDBData2, arrXMLData2, arrHeaderCols, intXMLPrimaryKeyIndex);
                if (lstComparedColumnsOfRow != null && lstComparedColumnsOfRow.size() > 0) {
                    ++intMatchCount;
                    if (CommonUtil.isMatchedRow(lstComparedColumnsOfRow)) {
                        if ("CHECK_MATCHING_DATA".equals(strComparisonType)) {
                            compareList.addAll(lstComparedColumnsOfRow);
                        }
                    }
                    else if ("CHECK_MISMATCHING_DATA".equals(strComparisonType)) {
                        compareList.addAll(lstComparedColumnsOfRow);
                    }
                    lstDBData_Temp.remove(intDBRowCtr2--);
                    lstXMLData_Temp.remove(intXMLRowCtr2--);
                    break;
                }
            }
            if (intMatchCount == 0 && "CHECK_MISMATCHING_DATA".equals(strComparisonType)) {
                compareList.addAll(CommonUtil.convertedListOfDBRowToCols(arrHeaderCols, strDBData));
            }
        }
        return compareList;
    }
    
    private static int getEntryCountOfStringInList(final String strXMLData, final List<String> lstXMLData) {
        int intEntryCountOfStringInList = 0;
        for (final String strTempXMLData : lstXMLData) {
            if (strTempXMLData.equals(strXMLData)) {
                ++intEntryCountOfStringInList;
            }
        }
        return intEntryCountOfStringInList;
    }
    
    public static void printParameters(final String strComparisonResultsFile, final String xmlStr, final String strMapDocFileName, final String strStoredProc, final String userName, final String password, final String strConnectionString, final String strXMLOPFileName, final String strSPOPFileName) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(String.valueOf(strComparisonResultsFile) + "1");
            out.println("strComparisonResultsFile : " + strComparisonResultsFile + "\n" + "xmlStr : " + xmlStr + "\n" + "strMapDocFileName : " + strMapDocFileName + "\n" + "strStoredProc : " + strStoredProc + "\n" + "userName : " + userName + "\n" + "password : " + password + "\n" + "strConnectionString : " + strConnectionString + "\n" + "strXMLOPFileName : " + strXMLOPFileName + "\n" + "strSPOPFileName: " + strSPOPFileName);
        }
        catch (FileNotFoundException e) {
            System.out.println("58 : FileNotFoundException : " + e);
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
}
