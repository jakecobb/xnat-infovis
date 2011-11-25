package edu.gatech.cs7450.xnat;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;

import au.com.bytecode.opencsv.CSVReader;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import edu.gatech.cs7450.Util;

/** xNAT search engine interface. */
public class XNATSearch {
	private static final Logger _log = Logger.getLogger(XNATSearch.class);
	
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
			
			template = Velocity.getTemplate("/xnat_search.vel", "UTF-8");
			
		} catch (IOException e) {
			_log.error("Failed to load velocity template.", e);
			throw new XNATException(e);
		} catch( ResourceNotFoundException e ) {
			_log.error("Could not find velocity template.", e);
			throw e;
		} catch( ParseErrorException e ) {
			_log.error("Could not parse the velocity template.", e);
			throw e;
		}
	}

	/** The server connection helper. */
	private XNATConnection connection;
	
	public XNATSearch(XNATConnection connection) {
		if( connection == null ) throw new NullPointerException("connection is null");
		this.connection = connection;
	}
	
	String createMessage(String rootElement, Collection<SearchField> fields, SearchWhere where) 
			throws XNATException {
		try {
			VelocityContext context = new VelocityContext();
			context.put(Keys.ROOT_ELEMENT, rootElement);
			context.put(Keys.SEARCH_FIELDS, fields);
			context.put(Keys.SEARCH_WHERE, where);
			
			StringWriter writer = new StringWriter();
			template.merge(context, writer);
			return writer.toString();
		} catch( VelocityException e ) {
			final String MSG = "Failed to merge template.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		}
	}
	
	
	public String doSearch(String rootElement, Collection<SearchField> fields, SearchWhere where) 
			throws XNATException {
		final boolean _debug = _log.isDebugEnabled();
		try {
			WebResource.Builder search = this.connection.resource("/search?format=xml");
			String query = createMessage(rootElement, fields, where);
			if( _debug ) _log.debug("Sending search query: " + query);
			
			String resp = search.accept("text/xml", "application/xml")
				.entity(query, "text/xml")
				.post(String.class);
			
			if( _debug ) _log.debug("Search response: " + resp);
			return resp;
			
		} catch( UniformInterfaceException e ) {
			final String MSG = "Search request failed.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		}
	}
	
	/**
	 * Fetches the set of searchable elements from xNAT.
	 * 
	 * @return the searchable elements, in the order given by the server
	 * @throws XNATException if anything goes wrong
	 */
	public List<SearchElement> fetchSearchableElements() throws XNATException {
		final boolean _debug = _log.isDebugEnabled();
		try {
			String resp = connection.resource("/search/elements?format=csv").get(String.class);
			
			ArrayList<SearchElement> elements = new ArrayList<SearchElement>();
			
			CSVReader reader = new CSVReader(new StringReader(resp));
			String[] fields = reader.readNext(); // discard header
			
			if( fields.length != 5 ) {
				_log.warn("Unexpected # of fields in the header: " + Arrays.toString(fields));
			}
			
			while( null != (fields = reader.readNext()) ) {
				if( fields.length < 1 ) {
					if( _debug ) _log.debug("Skipping empty field entry.");
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
						_log.warn("Element (" + name + ") count was not an int: " + fields[4]);
					}
				}
				// fall-back to name only
				if( element == null ) {
					element = new SearchElement(name);
					if( _debug ) _log.debug("Falling back to name-only for " + name);
				}
				
				elements.add(element);
				if( _debug ) _log.debug(element);
			}
		
			return elements;
			
		} catch( UniformInterfaceException e ) {
			final String MSG = "Searchable elements request failed.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		} catch( IOException e ) {
			final String MSG = "Could not parse searchable elements response.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		} catch( RuntimeException e ) {
			final String MSG = "Unexpected runtime exception.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
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
		final boolean _debug = _log.isDebugEnabled();
		
		try {
			// CSV because xNAT 1.5 is serving malformed XML for this request, bug has been reported
			String resp = connection.resource("/search/elements/" + elementName + "?format=csv")
				.get(String.class);
			
			CSVReader reader = new CSVReader(new StringReader(resp));
			String[] fields = reader.readNext();
			
			// logging and sanity checking
			if( fields == null ) {
				XNATException e = new XNATException("No elements found for " + elementName);
				_log.error(e.getMessage(), e);
				throw e;
			}
			if( fields.length != 8 ) {
				_log.warn("Expected 8 header fields, not " + fields.length + " in: " + Arrays.toString(fields));
			}
			if( _debug ) _log.debug("Header fields: " + Arrays.toString(fields));
			
			ArrayList<SearchField> searchFields = new ArrayList<SearchField>();
			while( null != (fields = reader.readNext()) ) {
				if( fields.length != 8 ) {
					_log.warn("Skipping entry with " + fields.length + " columns (expected 8): " + Arrays.toString(fields));
					continue;
				}
				
				// columns should be:
				// FIELD_ID, HEADER, SUMMARY, TYPE, VALUE_REQUIRED,  DESCRIPTION, ELEMENT_NAME, SRC
				String fieldId = fields[0],
				        header = fields[1],
				       summary = fields[2],
				          type = fields[3],
				          desc = fields[5],
				        elName = fields[6];
				if( !elementName.equals(elName) )
					_log.warn("Field element name (" + elName + ") does not match given element name (" + elementName + ")");
				
				Boolean isValueRequired = Boolean.valueOf(fields[4]);
				Integer src = null;
				try {
					src = Integer.valueOf(fields[7]);
				} catch( NumberFormatException e ) {
					_log.warn("Could not parse SRC field: " + fields[7], e);
				}
				
				SearchField field = new SearchField(elName, fieldId, type, header, desc, summary, isValueRequired, src);
				searchFields.add(field);
				if( _debug ) _log.debug(field);
			}
			
			return searchFields;
		} catch( UniformInterfaceException e ) {
			final String MSG = "Element fields request failed for: " + elementName;
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		} catch( IOException e ) {
			final String MSG = "Could not parse fields for element: " + elementName;
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		}
	}
	
}
