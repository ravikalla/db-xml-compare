package in.ravikalla.dbXmlCompare.xmlCompareUtil;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.pool.OracleDataSource;

public class StoredProc
{
    public static Map<Integer, Object> hashMap;
    
    static {
        StoredProc.hashMap = new HashMap<Integer, Object>();
    }
    
    public static void main(final String[] args) {
        String strStoredProc = null;
        final String userName = "<U_NAME>";
        final String password = "<PWD";
        final String strConnectionString = "jdbc:oracle:thin:@//<IP>:<PORT>/<DB>";
        strStoredProc = "<SchemaName>.<Package>.<ProcName>('Param1','Param2',?,?,?,?,?)";
    }
    
    public static void saveStoredProcDataToFile(final String strStoredProc, final String strDatatypesList, final String strOPFileName, final String strConnectionString, final String userName, final String password, final Map<String, Map<String, String>> mapElementToDB) {
        final Map<String, List<String>> mapStoredProcData = getStoredProcData(strStoredProc, strDatatypesList, strConnectionString, userName, password, mapElementToDB);
        if (strOPFileName != null && !strOPFileName.equals("")) {
            writeFileToDisk(mapStoredProcData, strOPFileName);
        }
    }
    
    public static Map<String, List<String>> getStoredProcData(final String strStoredProc, final String strDatatypesList, final String strConnectionString, final String userName, final String password, final Map<String, Map<String, String>> mapElementToDB) {
        final Map<String, List<String>> mapCursorNamesData = new HashMap<String, List<String>>();
        List<String> lstCursorData = null;
        final String driver = "oracle.jdbc.driver.OracleDriver";
        Connection conn = null;
        OracleCallableStatement oraCallableStmt = null;
        OracleResultSet deptResultSet = null;
        try {
            Class.forName(driver);
            final OracleDataSource ods = new OracleDataSource();
            ods.setURL(strConnectionString);
            ods.setUser(userName);
            ods.setPassword(password);
            conn = ods.getConnection();
            final String query = "call " + strStoredProc;
            oraCallableStmt = (OracleCallableStatement)conn.prepareCall(query);
            final int intNumberOfCursorParameters = getNumberOfCursorParameters(query, "?");
            final String[] arrDataTypes = strDatatypesList.split(",");
            if (arrDataTypes.length != intNumberOfCursorParameters) {
                throw new Exception("65 : Error in input parameters : StoredProc parameters count doesnt match the Datatypes list");
            }
            for (int i = 0; i < arrDataTypes.length; ++i) {
                if (arrDataTypes[i].equals("CURSOR")) {
                    oraCallableStmt.registerOutParameter(i + 1, -10);
                }
                else if (arrDataTypes[i].equals("VARCHAR") || arrDataTypes[i].equals("STRING")) {
                    oraCallableStmt.registerOutParameter(i + 1, 12);
                }
                else if (arrDataTypes[i].equals("INTEGER") || arrDataTypes[i].equals("NUMBER")) {
                    oraCallableStmt.registerOutParameter(i + 1, 4);
                }
            }
            oraCallableStmt.executeQuery();
            for (final Map.Entry<String, Map<String, String>> entry : mapElementToDB.entrySet()) {
                final int intCursorPosition = Integer.parseInt(entry.getKey());
                final List<String> lstColNames = getColumnNamesToBeConsidered(entry.getValue());
                deptResultSet = (OracleResultSet)oraCallableStmt.getCursor(intCursorPosition);
                final StringBuffer strRowData = new StringBuffer();
                final Map<String, String> mapDBCol = mapElementToDB.get(entry.getKey());
                for (final Map.Entry<String, String> entryDBCol : mapDBCol.entrySet()) {
                    if (strRowData.length() != 0) {
                        strRowData.append("|");
                    }
                    strRowData.append(entryDBCol.getKey());
                }
                lstCursorData = new ArrayList<String>();
                lstCursorData.add(strRowData.toString());
                while (deptResultSet.next()) {
                    final StringBuffer strTemp = new StringBuffer();
                    for (int j = 0; lstColNames != null && j < lstColNames.size(); ++j) {
                        if (j != 0) {
                            strTemp.append("|");
                        }
                        String strTempData = deptResultSet.getString((String)lstColNames.get(j));
                        if (strTempData == null) {
                            strTempData = "";
                        }
                        strTemp.append(strTempData);
                    }
                    if (3 == intCursorPosition) {
                        System.out.println("110 : " + (Object)strTemp);
                    }
                    lstCursorData.add(strTemp.toString());
                }
                mapCursorNamesData.put(Integer.toString(intCursorPosition), lstCursorData);
            }
        }
        catch (Exception e) {
            System.out.println("109 : Exception e : " + e);
            if (conn != null) {
                try {
                    conn.close();
                }
                catch (SQLException e2) {
                    System.out.println("SQLException e : " + e2);
                }
                return mapCursorNamesData;
            }
            return mapCursorNamesData;
        }
        finally {
            if (conn != null) {
                try {
                    conn.close();
                }
                catch (SQLException e2) {
                    System.out.println("SQLException e : " + e2);
                }
            }
        }
        if (conn != null) {
            try {
                conn.close();
            }
            catch (SQLException e2) {
                System.out.println("SQLException e : " + e2);
            }
        }
        return mapCursorNamesData;
    }
    
