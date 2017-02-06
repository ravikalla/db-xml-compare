package in.ravikalla.dbXmlCompare.xmlCompareUtil;

import java.io.Reader;
import au.com.bytecode.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CompareHTMLAndXML
{
    static List<String> lstMatchedDataForCSV;
    static List<String> lstMismatchedDataForCSV;
    
    static {
        CompareHTMLAndXML.lstMatchedDataForCSV = new LinkedList<String>();
        CompareHTMLAndXML.lstMismatchedDataForCSV = new LinkedList<String>();
    }
    
    public static void main(final String[] args) throws IOException {
        compareHTMLAndXMLData("C:\\Compare\\HTML TEST\\Service_Validation_Out1.csv", "C:\\HTML TEST\\TC001_Service_Validation_Out.xls");
    }
    
    public static void compareHTMLAndXMLData(final String csvDataFile, final String strComparisonResultsFile) throws IOException {
        final CSVReader reader = new CSVReader((Reader)new FileReader(csvDataFile));
        try {
	        String[] nextLine;
	        while ((nextLine = reader.readNext()) != null) {
	            nextLine[3] = ((nextLine[3] == null) ? "" : nextLine[3]);
	            nextLine[5] = ((nextLine[5] == null) ? "" : nextLine[5]);
	            if (nextLine[3].toString().equalsIgnoreCase(nextLine[5])) {
	                CompareHTMLAndXML.lstMatchedDataForCSV.add(String.valueOf(nextLine[2]) + "," + nextLine[3] + "," + nextLine[4] + "," + nextLine[5]);
	            }
	            else {
	                CompareHTMLAndXML.lstMismatchedDataForCSV.add(String.valueOf(nextLine[2]) + "," + nextLine[3] + "," + nextLine[4] + "," + nextLine[5]);
	            }
	        }
        }
        finally {
        	if (null != reader)
        		reader.close();
        }
        XMLDataConverter.printResultsToFile(strComparisonResultsFile, CompareHTMLAndXML.lstMatchedDataForCSV, CompareHTMLAndXML.lstMismatchedDataForCSV);
    }
}
