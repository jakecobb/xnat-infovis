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

import edu.gatech.cs7450.Util;
import edu.gatech.cs7450.xnat.SearchQuery;
import edu.gatech.cs7450.xnat.SearchWhere;
import edu.gatech.cs7450.xnat.XNATConnection;
import edu.gatech.cs7450.xnat.XNATSearch;

public class SearchFrame extends JFrame {
	
	private Preferences preferences = Preferences.userNodeForPackage(SearchFrame.class);

	
	private LinkedHashMap<String, SearchQuery> queries = new LinkedHashMap<String, SearchQuery>();
	
	private JButton btnLoadSearch;
	private JButton btnDeleteSearch;
	
	private JPanel contentPane;
	private JPanel pnlSearchPanel;
	private SearchPanel searchPanel;
	private JButton btnDoSearch;
	private JTextPane txtSearchResults;
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
		gbl_contentPane.columnWidths = new int[]{146, 0, 0, 96, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 56, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 1.0, 0.0};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0};
		contentPane.setLayout(gbl_contentPane);
		
		cmbSearchName = new JComboBox();
		cmbSearchName.setEditable(true);
		GridBagConstraints gbc_cmbSearchName = new GridBagConstraints();
		gbc_cmbSearchName.gridwidth = 2;
		gbc_cmbSearchName.insets = new Insets(0, 0, 5, 5);
		gbc_cmbSearchName.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbSearchName.gridx = 0;
		gbc_cmbSearchName.gridy = 0;
		contentPane.add(cmbSearchName, gbc_cmbSearchName);
		
		btnLoadSearch = new JButton("Load");
		GridBagConstraints gbc_btnLoadSearch = new GridBagConstraints();
		gbc_btnLoadSearch.insets = new Insets(0, 0, 5, 5);
		gbc_btnLoadSearch.gridx = 2;
		gbc_btnLoadSearch.gridy = 0;
		contentPane.add(btnLoadSearch, gbc_btnLoadSearch);
		
		btnSaveSearch = new JButton("Save");
		GridBagConstraints gbc_btnSaveSearch = new GridBagConstraints();
		gbc_btnSaveSearch.insets = new Insets(0, 0, 5, 5);
		gbc_btnSaveSearch.gridx = 3;
		gbc_btnSaveSearch.gridy = 0;
		contentPane.add(btnSaveSearch, gbc_btnSaveSearch);
		
		btnDeleteSearch = new JButton("Delete");
		GridBagConstraints gbc_btnDeleteSearch = new GridBagConstraints();
		gbc_btnDeleteSearch.insets = new Insets(0, 0, 5, 5);
		gbc_btnDeleteSearch.gridx = 4;
		gbc_btnDeleteSearch.gridy = 0;
		contentPane.add(btnDeleteSearch, gbc_btnDeleteSearch);
		
		btnDoSearch = new JButton("Search");
		GridBagConstraints gbc_btnDoSearch = new GridBagConstraints();
		gbc_btnDoSearch.insets = new Insets(0, 0, 5, 5);
		gbc_btnDoSearch.gridx = 4;
		gbc_btnDoSearch.gridy = 1;
		contentPane.add(btnDoSearch, gbc_btnDoSearch);
		
		txtSearchResults = new JTextPane();
		txtSearchResults.setContentType("text/xml");
		GridBagConstraints gbc_txtSearchResults = new GridBagConstraints();
		gbc_txtSearchResults.gridwidth = 2;
		gbc_txtSearchResults.gridheight = 2;
		gbc_txtSearchResults.fill = GridBagConstraints.BOTH;
		gbc_txtSearchResults.gridx = 4;
		gbc_txtSearchResults.gridy = 2;
		contentPane.add(txtSearchResults, gbc_txtSearchResults);
		
		searchPanel = new SearchPanel();
		pnlSearchPanel = searchPanel;
		GridBagConstraints gbc_pnlSearchPanel = new GridBagConstraints();
		gbc_pnlSearchPanel.gridwidth = 4;
		gbc_pnlSearchPanel.insets = new Insets(0, 0, 0, 5);
		gbc_pnlSearchPanel.fill = GridBagConstraints.BOTH;
		gbc_pnlSearchPanel.gridx = 0;
		gbc_pnlSearchPanel.gridy = 3;
		contentPane.add(pnlSearchPanel, gbc_pnlSearchPanel);
		
		_attachListeners();
		_initSavedQueries();
	}
	private void _initSavedQueries() {
		Preferences prefs = preferences.node("queries");
		byte[] data = prefs.getByteArray("saved_queries", null);
		if( data == null )
			return; // no saved queries
		
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new ByteArrayInputStream(data));
			List<SearchQuery> searches = (List<SearchQuery>)in.readObject();
			if( searches == null ) return;
			for( SearchQuery search : searches ) {
				this.queries.put(search.getName(), search);
				cmbSearchName.addItem(search.getName());
			}
		} catch( ClassCastException e ) {
			// FIXME handle this
			e.printStackTrace();
		} catch( IOException e ) {
			// FIXME handle this
			e.printStackTrace();
		} catch( ClassNotFoundException e ) {
			// FIXME handle this
			e.printStackTrace();
		} finally {
			Util.tryClose(in);
		}		
	}
	
	private void _saveQueries() {
		Preferences prefs = preferences.node("queries");
		
		byte[] bytes = null;
		ObjectOutputStream out = null;
		try {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			out = new ObjectOutputStream(byteOut);
			ArrayList<SearchQuery> queries = new ArrayList<SearchQuery>(this.queries.values());
			out.writeObject(queries);
			out.flush();
			bytes = byteOut.toByteArray();
			
			prefs.putByteArray("saved_queries", bytes);
			prefs.flush();
			
		} catch( IOException e ) {
			// FIXME handle
			e.printStackTrace();
		} catch( BackingStoreException e ) {
			// FIXME handle
			e.printStackTrace();
		} finally {
			Util.tryClose(out);
		}
	}
	
	private void _attachListeners() {
		ActionListener l = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				if( source == btnDoSearch )
					_doSearchClicked(e);
				else if( source == btnSaveSearch )
					_saveSearchClicked(e);
				else if( source == btnDeleteSearch )
					_deleteSearchClicked(e);
				else if( source == btnLoadSearch )
					_loadSearchClicked(e);
			}
		};
		
		btnDoSearch.addActionListener(l);
		btnSaveSearch.addActionListener(l);
		btnLoadSearch.addActionListener(l);
		btnDeleteSearch.addActionListener(l);
	}
	
	private void _loadSearchClicked(ActionEvent e) {
		String name = (String)cmbSearchName.getSelectedItem();
		if( !queries.containsKey(name) ) {
			JOptionPane.showMessageDialog(SearchFrame.this, "No such query: " + name, "Unknown Query", JOptionPane.ERROR_MESSAGE);
		} else {
			SearchQuery query = queries.get(name);
			searchPanel.fromSearchWhere(query.getSearchWhere());
		}
	}
	
	private void _saveSearchClicked(ActionEvent e) {
		String name = (String)cmbSearchName.getSelectedItem();
		if( name == null || "".equals(name = name.trim()) ) {
			JOptionPane.showMessageDialog(this, "The search name is empty.", "Empty Name", JOptionPane.ERROR_MESSAGE);
		} else {
			SearchQuery oldQuery = queries.put(name, SearchQuery.getDefault(name, searchPanel.toSearchWhere()));
			if( oldQuery == null )
				cmbSearchName.addItem(name);
			_saveQueries();
		}
	}

	private void _deleteSearchClicked(ActionEvent e) {
		String name = (String)cmbSearchName.getSelectedItem();
		int result = JOptionPane.showConfirmDialog(this, 
			"Really delete search: " + name, "Delete?", JOptionPane.YES_NO_OPTION);
		if( result != JOptionPane.YES_OPTION )
			return;
		
		queries.remove(name);
		_saveQueries();
		cmbSearchName.removeItem(name);
	}
	
	private void _doSearchClicked(ActionEvent e) {
		long s = System.currentTimeMillis();
		final SearchWhere where = searchPanel.toSearchWhere();
		long end = System.currentTimeMillis();
		System.out.println("Where copy took: " + (end - s) + " ms");
		
		// FIXME don't block the GUI thread
		
		// FIXME real connection handling
		XNATConnection conn = new XNATConnection("http://node18.cci.emory.edu:8080/xnat/REST", "nbia", "nbia");
		XNATSearch search = new XNATSearch(conn);
		
		SearchWorker worker = new SearchWorker(search, where) {
			@Override
			protected void done() {
				try {
					if( !isCancelled() )
						txtSearchResults.setText(get());
				} catch( InterruptedException e ) {
					e.printStackTrace();
				} catch( ExecutionException e ) {
					e.printStackTrace();
				}
			}
		};
		worker.execute();
		
	}
}
