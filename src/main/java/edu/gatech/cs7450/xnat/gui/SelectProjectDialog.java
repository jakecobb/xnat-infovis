package edu.gatech.cs7450.xnat.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import edu.gatech.cs7450.xnat.SearchWhere;
import edu.gatech.cs7450.xnat.SearchWhere.SearchMethod;
import edu.gatech.cs7450.xnat.SingleCriteria;
import edu.gatech.cs7450.xnat.SingleCriteria.CompareOperator;
import edu.gatech.cs7450.xnat.XNATConnection;
import edu.gatech.cs7450.xnat.XNATConstants.Projects;
import edu.gatech.cs7450.xnat.XNATConstants.Sessions;
import edu.gatech.cs7450.xnat.XNATException;
import edu.gatech.cs7450.xnat.XNATResultSet;
import edu.gatech.cs7450.xnat.XNATSearch;
import edu.gatech.cs7450.xnat.XNATTableResult;
import edu.gatech.cs7450.xnat.XNATTableResult.XNATTableRow;

public class SelectProjectDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final Logger _log = Logger.getLogger(SelectProjectDialog.class);

	private transient XNATConnection conn;

	private final JPanel contentPanel = new JPanel();
	private JComboBox cmbProject;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			SelectProjectDialog dialog = new SelectProjectDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public SelectProjectDialog(XNATConnection conn) {
		this();
		if( conn == null ) throw new NullPointerException("conn is null");
		this.conn = conn;
		
		_init();
	}

	/**
	 * Create the dialog.
	 */
	public SelectProjectDialog() {
		setTitle("Select Project");
		setBounds(100, 100, 450, 107);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblProject = new JLabel("Project");
			GridBagConstraints gbc_lblProject = new GridBagConstraints();
			gbc_lblProject.insets = new Insets(0, 0, 0, 5);
			gbc_lblProject.anchor = GridBagConstraints.EAST;
			gbc_lblProject.gridx = 0;
			gbc_lblProject.gridy = 0;
			contentPanel.add(lblProject, gbc_lblProject);
		}
		{
			cmbProject = new JComboBox();
			GridBagConstraints gbc_cmbProject = new GridBagConstraints();
			gbc_cmbProject.fill = GridBagConstraints.HORIZONTAL;
			gbc_cmbProject.gridx = 1;
			gbc_cmbProject.gridy = 0;
			contentPanel.add(cmbProject, gbc_cmbProject);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_openProjectOverview();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	private void _init() {
		try {
			XNATSearch search = new XNATSearch(conn);
			
			XNATTableResult result = search.fetchProjects();
			String id = Projects.COLUMNS.get(0);
			
			for( XNATTableRow row : result.getRows() )
				cmbProject.addItem(row.getValue(id));
		} catch( XNATException e ) {
			_log.error("Project load failed.", e);
			JOptionPane.showMessageDialog(this, "Failed to load projects.", "Load Failed", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void _openProjectOverview() {
		String project = (String)cmbProject.getSelectedItem();
		if( project == null || "".equals(project = project.trim()) ) {
			JOptionPane.showMessageDialog(this, "No project selected.", "No Project", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// query for the data for this project
		SearchWhere where = new SearchWhere(SearchMethod.AND, 
			new SingleCriteria(Sessions.PROJECT.getSummary(), CompareOperator.EQUAL, project));		
		XNATSearch search = new XNATSearch(conn);
		XNATResultSet result = search.runSearch(where);
		
		_log.info("Got result with: " + result.getNumRecords() + " reported records and " + result.getRows().size() + " actual rows.");
		throw new RuntimeException("FIXME: Open for project: " + project);
	}
}
