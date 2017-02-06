package in.ravikalla.dbXmlCompare.xmlCompareUtil.util.excel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.CommonUtil;

public class Mapping
{
    public static void insertColumn(final Sheet sheet, final String strCellContent, final List<String> lstValues) {
        final int[] intArrCoordinates = { -1, -1 };
        getStringPosition(sheet, strCellContent, intArrCoordinates);
        final int intStartRow = intArrCoordinates[0];
        final int intStartCol = intArrCoordinates[1];
        if (-1 == intStartRow || -1 == intStartCol) {
            return;
        }
        insertColumn(sheet, intStartRow + 1, intStartCol, lstValues);
    }
    
    public static void insertColumn(final Sheet sheet, int intRow, final int intCol, final List<String> lstValues) {
        if (lstValues == null || -1 == intCol || -1 == intRow) {
            return;
        }
        for (int intListPointer = 0; intListPointer < lstValues.size(); ++intListPointer, ++intRow) {
            final String strData = lstValues.get(intListPointer);
            Row objRow = sheet.getRow(intRow);
            if (objRow == null) {
                objRow = sheet.createRow(intRow);
            }
            Cell objCell = objRow.getCell(intCol);
            if (objCell == null) {
                objCell = objRow.createCell(intCol);
            }
            objCell.setCellValue(strData);
        }
    }
    
    public static List<String> getColumnList(final Sheet sheet, final String strCellContent) {
        final int[] intArrCoordinates = { -1, -1 };
        getStringPosition(sheet, strCellContent, intArrCoordinates);
        final int intStartRow = intArrCoordinates[0];
        final int intStartCol = intArrCoordinates[1];
        return getColumnList(sheet, intStartRow + 1, intStartCol);
    }
    
    public static List<String> getColumnList(final Sheet sheet, final int intStartRow, final int intStartCol) {
        final List<String> lstResult = new ArrayList<String>();
        if (-1 == intStartCol || -1 == intStartRow) {
            return lstResult;
        }
        int intRowCounter = intStartRow;
        while (true) {
            String strCellValue = "";
            final Row objRow = sheet.getRow(intRowCounter);
            if (objRow == null) {
                break;
            }
            final Cell objCell = objRow.getCell(intStartCol);
            if (objCell == null) {
                break;
            }
            if (1 == objCell.getCellType()) {
                strCellValue = objCell.getStringCellValue();
            }
            else if (objCell.getCellType() == 0) {
                strCellValue = CommonUtil.convertDoubleToString(objCell.getNumericCellValue());
            }
            else if (4 == objCell.getCellType()) {
                strCellValue = new Boolean(objCell.getBooleanCellValue()).toString();
            }
            if (strCellValue == null) {
                break;
            }
            lstResult.add(strCellValue);
            ++intRowCounter;
        }
        return lstResult;
    }
    
    public static List<String> getColumnList(final Sheet sheet, final int intStartRow, final int intEndRow, final int intCol) {
        final List<String> lstResult = new ArrayList<String>();
        if (-1 == intCol || -1 == intStartRow) {
            return lstResult;
        }
        if (-1 == intEndRow) {
            getColumnList(sheet, intStartRow, intCol);
        }
        for (int intRowCounter = intStartRow; intRowCounter <= intEndRow; ++intRowCounter) {
            String strCellValue = "";
            final Row objRow = sheet.getRow(intRowCounter);
            if (objRow == null) {
                break;
            }
            final Cell objCell = objRow.getCell(intCol);
            if (objCell == null) {
                break;
            }
            if (1 == objCell.getCellType()) {
                strCellValue = objCell.getStringCellValue();
            }
            else if (objCell.getCellType() == 0) {
                strCellValue = CommonUtil.convertDoubleToString(objCell.getNumericCellValue());
            }
            else if (4 == objCell.getCellType()) {
                strCellValue = new Boolean(objCell.getBooleanCellValue()).toString();
            }
            lstResult.add(strCellValue);
        }
        return lstResult;
    }
    
