package in.ravikalla.dbXmlCompare.xmlCompareUtil.util.excel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import in.ravikalla.dbXmlCompare.xmlCompareUtil.dto.MappingDataDTO;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.CommonUtil;

public class ExcelUtil
{
    public static MappingDataDTO getDataFromSheet(final Sheet sheet, final MappingDataDTO objMappingData) {
        final int intStartRow = 1;
        final int intStartCol = 0;
        List<String> lstElementMapping = null;
        Map<String, String> mapElementToDB = null;
        int intRowCounter = intStartRow;
        while (true) {
            String strCursorName = "";
            String strIterateElement = "";
            String strElementMapping = "";
            String strDBCol = "";
            String strFormatCol = "";
            String strLookupNameCol = "";
            final Row objRow = sheet.getRow(intRowCounter);
            System.out.println("Row : " + intRowCounter);
            if (objRow == null) {
                break;
            }
            final Cell cellCursorName = objRow.getCell(intStartCol);
            if (cellCursorName == null) {
                break;
            }
            if (1 == cellCursorName.getCellType()) {
                strCursorName = cellCursorName.getStringCellValue().trim();
            }
            else if (cellCursorName.getCellType() == 0) {
                strCursorName = CommonUtil.convertDoubleToString(cellCursorName.getNumericCellValue());
            }
            else if (4 == cellCursorName.getCellType()) {
                strCursorName = new Boolean(cellCursorName.getBooleanCellValue()).toString();
            }
            if (strCursorName == null) {
                break;
            }
            final Cell cellIterateElement = objRow.getCell(intStartCol + 1);
            if (cellIterateElement == null) {
                break;
            }
            if (1 == cellIterateElement.getCellType()) {
                strIterateElement = cellIterateElement.getStringCellValue().trim();
            }
            else if (cellIterateElement.getCellType() == 0) {
                strIterateElement = CommonUtil.convertDoubleToString(cellIterateElement.getNumericCellValue());
            }
            else if (4 == cellIterateElement.getCellType()) {
                strIterateElement = new Boolean(cellIterateElement.getBooleanCellValue()).toString();
            }
            if (strIterateElement == null) {
                break;
            }
            final Cell cellElementMapping = objRow.getCell(intStartCol + 2);
            if (cellElementMapping == null) {
                break;
            }
            if (1 == cellElementMapping.getCellType()) {
                strElementMapping = cellElementMapping.getStringCellValue().trim();
            }
            else if (cellElementMapping.getCellType() == 0) {
                strElementMapping = CommonUtil.convertDoubleToString(cellElementMapping.getNumericCellValue());
            }
            else if (4 == cellElementMapping.getCellType()) {
                strElementMapping = new Boolean(cellElementMapping.getBooleanCellValue()).toString();
            }
            if (strElementMapping == null) {
                break;
            }
            final Cell cellDBCol = objRow.getCell(intStartCol + 3);
            if (cellDBCol == null) {
                break;
            }
            if (1 == cellDBCol.getCellType()) {
                strDBCol = cellDBCol.getStringCellValue().trim();
            }
            else if (cellDBCol.getCellType() == 0) {
                strDBCol = CommonUtil.convertDoubleToString(cellDBCol.getNumericCellValue());
            }
            else if (4 == cellDBCol.getCellType()) {
                strDBCol = new Boolean(cellDBCol.getBooleanCellValue()).toString();
            }
            if (strDBCol == null) {
                break;
            }
            final Cell cellFormatCol = objRow.getCell(intStartCol + 4);
            if (cellFormatCol != null) {
                if (1 == cellFormatCol.getCellType()) {
                    strFormatCol = cellFormatCol.getStringCellValue().trim();
                }
                else if (cellFormatCol.getCellType() == 0) {
                    strFormatCol = CommonUtil.convertDoubleToString(cellFormatCol.getNumericCellValue());
                }
                else if (4 == cellFormatCol.getCellType()) {
                    strFormatCol = new Boolean(cellFormatCol.getBooleanCellValue()).toString();
                }
            }
            final Cell cellLookupNameCol = objRow.getCell(intStartCol + 5);
            if (cellLookupNameCol != null) {
                if (1 == cellLookupNameCol.getCellType()) {
                    strLookupNameCol = cellLookupNameCol.getStringCellValue().trim();
                }
                else if (cellLookupNameCol.getCellType() == 0) {
                    strLookupNameCol = CommonUtil.convertDoubleToString(cellLookupNameCol.getNumericCellValue());
                }
                else if (4 == cellLookupNameCol.getCellType()) {
                    strLookupNameCol = new Boolean(cellLookupNameCol.getBooleanCellValue()).toString();
                }
            }
            if (objMappingData.mapCursorRepeatableElement == null) {
                objMappingData.mapCursorRepeatableElement = new HashMap<String, String>();
            }
            if (objMappingData.mapCursorSpecificElements == null) {
                objMappingData.mapCursorSpecificElements = new HashMap<String, List<String>>();
            }
            if (objMappingData.mapElementToDB == null) {
                objMappingData.mapElementToDB = new HashMap<String, Map<String, String>>();
            }
            if (objMappingData.mapDataSheetFormatForComparison == null) {
                objMappingData.mapDataSheetFormatForComparison = new HashMap<String, String>();
            }
            if (objMappingData.mapDataSheetLookupForConversion == null) {
                objMappingData.mapDataSheetLookupForConversion = new HashMap<String, String>();
            }
            objMappingData.mapCursorRepeatableElement.put(strCursorName, strIterateElement);
            lstElementMapping = objMappingData.mapCursorSpecificElements.get(strCursorName);
            if (lstElementMapping == null) {
                lstElementMapping = new ArrayList<String>();
            }
            lstElementMapping.add(strElementMapping);
            objMappingData.mapCursorSpecificElements.put(strCursorName, lstElementMapping);
            mapElementToDB = objMappingData.mapElementToDB.get(strCursorName);
            if (mapElementToDB == null) {
                mapElementToDB = new LinkedHashMap<String, String>();
            }
            mapElementToDB.put(strElementMapping, strDBCol);
            objMappingData.mapElementToDB.put(strCursorName, mapElementToDB);
            final String strFormat = String.valueOf(strCursorName) + "|" + strElementMapping;
            objMappingData.mapDataSheetFormatForComparison.put(strFormat, strFormatCol);
            final String strLookupName = String.valueOf(strCursorName) + "|" + strElementMapping;
            objMappingData.mapDataSheetLookupForConversion.put(strLookupName, strLookupNameCol);
            ++intRowCounter;
        }
        return objMappingData;
    }
    
