package edu.gatech.cs7450.xnat.gui;

import java.util.Arrays;
import java.util.List;

import javax.swing.SwingWorker;

import edu.gatech.cs7450.xnat.SearchField;
import edu.gatech.cs7450.xnat.SearchWhere;
import edu.gatech.cs7450.xnat.XNATSearch;

public class SearchWorker extends SwingWorker<String, Object> {
	
	private XNATSearch search;
	private String rootElement;
	private List<SearchField> searchFields;
	private SearchWhere searchWhere;
	
	public static final String DEFAULT_ROOT_EL = "xnat:mrSessionData";
	public static final List<SearchField> DEFAULT_SEARCH_FIELDS;
	static {
		DEFAULT_SEARCH_FIELDS = Arrays.asList(
			new SearchField("xnat:mrSessionData", "LABEL", "string", "MR ID"),
			new SearchField("xnat:subjectData", "LABEL", "string", "Subject")
		);
	}
	
	public SearchWorker(XNATSearch search, SearchWhere where) {
		this(search, DEFAULT_ROOT_EL, DEFAULT_SEARCH_FIELDS, where);
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
