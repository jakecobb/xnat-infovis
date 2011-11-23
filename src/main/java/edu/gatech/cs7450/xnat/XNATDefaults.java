package edu.gatech.cs7450.xnat;

import java.util.Arrays;
import java.util.List;

public class XNATDefaults {
	public static final String DEFAULT_SEARCH_ROOT = "xnat:mrSessionData";
	public static final List<SearchField> DEFAULT_SEARCH_FIELDS;
	static {
		DEFAULT_SEARCH_FIELDS = Arrays.asList(
			new SearchField("xnat:mrSessionData", "LABEL", "string", "MR ID"),
			new SearchField("xnat:subjectData", "LABEL", "string", "Subject")
		);
	}
	
	
	private XNATDefaults() { }
}
