package edu.gatech.cs7450.xnat;

/**
 * A single search criteria for nesting in a {@link SearchWhere}.
 */
public class SingleCriteria extends SearchCriteria {
	/**
	 * Comparison operators allowed for each search criteria.
	 */
	public static enum CompareOperator {
		/** Less-than operator. */
		LESS {
			public String toString() { return "<"; }
		},
		/** Less-than-or-equal-to operator. */
		LESS_EQ {
			public String toString() { return "<="; }
		},
		/** Equality operator. */
		EQUAL {
			public String toString() { return "="; }
		},
		/** Greater-than operator. */
		GREATER {
			public String toString() { return ">"; }
		},
		/** Greater-than-or-equal operator. */
		GREATER_EQ {
			public String toString() { return ">="; }
		},
		/** "LIKE" operator, similar meaning to SQL's <code>LIKE</code>. */
		LIKE {
			public String toString() { return "LIKE"; }
		}
	}
	
	/** The field to match. */
	private String schemeField;
	
	/** The comparison operator. */
	private CompareOperator operator;
	
	/** The value to match. */
	private String value;

	/**
	 * Creates a new single-criteria search criteria.
	 * 
	 * @param schemeField the field to match
	 * @param operator    the comparison type
	 * @param value       the value to compare
	 * @throws NullPointerException if any argument is <code>null</code>
	 */
	public SingleCriteria(String schemeField, CompareOperator operator, String value) {
		setSchemeField(schemeField);
		setOperator(operator);
		setValue(value);
	}
	
	/**
	 * Returns the scheme field to match.
	 * @return the scheme field
	 */
	public String getSchemeField() {
		return schemeField;
	}
	
	/**
	 * Sets the scheme field to match.
	 * @param schemeField the scheme field
	 */
	public void setSchemeField(String schemeField) {
		if( schemeField == null ) throw new NullPointerException("schemeField is null");
		this.schemeField = schemeField;
	}
	
	/**
	 * Returns the comparison operator.
	 * @return the comparison operator
	 */
	public CompareOperator getOperator() {
		return operator;
	}
	
	/**
	 * Sets the comparison operator.
	 * @param operator the comparison operator
	 */
	public void setOperator(CompareOperator operator) {
		if( operator == null ) throw new NullPointerException("operator is null");
		this.operator = operator;
	}

	/**
	 * Returns the value.
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value to compare the field with.
	 * @param value the value to compare
	 */
	public void setValue(String value) {
		if( value == null ) throw new NullPointerException("value is null");
		this.value = value;
	}

	@Override 
	public boolean isWhere() { return false; }
}