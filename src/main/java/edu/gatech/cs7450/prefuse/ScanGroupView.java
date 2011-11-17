package edu.gatech.cs7450.prefuse;

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
import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.RendererFactory;
import prefuse.util.ColorLib;
import prefuse.util.force.DragForce;
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
}
