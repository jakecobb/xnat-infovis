package edu.gatech.cs7450.xnat.gui;

import static java.util.Arrays.asList;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import edu.gatech.cs7450.xnat.SearchField;
import edu.gatech.cs7450.xnat.SingleCriteria;
import edu.gatech.cs7450.xnat.SingleCriteria.CompareOperator;
import edu.gatech.cs7450.xnat.XNATConstants.Projects;
import edu.gatech.cs7450.xnat.XNATConstants.Scans;
import edu.gatech.cs7450.xnat.XNATConstants.Sessions;
import edu.gatech.cs7450.xnat.XNATConstants.Subjects;

public class SingleCriteriaPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JTextField txtSchemeField;
	private JTextField txtFieldValue;
	private JComboBox cmbOperator;
	
	/**
	 * Create the panel.
	 */
	public SingleCriteriaPanel() {
		setAlignmentY(Component.TOP_ALIGNMENT);
		setPreferredSize(new Dimension(500, 27));
		setMaximumSize(new Dimension(32767, 48));
		setMinimumSize(new Dimension(300, 12));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 1.0};
		gridBagLayout.rowWeights = new double[]{0.0};
		setLayout(gridBagLayout);
		
		txtFieldValue = new JTextField();
		GridBagConstraints gbc_txtFieldValue = new GridBagConstraints();
		gbc_txtFieldValue.fill = GridBagConstraints.BOTH;
		gbc_txtFieldValue.insets = new Insets(0, 0, 0, 5);
		gbc_txtFieldValue.gridx = 2;
		gbc_txtFieldValue.gridy = 0;
		add(txtFieldValue, gbc_txtFieldValue);
		txtFieldValue.setAlignmentX(Component.LEFT_ALIGNMENT);
		txtFieldValue.setAlignmentY(Component.CENTER_ALIGNMENT);
		txtFieldValue.setMaximumSize(new Dimension(2147483647, 36));
		txtFieldValue.setFont(new Font("Dialog", Font.PLAIN, 10));
		txtFieldValue.setToolTipText("The value to compare with the field value.");
		txtFieldValue.setPreferredSize(new Dimension(100, 24));
		txtFieldValue.setMinimumSize(new Dimension(100, 14));
		txtFieldValue.setColumns(10);
		
		cmbOperator = new JComboBox();
		GridBagConstraints gbc_cmbOperator = new GridBagConstraints();
		gbc_cmbOperator.fill = GridBagConstraints.VERTICAL;
		gbc_cmbOperator.insets = new Insets(0, 0, 0, 5);
		gbc_cmbOperator.gridx = 1;
		gbc_cmbOperator.gridy = 0;
		add(cmbOperator, gbc_cmbOperator);
		cmbOperator.setAlignmentX(Component.LEFT_ALIGNMENT);
		cmbOperator.setAlignmentY(Component.CENTER_ALIGNMENT);
		cmbOperator.setPreferredSize(new Dimension(60, 24));
		cmbOperator.setMinimumSize(new Dimension(60, 14));
		cmbOperator.setFont(new Font("Dialog", Font.BOLD, 10));
		cmbOperator.setModel(new DefaultComboBoxModel(CompareOperator.values()));
		cmbOperator.setToolTipText("The comparison operator.");
		cmbOperator.setMaximumSize(new Dimension(80, 36));
		
		txtSchemeField = new JTextField();
		GridBagConstraints gbc_txtSchemeField = new GridBagConstraints();
		gbc_txtSchemeField.fill = GridBagConstraints.BOTH;
		gbc_txtSchemeField.insets = new Insets(0, 0, 0, 5);
		gbc_txtSchemeField.gridx = 0;
		gbc_txtSchemeField.gridy = 0;
		add(txtSchemeField, gbc_txtSchemeField);
		txtSchemeField.setAlignmentX(Component.LEFT_ALIGNMENT);
		txtSchemeField.setAlignmentY(Component.CENTER_ALIGNMENT);
		txtSchemeField.setMaximumSize(new Dimension(2147483647, 36));
		txtSchemeField.setFont(new Font("Dialog", Font.PLAIN, 10));
		txtSchemeField.setToolTipText("The xNAT scheme field to match.");
		txtSchemeField.setPreferredSize(new Dimension(100, 24));
		txtSchemeField.setMinimumSize(new Dimension(100, 14));
		txtSchemeField.setColumns(10);
		
		_init();
	}
	
	public JTextField getSchemeField() {
		return txtSchemeField;
	}

	public JTextField getFieldValue() {
		return txtFieldValue;
	}
	
	public JComboBox getOperator() {
		return cmbOperator;
	}

	private void _init() {
		ArrayList<String>	fields = new ArrayList<String>();
		
		// reflect the SearchField constants
		for( Class<?> c : Arrays.<Class<?>>asList(Projects.class, Subjects.class, Sessions.class, Scans.class)) {
			for( Field f : c.getDeclaredFields() ) {
				if( 0 != (f.getModifiers() & Modifier.STATIC) && SearchField.class.isAssignableFrom(f.getType()) ) {
					try {
						SearchField searchField = (SearchField)f.get(null);
						fields.add(searchField.getSummary());
					} catch( IllegalAccessException e ) {
						System.err.println("Couldn't access field: " + f.getName());
					}
				}
			}
		}
		
		AutoCompleteDecorator.decorate(txtSchemeField, fields, false);
	}

	/**
	 * Copies the values into a single criteria.
	 * 
	 * @param criteria an existing criteria to copy into or <code>null</code> to create a new one
	 * @return the criteria that was copied into, will be <code>criteria</code> 
	 *         if <code>criteria</code> was not <code>null</code>
	 */
	public SingleCriteria toSingleCriteria(SingleCriteria criteria) {
		SingleCriteria c = criteria != null ? criteria : new SingleCriteria();
		c.setSchemeField(txtSchemeField.getText().trim()); // trim because spaces will fail the search
		c.setValue(txtFieldValue.getText());
		c.setOperator((CompareOperator)cmbOperator.getSelectedItem());
		return c;
	}
	
	/**
	 * Copies the values into a new single criteria.
	 * @return the single criteria
	 */
	public SingleCriteria toSingleCriteria() { 
		return toSingleCriteria(null); 
	}
	
	public void fromSingleCriteria(SingleCriteria criteria) {
		if( criteria == null ) throw new NullPointerException("criteria is null");
		txtSchemeField.setText(criteria.getSchemeField());
		txtFieldValue.setText(criteria.getValue());
		cmbOperator.setSelectedItem(criteria.getOperator());
	}
}
