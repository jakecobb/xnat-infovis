package edu.gatech.cs7450.xnat.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.gatech.cs7450.xnat.SearchWhere.SearchMethod;

/**
 * Panel for editing a search method.  Consists of a label 
 * and combo box.  Key events for the combo box are passed onto 
 * to listeners of this component.
 */
public class SearchMethodPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JLabel lblGroup;
	private JComboBox cmbSearchMethod;
	private EventHandler eventHandler = new EventHandler();

	/**
	 * Create the panel.
	 */
	public SearchMethodPanel() {
		setMinimumSize(new Dimension(0, 0));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		lblGroup = new JLabel("Group:");
		GridBagConstraints gbc_lblGroup = new GridBagConstraints();
		gbc_lblGroup.insets = new Insets(0, 0, 0, 5);
		gbc_lblGroup.anchor = GridBagConstraints.EAST;
		gbc_lblGroup.gridx = 0;
		gbc_lblGroup.gridy = 0;
		add(lblGroup, gbc_lblGroup);
		
		cmbSearchMethod = new JComboBox();
		cmbSearchMethod.setPreferredSize(new Dimension(70, 24));
		cmbSearchMethod.setMinimumSize(new Dimension(70, 14));
		cmbSearchMethod.setModel(new DefaultComboBoxModel(SearchMethod.values()));
		cmbSearchMethod.addKeyListener(eventHandler);
		GridBagConstraints gbc_cmbSearchMethod = new GridBagConstraints();
		gbc_cmbSearchMethod.anchor = GridBagConstraints.WEST;
		gbc_cmbSearchMethod.gridx = 1;
		gbc_cmbSearchMethod.gridy = 0;
		add(cmbSearchMethod, gbc_cmbSearchMethod);		
	}
	
	public JComboBox getSearchMethodCombo() {
		return cmbSearchMethod;
	}
	
	public JLabel getLabel() {
		return lblGroup;
	}
	
	public SearchMethod getSearchMethod() {
		return (SearchMethod)cmbSearchMethod.getSelectedItem();
	}
	
	public void setSearchMethod(SearchMethod searchMethod) {
		if( searchMethod == null ) throw new NullPointerException("searchMethod is null");
		cmbSearchMethod.setSelectedItem(searchMethod);
	}
	
	private class EventHandler extends KeyAdapter {
		@Override public void keyPressed(KeyEvent e) {
			SearchMethodPanel.this.processKeyEvent(e);
		}
		@Override public void keyReleased(KeyEvent e) {
			SearchMethodPanel.this.processKeyEvent(e);
		}
		@Override public void keyTyped(KeyEvent e) {
			SearchMethodPanel.this.processKeyEvent(e);
		}
	}

}
