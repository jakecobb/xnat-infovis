package edu.gatech.cs7450.xnat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
	protected List<String> headers;
	/** Map header name to order. */
	protected Map<String, Integer> headerPosition;
	/** The data rows. */
	protected List<? extends XNATTableRow> rows;
	
	/**
	 * Parses a result from the given CSV input.
	 * 
	 * @param csvData the data to parse
	 * @throws IOException if there is an error parsing <code>csvData</code>
	 */
	public XNATTableResult(String csvData) throws IOException {
		if( csvData == null ) throw new NullPointerException("csvData is null");
		
		this.parseData(wrapStringData(csvData));
		this.mapHeaders();
	}
	
	/**
	 * Parses a result from the given reader.
	 * <p>
	 * Note the reader is not closed, the caller is responsible for closing it 
	 * if needed.
	 * </p>
	 * 
	 * @param input the reader
	 * @throws IOException if there is an error parsing
	 * @throws NullPointerException if <code>reader</code> is <code>null</code>
	 */
	public XNATTableResult(InputStream input) throws IOException {
		if( input == null ) throw new NullPointerException("reader is null");
		
		this.parseData(input);
		this.mapHeaders();
	}
	
	/** For subclasses to avoid the other constructors. */
	protected XNATTableResult() { }
	
	/**
	 * Wraps the string in as a UTF-8 encoded input stream.
	 * 
	 * @param data the string to wrap
	 * @return the string as a UTF-8 input stream
	 */
	protected InputStream wrapStringData(String data) {
		ByteBuffer buffer = Charset.forName("UTF-8").encode(data);
		return new ByteArrayInputStream(buffer.array(), buffer.arrayOffset(), buffer.limit());
	}
	
	/**
	 * Parses the data. This method is expected to set 
	 * <code>headers</code> and <code>rows</code> before 
	 * returning normally.
	 * 
	 * @param reader the reader
	 * @throws IOException if there is an error parsing
	 */
	protected void parseData(InputStream in) throws IOException {
		if( in == null ) throw new NullPointerException("in is null");
		
		Reader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
		CSVReader csv = new CSVReader(reader);
		
		// read headers
		String[] fields = csv.readNext();
		if( fields == null || fields.length == 0 )
			throw new IOException("Header fields was null or empty.");
		
		headers = new ArrayList<String>(Arrays.asList(fields));
		
		// now create rows
		ArrayList<XNATTableRow> rows = new ArrayList<XNATTableRow>();
		while( null != (fields = csv.readNext()) ) {
			rows.add(new XNATTableRow(fields));
		}
		rows.trimToSize();
		this.rows = rows;
	}
	
	/**
	 * Instantiates and populates <code>headerPosition</code>.
	 * <code>headers</code> is expected to be set before this is 
	 * called.
	 */
	protected void mapHeaders() {
		headerPosition = new HashMap<String, Integer>();
		for( int i = 0, ilen = headers.size(); i < ilen; ++i )
			headerPosition.put(headers.get(i), i);
	}
	
	/**
	 * Returns an unmodifiable view of the headers.
	 * @return the headers
	 */
	public List<String> getHeaders() {
		return Collections.unmodifiableList(headers);
	}
	
	/** 
	 * Returns an unmodifiable view of the rows. 
	 * @return the rows
	 */
	public List<? extends XNATTableRow> getRows() {
		return Collections.unmodifiableList(rows);
	}
	
	/**
	 * A row of result data.
	 */
	public class XNATTableRow {
		/** The data values. */
		protected List<String> values;
		
		/**
		 * Creates a new data row.
		 * @param values the values
		 */
		public XNATTableRow(Collection<String> values) {
			if( values == null ) throw new NullPointerException("values is null");
			if( values.size() != headers.size() ) {
				_log.warn("Mismatch in length of headers (" + headers.size() + ") and values (" + values.size() + ")");
				_log.info("Values: " + values);
			}
			this.values = new ArrayList<String>(values);
		}
		
		/**
		 * Creates a new data row.
		 * @param values the values
		 */		
		public XNATTableRow(String... values) {
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
		
		/**
		 * Returns the indexes of columns missing a value in this row, where 
		 * missing means <code>null</code> or empty.
		 * 
		 * @return the indexes
		 */
		public List<Integer> getMissingColumns() {
			final int nHeaders = headers.size();
			ArrayList<Integer> missing = new ArrayList<Integer>(Math.max(2, nHeaders / 4));
			for( int i = 0; i < nHeaders; ++i ) {
				String val = values.get(i);
				if( val == null || "".equals(val.trim()) )
					missing.add(i);
			}
			
			return missing;
			
		}
		
		/**
		 * Returns a list of headers where the value in this row is 
		 * missing (<code>null</code> or blank).
		 * 
		 * @return the missing headers
		 */
		public List<String> getMissingHeaders() {
			List<Integer> cols = getMissingColumns();
			ArrayList<String> missing = new ArrayList<String>(cols.size());
			for( Integer col : cols )
				missing.add(headers.get(col));
			return missing;
		}
	}
}
