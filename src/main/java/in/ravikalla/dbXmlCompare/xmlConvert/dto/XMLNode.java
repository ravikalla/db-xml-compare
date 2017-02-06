package in.ravikalla.dbXmlCompare.xmlConvert.dto;

import java.util.ArrayList;
import java.util.List;

public class XMLNode
{
    public String strElementName;
    public int intElementType;
    public String strElementValue;
    public String strSQLQuery;
    public String strColumnName;
    public List<XMLNode> lstChildNodes;
    
    public XMLNode() {
        this.strElementName = "";
        this.intElementType = -1;
        this.strElementValue = "";
        this.strSQLQuery = "";
        this.strColumnName = "";
        this.lstChildNodes = new ArrayList<XMLNode>();
    }
    
    public XMLNode clone() {
        final XMLNode objNode = new XMLNode();
        objNode.strElementName = this.strElementName;
        objNode.intElementType = this.intElementType;
        objNode.strElementValue = this.strElementValue;
        objNode.strSQLQuery = this.strSQLQuery;
        objNode.strColumnName = this.strColumnName;
        final List<XMLNode> newLstChildNodes = new ArrayList<XMLNode>();
        if (this.lstChildNodes != null) {
            for (final XMLNode objChildXMLNode : this.lstChildNodes) {
                newLstChildNodes.add(objChildXMLNode.clone());
            }
        }
        objNode.lstChildNodes = newLstChildNodes;
        return objNode;
    }
}
