package edu.gatech.cs7450.prefuse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.LocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.filter.FisheyeTreeFilter;
import prefuse.action.layout.CollapsedSubtreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.ControlAdapter;
import prefuse.controls.FocusControl;
import prefuse.controls.PanControl;
import prefuse.controls.SubtreeDragControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Tree;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.io.TreeMLReader;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.MathLib;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JSearchPanel;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;
public class TreeView extends Display {

    public static final String TREE_CHI = "/chi-ontology.xml.gz";
    private static final String tree = "tree";
    private static final String treeNodes = "tree.nodes";
    private static final String treeEdges = "tree.edges";
    public static String[] path = new String[50];
    static String pt;
    static int len;
    
    /** Types of nodes expected. */
    public static enum NodeType {
   	 PROJECT,
   	 SUBJECT,
   	 SESSION,
   	 SCAN;
   	 
   	 public static final String COLUMN = "node_type";
    }
    
    
    private LabelRenderer m_nodeRenderer;
    private EdgeRenderer m_edgeRenderer;
    
    private String m_label = "label";
    private int m_orientation = Constants.ORIENT_LEFT_RIGHT;
    public TreeView(Tree t, String label) {
        super(new Visualization());
        path[0] = "Categories";
        len = 1;
        m_label = label;
        m_vis.add(tree, t);
        m_nodeRenderer = new LabelRenderer(m_label);
        m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
        m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
        m_nodeRenderer.setRoundedCorner(14,14);
        m_edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_CURVE);
        
        DefaultRendererFactory rf = new DefaultRendererFactory(m_nodeRenderer);
        rf.add(new InGroupPredicate(treeEdges), m_edgeRenderer);
        m_vis.setRendererFactory(rf);
               
        // colors
        ItemAction nodeColor = new NodeColorAction(treeNodes);
        ItemAction textColor = new ColorAction(treeNodes,
                VisualItem.TEXTCOLOR, ColorLib.rgb(139,69,19));
        m_vis.putAction("textColor", textColor);
        
        ItemAction edgeColor = new ColorAction(treeEdges,
                VisualItem.STROKECOLOR, ColorLib.rgb(200,200,200));
        
        // quick repaint
        ActionList repaint = new ActionList();
        repaint.add(nodeColor);
        repaint.add(new RepaintAction());
        m_vis.putAction("repaint", repaint);
        
        // full paint
        ActionList fullPaint = new ActionList();
        fullPaint.add(nodeColor);
        m_vis.putAction("fullPaint", fullPaint);
        
        // animate paint change
        ActionList animatePaint = new ActionList(400);
        animatePaint.add(new ColorAnimator(treeNodes));
        animatePaint.add(new RepaintAction());
        m_vis.putAction("animatePaint", animatePaint);
        
        // create the tree layout action
        RadialTreeLayout treeLayout = new RadialTreeLayout(tree, 300) {
      	  // try to prevent text overlap on the top and bottom
           @Override
           protected void setPolarLocation(NodeItem n, NodeItem p, double r, double t) {
         	  // normalize angle
              while ( t > MathLib.TWO_PI )
                  t -= MathLib.TWO_PI;
              while ( t < 0 )
                  t += MathLib.TWO_PI;
              
              final double ONE_FOURTH_PI = Math.PI / 4;
              final double TOP_LEFT = MathLib.TWO_PI - (3d*ONE_FOURTH_PI);
              final double TOP_RIGHT = MathLib.TWO_PI - ONE_FOURTH_PI;
              final double BOTTOM_RIGHT = ONE_FOURTH_PI;
              final double BOTTOM_LEFT = 3d * ONE_FOURTH_PI;
              
              // adjust in top and bottom middle quadrants
              double dist = 0d;
              if( t > TOP_LEFT && t < TOP_RIGHT ) {
            	  dist = Math.abs(((3d * Math.PI) / 2d) - t);
              } else if( t > BOTTOM_RIGHT && t < BOTTOM_LEFT ) {
            	  dist = Math.abs((Math.PI/2d) - t);
              }
              if( dist != 0d ) {
                 dist = ONE_FOURTH_PI - dist;
                 final double PEAK = MathLib.TWO_PI / 9d;
                 // add extra space very close to the middle
                 if( dist > PEAK )
                    dist += ((dist - PEAK ) / (ONE_FOURTH_PI - PEAK)) / 5d;

                 dist /= 2d;
              }
//              System.out.println("n.name=" + n.getString("name") + ", r=" + r + ", t=" + t + ", dist=" + dist + ", r+dist=" + (r+dist) + ", r+(r*dist)=" + (r+(r*dist)));
              double newRadius = r + (r * dist);
              super.setPolarLocation(n, p, newRadius, t);
           }
        };
        treeLayout.setLayoutAnchor(new Point2D.Double(getWidth()/2, getHeight()/2));

