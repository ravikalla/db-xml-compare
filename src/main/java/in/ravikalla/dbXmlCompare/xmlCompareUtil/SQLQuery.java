package in.ravikalla.dbXmlCompare.xmlCompareUtil;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import oracle.jdbc.pool.OracleDataSource;

public class SQLQuery
{
    public static void main(final String[] args) {
        String strSQLQuery = null;
        final String strConnectionString = "jdbc:oracle:thin:@//<IP>:<PORT>/<DB NAME>";
        final String userName = "<U_NAME>";
        final String password = "<PWD>";
        strSQLQuery = "select distinct rl.list_id list_id,rl.remittance_list_name remittance_list_name,rl.payroll_due_date payroll_due_date, case when RL.List_type_code in ('2','3') then 'Unscheduled' end payroll_frequency_Name, PF.payroll_frequency_Name as PAYROLL, rl.drv_participant_count, ls.list_status_description, to_char(rl.LAST_UPDATE_DATETIME, 'mm/dd/yyyy')LASTUPDATEDDATE, rl.drv_total_remit_amt from remittance.remit_list_template rlt,remittance.payroll_frequency pf,remittance.remit_list rl, remittance.organization cl,list_status ls, remittance.list_type LT where rlt.payroll_frequency_code=pf.payroll_frequency_code and rlt.template_id=rl.template_id and Rl.list_type_code = LT.list_type_code and cl.organization_id = rl.client_id and rl.list_status_code=ls.list_status_Code and cl.ORGANIZATION_TYPE_CODE ='CL' and rl.list_status_code in (0,2,3,4,10,15,1) and cl.SOURCE_SYSTEM_ID='064546'";
        final Map<String, Map<String, String>> mapElementToDB = new LinkedHashMap<String, Map<String, String>>();
        final Map<String, String> mapDBColNames = new LinkedHashMap<String, String>();
        mapDBColNames.put("XML Col1", "list_id");
        mapDBColNames.put("XML Col2", "remittance_list_name");
        mapDBColNames.put("XML Col3", "payroll_due_date");
        mapElementToDB.put("1", mapDBColNames);
        saveSQLQueryDataToFile(strSQLQuery, "C:\\Users\\kalla\\Desktop\\test.xls", strConnectionString, userName, password, mapElementToDB);
    }
    
    public static void saveSQLQueryDataToFile(final String strSQLQuery, final String strOPFileName, final String strConnectionString, final String userName, final String password, final Map<String, Map<String, String>> mapElementToDB) {
        final Map<String, List<String>> mapSQLQueryData = getSQLQueryData(strSQLQuery, strConnectionString, userName, password, mapElementToDB);
        if (strOPFileName != null && !strOPFileName.equals("")) {
            writeFileToDisk(mapSQLQueryData, strOPFileName);
        }
    }
    
    public static Map<String, List<String>> getSQLQueryData(final String strSQLQuery, final String strConnectionString, final String userName, final String password, final Map<String, Map<String, String>> mapElementToDB) {
        String strSheetName = null;
        Map<String, List<String>> mapParametersData = null;
        List<String> lstParametersData = null;
        final String driver = "oracle.jdbc.driver.OracleDriver";
        Connection conn = null;
        PreparedStatement objPreparedStmt = null;
        ResultSet objResultSet = null;
        try {
            List<String> lstDBEntries = new ArrayList<String>();
            for (final Map.Entry<String, Map<String, String>> entry : mapElementToDB.entrySet()) {
                lstDBEntries = getColumnNamesToBeConsidered(entry.getValue());
                final Map<String, String> mapDBCol = mapElementToDB.get(entry.getKey());
                final StringBuffer strRowData = new StringBuffer();
                for (final Map.Entry<String, String> entryDBCol : mapDBCol.entrySet()) {
                    if (strRowData.length() != 0) {
                        strRowData.append("|");
                    }
                    strRowData.append(entryDBCol.getKey());
                }
                strSheetName = entry.getKey();
                lstParametersData = new ArrayList<String>();
                lstParametersData.add(strRowData.toString());
            }
            Class.forName(driver);
            final OracleDataSource ods = new OracleDataSource();
            ods.setURL(strConnectionString);
            ods.setUser(userName);
            ods.setPassword(password);
            conn = ods.getConnection();
            objPreparedStmt = conn.prepareStatement(strSQLQuery);
            objResultSet = objPreparedStmt.executeQuery();
            while (objResultSet.next()) {
                final StringBuffer strTemp = new StringBuffer();
                for (int i = 0; lstDBEntries != null && i < lstDBEntries.size(); ++i) {
                    if (i != 0) {
                        strTemp.append("|");
                    }
                    String strTempData = objResultSet.getString(lstDBEntries.get(i));
                    if (strTempData == null) {
                        strTempData = "";
                    }
                    strTemp.append(strTempData);
                }
                lstParametersData.add(strTemp.toString());
            }
            if (mapParametersData == null) {
                mapParametersData = new LinkedHashMap<String, List<String>>();
            }
            mapParametersData.put(strSheetName, lstParametersData);
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
                return mapParametersData;
            }
            return mapParametersData;
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
        return mapParametersData;
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
}
