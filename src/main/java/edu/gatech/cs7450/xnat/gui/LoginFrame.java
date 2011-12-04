package edu.gatech.cs7450.xnat.gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import edu.gatech.cs7450.xnat.XNATConnection;
import edu.gatech.cs7450.xnat.XNATException;
import edu.gatech.cs7450.xnat.settings.LoginSettings;
import edu.gatech.cs7450.xnat.settings.SettingsException;

/**
 * Basic login window.
 */
public class LoginFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private static final Logger _log = Logger.getLogger(LoginFrame.class);
	
	/** The settings store. */
	private LoginSettings settings = new LoginSettings();
	
	private JPanel contentPane;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JComboBox cmbServer;
	private JButton btnLogin;
	private JButton btnDelete;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginFrame frame = new LoginFrame();
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
	public LoginFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.UNRELATED_GAP_COLSPEC,
				ColumnSpec.decode("right:default"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("left:default:grow"),
				FormFactory.GLUE_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.UNRELATED_GAP_COLSPEC,},
			new RowSpec[] {
				RowSpec.decode("bottom:default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.UNRELATED_GAP_ROWSPEC,
				RowSpec.decode("top:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("top:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("top:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("top:default:grow"),}));
		
		JLabel label = new JLabel("xNAT Information Visualization");
		label.setVerticalAlignment(SwingConstants.BOTTOM);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("Dialog", Font.BOLD, 16));
		contentPane.add(label, "2, 1, 5, 1");
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setHorizontalAlignment(SwingConstants.RIGHT);
		contentPane.add(lblUsername, "2, 4, right, default");
		
		txtUsername = new JTextField();
		contentPane.add(txtUsername, "4, 4, 3, 1, fill, default");
		txtUsername.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setHorizontalAlignment(SwingConstants.RIGHT);
		contentPane.add(lblPassword, "2, 6, right, default");
		
		txtPassword = new JPasswordField();
		contentPane.add(txtPassword, "4, 6, 3, 1, fill, default");
		txtPassword.setColumns(10);
		
		JLabel lblServer = new JLabel("Server:");
		contentPane.add(lblServer, "2, 8, right, default");
		
		cmbServer = new JComboBox();
		cmbServer.setEditable(true);
		contentPane.add(cmbServer, "4, 8, 3, 1, fill, default");
		
		btnLogin = new JButton("Login");
		contentPane.add(btnLogin, "4, 11, right, default");
		
		btnDelete = new JButton("Delete");
		contentPane.add(btnDelete, "6, 11, left, default");
		
		_init();
	}
	
	private void _init() {
		_initFromSettings();
		_attachListeners();
	}
	
	/** Attaches various listeners. */
	private void _attachListeners() {
		btnDelete.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				final String host = cmbServer.getSelectedItem().toString().trim();
				if( "".equals(host) )
					return;
				
				int resp = JOptionPane.showConfirmDialog(LoginFrame.this, 
					"Really delete server?<br />" + host, 
					"Confirm Delete", JOptionPane.YES_NO_OPTION);
				if( resp != JOptionPane.YES_OPTION ) {
					_log.info("Delete declined, resp=" + resp);
					return;
				}
				
				try {
					settings.removeConnection(host);
					cmbServer.removeItem(host);
				} catch( SettingsException ex ) {
					_log.error(ex.getMessage(), ex);
					JOptionPane.showMessageDialog(LoginFrame.this, "Error saving changed settings.", 
						"Delete Failed.", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		ActionListener loginListener = new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				_login();
			}
		};
		btnLogin.addActionListener(loginListener);
		txtUsername.addActionListener(loginListener);
		txtPassword.addActionListener(loginListener);
		
		cmbServer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( "comboEdited".equals(e.getActionCommand()) )
					_login();
			}
		});
	}

	/** Loads the UI widgets from the stored connection settings. */
	private void _initFromSettings() {
		try {
			List<String> users = settings.getUsers(),
			             hosts = settings.getHosts();
			
			// add hosts to combo
			Collections.sort(hosts);
			for( String host : hosts )
				cmbServer.addItem(host);
			AutoCompleteDecorator.decorate(cmbServer);
			
			// auto-complete for users
			Collections.sort(users);
			AutoCompleteDecorator.decorate(txtUsername, users, false);
			
			if( _log.isInfoEnabled() ) {
				_log.info("Loaded users: " + users);
				_log.info("Loaded hosts: " + hosts);
			}
		} catch( SettingsException e ) {
			_log.error("Could not init user/host autocomplete.", e);
		}
	}
	
	/**
	 * Peforms the login with the current settings.
	 */
	private void _login() {
		// get and check host and username
		final String host = cmbServer.getSelectedItem().toString().trim();
		if( "".equals(host) ) {
			JOptionPane.showMessageDialog(LoginFrame.this, "No host specified.", "No Host", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		final String user = txtUsername.getText().trim();
		if( "".equals(user) ) {
			JOptionPane.showMessageDialog(LoginFrame.this, "No username specified.", "No User", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		XNATConnection conn = new XNATConnection(host, user, new String(txtPassword.getPassword()));
		
		// FIXME don't block GUI thread for auth test and connection storing 
		
		// test authentication before trying to store
		try {
			conn.authenticate();
		} catch( XNATException ex ) {
			_log.debug(user + "@" + host + " authentication failed.", ex);
			JOptionPane.showMessageDialog(LoginFrame.this, 
				"Could not authenticate with the given credentials.\n" +
					"Please check your credentials and if the network is up.", 
				"Authentication Failed", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		try {
			// store the new connection
			settings.addConnection(conn);
			
			// add to the combo if not already present
			HashSet<String> items = new HashSet<String>();
			for( int i = 0, ilen = cmbServer.getItemCount(); i < ilen; ++i )
				items.add(cmbServer.getItemAt(i).toString());
			if( !items.contains(host) )
				cmbServer.addItem(host);
			
		} catch( SettingsException ex ) {
			_log.error("Error adding host: " + host, ex);
			JOptionPane.showMessageDialog(LoginFrame.this, "Failed to save connection settings.", "Save Failed", JOptionPane.WARNING_MESSAGE);
		}
		
		// TODO open some other window with the new connection
		_log.info("FIXME: Open with connection: " + conn);
	}
}
