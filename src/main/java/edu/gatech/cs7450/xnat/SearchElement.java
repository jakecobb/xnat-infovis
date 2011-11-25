package edu.gatech.cs7450.xnat;

import java.io.Serializable;

/**
 * A search element, as returned by the <code>search/elements</code> REST call.
 */
public class SearchElement implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/** The element name. */
	private String name;
	/** Human-readable singular form. */
	private String singular;
	/** Human-readable plural form. */
	private String plural;
	/** If the element is secured or open. */
	private boolean isSecured;
	/** FIXME The number of such elements in the database? */
	private int count;

	/**
	 * Create a search element with a name only.
	 * @param name the name
	 */
	public SearchElement(String name) {
		setName(name); // accessor for the null check
	}
	
	/**
	 * Creates a search element.
	 * 
	 * @param name      the element name, may <strong>not</strong> be <code>null</code>
	 * @param singular  human-readable singular form, may be <code>null</code>
	 * @param plural    human-readable plural form, may be <code>null</code>
	 * @param isSecured if the element is secure or not
	 * @param count     <strong>FIXME</strong> number of elements in the database?
	 */
	public SearchElement(String name, String singular, String plural, boolean isSecured, int count) {
		this(name);
		this.singular = singular;
		this.plural = plural;
		this.isSecured = isSecured;
		this.count = count;
	}
	
	
	public String getSingular() {
		return singular;
	}

	public void setSingular(String singular) {
		this.singular = singular;
	}

	public String getPlural() {
		return plural;
	}

	public void setPlural(String plural) {
		this.plural = plural;
	}

	public boolean isSecured() {
		return isSecured;
	}

	public void setSecured(boolean isSecured) {
		this.isSecured = isSecured;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		if( name == null ) throw new NullPointerException("name is null");
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "SearchElement [name=" + name + ", singular=" + singular + ", plural=" + plural 
			+ ", secure=" + isSecured + ", count=" + count + "]";
	}
}