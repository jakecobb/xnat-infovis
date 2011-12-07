package edu.gatech.cs7450.xnat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchQuery implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// FIXME temporary
	/** 
	 * Return a search query with default root element and search fields. 
	 * @deprecated Use {@link #SearchQuery(String, SearchWhere)} instead. 
	 */
	@Deprecated
	public static SearchQuery getDefault(String name, SearchWhere where) {
		return new SearchQuery(name, where);
	}
	
	protected String name;
	protected String rootElement;
	protected List<SearchField> searchFields;
	protected SearchWhere searchWhere;
	
	public SearchQuery(String name, String rootElement, List<SearchField> searchFields, SearchWhere searchWhere) {
		this.name = name;
		this.rootElement = rootElement;
		this.searchFields = searchFields;
		this.searchWhere = searchWhere;
	}
	
	public SearchQuery(String name, SearchWhere searchWhere) {
		this(name, XNATDefaults.DEFAULT_SEARCH_ROOT, XNATDefaults.DEFAULT_SEARCH_FIELDS, searchWhere);
	}
	
	public SearchQuery(SearchQuery query) {
		if( query == null ) throw new NullPointerException("query is null");
		name = query.name;
		rootElement = query.rootElement;
		
		// mutable, so copy these
		searchFields = new ArrayList<SearchField>(query.searchFields);
		searchWhere = new SearchWhere(query.searchWhere);
	}
	
	protected SearchQuery() {
		
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRootElement() {
		return rootElement;
	}

	public void setRootElement(String rootElement) {
		this.rootElement = rootElement;
	}

	public List<SearchField> getSearchFields() {
		return Collections.unmodifiableList(searchFields);
	}

	public void setSearchFields(List<SearchField> searchFields) {
		this.searchFields = new ArrayList<SearchField>(searchFields);
	}

	public SearchWhere getSearchWhere() {
		return searchWhere;
	}

	public void setSearchWhere(SearchWhere searchWhere) {
		this.searchWhere = searchWhere;
	}
	
	
}
