package edu.gatech.cs7450.prefuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.RendererFactory;
import prefuse.util.ColorLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceItem;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

public class ScanGroupView extends JPanel {
	private static final long serialVersionUID = 1L;
	
	protected Graph graph;
	protected Visualization vis;
	protected Display display;
	
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
		MAX_WEIGHT = 5f,
		TRANSLATE_WEIGHT = 20f,
		SCALE_WEIGHT = 10f;
	protected Action createLayoutActions() {
		ActionList actions = new ActionList(Activity.INFINITY);
		ForceSimulator sim = new ForceSimulator();
		sim.addForce(new NBodyForce(NBodyForce.DEFAULT_GRAV_CONSTANT, 50, NBodyForce.DEFAULT_THETA));
		sim.addForce(new SpringForce());
		sim.addForce(new DragForce());
		ForceDirectedLayout layout = new ForceDirectedLayout("graph", sim, false, false) {
			@Override
			protected float getSpringCoefficient(EdgeItem e) {
				return 1.0E-5f;
			}
			
			@Override
			protected float getSpringLength(EdgeItem e) {
				float weight = e.getFloat("weight");
				return TRANSLATE_WEIGHT + (SCALE_WEIGHT * (1f - (weight / MAX_WEIGHT)));
			}
		};
		actions.add(layout);
		actions.add(new RepaintAction());
		return actions;
	}

	protected RendererFactory createRendererFactory() {
		// Renderers
		LabelRenderer r = new LabelRenderer("scanGroup");
		r.setRoundedCorner(20, 20); // round the corners
		return new DefaultRendererFactory(r);		
	}
	
	protected ActionList createColorActions() {
		
		// Colors for text and edges
		ColorAction text = new ColorAction("graph.nodes" , VisualItem.TEXTCOLOR, ColorLib.gray(0));
		ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));
		
		ActionList color = new ActionList();
		color.add(text);
		color.add(edges);
		return color;
	}
	
	/**
	 * @param args
	 */
	static int edgeCount;
	static Edge edgeTuple; 
	static String edgeTupleStr;
	static String[] tempArr;
	static String[] edgeTupleArr;
	static ArrayList al = new ArrayList();
	static String[] tempArrNodes;
	static int srcNode;
	static int trgtNode;
	static double wt;
	static int checkingNode;
	static int[] arrSrcNodes;
	static int[]arrTrgtNodes;
	static double[] arrWt;
	static int[] checkedNodeArr;
	static int flag = 0;
	static double[] tempArrWt;
	static int[] tempArrTrgt;
	static int m;
	
	//static double[]
	public static void main(String[] args) {
		String inFile = "/testing.xml";
		if( args.length > 0 )
			inFile = args[0];

		prefuse.data.Graph graph = null;
		
		try {
			graph =  new GraphMLReader().readGraph(inFile);
			//System.out.println("File Reading Success");
			
		}
		catch (DataIOException e) {
			e.printStackTrace();
			System.err.println("Error Loading the graph.. ");
			System.exit(1);
			
		}

		

		//----- Visualization ---/// 
		
		Visualization viz = new Visualization();	
		viz.add("graph", (prefuse.data.Graph) graph);
		viz.setInteractive(	"graph.nodes", null, true);
		
		// Renderers
		LabelRenderer r = new LabelRenderer("scanGroup");
		r.setRoundedCorner(20, 20); // round the corners
		viz.setRendererFactory(new DefaultRendererFactory(r));
		
		// Colors for text and edges
		ColorAction text = new ColorAction("graph.nodes" , VisualItem.TEXTCOLOR, ColorLib.gray(0));
		ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));
		
		ActionList color = new ActionList();
		color.add(text);
		color.add(edges);
		
		// Laying out the graph 
		ActionList layout = new ActionList(Activity.INFINITY);
		layout.add(new ForceDirectedLayout("graph"));
//		ForceDirectedLayout fdLayout = new ForceDirectedLayout("graph");
//		ForceSimulator fdSim = new ForceSimulator();
////		fdSim.addForce(new NBodyForce());
//		fdSim.addForce(new SpringForce());
////		fdSim.addForce(new DragForce());
//		fdLayout.setForceSimulator(fdSim);
//		layout.add(fdLayout);
		layout.add(new RepaintAction());
		
		// Adding the actions to the viz
		viz.putAction("color", color);
		viz.putAction("layout", layout);
		
// TODO custom start
		
		// Getting the count of all the existing edges in the graph
		edgeCount = graph.getEdgeCount();

		// map source nodes to outgoing edges
		HashMap<Node, List<Edge>> sourceToEdges = new HashMap<Node, List<Edge>>();
		
		for( int i = 0, ilen = edgeCount; i < ilen; ++i ) {
			Edge edge = graph.getEdge(i);
			Node source = edge.getSourceNode(), target = edge.getTargetNode();
			Double weight = edge.getDouble("weight");

			if( sourceToEdges.containsKey(source) ) {
				sourceToEdges.get(source).add(edge);
			} else {
				ArrayList<Edge> newEdges = new ArrayList<Edge>();
				newEdges.add(edge);
				sourceToEdges.put(source, newEdges);
			}
		}
		
		// for every source node...
		for( Map.Entry<Node, List<Edge>> entry : sourceToEdges.entrySet() ) {
			Node source = entry.getKey();
			List<Edge> outEdges = entry.getValue();
			ForceDirectedLayout newLayout = new ForceDirectedLayout("graph.subgraph" + source);
			Graph subGraph = new Graph();
			subGraph.addColumn("weight", double.class);
			Node newSource = subGraph.addNode();
			for( Edge edge : outEdges ) {
				Node newTarget = subGraph.addNode();
				Edge newEdge = subGraph.addEdge(newSource, newTarget);
				Double weight = edge.getDouble("weight");
				newEdge.setDouble("weight", weight);

				
				newLayout.getForceSimulator().addSpring(new ForceItem(), new ForceItem(), weight.floatValue(), 500);
			}
			
			viz.add("graph.subgraph" + source, subGraph);
			layout.add(newLayout);
		}
		viz.putAction("layout", layout);
		
		
		Display display = new Display(viz);
		display.setSize(720, 500);
		display.addControlListener(new DragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		
		JFrame frame = new JFrame("blah");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(display);
		frame.pack();
		frame.setVisible(true);
		
		viz.run("color");
		viz.run("layout");

		
	}

}