        m_vis.putAction("treeLayout", treeLayout);
        
        CollapsedSubtreeLayout subLayout = 
            new CollapsedSubtreeLayout(tree);
        m_vis.putAction("subLayout", subLayout);
        
        AutoPanAction autoPan = new AutoPanAction();
        
        // create the filtering and layout
        ActionList filter = new ActionList();
        filter.add(new FisheyeTreeFilter(tree, 1));
       
        //filter.add(new GraphDistanceFilter(tree , 2));
        filter.add(new FontAction(treeNodes, FontLib.getFont("Courier New", 16)) {
           @Override
           public Font getFont(VisualItem item) {
             Font font = super.getFont(item);
             if( item.isExpanded() )
            	 return font;
             return font.deriveFont(10f);
           }
        });
        filter.add(treeLayout);
        filter.add(subLayout);
        filter.add(textColor);
        filter.add(nodeColor);
        filter.add(edgeColor);
        m_vis.putAction("filter", filter);
        
        // animated transition
        ActionList animate = new ActionList(1000);
        animate.setPacingFunction(new SlowInSlowOutPacer());
        animate.add(autoPan);
        animate.add(new QualityControlAnimator());
        animate.add(new VisibilityAnimator(tree));
        //animate.add(new SizeAnimator(treeNodes));
        animate.add(new LocationAnimator(treeNodes));
        //animate.add(new PolarLocationAnimator(treeNodes));
        //animate.add(new PolarLocationAnimator(treeEdges));
        animate.add(new ColorAnimator(treeNodes));
        animate.add(new RepaintAction());
        m_vis.putAction("animate", animate);
        m_vis.alwaysRunAfter("filter", "animate");
        
        // create animator for orientation changes
        ActionList orient = new ActionList(2000);
        orient.setPacingFunction(new SlowInSlowOutPacer());
        orient.add(autoPan);
        orient.add(new QualityControlAnimator());
        //orient.add(new SizeAnimator(treeNodes));
        orient.add(new LocationAnimator(treeNodes));
        //orient.add(new LocationAnimator(treeNodes));
        orient.add(new LocationAnimator(treeEdges));
        orient.add(new RepaintAction());
        m_vis.putAction("orient", orient);
        
        //ActionList dist = new ActionList(2000);
        //BifocalDistortion bd = new BifocalDistortion();
        //dist.add(bd);
        
       // m_vis.putAction("distort", dist);
        // ------------------------------------------------
        
        // initialize the display
        setSize(1000,1000);
        setItemSorter(new TreeDepthItemSorter());
        addControlListener(new SubtreeDragControl());
        addControlListener(new ZoomToFitControl());
        //addControlListener(new ZoomControl());
        addControlListener(new WheelZoomControl());
        //addControlListener(new RotationControl());
        addControlListener(new PanControl());
        addControlListener(new FocusControl(1, "filter"));
        
        registerKeyboardAction(
            new OrientAction(Constants.ORIENT_LEFT_RIGHT),
            "left-to-right", KeyStroke.getKeyStroke("ctrl 1"), WHEN_FOCUSED);
        registerKeyboardAction(
            new OrientAction(Constants.ORIENT_TOP_BOTTOM),
            "top-to-bottom", KeyStroke.getKeyStroke("ctrl 2"), WHEN_FOCUSED);
        registerKeyboardAction(
            new OrientAction(Constants.ORIENT_RIGHT_LEFT),
            "right-to-left", KeyStroke.getKeyStroke("ctrl 3"), WHEN_FOCUSED);
        registerKeyboardAction(
            new OrientAction(Constants.ORIENT_BOTTOM_TOP),
            "bottom-to-top", KeyStroke.getKeyStroke("ctrl 4"), WHEN_FOCUSED);
        
        // ------------------------------------------------
        
        // filter graph and perform layout
        setOrientation(m_orientation);
        m_vis.run("filter");
        
