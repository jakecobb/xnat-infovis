package edu.gatech.cs7450.prefuse;
/**
 * The Size of the scan group has been fixed and the colors have been fixed
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.column.Column;
import prefuse.data.event.ColumnListener;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.data.tuple.TableTuple;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.RendererFactory;
import prefuse.util.ColorLib;
import prefuse.util.PrefuseLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import edu.gatech.cs7450.Pair;
import edu.gatech.cs7450.prefuse.controls.HTMLToolTipControl;
import edu.gatech.cs7450.prefuse.controls.TableToolTipControl;

public class ScanGroupView extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger _log = Logger.getLogger(ScanGroupView.class);
	
	/**
	 * Checks if a visual item is for a subject node.
	 * 
	 * @param item the visual item to check
	 * @return if <code>item</code> is for a subject node
	 */
	private static final boolean isSubjectItem(VisualItem item) {
		if( item instanceof NodeItem ) {
			Node node = (Node)item.getSourceTuple();
			if( node == null ) {
				_log.warn("No backing node for: " + item);
			} else {
				return "subject".equalsIgnoreCase(node.getString("type"));
			}
		}
		return false;
	}
	
	/**
	 * Checks if a visual item is for a scan group node.
	 * 
	 * @param item the visual item to check
	 * @return if <code>item</code> is for a scan group node
	 */
	private static final boolean isScanGroupItem(VisualItem item) {
		if( item instanceof NodeItem ) {
			Node node = (Node)item.getSourceTuple();
			if( node == null ) {
				_log.warn("No backing node for: " + item);
			} else {
				return "scan".equalsIgnoreCase(node.getString("type"));
			}
		}
		return false;
	}
	
	protected Graph graph;
	protected Visualization vis;
	protected Display display;
	protected String m_edgeGroup;
	
	/** Tracks Node objects by scan group / subject id. */
	private NodeTracker nodeTracker;
	
	private Stack<String> unusedColors = new Stack<String>();
	
	public ScanGroupView(String xmlPath) throws DataIOException {
		graph =  new GraphMLReader().readGraph(xmlPath);
		createVisualization();

	}
	
	public ScanGroupView(Graph graph) {
		if( graph == null ) throw new NullPointerException("graph is null");
		this.graph = graph;
		
		createVisualization();
	}
	
	public Node getNodeForScanGroup(String id) {
		return nodeTracker.getNodeForScanGroup(id);
	}
	
	public Graph getGraph() {
		return graph;
	}
	
	protected void createVisualization() {
		nodeTracker = new NodeTracker();
		
		vis = new Visualization();
		vis.add("graph", graph);
		vis.setInteractive("graph.nodes", null, true); // allow node interaction
		
		vis.setRendererFactory(this.createRendererFactory());

		vis.putAction("color", this.createColorActions());	
		vis.putAction("layout", this.createLayoutActions());
		
		setupDisplay();
	}
	
	@SuppressWarnings("serial")
	protected void setupDisplay() {
		Dimension minSize = new Dimension(720, 500);
		display = new Display(vis);
		display.setSize(minSize);
		display.setMinimumSize(minSize);
		display.setHighQuality(true);
		
		this.setFocusable(false);
		if( _log.isTraceEnabled() ) {
			display.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
					_log.trace("display.focusLost: " + e);
				}
				public void focusGained(FocusEvent e) {
					_log.trace("display.focusGained: " + e);
				}
			});
		}
		
		// tooltips
		HTMLToolTipControl subjectToolTip = new TableToolTipControl("scanGroup", "project", "label") {
			@Override
			public void itemEntered(VisualItem item, MouseEvent e) {
				if( isSubjectItem(item) )
					super.itemEntered(item, e);
			}
			@Override
			public void itemExited(VisualItem item, MouseEvent e) {
				if( isSubjectItem(item) )
					super.itemExited(item, e);
			}
		};
		subjectToolTip.setShowLabel(true);
		subjectToolTip.setLabelOverrides("ID", "Project", "Label");
		display.addControlListener(subjectToolTip);
		
		HTMLToolTipControl scanGroupToolTip = new TableToolTipControl("scanGroup", "nscans", "scan_list") {
			@Override
			public void itemEntered(VisualItem item, MouseEvent e) {
				if( isScanGroupItem(item) )
					super.itemEntered(item, e);
			}
			
			@Override
			public void itemExited(VisualItem item, MouseEvent e) {
				if( isScanGroupItem(item) )
					super.itemExited(item, e);
			}
			
			@Override
			protected String getFieldValue(VisualItem item, String field) {
				if( !"scan_list".equals(field) )
					return super.getFieldValue(item, field);
				
				// show at least a subset of scans
				try {
					final int MAX_SCANS = 10; // ellide any over this number
					
					@SuppressWarnings("unchecked")
					List<String> scanList = (List<String>)item.get(field);
					StringBuilder b = new StringBuilder();
					int i = 0;
					for( String scanId : scanList ) {
						b.append(escapeHtml(scanId)).append("<br />");
						if( i++ >= MAX_SCANS ) {
							// limit hit, ellide the rest
							b.append('[').append(scanList.size() - MAX_SCANS).append("&nbsp;more...]");
							break;
						}
					}
					return b.toString();
				} catch( ClassCastException e ) {
					_log.error("scan_list was not a List", e);
					return super.getFieldValue(item, field);
				}
			}
		};
		scanGroupToolTip.setShowLabel(true);
		scanGroupToolTip.setLabelOverrides("ID", "# Scans", "Scans");
		display.addControlListener(scanGroupToolTip);
		
		// double-click to toggle fixed position
		display.addControlListener(new ControlAdapter() {
			@Override
			public void itemClicked(VisualItem item, MouseEvent e) {
				if( SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2 ) {
					if( _log.isDebugEnabled() ) {
						_log.debug("toggle.itemClicked: " + e);
						_log.debug("item.isFixed: " + item.isFixed());
					}
					item.setFixed(!item.isFixed());
				}
			}
		});

		display.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0,true), "DEL released");
		display.getActionMap().put("DEL released", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( _log.isDebugEnabled() ) _log.debug("delete.actionPerformed: " + e);
				
				// stop rendering while we process
				pause();
				
				try {
					// gather nodes to delete
					ArrayList<Pair<VisualItem, Node>> toRemove = new ArrayList<Pair<VisualItem, Node>>();
					for( @SuppressWarnings("unchecked") Iterator<VisualItem> iter = vis.items(Visualization.FOCUS_ITEMS); iter.hasNext(); ) {
						VisualItem item = iter.next();
						if( item instanceof NodeItem )
							toRemove.add(Pair.make(item, (Node)item.getSourceTuple()));
					}
					
					if( toRemove.size() == 0 )
						return;
					
					// make sure
					int choice = JOptionPane.showConfirmDialog(display, "Really delete " + toRemove.size() + " nodes?", 
						"Confirm Delete", JOptionPane.YES_NO_OPTION);
					if( choice != JOptionPane.YES_OPTION )
						return;
					
					// delete the nodes, tracking freed scan group colors
					LinkedHashSet<String> returnColors = new LinkedHashSet<String>();
					TupleSet focusGroup = vis.getGroup(Visualization.FOCUS_ITEMS);
					for( Pair<VisualItem, Node> pair : toRemove ) {
						VisualItem focusItem = pair.getFirst();
						Node node = pair.getSecond();
						
						// remove the node
						if( "scan".equals(node.getString("type")) )
							returnColors.add(node.getString("color"));
						graph.removeTuple(node);
						
						// and the visual item from the focus group
						focusGroup.removeTuple(focusItem);
					}
					
					// return the colors to the available pool
					for( String color : returnColors )
						returnColor(color);
					
				} finally {
					begin(); // resume rendering
				}
			}
		});
		
		display.addControlListener(new DragControl(false, false) {
			@Override
			public void itemReleased(VisualItem item, MouseEvent e) {
				if( !SwingUtilities.isLeftMouseButton(e) ) return;
				
				// super doesn't remove the fixed setting unless it was dragged, causing it to become stuck
				try {
					// nullify the private activeItem field
					Field activeItem = DragControl.class.getDeclaredField("activeItem");
					activeItem.setAccessible(true);
					activeItem.set(this, null);
				} catch( Exception ex ) {
					_log.error("Couldn't nullify activeItem.", ex);
				}
            item.getTable().removeTableListener(this);
            if ( resetItem ) item.setFixed(wasFixed);
            dragged = false;
			}
		});
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomToFitControl());
		display.addControlListener(new ZoomControl());
		display.addControlListener(new FocusControl(1, "color"));
		display.addControlListener(new ControlAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if( _log.isDebugEnabled() ) _log.debug("focusControl.mouseClicked: " + e);
				display.requestFocusInWindow();
			}
			@Override
			public void itemClicked(VisualItem item, MouseEvent e) {
				if( _log.isDebugEnabled() ) _log.debug("focusControl.itemClicked: " + e);
				display.requestFocusInWindow();
			}
		});
		
		// make the display fill the available space
		BorderLayout layout = new BorderLayout();
		this.setLayout(layout);
		this.add(display, BorderLayout.CENTER);
	}
	
	public void begin() {
		vis.run("color");
		vis.run("layout");
	}
	
	public void pause() {
		vis.cancel("color");
		vis.cancel("layout");
	}
	
	public Visualization getVisualization() {
		return vis;
	}
	
	public Display getDisplay() {
		return display;
	}
	
	private static final float 
		MAX_WEIGHT = 30f,
		TRANSLATE_WEIGHT = 50f,
		SCALE_WEIGHT = 100f;
	protected Action createLayoutActions() {
		ActionList actions = new ActionList(Activity.INFINITY);
		ForceSimulator sim = new ForceSimulator();
		sim.addForce(new NBodyForce(NBodyForce.DEFAULT_GRAV_CONSTANT, 50, NBodyForce.DEFAULT_THETA));
		sim.addForce(new SpringForce());
		sim.addForce(new DragForce());
		ForceDirectedLayout layout = new ForceDirectedLayout("graph", sim, false, false) {
			
			@Override
			protected void initSimulator(ForceSimulator fsim) {
				super.initSimulator(fsim);
				
				Iterator<?> iter = m_vis.visibleItems(m_nodeGroup);
				while( iter.hasNext() ) {
					VisualItem item = (VisualItem)iter.next();
					float mass = getMassValue(item);
					item.setSize(mass);
				}
			}
			
			@Override
			protected float getSpringCoefficient(EdgeItem e) {
				return 1.0E-5f;
			}
			
			@Override
			protected float getSpringLength(EdgeItem e) {
				float weight = e.getFloat("weight");
				return TRANSLATE_WEIGHT + (SCALE_WEIGHT * (1f - (weight / MAX_WEIGHT)));
			}
			@Override
			protected float getMassValue(VisualItem n) {
				//System.out.println(n.getString(m_nodeGroup));
		        float size = n.getFloat("size");
		        float newSize = (float) Math.log(size);
		        if (size == 0.0) {
		        	newSize = 1;
		        }
		        return newSize;
		    }
		};
		actions.add(layout);
		actions.add(new RepaintAction());
		return actions;
	}
 
 
	protected RendererFactory createRendererFactory() {
		// Renderers
		LabelRenderer r = new LabelRenderer("scanGroup");
		r.setRoundedCorner(40, 40); // round the corners
		return new DefaultRendererFactory(r);		
	}
	
	
	
	//Processing Actions
	
	int[] pallete = new int[]{
		   
			//ColorLib.rgb(getRed(), getGreen(), getBlue()) 
			ColorLib.rgb(0, 191, 255) 
			,ColorLib.rgb(0, 245, 255)
			,ColorLib.rgb(78, 238, 148)
			,ColorLib.rgb(255, 255, 0)
			,ColorLib.rgb(0, 245, 255)
			,ColorLib.rgb(238, 180, 34)
			,ColorLib.rgb(255, 69, 0)
			,ColorLib.rgb(183, 183, 183)
			,ColorLib.rgb(238, 130, 238)
			
			
	};
	
	protected ActionList createColorActions() {
		final int NUM_COLORS = pallete.length - 1;
		ArrayList<String> colorLabels = new ArrayList<String>(NUM_COLORS);
		for( int i = 0; i < NUM_COLORS; ++i ) {
			colorLabels.add("color" + i);
		}
		HashSet<String> availableColors = new LinkedHashSet<String>(colorLabels); // colors not already used by a scan group
		colorLabels.add("subjectColor");
		
		DataColorAction fill = new DataColorAction("graph.nodes", "color", Constants.NOMINAL, VisualItem.FILLCOLOR, pallete) {
			@Override
			public int getColor(VisualItem item) {
				if( item.isInGroup(Visualization.FOCUS_ITEMS) )
					return ColorLib.gray(0);
				return super.getColor(item);
			}
		};
		fill.setOrdinalMap(colorLabels.toArray());
		
		// Colors for text and edges
		ColorAction text = new ColorAction("graph.nodes" , VisualItem.TEXTCOLOR, ColorLib.gray(0)) {
			@Override
			public int getColor(VisualItem item) {
				if( item.isInGroup(Visualization.FOCUS_ITEMS) )
					return ColorLib.gray(255);
				return super.getColor(item);
			}
		};
		
		DataColorAction edgeColors = new DataColorAction("graph.edges", "color", Constants.NOMINAL, VisualItem.STROKECOLOR, pallete);
		edgeColors.setOrdinalMap(colorLabels.toArray());
		
		
		// set subject nodes to the single subject color
		for( Iterator<?> nodeIter = vis.visibleItems("graph.nodes"); nodeIter.hasNext(); ) {
			NodeItem node = (NodeItem)nodeIter.next();
			if( "subject".equalsIgnoreCase(node.getString("type")) )
				node.setString("color", "subjectColor");
		}
		
		// Coloring the edges
		m_edgeGroup = PrefuseLib.getGroupName("graph", Graph.EDGES);
		if( m_edgeGroup != null ) {
			Iterator<?> iter = vis.visibleItems(m_edgeGroup);
			while( iter.hasNext() ) {

				EdgeItem e = (EdgeItem)iter.next();
				NodeItem srcNode = e.getSourceItem();
				NodeItem targetNode = e.getTargetItem();

				// determine which is the scan group
				NodeItem scanNode;
				if( "scan".equalsIgnoreCase(srcNode.getString("type")) ) {
					scanNode = srcNode;
				} else if( "scan".equalsIgnoreCase(targetNode.getString("type")) ) {
					scanNode = targetNode;
				} else {
					throw new RuntimeException("FIXME: Neither is a scan node?");
				}

				// transfer scan group color to the edge
				String color = scanNode.getString("color");
				availableColors.remove(color); // color is in use
				e.setString("color", color);
			}
		}
		
		unusedColors.addAll(availableColors);
		
		ActionList color = new ActionList();
		color.add(fill);
		color.add(text);
		color.add(edgeColors);
		return color;
	}
	
	/**
	 * Returns whether an unused scan group color is available.
	 * @return if a color is available
	 */
	public boolean isColorAvailable() {
		return !unusedColors.isEmpty();
	}
	
	/**
	 * Returns an unused scan group color.
	 * <p>
	 * The color returned is not available again until returned.  Call 
	 * <code>returnColor(String)</code> to make it available again.
	 * </p>
	 * 
	 * @return the color label, or <code>null</code> if no colors are available
	 */
	public String reserveColor() {
		if( !unusedColors.isEmpty() )
			return unusedColors.pop();
		return null;
	}
	
	/**
	 * Makes a color label available again for new scan groups.
	 * @param color the color to return
	 */
	public void returnColor(String color) {
		if( color == null ) throw new NullPointerException("color is null");
		if( !color.matches("color[0-9]+") )
			_log.warn("Unexpected color label: " + color);

		unusedColors.push(color);
	}
	
	public int getColor(VisualItem item , ColorAction c){
		NodeItem nitem = (NodeItem) item;
		int testColor = c.getColor(nitem);
		
		/*
		int testColor = nitem.getEndFillColor();
		System.out.println(testColor);
		*/
		return testColor;
		
	}
	
	/**
	 * Track nodes by "scanGroup" value, which is subject-id for subjects and the 
	 * group name for scan groups.
	 */
	private class NodeTracker implements ColumnListener {
		private HashMap<String, Node> scanGroupToNode = new HashMap<String, Node>();
		
		private NodeTracker() {
			// initialize current mapping
			for( Iterator<?> iter = graph.getNodes().tuples(); iter.hasNext(); ) {
				TableTuple tuple = (TableTuple)iter.next();
				Node node = graph.getNode(tuple.getRow());
				scanGroupToNode.put(node.getString("scanGroup"), node);
			}
			
			// monitor future changes
			Table nodeTable = graph.getNodeTable();
			Column sgCol = nodeTable.getColumn("scanGroup");
			sgCol.addColumnListener(this);
		}
		
		public Node getNodeForScanGroup(String id) {
			if( id == null ) throw new NullPointerException("id is null");
			return scanGroupToNode.get(id);
		}


		@Override
		public void columnChanged(Column src, int idx, Object prev) {
			if( _log.isDebugEnabled() )
				_log.debug("src=" + src + ", idx=" + idx + ", prev=" + prev);
			
			// prev == null -> newly added row
			if( prev != null )
				scanGroupToNode.remove(prev);
			
			String newId = src.getString(idx);
			
			// newId == null -> removed row
			if( newId != null )
				scanGroupToNode.put(newId, graph.getNode(idx));
		}

		@Override
		public void columnChanged(Column src, int type, int start, int end) {
			_log.error("src=" + src + ", type=" + type + ", start=" + start + ", end=" + end);
			throw new UnsupportedOperationException("FIXME: Not implemented.");
		}

		@Override
		public void columnChanged(Column src, int idx, int prev) {
			throw new UnsupportedOperationException("Expecting strings only.");
		}

		@Override
		public void columnChanged(Column src, int idx, long prev) {
			throw new UnsupportedOperationException("Expecting strings only.");
		}

		@Override
		public void columnChanged(Column src, int idx, float prev) {
			throw new UnsupportedOperationException("Expecting strings only.");
		}

		@Override
		public void columnChanged(Column src, int idx, double prev) {
			throw new UnsupportedOperationException("Expecting strings only.");			
		}

		@Override
		public void columnChanged(Column src, int idx, boolean prev) {
			throw new UnsupportedOperationException("Expecting strings only.");			
		}
	}
}
