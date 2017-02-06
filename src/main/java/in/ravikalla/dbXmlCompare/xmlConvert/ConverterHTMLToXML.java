package in.ravikalla.dbXmlCompare.xmlConvert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class ConverterHTMLToXML
{
    public static void main(final String[] args) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
        generateExcelFileXpathsAndValuesForHtml("C:/HTMLResponse/Report.html", "C:/HTMLResponse/Data/GeneratedFiles/Data.xls");
        generateExcelHtmlMappingFileByXMLfile("C:/UI-XML/Data.xml", "C:/UI-XML/Generated Files/ExcelHtmlMappingFile.xls");
        final String strParams = "1:true;0:false;Hi, Ravi:Hi, Kalla";
        final String strXmlFie = convertExcelHtmlMappinFileToXml("C:/Web Services Automation/Data/GeneratedFiles/TC001_MappingwithHTML.xls", "Sheet1", strParams, "C:/Web Services Automation/Lisa Tests/Data/HTMLResponse/TC001_UI_Service_Validation.html");
        System.out.println("Xml File--->" + strXmlFie);
        final String strXMLFileName1 = "C:/UI-XML/Data.xml";
        final String strXMLFileName2 = "C:/UI-XML/Generated Files/GeneratedXmlFile.xml";
        final String configFilePath = null;
    }
    
    public static void generateExcelFileXpathsAndValuesForHtml(final String strHtmlFilePath, final String strExcleFilePath) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
        final HTMLUtility utility = new HTMLUtility();
        HTMLUtility.getWellformedHtml(strHtmlFilePath);
        HTMLUtility.removeUnnecessaryCodeFromXhtml(strHtmlFilePath);
        final LinkedHashMap<String, String> xpathValueMap = HTMLUtility.getXPathsAndValues(strHtmlFilePath);
        HTMLUtility.createExcleFileByMap(xpathValueMap, strExcleFilePath);
    }
    
    public static void generateExcelFileXpathsAndValuesForXml(final String strXmlFilePath, final String strExcleFilePath) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
        final HTMLUtility utility = new HTMLUtility();
        final LinkedHashMap<String, String> xpathValueMap = HTMLUtility.getXPathsAndValues(strXmlFilePath);
        HTMLUtility.createExcleFileByMap(xpathValueMap, strExcleFilePath);
    }
    
    public static void generateExcelHtmlMappingFileByXMLfile(final String strXmlFilePath, final String strExcelMappingFilePath) throws IOException {
    }
    
    public static String convertExcelHtmlMappinFileToXml(final String excelHtmlMappingFile, final String strSheetName, final String strParams, final String strHtmlFilePath) {
        final String strXML = ConvertHTMLDataToXML.readExcelAndConvertToXML(excelHtmlMappingFile, strSheetName, strParams, strHtmlFilePath);
        return strXML;
    }
}
