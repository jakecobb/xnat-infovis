package edu.gatech.cs7450.xnat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XNATMetaData {
	private XNATConnection conn;
	private List<SearchElement> searchElements;
	
	private Map<String, List<SearchField>> elToFields = 
		new HashMap<String, List<SearchField>>();
	
	private XNATSearch search;
	
	public XNATMetaData(XNATConnection conn) {
		if( conn == null ) throw new NullPointerException("search is null");
		this.conn = conn;
		search = new XNATSearch(conn);
	}
	
	public List<SearchElement> getSearchElements() throws XNATException {
		if( searchElements == null )
			searchElements = search.fetchSearchableElements();
		return Collections.unmodifiableList(searchElements);
	}
	
	public List<SearchField> getSearchFields(String elementName) throws XNATException {
		if( elementName == null ) throw new NullPointerException("elementName is null");
		
		List<SearchField> fields = elToFields.get(elementName);
		if( fields == null ) {
			fields = search.fetchElementFields(elementName);
			elToFields.put(elementName, fields);
		}
		
		return Collections.unmodifiableList(fields);
	}
	
	public List<SearchField> getAllSearchFields() throws XNATException {
		ArrayList<SearchField> fields = new ArrayList<SearchField>();
		for( SearchElement el : getSearchElements() )
			for( SearchField f : getSearchFields(el.getName()) )
				fields.add(f);
		return fields;
	}
}
