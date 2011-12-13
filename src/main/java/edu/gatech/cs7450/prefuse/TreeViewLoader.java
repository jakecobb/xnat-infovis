package edu.gatech.cs7450.prefuse;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tree;
import edu.gatech.cs7450.prefuse.TreeView.NodeType;
import edu.gatech.cs7450.xnat.SearchField;
import edu.gatech.cs7450.xnat.SearchQuery;
import edu.gatech.cs7450.xnat.SearchWhere;
import edu.gatech.cs7450.xnat.SearchWhere.SearchMethod;
import edu.gatech.cs7450.xnat.SingleCriteria;
import edu.gatech.cs7450.xnat.SingleCriteria.CompareOperator;
import edu.gatech.cs7450.xnat.XNATConnection;
import edu.gatech.cs7450.xnat.XNATConstants.Projects;
import edu.gatech.cs7450.xnat.XNATConstants.Scans;
import edu.gatech.cs7450.xnat.XNATConstants.Sessions;
import edu.gatech.cs7450.xnat.XNATConstants.Subjects;
import edu.gatech.cs7450.xnat.XNATDefaults;
import edu.gatech.cs7450.xnat.XNATException;
import edu.gatech.cs7450.xnat.XNATResultSet;
import edu.gatech.cs7450.xnat.XNATResultSet.XNATResultSetRow;
import edu.gatech.cs7450.xnat.XNATSearch;

/**
 * Data loader for the {@link TreeView} visualization.
 */
public class TreeViewLoader {
	private static final Logger _log = Logger.getLogger(TreeViewLoader.class);
	
	/** Data fields for the project node. */
	public static final List<SearchField> projectFields = unmodifiableList(asList(
		Projects.ID,
		Projects.SECONDARY_ID,
		Projects.NAME,
		Projects.KEYWORDS,
		Projects.PI,
		Projects.INSERT_DATE,
		Projects.INSERT_USER,
		Projects.DESCRIPTION_CSV
	));
	
	/** Data fields for subject nodes. */
	public static final List<SearchField> subjectFields = unmodifiableList(asList(
		Subjects.SUBJECTID,
//		Subjects.LABEL,
		Subjects.XNAT_COL_SUBJECTDATALABEL,
		Subjects.SUB_GROUP,
		Subjects.INSERT_DATE,
		Subjects.INSERT_USER
	));
	
	/** Data fields for session nodes. */
	public static final List<SearchField> sessionFields = unmodifiableList(asList(
		Sessions.SESSION_ID,
		Sessions.LABEL,
		Sessions.TYPE,
		Sessions.DATE,
		Sessions.INSERT_USER,
		Sessions.INSERT_DATE,
		Sessions.LAST_MODIFIED
	));
	
	/** Data fields for scan nodes. */
	public static final List<SearchField> scanFields = XNATDefaults.DEFAULT_SCAN_FIELDS;
	
	private static Schema nodeSchema, edgeSchema;
	static {
		nodeSchema = new Schema();
		nodeSchema.addColumn("name", String.class);
		nodeSchema.addColumn(NodeType.COLUMN, NodeType.class);
		for( SearchField f : projectFields )
			nodeSchema.addColumn(f.getSummary(), String.class);
		for( SearchField f : subjectFields )
			nodeSchema.addColumn(f.getSummary(), String.class);
		for( SearchField f : sessionFields )
			nodeSchema.addColumn(f.getSummary(), String.class);
		for( SearchField f : scanFields )
			nodeSchema.addColumn(f.getSummary(), String.class);
		
		edgeSchema = new Schema();
		edgeSchema.addColumn(Tree.DEFAULT_SOURCE_KEY, int.class);
		edgeSchema.addColumn(Tree.DEFAULT_TARGET_KEY, int.class);
	}

