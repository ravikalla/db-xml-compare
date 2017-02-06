package in.ravikalla.dbXmlCompare.xmlConvert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.CommonUtil;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.FragmentContentHandler;

public class HTMLUtility
{
    private static HSSFFormulaEvaluator objFormulaEvaluator;
    private static DataFormatter objDefaultFormat;
    
    static {
        HTMLUtility.objFormulaEvaluator = null;
        HTMLUtility.objDefaultFormat = new DataFormatter();
    }
    
    public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
        final String mappingFilePath = "C:/data/ExcelMappingConfigFileTemplate.xls";
        final String htmlFilePath = "C:/data/TC001_UI_Service_Validation.html";
        final String xmFilePath = "C:/data/Temp.xml";
        final String matchedXpathsFilePath = "C:/data/MatchedXpathsAndValues.csv";
        findMatchedXpaths(mappingFilePath, htmlFilePath, xmFilePath, matchedXpathsFilePath);
    }
    
    public static void findMatchedXpaths(final String mappingFilePath, final String htmlFilePath, final String xmFilePath, final String matchedXpathsFilePath) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
        final Map<String, String> mapForHtml = getXPathsAndValues(htmlFilePath);
        final Map<String, String> mapForXml = getXPathsAndValues(xmFilePath);
        final Map<String, String> matchedXpaths = new HashMap<String, String>();
        int count = 0;
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(matchedXpathsFilePath), "utf-8"));
        writer.write("xml xpath,xml vlaue,html xpath,html value");
        for (final Map.Entry<String, String> xmlEntry : mapForXml.entrySet()) {
            for (final Map.Entry<String, String> htmlEntry : mapForHtml.entrySet()) {
                if (xmlEntry.getValue().equalsIgnoreCase(htmlEntry.getValue())) {
                    writer.newLine();
                    writer.write(String.valueOf(xmlEntry.getKey()) + "," + xmlEntry.getValue() + "," + htmlEntry.getKey() + "," + htmlEntry.getValue());
                    matchedXpaths.put(xmlEntry.getKey(), htmlEntry.getKey());
                    ++count;
                    break;
                }
            }
        }
        writer.flush();
        writer.close();
        System.out.println("i valuee------>" + count);
        mapXmlElmentsinMappingSheet(matchedXpaths, mappingFilePath);
    }
    
    private static void mapXmlElmentsinMappingSheet(final Map<String, String> matchedXpaths, final String mappingFileName) {
        try {
            final FileInputStream file = new FileInputStream(new File(mappingFileName));
            final HSSFWorkbook workbook = new HSSFWorkbook((InputStream)file);
            final HSSFSheet sheet = workbook.getSheetAt(0);
            for (final String xmlXpath : matchedXpaths.keySet()) {
                final String[] xmlXpaths = xmlXpath.split("/");
                final int nodesinXpath = xmlXpaths.length - 1;
                String element = xmlXpaths[nodesinXpath];
                element = element.replaceAll("\\[\\d*\\]", "");
                HTMLUtility.objFormulaEvaluator = new HSSFFormulaEvaluator(workbook);
                putElementInMappingFile(xmlXpath, matchedXpaths.get(xmlXpath), element, nodesinXpath, sheet);
            }
            workbook.write((OutputStream)new FileOutputStream(new File(mappingFileName)));
            file.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
    }
    
    private static void putElementInMappingFile(final String xmlXpath, final String htmlXpath, final String element, final int column, final HSSFSheet sheet) {
        final int excelColumn = column + 1;
        try {
            for (int i = 1; i < sheet.getLastRowNum() - 1; ++i) {
                final Row row = (Row)sheet.getRow(i);
                if (row != null) {
                    final Cell cell = row.getCell(excelColumn);
                    if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase(element.trim())) {
                        row.createCell(1).setCellValue(htmlXpath);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String convertHtmlToXml(final File file, final String tableId, final String tableType, final String root, final String recordTag) {
        final StringBuilder sb = new StringBuilder();
        String row = null;
        String cell = null;
        if (tableType.equals("table")) {
            row = "tr";
            cell = "td";
        }
        else {
            row = "ul";
            cell = "li";
        }
        final ArrayList<String> headers = new ArrayList<String>();
        try {
            final Document doc = Jsoup.parse(file, "ISO-8859-1");
            sb.append("<" + root + ">");
            final Element ele = doc.getElementById(tableId);
            final Elements trs = ele.getElementsByTag(row);
            for (final Element tr : trs) {
                final Elements ths = tr.getElementsByTag("th");
                int i = 0;
                for (final Element th : ths) {
                    headers.add(th.text());
                }
                final Elements tds = tr.getElementsByTag(cell);
                if (tds.size() > 0) {
                    sb.append("<" + recordTag + ">");
                    for (final Element td : tds) {
                        sb.append("<" + headers.get(i) + ">" + td.text() + "<" + headers.get(i) + "/>");
                        ++i;
                    }
                    sb.append("<" + recordTag + "/>");
                }
            }
            sb.append("<" + root + "/>");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    
    public static List<Map<String, String>> getRecordsFromTable(final String strHtmlFilePath, final String tableId, final String columnHeaders, final Map<String, String> mapParamsFromParent) {
        System.out.println("table id-->" + tableId);
        System.out.println("columnHeaders-->" + columnHeaders);
        final File file = new File(strHtmlFilePath);
        Map<String, String> linkedHashMap = new LinkedHashMap<String, String>();
        final List<Map<String, String>> listOfRecordData = new LinkedList<Map<String, String>>();
        Element ele = null;
        final StringBuilder sb = new StringBuilder();
        String row = null;
        String cell = null;
        row = "tr";
        cell = "td";
        final ArrayList<String> headers = new ArrayList<String>();
        final String[] arrHeaders = columnHeaders.split(",");
        try {
            int recordData = 0;
            final Document doc = Jsoup.parse(file, "ISO-8859-1");
            if (tableId.startsWith("id")) {
                ele = doc.getElementById(tableId);
            }
            else {
                ele = doc.getElementById(tableId);
            }
            final Elements trs = ele.getElementsByTag(row);
            for (final Element tr : trs) {
                final Elements ths = tr.getElementsByTag("th");
                int i = 0;
                for (int h = 0; h < arrHeaders.length; ++h) {
                    headers.add(arrHeaders[h]);
                }
                final Elements tds = tr.getElementsByTag(cell);
                if (tds.size() > 0) {
                    linkedHashMap = new LinkedHashMap<String, String>();
                    for (final Element td : tds) {
                        linkedHashMap.put(headers.get(i).toUpperCase(), getTransmissionData(mapParamsFromParent, td.text()));
                        System.out.println("column header--->" + headers.get(i) + "  column value--->" + td.text());
                        ++i;
                    }
                }
                if (recordData != 0) {
                    listOfRecordData.add(linkedHashMap);
                }
                ++recordData;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return listOfRecordData;
    }
    
    public static String getTagData(final File strHtmlFile) throws IOException {
        final String strTagValue = "";
        final Document doc = Jsoup.parse(strHtmlFile, "ISO-8859-1");
        final Elements elements = doc.body().getAllElements();
        for (final Element element : elements) {
            System.out.println("tag name       " + element.tagName());
            System.out.println("tag text value   " + element.text());
        }
        return null;
    }
    
    public static LinkedHashMap<String, String> getXPathsAndValues(final String strHtmlFilePath) throws ParserConfigurationException, SAXException, FileNotFoundException, IOException {
        LinkedHashMap<String, String> map = null;
        try {
            final SAXParserFactory spf = SAXParserFactory.newInstance();
            final SAXParser sp = spf.newSAXParser();
            final XMLReader xr = sp.getXMLReader();
            final FragmentContentHandler contentHandler = new FragmentContentHandler(xr);
            xr.setContentHandler(contentHandler);
            xr.parse(new InputSource(new FileInputStream(strHtmlFilePath)));
            map = new LinkedHashMap<String, String>(FragmentContentHandler.linkedHashMap);
            FragmentContentHandler.linkedHashMap = new LinkedHashMap<String, String>();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }
    
    public static LinkedHashMap<String, String> getValuesAndXpaths(final String strHtmlFilePath) throws ParserConfigurationException, SAXException, FileNotFoundException, IOException {
        LinkedHashMap<String, String> map = null;
        try {
            final SAXParserFactory spf = SAXParserFactory.newInstance();
            final SAXParser sp = spf.newSAXParser();
            final XMLReader xr = sp.getXMLReader();
            final FragmentContentHandler contentHandler = new FragmentContentHandler(xr);
            xr.setContentHandler(contentHandler);
            xr.parse(new InputSource(new FileInputStream(strHtmlFilePath)));
            map = (LinkedHashMap<String, String>)(LinkedHashMap)FragmentContentHandler.linkedHashMap;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }
    
    public static void createExcleFileByMap(final Map<String, String> mapData, final String excelFilePath) throws IOException {
        final HSSFWorkbook new_workbook = new HSSFWorkbook();
        final HSSFSheet sheet = new_workbook.createSheet("Sheet1");
        final Set<String> keyset = mapData.keySet();
        int rownum = 0;
        for (final Map.Entry entry : mapData.entrySet()) {
            final Row row = (Row)sheet.createRow(rownum++);
            final Cell cell = row.createCell(0);
            cell.setCellValue(entry.getKey().toString());
            final Cell cell2 = row.createCell(1);
            cell2.setCellValue(entry.getValue().toString());
        }
        final FileOutputStream output_file = new FileOutputStream(new File(excelFilePath));
        new_workbook.write((OutputStream)output_file);
        output_file.close();
    }
    
    public static void generateExcelFileXpathsAndValuesForHtml(final String strHtmlFilePath, final String strExcleFilePath) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
        getWellformedHtml(strHtmlFilePath);
        removeUnnecessaryCodeFromXhtml(strHtmlFilePath);
        final LinkedHashMap<String, String> xpathValueMap = getXPathsAndValues(strHtmlFilePath);
        createExcleFileByMap(xpathValueMap, strExcleFilePath);
    }
    
    public static void generateExcelFileXpathsAndValuesForXML(final String strXMLFilePath, final String strExcleFilePath) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
        final LinkedHashMap<String, String> xpathValueMap = getXPathsAndValues(strXMLFilePath);
        createExcleFileByMap(xpathValueMap, strExcleFilePath);
    }
    
    public static String getTransmissionData(final Map<String, String> mapParamsFromParent, final String strData) {
        String transmissionData = strData;
        System.out.println("strData--->" + strData);
        try {
            if (mapParamsFromParent != null) {
                transmissionData = ((mapParamsFromParent.get(strData) == null) ? strData : mapParamsFromParent.get(strData));
                System.out.println("mapdata--->" + mapParamsFromParent.get(strData));
            }
            System.out.println("transmissionData--->" + transmissionData);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return transmissionData;
    }
    
    public static String convertWelFormedhtml(final String strHtml) throws IOException {
        String str = CommonUtil.readDataFromFile("F:/Coding-WorkSpace/TestJarPackage/src/ActualHtmlFile.xml");
        str = str.replaceAll("<script.*>\\s*[^<]+<!+[^<]+<\\/script>|<script.*>\\s*<\\/script>|<script.*>\\s*[^<]+<\\/script>", "");
        return null;
    }
    
    public static void writeFileToDisk(final String strData, final String strFileName) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(strFileName), "utf-8"));
            writer.write(strData);
        }
        catch (IOException ex) {
            System.out.println("79 : IOException : " + ex);
            try {
                writer.close();
            }
            catch (Exception ex2) {
                System.out.println("84 : Exception : " + ex2);
            }
            return;
        }
        finally {
            try {
                writer.close();
            }
            catch (Exception ex2) {
                System.out.println("84 : Exception : " + ex2);
            }
        }
        try {
            writer.close();
        }
        catch (Exception ex2) {
            System.out.println("84 : Exception : " + ex2);
        }
    }
    
    public static void getWellformedHtml(final String strHtmlFilePath) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(strHtmlFilePath);
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found: " + strHtmlFilePath);
        }
        final Tidy tidy = new Tidy();
        tidy.setShowWarnings(false);
        tidy.setXmlTags(false);
        tidy.setInputEncoding("UTF-8");
        tidy.setOutputEncoding("UTF-8");
        tidy.setXHTML(true);
        tidy.setMakeClean(true);
        final org.w3c.dom.Document xmlDoc = tidy.parseDOM((InputStream)fis, (OutputStream)null);
        try {
            tidy.pprint(xmlDoc, (OutputStream)new FileOutputStream(strHtmlFilePath));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void removeUnnecessaryCodeFromXhtml(final String strHtmlFilePath) throws IOException {
        String strXhtmlFile = CommonUtil.readDataFromFile(strHtmlFilePath);
        strXhtmlFile = strXhtmlFile.replaceAll("<!DOCTYPE html.*.\\s.*dtd.*>", "");
        strXhtmlFile = strXhtmlFile.replaceAll("&nbsp", "");
        final Matcher matcher = Pattern.compile("<script.*?</script>", 42).matcher(strXhtmlFile);
        if (matcher.find()) {
            strXhtmlFile = matcher.replaceAll("");
        }
        writeFileToDisk(strXhtmlFile, strHtmlFilePath);
    }
}
