package edu.gatech.cs7450.xnat;

import java.io.Serializable;

/** 
 * Abstract base for search criteria, which are either 
 * {@link SingleCriteria} or {@link SearchWhere}.
 */
public abstract class SearchCriteria implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	/**
	 * Returns whether this is a <code>SearchWhere</code> or <code>SingleCriteria</code> instance.
	 * @return if this is a <code>SearchWhere</code>
	 */
	public abstract boolean isWhere();
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
