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

public class TestSearchFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final Logger _log = Logger.getLogger(TestSearchFrame.class);
	
	/** FIXME temporary hard-coded connection */
	private static XNATConnection conn = new XNATConnection("http://node18.cci.emory.edu:8080/xnat/REST", "nbia", "nbia");
	private static XNATMetaData metaData = new XNATMetaData(conn);
	
	private Preferences preferences = Preferences.userNodeForPackage(TestSearchFrame.class);

	
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
	private JComboBox cmbRootElement;
	private JButton btnScatterplot;
	private JButton btnScanGroups;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestSearchFrame frame = new TestSearchFrame();
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
	public TestSearchFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 718, 391);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{146, 0, 0, 96, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 56, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 1.0, 0.0, 0.0, 1.0, 0.0};
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
		
		cmbRootElement = new JComboBox();
		cmbRootElement.setEditable(true);
		GridBagConstraints gbc_cmbRootElement = new GridBagConstraints();
		gbc_cmbRootElement.insets = new Insets(0, 0, 5, 5);
		gbc_cmbRootElement.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbRootElement.gridx = 1;
		gbc_cmbRootElement.gridy = 1;
		contentPane.add(cmbRootElement, gbc_cmbRootElement);
		
		btnDoSearch = new JButton("Search");
		GridBagConstraints gbc_btnDoSearch = new GridBagConstraints();
		gbc_btnDoSearch.insets = new Insets(0, 0, 5, 5);
		gbc_btnDoSearch.gridx = 4;
		gbc_btnDoSearch.gridy = 1;
		contentPane.add(btnDoSearch, gbc_btnDoSearch);
		
		btnScanGroups = new JButton("Scan Groups");
		GridBagConstraints gbc_btnScanGroups = new GridBagConstraints();
		gbc_btnScanGroups.insets = new Insets(0, 0, 5, 5);
		gbc_btnScanGroups.gridx = 1;
		gbc_btnScanGroups.gridy = 2;
		contentPane.add(btnScanGroups, gbc_btnScanGroups);
		
		btnScatterplot = new JButton("Scatterplot");
		GridBagConstraints gbc_btnScatterplot = new GridBagConstraints();
		gbc_btnScatterplot.insets = new Insets(0, 0, 5, 5);
		gbc_btnScatterplot.gridx = 3;
		gbc_btnScatterplot.gridy = 2;
		contentPane.add(btnScatterplot, gbc_btnScatterplot);
		
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
		
		_init();
	}
	
	private void _init() {
		_attachListeners();
		_initSavedQueries();
		_populateRootElements();
	}
	
	private void _populateRootElements() {
		ArrayList<SearchElement> elements = 
				new ArrayList<SearchElement>(metaData.getSearchElements());
		Collections.sort(elements, new Comparator<SearchElement>() {
			@Override
			public int compare(SearchElement o1, SearchElement o2) {
				assert o1 != null && o2 != null : "arg was null";
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		for( SearchElement el : elements )
			cmbRootElement.addItem(el.getName());
		AutoCompleteDecorator.decorate(cmbRootElement);
//		
//		SortedSet<SearchElement> elements = new LinkedHashSet(metaData.getSearchElements());
//		for( SearchElement el : metaData.getSearchElements() )
//			cmbRootElement.addItem(el.getName());
	}
	
	private void _initSavedQueries() {
		Preferences prefs = preferences.node("queries");
		byte[] data = prefs.getByteArray("saved_queries", null);
		if( data == null )
			return; // no saved queries
		
		final String ERR_MSG = "Failed to load saved queries.";
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
			_log.error(ERR_MSG, e);
		} catch( IOException e ) {
			_log.error(ERR_MSG, e);
		} catch( ClassNotFoundException e ) {
			_log.error(ERR_MSG, e);
		} finally {
			Util.tryClose(in);
		}		
	}
	
	private void _saveQueries() {
		Preferences prefs = preferences.node("queries");
		
		final String ERR_MSG = "Failed to save queries.";
		
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
			_log.error(ERR_MSG, e);
		} catch( BackingStoreException e ) {
			_log.error(ERR_MSG, e);
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
				else if( source == btnScatterplot )
					_openScatterplotClicked(e);
				else if( source == btnScanGroups )
					_openScanGroupsClicked(e);
				else
					_log.warn("Unexpected source: " + source);
			}
		};
		
		btnDoSearch.addActionListener(l);
		btnSaveSearch.addActionListener(l);
		btnLoadSearch.addActionListener(l);
		btnDeleteSearch.addActionListener(l);
		btnScatterplot.addActionListener(l);
		btnScanGroups.addActionListener(l);
	}
	
	private void _openScanGroupsClicked(ActionEvent e) {
		Graph sgGraph = ScanGroupViewLoader.loadSubjects(conn);
		ScanGroupView view = new ScanGroupView(sgGraph);

		SearchWhere where = searchPanel.toSearchWhere();
		
		XNATSearch search = new XNATSearch(conn);
		XNATResultSet result = search.runSearch(where);
		
		ScanGroupViewLoader.addResultAsScanGroup(view, cmbSearchName.getSelectedItem().toString(), result);
	
		final JFrame frame = new JFrame("blah");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.add(view);
		frame.pack();
		frame.setVisible(true);
		
		view.begin();

	}
	
	private void _loadSearchClicked(ActionEvent e) {
		String name = (String)cmbSearchName.getSelectedItem();
		if( !queries.containsKey(name) ) {
			JOptionPane.showMessageDialog(TestSearchFrame.this, "No such query: " + name, "Unknown Query", JOptionPane.ERROR_MESSAGE);
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
	
	private void _openScatterplotClicked(ActionEvent e) {
		SearchWhere where = searchPanel.toSearchWhere();
		
		XNATSearch search = new XNATSearch(conn);
		XNATResultSet result = search.runSearch(where);
		
		Table table = XNATScatterPlotTableReader.convertResultSet(result);


      JFrame frame = new JFrame("XNAT Visualizer");

      frame.setContentPane(new XNATScatterPlot(table));
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      frame.pack();
      frame.setVisible(true);
	}
	
	private void _doSearchClicked(ActionEvent e) {
		long s = System.currentTimeMillis();
		final SearchWhere where = searchPanel.toSearchWhere();
		long end = System.currentTimeMillis();
		System.out.println("Where copy took: " + (end - s) + " ms");
		
		// FIXME don't block the GUI thread
		
		// FIXME real connection handling
		XNATSearch search = new XNATSearch(conn);
		
		String rootElement = cmbRootElement.getSelectedItem().toString();
		
		List<SearchField> searchFields;
		searchFields = XNATDefaults.DEFAULT_SEARCH_FIELDS;
		
//		searchFields = new ArrayList<SearchField>();//XNATDefaults.DEFAULT_SCAN_FIELDS);
////		searchFields.add(new SearchField("xnat:projectData", "ID", "string", "Project"));
//		searchFields.add(new SearchField("xnat:mrSessionData", "LABEL", "string", "MR Session"));
//		searchFields.add(new SearchField("xnat:subjectData", "ID", "string", "Subject ID"));
//		searchFields.addAll(XNATDefaults.DEFAULT_SCAN_FIELDS);
		
		
		
		SearchWorker worker = new SearchWorker(search, rootElement, searchFields, where) {
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
