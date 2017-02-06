package in.ravikalla.dbXmlCompare.xmlCompareUtil.util.excel;

import java.io.OutputStream;
import java.io.FileOutputStream;
import org.apache.poi.ss.usermodel.Sheet;
import java.io.IOException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import java.io.InputStream;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import java.io.FileInputStream;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelCommon
{
    public static Workbook getWorkBook(final String strInputFile) throws InvalidFormatException, IOException {
        final InputStream inp = new FileInputStream(strInputFile);
        final Workbook wb = WorkbookFactory.create(inp);
        return wb;
    }
    
    public static Sheet getSheet(final Workbook wb, final int intSheetNumber) {
        return wb.getSheetAt(intSheetNumber);
    }
    
    public static Sheet getSheet(final Workbook wb, final String strSheetName) {
        return wb.getSheet(strSheetName);
    }
    
    public static void writeToFile(final Workbook wb, final String strOPFile) throws IOException {
        final FileOutputStream fileOut = new FileOutputStream(strOPFile);
        wb.write((OutputStream)fileOut);
        fileOut.close();
    }
    
    public static void main(final String[] args) {
        try {
            getWorkBook("D:/Projects/SampleProject/ExcellConverter/src/Defect Tracker (Input).xlsx");
        }
        catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
    }
}
