package edu.gatech.cs7450.xnat;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

/**
 * The result of a CSV format response from xNAT.
 */
public class XNATTableResult {
	/** Logger. */
	private static final Logger _log = Logger.getLogger(XNATTableResult.class);
	
	/** The headers / column names. */
	private List<String> headers;
	/** Map header name to order. */
	private Map<String, Integer> headerPosition;
	/** The data rows. */
	private List<XNATTableRow> rows;
	
	/**
	 * Parses a result from the given CSV input.
	 * 
	 * @param csvData the data to parse
	 * @throws IOException if there is an error parsing <code>csvData</code>
	 */
	public XNATTableResult(String csvData) throws IOException {
		this(new StringReader(csvData));
	}
	
	/**
	 * Parses a result from the given reader.
	 * <p>
	 * Note the reader is not closed, the caller is responsible for closing it 
	 * if needed.
	 * </p>
	 * 
	 * @param reader the reader
	 * @throws IOException if there is an error parsing
	 * @throws NullPointerException if <code>reader</code> is <code>null</code>
	 */
	public XNATTableResult(Reader reader) throws IOException {
		if( reader == null ) throw new NullPointerException("reader is null");
		CSVReader csv = new CSVReader(reader);
		
		// read headers
		String[] fields = csv.readNext();
		if( fields == null || fields.length == 0 )
			throw new IOException("Header fields was null or empty.");
		
		headers = new ArrayList<String>(Arrays.asList(fields));
		headerPosition = new HashMap<String, Integer>();
		for( int i = 0, ilen = headers.size(); i < ilen; ++i )
			headerPosition.put(headers.get(i), i);
		
		// now create rows
		rows = new ArrayList<XNATTableRow>();
		while( null != (fields = csv.readNext()) ) {
			rows.add(new XNATTableRow(fields));
		}
	}
	
	/**
	 * Returns an unmodifiable view of the headers.
	 * @return the headers
	 */
	public List<String> getHeaders() {
		return Collections.unmodifiableList(headers);
	}
	
	/**
	 * A row of result data.
	 */
	public class XNATTableRow {
		/** The data values. */
		private List<String> values;
		
		/**
		 * Creates a new data row.
		 * @param values the values
		 */
		public XNATTableRow(Collection<String> values) {
			if( values == null ) throw new NullPointerException("values is null");
			if( values.size() != headers.size() )
				_log.warn("Mismatch in length of headers (" + headers.size() + ") and values (" + values.size() + ")");
			this.values = new ArrayList<String>(values);
		}
		
		/**
		 * Creates a new data row.
		 * @param values the values
		 */		
		public XNATTableRow(String[] values) {
			this(values == null ? null : Arrays.asList(values));
		}
		
		/**
		 * Returns an unmodifiable view of the headers.
		 * @return the headers
		 */
		public List<String> getHeaders() {
			return XNATTableResult.this.getHeaders();
		}
		
		/**
		 * Returns an unmodifiable view of the data values.
		 * @return the data values
		 */
		public List<String> getValues() {
			return Collections.unmodifiableList(this.values);
		}
		
		/**
		 * Returns the value at a particular index.
		 * 
		 * @param index the index
		 * @return the value
		 * @throws IndexOutOfBoundsException
		 */
		public String getValue(int index) {
			return values.get(index);
		}
		
		/**
		 * Returns the value of a field by the header.
		 * 
		 * @param header the header
		 * @return the value of the field
		 * @throws IllegalArgumentException if there is no such header
		 * @throws NullPointerException if <code>header</code> is <code>null</code>
		 */
		public String getValue(String header) {
			if( header == null ) throw new NullPointerException("header is null");
			Integer pos = headerPosition.get(header);
			if( pos == null )
				throw new IllegalArgumentException("No such header: " + header);
			return getValue(pos);
		}
	}
}
