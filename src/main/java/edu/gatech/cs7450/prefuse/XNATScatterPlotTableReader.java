package edu.gatech.cs7450.prefuse;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.log4j.Logger;

import prefuse.data.DataTypeException;
import prefuse.data.Table;
import prefuse.data.column.ObjectColumn;
import prefuse.data.parser.DataParseException;
import edu.gatech.cs7450.xnat.SearchField;
import edu.gatech.cs7450.xnat.XNATConstants.Scans;
import edu.gatech.cs7450.xnat.XNATConstants.Sessions;
import edu.gatech.cs7450.xnat.XNATException;
import edu.gatech.cs7450.xnat.XNATResultSet;
import edu.gatech.cs7450.xnat.XNATResultSet.XNATResultSetRow;

public class XNATScatterPlotTableReader {
	private static final Logger _log = Logger.getLogger(XNATScatterPlotTableReader.class);

	public static class NumberTable extends Table {
		private static HashMap<Class<?>, Method> parseMethods = new HashMap<Class<?>, Method>();

		// FIXME this is an ugly hack to allow null values in int/float columns
		@Override
		public void addColumn(String name, Class type, Object defaultValue) {
			if( !checkClass(type) ) {
				super.addColumn(name, type, defaultValue);
			} else {
				int nextRow = m_rows.getMaximumRow() + 1;
				addColumn(name, new ObjectColumn(type, nextRow, nextRow, defaultValue) {
					public void set(Object val, int row) {
						try {
							if( val != null && val.getClass() == String.class )
								val = parse(getColumnType(), val.toString().trim());
						} catch( DataParseException e ) {
							_log.error(e);
						}
						super.set(val, row);
					}
				});
			}
		}

		public static Object parse(Class<?> type, String text) throws DataParseException {
			if( !checkClass(type) )
				throw new DataParseException(type.getName() + " not a number.");
			
			// empty strings are treated as null
			if( text == null || (text = text.trim()).isEmpty() )
				return null;
			
			// try to parse it with valueOf() method
			try {
				Method valueOf = parseMethods.get(type);
				return valueOf.invoke(null, text);
			} catch( Exception e ) {
				_log.error(e.getMessage(), e);
				throw new DataParseException(e);
			}
		}

		public static boolean checkClass(final Class<?> type) {
			if( type == null || !Number.class.isAssignableFrom(type) )
				return false;
			if( parseMethods.containsKey(type) )
				return true;
			
			try {
				Method valueOf = type.getDeclaredMethod("valueOf", String.class);
				if( 0 != (valueOf.getModifiers() & Modifier.STATIC) && type.equals(valueOf.getReturnType()) ) {
					parseMethods.put(type, valueOf);
					return true;
				}
				_log.error("Bad method signature: " + valueOf.toGenericString());
			} catch ( Exception e ) {
				_log.error(e.getMessage(), e);
			}
			
			return false;
		}
	}
	public static Table convertResultSet(XNATResultSet result) {
		if( result == null ) throw new NullPointerException("result is null");
		final boolean _debug = _log.isDebugEnabled();

		// columns from test CSV file:
		// Project,Subject,Experiments,Scan type,Frames Image Type,Field Strength,Vox. Res.,FOV,TR,TE,TI,Flip ,Image
		List<SearchField> fields = Arrays.asList(
			Sessions.PROJECT, 
			Sessions.SUBJECT_ID, 
			Sessions.SESSION_ID,
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
		LinkedHashSet<SearchField> requiredFields = new LinkedHashSet<SearchField>(Arrays.asList(
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
		
		// FIXME make scatterplot use the field ids so this is not necessary
		HashMap<SearchField, String> fieldToCol = new HashMap<SearchField, String>();
		fieldToCol.put(Sessions.PROJECT, "Project");
		fieldToCol.put(Sessions.SUBJECT_ID, "Subject");
		fieldToCol.put(Sessions.SESSION_ID, "Experiments");
		fieldToCol.put(Scans.TYPE, "Scan type");
		fieldToCol.put(Scans.SERIES_DESCRIPTION, "series desc");
		fieldToCol.put(Scans.FRAMES, "Frames Image Type");
		fieldToCol.put(Scans.FIELDSTRENGTH, "Field Strength");
		fieldToCol.put(Scans.QUALITY, "Quality");
		fieldToCol.put(Scans.PARAMETERS_VOXELRES_X, "VRx");
		fieldToCol.put(Scans.PARAMETERS_VOXELRES_Y, "VRy");
		fieldToCol.put(Scans.PARAMETERS_VOXELRES_Z, "VRz");
		fieldToCol.put(Scans.PARAMETERS_FOV_X, "FOVx");
		fieldToCol.put(Scans.PARAMETERS_FOV_Y, "FOVy");
		fieldToCol.put(Scans.PARAMETERS_TE, "TE");
		fieldToCol.put(Scans.PARAMETERS_TR, "TR");
		fieldToCol.put(Scans.PARAMETERS_TI, "TI");
		fieldToCol.put(Scans.PARAMETERS_FLIP, "Flip");
		
		HashMap<String, Class<?>> typeMap = new HashMap<String, Class<?>>(6);
		typeMap.put("string", String.class);
		// object instead of primitive to allow null for missing
//		typeMap.put("integer", Integer.class); 
//		typeMap.put("float", Float.class);
		typeMap.put("integer", int.class);
		typeMap.put("float", float.class);

		
//		Table table = new NumberTable();
		Table table = new Table();

		int colIdx = 0;
		for( SearchField field : fields ) {
			Class<?> type = typeMap.get(field.getType());
			if( type == null )
				throw new XNATException("No mapping for type: " + field.getType());
			// FIXME make scatterplot use the field ids so this is not necessary
			String colName = field.getFieldId();
			if( fieldToCol.containsKey(field) ) {
				colName = fieldToCol.get(field);
				_log.warn("FIXME: Substituting \"" + colName + "\" for field id \"" + field.getFieldId() + "\"");
			}
			
			if( _debug ) _log.debug("Adding column: colName=" + colName + ", type=" + type.getName());
			table.addColumn(colName, type, null);
		}
		table.addColumn("Image", byte[].class, null); // FIXME need the data 
		
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
					Class<?> type = typeMap.get(field.getType());
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
