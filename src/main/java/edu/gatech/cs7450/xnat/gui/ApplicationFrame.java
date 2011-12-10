package edu.gatech.cs7450.xnat.gui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import edu.gatech.cs7450.xnat.SearchQuery;
import edu.gatech.cs7450.xnat.XNATConnection;
import edu.gatech.cs7450.xnat.XNATException;
import edu.gatech.cs7450.xnat.XNATResultSet;
import edu.gatech.cs7450.xnat.XNATSearch;

/**
 * The main application frame.
 */
public class ApplicationFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final Logger _log = Logger.getLogger(ApplicationFrame.class);
	
	private transient XNATConnection connection;
	
	private JPanel contentPane;
	private SearchFrame searchFrame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ApplicationFrame frame = new ApplicationFrame();
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
	public ApplicationFrame() {
		setTitle("xNAT Information Visualization");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 500, 300);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		mnFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(mnFile);
		
		JMenuItem mntmLogout = new JMenuItem("Logout");
		mntmLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LoginFrame login = new LoginFrame();
				login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				setVisible(false);
				login.setVisible(true);
				dispose();
			}
		});
		mntmLogout.setMnemonic(KeyEvent.VK_L);
		mnFile.add(mntmLogout);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ApplicationFrame.this.setVisible(false);
				ApplicationFrame.this.dispose();
			}
		});
		mntmExit.setMnemonic(KeyEvent.VK_X);
		mnFile.add(mntmExit);
		
		JMenu mnViews = new JMenu("Views");
		mnViews.setMnemonic(KeyEvent.VK_V);
		menuBar.add(mnViews);
		
		JMenuItem mntmScanGroups = new JMenuItem("Scan Groups");
		mntmScanGroups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_showScanGroupView();
			}
		});
		mnViews.add(mntmScanGroups);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{274, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 242, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JButton btnScatterplot = new JButton("Search to Scatterplot");
		btnScatterplot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_searchToScatterplot();
			}
		});
		GridBagConstraints gbc_btnScatterplot = new GridBagConstraints();
		gbc_btnScatterplot.insets = new Insets(0, 0, 5, 5);
		gbc_btnScatterplot.gridx = 0;
		gbc_btnScatterplot.gridy = 1;
		contentPane.add(btnScatterplot, gbc_btnScatterplot);
		
		JButton btnScanGroups = new JButton("Search to Scan Groups");
		GridBagConstraints gbc_btnScanGroups = new GridBagConstraints();
		gbc_btnScanGroups.insets = new Insets(0, 0, 5, 0);
		gbc_btnScanGroups.gridx = 1;
		gbc_btnScanGroups.gridy = 1;
		contentPane.add(btnScanGroups, gbc_btnScanGroups);
		
		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridwidth = 2;
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.insets = new Insets(0, 0, 5, 5);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 2;
		contentPane.add(separator, gbc_separator);
		
		searchFrame = new SearchFrame();
		GridBagLayout gbl_searchFrame = (GridBagLayout) searchFrame.getLayout();
		gbl_searchFrame.rowWeights = new double[]{0.0, 1.0};
		gbl_searchFrame.rowHeights = new int[]{0, 0};
		gbl_searchFrame.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 1.0};
		gbl_searchFrame.columnWidths = new int[]{97, 0, 0, 96, 0};
		GridBagConstraints gbc_searchFrame = new GridBagConstraints();
		gbc_searchFrame.gridwidth = 2;
		gbc_searchFrame.fill = GridBagConstraints.BOTH;
		gbc_searchFrame.gridx = 0;
		gbc_searchFrame.gridy = 3;
		contentPane.add(searchFrame, gbc_searchFrame);
	}

	public ApplicationFrame(XNATConnection conn) {
		this();
		this.connection = conn;
	}
	
	private void _showScanGroupView() {
		ScanGroupPanel sgPanel = new ScanGroupPanel(connection);
		
		JFrame frame = new JFrame("Scan Groups View");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(sgPanel);
		frame.pack();
		frame.setVisible(true);
	}
	
	private void _searchToScatterplot() {
		try {
			SearchQuery query = searchFrame.toSearchQuery();
			XNATSearch search = new XNATSearch(connection);
			XNATResultSet result = search.runSearch(query);
			
			ScatterPlotDialog dialog = new ScatterPlotDialog(result);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch( XNATException e ) {
			_log.error("Query failed.", e);
			JOptionPane.showMessageDialog(this, "Search query failed.", "Query Failed", JOptionPane.ERROR_MESSAGE);
		}
	}
}
