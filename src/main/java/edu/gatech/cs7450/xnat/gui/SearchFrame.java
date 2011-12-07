package edu.gatech.cs7450.xnat.gui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import edu.gatech.cs7450.xnat.SearchQuery;
import edu.gatech.cs7450.xnat.SearchWhere;
import edu.gatech.cs7450.xnat.settings.SearchSettings;
import edu.gatech.cs7450.xnat.settings.SettingsException;

public class SearchFrame extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger _log = Logger.getLogger(SearchFrame.class);

	private SearchSettings searchSettings = new SearchSettings();
	
	private JButton btnLoadSearch;
	private JButton btnDeleteSearch;
	
	private JPanel pnlSearchPanel;
	private SearchTreePanel searchPanel;
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
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{97, 0, 0, 96, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 1.0};
		gbl_contentPane.rowWeights = new double[]{0.0, 1.0};
		this.setLayout(gbl_contentPane);
		
		btnDeleteSearch = new JButton("Delete");
		GridBagConstraints gbc_btnDeleteSearch = new GridBagConstraints();
		gbc_btnDeleteSearch.anchor = GridBagConstraints.WEST;
		gbc_btnDeleteSearch.insets = new Insets(0, 0, 5, 5);
		gbc_btnDeleteSearch.gridx = 0;
		gbc_btnDeleteSearch.gridy = 0;
		this.add(btnDeleteSearch, gbc_btnDeleteSearch);
		
		cmbSearchName = new JComboBox();
		cmbSearchName.setEditable(true);
		GridBagConstraints gbc_cmbSearchName = new GridBagConstraints();
		gbc_cmbSearchName.gridwidth = 2;
		gbc_cmbSearchName.insets = new Insets(0, 0, 5, 5);
		gbc_cmbSearchName.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbSearchName.gridx = 1;
		gbc_cmbSearchName.gridy = 0;
		this.add(cmbSearchName, gbc_cmbSearchName);
		
		btnLoadSearch = new JButton("Load");
		GridBagConstraints gbc_btnLoadSearch = new GridBagConstraints();
		gbc_btnLoadSearch.insets = new Insets(0, 0, 5, 5);
		gbc_btnLoadSearch.gridx = 3;
		gbc_btnLoadSearch.gridy = 0;
		this.add(btnLoadSearch, gbc_btnLoadSearch);
		
		btnSaveSearch = new JButton("Save");
		GridBagConstraints gbc_btnSaveSearch = new GridBagConstraints();
		gbc_btnSaveSearch.anchor = GridBagConstraints.WEST;
		gbc_btnSaveSearch.insets = new Insets(0, 0, 5, 0);
		gbc_btnSaveSearch.gridx = 4;
		gbc_btnSaveSearch.gridy = 0;
		this.add(btnSaveSearch, gbc_btnSaveSearch);
		
		searchPanel = new SearchTreePanel();
		pnlSearchPanel = searchPanel;
		GridBagConstraints gbc_pnlSearchPanel = new GridBagConstraints();
		gbc_pnlSearchPanel.gridwidth = 5;
		gbc_pnlSearchPanel.fill = GridBagConstraints.BOTH;
		gbc_pnlSearchPanel.gridx = 0;
		gbc_pnlSearchPanel.gridy = 1;
		this.add(pnlSearchPanel, gbc_pnlSearchPanel);
		
		_init();
	}
	
	/**
	 * Returns the current search query.
	 * <p>
	 * The query name and search criteria are the currently entered values and 
	 * will not necessarily match a stored query.
	 * </p>
	 * 
	 * @return the query
	 */
	public SearchQuery toSearchQuery() {
		String name = cmbSearchName.getSelectedItem().toString().trim();
		return new SearchQuery(name, searchPanel.toSearchWhere());
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
		SearchQuery query = this.toSearchQuery();
		String name = query.getName();
		if( name == null || "".equals(name) ) {
			JOptionPane.showMessageDialog(this, "The search name is empty.", "Empty Name", JOptionPane.ERROR_MESSAGE);
		} else {
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

