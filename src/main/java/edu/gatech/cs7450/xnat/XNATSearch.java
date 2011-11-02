package edu.gatech.cs7450.xnat;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;

import com.sun.jersey.api.client.WebResource;

import edu.gatech.cs7450.Util;

/** xNAT search engine interface. */
public class XNATSearch {
	/** Keys used by the template. */
	static class Keys {
		static final String 
			ROOT_ELEMENT  = "rootElement",
			SEARCH_FIELDS = "searchFields",
			SEARCH_WHERE  = "searchCriteria";
	}
	/** The velocity template instance. */
	private static Template template;
	
	static {
		// initialize velocity and load the template
		try {
			Properties p = new Properties();
			p.setProperty("resource.loader", "string");
			p.setProperty("string.resource.loader.class", StringResourceLoader.class.getName());
			Velocity.init(p);
			StringResourceLoader.getRepository()
				.putStringResource("/xnat_search.vel", Util.classString(XNATSearch.class, "/xnat_search.vel"), "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		template = Velocity.getTemplate("/xnat_search.vel", "UTF-8");		
	}

	/** The server connection helper. */
	private XNATConnection connection;
	
	public XNATSearch(XNATConnection connection) {
		if( connection == null ) throw new NullPointerException("connection is null");
		this.connection = connection;
	}
	
	String createMessage(String rootElement, Collection<SearchField> fields, SearchWhere where) {
		VelocityContext context = new VelocityContext();
		context.put(Keys.ROOT_ELEMENT, rootElement);
		context.put(Keys.SEARCH_FIELDS, fields);
		context.put(Keys.SEARCH_WHERE, where);
		
		StringWriter writer = new StringWriter();
		template.merge(context, writer);
		return writer.toString();
	}
	
	public String doSearch(String rootElement, Collection<SearchField> fields, SearchWhere where) {
		WebResource.Builder search = this.connection.resource("/search?format=xml");
		String query = createMessage(rootElement, fields, where);
		
		String resp = search.accept("text/xml", "application/xml")
			.entity(query, "text/xml")
			.post(String.class);
		
		return resp;
	}
	
}
