package edu.gatech.cs7450.xnat;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Cookie;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import edu.gatech.cs7450.Util;


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
		SearchWhere searchWhere = new SearchWhere("AND", Arrays.asList(
			new SearchWhere("OR", Arrays.asList(
				new SingleCriteria("xnat:mrSessionData/sharing/share/project", "=", "CENTRAL_OASIS_CS"),
				new SingleCriteria("xnat:mrSessionData/PROJECT", "=", "CENTRAL_OASIS_CS")
			)),
			new SingleCriteria("xnat:mrSessionData/PROJECT", "LIKE", "a")
		));
		
		System.out.println("MESSAGE: ");
		System.out.println(search.createMessage(rootElement, searchFields, searchWhere));
		System.out.println("RESPONSE: \n");
		System.out.println(search.doSearch(rootElement, searchFields, searchWhere));
	}
}
