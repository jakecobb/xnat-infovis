package edu.gatech.cs7450.xnat.gui;

import java.util.List;

import javax.swing.SwingWorker;

import edu.gatech.cs7450.xnat.SearchField;
import edu.gatech.cs7450.xnat.SearchWhere;
import edu.gatech.cs7450.xnat.XNATDefaults;
import edu.gatech.cs7450.xnat.XNATSearch;

public class SearchWorker extends SwingWorker<String, Object> {
	
	private XNATSearch search;
	private String rootElement;
	private List<SearchField> searchFields;
	private SearchWhere searchWhere;
	
	public SearchWorker(XNATSearch search, SearchWhere where) {
		this(search, XNATDefaults.DEFAULT_SEARCH_ROOT, XNATDefaults.DEFAULT_SEARCH_FIELDS, where);
	}

	public SearchWorker(XNATSearch search, String rootElement, List<SearchField> searchFields, SearchWhere searchWhere) {
		if( search == null ) throw new NullPointerException("search is null");
		if( rootElement == null ) throw new NullPointerException("rootElement is null");
		if( searchFields == null ) throw new NullPointerException("searchFields is null");
		if( searchWhere == null ) throw new NullPointerException("searchWhere is null");
		
		this.search = search;
		this.rootElement = rootElement;
		this.searchFields = searchFields;
		this.searchWhere = searchWhere;
	}



	@Override
	protected String doInBackground() throws Exception {
		return search.doSearch(rootElement, searchFields, searchWhere);
	}

}
