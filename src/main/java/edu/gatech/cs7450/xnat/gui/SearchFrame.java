package edu.gatech.cs7450.xnat.gui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import prefuse.data.Graph;
import prefuse.data.Table;
import edu.gatech.cs7450.Util;
import edu.gatech.cs7450.prefuse.ScanGroupView;
import edu.gatech.cs7450.prefuse.ScanGroupViewLoader;
import edu.gatech.cs7450.prefuse.XNATScatterPlot;
import edu.gatech.cs7450.prefuse.XNATScatterPlotTableReader;
import edu.gatech.cs7450.xnat.SearchElement;
import edu.gatech.cs7450.xnat.SearchField;
import edu.gatech.cs7450.xnat.SearchQuery;
import edu.gatech.cs7450.xnat.SearchWhere;
import edu.gatech.cs7450.xnat.XNATConnection;
import edu.gatech.cs7450.xnat.XNATDefaults;
import edu.gatech.cs7450.xnat.XNATMetaData;
import edu.gatech.cs7450.xnat.XNATResultSet;
import edu.gatech.cs7450.xnat.XNATSearch;
import edu.gatech.cs7450.xnat.settings.SearchSettings;
import edu.gatech.cs7450.xnat.settings.SettingsException;

public class SearchFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final Logger _log = Logger.getLogger(TestSearchFrame.class);
	
	/** FIXME temporary hard-coded connection */
	private static XNATConnection conn = new XNATConnection("http://node18.cci.emory.edu:8080/xnat/REST", "nbia", "nbia");
	private static XNATMetaData metaData = new XNATMetaData(conn);

	private XNATConnection xnatConn;
	private SearchSettings searchSettings = new SearchSettings();
	
	private JButton btnLoadSearch;
	private JButton btnDeleteSearch;
	
	private JPanel contentPane;
	private JPanel pnlSearchPanel;
	private SearchPanel searchPanel;
	private JComboBox cmbSearchName;
	private JButton btnSaveSearch;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SearchFrame frame = new SearchFrame();
					frame.setVisible(true);
				} catch( Exception e ) {
					e.printStackTrace();
				}
			}
		});
	}
	

	/**
	 * Create the frame.
	 */
	public SearchFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 718, 391);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{97, 0, 0, 96, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 1.0, 0.0};
		gbl_contentPane.rowWeights = new double[]{0.0, 1.0};
		contentPane.setLayout(gbl_contentPane);
		
		btnDeleteSearch = new JButton("Delete");
		GridBagConstraints gbc_btnDeleteSearch = new GridBagConstraints();
		gbc_btnDeleteSearch.anchor = GridBagConstraints.WEST;
		gbc_btnDeleteSearch.insets = new Insets(0, 0, 5, 5);
		gbc_btnDeleteSearch.gridx = 0;
		gbc_btnDeleteSearch.gridy = 0;
		contentPane.add(btnDeleteSearch, gbc_btnDeleteSearch);
		
		cmbSearchName = new JComboBox();
		cmbSearchName.setEditable(true);
		GridBagConstraints gbc_cmbSearchName = new GridBagConstraints();
		gbc_cmbSearchName.gridwidth = 2;
		gbc_cmbSearchName.insets = new Insets(0, 0, 5, 5);
		gbc_cmbSearchName.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbSearchName.gridx = 1;
		gbc_cmbSearchName.gridy = 0;
		contentPane.add(cmbSearchName, gbc_cmbSearchName);
		
		btnLoadSearch = new JButton("Load");
		GridBagConstraints gbc_btnLoadSearch = new GridBagConstraints();
		gbc_btnLoadSearch.insets = new Insets(0, 0, 5, 5);
		gbc_btnLoadSearch.gridx = 3;
		gbc_btnLoadSearch.gridy = 0;
		contentPane.add(btnLoadSearch, gbc_btnLoadSearch);
		
		btnSaveSearch = new JButton("Save");
		GridBagConstraints gbc_btnSaveSearch = new GridBagConstraints();
		gbc_btnSaveSearch.anchor = GridBagConstraints.WEST;
		gbc_btnSaveSearch.insets = new Insets(0, 0, 5, 5);
		gbc_btnSaveSearch.gridx = 4;
		gbc_btnSaveSearch.gridy = 0;
		contentPane.add(btnSaveSearch, gbc_btnSaveSearch);
		
		searchPanel = new SearchPanel();
		pnlSearchPanel = searchPanel;
		GridBagConstraints gbc_pnlSearchPanel = new GridBagConstraints();
		gbc_pnlSearchPanel.gridwidth = 5;
		gbc_pnlSearchPanel.insets = new Insets(0, 0, 0, 5);
		gbc_pnlSearchPanel.fill = GridBagConstraints.BOTH;
		gbc_pnlSearchPanel.gridx = 0;
		gbc_pnlSearchPanel.gridy = 1;
		contentPane.add(pnlSearchPanel, gbc_pnlSearchPanel);
		
		_init();
	}
	
	private void _init() {
		_attachListeners();
		_initSavedQueries();
	}
	
	private void _initSavedQueries() {
		try {
			List<String> queryNames = searchSettings.getQueryNames();
			for( String name : queryNames )
				cmbSearchName.addItem(name);
		} catch( SettingsException e ) {
			_log.error("Could not initialize query list.", e);
		}
	}
	
	private void _attachListeners() {
		ActionListener l = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				if( source == btnSaveSearch )
					_saveSearchClicked(e);
				else if( source == btnDeleteSearch )
					_deleteSearchClicked(e);
				else if( source == btnLoadSearch )
					_loadSearchClicked(e);
				else
					_log.warn("Unexpected source: " + source);
			}
		};
		btnSaveSearch.addActionListener(l);
		btnLoadSearch.addActionListener(l);
		btnDeleteSearch.addActionListener(l);
	}
	
	private void _loadSearchClicked(ActionEvent e) {
		String name = (String)cmbSearchName.getSelectedItem();
		try {
			SearchQuery query = searchSettings.loadSearch(name);
			searchPanel.fromSearchWhere(query.getSearchWhere());
		} catch( SettingsException ex ) {
			final String MSG = "Could not load query: " + name;
			_log.error(MSG, ex);
			JOptionPane.showMessageDialog(SearchFrame.this, MSG, "Load Failed", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void _saveSearchClicked(ActionEvent e) {
		String name = (String)cmbSearchName.getSelectedItem();
		if( name == null || "".equals(name = name.trim()) ) {
			JOptionPane.showMessageDialog(this, "The search name is empty.", "Empty Name", JOptionPane.ERROR_MESSAGE);
		} else {
			SearchQuery query = SearchQuery.getDefault(name, searchPanel.toSearchWhere());
			try {
				searchSettings.saveSearch(query);
				
				// add if it didn't exist already
				for( int i = 0, ilen = cmbSearchName.getItemCount(); i < ilen; ++i )
					if( name.equals(cmbSearchName.getItemAt(i)) )
						return;
				cmbSearchName.addItem(name);
			} catch( SettingsException ex ) {
				String msg = "Could not save query: " + name;
				_log.error(msg, ex);
				JOptionPane.showMessageDialog(this, msg, "Save Failed", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void _deleteSearchClicked(ActionEvent e) {
		String name = (String)cmbSearchName.getSelectedItem();
		int result = JOptionPane.showConfirmDialog(this, 
			"Really delete search: " + name, "Delete?", JOptionPane.YES_NO_OPTION);
		if( result != JOptionPane.YES_OPTION )
			return;
		
		try {
			// delete, remove from list, clear the search tree
			searchSettings.deleteSearch(name);
			cmbSearchName.removeItem(name);
			searchPanel.fromSearchWhere(new SearchWhere());
		} catch( SettingsException ex ) {
			String msg = "Failed to delete search: " + name;
			_log.error(msg, ex);
			JOptionPane.showMessageDialog(this, msg, "Delete Failed", JOptionPane.ERROR_MESSAGE);
		}
	}
}

