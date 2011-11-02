package edu.gatech.cs7450.xnat;

/**
 * A single search criteria for nesting in a {@link SearchWhere}.
 */
public class SingleCriteria extends SearchCriteria {
	private String schemeField, compType, value;

	public SingleCriteria(String schemeField, String compType, String value) {
		this.schemeField = schemeField;
		this.compType = compType;
		this.value = value;
	}
	
	public String getSchemeField() {
		return schemeField;
	}
	public void setSchemeField(String schemeField) {
		this.schemeField = schemeField;
	}

	public String getCompType() {
		return compType;
	}

	public void setCompType(String compType) {
		this.compType = compType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override 
	public boolean isWhere() { return false; }
}