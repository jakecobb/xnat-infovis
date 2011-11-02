package edu.gatech.cs7450.xnat;

/**
 * A field to retrieve for search purposes.
 */
public class SearchField {
	private String elementName, fieldId, type, header;

	public SearchField(String elementName, String fieldId, String type, String header) {
		this.elementName = elementName;
		this.fieldId = fieldId;
		this.type = type;
		this.header = header;
	}
	
	public String getFieldId() {
		return fieldId;
	}

	public String getType() {
		return type;
	}

	public String getHeader() {
		return header;
	}

	public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setHeader(String header) {
		this.header = header;
	}
}