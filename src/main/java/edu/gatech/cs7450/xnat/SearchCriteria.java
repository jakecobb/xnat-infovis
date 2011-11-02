package edu.gatech.cs7450.xnat;

/** 
 * Abstract base for search criteria, which are either 
 * {@link SingleCriteria} or {@link SearchWhere}.
 */
public abstract class SearchCriteria {
	/**
	 * Returns whether this is a <code>SearchWhere</code> or <code>SingleCriteria</code> instance.
	 * @return if this is a <code>SearchWhere</code>
	 */
	public abstract boolean isWhere();
}
