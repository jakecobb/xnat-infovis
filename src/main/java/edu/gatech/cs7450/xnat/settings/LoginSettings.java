package edu.gatech.cs7450.xnat.settings;

import static edu.gatech.cs7450.Util.hex;
import static edu.gatech.cs7450.Util.sha1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import edu.gatech.cs7450.xnat.XNATConnection;

/**
 * Settings manager for login/connection information.
 */
public class LoginSettings {
	private static final Logger _log = Logger.getLogger(LoginSettings.class);
	
	/** Host key for each host connection node. */
	private static final String HOST_KEY = "host";

	/** Users node name under each host connection node. */
	private static final String USER_NODE = "users";

	/** Node name for all connections. */
	private static final String XNAT_CONNECTIONS = "xnat.connections";
	
	/** Preferences where connections are stored. */
	private static final Preferences loginPrefs = 
		Preferences.userNodeForPackage(LoginSettings.class).node(XNAT_CONNECTIONS);

	public static void main(String[] args) throws Exception {
		LoginSettings s = new LoginSettings();
		s.addConnection(new XNATConnection("https://node18.cci.emory.edu:8080/xnat/REST", "nbia", "nbia"));
		s.addConnection(new XNATConnection("https://xnat.cci.emory.edu:8080/xnat/REST", "nbia", "nbia"));
		s.addConnection(new XNATConnection("https://node18.cci.emory.edu:8080/xnat/REST", "nbia2", "nbia"));
		
		_log.info("HOSTS: " + s.getHosts());
		_log.info("USERS: " + s.getUsers());
		
	}
	
	/** Creates new login settings. */
	public LoginSettings() { }
	
	private static void dumpPrefs(String pre, Preferences p) throws BackingStoreException {
		String next = "-" + pre;
		for( String k : p.keys() )
			_log.info(next + k);
		for( String c : p.childrenNames() ) {
			_log.info(next + c + "/");
			dumpPrefs(next, p.node(c));
		}
	}
	
	/**
	 * Converts the host name to a key that can be used as a node name.
	 * 
	 * @param host the host name
	 * @return the host key
	 */
	protected String hostToKey(String host) {
		if( host == null ) throw new NullPointerException("host is null");
		return hex(sha1(host));
	}
	
	/**
	 * Adds a connection to the store.
	 * 
	 * @param conn the connection to add
	 * @throws NullPointerException if <code>conn</code> is <code>null</code>
	 * @throws SettingsException    if the new connection could not be persisted
	 */
	public void addConnection(XNATConnection conn) {
		if( conn == null ) throw new NullPointerException("conn is null");
		String hostBase = conn.getHostBase(),
		           user = conn.getUser();
		String hostKey = hostToKey(hostBase);
		
		try {
			loginPrefs.sync();
			Preferences connNode = loginPrefs.node(hostKey),
			               users = connNode.node(USER_NODE);
			if( connNode.get(HOST_KEY, null) == null )
				connNode.put(HOST_KEY, hostBase);
			users.putBoolean(user, true);
			loginPrefs.flush();
			
			dumpPrefs(" ", loginPrefs);
		} catch ( BackingStoreException e ) {
			final String MSG = "Failed to save new connection for host: " + hostBase;
			_log.error(MSG, e);
			throw new SettingsException(MSG, e);
		}
	}
	
	/**
	 * Removes all stored data for the given host.
	 * 
	 * @param host the host to remove
	 * @throws SettingsException    if the new connection could not be persisted
	 */
	public void removeConnection(String host) {
		String hostKey = hostToKey(host);
		try {
			loginPrefs.sync();
			if( loginPrefs.nodeExists(hostKey) ) {
				loginPrefs.node(hostKey).removeNode();
				loginPrefs.flush();
			} else {
				_log.warn("No node to delete [host=" + host + ", key=" + hostKey + "]");
			}
		} catch( BackingStoreException e ) {
			final String MSG = "Failed to remove host: " + host;
			_log.error(MSG, e);
			throw new SettingsException(MSG, e);
		}
	}
	
	/**
	 * Returns all the hosts currently stored.
	 * 
	 * @return the host names, may be empty but never <code>null</code>
	 * @throws SettingsException if the hosts cannot be loaded
	 */
	public List<String> getHosts() throws SettingsException {
		try {
			loginPrefs.sync();
			
			String[] hostKeys = loginPrefs.childrenNames();
			ArrayList<String> hosts = new ArrayList<String>(hostKeys.length);
			for( String child : hostKeys ) {
				String host = loginPrefs.node(child).get(HOST_KEY, null);
				if( host != null )
					hosts.add(host);
				else
					_log.warn("No host for connection key: " + child);
			}
			
			return hosts;

		} catch( BackingStoreException e ) {
			final String MSG = "Failed to retrieve hosts.";
			_log.error(MSG, e);
			throw new SettingsException(MSG, e);
		}
	}
	
	/**
	 * Returns all the users stored for the given host.
	 * 
	 * @param host the host
	 * @return the users, may be empty but never <code>null</code>
	 * @throws SettingsException if the users cannot be loaded
	 */
	public List<String> getUsers(String host) throws SettingsException {
		String hostKey = hostToKey(host);
		try {
			loginPrefs.sync();
			String users = hostKey + "/users";
			if( loginPrefs.nodeExists(users) )
				return Arrays.asList(loginPrefs.node(users).keys());
			return Collections.emptyList();
			
		} catch( BackingStoreException e ) {
			final String MSG = "Failed to retrieve users.";
			_log.error(MSG + " [host=" + host + ", key=" + hostKey + "]", e);
			throw new SettingsException(MSG, e);
		}
	}
	
	/**
	 * Gets all users for any host.
	 * <p>Only distinct usernames will be returned, order is arbitrary.</p>
	 * 
	 * @return the users, may be empty but never <code>null</code>
	 * @throws SettingsException if the users cannot be loaded
	 */
	public List<String> getUsers() throws SettingsException {
		try {
			loginPrefs.sync();
			
			HashSet<String> users = new HashSet<String>();
			for( String hostKey : loginPrefs.childrenNames() ) {
				String[] hostUsers = loginPrefs.node(hostKey).node(USER_NODE).keys();
				users.addAll(Arrays.asList(hostUsers));
			}
			return new ArrayList<String>(users);
			
		} catch( BackingStoreException e ) {
			final String MSG = "Failed to retrieve users.";
			_log.error(MSG, e);
			throw new SettingsException(MSG, e);
		}
	}
}
