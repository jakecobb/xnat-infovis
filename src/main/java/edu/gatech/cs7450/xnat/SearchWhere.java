package edu.gatech.cs7450.xnat;

import java.util.Collection;

/**
 * A search clause, which consists of a method ('AND' or 'OR') and a 
 * collection of child criteria.
 */
public class SearchWhere extends SearchCriteria {
	
	private String method;
	private Collection<? extends SearchCriteria> criteria;
	
	public SearchWhere(String method, Collection<? extends SearchCriteria> criteria) {
		if( method == null )   throw new NullPointerException("method is null");
		if( criteria == null ) throw new NullPointerException("criteria is null");
		
		this.method = method;
		this.criteria = criteria;
	}

	@Override
	public boolean isWhere() {
		return true;
	}
	
	public String getMethod() {
		return method;
	}

	public Collection<? extends SearchCriteria> getCriteria() {
		return criteria;
	}

	public void setCriteria(Collection<? extends SearchCriteria> criteria) {
		if( criteria == null ) throw new NullPointerException("criteria is null");
		this.criteria = criteria;
	}

	public void setMethod(String method) {
		this.method = method;
	}
}
