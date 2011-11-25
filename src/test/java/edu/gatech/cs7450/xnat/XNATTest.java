package edu.gatech.cs7450.xnat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.WebResource;

import edu.gatech.cs7450.Util;
import edu.gatech.cs7450.xnat.SearchWhere.SearchMethod;
import edu.gatech.cs7450.xnat.SingleCriteria.CompareOperator;


public class XNATTest {
	private static final String 
//		HOST = "xnat.cci.psy.emory.edu:8080",
		HOST = "node18.cci.emory.edu:8080",
		HOST_BASE = "http://" + HOST + "/xnat/REST",
		USER = "nbia",
		PASS = "nbia";
	
	private static XNATConnection conn;
	
	@BeforeClass
	public static void beforeClass() {
		conn = new XNATConnection(HOST_BASE, USER, PASS);
	}
	
	@AfterClass
	public static void afterClass() {
		conn = null;
	}
	
	@Test
	public void testBasicRequest() throws Exception {
		WebResource.Builder res = conn.resource("/projects?format=xml");
		
		String response = res.accept("text/xml", "application/xml").get(String.class);
		System.out.println("Projects:\n" + response);
	}
	
	@Test
	public void testSearchRequest() throws Exception {
		WebResource.Builder res = conn.resource("/search?format=xml");
		
		String searchXML = Util.classString(XNATTest.class, "/xnat_example_search.xml");
		
		String response = 
			res.accept("text/xml", "application/xml")
			.entity(searchXML, "text/xml")
			.post(String.class);
		
		System.out.println(response);
	}
	
	@Test
	public void testGeneratedSearch() throws Exception {
		XNATSearch search = new XNATSearch(conn);
		String rootElement = "xnat:mrSessionData";
		List<SearchField> searchFields = Arrays.asList(
			new SearchField("xnat:mrSessionData", "LABEL", "string", "MR ID"),
			new SearchField("xnat:subjectData", "LABEL", "string", "Subject")
		);
		SearchWhere searchWhere = new SearchWhere(SearchMethod.AND, Arrays.asList(
			new SearchWhere(SearchMethod.OR, Arrays.asList(
				new SingleCriteria("xnat:mrSessionData/sharing/share/project", CompareOperator.EQUAL, "CENTRAL_OASIS_CS"),
				new SingleCriteria("xnat:mrSessionData/PROJECT", CompareOperator.EQUAL, "CENTRAL_OASIS_CS")
			)),
			new SingleCriteria("xnat:mrSessionData/PROJECT", CompareOperator.LIKE, "a")
		));
		
		System.out.println("MESSAGE: ");
		System.out.println(search.createMessage(rootElement, searchFields, searchWhere));
		System.out.println("RESPONSE: \n");
		System.out.println(search.doSearch(rootElement, searchFields, searchWhere));
	}
	
	@Test
	public void testFetchSearchElements() throws Exception {
		XNATSearch search = new XNATSearch(conn);
		
		List<SearchElement> elements = search.fetchSearchableElements();
		Assert.assertNotNull("elements is null", elements);
		
		HashSet<String> names = new HashSet<String>( (int)(1.5f * elements.size()) );
		for( SearchElement el : elements ) {
			Assert.assertNotNull("elements contained null", el);
			names.add(el.getName());
		}
		
		// check for a couple expected entries
		for( String name : Arrays.asList("xnat:subjectData", "xnat:projectData", "xnat:mrSessionData") ) {
			if( !names.contains(name) )
				Assert.fail("Missing expected search element: " + name);
		}
		
		System.out.println("ELEMENTS:\n");
		for( SearchElement el : elements )
			System.out.println(el);
	}
	
	@Test
	public void testFetchElementFields() throws Exception {
		XNATSearch search = new XNATSearch(conn);
		
		List<SearchField> fields = search.fetchElementFields("xnat:projectData");
		Assert.assertNotNull("fields is null", fields);
		Assert.assertFalse("fields is empty", fields.isEmpty());
		
		System.out.println("FIELDS:");
		for( SearchField field : fields )
			System.out.println(field);
	}
}
