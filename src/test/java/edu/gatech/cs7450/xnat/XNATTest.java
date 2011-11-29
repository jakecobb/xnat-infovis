package edu.gatech.cs7450.xnat;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jersey.api.client.WebResource;

import edu.gatech.cs7450.Util;
import edu.gatech.cs7450.xnat.SearchWhere.SearchMethod;
import edu.gatech.cs7450.xnat.SingleCriteria.CompareOperator;
import edu.gatech.cs7450.xnat.XNATConstants.Projects;
import edu.gatech.cs7450.xnat.XNATConstants.Sessions;
import edu.gatech.cs7450.xnat.XNATConstants.Subjects;


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
		
		String result = search.doSearch(rootElement, searchFields, searchWhere);
		Assert.assertNotNull("result was null", result);
		Assert.assertFalse("Result was empty.", result.trim().isEmpty());
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
	}
	
	@Test
	public void testFetchElementFields() throws Exception {
		XNATSearch search = new XNATSearch(conn);
		
		List<SearchField> fields = search.fetchElementFields("xnat:projectData");
		Assert.assertNotNull("fields is null", fields);
		Assert.assertFalse("fields is empty", fields.isEmpty());
		
		// enable DEBUG logging to see the fields
	}
	
	@Test
	public void testFetchAllFields() throws Exception {
		XNATMetaData meta = new XNATMetaData(conn);
		
		List<SearchElement> elements = meta.getSearchElements();
		Assert.assertNotNull("elements was null", elements);
		
		List<SearchField> fields = meta.getAllSearchFields();
		Assert.assertNotNull("fields was null", fields);
		Assert.assertTrue("There were fewer fields than elements.", elements.size() < fields.size() );
	}
	
	@Test
	public void testGetHenryFordScans() throws Exception {
		XNATSearch search = new XNATSearch(conn);
		String rootElement = "xnat:mrSessionData";
		List<SearchField> searchFields = Arrays.asList(
			new SearchField("xnat:mrSessionData", "PROJECT", "string", "Project"),
			new SearchField("xnat:mrSessionData", "LABEL", "string", "MR ID"),
			new SearchField("xnat:subjectData", "LABEL", "string", "Subject")
		);
		SearchWhere searchWhere = new SearchWhere(SearchMethod.AND, Arrays.asList(
			new SearchWhere(SearchMethod.OR, Arrays.asList(
				new SingleCriteria("xnat:mrSessionData/sharing/share/project", CompareOperator.EQUAL, "HF_BRN_TUMOR"),
				new SingleCriteria("xnat:mrSessionData/PROJECT", CompareOperator.EQUAL, "HF_BRN_TUMOR")
			))
		));
		
		Object result = search.doSearch(rootElement, searchFields, searchWhere);
		System.out.println("RESULT:\n" + result);
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setNamespaceAware(true);
		DocumentBuilder builder = docFactory.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(result.toString().getBytes("UTF-8")));
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath.compile("//columns/column");
		
		System.out.println("COLS:");
		NodeList cols = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
		for( int i = 0, ilen = cols.getLength(); i < ilen; ++i ) {
			Node node = cols.item(i);
			System.out.println(node);
			System.out.println(node.getTextContent());
		}
		
		expr = xpath.compile("//rows/row/cell[position()=2 or position()=3]");
		NodeList data = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
		
		System.out.println("DATA:");
		for( int i = 0, ilen = data.getLength(); i < ilen; ++i ) {
			String text = data.item(i).getTextContent();
			if( (i & 1) != 0 )
				System.out.print("ID: " + text);
			else
				System.out.println(" Session: " + text);
		}
	}
	
	@Test
	public void testFetchProjects() throws Exception {
		XNATSearch search = new XNATSearch(conn);
		
		XNATTableResult result = search.fetchProjects();
		Assert.assertNotNull("result was null", result);
		Assert.assertEquals("Expected and actual headers do not match.", Projects.COLUMNS, result.getHeaders());	
	}
	
	@Test
	public void testFetchExperiments() throws Exception {
		XNATSearch search = new XNATSearch(conn);
		
		XNATTableResult result = search.fetchExperiments();
		Assert.assertNotNull("result was null", result);
		Assert.assertEquals("Expected and actual headers do not match.", Sessions.COLUMNS, result.getHeaders());			
	}
	
	@Test
	public void testFetchSubjects() throws Exception {
		XNATSearch search = new XNATSearch(conn);
		
		XNATTableResult result = search.fetchSubjects();
		Assert.assertNotNull("result was null", result);
		Assert.assertEquals("Expected and actual headers do not match.", Subjects.COLUMNS, result.getHeaders());	
	}
	
	@Test
	public void testFetchAllScans() throws Exception {
		XNATSearch search = new XNATSearch(conn);
		
		XNATTableResult result = search.fetchScans(null);
		Assert.assertNotNull("result was null", result);
	}
}
