package edu.gatech.cs7450.prefuse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.Table;
import edu.gatech.cs7450.xnat.XNATConnection;
import edu.gatech.cs7450.xnat.XNATConstants.Scans;
import edu.gatech.cs7450.xnat.XNATConstants.Sessions;
import edu.gatech.cs7450.xnat.XNATException;
import edu.gatech.cs7450.xnat.XNATResultSet;
import edu.gatech.cs7450.xnat.XNATResultSet.XNATResultSetRow;
import edu.gatech.cs7450.xnat.XNATSearch;
import edu.gatech.cs7450.xnat.XNATTableResult;
import edu.gatech.cs7450.xnat.XNATTableResult.XNATTableRow;

public class ScanGroupViewLoader {
	private static final Logger _log = Logger.getLogger(ScanGroupViewLoader.class);
	
	private static Schema nodeSchema;
	private static Schema edgeSchema;
	static {
		nodeSchema = new Schema();
		nodeSchema.addColumn("scanGroup", String.class);
		nodeSchema.addColumn("type", String.class);
		nodeSchema.addColumn("size", double.class);
		nodeSchema.addColumn("color", String.class);
		
		edgeSchema = new Schema();
		edgeSchema.addColumn(Graph.DEFAULT_SOURCE_KEY, int.class);
		edgeSchema.addColumn(Graph.DEFAULT_TARGET_KEY, int.class);
		edgeSchema.addColumn("weight", double.class);
		edgeSchema.addColumn("color", String.class);
	}
	
	/**
	 * Loads an initial graph for {@link ScanGroupView} with all the subject nodes.
	 * 
	 * @param conn the connection to fetch subjects from
	 * @return the graph
	 * @throws NullPointerException if <code>conn</code> is <code>null</code>
	 * @throws XNATException        if the subject fetch fails
	 */
	public static Graph loadSubjects(XNATConnection conn) throws XNATException {
		if( conn == null ) throw new NullPointerException("conn is null");
		
		XNATSearch search = new XNATSearch(conn);
		
		XNATTableResult subjects = search.fetchSubjects();
		
		Table nodeTable = nodeSchema.instantiate(),
		      edgeTable = edgeSchema.instantiate();
		
		for( XNATTableRow row : subjects.getRows() ) {
			int rowIdx = nodeTable.addRow();
			nodeTable.set(rowIdx, "scanGroup", row.getValue("ID"));
			nodeTable.set(rowIdx, "type", "subject");
			nodeTable.set(rowIdx, "size", 0.0d);
			nodeTable.set(rowIdx, "color", "subjectColor");
		}
		
		Graph graph = new Graph(nodeTable, edgeTable, false);
		return graph;
	}
	
	/**
	 * Adds a search result as a new scan group.
	 * 
	 * @param view      the view to add the group to
	 * @param groupName the name of the new group
	 * @param result    the search result
	 * @throws NullPointerException if any argument is <code>null</code>
	 */
	public static void addResultAsScanGroup(ScanGroupView view, String groupName, XNATResultSet result) {
		if( view == null ) throw new NullPointerException("view is null");
		if( groupName == null ) throw new NullPointerException("groupName is null");
		if( result == null ) throw new NullPointerException("result is null");
		
		String nextColor = view.reserveColor();
		if( nextColor == null )
			throw new IllegalStateException("No more scan group colors available.");

		Graph graph = view.getGraph();
		
		// see if the name is already in use
		if( view.getNodeForScanGroup(groupName) != null ) {

			// search for an unused suffix
			StringBuilder nameBuilder = new StringBuilder(groupName).append(" [");
			int preLen = nameBuilder.length();
			int i = 2;
			do {
				nameBuilder.setLength(preLen);
				nameBuilder.append(i++).append(']');
			} while( null != view.getNodeForScanGroup(nameBuilder.toString()) );
			
			// use the available suffix name
			groupName = nameBuilder.toString();
		}
		
		LinkedHashMap<String, List<XNATResultSetRow>> subjToScans = 
			new LinkedHashMap<String, List<XNATResultSetRow>>();
		
		// count valid scans and group them by subject
		int nScans = 0;
		for( XNATResultSetRow row : result.getRows() ) {
			if( !row.getMissingFields().contains(Scans.TYPE) ) {
				String subjectId = row.getValue(Sessions.SUBJECT_ID);
				if( subjToScans.containsKey(subjectId) ) {
					subjToScans.get(subjectId).add(row);
				} else {
					ArrayList<XNATResultSetRow> scans = new ArrayList<XNATResultSetRow>();
					scans.add(row);
					subjToScans.put(subjectId, scans);
				}
				++nScans;
			}
		}
		
		view.pause(); // stop rendering while we update

		// add the new scan group node
		Node sgNode = graph.addNode();
		sgNode.set("scanGroup", groupName);
		sgNode.set("type", "scan");
		sgNode.set("size", (double)nScans);
		sgNode.set("color", nextColor);
		
		// connect edges for each subject
		for( Map.Entry<String, List<XNATResultSetRow>> entry : subjToScans.entrySet() ) {
			String subjectId = entry.getKey();
			List<XNATResultSetRow> scans = entry.getValue();
			
			Node subjNode = view.getNodeForScanGroup(subjectId);
			if( subjNode == null ) {
				_log.error("No node found for subject: " + subjectId);
				continue;
			}
			
			Edge edge = graph.addEdge(subjNode, sgNode);
			edge.setDouble("weight", scans.size() / (double)nScans); // FIXME check this formula
			edge.setString("color", nextColor);
		}
		
		view.begin(); // resume rendering
	}
}
