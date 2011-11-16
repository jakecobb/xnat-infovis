package edu.gatech.cs7450.prefuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import prefuse.Display;
import prefuse.Visualization;
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
import prefuse.data.Table;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.force.ForceItem;
import prefuse.visual.VisualItem;

public class ScanGroupView extends JPanel{

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
		viz.setInteractive(	"graph.nodes", null, false);
		
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
//		
//		
//		// Initializing all the arrays required
//		arrSrcNodes = new int[edgeCount];
//		arrTrgtNodes = new int[edgeCount];
//		arrWt = new double[edgeCount];
//		checkedNodeArr = new int[edgeCount];
//		tempArrWt = new double[edgeCount];
//		tempArrTrgt = new int[edgeCount];
//		
//		for (int i = 0; i < edgeCount; i++){
//			// getting the Tuple Instance for each edge.
//			edgeTuple = graph.getEdge(i);
//			
//		   edgeTupleStr = edgeTuple.toString();
//		   // Parsing the Tuple to get the node ids.
//		   String delimiter = "\\[";
//		  
//		   tempArr = edgeTupleStr.split(delimiter);
//		   delimiter = "\\]";
//		   edgeTupleArr = tempArr[1].split(delimiter);
//		   
//		  // al.add(edgeTupleArr[0]);
//		  
//		   delimiter = ",";
//		   tempArrNodes = edgeTupleArr[0].split(delimiter);
//		   
//		   // Creating 3 arrays --- one for src nodes , one for target nodes, and one for the weights
//		   arrSrcNodes[i] = Integer.parseInt(tempArrNodes[0]);
//		   arrTrgtNodes[i] = Integer.parseInt(tempArrNodes[1]);
//		   arrWt[i] = Double.parseDouble(tempArrNodes[2]);
//		  	   
//		}
//		
//		// To group a source node and its edges/ connections to source nodes together
//		// and process them one at a time. 
//		for (int i=0; i < edgeCount; i++){
//			flag = storeCheckingNode(arrSrcNodes[i]);
//			
//			if (flag == 1){
//				
//				checkedNodeArr[i] = arrSrcNodes[i];
//					m = 0;
//					for (int k = 0; k < checkedNodeArr.length; k++){
//						
//						if(arrSrcNodes[k] == checkedNodeArr[i]){
//							
//							tempArrWt[m] = arrWt[k];
//							tempArrTrgt[m] = arrTrgtNodes[k];
//							System.out.println(tempArrWt[m]);
//							System.out.println(tempArrTrgt[m]);
//							
//							m++;
//						}	
//					
//				}
//					Table tempTable = new Table(1,m);
//					
//					for (int j=0 ; j < m ;j++){
//						//ForceSimulator fSim = new ForceSimulator();
//						//fSim.addForce(new SpringForce(3,6));
//						//wanted to use the addSpring Method.
//					//tempTable.addColumn("target", tempArrTrgt[j]);	
//						
//						
//					}
//					// force direcred code
//					
//			}//end of if (flag)
//			else {
//				// do nothn.... 
//			}
//		}
//		
		
	}
	
	// Function to check if a node has been considered once or not (To avoid duplication)
	public static int storeCheckingNode(int node){
	
		for (int pos = 0; pos < checkedNodeArr.length ; pos++){
			System.out.println(checkedNodeArr[pos]);
			if (checkedNodeArr[pos] == node){
				flag = 0;
				return flag;
			}
			
		}
		return 1;
		
		
	}

}
