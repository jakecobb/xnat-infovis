package edu.gatech.cs7450.xnat.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import prefuse.util.ui.UILib;
import edu.gatech.cs7450.xnat.SearchCriteria;
import edu.gatech.cs7450.xnat.SearchWhere;
import edu.gatech.cs7450.xnat.SearchWhere.SearchMethod;
import edu.gatech.cs7450.xnat.SingleCriteria;

public class SearchPanel extends JPanel {
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
	

	private static class SearchTreeCellEditor implements TreeCellEditor {
		private Vector<CellEditorListener> _listeners = new Vector<CellEditorListener>(1);
		private SingleCriteriaPanel _pnlSingleCriteria;
		private SearchTreeNode _node;	
		private TreePath _lastPath;
		
		private DefaultCellEditor _defaultEditor;
		
		public SearchTreeCellEditor() {
		}
		
		protected void checkSearchMethod() { 
			if( _defaultEditor == null ) {
				JComboBox searchMethod = new JComboBox();
				searchMethod = new JComboBox();
				searchMethod.setModel(new DefaultComboBoxModel(SearchMethod.values()));
				searchMethod.setMinimumSize(new Dimension(32, 12));
				searchMethod.setMaximumSize(new Dimension(32, 18));
				searchMethod.setFont(new Font("Dialog", Font.BOLD, 10));
				
				_defaultEditor = new DefaultCellEditor(searchMethod);
			}
		}
		
		@Override
		public boolean stopCellEditing() {
			_node = null;
			ChangeEvent event = new ChangeEvent(this);
			for( CellEditorListener l : new ArrayList<CellEditorListener>(_listeners) ) // iterate on copy
				l.editingStopped(event);
			return true;
		}
		
		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			System.out.println("shouldSelectCell: " + anEvent);
			return true;
		}
		
		@Override
		public void removeCellEditorListener(CellEditorListener l) {
			_listeners.remove(l);
		}
		
		@Override
		public boolean isCellEditable(EventObject anEvent) {
			System.out.println("isCellEditable: " + anEvent);
			if( !(anEvent instanceof MouseEvent ) )
				return false;
			MouseEvent me = (MouseEvent)anEvent;
			if( me.getButton() != MouseEvent.BUTTON1 )
				return false;
			
			JTree tree = (JTree)anEvent.getSource();
			TreePath path = tree.getPathForLocation(me.getX(), me.getY());
			switch( me.getClickCount() ) {
			case 1:
				_lastPath = path;
			default:
				return false;
			case 2:
				return path != null && path.equals(_lastPath);
			// FIXME wait a little to see if there is a third click?
			}
		}
		
		@Override
		public Object getCellEditorValue() {
			if( _node.isSingleCriteria() ) {
				_pnlSingleCriteria.toSingleCriteria(_node.asSingleCriteria());
			} else {
				_node.asSearchWhere().setMethod((SearchMethod)_defaultEditor.getCellEditorValue());
			}
			return _node;
		}
		
		@Override
		public void cancelCellEditing() {
			_node = null; _lastPath = null;
			ChangeEvent event = new ChangeEvent(this);
			for( CellEditorListener l : new ArrayList<CellEditorListener>(_listeners) ) // iterate on copy
				l.editingCanceled(event);
		}
		
		@Override
		public void addCellEditorListener(CellEditorListener l) {
			_listeners.add(l);
		}
		
		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
			SearchTreeNode node = (SearchTreeNode)value;
			_node = node;
			if( node.isSingleCriteria() ) {
				if( _pnlSingleCriteria == null )
					_pnlSingleCriteria = new SingleCriteriaPanel();
				_pnlSingleCriteria.fromSingleCriteria(node.asSingleCriteria());
				return _pnlSingleCriteria;
			} else {
				checkSearchMethod();
				SearchMethod method = node.asSearchWhere().getMethod();
				return _defaultEditor.getTreeCellEditorComponent(tree, method, isSelected, expanded, leaf, row);
			}
		}
	}
	
	private void setupTree() {
		treeModel = new DefaultTreeModel(new SearchTreeNode(rootSearchGroup), true);
		treeCriteria.setModel(treeModel);
		
		treeCriteria.setCellEditor(new SearchTreeCellEditor());
	}

	private static class SearchTreeNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = 1L;

		public SearchTreeNode() { }
		public SearchTreeNode(Object userObject, boolean allowsChildren) {
			super(userObject, allowsChildren);
		}
		public SearchTreeNode(Object userObject) {
			super(userObject);
		}
		
		public boolean isSearchWhere() { return userObject instanceof SearchWhere; }
		public boolean isSingleCriteria() { return userObject instanceof SingleCriteria; }
		public SearchWhere asSearchWhere() { return (SearchWhere)userObject; }
		public SingleCriteria asSingleCriteria() { return (SingleCriteria)userObject; }
		public SearchCriteria asSearchCriteria() { return (SearchCriteria)userObject; }
		
		@Override
		public String toString() {
			if( userObject instanceof SearchWhere )
				return "Group (" + ((SearchWhere)userObject).getMethod() + ")";
			if( userObject instanceof SingleCriteria ) {
				SingleCriteria criteria = (SingleCriteria)userObject;
				return "Criteria: " + criteria.getSchemeField() + " " + criteria.getOperator() + " " + criteria.getValue();
			}
			return super.toString();
		}
	}
	
	public static void main(String[] args) {
		
		UILib.setPlatformLookAndFeel();
		
		final SearchPanel view = new SearchPanel();
		JFrame frame = new JFrame("blah");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(view);
		
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				System.out.println("SearchWhere: ");
				System.out.println(view.rootSearchGroup);
				SearchWhere root = view.rootSearchGroup;
				System.out.println(root);
			}
		});

		frame.pack();
		frame.setVisible(true);
	}
}