    public static MappingDataDTO getLookupInfoFromSheet(final Sheet sheet, final MappingDataDTO objMappingData) {
        final int intStartRow = 1;
        final int intStartCol = 0;
        Map<String, String> mapWSLOVKeyValuePairs = null;
        Map<String, String> mapDBLOVKeyValuePairs = null;
        int intRowCounter = intStartRow;
        while (true) {
            String strLOVName = "";
            String strCellFromValue = "";
            String strCellToValue = "";
            final Row objRow = sheet.getRow(intRowCounter);
            System.out.println("Row : " + intRowCounter);
            if (objRow == null) {
                break;
            }
            final Cell cellLOVName = objRow.getCell(intStartCol);
            if (cellLOVName == null) {
                break;
            }
            if (1 == cellLOVName.getCellType()) {
                strLOVName = cellLOVName.getStringCellValue().trim();
            }
            else if (cellLOVName.getCellType() == 0) {
                strLOVName = CommonUtil.convertDoubleToString(cellLOVName.getNumericCellValue());
            }
            else if (4 == cellLOVName.getCellType()) {
                strLOVName = new Boolean(cellLOVName.getBooleanCellValue()).toString();
            }
            if (strLOVName == null) {
                break;
            }
            final Cell cellFromValue = objRow.getCell(intStartCol + 1);
            if (cellFromValue == null) {
                break;
            }
            if (1 == cellFromValue.getCellType()) {
                strCellFromValue = cellFromValue.getStringCellValue().trim();
            }
            else if (cellFromValue.getCellType() == 0) {
                strCellFromValue = CommonUtil.convertDoubleToString(cellFromValue.getNumericCellValue());
            }
            else if (4 == cellFromValue.getCellType()) {
                strCellFromValue = new Boolean(cellFromValue.getBooleanCellValue()).toString();
            }
            if (strCellFromValue == null) {
                break;
            }
            final Cell cellTOValue = objRow.getCell(intStartCol + 2);
            if (cellTOValue == null) {
                break;
            }
            if (1 == cellTOValue.getCellType()) {
                strCellToValue = cellTOValue.getStringCellValue().trim();
            }
            else if (cellTOValue.getCellType() == 0) {
                strCellToValue = CommonUtil.convertDoubleToString(cellTOValue.getNumericCellValue());
            }
            else if (4 == cellTOValue.getCellType()) {
                strCellToValue = new Boolean(cellTOValue.getBooleanCellValue()).toString();
            }
            if (strCellToValue == null) {
                break;
            }
            if (objMappingData.mapDBLookup == null) {
                objMappingData.mapDBLookup = new HashMap<String, Map<String, String>>();
            }
            if (objMappingData.mapWSLookup == null) {
                objMappingData.mapWSLookup = new HashMap<String, Map<String, String>>();
            }
            if (strLOVName != null && strLOVName.length() > 0) {
                if (strLOVName.indexOf("WS_") == 0) {
                    mapWSLOVKeyValuePairs = objMappingData.mapWSLookup.get(strLOVName);
                    if (mapWSLOVKeyValuePairs == null) {
                        mapWSLOVKeyValuePairs = new HashMap<String, String>();
                    }
                    mapWSLOVKeyValuePairs.put(strCellFromValue, strCellToValue);
                    objMappingData.mapWSLookup.put(strLOVName, mapWSLOVKeyValuePairs);
                }
                if (strLOVName.indexOf("DB_") == 0) {
                    mapDBLOVKeyValuePairs = objMappingData.mapDBLookup.get(strLOVName);
                    if (mapDBLOVKeyValuePairs == null) {
                        mapDBLOVKeyValuePairs = new HashMap<String, String>();
                    }
                    mapDBLOVKeyValuePairs.put(strCellFromValue, strCellToValue);
                    objMappingData.mapDBLookup.put(strLOVName, mapDBLOVKeyValuePairs);
                }
            }
            ++intRowCounter;
        }
        return objMappingData;
    }
    
