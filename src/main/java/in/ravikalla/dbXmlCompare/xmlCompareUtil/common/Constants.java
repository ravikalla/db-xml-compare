package in.ravikalla.dbXmlCompare.xmlCompareUtil.common;

public class Constants {
    public static final String EMPTY_STRING = "";
    public static final String ORACLE_CURSOR = "CURSOR";
    public static final String ORACLE_VARCHAR = "VARCHAR";
    public static final String ORACLE_STRING = "STRING";
    public static final String ORACLE_INTEGER = "INTEGER";
    public static final String ORACLE_NUMBER = "NUMBER";
    public static final String FIELD_SEPARATOR = "|";
    public static final String FIELD_SEPARATOR_SPLIT = "\\|";
    public static final String FIRST_SHEET_NAME = "1";
    public static final String CHECK_MATCHING_DATA = "CHECK_MATCHING_DATA";
    public static final String CHECK_MISMATCHING_DATA = "CHECK_MISMATCHING_DATA";
    public static final String MATCHED = "MATCHED";
    public static final String MISMATCHED = "MISMATCHED";
    public static final int INVALID_TYPE = -1;
    public static final int MAX_DEPTH_OF_XML = 100;
    public static final int MIN_ROW_INDEX_OF_EXCEL = 1;
    public static final int MIN_COL_INDEX_OF_EXCEL = 2;
    public static final String DB_CONNECTION_STRING = "jdbc:oracle:thin:@//<IP>:<PORT>/<DB>";
    public static final String DB_USER_ID = "<DB_USER_NAME>";
    public static final String DB_PWD = "<DB_USER_PWD>";
}
