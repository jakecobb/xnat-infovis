package edu.gatech.cs7450.xnat.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import edu.gatech.cs7450.prefuse.ScanGroupView;
import edu.gatech.cs7450.prefuse.ScanGroupViewLoader;
import edu.gatech.cs7450.xnat.SearchQuery;
import edu.gatech.cs7450.xnat.XNATConnection;
import edu.gatech.cs7450.xnat.XNATException;
import edu.gatech.cs7450.xnat.XNATResultSet;
import edu.gatech.cs7450.xnat.XNATSearch;

/**
 * A panel with a scan group view, search panel and the ability to add scan groups 
 * from a search.
 */
public class ScanGroupPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger _log = Logger.getLogger(ScanGroupPanel.class);
	
	private XNATConnection connection;
	private String project;
	private SearchFrame searchPanel;
	private ScanGroupView scanGroupView;
	private JSplitPane splitPane;
	
	public ScanGroupPanel(XNATConnection conn, String project) {
		this();
		connection = conn;
		this.project = project;
		_initScanGroupView();
	}

	/**
	 * Create the panel.
	 */
	public ScanGroupPanel() {
		setLayout(new GridLayout(0, 1, 0, 0));
		
		splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		add(splitPane);
		
		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{448, 0};
		gbl_panel.rowHeights = new int[]{35, 131, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton btnAddScanGroup = new JButton("Add Scan Group");
		btnAddScanGroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_addScanGroup();
			}
		});
		GridBagConstraints gbc_btnAddScanGroup = new GridBagConstraints();
		gbc_btnAddScanGroup.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddScanGroup.anchor = GridBagConstraints.SOUTH;
		gbc_btnAddScanGroup.insets = new Insets(0, 0, 5, 0);
		gbc_btnAddScanGroup.gridx = 0;
		gbc_btnAddScanGroup.gridy = 0;
		panel.add(btnAddScanGroup, gbc_btnAddScanGroup);
		
		searchPanel = new SearchFrame();
		JPanel panel_1 = searchPanel;
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		panel.add(panel_1, gbc_panel_1);
	}
	
	public ScanGroupView getScanGroupView() {
		return scanGroupView;
	}
	
	private void _initScanGroupView() {
		
		Graph sgGraph = ScanGroupViewLoader.loadSubjects(connection, project);
		
		scanGroupView = new ScanGroupView(sgGraph);
		splitPane.setRightComponent(scanGroupView);
		scanGroupView.begin();
		
		this.setFocusTraversalPolicy(new FocusTraversalPolicy() {
			@Override
			public Component getLastComponent(Container aContainer) {
				return splitPane.getLeftComponent();
			}
			
			@Override
			public Component getFirstComponent(Container aContainer) {
				return splitPane.getRightComponent();
			}
			
			@Override
			public Component getDefaultComponent(Container aContainer) {
				return splitPane.getRightComponent();
			}
			
			@Override
			public Component getComponentBefore(Container aContainer, Component aComponent) {
				if( aComponent == splitPane.getLeftComponent() )
					return splitPane.getRightComponent();
				return splitPane.getLeftComponent();
			}
			
			@Override
			public Component getComponentAfter(Container aContainer, Component aComponent) {
				if( aComponent == splitPane.getLeftComponent() )
					return splitPane.getRightComponent();
				return splitPane.getLeftComponent();
			}
		});
	}
	
	private void _addScanGroup() {
		SearchQuery query = searchPanel.toSearchQuery();
		XNATSearch search = new XNATSearch(connection);
		
		try {
			XNATResultSet result = search.runSearch(query);
			ScanGroupViewLoader.addResultAsScanGroup(scanGroupView, query.getName(), result);
		} catch( XNATException e ) {
			final String MSG = "Scan group query failed.";
			_log.error(MSG, e);
			JOptionPane.showMessageDialog(this, MSG, "Query Failed", JOptionPane.ERROR_MESSAGE);
		} catch( IllegalStateException e ) {
			JOptionPane.showMessageDialog(this, "The maximum number of scan groups has already been reached.", 
				"Add Failed", JOptionPane.ERROR_MESSAGE);
		}
	}
}