	/** @deprecated Use an instance and {@link #loadProjectData()}. */
	@Deprecated
	public static Tree loadResult(String projectId, XNATResultSet result) {
		if( projectId == null ) throw new NullPointerException("projectId is null");
		if( result == null ) throw new NullPointerException("result is null");
		
		Table nodeTable = nodeSchema.instantiate(),
		      edgeTable = edgeSchema.instantiate();
		
		Tree dataTree = new Tree(nodeTable, edgeTable);
		
		// add the project node as the root
		int rootRowIdx = dataTree.addRootRow();
		nodeTable.set(rootRowIdx, "name", projectId);
		nodeTable.set(rootRowIdx, NodeType.COLUMN, NodeType.PROJECT);
		
		HashMap<String, Integer> subjToRow = new HashMap<String, Integer>(),
		                         sessToRow = new HashMap<String, Integer>();
		
		// process into tree form
		for( XNATResultSetRow row : result.getRows() ) {
			String subjectId = row.getValue(Sessions.SUBJECT_ID);
			
			int subjRowIdx;
			if( subjToRow.containsKey(subjectId) ) {
				subjRowIdx = subjToRow.get(subjectId);
			} else {
				// add new subject and connect it to the root
				subjRowIdx = nodeTable.addRow();
				nodeTable.set(subjRowIdx, "name", subjectId);
				nodeTable.set(subjRowIdx, NodeType.COLUMN, NodeType.SUBJECT);
				dataTree.addEdge(rootRowIdx, subjRowIdx);
				subjToRow.put(subjectId, subjRowIdx);
			}
			
			String sessionId = row.getValue(Sessions.SESSION_ID);
			
			int sessRowIdx;
			if( sessToRow.containsKey(sessionId) ) {
				sessRowIdx = sessToRow.get(sessionId);
			} else {
				// add new session and connect it to the subject
				sessRowIdx = nodeTable.addRow();
				nodeTable.set(sessRowIdx, "name", sessionId);
				nodeTable.set(sessRowIdx, NodeType.COLUMN, NodeType.SESSION);
				dataTree.addEdge(subjRowIdx, sessRowIdx);
				sessToRow.put(sessionId, sessRowIdx);
			}
			
			// missing type means no scans
			if( row.getMissingFields().contains(Scans.TYPE) )
				continue;
			
			String scanId = row.getValue(Scans.ID);
			int scanRowIdx = nodeTable.addRow();
			nodeTable.set(scanRowIdx, "name", scanId);
			nodeTable.set(scanRowIdx, NodeType.COLUMN, NodeType.SCAN);
			dataTree.addEdge(sessRowIdx, scanRowIdx);
		}
		
		return dataTree;
	}
	
	/** The connection for fetching data. */
	protected XNATConnection conn;
	/** The project id. */
	protected String projectId;
	
	/** Temporary map subject id -> row number. */
	protected Map<String, Integer> subjToRow;
	/** Temporary map session id -> row number. */
	protected Map<String, Integer> sessToRow;
	
	/** Temporary storage of the tree and related info. */
	protected Tree dataTree;
	
	public TreeViewLoader(XNATConnection conn, String projectId) {
		if( conn == null ) throw new NullPointerException("conn is null");
		if( projectId == null ) throw new NullPointerException("projectId is null");
		projectId = projectId.trim();
		if( "".equals(projectId) )
			throw new IllegalArgumentException("projectId is blank");
		
		this.conn = conn;
		this.projectId = projectId;
	}
	
	/**
	 * Fetches and loads all the data for the project into a 
	 * <code>Tree</code> for the {@link TreeView}.
	 * 
	 * @return the populated tree
	 * @throws XNATException
	 */
	public Tree loadProjectData() throws XNATException {
		try {
			dataTree = makeTree();
			subjToRow = new HashMap<String, Integer>();
			sessToRow = new HashMap<String, Integer>();

			addProject();
			addSubjects();
			addSessionsScans();

			return dataTree;
		} finally {
			dataTree = null;
			subjToRow = null;
			sessToRow = null;
		}
	}

	/**
	 * Instantiates the initial empty tree.
	 * @return the tree
	 */
	protected Tree makeTree() {
		return new Tree(nodeSchema.instantiate(), edgeSchema.instantiate());
	}
	
	/**
	 * Fetches project data and adds the root node.
	 * @throws XNATException
	 */
	protected void addProject() throws XNATException {
		long start = System.currentTimeMillis();
		try {
			XNATSearch search = new XNATSearch(conn);
			
			SearchWhere where = new SearchWhere(SearchMethod.AND,
				new SingleCriteria(Projects.ID.getSummary(), CompareOperator.EQUAL, projectId));
			SearchQuery query = new SearchQuery(projectId, Projects.ELEMENT, 
				projectFields, where); 
			XNATResultSet result = search.runSearch(query);
			
			List<? extends XNATResultSetRow> rows = result.getRows();
			XNATResultSetRow firstRow = rows.get(0);
			if( rows.size() > 1 )
				_log.warn("Too many project results: " + rows.size());
			
			Table nodeTable = dataTree.getNodeTable();
			
			int rootRowIdx = dataTree.addRootRow();
			nodeTable.set(rootRowIdx, "name", projectId);
			nodeTable.set(rootRowIdx, NodeType.COLUMN, NodeType.PROJECT);
			for( SearchField f : projectFields )
				nodeTable.set(rootRowIdx, f.getSummary(), firstRow.getValue(f));
		} finally {
			long end = System.currentTimeMillis();
			_log.info("addProject took " + (end - start) + " ms");
		}
	}
	
