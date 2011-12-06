package edu.gatech.cs7450.prefuse;
/**
 * The Size of the scan group has been fixed and the colors have been fixed
 */
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;

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
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
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

public class ScanGroupView extends JPanel {
	private static final long serialVersionUID = 1L;
	
	protected Graph graph;
	protected Visualization vis;
	protected Display display;
	protected String m_edgeGroup;
	
	public ScanGroupView(String xmlPath) throws DataIOException {
		graph =  new GraphMLReader().readGraph(xmlPath);
		createVisualization();

	}
	
	public ScanGroupView(Graph graph) {
		if( graph == null ) throw new NullPointerException("graph is null");
		this.graph = graph;
		
		createVisualization();
	}
	
	protected void createVisualization() {
		vis = new Visualization();
		vis.add("graph", graph);
		vis.setInteractive("graph.nodes", null, true); // allow node interaction
		
		vis.setRendererFactory(this.createRendererFactory());

		vis.putAction("color", this.createColorActions());	
		vis.putAction("layout", this.createLayoutActions());
		
		setupDisplay();
	}
	
	protected void setupDisplay() {
		display = new Display(vis);
		display.setSize(720, 500);
		display.addControlListener(new DragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		this.setSize(720, 500);
		this.add(display);
	}
	
	public void begin() {
		vis.run("color");
		vis.run("layout");
		
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
		colorLabels.add("subjectColor");
		DataColorAction fill = new DataColorAction("graph.nodes", "color", Constants.NOMINAL, VisualItem.FILLCOLOR, pallete);
		fill.setOrdinalMap(colorLabels.toArray());
		
		// Colors for text and edges
		ColorAction text = new ColorAction("graph.nodes" , VisualItem.TEXTCOLOR, ColorLib.gray(0));
		
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
		 if ( m_edgeGroup != null ) {
	           Iterator iter = vis.visibleItems(m_edgeGroup);
	            while ( iter.hasNext() ) {
	            	
	                EdgeItem e  = (EdgeItem)iter.next();
	                NodeItem srcNode = e.getSourceItem();
	                NodeItem targetNode = e.getTargetItem();
	                
	                // determine which is the scan group
	                NodeItem scanNode;
	                if( "scan".equalsIgnoreCase(srcNode.getString("type")) ) {
	               	 scanNode = srcNode;
	                } else if ( "scan".equalsIgnoreCase(targetNode.getString("type")) ) {
	               	 scanNode = targetNode;
	                } else {
	               	 throw new RuntimeException("FIXME: Neither is a scan node?");
	                }
	                
	                // transfer scan group color to the edge
	                e.setString("color", scanNode.getString("color"));
	              }
	        }
		
		ActionList color = new ActionList();
		color.add(fill);
		color.add(text);
		color.add(edgeColors);
		return color;
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
	
	/*
	public Color createRandomColors(int i){

        for (int j = 0; j < i; j++){
        	int R = (int) (Math.random( )*256);
    		int G = (int)(Math.random( )*256);
    		int B= (int)(Math.random( )*256);
    		
    		Color randomColor = new Color(R, G, B);
    		
    		return randomColor;
	
        }
				
		
	}
	*/
}
