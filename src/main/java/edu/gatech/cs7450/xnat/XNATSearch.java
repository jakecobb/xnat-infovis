package edu.gatech.cs7450.xnat;

import java.io.IOException;
import java.io.Serializable;
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
	 * A search element, as returned by the <code>search/elements</code> REST call.
	 */
	public static class SearchElement implements Serializable {
		private static final long serialVersionUID = 1L;
		
		/** The element name. */
		private String name;
		/** Human-readable singular form. */
		private String singular;
		/** Human-readable plural form. */
		private String plural;
		/** If the element is secured or open. */
		private boolean isSecured;
		/** FIXME The number of such elements in the database? */
		private int count;

		/**
		 * Create a search element with a name only.
		 * @param name the name
		 */
		public SearchElement(String name) {
			setName(name); // accessor for the null check
		}
		
		/**
		 * Creates a search element.
		 * 
		 * @param name      the element name, may <strong>not</strong> be <code>null</code>
		 * @param singular  human-readable singular form, may be <code>null</code>
		 * @param plural    human-readable plural form, may be <code>null</code>
		 * @param isSecured if the element is secure or not
		 * @param count     <strong>FIXME</strong> number of elements in the database?
		 */
		public SearchElement(String name, String singular, String plural, boolean isSecured, int count) {
			this(name);
			this.singular = singular;
			this.plural = plural;
			this.isSecured = isSecured;
			this.count = count;
		}
		
		
		public String getSingular() {
			return singular;
		}

		public void setSingular(String singular) {
			this.singular = singular;
		}

		public String getPlural() {
			return plural;
		}

		public void setPlural(String plural) {
			this.plural = plural;
		}

		public boolean isSecured() {
			return isSecured;
		}

		public void setSecured(boolean isSecured) {
			this.isSecured = isSecured;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			if( name == null ) throw new NullPointerException("name is null");
			this.name = name;
		}
		
		@Override
		public String toString() {
			return "SearchElement(name=" + name + ", singular=" + singular + ", plural=" + plural 
				+ ", secure=" + isSecured + ", count=" + count + ")";
		}
	}
	
}
