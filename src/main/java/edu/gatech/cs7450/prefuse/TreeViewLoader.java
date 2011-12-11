package edu.gatech.cs7450.prefuse;

import java.util.HashMap;

import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tree;
import edu.gatech.cs7450.prefuse.TreeView.NodeType;
import edu.gatech.cs7450.xnat.XNATConstants.Scans;
import edu.gatech.cs7450.xnat.XNATConstants.Sessions;
import edu.gatech.cs7450.xnat.XNATResultSet;
import edu.gatech.cs7450.xnat.XNATResultSet.XNATResultSetRow;

public class TreeViewLoader {
	private static Schema nodeSchema, edgeSchema;
	static {
		nodeSchema = new Schema();
		nodeSchema.addColumn("name", String.class);
		nodeSchema.addColumn(NodeType.COLUMN, NodeType.class);
		
		edgeSchema = new Schema();
		edgeSchema.addColumn(Tree.DEFAULT_SOURCE_KEY, int.class);
		edgeSchema.addColumn(Tree.DEFAULT_TARGET_KEY, int.class);
	}
	
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
}
