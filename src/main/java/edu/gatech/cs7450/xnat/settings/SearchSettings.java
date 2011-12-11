package edu.gatech.cs7450.xnat.settings;

import static edu.gatech.cs7450.Util.hex;
import static edu.gatech.cs7450.Util.sha1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import edu.gatech.cs7450.Util;
import edu.gatech.cs7450.xnat.SearchQuery;

/**
 * Settings manager for stored searches.
 */
public class SearchSettings {
	private static final Logger _log = Logger.getLogger(SearchSettings.class);

	/** Node name for all searches. */
	private static final String SEARCHES_NODE = "searches";
	
	/** Key name for the search name. */
	private static final String NAME_KEY = "name";
	
	/** Key name for the serialized search object. */
	private static final String DATA_KEY = "data";
	
	/** Preferences where searches are stored. */
	private static final Preferences searchPrefs =
		Preferences.userNodeForPackage(SearchSettings.class).node(SEARCHES_NODE);
	
	/** Creates new search settings. */
	public SearchSettings() { }
	
	/**
	 * Saves a search to the store.
	 * 
	 * @param query the query to save
	 * @throws NullPointerException if <code>query</code> is <code>null</code>
	 * @throws SettingsException    if the query has no name or can't be saved
	 */
	public void saveSearch(SearchQuery query) throws SettingsException {
		if( query == null ) throw new NullPointerException("query is null");
		
		String name = query.getName();
		if( name == null || "".equals(name) )
			throw new SettingsException("Query name is null or empty.");
		
		String key = nameToKey(name);
		
		final String ERR_MSG = "Could not save search for: " + name;
		try {
			byte[] serialized = Util.serialize(query);
			
			Preferences searchNode = searchPrefs.node(key);
			searchNode.put(NAME_KEY, name);
			searchNode.putByteArray(DATA_KEY, serialized);
			searchNode.flush();
			
		} catch( BackingStoreException e ) {
			_log.error(ERR_MSG, e);
			throw new SettingsException(ERR_MSG, e);
		} catch( IOException e ) {
			_log.error("Serialization failed for: " + name, e);
			throw new SettingsException(ERR_MSG, e);
		}
	}
	
	/**
	 * Loads a saved query from the store.
	 * 
	 * @param name the query name
	 * @return the saved query
	 * @throws NullPointerException if <code>name</code> is <code>null</code>
	 * @throws SettingsException    if the query does not exist or can't be loaded
	 */
	public SearchQuery loadSearch(String name) {
		if( name == null ) throw new NullPointerException("name is null");
		
		String key = nameToKey(name);
		final String ERR_MSG = "Failed to load query: " + name;
		try {
			searchPrefs.sync();
			if( !searchPrefs.nodeExists(key) )
				throw new SettingsException("No query stored for name: " + name);
			
			Preferences searchNode = searchPrefs.node(key);
			byte[] serialized = searchNode.getByteArray(DATA_KEY, null);
			if( serialized == null ) {
				_log.error("Search node [key=" + key + "] exists, but has no data.");
				throw new SettingsException("No query stored for name: " + name);
			}
			
			Object queryObj = Util.deserialize(serialized);
			if( !(queryObj instanceof SearchQuery) ) {
				_log.error("Wrong class for query [key=" + key + "]: " + queryObj.getClass().getName());
				throw new SettingsException("Bad object stored for name: " + name);
			}
			return (SearchQuery)queryObj;
			
		} catch( BackingStoreException e ) {
			_log.error(ERR_MSG, e);
			throw new SettingsException(ERR_MSG, e);
		} catch( IOException e ) {
			_log.error(ERR_MSG, e);
			throw new SettingsException(ERR_MSG, e);
		} catch( ClassNotFoundException e ) {
			_log.error(ERR_MSG, e);
			throw new SettingsException(ERR_MSG, e);
		}
	}
	
	/**
	 * Deletes a previously saved search from the store.  If the search 
	 * was not stored, nothing is done.
	 * 
	 * @param name the search name
	 * @throws NullPointerException if <code>name</code> is <code>null</code>
	 * @throws SettingsException if the search cannot be deleted
	 */
	public void deleteSearch(String name) throws SettingsException {
		if( name == null ) throw new NullPointerException("name is null");
		String key = nameToKey(name);
		
		try {
			searchPrefs.sync();
			if( searchPrefs.nodeExists(key) ) {
				searchPrefs.node(key).removeNode();
				searchPrefs.flush();
			}
		} catch( BackingStoreException e ) {
			_log.error("Failed to delete query [key=" + key + ", name=" + name +"]", e);
			throw new SettingsException("Failed to delete query: " + name, e);
		}
	}
	
	/**
	 * Retrieves the query names of all stored queries.
	 * 
	 * @return the query names, may be empty but not <code>null</code>
	 * @throws SettingsException if the query names cannot be loaded
	 */
	public List<String> getQueryNames() throws SettingsException {
		try {
			searchPrefs.sync();
			
			String[] children = searchPrefs.childrenNames();
			ArrayList<String> names = new ArrayList<String>(children.length);
			for( String child : children ) {
				String name = searchPrefs.node(child).get(NAME_KEY, null);
				if( name != null )
					names.add(name);
				else
					_log.warn("No name for search key: " + child);
			}
			
			return names;
			
		} catch( BackingStoreException e ) {
			final String MSG = "Could not load queries from store.";
			_log.error(MSG, e);
			throw new SettingsException(MSG, e);
		}
	}
		
	/**
	 * Converts the search query name to a key for the node name.
	 * 
	 * @param searchName the query name
	 * @return the node key
	 */
	protected String nameToKey(String searchName) {
		if( searchName == null ) throw new NullPointerException("searchName is null");
		return hex(sha1(searchName));
	}
}
