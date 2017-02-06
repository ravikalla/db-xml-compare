package in.ravikalla.dbXmlCompare.xmlCompareUtil.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CommonUtil
{
    public static String join(final String strFirst, final String strSecond, final String strSeparator) {
        String strResult = "";
        if (strFirst != null) {
            if (strSecond != null) {
                if (strFirst.equalsIgnoreCase("")) {
                    strResult = strSecond;
                }
                else if (strSecond.equalsIgnoreCase("")) {
                    strResult = strFirst;
                }
                else {
                    strResult = String.valueOf(strFirst) + strSeparator + strSecond;
                }
            }
            else {
                strResult = strFirst;
            }
        }
        else if (strSecond != null) {
            strResult = strSecond;
        }
        return strResult;
    }
    
    public static String convertDoubleToString(final Double dblValue) {
        final StringBuffer strBuffValue = new StringBuffer(Double.toString(dblValue));
        final int intPos = strBuffValue.lastIndexOf(".");
        for (int intTempPos = intPos + 1; intTempPos < strBuffValue.length(); ++intTempPos) {
            if ('0' != strBuffValue.charAt(intTempPos)) {
                return strBuffValue.toString();
            }
        }
        return strBuffValue.subSequence(0, intPos).toString();
    }
    
    public static String readDataFromFile(final String strFileName) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(strFileName));
        try {
            final StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        }
        finally {
            br.close();
        }
    }
    
    public static String readDataFromFile(final String strFileName, final String strSeparator) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(strFileName));
        try {
            final StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
                sb.append(strSeparator);
            }
            return sb.toString();
        }
        finally {
            br.close();
        }
    }
    
    public static List<String> readListFromFile(final String strFileName) throws IOException {
        final List<String> lstResult = new ArrayList<String>();
        final BufferedReader br = new BufferedReader(new FileReader(strFileName));
        try {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                lstResult.add(line);
            }
        }
        finally {
            br.close();
        }
        br.close();
        return lstResult;
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
    
    public static void main(final String[] args) {
        System.out.println(convertDoubleToString(1.0));
        System.out.println(convertDoubleToString(12.0));
        System.out.println(convertDoubleToString(2.0));
        System.out.println(convertDoubleToString(213.0));
        System.out.println(convertDoubleToString(2.0001));
        System.out.println(convertDoubleToString(232.0001));
        System.out.println(convertDoubleToString(2.0));
        System.out.println(convertDoubleToString(232.0));
        System.out.println(convertDoubleToString(2.0001));
        System.out.println(convertDoubleToString(22.0001));
    }
    
    public static List<String> getComparedColumnsInRow(final String[] arrDBData, final String[] arrXMLData, final String[] arrHeaderCols, final int intXMLPrimaryKeyIndex) {
        List<String> lstComparedColumnsInRow = null;
        String strTemp = null;
        if (arrDBData != null && arrXMLData != null && intXMLPrimaryKeyIndex < arrDBData.length && intXMLPrimaryKeyIndex < arrXMLData.length) {
            System.out.println("115 : intXMLPrimaryKeyIndex : " + intXMLPrimaryKeyIndex + " : " + arrDBData.length + " : " + arrXMLData.length + " : " + arrHeaderCols.length);
            if (arrDBData[intXMLPrimaryKeyIndex].equals(arrXMLData[intXMLPrimaryKeyIndex])) {
                lstComparedColumnsInRow = new ArrayList<String>();
                for (int intCtr = 0; intCtr < arrHeaderCols.length; ++intCtr) {
                    if (intCtr < arrXMLData.length && intCtr < arrDBData.length && arrXMLData[intCtr].equals(arrDBData[intCtr])) {
                        strTemp = String.valueOf(arrHeaderCols[intCtr]) + "|" + arrXMLData[intCtr] + "|" + arrDBData[intCtr] + "|" + "MATCHED";
                    }
                    else if (intCtr < arrXMLData.length && intCtr < arrDBData.length) {
                        strTemp = String.valueOf(arrHeaderCols[intCtr]) + "|" + arrXMLData[intCtr] + "|" + arrDBData[intCtr] + "|" + "MISMATCHED";
                    }
                    else if (intCtr >= arrXMLData.length && intCtr >= arrDBData.length) {
                        strTemp = String.valueOf(arrHeaderCols[intCtr]) + "| | |" + "MATCHED";
                    }
                    else if (intCtr >= arrXMLData.length) {
                        strTemp = String.valueOf(arrHeaderCols[intCtr]) + "| |" + arrDBData[intCtr] + "|" + "MISMATCHED";
                    }
                    else if (intCtr >= arrDBData.length) {
                        strTemp = String.valueOf(arrHeaderCols[intCtr]) + "|" + arrXMLData[intCtr] + "| |" + "MISMATCHED";
                    }
                    lstComparedColumnsInRow.add(strTemp);
                }
            }
        }
        return lstComparedColumnsInRow;
    }
    
    public static boolean isMatchedRow(final List<String> lstComparedColumnsOfRow) {
        boolean isMismatchedRow = false;
        for (final String strTemp : lstComparedColumnsOfRow) {
            final String[] arrTemp = strTemp.split("\\|");
            if (arrTemp != null && arrTemp.length > 0 && arrTemp[arrTemp.length - 1].equals("MISMATCHED")) {
                isMismatchedRow = true;
            }
        }
        return !isMismatchedRow;
    }
    
    public static List<String> convertedListOfXMLRowToCols(final String[] arrHeaderCols, final String strXMLData) {
        final String[] arrXMLData = strXMLData.split("\\|");
        final List<String> lstXMLRowToCols = new ArrayList<String>();
        for (int intCtr = 0; intCtr < arrHeaderCols.length; ++intCtr) {
            if (intCtr < arrXMLData.length) {
                lstXMLRowToCols.add(String.valueOf(arrHeaderCols[intCtr]) + "|" + arrXMLData[intCtr] + "| |" + "MISMATCHED");
            }
            else {
                lstXMLRowToCols.add(String.valueOf(arrHeaderCols[intCtr]) + "| | |" + "MATCHED");
            }
        }
        return lstXMLRowToCols;
    }
    
    public static List<String> convertedListOfDBRowToCols(final String[] arrHeaderCols, final String strDBData) {
        final String[] arrDBData = strDBData.split("\\|");
        final List<String> lstDBRowToCols = new ArrayList<String>();
        for (int intCtr = 0; intCtr < arrHeaderCols.length; ++intCtr) {
            if (intCtr < arrDBData.length) {
                lstDBRowToCols.add(String.valueOf(arrHeaderCols[intCtr]) + "| |" + arrDBData[intCtr] + "|" + "MISMATCHED");
            }
            else {
                lstDBRowToCols.add(String.valueOf(arrHeaderCols[intCtr]) + "| | |" + "MATCHED");
            }
        }
        return lstDBRowToCols;
    }
}
