package in.ravikalla.dbXmlCompare.xmlCompareUtil.dto;


import java.util.List;
import java.util.Map;

public class MappingDataDTO
{
    public Map<String, String> mapCursorRepeatableElement;
    public Map<String, List<String>> mapCursorSpecificElements;
    public Map<String, Map<String, String>> mapElementToDB;
    public Map<String, String> mapDataSheetFormatForComparison;
    public Map<String, String> mapFormatSheetInfo;
    public Map<String, String> mapDataSheetLookupForConversion;
    public Map<String, Map<String, String>> mapDBLookup;
    public Map<String, Map<String, String>> mapWSLookup;
    
    public MappingDataDTO() {
        this.mapCursorRepeatableElement = null;
        this.mapCursorSpecificElements = null;
        this.mapElementToDB = null;
        this.mapDataSheetFormatForComparison = null;
        this.mapFormatSheetInfo = null;
        this.mapDataSheetLookupForConversion = null;
        this.mapDBLookup = null;
        this.mapWSLookup = null;
    }
}
