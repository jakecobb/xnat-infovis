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
		setMinimumSize(new Dimension(130, 30));
		setLayout(new GridLayout(0, 1, 0, 0));
		
		Box horizontalBox = Box.createHorizontalBox();
		add(horizontalBox);
		
		txtSchemeField = new JTextField();
		horizontalBox.add(txtSchemeField);
		txtSchemeField.setToolTipText("The xNAT scheme field to match.");
		txtSchemeField.setPreferredSize(new Dimension(50, 19));
		txtSchemeField.setMinimumSize(new Dimension(50, 19));
		txtSchemeField.setColumns(10);
		
		Component horizontalStrut = Box.createHorizontalStrut(10);
		horizontalBox.add(horizontalStrut);
		
		cmbOperator = new JComboBox();
		cmbOperator.setModel(new DefaultComboBoxModel(CompareOperator.values()));
		horizontalBox.add(cmbOperator);
		cmbOperator.setToolTipText("The comparison operator.");
		cmbOperator.setMaximumSize(new Dimension(50, 50));
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(10);
		horizontalBox.add(horizontalStrut_1);
		
		txtFieldValue = new JTextField();
		horizontalBox.add(txtFieldValue);
		txtFieldValue.setToolTipText("The value to compare with the field value.");
		txtFieldValue.setPreferredSize(new Dimension(50, 19));
		txtFieldValue.setMinimumSize(new Dimension(50, 19));
		txtFieldValue.setColumns(10);
		
		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		horizontalBox.add(horizontalStrut_2);
		
		btnDelete = new JButton("x");
		btnDelete.setVisible(false);
		btnDelete.setEnabled(false);
		btnDelete.setSize(new Dimension(25, 25));
		btnDelete.setPreferredSize(new Dimension(25, 25));
		btnDelete.setMinimumSize(new Dimension(25, 25));
		btnDelete.setMargin(new Insets(0, 0, 0, 0));
		btnDelete.setFont(new Font("Dialog", Font.BOLD, 18));
		horizontalBox.add(btnDelete);

	}
	
	public JButton getDeleteButton() { return btnDelete; }
	
	public SingleCriteria toSingleCriteria() {
		String field = txtSchemeField.getText(), value = txtFieldValue.getText();
		CompareOperator operator = CompareOperator.valueOf(cmbOperator.getSelectedItem().toString());
		return new SingleCriteria(field, operator, value);
	}

}
