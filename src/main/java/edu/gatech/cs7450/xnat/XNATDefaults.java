package edu.gatech.cs7450.xnat;


import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import edu.gatech.cs7450.xnat.XNATConstants.Scans;
import edu.gatech.cs7450.xnat.XNATConstants.Sessions;

/**
 * Various default values for xNAT.
 */
public class XNATDefaults {
	/** The default search root element type. */
	public static final String DEFAULT_SEARCH_ROOT = Sessions.ELEMENT;
	
	/** The default scan fields we are supporting. */
	public static final List<SearchField> DEFAULT_SCAN_FIELDS = unmodifiableList(asList(
		Scans.TYPE,
		Scans.COIL,
		Scans.QUALITY,
		Scans.SERIES_DESCRIPTION,
		Scans.FRAMES,
		Scans.FIELDSTRENGTH,
		Scans.PARAMETERS_IMAGETYPE,
		Scans.PARAMETERS_FOV_X,
		Scans.PARAMETERS_FOV_Y,
		Scans.PARAMETERS_TE,
		Scans.PARAMETERS_TR,
		Scans.PARAMETERS_TI,
		Scans.PARAMETERS_FLIP,
		Scans.PARAMETERS_VOXELRES_UNITS,
		Scans.PARAMETERS_VOXELRES_X,
		Scans.PARAMETERS_VOXELRES_Y,
		Scans.PARAMETERS_VOXELRES_Z));
	
	/** The default set of fields we want to retrieve. */
	public static final List<SearchField> DEFAULT_SEARCH_FIELDS;
	static {
		ArrayList<SearchField> searchFields = new ArrayList<SearchField>(asList(
			Sessions.PROJECT,
			Sessions.SUBJECT_ID,
			Sessions.SESSION_ID));
		searchFields.addAll(DEFAULT_SCAN_FIELDS);
		searchFields.trimToSize();
		DEFAULT_SEARCH_FIELDS = unmodifiableList(searchFields);
	}
	
	/** Nominal fields to use for e.g. the scatterplot. */
	public static final List<SearchField> DEFAULT_NOMINAL_FIELDS = unmodifiableList(asList(
		Sessions.PROJECT,
		Sessions.SUBJECT_ID,
		Sessions.SESSION_ID,
		Scans.TYPE,
		Scans.COIL,
		Scans.QUALITY,
		Scans.PARAMETERS_IMAGETYPE
	));
	
	/** Numeric / quantitative fields for e.g. the scatterplot. */
	public static final List<SearchField> DEFAULT_QUANT_FIELDS = unmodifiableList(asList(
		Scans.PARAMETERS_TE,
		Scans.PARAMETERS_TI,
		Scans.PARAMETERS_TR,
		Scans.PARAMETERS_FOV_X,
		Scans.PARAMETERS_FOV_Y,
		Scans.PARAMETERS_VOXELRES_X,
		Scans.PARAMETERS_VOXELRES_Y,
		Scans.PARAMETERS_VOXELRES_Z,
		Scans.PARAMETERS_FLIP,
		Scans.FRAMES,
		Scans.FIELDSTRENGTH
	));
	
	private XNATDefaults() { }
}