    private static List<String> getColumnNamesToBeConsidered(final Map<String, String> value) {
        List<String> lstColumnNames = null;
        if (value != null) {
            for (final Map.Entry<String, String> entry : value.entrySet()) {
                if (lstColumnNames == null) {
                    lstColumnNames = new ArrayList<String>();
                }
                lstColumnNames.add(entry.getValue());
            }
        }
        return lstColumnNames;
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
    
    private static int getNumberOfCursorParameters(final String query, final String searchFor) {
        final int len = searchFor.length();
        int result = 0;
        if (len > 0) {
            for (int start = query.indexOf(searchFor); start != -1; start = query.indexOf(searchFor, start + len)) {
                ++result;
            }
        }
        return result;
    }
    
    public static Map<Integer, Object> getStoredProcData(final String strStoredProc, final String strDatatypesList, final String inParametersPositions, final String outParameterPositions, final String strConnectionString, final String userName, final String password) {
        final String driver = "oracle.jdbc.driver.OracleDriver";
        Connection conn = null;
        OracleCallableStatement oraCallableStmt = null;
        try {
            Class.forName(driver);
            final OracleDataSource ods = new OracleDataSource();
            ods.setURL(strConnectionString);
            ods.setUser(userName);
            ods.setPassword(password);
            conn = ods.getConnection();
            System.out.println("oracle database version--->" + conn.getMetaData().getDatabaseMajorVersion() + "." + conn.getMetaData().getDatabaseMinorVersion());
            final String query = "call " + strStoredProc;
            oraCallableStmt = (OracleCallableStatement)conn.prepareCall(query);
            final int intNumberOfCursorParameters = getNumberOfCursorParameters(query, "?");
            final String[] arrDataTypes = strDatatypesList.split(",");
            System.out.println("intNumberOfCursorParameters-->" + intNumberOfCursorParameters);
            System.out.println("arrDataTypes length-->" + arrDataTypes.length);
            registerOutParameters(strDatatypesList, inParametersPositions, outParameterPositions, strStoredProc, conn);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return StoredProc.hashMap;
    }
    
    public static void registerOutParameters(final String strDatatypesList, final String inParametersPositions, final String outParameterPositions, final String strStoredProc, final Connection conn) throws Exception {
        OracleCallableStatement oraCallableStmt = null;
        String[] arrDataTypes = strDatatypesList.split(",");
        final String query = "call " + strStoredProc;
        oraCallableStmt = (OracleCallableStatement)conn.prepareCall(query);
        final int intNumberOfCursorParameters = oraCallableStmt.getParameterMetaData().getParameterCount();
        final String[] outParameterPositionsArray = outParameterPositions.split(",");
        final String[] inParametersPositionsArray = inParametersPositions.split(",");
        for (int inparam = 0; inparam < inParametersPositionsArray.length; ++inparam) {
            final String[] inParamMetadata = inParametersPositionsArray[inparam].split(":");
            System.out.println("in position: " + inParamMetadata[0] + "inpostion value :" + inParamMetadata[1]);
            oraCallableStmt.setString(Integer.parseInt(inParamMetadata[0]), inParamMetadata[1]);
        }
        for (int i = 0; i < arrDataTypes.length; ++i) {
            if (arrDataTypes[i].equals("CURSOR")) {
                oraCallableStmt.registerOutParameter(Integer.parseInt(outParameterPositionsArray[i]), -10);
                System.out.println(String.valueOf(i) + "   out position--->" + outParameterPositionsArray[i]);
            }
            else if (arrDataTypes[i].equals("VARCHAR") || arrDataTypes[i].equals("STRING")) {
                oraCallableStmt.registerOutParameter(Integer.parseInt(outParameterPositionsArray[i]), 12);
                System.out.println(String.valueOf(i) + "   out position--->" + outParameterPositionsArray[i]);
            }
            else if (arrDataTypes[i].equals("INTEGER") || arrDataTypes[i].equals("NUMBER")) {
                oraCallableStmt.registerOutParameter(Integer.parseInt(outParameterPositionsArray[i]), 2);
                System.out.println(String.valueOf(i) + "   out position--->" + outParameterPositionsArray[i]);
            }
        }
        oraCallableStmt.executeQuery();
        final ParameterMetaData parameterMetaData = oraCallableStmt.getParameterMetaData();
        arrDataTypes = strDatatypesList.split(",");
        for (int j = 0; j < arrDataTypes.length; ++j) {
            if (arrDataTypes[j].equalsIgnoreCase("CURSOR")) {
                System.out.println("data type-->" + arrDataTypes[j]);
                System.out.println("out position-->" + Integer.parseInt(outParameterPositionsArray[j]));
                final OracleResultSet oracleResultSet = (OracleResultSet)oraCallableStmt.getCursor(Integer.parseInt(outParameterPositionsArray[j]));
                final ResultSetMetaData rmd = oracleResultSet.getMetaData();
                System.out.println("column count-->" + rmd.getColumnCount());
                StoredProc.hashMap.put(Integer.parseInt(outParameterPositionsArray[j]), oracleResultSet);
            }
            else if (arrDataTypes[j].equals("VARCHAR") || arrDataTypes[j].equals("STRING")) {
                System.out.println("out position-->" + Integer.parseInt(outParameterPositionsArray[j]));
                final String strValue = oraCallableStmt.getString(Integer.parseInt(outParameterPositionsArray[j]));
                System.out.println("String value--->" + strValue);
                StoredProc.hashMap.put(Integer.parseInt(outParameterPositionsArray[j]), strValue);
            }
            else if (arrDataTypes[j].equalsIgnoreCase("NUMBER")) {
                final String strValue = Integer.valueOf(oraCallableStmt.getInt(Integer.parseInt(outParameterPositionsArray[j]))).toString();
                System.out.println("out position-->" + Integer.parseInt(outParameterPositionsArray[j]));
                System.out.println("Integer value--->" + strValue);
                StoredProc.hashMap.put(Integer.parseInt(outParameterPositionsArray[j]), strValue);
            }
        }
        System.out.println("size of hash map-->" + StoredProc.hashMap.size());
    }
}