        TupleSet search = new PrefixSearchTupleSet(); 
        m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, search);
        search.addTupleSetListener(new TupleSetListener() {
            public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
                m_vis.cancel("animatePaint");
                m_vis.run("fullPaint");
                m_vis.run("animatePaint");
            }
        });
    }
    
    // ------------------------------------------------------------------------
    
    public void setOrientation(int orientation) {
//    	RadialTreeLayout rtl 
//            = (RadialTreeLayout)m_vis.getAction("treeLayout");
//      ActionList treeLayout = (ActionList)m_vis.getAction("treeLayout");
//      RadialTreeLayout rt1 = (RadialTreeLayout)treeLayout.get(0);
    	CollapsedSubtreeLayout stl
            = (CollapsedSubtreeLayout)m_vis.getAction("subLayout");
        switch ( orientation ) {
        case Constants.ORIENT_LEFT_RIGHT:
            m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
            m_edgeRenderer.setHorizontalAlignment1(Constants.RIGHT);
            m_edgeRenderer.setHorizontalAlignment2(Constants.LEFT);
            m_edgeRenderer.setVerticalAlignment1(Constants.CENTER);
            m_edgeRenderer.setVerticalAlignment2(Constants.CENTER);
            break;
        case Constants.ORIENT_RIGHT_LEFT:
            m_nodeRenderer.setHorizontalAlignment(Constants.RIGHT);
            m_edgeRenderer.setHorizontalAlignment1(Constants.LEFT);
            m_edgeRenderer.setHorizontalAlignment2(Constants.RIGHT);
            m_edgeRenderer.setVerticalAlignment1(Constants.CENTER);
            m_edgeRenderer.setVerticalAlignment2(Constants.CENTER);
            break;
        case Constants.ORIENT_TOP_BOTTOM:
            m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
            m_edgeRenderer.setHorizontalAlignment1(Constants.CENTER);
            m_edgeRenderer.setHorizontalAlignment2(Constants.CENTER);
            m_edgeRenderer.setVerticalAlignment1(Constants.BOTTOM);
            m_edgeRenderer.setVerticalAlignment2(Constants.TOP);
            break;
        case Constants.ORIENT_BOTTOM_TOP:
            m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
            m_edgeRenderer.setHorizontalAlignment1(Constants.CENTER);
            m_edgeRenderer.setHorizontalAlignment2(Constants.CENTER);
            m_edgeRenderer.setVerticalAlignment1(Constants.TOP);
            m_edgeRenderer.setVerticalAlignment2(Constants.BOTTOM);
            break;
        default:
            throw new IllegalArgumentException(
                "Unrecognized orientation value: "+orientation);
        }
        m_orientation = orientation;
        //rtl.setOrientation(orientation);
        stl.setOrientation(orientation);
    }
    
    public int getOrientation() {
        return m_orientation;
    }
    
    // ------------------------------------------------------------------------
    
    public static void main(String argv[]) {
        String infile = TREE_CHI;
        String label = "name";
        if ( argv.length > 1 ) {
            infile = argv[0];
            label = argv[1];
        }
        JComponent treeview = demo(infile, label);
        JFrame frame = new JFrame("XNAT Overview");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(treeview);
        frame.pack();
        frame.setVisible(true);
    }
    
    public static JComponent demo() {
        return demo(TREE_CHI, "name");
    }

    public static JComponent demo(String datafile, final String label) {
       Tree dataTree = null;
       try {
           dataTree = (Tree)new TreeMLReader().readGraph(datafile);
           System.out.println("node.schema=" + dataTree.getNodeTable().getSchema());
           System.out.println("edge.schema=" + dataTree.getEdgeTable().getSchema());
           System.out.println("###"+dataTree);
           System.out.println(label);
       } catch ( Exception e ) {
           e.printStackTrace();
           System.exit(1);
       }
       
       return demo(dataTree, label);
    }
    
    public static JComponent demo(Tree dataTree, final String label) {
        Color BACKGROUND = Color.WHITE;
        Color FOREGROUND = Color.BLACK;
        
        // create a new treemap
        final TreeView tview = new TreeView(dataTree, label);
        tview.setBackground(BACKGROUND);
        tview.setForeground(FOREGROUND);
        
        // create a search panel for the tree map
        JSearchPanel search = new JSearchPanel(tview.getVisualization(),
            treeNodes, Visualization.SEARCH_ITEMS, label, true, true);
        search.setShowResultCount(true);
        search.setBorder(BorderFactory.createEmptyBorder(5,5,4,0));
        search.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));
        search.setBackground(BACKGROUND);
        search.setForeground(FOREGROUND);
        
        final JFastLabel title = new JFastLabel("                 ");
        title.setPreferredSize(new Dimension(350, 20));
        title.setVerticalAlignment(SwingConstants.TOP);
        title.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
        title.setFont(FontLib.getFont("Courier New", Font.PLAIN, 20));
        title.setBackground(BACKGROUND);
        title.setForeground(Color.RED);
        
        final JFastLabel title1 = new JFastLabel("                 ");
        title1.setPreferredSize(new Dimension(10000, 20));
        title1.setVerticalAlignment(SwingConstants.TOP);
        title1.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
        title1.setFont(FontLib.getFont("Courier New", Font.PLAIN, 20));
        title1.setBackground(BACKGROUND);
        title1.setForeground(Color.YELLOW);
        
        tview.addControlListener(new ControlAdapter() {
            public void itemEntered(VisualItem item, MouseEvent e) {
				if ( item.canGetString(label) )
                    title.setText(item.getString(label));
            }
            public void itemExited(VisualItem item, MouseEvent e) {
                title.setText(null);
            }
        });
        
       /* tview.addControlListener(new ControlAdapter() {
        	public void itemClicked(VisualItem item, MouseEvent e)  {
        		pt = item.getString("name");
            	int i = 0 , flag = 0;
            	
            	for( int j = 0; j<len;j++)
            	{
            		i = i + 1;
            		if(path[j].equals(pt))
            		{
            			flag = 1;
            			break;
            		}
            	}
            	if(flag == 0)
            	{
            			path[i] = pt;
            			len = i+1;
            		
            	}
            	else{
            		len = i;
            	}
            	for(int j=0;j<len;j++)
            	{
            		System.out.println(path);
            	}
        				String s = " ";
        				for(int k=0; k<len;k++)
        				{
        					s = s + path[k] + " -->";
        				}
        					
        		    	title1.setText(s);
            }
            //public void itemExited(VisualItem item, MouseEvent e) {
              //  title1.setText(null);
            //}
        });
        */
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalStrut(10));
        box.add(title);
        box.add(Box.createHorizontalGlue());
        box.add(search);
        box.add(Box.createHorizontalStrut(3));
        box.setBackground(BACKGROUND);
        
        /*Box box1 = new Box(BoxLayout.X_AXIS);
        box1.add(Box.createHorizontalStrut(10));
        box1.add(title1);
        box1.add(Box.createHorizontalGlue());
        box1.setBackground(BACKGROUND);*/
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setForeground(FOREGROUND);
        panel.add(tview, BorderLayout.CENTER);
        panel.add(box, BorderLayout.NORTH);
        //panel.add(box1, BorderLayout.SOUTH);
        return panel;
    }
    
    // ------------------------------------------------------------------------
   
    public class OrientAction extends AbstractAction {
        private int orientation;
        
        public OrientAction(int orientation) {
            this.orientation = orientation;
        }
        public void actionPerformed(ActionEvent evt) {
            setOrientation(orientation);
            getVisualization().cancel("orient");
            getVisualization().run("treeLayout");
            getVisualization().run("orient");
        }
    }
    
    public class AutoPanAction extends Action {
        private Point2D m_start = new Point2D.Double();
        private Point2D m_end   = new Point2D.Double();
        private Point2D m_cur   = new Point2D.Double();
        private int     m_bias  = 150;
        
        public void run(double frac) {
            TupleSet ts = m_vis.getFocusGroup(Visualization.FOCUS_ITEMS);
            if ( ts.getTupleCount() == 0 ){
            	//System.out.println("1.\n");
            	return;
            }
                
            
            if ( frac == 0.0 ) {
                int xbias=0, ybias=0;
                switch ( m_orientation ) {
                case Constants.ORIENT_LEFT_RIGHT:
                    xbias = m_bias;
                    break;
                case Constants.ORIENT_RIGHT_LEFT:
                    xbias = -m_bias;
                    break;
                case Constants.ORIENT_TOP_BOTTOM:
                    ybias = m_bias;
                    break;
                case Constants.ORIENT_BOTTOM_TOP:
                    ybias = -m_bias;
                    break;
                }
                //System.out.println("2.\n");
                VisualItem vi = (VisualItem)ts.tuples().next();
                m_cur.setLocation(getWidth()/2, getHeight()/2);
                getAbsoluteCoordinate(m_cur, m_start);
                m_end.setLocation(vi.getX()+xbias, vi.getY()+ybias);
            } else {
                m_cur.setLocation(m_start.getX() + frac*(m_end.getX()-m_start.getX()),
                                  m_start.getY() + frac*(m_end.getY()-m_start.getY()));
                panToAbs(m_cur);
            }
        }
    }
    
    public static class NodeColorAction extends ColorAction {
        public NodeColorAction(String group) {
            super(group, VisualItem.FILLCOLOR);
        }
        
        public int getColor(VisualItem item) {
            if ( m_vis.isInGroup(item, Visualization.SEARCH_ITEMS) )
                {//return ColorLib.rgb(255,190,190);
            	item.setVisible(true);
            	return ColorLib.rgb(124,252,0);
            	
                }
            //else
            	//item.setVisible(false);
            else if ( m_vis.isInGroup(item, Visualization.FOCUS_ITEMS) ){
                //return ColorLib.rgb(198,229,229);
            	
            	return ColorLib.rgb(123,104,238);
            }
            else if ( item.getDOI() > -1 )
                return ColorLib.rgb(123,104,238);
            else
                return ColorLib.rgba(255,255,255,0);
        }
        
    } // end of inner class TreeMapColorAction
    
    
} // end of class TreeMap
