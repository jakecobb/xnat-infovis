package edu.gatech.cs7450.xnat.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import prefuse.util.ui.UILib;
import edu.gatech.cs7450.xnat.SearchCriteria;
import edu.gatech.cs7450.xnat.SearchWhere;
import edu.gatech.cs7450.xnat.SingleCriteria;

public class SearchPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JTree treeCriteria;
	private DefaultTreeModel treeModel;
	
	private SearchWhere rootSearchGroup = new SearchWhere();

	/**
	 * Create the panel.
	 */
	public SearchPanel() {
		setPreferredSize(new Dimension(400, 300));
		setLayout(new GridLayout(0, 1, 0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane);
		
		treeCriteria = new JTree();
		treeCriteria.setEditable(true);
		treeCriteria.setPreferredSize(new Dimension(150, 72));
		treeCriteria.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if( e.getButton() != MouseEvent.BUTTON3 )
					return;
				
				// get the clicked item
				int x = e.getX(), y = e.getY();
				JTree tree = (JTree)e.getSource();
				TreePath path = tree.getPathForLocation(x, y);
				if( path == null ) 
					return;
				
				// select it, then get the selected object
				tree.setSelectionPath(path);
				
				final SearchTreeNode node = (SearchTreeNode)path.getLastPathComponent();
				if( node.isSearchWhere() ) {					
					JPopupMenu menu = new JPopupMenu();
					JMenuItem itmAddCriteria = menu.add("Add Criteria");
					itmAddCriteria.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							// add to search criteria
							SingleCriteria newCriteria = new SingleCriteria();
							node.asSearchWhere().addCriteria(newCriteria);
							
							// and as node in the tree
							SearchTreeNode newNode = new SearchTreeNode(newCriteria, false);
							treeModel.insertNodeInto(newNode, node, node.getChildCount());
							
							// show and start editing
							TreePath newPath = new TreePath(newNode.getPath());
							treeCriteria.scrollPathToVisible(newPath);
							treeCriteria.startEditingAtPath(newPath);
						}
					});
					
					JMenuItem itmAddGroup = menu.add("Add Group");
					itmAddGroup.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							// add to search criteria
							SearchWhere newWhere = new SearchWhere();
							node.asSearchWhere().addCriteria(newWhere);
							
							// and as node in the tree
							SearchTreeNode newNode = new SearchTreeNode(newWhere, true);
							treeModel.insertNodeInto(newNode, node, node.getChildCount());
							
							// show and start editing
							TreePath newPath = new TreePath(newNode.getPath());
							treeCriteria.scrollPathToVisible(newPath);
							treeCriteria.startEditingAtPath(newPath);
						}
					});
					
					JMenuItem itmDelete = menu.add("Delete Group");
					itmDelete.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							// remove from parent and node from the tree
							SearchWhere parentGroup = ((SearchTreeNode)node.getParent()).asSearchWhere();
							parentGroup.removeCriteria(node.asSearchCriteria());
							treeModel.removeNodeFromParent(node);
						}
					});
					
					// can't delete the root group
					if( node.isRoot() )
						itmDelete.setEnabled(false);
					
					menu.show(tree, x, y);
					
				} else if ( node.isSingleCriteria() ) {
					JPopupMenu menu = new JPopupMenu();
					
					JMenuItem itmDelete = menu.add("Delete Criteria");
					itmDelete.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							// remove from parent
							SingleCriteria criteria = node.asSingleCriteria();
							SearchWhere parentGroup = ((SearchTreeNode)node.getParent()).asSearchWhere();
							parentGroup.removeCriteria(criteria);
							
							// and node from tree
							treeModel.removeNodeFromParent(node);
						}
					});
					
					menu.show(tree, x, y);
				}
				
			}
		});
		scrollPane.setViewportView(treeCriteria);

		setupTree();
	}
	
	/**
	 * Reinitializes the panel with the given search contents.  
	 * The <code>where</code> object is copied and not modified.
	 * 
	 * @param where the search contents
	 * @throws NullPointerException if <code>where</code> is <code>null</code>
	 */
	public void fromSearchWhere(SearchWhere where) {
		System.out.println("SearchPanel.fromSearchWhere(): where = " + where);
		if( where == null ) throw new NullPointerException("where is null");
		rootSearchGroup = new SearchWhere(where);
		_changeTreeRoot();
	}
	
	private void _changeTreeRoot() {
		assert ((SearchTreeNode)treeModel.getRoot()).asSearchWhere() != rootSearchGroup : "Group already established?";

		SearchTreeNode newRoot = new SearchTreeNode(rootSearchGroup);
		treeModel.setRoot(newRoot);
		
		LinkedList<SearchTreeNode> groups = new LinkedList<SearchTreeNode>();
		groups.add(newRoot);
		
		while( !groups.isEmpty() ) {
			SearchTreeNode parent = groups.removeFirst();
			int i = 0;
			for( SearchCriteria c : parent.asSearchWhere().getCriteria() ) {
				SearchTreeNode newNode = new SearchTreeNode(c);
				treeModel.insertNodeInto(newNode, parent, i++);
				
				// add its children next, in the order they appear in the parent
				if( c.isWhere() )
					groups.add(newNode);
			}
		}
		
		
	}
	
	/**
	 * Returns a copy of the current search contents, safe to modify.
	 * @return the current search contents
	 */
	public SearchWhere toSearchWhere() {
		return new SearchWhere(rootSearchGroup);
	}
	
	private static class SearchTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {
		private static final long serialVersionUID = 1L;
		
		private KeyHandler keyHandler = new KeyHandler();
		private SearchTreeNode searchNode;
		private SearchCriteria editingCriteria;
		private SingleCriteriaPanel pnlSingleCriteria;
		private SearchMethodPanel pnlSearchMethod;
				
		public SearchTreeCellEditor() {
			pnlSingleCriteria = makeSingleCriteriaPanel();
			pnlSearchMethod = makeSearchMethodPanel();
		}
		
		private SingleCriteriaPanel makeSingleCriteriaPanel() {
			SingleCriteriaPanel panel = new SingleCriteriaPanel();
			panel.setOpaque(false);
			panel.getOperator().addKeyListener(keyHandler);
			panel.getSchemeField().addActionListener(keyHandler);
			panel.getFieldValue().addActionListener(keyHandler);
			return panel;
		}
		
		private SearchMethodPanel makeSearchMethodPanel() {
			SearchMethodPanel searchMethod = new SearchMethodPanel();
			searchMethod.setOpaque(false);
			searchMethod.putClientProperty("JPanel.isTreeCellEditor", Boolean.TRUE);
			JComboBox combo = searchMethod.getSearchMethodCombo();
			combo.putClientProperty("JComboBox.isTreeCellEditor", Boolean.TRUE);
			combo.addKeyListener(keyHandler);
			JComponent comboEditor = (JComponent)combo.getEditor().getEditorComponent();
			comboEditor.setBackground(null);
			comboEditor.setBorder(null);
			return searchMethod;
		}
		
		@Override
		public boolean stopCellEditing() {
			if( editingCriteria.isWhere() ) {
//				((SearchWhere)editingCriteria).setMethod((SearchMethod)cmbSearchMethod.getSelectedItem());
				((SearchWhere)editingCriteria).setMethod(pnlSearchMethod.getSearchMethod());
			} else {
				pnlSingleCriteria.toSingleCriteria((SingleCriteria)editingCriteria);
			}
			return super.stopCellEditing();
		}		
		
		@Override
		public Object getCellEditorValue() {
			return editingCriteria;
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
			if( !(value instanceof SearchTreeNode) )
				throw new RuntimeException("Expecting search tree node, not: " + value.getClass());
			
			searchNode = (SearchTreeNode)value;
			editingCriteria = searchNode.asSearchCriteria();
			adaptToRendererStyle(tree, value, isSelected, expanded, leaf, row);
			if( searchNode.isSearchWhere() ) {
				pnlSearchMethod.setSearchMethod(searchNode.asSearchWhere().getMethod());
				return pnlSearchMethod;
			} else {
				pnlSingleCriteria.fromSingleCriteria(searchNode.asSingleCriteria());
				return pnlSingleCriteria;
			}
		}
		
		private void adaptToRendererStyle(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
			if( tree == null ) return;
			TreeCellRenderer rend = tree.getCellRenderer();
			if( rend == null ) return;
			
			Component comp = rend.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, true);
			if( comp == null ) {
				System.err.println("null component");
				return;
			}
			
			JComponent editComponent = searchNode.isSearchWhere() ? pnlSearchMethod : pnlSingleCriteria;
			editComponent.setBorder(null);
			
			adaptChildrenToRenderer(editComponent, comp);
		}
		
		private void adaptChildrenToRenderer(Component parent, Component rendComp) {
			Color foreground = rendComp.getForeground();
			Font font = rendComp.getFont();
			Dimension rendMin = rendComp.getMinimumSize(),
			          rendMax = rendComp.getMaximumSize(),
			         rendPref = rendComp.getPreferredSize();
			
			
			List<Component> components = new ArrayList<Component>();
			components.add(parent);
			if( parent instanceof Container )
				components.addAll(Arrays.asList(((Container)parent).getComponents()));
			
			for( Component child : components ) {
				Dimension childMin = child.getMinimumSize(),
				          childMax = child.getMaximumSize(),
				         childPref = child.getPreferredSize();
				
				childMin.height = rendMin.height;
				childMax.height = rendMax.height;
				
				child.setMinimumSize(childMin);
				child.setMaximumSize(childMax);
				child.setPreferredSize(childPref);
				
				// only set font and text color on labels
				if( child instanceof JLabel ) {
					child.setForeground(foreground);
					child.setFont(font);
				}
				if( child instanceof JComponent ) {
					JComponent jchild = (JComponent)child;
					Border b = jchild.getBorder();
					if( b != null ) {						
						Insets in = b.getBorderInsets(jchild);
						childMax.height -= in.top + in.bottom; 
						child.setMaximumSize(childMax);

						if( !(jchild instanceof JTextField) )
							jchild.setBorder(null);
					}
				}
			}
		}
		
		/** Helper to accept on <code>&lt;ENTER&gt;</code> and cancel on <code>&lt;ESC&gt;</code>. */
		private class KeyHandler extends KeyAdapter implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopCellEditing();
			}
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if( keyCode == KeyEvent.VK_ENTER )
					stopCellEditing();
				else if( keyCode == KeyEvent.VK_ESCAPE )
					cancelCellEditing();
			}
		}
	}
	


	
	private void setupTree() {
		treeModel = new DefaultTreeModel(new SearchTreeNode(rootSearchGroup), true);
		treeCriteria.setModel(treeModel);
		
		treeCriteria.setCellEditor(new DefaultTreeCellEditor(treeCriteria, 
			(DefaultTreeCellRenderer)treeCriteria.getCellRenderer(), 
			new SearchTreeCellEditor()));
	}

	private static class SearchTreeNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = 1L;

		public SearchTreeNode(SearchCriteria userObject, boolean allowsChildren) {
			super(userObject, allowsChildren);
		}
		public SearchTreeNode(SearchCriteria userObject) {
			// children only for SearchWhere
			super(userObject, userObject instanceof SearchWhere);
		}
		
		public boolean isSearchWhere() { return userObject instanceof SearchWhere; }
		public boolean isSingleCriteria() { return userObject instanceof SingleCriteria; }
		public SearchWhere asSearchWhere() { return (SearchWhere)userObject; }
		public SingleCriteria asSingleCriteria() { return (SingleCriteria)userObject; }
		public SearchCriteria asSearchCriteria() { return (SearchCriteria)userObject; }
		
		@Override
		public void setUserObject(Object userObject) {
			if( !(userObject instanceof SearchCriteria) )
				throw new RuntimeException("Tried to set type: " + userObject.getClass());
			super.setUserObject(userObject);
		}
		
		@Override
		public String toString() {
			if( userObject instanceof SearchWhere )
				return "Group (" + ((SearchWhere)userObject).getMethod() + ")";
			if( userObject instanceof SingleCriteria ) {
				SingleCriteria criteria = (SingleCriteria)userObject;
				return "\"" + criteria.getSchemeField() + "\" " + criteria.getOperator() + " \"" 
					+ criteria.getValue() + "\"";
			}
			throw new RuntimeException("Unexpected type: " + userObject.getClass());
		}
	}
	
	public static void main(String[] args) {
		
		UILib.setPlatformLookAndFeel();
//		try {
//			NimbusLookAndFeel nimbus = new NimbusLookAndFeel();
//			UIManager.setLookAndFeel(nimbus);
//		} catch( Exception e ) {
//			e.printStackTrace();
//		}
		
		final SearchPanel view = new SearchPanel();
		JFrame frame = new JFrame("blah");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(view);
		
//		frame.addComponentListener(new ComponentAdapter() {
//			@Override
//			public void componentMoved(ComponentEvent e) {
//				System.out.println("SearchWhere: ");
//				System.out.println(view.rootSearchGroup);
//				SearchWhere root = view.rootSearchGroup;
//				System.out.println(root);
//			}
//		});

		frame.pack();
		frame.setVisible(true);
	}
}
