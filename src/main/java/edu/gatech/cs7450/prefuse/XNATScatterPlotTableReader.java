package edu.gatech.cs7450.prefuse;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.log4j.Logger;

import prefuse.data.DataTypeException;
import prefuse.data.Table;
import edu.gatech.cs7450.xnat.SearchField;
import edu.gatech.cs7450.xnat.XNATConstants.Scans;
import edu.gatech.cs7450.xnat.XNATConstants.Sessions;
import edu.gatech.cs7450.xnat.XNATException;
import edu.gatech.cs7450.xnat.XNATResultSet;
import edu.gatech.cs7450.xnat.XNATResultSet.XNATResultSetRow;

public class XNATScatterPlotTableReader {
	private static final Logger _log = Logger.getLogger(XNATScatterPlotTableReader.class);
	
	public static String CONSTANT_SIZE_COL = "constant_size";

	public static Table convertResultSet(XNATResultSet result, Collection<? extends SearchField> requiredFields) {
		if( result == null ) throw new NullPointerException("result is null");
		final boolean _debug = _log.isDebugEnabled();

		// columns from test CSV file:
		// Project,Subject,Experiments,Scan type,Frames Image Type,Field Strength,Vox. Res.,FOV,TR,TE,TI,Flip ,Image
		List<SearchField> fields = Arrays.asList(
			Sessions.PROJECT, 
			Sessions.SUBJECT_ID, 
			Sessions.SESSION_ID,
			Scans.ID,
			Scans.TYPE, 
			Scans.SERIES_DESCRIPTION,
			Scans.FRAMES, 
			Scans.PARAMETERS_IMAGETYPE,
			Scans.FIELDSTRENGTH, 
			Scans.QUALITY,
			Scans.PARAMETERS_VOXELRES_X,
			Scans.PARAMETERS_VOXELRES_Y,
			Scans.PARAMETERS_VOXELRES_Z,
			Scans.PARAMETERS_FOV_X,
			Scans.PARAMETERS_FOV_Y,
			Scans.PARAMETERS_TR, 
			Scans.PARAMETERS_TE, 
			Scans.PARAMETERS_TI, 
			Scans.PARAMETERS_FLIP
			// FIXME scatterplot expects image data here
		);
		
		if( requiredFields == null ) {
			requiredFields = new LinkedHashSet<SearchField>(Arrays.asList(
				Sessions.PROJECT,
				Sessions.SUBJECT_ID,
				Sessions.SESSION_ID,
				Scans.TYPE,
				Scans.FRAMES, 
				Scans.FIELDSTRENGTH, 
				Scans.PARAMETERS_VOXELRES_X,
				Scans.PARAMETERS_VOXELRES_Y,
				Scans.PARAMETERS_VOXELRES_Z,
				Scans.PARAMETERS_FOV_X,
				Scans.PARAMETERS_FOV_Y,
				Scans.PARAMETERS_TR, 
				Scans.PARAMETERS_TE, 
				Scans.PARAMETERS_TI
				
			));
		}
		
		HashMap<String, Class<?>> typeMap = new HashMap<String, Class<?>>(6);
		typeMap.put("string", String.class);
		// object instead of primitive to allow null for missing
//		typeMap.put("integer", Integer.class); 
//		typeMap.put("float", Float.class);
		typeMap.put("integer", int.class);
		typeMap.put("float", float.class);
		
		Table table = new Table();

		int colIdx = 0;
		for( SearchField field : fields ) {
			Class<?> type = typeMap.get(field.getType());
			if( type == null )
				throw new XNATException("No mapping for type: " + field.getType());
			
			String colName = field.getFieldId();
			if( _debug ) _log.debug("Adding column: colName=" + colName + ", type=" + type.getName());
			table.addColumn(colName, type, null);
		}
		table.addColumn("Image", byte[].class, null); // FIXME need the data 
		table.addColumn(CONSTANT_SIZE_COL, int.class, 1); // used if no size mapping requested
		
		int missing = 0, total = 0;
		for( XNATResultSetRow row : result.getRows() ) {
			
			// FIXME: Skipping required fields for now...
			List<SearchField> missingFields = row.getMissingFields();
			if( missingFields.size() > 0 ) {
				LinkedHashSet<SearchField> missingRequired = new LinkedHashSet<SearchField>(requiredFields);
				missingRequired.retainAll(new HashSet<SearchField>(missingFields));
				if( missingRequired.size() > 0 ) {
					++missing;
					StringBuilder b = new StringBuilder("Row ").append(total++).append(" is missing: ");
					for( SearchField f : missingRequired )
						b.append(f.getFieldId()).append(", ");
					b.delete(b.length() - 2, b.length() - 1);
					_log.warn(b);
					continue;
				}
			}
			++total;
			
			int rowIdx = table.addRow();
			colIdx = 0;
			
			for( SearchField field : fields ) {
				try {
					String fieldVal = row.getValue(field);
					
					// don't set missing numeric values
					Class<?> type = typeMap.get(field.getType());
					if( Number.class.isAssignableFrom(type) && fieldVal == null || "".equals(fieldVal.trim()) ) {
						++colIdx;
						continue;
					}
					
					table.set(rowIdx, colIdx++, fieldVal);
				} catch( DataTypeException e ) {
					_log.error("Problem with field: " + field, e);
					throw e;
				}
			}
			// FIXME image data
		}
		
		_log.info(missing + " / " + total + " rows are missing fields.");
		return table;
	}
}