    public static void getStringPosition(final Sheet sheet, final String strCellContent, final int[] intArrCoordinates) {
        int intStartRow = -1;
        int intStartCol = -1;
        String strLocalColName = "";
        int intRowCounter = 0;
        while (true) {
            final Row objRow = sheet.getRow(intRowCounter);
            if (objRow == null) {
                break;
            }
            int intColCounter = 0;
            while (true) {
                final Cell objCell = objRow.getCell(intColCounter);
                if (objCell == null) {
                    break;
                }
                if (1 == objCell.getCellType()) {
                    strLocalColName = objCell.getStringCellValue();
                }
                if (strLocalColName != null && strLocalColName.trim().equalsIgnoreCase(strCellContent.trim())) {
                    intStartRow = intRowCounter;
                    intStartCol = intColCounter;
                    System.out.println("Matched : " + intStartRow + " : " + intStartCol);
                    break;
                }
                ++intColCounter;
            }
            if (-1 != intStartRow) {
                break;
            }
            if (-1 != intStartCol) {
                break;
            }
            ++intRowCounter;
        }
        intArrCoordinates[0] = intStartRow;
        intArrCoordinates[1] = intStartCol;
    }
    
    public static List<String> getHeaders(final Sheet sheet) {
        final List<String> lstHeaderValues = new ArrayList<String>();
        boolean blnHeaderIdentified = false;
        String strLocalColName = null;
        int intRowCounter = 0;
        while (!blnHeaderIdentified) {
            final Row objRow = sheet.getRow(intRowCounter);
            if (objRow == null) {
                break;
            }
            int intColCounter = 0;
            while (true) {
                final Cell objCell = objRow.getCell(intColCounter);
                if (objCell == null) {
                    break;
                }
                strLocalColName = null;
                if (1 == objCell.getCellType()) {
                    strLocalColName = objCell.getStringCellValue();
                }
                if (strLocalColName != null && strLocalColName.trim().length() > 0) {
                    lstHeaderValues.add(strLocalColName.trim());
                    blnHeaderIdentified = true;
                }
                ++intColCounter;
            }
            ++intRowCounter;
        }
        return lstHeaderValues;
    }
    
    public static List<String> getRowList(final Sheet sheet, final int intRow, final int intStartCol, final int intEndCol) {
        final List<String> lstHeaderValues = new ArrayList<String>();
        String strLocalColName = null;
        final Row objRow = sheet.getRow(intRow);
        if (objRow != null) {
            for (int intColCounter = intStartCol; intColCounter <= intEndCol; ++intColCounter) {
                final Cell objCell = objRow.getCell(intColCounter);
                if (objCell == null) {
                    break;
                }
                strLocalColName = null;
                if (1 == objCell.getCellType()) {
                    strLocalColName = objCell.getStringCellValue();
                }
                else {
                    strLocalColName = "";
                }
                if (strLocalColName != null && strLocalColName.trim().length() > 0) {
                    lstHeaderValues.add(strLocalColName.trim());
                }
            }
        }
        return lstHeaderValues;
    }
    
    public static void main(final String[] args) {
        final String strInputFile = "D:/Projects/SampleProject/ExcellConverter/src/Defect Tracker (Input).xlsx";
        try {
            final Workbook wb = ExcelCommon.getWorkBook(strInputFile);
            final Sheet sheet = wb.getSheetAt(0);
            List<String> lstResult = getColumnList(sheet, "Detected By");
            System.out.println("List Size : " + lstResult.size());
            for (final String strResult : lstResult) {
                System.out.println(strResult);
            }
            lstResult = getHeaders(sheet);
            System.out.println("Header Size : " + lstResult.size());
            for (final String strResult : lstResult) {
                System.out.println(strResult);
            }
            System.out.println("Before calling insertColumn");
            insertColumn(sheet, "Blocking", lstResult);
            ExcelCommon.writeToFile(wb, strInputFile);
            System.out.println("After calling insertColumn");
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
    }
}
