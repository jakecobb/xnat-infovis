package edu.gatech.cs7450.xnat;

import java.io.Serializable;

/**
 * A field to retrieve for search purposes.
 */
public class SearchField implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String elementName, fieldId, type, header;
	
	/** Human-readable description. */
	private String desc;
	
	/** The "summary" of the field, which appears to be a path such 
	 * as <code>xnat:projectData/meta/insert_date</code>. */
	private String summary;
	
	/** Whether a value is required. */
	private Boolean isValueRequired;
	
	/** The 'SRC' value. */
	private Integer source;

	/**
	 * Creates a search field with the parameters needed for the search engine.
	 * 
	 * @param elementName the element name
	 * @param fieldId     the field id
	 * @param type        the field type
	 * @param header      the header description
	 * @throws NullPointerException if an argument other than <code>header</code> is <code>null</code>
	 */
	public SearchField(String elementName, String fieldId, String type, String header) {
		// accessors for null checks
		setElementName(elementName);
		setFieldId(fieldId);
		setType(type);
		this.header = header;
	}
	
	
	/**
	 * Creates a search field with all available information.
	 * 
	 * @param elementName     the element name
	 * @param fieldId         the field id
	 * @param type            the field type
	 * @param header          the header description
	 * @param desc            a human-readable description
	 * @param summary         the summary, which appears to be an XPATH expression
	 * @param isValueRequired if a value is required for this field
	 * @param source          the 'SRC' attribute
	 */
	public SearchField(String elementName, String fieldId, String type, String header, 
			String desc, String summary, Boolean isValueRequired, Integer source) {
		this(elementName, fieldId, type, header);
		this.desc = desc;
		this.summary = summary;
		this.isValueRequired = isValueRequired;
		this.source = source;
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
		if( elementName == null ) throw new NullPointerException("elementName is null");
		this.elementName = elementName;
	}

	public void setFieldId(String fieldId) {
		if( fieldId == null ) throw new NullPointerException("fieldId is null");
		this.fieldId = fieldId;
	}

	public void setType(String type) {
		if( type == null ) throw new NullPointerException("type is null");
		this.type = type;
	}

	public void setHeader(String header) {
		this.header = header;
	}


	public String getSummary() {
		return summary;
	}


	public void setSummary(String summary) {
		this.summary = summary;
	}


	/**
	 * Returns whether a value is required.
	 * @return if a value is required, or <code>null</code> if unknown
	 */
	public Boolean getIsValueRequired() {
		return isValueRequired;
	}


	public void setIsValueRequired(Boolean isValueRequired) {
		this.isValueRequired = isValueRequired;
	}

	/**
	 * Returns the 'SRC' attribute.
	 * @return the source, or <code>null</code> if unknown
	 */
	public Integer getSource() {
		return source;
	}


	public void setSource(Integer source) {
		this.source = source;
	}


	public String getDesc() {
		return desc;
	}


	public void setDesc(String desc) {
		this.desc = desc;
	}


	@Override
	public String toString() {
		return "SearchField [elementName=" + elementName + ", fieldId=" + fieldId 
			+ ", type=" + type + ", header=" + header + ", desc=" + desc + ", summary="
			+ summary + ", isValueRequired=" + isValueRequired + ", source=" + source + "]";
	}
	
	
}