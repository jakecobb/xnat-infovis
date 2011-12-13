package edu.gatech.cs7450.prefuse;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import prefuse.data.Tuple;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.DefaultTupleSet;
import prefuse.data.tuple.TupleSet;

public class PartialSearchTupleSet extends SearchTupleSet {
	private static final Logger _log = Logger.getLogger(PartialSearchTupleSet.class);
	
	protected String query = "";
	protected boolean isCaseSensitive;
	protected LinkedHashMap<String, TupleSet> index = new LinkedHashMap<String, TupleSet>();
	
	public PartialSearchTupleSet() {
		this(false);
	}
	
	public PartialSearchTupleSet(boolean isCaseSensitive) {
		this.isCaseSensitive = isCaseSensitive;
	}

	@Override
	public String getQuery() {
		return query;
	}

	@Override
	public void search(String query) {
		if( query == null )
			query = "";
		query = query.trim();
		if( this.query.equals(query) )
			return;
		this.query = query;
		
		Tuple[] removed = clearInternal();
		if( !"".equals(query) ) {
			int mode = Pattern.LITERAL;
			if( !isCaseSensitive )
				mode |= Pattern.CASE_INSENSITIVE;
			try {
				Pattern pattern = Pattern.compile(query, mode);
				
				for( Entry<String, TupleSet> entry : index.entrySet() ) {
					String field = entry.getKey();
					for( Iterator<?> iter = entry.getValue().tuples(); iter.hasNext(); ) {
						Tuple tuple = (Tuple)iter.next();
						String value = tuple.getString(field);
						
						if( pattern.matcher(value).find() )
							addInternal(tuple);
					}
				}
			} catch( PatternSyntaxException ignore ) {
				_log.info(ignore.getMessage(), ignore);
			}
		}
		
		fireTupleEvent(getTupleCount() > 0 ? toArray() : null, removed);
	}

	@Override
	public void index(Tuple t, String field) {
		TupleSet ts = index.get(field);
		if( ts == null ) {
			ts = new DefaultTupleSet();
			index.put(field, ts);
		}
		ts.addTuple(t);
	}

	@Override
	public void unindex(Tuple t, String field) {
		if( index.containsKey(field) )
			index.get(field).removeTuple(t);
	}

	@Override
	public boolean isUnindexSupported() {
		return true;
	}
	
	@Override
	public void clear() {
		index.clear();
		super.clear();
	}

}
