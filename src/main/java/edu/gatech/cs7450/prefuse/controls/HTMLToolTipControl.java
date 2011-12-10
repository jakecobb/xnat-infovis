package edu.gatech.cs7450.prefuse.controls;

import java.awt.event.MouseEvent;

import org.apache.commons.lang.StringEscapeUtils;

import prefuse.Display;
import prefuse.controls.ControlAdapter;
import prefuse.visual.VisualItem;

/**
 * Prefuse tooltip control that uses HTML and gives more control over 
 * the output than the provided <code>ToolTipControl</code>.
 */
public class HTMLToolTipControl extends ControlAdapter {
	/** If we should show the field label. */
	protected boolean showLabel = false;
	
	/** Seperator between each field value. */
	protected String fieldSep = "<br />";
	
	/** Appended before the start of the fields. */
	protected String beforeFields = "";
	
	/** Appended after the end of the fields. */
	protected String afterFields = "";
	
	/** Appended before a field label. */
	protected String beforeLabel = "";
	
	/** Appeneded after a field label. */
	protected String afterLabel = "";
	
	/** Appended before a field value. */
	protected String beforeValue = "";
	
	/** Appended after a field value. */
	protected String afterValue = "";
	
	/** The fields to use. */
	protected String[] fields;
	
	/** Optional, used to override the field name as the label. */
	protected String[] labelOverrides;
	
	/** Reused for tooltip building. */
	protected StringBuilder sbuf = new StringBuilder();
	
	/**
	 * Creates an HTML tooltip control using the given fields.
	 * 
	 * @param fields the fields to use
	 * @throws IllegalArgumentException if there is not at least one field
	 */
	public HTMLToolTipControl(String... fields) {
		if( fields == null || fields.length == 0 )
			throw new IllegalArgumentException("Need at least one field.");
		this.fields = fields;
	}
	
	@Override
	public void itemEntered(VisualItem item, MouseEvent e) {
		Display disp = (Display)e.getSource();
		sbuf.setLength(0);
		sbuf.append("<html>").append(beforeFields);
		
		boolean isFirst = true;
		for( int i = 0; i < fields.length; ++i ) {
			String field = fields[i];
			if( item.canGetString(field) ) {
				if( !isFirst )
					sbuf.append(fieldSep);
				else
					isFirst = false;
				
				// FIXME escaping?
				if( showLabel ) {
					String label = labelOverrides != null ? labelOverrides[i] : null;
					if( label == null )
						label = escapeHtml(field);
					
					sbuf.append(beforeLabel).append(label).append(afterLabel);
				}
				
				sbuf.append(beforeValue).append(escapeHtml(item.getString(field))).append(afterValue);
			}
		}
		
		sbuf.append(afterFields).append("</html>");
		
		disp.setToolTipText(sbuf.toString());
	}
	
	@Override
	public void itemExited(VisualItem item, MouseEvent e) {
		Display disp = (Display)e.getSource();
		disp.setToolTipText(null);
	}
	
	/** 
	 * Escapes HTML in a label or field value.
	 * 
	 * @param value the value to escape
	 * @return the escaped value, or <code>""</code> if <code>value</code> is <code>null</code>
	 */
	protected String escapeHtml(String value) {
		if( value == null )
			return "";
		return StringEscapeUtils.escapeHtml(value);
	}

// accessors
	
	/**
	 * Sets the label overrides, which should match the field order.
	 * <p>A <code>null</code> entry means use the field name as the label.</p>
	 * 
	 * <p>Label overrides are not escaped, so you may include HTML in them.</p>
	 * 
	 * @param labels the label overrides or <code>null</code> to clear the overrides
	 */
	public void setLabelOverrides(String... labels) {
		if( labels == null || labels.length >= fields.length ) {
			labelOverrides = labels;
		} else {
			// fill in nulls to match field length
			String[] overrides = new String[fields.length];
			System.arraycopy(labels, 0, overrides, 0, labels.length);
		}
	}
	
	public String[] getLabelOverrides() {
		return labelOverrides;
	}
	
	public boolean getShowLabel() {
		return showLabel;
	}

	public void setShowLabel(boolean showLabel) {
		this.showLabel = showLabel;
	}

	public String getFieldSep() {
		return fieldSep;
	}

	public void setFieldSep(String fieldSep) {
		this.fieldSep = fieldSep != null ? fieldSep : "";
	}

	public String getBeforeFields() {
		return beforeFields;
	}

	public void setBeforeFields(String beforeFields) {
		this.beforeFields = beforeFields != null ? beforeFields : "";
	}

	public String getAfterFields() {
		return afterFields;
	}

	public void setAfterFields(String afterFields) {
		this.afterFields = afterFields != null ? afterFields : "";
	}

	public String getBeforeLabel() {
		return beforeLabel;
	}

	public void setBeforeLabel(String beforeLabel) {
		this.beforeLabel = beforeLabel != null ? beforeLabel : "";
	}

	public String getAfterLabel() {
		return afterLabel;
	}

	public void setAfterLabel(String afterLabel) {
		this.afterLabel = afterLabel != null ? afterLabel : "";
	}

	public String getBeforeValue() {
		return beforeValue;
	}

	public void setBeforeValue(String beforeValue) {
		this.beforeValue = beforeValue != null ? beforeValue : "";
	}

	public String getAfterValue() {
		return afterValue;
	}

	public void setAfterValue(String afterValue) {
		this.afterValue = afterValue != null ? afterValue : "";
	}
}