    public static MappingDataDTO getFormatInfoFromSheet(final Sheet sheet, final MappingDataDTO objMappingData) {
        final int intStartRow = 1;
        final int intStartCol = 0;
        int intRowCounter = intStartRow;
        while (true) {
            String strFormatType = "";
            String strFormatValue = "";
            final Row objRow = sheet.getRow(intRowCounter);
            System.out.println("Row : " + intRowCounter);
            if (objRow == null) {
                break;
            }
            final Cell cellFormatType = objRow.getCell(intStartCol);
            if (cellFormatType == null) {
                break;
            }
            if (1 == cellFormatType.getCellType()) {
                strFormatType = cellFormatType.getStringCellValue().trim();
            }
            else if (cellFormatType.getCellType() == 0) {
                strFormatType = CommonUtil.convertDoubleToString(cellFormatType.getNumericCellValue());
            }
            else if (4 == cellFormatType.getCellType()) {
                strFormatType = new Boolean(cellFormatType.getBooleanCellValue()).toString();
            }
            if (strFormatType == null) {
                break;
            }
            final Cell cellFormatValue = objRow.getCell(intStartCol + 1);
            if (cellFormatValue == null) {
                break;
            }
            if (1 == cellFormatValue.getCellType()) {
                strFormatValue = cellFormatValue.getStringCellValue().trim();
            }
            else if (cellFormatValue.getCellType() == 0) {
                strFormatValue = CommonUtil.convertDoubleToString(cellFormatValue.getNumericCellValue());
            }
            else if (4 == cellFormatValue.getCellType()) {
                strFormatValue = new Boolean(cellFormatValue.getBooleanCellValue()).toString();
            }
            if (strFormatValue == null) {
                break;
            }
            if (objMappingData.mapFormatSheetInfo == null) {
                objMappingData.mapFormatSheetInfo = new HashMap<String, String>();
            }
            if (strFormatType != null && strFormatType.length() > 0) {
                objMappingData.mapFormatSheetInfo.put(strFormatType, strFormatValue);
            }
            ++intRowCounter;
        }
        return objMappingData;
    }
}
