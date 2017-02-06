package in.ravikalla.dbXmlCompare.xmlCompareUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import in.ravikalla.dbXmlCompare.xmlCompareUtil.dto.XMLToXMLComparisonResultsHolderDTO;
import in.ravikalla.dbXmlCompare.xmlCompareUtil.util.CommonUtil;

public class CompareXMLAndXML
{
    public static void main(final String[] args) throws IOException {
        testCompareXMLAndXML_WriteResults();
    }
    
    public static void testCompareXMLAndXML_WriteResults() throws IOException {
        System.out.println("@testing");
        final String strXMLFileName1 = "C:/data/xml1.xml";
        final String strXMLFileName2 = "C:/data/xml2.xml";
        String xmlStr1 = CommonUtil.readDataFromFile(strXMLFileName1);
        String xmlStr2 = CommonUtil.readDataFromFile(strXMLFileName2);
//        final ResponseAutomationProcess automationProcess = new ResponseAutomationProcess();
//        final String strIterateAndExcludeElements = automationProcess.getIterateElementsAndExcludeElements("F:/Bigfix/Abhinav/Test/POST _remittances_save-validate-list.csv");
//        final String[] strIterateAndExcludeElementsArray = strIterateAndExcludeElements.split("&");
        String strIterativeElement = null;
        String strElementsToExclude = null;
        xmlStr1 = removeXmlStringNamespaceAndPreamble(xmlStr1);
        xmlStr1 = removeSOAPENVPrefix(xmlStr1);
        System.out.println("288 : " + xmlStr1);
        xmlStr2 = removeXmlStringNamespaceAndPreamble(xmlStr2);
        xmlStr2 = removeSOAPENVPrefix(xmlStr2);
//        if (strIterateAndExcludeElementsArray.length > 1) {
//            strIterativeElement = strIterateAndExcludeElementsArray[0];
//            strElementsToExclude = strIterateAndExcludeElementsArray[1];
//        }
//        else {
            strIterativeElement = ConvertXMLToFullPathInCSV.getFirstLevelOfRepeatingElements(xmlStr1, xmlStr2, ";");
//        }
        strElementsToExclude = null;
        strElementsToExclude = "/ContributionListResponse/ActiveInstitutionRemittanceList/ListHistory;/ContributionListResponse/ClosedInstitutionRemittanceList/LockingUser;/ContributionListResponse/ActiveInstitutionRemittanceList/LockingUser;/ContributionListResponse/ResponseStatus/StatusText;/ContributionListResponse/ResponseStatus/Messages/Message/Text;/ContributionListResponse/ActiveInstitutionRemittanceList/LockedStatus;/ContributionListResponse/ActiveInstitutionRemittanceList/EnrollmentAllowed;/ContributionListResponse/ActiveInstitutionRemittanceList/ListHistory/DaysPastDue;/ContributionListResponse/ActiveInstitutionRemittanceList/ListHistory/LastSavedDate;/ContributionListResponse/ActiveInstitutionRemittanceList/LockingUser/UserID;/ContributionListResponse/ActiveInstitutionRemittanceList/LockingUser/UserFirstName;/ContributionListResponse/ActiveInstitutionRemittanceList/LockingUser/UserLastName;/ContributionListResponse/ActiveInstitutionRemittanceList/LockingUser/TypeOfUser;/ContributionListResponse/ClosedInstitutionRemittanceList/LockedStatus;/ContributionListResponse/ClosedInstitutionRemittanceList/EnrollmentAllowed;/ContributionListResponse/ClosedInstitutionRemittanceList/ListHistory/DaysPastDue;/ContributionListResponse/ClosedInstitutionRemittanceList/ListHistory/SubmittedDate;/ContributionListResponse/ClosedInstitutionRemittanceList/ListHistory/StatusDate;/ResponseStatus/StatusText;/ResponseStatus/Messages/Message/Text;/ContributionListResponse/ClosedInstitutionRemittanceList/LockingUser/UserID;/ContributionListResponse/ClosedInstitutionRemittanceList/LockingUser/UserFirstName;/ContributionListResponse/ClosedInstitutionRemittanceList/LockingUser/UserLastName;/ContributionListResponse/ClosedInstitutionRemittanceList/LockingUser/TypeOfUser;/ContributionListResponse/ClosedInstitutionRemittanceList/ListHistory;";
        strIterativeElement = ConvertXMLToFullPathInCSV.getFirstLevelOfRepeatingElements(xmlStr1, xmlStr2, ";");
        final String strComparisonResultsFile = "C:/data/xmltoXML/ComparisonResultsFile_Test_1.xls";
        final String strPrimaryNodeXMLElementName = null;
        final String strTrimElements = null;
        final boolean testResult = compareXMLAndXML_WriteResults(strComparisonResultsFile, xmlStr1, xmlStr2, strIterativeElement, strElementsToExclude, strPrimaryNodeXMLElementName, strTrimElements);
        System.out.println("testResult------------>" + testResult);
    }
    
