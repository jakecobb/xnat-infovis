package edu.gatech.cs7450.xnat.gui;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import edu.gatech.cs7450.xnat.XNATConnection;

/**
 * The main application frame.
 */
public class ApplicationFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final Logger _log = Logger.getLogger(ApplicationFrame.class);
	
	private transient XNATConnection connection;
	
	private JPanel contentPane;

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
		setBounds(100, 100, 450, 300);
		
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
		contentPane.setLayout(new GridLayout(1, 0, 0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane);
		
		JPanel searchPanel = new SearchFrame();
		JPanel panel = searchPanel;
		tabbedPane.addTab("Search", null, panel, null);
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
}
