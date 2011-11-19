package edu.gatech.cs7450.xnat.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.gatech.cs7450.xnat.SingleCriteria;
import edu.gatech.cs7450.xnat.SingleCriteria.CompareOperator;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.Insets;

public class SingleCriteriaPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JTextField txtSchemeField;
	private JTextField txtFieldValue;
	private JComboBox cmbOperator;
	private JButton btnDelete;

	/**
	 * Create the panel.
	 */
	public SingleCriteriaPanel() {
		setMaximumSize(new Dimension(32767, 20));
		setMinimumSize(new Dimension(130, 12));
		setLayout(new GridLayout(0, 1, 0, 0));
		
		Box horizontalBox = Box.createHorizontalBox();
		add(horizontalBox);
		
		txtSchemeField = new JTextField();
		txtSchemeField.setMaximumSize(new Dimension(2147483647, 18));
		txtSchemeField.setFont(new Font("Dialog", Font.PLAIN, 10));
		horizontalBox.add(txtSchemeField);
		txtSchemeField.setToolTipText("The xNAT scheme field to match.");
		txtSchemeField.setPreferredSize(new Dimension(50, 12));
		txtSchemeField.setMinimumSize(new Dimension(50, 12));
		txtSchemeField.setColumns(10);
		
		Component horizontalStrut = Box.createHorizontalStrut(10);
		horizontalStrut.setPreferredSize(new Dimension(2, 0));
		horizontalStrut.setMinimumSize(new Dimension(2, 0));
		horizontalBox.add(horizontalStrut);
		
		cmbOperator = new JComboBox();
		cmbOperator.setMinimumSize(new Dimension(32, 16));
		cmbOperator.setFont(new Font("Dialog", Font.BOLD, 10));
		cmbOperator.setModel(new DefaultComboBoxModel(CompareOperator.values()));
		horizontalBox.add(cmbOperator);
		cmbOperator.setToolTipText("The comparison operator.");
		cmbOperator.setMaximumSize(new Dimension(32, 18));
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(10);
		horizontalStrut_1.setPreferredSize(new Dimension(2, 0));
		horizontalStrut_1.setMinimumSize(new Dimension(2, 0));
		horizontalBox.add(horizontalStrut_1);
		
		txtFieldValue = new JTextField();
		txtFieldValue.setMaximumSize(new Dimension(2147483647, 18));
		txtFieldValue.setFont(new Font("Dialog", Font.PLAIN, 10));
		horizontalBox.add(txtFieldValue);
		txtFieldValue.setToolTipText("The value to compare with the field value.");
		txtFieldValue.setPreferredSize(new Dimension(50, 12));
		txtFieldValue.setMinimumSize(new Dimension(50, 12));
		txtFieldValue.setColumns(10);
		
		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		horizontalBox.add(horizontalStrut_2);
		
		btnDelete = new JButton("x");
		btnDelete.setMaximumSize(new Dimension(41, 18));
		btnDelete.setVisible(false);
		btnDelete.setEnabled(false);
		btnDelete.setSize(new Dimension(25, 25));
		btnDelete.setPreferredSize(new Dimension(25, 25));
		btnDelete.setMinimumSize(new Dimension(25, 12));
		btnDelete.setMargin(new Insets(0, 0, 0, 0));
		btnDelete.setFont(new Font("Dialog", Font.BOLD, 18));
		horizontalBox.add(btnDelete);

	}
	
	public JButton getDeleteButton() { return btnDelete; }
	
	/**
	 * Copies the values into a single criteria.
	 * 
	 * @param criteria an existing criteria to copy into or <code>null</code> to create a new one
	 * @return the criteria that was copied into, will be <code>criteria</code> 
	 *         if <code>criteria</code> was not <code>null</code>
	 */
	public SingleCriteria toSingleCriteria(SingleCriteria criteria) {
		SingleCriteria c = criteria != null ? criteria : new SingleCriteria();
		c.setSchemeField(txtSchemeField.getText());
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
