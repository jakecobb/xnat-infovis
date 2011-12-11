package edu.gatech.cs7450.xnat.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.combobox.MapComboBoxModel;

import prefuse.data.Table;
import edu.gatech.cs7450.prefuse.XNATScatterPlot;
import edu.gatech.cs7450.prefuse.XNATScatterPlotTableReader;
import edu.gatech.cs7450.xnat.SearchField;
import edu.gatech.cs7450.xnat.XNATConstants.Scans;
import edu.gatech.cs7450.xnat.XNATConstants.Sessions;
import edu.gatech.cs7450.xnat.XNATDefaults;
import edu.gatech.cs7450.xnat.XNATException;
import edu.gatech.cs7450.xnat.XNATResultSet;

public class ScatterPlotDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final Logger _log = Logger.getLogger(ScatterPlotDialog.class);
	
	private XNATResultSet searchResult;
	
	private final JPanel contentPanel = new JPanel();
	private JComboBox cmbColor;
	private JComboBox cmbShape;
	private JComboBox cmbSize;
	private JComboBox cmbAxisY;
	private JComboBox cmbAxisX;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ScatterPlotDialog dialog = new ScatterPlotDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public ScatterPlotDialog(XNATResultSet searchResult) {
		this();
		if( searchResult == null ) throw new NullPointerException("searchResult is null");
		this.searchResult = searchResult;
	}

	/**
	 * Create the dialog.
	 */
	public ScatterPlotDialog() {
		setTitle("Scatterplot Settings");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblXaxis = new JLabel("X-Axis");
			GridBagConstraints gbc_lblXaxis = new GridBagConstraints();
			gbc_lblXaxis.anchor = GridBagConstraints.EAST;
			gbc_lblXaxis.insets = new Insets(0, 0, 5, 5);
			gbc_lblXaxis.gridx = 0;
			gbc_lblXaxis.gridy = 0;
			contentPanel.add(lblXaxis, gbc_lblXaxis);
		}
		{
			cmbAxisX = new JComboBox();
			GridBagConstraints gbc_cmbAxisX = new GridBagConstraints();
			gbc_cmbAxisX.insets = new Insets(0, 0, 5, 0);
			gbc_cmbAxisX.fill = GridBagConstraints.HORIZONTAL;
			gbc_cmbAxisX.gridx = 1;
			gbc_cmbAxisX.gridy = 0;
			contentPanel.add(cmbAxisX, gbc_cmbAxisX);
		}
		{
			JLabel lblYaxis = new JLabel("Y-Axis");
			GridBagConstraints gbc_lblYaxis = new GridBagConstraints();
			gbc_lblYaxis.anchor = GridBagConstraints.EAST;
			gbc_lblYaxis.insets = new Insets(0, 0, 5, 5);
			gbc_lblYaxis.gridx = 0;
			gbc_lblYaxis.gridy = 1;
			contentPanel.add(lblYaxis, gbc_lblYaxis);
		}
		{
			cmbAxisY = new JComboBox();
			GridBagConstraints gbc_cmbAxisY = new GridBagConstraints();
			gbc_cmbAxisY.insets = new Insets(0, 0, 5, 0);
			gbc_cmbAxisY.fill = GridBagConstraints.HORIZONTAL;
			gbc_cmbAxisY.gridx = 1;
			gbc_cmbAxisY.gridy = 1;
			contentPanel.add(cmbAxisY, gbc_cmbAxisY);
		}
		{
			JLabel lblSize = new JLabel("Size");
			GridBagConstraints gbc_lblSize = new GridBagConstraints();
			gbc_lblSize.anchor = GridBagConstraints.EAST;
			gbc_lblSize.insets = new Insets(0, 0, 5, 5);
			gbc_lblSize.gridx = 0;
			gbc_lblSize.gridy = 2;
			contentPanel.add(lblSize, gbc_lblSize);
		}
		{
			cmbSize = new JComboBox();
			GridBagConstraints gbc_cmbSize = new GridBagConstraints();
			gbc_cmbSize.insets = new Insets(0, 0, 5, 0);
			gbc_cmbSize.fill = GridBagConstraints.HORIZONTAL;
			gbc_cmbSize.gridx = 1;
			gbc_cmbSize.gridy = 2;
			contentPanel.add(cmbSize, gbc_cmbSize);
		}
		{
			JLabel lblShape = new JLabel("Shape");
			GridBagConstraints gbc_lblShape = new GridBagConstraints();
			gbc_lblShape.anchor = GridBagConstraints.EAST;
			gbc_lblShape.insets = new Insets(0, 0, 5, 5);
			gbc_lblShape.gridx = 0;
			gbc_lblShape.gridy = 3;
			contentPanel.add(lblShape, gbc_lblShape);
		}
		{
			cmbShape = new JComboBox();
			GridBagConstraints gbc_cmbShape = new GridBagConstraints();
			gbc_cmbShape.insets = new Insets(0, 0, 5, 0);
			gbc_cmbShape.fill = GridBagConstraints.HORIZONTAL;
			gbc_cmbShape.gridx = 1;
			gbc_cmbShape.gridy = 3;
			contentPanel.add(cmbShape, gbc_cmbShape);
		}
		{
			JLabel lblColor = new JLabel("Color");
			GridBagConstraints gbc_lblColor = new GridBagConstraints();
			gbc_lblColor.insets = new Insets(0, 0, 0, 5);
			gbc_lblColor.anchor = GridBagConstraints.EAST;
			gbc_lblColor.gridx = 0;
			gbc_lblColor.gridy = 4;
			contentPanel.add(lblColor, gbc_lblColor);
		}
		{
			cmbColor = new JComboBox();
			GridBagConstraints gbc_cmbColor = new GridBagConstraints();
			gbc_cmbColor.fill = GridBagConstraints.HORIZONTAL;
			gbc_cmbColor.gridx = 1;
			gbc_cmbColor.gridy = 4;
			contentPanel.add(cmbColor, gbc_cmbColor);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_okClicked();
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
		
		_init();
	}

	private void _init() {
		LinkedHashMap<String, SearchField> nominalMap = new LinkedHashMap<String, SearchField>();
		for( SearchField field : XNATDefaults.DEFAULT_NOMINAL_FIELDS ) {
			nominalMap.put(field.getFieldId(), field);
		}
		
		LinkedHashMap<String, SearchField> quantMap = new LinkedHashMap<String, SearchField>();
		for( SearchField field : XNATDefaults.DEFAULT_QUANT_FIELDS ) {
			quantMap.put(field.getFieldId(), field);
		}
		
		cmbColor.setModel(new MapComboBoxModel<String, SearchField>(nominalMap));
		cmbShape.setModel(new MapComboBoxModel<String, SearchField>(nominalMap));
		cmbAxisX.setModel(new MapComboBoxModel<String, SearchField>(quantMap));
		cmbAxisY.setModel(new MapComboBoxModel<String, SearchField>(quantMap));
		
		LinkedHashMap<String, SearchField> optQuantMap = new LinkedHashMap<String, SearchField>();
		optQuantMap.put("NONE", null);
		optQuantMap.putAll(quantMap);
		cmbSize.setModel(new MapComboBoxModel<String, SearchField>(optQuantMap));
	}
	
	private SearchField getSelectedField(JComboBox combo) {
		@SuppressWarnings("unchecked")
		MapComboBoxModel<String, SearchField> model = (MapComboBoxModel<String, SearchField>)combo.getModel();
		return model.getValue(combo.getSelectedItem());
	}
	
	private void _okClicked() {
		SearchField xField = getSelectedField(cmbAxisX),
		            yField = getSelectedField(cmbAxisY),
		        shapeField = getSelectedField(cmbShape),
		        colorField = getSelectedField(cmbColor),
		         sizeField = getSelectedField(cmbSize);
		assert xField != null && yField != null && shapeField != null && colorField != null;
		
		ArrayList<SearchField> requiredFields = new ArrayList<SearchField>(Arrays.asList(
			Sessions.PROJECT, Sessions.SUBJECT_ID, Sessions.SESSION_ID, Scans.TYPE, 
			xField, yField, shapeField, colorField
		));
		if( sizeField != null )
			requiredFields.add(sizeField);
		
		try {			
			Table scatterPlotTable = XNATScatterPlotTableReader.convertResultSet(searchResult, requiredFields);
			String sizeColumn = sizeField != null ? sizeField.getFieldId() : XNATScatterPlotTableReader.CONSTANT_SIZE_COL;
			XNATScatterPlot scatterPlot = new XNATScatterPlot(scatterPlotTable, xField.getFieldId(), 
				yField.getFieldId(), sizeColumn, shapeField.getFieldId(), colorField.getFieldId());

	      JFrame frame = new JFrame("XNAT Visualizer");

	      frame.setContentPane(scatterPlot);
	      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	      frame.pack();
	      
	      this.setVisible(false);
	      frame.setVisible(true);
	      this.dispose();
		} catch( XNATException e ) {
			_log.error("Scatter plot query failed.", e);
			JOptionPane.showMessageDialog(this, "Search query failed.", "Query Failed", JOptionPane.ERROR_MESSAGE);
		}
			
	}
}
