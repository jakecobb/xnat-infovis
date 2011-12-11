package edu.gatech.cs7450.xnat;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.event.implement.EscapeXmlReference;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;

import au.com.bytecode.opencsv.CSVReader;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import edu.gatech.cs7450.Util;
import edu.gatech.cs7450.xnat.XNATConstants.Subjects;

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
			
			// make sure we escape XML in user input and compare operator values
			p.setProperty("eventhandler.referenceinsertion.class", EscapeXmlReference.class.getName());
			p.setProperty("eventhandler.escape.xml.match", "/(rootElement|field|criteria).*/");
			
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
	
	String createMessage(String rootElement, Collection<? extends SearchField> fields, SearchWhere where) 
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
	
	/**
	 * Executes a search and returns the result set.
	 * 
	 * @param query the query containing the root element, search fields, and search criteria to use
	 * @return the search result
	 * @throws NullPointerException if <code>query</code> is <code>null</code>
	 * @throws XNATException if anything goes wrong
	 */
	public XNATResultSet runSearch(SearchQuery query) throws XNATException {
		if( query == null ) throw new NullPointerException("query is null");
		return runSearch(query.getRootElement(), query.getSearchFields(), query.getSearchWhere());
	}
	
	/**
	 * Executes a search using default search fields and returns the result set.
	 * 
	 * @param where the search criteria
	 * @return the result set
	 * @throws NullPointerException if <code>where</code> is <code>null</code>
	 * @throws XNATException if anything goes wrong
	 */
	public XNATResultSet runSearch(SearchWhere where) throws XNATException {
		if( where == null ) throw new NullPointerException("where is null");
		return runSearch(XNATDefaults.DEFAULT_SEARCH_ROOT, XNATDefaults.DEFAULT_SEARCH_FIELDS, where);
	}
	
	/**
	 * Executes a search and returns the result set.
	 * 
	 * @param rootElement the root element of the search
	 * @param fields      the search fields
	 * @param where       the search criteria
	 * @return the result set
	 * @throws NullPointerException if any argument is <code>null</code>
	 * @throws XNATException if anything goes wrong
	 */
	public XNATResultSet runSearch(String rootElement, Collection<? extends SearchField> fields, SearchWhere where) 
			throws XNATException {
		if( rootElement == null ) throw new NullPointerException("rootElement is null");
		if( fields == null )      throw new NullPointerException("fields is null");
		if( where == null )       throw new NullPointerException("where is null");
		
		final boolean _debug = _log.isDebugEnabled();
		if( _log.isTraceEnabled() ) {
			_log.trace("rootElement: " + rootElement);
			_log.trace("     fields: " + fields);
			_log.trace("      where: " + where);
		}
		
		try {
			// prepare to POST the query
			WebResource.Builder search = this.connection.resource("/search?format=xml");
			String query = createMessage(rootElement, fields, where);
			if( _debug ) _log.debug("Sending search query: " + query);
			
			// POST it
			String resp = search.accept("text/xml", "application/xml")
				.entity(query, "text/xml")
				.post(String.class);
			if( _debug ) _log.debug("Search response: " + resp);
			
			return new XNATResultSet(resp);
			
		} catch( UniformInterfaceException e ) {
			final String MSG = "Search request failed.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		} catch( IOException e ) {
			final String MSG = "Error parsing search response.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		} catch( RuntimeException e ) {
			final String MSG = "Unexpected runtime exception during search processing.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		}
	}
	
	/** @deprecated Use <code>runSearch</code> */
	@Deprecated
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
		} catch( RuntimeException e ) {
			final String MSG = "Unexpected runtime exception.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		}
	}
	
	/**
	 * Fetches the list of projects.
	 * 
	 * @return the projects
	 * @throws XNATException if there is a problem fetching the projects
	 * @see XNATConstants.Projects#COLUMNS
	 */
	public XNATTableResult fetchProjects() throws XNATException {
		try {
			String resp = connection.resource("/projects?format=csv").get(String.class);
			if( _log.isDebugEnabled() )
				_log.debug("Project response:\n" + resp);
			
			
			return new XNATTableResult(resp);
			
		// exception wrapping
		} catch( UniformInterfaceException e ) {
			final String MSG = "Projects request failed.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		} catch( IOException e ) {
			final String MSG = "Failed to parse result.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		} catch( RuntimeException e ) {
			final String MSG = "Unexpected runtime exception.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		}
	}
	
	/**
	 * Fetches all the subjects in table form.
	 * 
	 * @return the subjects
	 * @throws XNATException if there is a problem fetching the subjects
	 */
	public XNATTableResult fetchSubjects() throws XNATException {
		return fetchSubjects(null);
	}
	
	/**
	 * Fetches a set of subjects in table form, optionally for a given project.
	 * 
	 * @param projectID the project ID or <code>null</code> to fetch all subjects
	 * @param extraCols optional extra columns to retrieve
	 * @return the set of subjects
	 * @throws XNATException if there is a problem fetching the subjects
	 * @see XNATConstants.Subjects#COLUMNS
	 */
	public XNATTableResult fetchSubjects(String projectID, String... extraCols) throws XNATException {
		try {
			// standard URL params
			StringBuilder b = new StringBuilder();
			if( projectID != null )
				b.append("/projects/").append(URLEncoder.encode(projectID, "UTF-8"));
			b.append("/subjects?format=csv");
			
			// if extra columns, specify the defaults plus the extras
			if( extraCols != null && extraCols.length > 0 ) {
				b.append("&columns=");
				for( String defCol : Subjects.COLUMNS )
					b.append(URLEncoder.encode(defCol, "UTF-8")).append(',');
				for( String extraCol : extraCols )
					b.append(URLEncoder.encode(extraCol, "UTF-8")).append(',');
				b.setLength(b.length() - 1); // delete last ','
			}
			
			String webPath = b.toString();
			
			String resp = connection.resource(webPath).get(String.class);
			if( _log.isDebugEnabled() ) _log.debug("Subject response:\n" + resp);
			
			return new XNATTableResult(resp);
			
		// exception wrapping
		} catch( UniformInterfaceException e ) {
			final String MSG = "Subjects request failed.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		} catch( IOException e ) {
			final String MSG = "Failed to parse result.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		} catch( RuntimeException e ) {
			final String MSG = "Unexpected runtime exception.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		}
	}
	
	/**
	 * Fetches all experiments in table form.
	 * 
	 * @return the experiments
	 * @throws XNATException if there is any problem fetching experiments
	 */
	public XNATTableResult fetchExperiments() throws XNATException {
		return fetchExperiments(null, null);
	}
	
	/**
	 * Fetches all experiments in table form, optionally for a project and/or subject.
	 * 
	 * @param projectID the project ID or <code>null</code> for any project
	 * @param subjectId the subject ID or <code>null</code> for any subject
	 * 
	 * @return the experiments
	 * @throws XNATException if there is any problem fetching experiments
	 * @see XNATConstants.Sessions#COLUMNS
	 */	
	public XNATTableResult fetchExperiments(String projectID, String subjectId) throws XNATException {
		
		try {
			String webPath = "";
			if( projectID != null )
				webPath = "/projects/" + URLEncoder.encode(projectID, "UTF-8");
			if( subjectId != null )
				webPath += "/subjects/" + URLEncoder.encode(subjectId, "UTF-8");
			webPath += "/experiments?format=csv";
			
			String resp = connection.resource(webPath).get(String.class);
			if( _log.isDebugEnabled() ) _log.debug("Experiment response:\n" + resp);
			
			return new XNATTableResult(resp);
			
		// exception wrapping
		} catch( UniformInterfaceException e ) {
			final String MSG = "Experiments request failed.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		} catch( IOException e ) {
			final String MSG = "Failed to parse result.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		} catch( RuntimeException e ) {
			final String MSG = "Unexpected runtime exception.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		}
		
	}
	
	/**
	 * Fetches a set of scans in table form.
	 * <p>
	 * If <code>query</code> is not <code>null</code>, the columns will be those of its 
	 * search fields.  Otherwise, {@link XNATDefaults#DEFAULT_SEARCH_FIELDS} will be used.
	 * 
	 * @param query optional query to get columns from (search filter is not currently supported)
	 * @return the scans
	 * @throws XNATException if there is a problem fetching the scans
	 */
	public XNATTableResult fetchScans(SearchQuery query) throws XNATException {
		try {
			final Pattern pattern  = Pattern.compile("xnat:\\w+(/\\w+)*", Pattern.CASE_INSENSITIVE);
			
			// build up the query string
			StringBuilder b = new StringBuilder("/experiments?format=csv&xsiType=xnat:mrSessionData&columns=ID");
			List<SearchField> searchFields = query != null ? query.getSearchFields() : XNATDefaults.DEFAULT_SEARCH_FIELDS;
			for( SearchField f : searchFields ) {
				String summary = f.getSummary();
				
				// sanity checks
				if( summary == null ) {
					XNATException e = new XNATException("A search field had a null summary.");
					_log.error(e.getMessage() + ": " + f, e);
					throw e;
				}
				if( !pattern.matcher(summary).matches() )
					_log.warn("Field summary may not work: " + summary);
				
				b.append(',').append(URLEncoder.encode(summary, "UTF-8"));
			}
			
			String resp = connection.resource(b.toString()).get(String.class);
			if( _log.isDebugEnabled() ) _log.debug("Scan response:\n" + resp);
			
			return new XNATTableResult(resp);
			
		// exception wrapping
		} catch( UniformInterfaceException e ) {
			final String MSG = "Experiments request failed.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		} catch( IOException e ) {
			final String MSG = "Failed to parse result.";
			_log.error(MSG, e);
			throw new XNATException(MSG, e);
		} 
	}
}
