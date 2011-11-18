package edu.gatech.cs7450.xnat.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import edu.gatech.cs7450.xnat.SearchWhere.SearchMethod;

/**
 * A panel for manipulating an xNAT search group.
 */
public class SearchWherePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JButton btnAddCriteria;
	private JButton btnAddGroup;
	private JComboBox searchMethod;

	
	public static void main(String[] args) {
		SearchWherePanel view = new SearchWherePanel();
		JFrame frame = new JFrame("blah");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(view);
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Create the panel.
	 */
	public SearchWherePanel() {
		setBorder(new LineBorder(new Color(0, 0, 0)));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{129, 71, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{37, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Search Method:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		searchMethod = new JComboBox();
		GridBagConstraints gbc_searchMethod = new GridBagConstraints();
		gbc_searchMethod.anchor = GridBagConstraints.WEST;
		gbc_searchMethod.insets = new Insets(0, 0, 5, 5);
		gbc_searchMethod.gridx = 1;
		gbc_searchMethod.gridy = 0;
		add(searchMethod, gbc_searchMethod);
		searchMethod.setModel(new DefaultComboBoxModel(SearchMethod.values()));
		
		btnAddCriteria = new JButton("Add Criteria");
		GridBagConstraints gbc_btnAddCriteria = new GridBagConstraints();
		gbc_btnAddCriteria.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddCriteria.gridx = 2;
		gbc_btnAddCriteria.gridy = 0;
		add(btnAddCriteria, gbc_btnAddCriteria);
		
		btnAddGroup = new JButton("Add Group");
		GridBagConstraints gbc_btnAddGroup = new GridBagConstraints();
		gbc_btnAddGroup.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddGroup.gridx = 3;
		gbc_btnAddGroup.gridy = 0;
		add(btnAddGroup, gbc_btnAddGroup);
		
		addButtonListeners();
	}
	
	private void addButtonListeners() {
		btnAddCriteria.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addSingleCriteria();
			}
		});
		btnAddGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addSearchWhere();
			}
		});
	}
	
	private int nextRow = 1;
	protected void addSingleCriteria() {
		System.out.println("SearchWherePanel.addCriteria()");
		final SingleCriteriaPanel pnlCriteria = new SingleCriteriaPanel();
		addDeletableComponent(pnlCriteria);
	}
	
	protected void addDeletableComponent(final Component component) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridwidth = 4;
		constraints.gridx = 0;
//		constraints.gridy = GridBagConstraints.RELATIVE;
		constraints.gridy = nextRow;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(component, constraints);

		final JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnDelete.removeActionListener(this);
				remove(btnDelete);
				remove(component);
				--nextRow; // fixme, can leave a gap
				revalidate();
				repaint();
			}
		});
		
		constraints.gridwidth = 1;
		constraints.gridx = 4;
		constraints.gridy = nextRow;
		constraints.fill = GridBagConstraints.NONE;
		add(btnDelete, constraints);
		
		++nextRow;
		
		revalidate();
		repaint();
	}
	
	protected void addSearchWhere() {
		final SearchWherePanel pnlSearch = new SearchWherePanel();
		addDeletableComponent(pnlSearch);
	}

}
