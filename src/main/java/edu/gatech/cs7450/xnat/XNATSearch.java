package edu.gatech.cs7450.xnat;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;

import au.com.bytecode.opencsv.CSVReader;

import com.sun.jersey.api.client.UniformInterfaceException;
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
	
	
	public String doSearch(String rootElement, Collection<SearchField> fields, SearchWhere where) 
			throws XNATException {
		try {
			WebResource.Builder search = this.connection.resource("/search?format=xml");
			String query = createMessage(rootElement, fields, where);
			
			String resp = search.accept("text/xml", "application/xml")
				.entity(query, "text/xml")
				.post(String.class);
			
			return resp;
		} catch( UniformInterfaceException e ) {
			throw new XNATException("Search request failed.", e);
		}
	}
	
	/**
	 * Fetches the set of searchable elements from xNAT.
	 * 
	 * @return the searchable elements, in the order given by the server
	 * @throws XNATException if anything goes wrong
	 */
	public List<SearchElement> fetchSearchableElements() throws XNATException {
		try {
			String resp = connection.resource("/search/elements?format=csv").get(String.class);
			
			ArrayList<SearchElement> elements = new ArrayList<SearchElement>();
			
			CSVReader reader = new CSVReader(new StringReader(resp));
			String[] fields = reader.readNext(); // discard header
			
			// FIXME error handling and/or logging
			if( fields.length != 5 )
				System.err.println("Unexpected # of fields in the header: " + Arrays.toString(fields));
			
			while( null != (fields = reader.readNext()) ) {
				if( fields.length < 1 ) {
					// FIXME error? skipped for now
					continue;
				}
				
				SearchElement element = null;
				String name = fields[0];
				if( fields.length == 5 ) {
					try {
						boolean isSecured = Boolean.valueOf(fields[3]); // FIXME non-boolean values will yield false right now
						int count = Integer.valueOf(fields[4]);
						element = new SearchElement(name, fields[1], fields[2], isSecured, count);
					} catch( NumberFormatException e ) {
						System.err.println("Count was not an integer?: " + fields[4]);
					}
				}
				// fall-back to name only
				if( element == null )
					element = new SearchElement(name);
				
				elements.add(element);
			}
		
			return elements;
		} catch( UniformInterfaceException e ) {
			throw new XNATException("Searchable elements request failed.", e);
		} catch( IOException e ) {
			throw new XNATException("Could not parse searchable elements response.", e);
		} catch( RuntimeException e ) {
			throw new XNATException("Unexpected runtime exception.", e);
		}
	}
	
	/**
	 * Fetches the search fields for a given element.
	 * 
	 * @param elementName the element name
	 * @return the search fields, in the order given by the server
	 * @throws NullPointerException if <code>elementName</code> is <code>null</code>
	 * @throws XNATException if there is an error with the request or parsing the response
	 */
	public List<SearchField> fetchElementFields(String elementName) throws XNATException {
		if( elementName == null ) throw new NullPointerException("elementName is null");
		
		try {
			// CSV because xNAT 1.5 is serving malformed XML for this request, bug has been reported
			String resp = connection.resource("/search/elements/" + elementName + "?format=csv")
				.get(String.class);
			
			CSVReader reader = new CSVReader(new StringReader(resp));
			String[] fields = reader.readNext();
			
			if( fields == null )
				throw new XNATException("No elements found for " + elementName);
			if( fields.length != 8 )
				throw new XNATException("Expected 8 header fields, not " + fields.length);
			
			ArrayList<SearchField> searchFields = new ArrayList<SearchField>();
			while( null != (fields = reader.readNext()) ) {
				if( fields.length != 8 ) {
					// FIXME throw exception?
					System.err.println("Skipping entry with " + fields.length + " columns (expected 8): " + Arrays.toString(fields));
					continue;
				}
				
				String fieldId = fields[0],
				        header = fields[1],
				       summary = fields[2],
				          type = fields[3],
				          desc = fields[5],
				        elName = fields[6];
				if( !elementName.equals(elName) )
					System.err.println("WARN: Field element name (" + elName + ") does not match given element name (" + elementName + ")");
				Boolean isValueRequired = Boolean.valueOf(fields[4]);
				Integer src = null;
				try {
					src = Integer.valueOf(fields[7]);
				} catch( NumberFormatException e ) {
					// FIXME logging
					e.printStackTrace();
				}
				
				searchFields.add(new SearchField(elName, fieldId, type, header, desc, summary, isValueRequired, src));
			}
			
			return searchFields;
		} catch( UniformInterfaceException e ) {
			throw new XNATException("Element fields request failed for: " + elementName, e);
		} catch( IOException e ) {
			throw new XNATException("Could not parse fields for element: " + elementName, e);
		}
	}
	
}
