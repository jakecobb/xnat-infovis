package edu.gatech.cs7450.xnat;

import javax.ws.rs.core.Cookie;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/** Connection helper for XNAT. */
public class XNATConnection {
	private String hostBase;
	private String user;
	private String pass;
	private Client client;
	private Cookie sessCookie;
	
	public XNATConnection(String hostBase, String user, String pass) {
		if( hostBase == null ) throw new NullPointerException("hostBase is null");
		this.hostBase = hostBase;
		this.user = user;
		this.pass = pass;
		
		client = Client.create();
		client.addFilter(new HTTPBasicAuthFilter(user, pass));
	}
	
	/**
	 * Get a resource builder, authenticating and adding the session cookie first.
	 * 
	 * @param path the resource path, which will be appended to the host base
	 * @return
	 */
	public WebResource.Builder resource(String path) {
		maybeAuthenticate();
		return client.resource(hostBase + path).cookie(sessCookie);
	}
	
	/**
	 * (Re)authenticate and store the new session cookie.
	 */
	public void authenticate() {
		ClientResponse response = client.resource(hostBase + "/JSESSION").get(ClientResponse.class);
		for( Cookie cookie : response.getCookies() ) {
			if( "JSESSIONID".equals(cookie.getName()) ) {
				sessCookie = cookie;
				return;
			}
		}
		// FIXME better exception type
		throw new RuntimeException("JSESSIONID cookie not found, response: " + response);
	}
	
	/**
	 * Authenticate only if there's no cookie currently stored.
	 */
	public void maybeAuthenticate() {
		if( sessCookie == null )
			authenticate();
	}
}
