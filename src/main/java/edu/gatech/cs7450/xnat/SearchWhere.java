package edu.gatech.cs7450.xnat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A search clause, which consists of a method ('AND' or 'OR') and a 
 * collection of child criteria.
 */
public class SearchWhere extends SearchCriteria {
	private static final long serialVersionUID = 1L;
	
	/** The logical operator for combining child criteria of a <code>SearchWhere</code> group. */
	public static enum SearchMethod {
		/** Logical AND of the search criteria. */
		AND {
			public String toString() { return "AND"; }
		},
		/** Logical OR of the search criteria. */
		OR {
			public String toString() { return "OR"; }
		}
	}
	
	/** The logical operator for child criteria. */
	private SearchMethod method;
	
	/** The child criteria. */
	private List<SearchCriteria> criteria;
	
	/**
	 * Creates an <code>AND</code> group with no initial criteria.
	 */
	public SearchWhere() {
		this(SearchMethod.AND);
	}
	
	/**
	 * Creates a new search group.
	 * 
	 * @param method    the search method (logical AND or OR)
	 * @param criteria the child criteria (will be copied)
	 */
	public SearchWhere(SearchMethod method, SearchCriteria... criteria) {
		this(method, Arrays.asList(criteria));
	}
	
	/**
	 * Creates a new search group.
	 * 
	 * @param method    the search method (logical AND or OR)
	 * @param criteria the child criteria (will be copied)
	 */
	public SearchWhere(SearchMethod method, Collection<? extends SearchCriteria> criteria) {
		setMethod(method);
		setCriteria(criteria);
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param where the instance to copy, may not be <code>null</code>
	 */
	public SearchWhere(SearchWhere where) {
		if( where == null ) throw new NullPointerException("where is null");
		setMethod(where.method);
		this.criteria = new ArrayList<SearchCriteria>(where.criteria.size());
		for( SearchCriteria c : where.criteria ) {
			// reflection would be more general but more expensive
			if( c instanceof SingleCriteria )
				this.criteria.add(new SingleCriteria((SingleCriteria)c));
			else if ( c instanceof SearchWhere )
				this.criteria.add(new SearchWhere((SearchWhere)c));
			else
				assert false : "FIXME: Unexpected subclass in copy constructor.";
		}
	}

	@Override
	public boolean isWhere() {
		return true;
	}

	/**
	 * Adds a new criteria to the group. 
	 * @param criteria the new criteria
	 */
	public void addCriteria(SearchCriteria criteria) {
		if( criteria == null ) throw new NullPointerException("criteria is null");
		this.criteria.add(criteria);
	}
	
	/**
	 * Removes a criteria from the group, if present.
	 * @param criteria the criteria
	 */
	public void removeCriteria(SearchCriteria criteria) {
		if( criteria == null ) throw new NullPointerException("criteria is null");
		this.criteria.remove(criteria);
	}

	/**
	 * Returns an unmodifiable view of the criteria.
	 * @return the criteria
	 */
	public List<SearchCriteria> getCriteria() {
		return Collections.unmodifiableList(criteria);
	}

	/**
	 * Sets the children criteria.  The criteria are copied and subsequent 
	 * changes will not be reflected.
	 * 
	 * @param criteria the criteria
	 * @throws NullPointerException if <code>criteria</code> is <code>null</code>
	 */
	public void setCriteria(Collection<? extends SearchCriteria> criteria) {
		if( criteria == null ) throw new NullPointerException("criteria is null");
		this.criteria = new ArrayList<SearchCriteria>(criteria);
	}
	
	/**
	 * Returns the search method (logical AND or OR) of the criteria.
	 * @return the search method
	 */
	public SearchMethod getMethod() {
		return method;
	}

	/**
	 * Sets the search method (logical AND or OR). 
	 * 
	 * @param method the search method
	 * @throws NullPointerException if <code>method</code> is <code>null</code>
	 */
	public void setMethod(SearchMethod method) {
		if( method == null ) throw new NullPointerException("method is null");
		this.method = method;
	}
	
	@Override
	public String toString() {
		return "SearchWhere(" + method + " {" + criteria + "})";
	}
	
// Cloneable
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		SearchWhere clone = (SearchWhere)super.clone();
		
		ArrayList<SearchCriteria> criteria = new ArrayList<SearchCriteria>(this.criteria.size());
		for( SearchCriteria c : this.criteria ) {
			criteria.add((SearchCriteria)c.clone());
		}
		clone.criteria = criteria;
		
		return clone;
	}
}
