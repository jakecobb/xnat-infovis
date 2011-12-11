package edu.gatech.cs7450.prefuse.controls;

/**
 * An HTML tooltip that uses a <code>&lt;table&gt;</code> format.
 */
public class TableToolTipControl extends HTMLToolTipControl {

	/**
	 * Creates an HTML table tooltip control using the given fields.
	 * @param fields the fields to use
	 */
	public TableToolTipControl(String... fields) {
		super(fields);
		this.beforeFields = "<table border='0' cellpadding='1' cellspacing='0' width='100%'><tr>";
		this.beforeLabel = "<td align='right' valign='top'><b>";
		this.afterLabel = "</b></td>";
		this.beforeValue = "<td align='left' valign='top'>";
		this.afterValue = "</td>";
		this.fieldSep = "</tr><tr>";
		this.afterFields = "</tr></table>";
	}

	/** 
	 * {@inheritDoc}
	 * Extends super to make spaces non-breaking.
	 */
	@Override
	protected String escapeHtml(String value) {
		String escaped = super.escapeHtml(value);
		return escaped.replace(" ", "&nbsp;");
	}
}
