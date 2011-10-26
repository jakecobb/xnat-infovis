package edu.gatech.cs7450.xnat;

import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.Cookie;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;


public class XNATTest {
	private static final String 
		HOST_BASE = "http://xnat.cci.psy.emory.edu:8080/xnat/REST",
		USER = "nbia",
		PASS = "nbia";
	
	private static Client restClient;
	private static Cookie sessCookie;
	
	@BeforeClass
	public static void beforeClass() {
		restClient = Client.create();
		restClient.addFilter(new HTTPBasicAuthFilter(USER, PASS));
		sessCookie = getSessionCookie();
	}
	
	@AfterClass
	public static void afterClass() {
		restClient = null;
		sessCookie = null;
	}
	
	private static Cookie getSessionCookie() {
		ClientResponse response = restClient.resource(HOST_BASE + "/JSESSION").get(ClientResponse.class);
		for( Cookie cookie : response.getCookies() )
			if( "JSESSIONID".equals(cookie.getName()) )
				return cookie;
		throw new RuntimeException("JESSIONID cookie not found, response: " + response);
	}
	
	@Test
	public void testBasicRequest() throws Exception {
		Client client = Client.create();
		WebResource res = client.resource(HOST_BASE + "/projects?format=xml");
		
		String response = 
			res.accept("text/xml", "application/xml")
			.cookie(sessCookie)
			.get(String.class);
		
		System.out.println(response);
	}
}