    public static boolean compareXMLAndXML_WriteResults(final String strComparisonResultsFile, String xmlStr1, String xmlStr2, final String strIterativeElement, final String strElementsToExclude, final String strPrimaryNodeXMLElementName, final String strTrimElements) {
        xmlStr1 = replaceEscapes(xmlStr1);
        xmlStr2 = replaceEscapes(xmlStr2);
        printParametersOfXMLToXMLComparison(strComparisonResultsFile, xmlStr1, xmlStr2, strIterativeElement, strElementsToExclude, strPrimaryNodeXMLElementName, strTrimElements);
        boolean blnDifferencesExist = false;
        try {
            final String[] arrIterativeElement = strIterativeElement.split(";");
            System.out.println("59 : length : " + arrIterativeElement.length);
            XMLToXMLComparisonResultsHolderDTO objXMLToXMLComparisonResultsHolderDTO = null;
            final List<String> lstMatchedDataForCSV = new ArrayList<String>();
            final List<String> lstMismatchedDataForCSV = new ArrayList<String>();
            lstMatchedDataForCSV.add("Expected XPath,Expected Data,Actual Data");
            lstMismatchedDataForCSV.add("Expected Path,Expected Data,Actual Path,Actual Data");
            String[] arrElementsToExclude = null;
            if (strElementsToExclude != null && strElementsToExclude.trim().length() > 0) {
                arrElementsToExclude = strElementsToExclude.split(";");
            }
            for (int intCtr = 0; intCtr < arrIterativeElement.length; ++intCtr) {
                final String strIterationElement = arrIterativeElement[intCtr];
                if (!strIterationElement.equalsIgnoreCase("")) {
                    objXMLToXMLComparisonResultsHolderDTO = XMLDataConverter.compareXPathElementsData_WithChildElements(xmlStr1, xmlStr2, arrIterativeElement[intCtr], arrElementsToExclude, strPrimaryNodeXMLElementName, strTrimElements);
                    lstMatchedDataForCSV.addAll(objXMLToXMLComparisonResultsHolderDTO.lstMatchedDataForCSV);
                    lstMismatchedDataForCSV.addAll(objXMLToXMLComparisonResultsHolderDTO.lstMismatchedDataForCSV);
                }
            }
            System.out.println("lstMismatchedDataForCSV size------------->" + lstMismatchedDataForCSV.size());
            if (lstMismatchedDataForCSV.size() > 1) {
                blnDifferencesExist = true;
            }
            XMLDataConverter.printResultsToFile(strComparisonResultsFile, lstMatchedDataForCSV, lstMismatchedDataForCSV);
        }
        catch (SAXException e) {
            System.out.println("56 : CompareXMLAndXMl.compareXMLAndXML_WriteResults(...) : SAXException : " + e);
        }
        catch (IOException e2) {
            System.out.println("56 : CompareXMLAndXMl.compareXMLAndXML_WriteResults(...) : IOException : " + e2);
        }
        catch (ParserConfigurationException e3) {
            System.out.println("81 : CompareXMLAndXMl.compareXMLAndXML_WriteResults(...) : ParserConfigurationException : " + e3);
        }
        return !blnDifferencesExist;
    }
    
    private static String replaceEscapes(String xmlStr1) {
        xmlStr1 = xmlStr1.replaceAll("&lt;", "<").replaceAll("<\\?.*?\\?>", "");
        xmlStr1 = xmlStr1.replaceAll("&gt;", ">").replaceAll("<\\?.*?\\?>", "");
        return xmlStr1;
    }
    
    public static int eligibleNodeForDisplayInReport(final String strXpath, final String[] excludeXpaths) {
        int isEligibleForDisplay = -1;
        if (excludeXpaths != null) {
            for (final String strTempElementToExclude : excludeXpaths) {
                if (strXpath.indexOf(strTempElementToExclude) != -1) {
                    isEligibleForDisplay = 0;
                }
            }
        }
        return isEligibleForDisplay;
    }
    
    public static void printParametersOfXMLToXMLComparison(final String strComparisonResultsFile, final String xmlStr1, final String xmlStr2, final String strIterativeElement, final String strElementsToExclude, final String strPrimaryNodeXMLElementName, final String strTrimElements) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(String.valueOf(strComparisonResultsFile) + "_Params");
            out.println(String.valueOf(strComparisonResultsFile) + "\n" + xmlStr1 + "\n" + xmlStr2 + "\n@strIterativeElement" + strIterativeElement + "\n@strElementsToExclude" + strElementsToExclude + "\n" + strPrimaryNodeXMLElementName + "\n" + strTrimElements);
        }
        catch (FileNotFoundException e) {
            System.out.println("315 : printParametersOfXMLToXMLComparison(...) : FileNotFoundException : " + e);
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
    
    public static String removeXmlStringNamespaceAndPreamble(String xmlString) {
        xmlString = xmlString.replaceAll("(<\\?[^<]*\\?>)?", "").replaceAll("xmlns.*?(\"|').*?(\"|')", "");
        for (int i = 1; i < 5; ++i) {
            xmlString = xmlString.replaceAll("(<)(\\w+\\-)(.*?>)", "$1$3");
            xmlString = xmlString.replaceAll("(</)(\\w+\\-)(.*?>)", "$1$3");
        }
        xmlString = xmlString.replaceAll("(<)(\\w+:)(.*?>)", "$1$3");
        xmlString = xmlString.replaceAll("(</)(\\w+:)(.*?>)", "$1$3");
        xmlString = xmlString.replaceAll("[a-z]+[:]|[a-z]+[0-9][:]", "");
        return xmlString;
    }
    
    public static String removeSOAPENVPrefix(final String xmlString) {
        return xmlString.replaceAll("SOAP-ENV:", "");
    }
    
    public static String getFirstLevelOfRepeatingElements(final String strXMLData1, final String strXMLData2, final String strSeparator) {
        return ConvertXMLToFullPathInCSV.getFirstLevelOfRepeatingElements(strXMLData1, strXMLData2, strSeparator);
    }
}