	/**
	 * Fetches subjects and adds them to the tree.
	 * @throws XNATException
	 */
	protected void addSubjects() throws XNATException {
		long start = System.currentTimeMillis();
		try {
			XNATSearch search = new XNATSearch(conn);
			
			SearchWhere where = new SearchWhere(SearchMethod.AND,
				new SingleCriteria(Subjects.PROJECT.getSummary(), CompareOperator.EQUAL, projectId));
			SearchQuery query = new SearchQuery(projectId, Subjects.ELEMENT, 
				subjectFields, where); 
			XNATResultSet result = search.runSearch(query);
			
			Table nodeTable = dataTree.getNodeTable();
			int rootRowIdx = dataTree.getRootRow();
			for( XNATResultSetRow row : result.getRows() ) {
				String subjectId = row.getValue(Subjects.SUBJECTID);
				
				int subjRowIdx;
				if( subjToRow.containsKey(subjectId) ) {
					subjRowIdx = subjToRow.get(subjectId);
				} else {
					// add new subject and connect it to the root
					subjRowIdx = nodeTable.addRow();
					nodeTable.set(subjRowIdx, "name", subjectId);
					nodeTable.set(subjRowIdx, NodeType.COLUMN, NodeType.SUBJECT);
					for( SearchField f : subjectFields ) {
						try {
							nodeTable.set(subjRowIdx, f.getSummary(), row.getValue(f));
						} catch( IllegalArgumentException e ) {
							_log.error("No values for field: " + f);
						}
					}
					
					dataTree.addEdge(rootRowIdx, subjRowIdx);
					subjToRow.put(subjectId, subjRowIdx);
				}
			}
		} finally {
			long end = System.currentTimeMillis();
			_log.info("addSubjects took " + (end - start) + " ms");
		}
	}
	
	/**
	 * Returns the expected session and scan fields.
	 * @return the fields
	 */
	protected List<SearchField> getSessionScanFields() {
		LinkedHashSet<SearchField> searchFields = new LinkedHashSet<SearchField>(XNATDefaults.DEFAULT_SEARCH_FIELDS);
		searchFields.addAll(sessionFields);
		searchFields.addAll(scanFields);
		return new ArrayList<SearchField>(searchFields);
	}
	
	/**
	 * Adds the sessions and scans to the tree.
	 * @throws XNATException
	 */
	protected void addSessionsScans() throws XNATException {
		long start = System.currentTimeMillis();
		try {
			XNATSearch search = new XNATSearch(conn);
			

			
			String searchRoot = XNATDefaults.DEFAULT_SEARCH_ROOT;
			List<SearchField> searchFields = getSessionScanFields();

			SearchWhere where = new SearchWhere(SearchMethod.AND, 
				new SingleCriteria(Sessions.PROJECT.getSummary(), CompareOperator.EQUAL, projectId));
			
			SearchQuery query = new SearchQuery(projectId, searchRoot, searchFields, where);
			XNATResultSet result = search.runSearch(query);
			
			Table nodeTable = dataTree.getNodeTable();
			int rootRowIdx = dataTree.getRootRow();
			
			// process into tree form
			for( XNATResultSetRow row : result.getRows() ) {
				String subjectId = row.getValue(Sessions.SUBJECT_ID);
				
				int subjRowIdx;
				if( subjToRow.containsKey(subjectId) ) {
					subjRowIdx = subjToRow.get(subjectId);
				} else {
					// add new subject and connect it to the root
					subjRowIdx = nodeTable.addRow();
					nodeTable.set(subjRowIdx, "name", subjectId);
					nodeTable.set(subjRowIdx, NodeType.COLUMN, NodeType.SUBJECT);
					
					// other fields are not available in this context
					_log.warn("Subject (" + subjectId + ") in scan result but not project/subject result.");
					
					dataTree.addEdge(rootRowIdx, subjRowIdx);
					subjToRow.put(subjectId, subjRowIdx);
				}
				
				String sessionId = row.getValue(Sessions.SESSION_ID);
				
				int sessRowIdx;
				if( sessToRow.containsKey(sessionId) ) {
					sessRowIdx = sessToRow.get(sessionId);
				} else {
					// add new session and connect it to the subject
					sessRowIdx = nodeTable.addRow();
					nodeTable.set(sessRowIdx, "name", sessionId);
					nodeTable.set(sessRowIdx, NodeType.COLUMN, NodeType.SESSION);
					for( SearchField f : sessionFields ) {
						nodeTable.set(sessRowIdx, f.getSummary(), row.getValue(f));
					}
					dataTree.addEdge(subjRowIdx, sessRowIdx);
					sessToRow.put(sessionId, sessRowIdx);
				}
				
				// missing type means no scans
				if( row.getMissingFields().contains(Scans.TYPE) )
					continue;
				
				String scanId = row.getValue(Scans.ID);
				int scanRowIdx = nodeTable.addRow();
				nodeTable.set(scanRowIdx, "name", scanId);
				nodeTable.set(scanRowIdx, NodeType.COLUMN, NodeType.SCAN);
				
				for( SearchField f : scanFields )
					nodeTable.set(scanRowIdx, f.getSummary(), row.getValue(f));
				
				dataTree.addEdge(sessRowIdx, scanRowIdx);
			}
		} finally {
			long end = System.currentTimeMillis();
			_log.info("addSessionsScans took " + (end - start) + " ms");
		}
	}
}
