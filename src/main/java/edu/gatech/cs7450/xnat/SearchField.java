package edu.gatech.cs7450.xnat;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.apache.log4j.Logger;

import edu.gatech.cs7450.xnat.XNATConstants.Projects;
import edu.gatech.cs7450.xnat.XNATConstants.Scans;
import edu.gatech.cs7450.xnat.XNATConstants.Sessions;
import edu.gatech.cs7450.xnat.XNATConstants.Subjects;

/**
 * A field to retrieve for search purposes.
 */
public class SearchField implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger _log = Logger.getLogger(SearchField.class);
	
	/**
	 * Tries to find the {@link SearchField} constant in one of the {@link XNATConstants} subclasses 
	 * for the given element.
	 * 
	 * @param elName the element name, should not be <code>null</code>
	 * @param id     the element id, should not be <code>null</code>
	 * @return the search field if found, or <code>null</code> if it could not be determined
	 * @throws NullPointerException if either argument is <code>null</code>
	 */
	public static SearchField findCanonicalSearchField(String elName, String id) {
		if( elName == null ) throw new NullPointerException("elName is null");
		if( id == null ) throw new NullPointerException("id is null");
		
		// find the class
		Class<?> keyClass = null;
		if( Projects.ELEMENT.equalsIgnoreCase(elName) ) {
			keyClass = Projects.class;
		} else if ( Subjects.ELEMENT.equalsIgnoreCase(elName) ) {
			keyClass = Subjects.class;
		} else if ( Sessions.ELEMENT.equalsIgnoreCase(elName) ) {
			keyClass = Sessions.class;
		} else if ( Scans.ELEMENT.equalsIgnoreCase(elName) ) {
			keyClass = Scans.class;
		} else {
			_log.error("Could not match element name: "  + elName);
			return null;
		}
		
		try {
			// expecting id to match the field constant
			Field field = keyClass.getDeclaredField(id);
			return (SearchField)field.get(null);
			
		} catch( NoSuchFieldException e ) {
			_log.error("No field for " + elName + "/" + id, e);
		} catch( ClassCastException e ) {
			_log.error("Field was not a SearchField instance for " + elName + "/" + id, e);
		} catch( SecurityException e ) {
			_log.error("Could not access field for " + elName + "/" + id, e);
		} catch( IllegalAccessException e ) {
			_log.error("Could not access field for " + elName + "/" + id, e);
		}
		return null;
	}
	
	/**
	 * Tries to find the {@link SearchField} constant in one of the {@link XNATConstants} subclasses 
	 * for the given element.
	 * 
	 * @param field the field, something like <code>xnat:mrSessionData/LABEL</code>.
	 * @return the field if found, or <code>null</code> if it could not be determined
	 * @throws IllegalArgumentException if the field is not formatted as expected
	 * @throws NullPointerException if <code>field</code> is <code>null</code>
	 */
	public static SearchField findCanonicalSearchField(String field) {
		if( field == null ) throw new NullPointerException("field is null");
		String[] parts = field.split("/", 2);
		if( parts.length != 2 )
			throw new IllegalArgumentException("Bad format for field: " + field);

		// force second part to match field name convention
		parts[1] = parts[1].toUpperCase().replace('/', '_');
		
		return findCanonicalSearchField(parts[0], parts[1]);
	}
	
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

	/**
	 * Returns the canonical instance of this field, if it exists.
	 * @return the canonical version or <code>null</code> if it doesn't exist
	 */
	public SearchField toCanonical() {
		try {
			return findCanonicalSearchField(elementName, fieldId);
		} catch( NullPointerException e ) {
			_log.error("NPE looking for canonical version of: " + this, e);
			return null;
		}
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