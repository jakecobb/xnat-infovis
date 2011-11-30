package edu.gatech.cs7450.xnat;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.List;

/**
 * Constants for xNAT, mostly organized under the nested classes.
 */
public final class XNATConstants {	
	/** 
	 * Keys, search fields, etc for projects.
	 * <p><strong>Do not mutate the <code>SearchField</code> constants.</strong></p>
	 */		
	public static final class Projects {
		/** The xNAT schema element name. */
		public static final String ELEMENT = "xnat:projectData";
		
		/** Columns returned by a non-customized project REST request. */
		public static final List<String> COLUMNS = unmodifiableList(asList(
			"ID", "secondary_ID", "name", "description", "pi_firstname", "pi_lastname", "URI"));
		
		// available fields, generated from /search/elements/xnat:projectData response
		public static final SearchField INSERT_DATE = new SearchField("xnat:projectData", "INSERT_DATE", "string", "Inserted", "Inserted", "xnat:projectData/meta/insert_date", false, 0);
		public static final SearchField INSERT_USER = new SearchField("xnat:projectData", "INSERT_USER", "string", "Creator", "Creator", "xnat:projectData/meta/insert_user/login", false, 0);
		public static final SearchField ID = new SearchField("xnat:projectData", "ID", "string", "ID", "ID", "xnat:projectData/ID", false, 0);
		public static final SearchField NAME = new SearchField("xnat:projectData", "NAME", "string", "Title", "Title", "xnat:projectData/name", false, 0);
		public static final SearchField NAME_CSV = new SearchField("xnat:projectData", "NAME_CSV", "string", "Title", "Title", "xnat:projectData/name", false, 0);
		public static final SearchField DESCRIPTION = new SearchField("xnat:projectData", "DESCRIPTION", "string", "Description", "Description", "substring(xnat:projectData/description FROM 0 FOR 162)", false, 0);
		public static final SearchField DESCRIPTION_CSV = new SearchField("xnat:projectData", "DESCRIPTION_CSV", "string", "Description", "Description", "xnat:projectData/description", false, 0);
		public static final SearchField SECONDARY_ID = new SearchField("xnat:projectData", "SECONDARY_ID", "string", "Running Title", "Running Title", "xnat:projectData/secondary_ID", false, 0);
		public static final SearchField KEYWORDS = new SearchField("xnat:projectData", "KEYWORDS", "string", "Keywords", "Keywords", "xnat:projectData/keywords", false, 0);
		public static final SearchField PI = new SearchField("xnat:projectData", "PI", "string", "PI", "PI", "xnat:projectData/PI/firstname || ' ' || xnat:projectData/PI/lastname", false, 0);
		public static final SearchField PROJECT_INVS = new SearchField("xnat:projectData", "PROJECT_INVS", "string", "Investigators", "Investigators", "PROJECT_INVS.PROJ_INVS", false, 0);
		public static final SearchField PROJECT_ACCESS = new SearchField("xnat:projectData", "PROJECT_ACCESS", "string", "Accessibility", "Accessibility", "PROJECT_ACCESS.accessibility", false, 0);
		public static final SearchField PROJECT_USERS = new SearchField("xnat:projectData", "PROJECT_USERS", "string", "Users", "Users", "PROJECT_USERS.users", false, 0);
		public static final SearchField PROJECT_OWNERS = new SearchField("xnat:projectData", "PROJECT_OWNERS", "string", "Owners", "Owners", "PROJECT_OWNERS.users", false, 0);
		public static final SearchField PROJECT_MEMBERS = new SearchField("xnat:projectData", "PROJECT_MEMBERS", "string", "Members", "Members", "PROJECT_MEMBERS.users", false, 0);
		public static final SearchField PROJECT_COLLABS = new SearchField("xnat:projectData", "PROJECT_COLLABS", "string", "Collaborators", "Collaborators", "PROJECT_COLLABS.users", false, 0);
		public static final SearchField PROJECT_LAST_WORKFLOW = new SearchField("xnat:projectData", "PROJECT_LAST_WORKFLOW", "string", "Last Workflow", "Last Workflow", "PROJECT_LAST_WORKFLOW.LAST_WORKFLOW", false, 0);
		public static final SearchField PROJECT_LAST_ACCESS = new SearchField("xnat:projectData", "PROJECT_LAST_ACCESS", "string", "Last Access", "Last Access", "PROJECT_LAST_ACCESS.users", false, 0);
		public static final SearchField PROJECT_FAV = new SearchField("xnat:projectData", "PROJECT_FAV", "string", "Favorites", "Favorites", "PROJECT_FAVS.users", false, 0);
		public static final SearchField PROJ_MR_COUNT = new SearchField("xnat:projectData", "PROJ_MR_COUNT", "integer", "MR Count", "MR Count", "PROJ_MR_COUNTS.PROJ_EXPT_COUNT", false, 0);
		public static final SearchField PROJ_CT_COUNT = new SearchField("xnat:projectData", "PROJ_CT_COUNT", "integer", "CT Count", "CT Count", "PROJ_CT_COUNTS.PROJ_EXPT_COUNT", false, 0);
		public static final SearchField PROJ_PET_COUNT = new SearchField("xnat:projectData", "PROJ_PET_COUNT", "integer", "PET Count", "PET Count", "PROJ_PET_COUNTS.PROJ_EXPT_COUNT", false, 0);
		public static final SearchField PROJ_UT_COUNT = new SearchField("xnat:projectData", "PROJ_UT_COUNT", "integer", "UT Count", "UT Count", "PROJ_UT_COUNTS.PROJ_EXPT_COUNT", false, 0);
	}
	
	/** 
	 * Keys, search fields, etc for subjects.
	 * <p><strong>Do not mutate the <code>SearchField</code> constants.</strong></p>
	 */	
	public static final class Subjects {
		/** The xNAT schema element name. */
		public static final String ELEMENT = "xnat:subjectData";
		
		/** Columns returned by a non-customized subjects REST request. */
		public static final List<String> COLUMNS = unmodifiableList(asList(
			"ID","project","label","insert_date","insert_user", "URI"));
		
		// present but not reflected
		public static final SearchField LABEL = 
			new SearchField("xnat:subjectData", "LABEL", "string", "SUBJECT ID", "Subject Label", "xnat:subjectData/LABEL", false, 0);
		
		// available fields, generated from /search/elements/xnat:subjectData
		public static final SearchField INSERT_DATE = new SearchField("xnat:subjectData", "INSERT_DATE", "date", "Inserted", "Inserted", "xnat:subjectData/meta/insert_date", false, 0);
		public static final SearchField INSERT_USER = new SearchField("xnat:subjectData", "INSERT_USER", "string", "Creator", "Creator", "xnat:subjectData/meta/insert_user/login", false, 0);
		public static final SearchField GENDER_TEXT = new SearchField("xnat:subjectData", "GENDER_TEXT", "string", "M/F", "M/F", "CASE xnat:demographicData/gender WHEN 'male' THEN 'M' WHEN 'female' THEN 'F' ELSE 'U' END", false, 0);
		public static final SearchField HANDEDNESS_TEXT = new SearchField("xnat:subjectData", "HANDEDNESS_TEXT", "string", "Hand", "Hand", "CASE xnat:demographicData/handedness WHEN 'left' THEN 'L' WHEN 'right' THEN 'R' WHEN 'ambidextrous' THEN 'A' ELSE 'U' END", false, 0);
		public static final SearchField DOB = new SearchField("xnat:subjectData", "DOB", "UNKNOWN", "DOB", "DOB", "CAST(FLOOR(COALESCE(xnat:demographicData/yob,EXTRACT(YEAR FROM xnat:demographicData/dob))) AS VARCHAR)", false, 0);
		public static final SearchField EDUC = new SearchField("xnat:subjectData", "EDUC", "integer", "Education", "Education", "xnat:demographicData/education", false, 0);
		public static final SearchField SES = new SearchField("xnat:subjectData", "SES", "integer", "Ses", "Ses", "xnat:demographicData/ses", false, 0);
		public static final SearchField INVEST_CSV = new SearchField("xnat:subjectData", "INVEST_CSV", "string", "PI", "PI", "xnat:subjectData/investigator/lastname", false, 0);
		public static final SearchField PROJECTS = new SearchField("xnat:subjectData", "PROJECTS", "string", "Projects", "All projects tied to a subject", "SUB_PROJECTS.PROJECTS", false, 0);
		public static final SearchField PROJECT = new SearchField("xnat:subjectData", "PROJECT", "string", "Project", "Subject's primary project", "xnat:subjectData/project", false, 0);
		public static final SearchField SUB_GROUP = new SearchField("xnat:subjectData", "SUB_GROUP", "string", "Group", "Group", "xnat:subjectData/group", false, 0);
		public static final SearchField ADD_IDS = new SearchField("xnat:subjectData", "ADD_IDS", "string", "Labels", "Labels", "SUBJECT_IDS.ADDIDS", false, 0);
		public static final SearchField RACE = new SearchField("xnat:subjectData", "RACE", "string", "Race", "Race", "xnat:demographicData/race", false, 0);
		public static final SearchField ETHNICITY = new SearchField("xnat:subjectData", "ETHNICITY", "string", "Ethnicity", "Ethnicity", "xnat:demographicData/ethnicity", false, 0);
		public static final SearchField XNAT_COL_MRSESSIONDATALABEL = new SearchField("xnat:subjectData", "XNAT_COL_MRSESSIONDATALABEL", "string", "LABEL", "LABEL", "xnat:mrSessionData/LABEL", false, 0);
		public static final SearchField XNAT_COL_SUBJECTDATALABEL = new SearchField("xnat:subjectData", "XNAT_COL_SUBJECTDATALABEL", "string", "LABEL", "LABEL", "xnat:subjectData/LABEL", false, 0);
		public static final SearchField XNAT_COL_CRSESSIONDATALABEL = new SearchField("xnat:subjectData", "XNAT_COL_CRSESSIONDATALABEL", "string", "LABEL", "LABEL", "xnat:crSessionData/LABEL", false, 0);
		public static final SearchField XNAT_COL_MRSESSIONDATAID = new SearchField("xnat:subjectData", "XNAT_COL_MRSESSIONDATAID", "string", "ID", "ID", "xnat:mrSessionData/ID", false, 0);
		public static final SearchField XNAT_COL_PROJECTDATAID = new SearchField("xnat:subjectData", "XNAT_COL_PROJECTDATAID", "string", "ID", "ID", "xnat:projectData/ID", false, 0);
		public static final SearchField XNAT_COL_MRSCANDATAID = new SearchField("xnat:subjectData", "XNAT_COL_MRSCANDATAID", "string", "ID", "ID", "xnat:mrScanData/ID", false, 0);
		
// think these were custom?
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=affy_data = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=affy_data", "boolean", "AFFY_DATA", "Custom Field: AFFY_DATA", "Custom Field: AFFY_DATA", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=tcga_accepted = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=tcga_accepted", "string", "TCGA_ACCEPTED", "Custom Field: TCGA_ACCEPTED", "Custom Field: TCGA_ACCEPTED", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=tcga_short_id = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=tcga_short_id", "string", "TCGA_SHORT_ID", "Custom Field: TCGA_SHORT_ID", "Custom Field: TCGA_SHORT_ID", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=age_at_initial_path_diagnosis = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=age_at_initial_path_diagnosis", "integer", "age_at_initial_path_diagnosis", "Custom Field: age_at_initial_path_diagnosis", "Custom Field: age_at_initial_path_diagnosis", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=bcr_surgery_barcode = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=bcr_surgery_barcode", "string", "bcr_surgery_barcode", "Custom Field: bcr_surgery_barcode", "Custom Field: bcr_surgery_barcode", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=chemo_therapy = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=chemo_therapy", "string", "chemo_therapy", "Custom Field: chemo_therapy", "Custom Field: chemo_therapy", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=days_to_death = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=days_to_death", "integer", "days_to_death", "Custom Field: days_to_death", "Custom Field: days_to_death", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=days_to_procedure = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=days_to_procedure", "integer", "days_to_procedure", "Custom Field: days_to_procedure", "Custom Field: days_to_procedure", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=days_to_tumor_progression = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=days_to_tumor_progression", "integer", "days_to_tumor_progression", "Custom Field: days_to_tumor_progression", "Custom Field: days_to_tumor_progression", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=days_to_tumor_recurrence = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=days_to_tumor_recurrence", "integer", "days_to_tumor_recurrence", "Custom Field: days_to_tumor_recurrence", "Custom Field: days_to_tumor_recurrence", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=dig_path_slides_available = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=dig_path_slides_available", "integer", "DIG_PATH_SLIDES_AVAILABLE", "Custom Field: DIG_PATH_SLIDES_AVAILABLE", "Custom Field: DIG_PATH_SLIDES_AVAILABLE", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=extend_subtype_affy = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=extend_subtype_affy", "string", "Extend_Subtype_Affy", "Custom Field: Extend_Subtype_Affy", "Custom Field: Extend_Subtype_Affy", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=molec_subtype = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=molec_subtype", "string", "MOLEC_SUBTYPE", "Custom Field: MOLEC_SUBTYPE", "Custom Field: MOLEC_SUBTYPE", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=path_manual_scored = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=path_manual_scored", "boolean", "PATH_MANUAL_SCORED", "Custom Field: PATH_MANUAL_SCORED", "Custom Field: PATH_MANUAL_SCORED", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=percenttumornuclei = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=percenttumornuclei", "float", "percentTumorNuclei", "Custom Field: percentTumorNuclei", "Custom Field: percentTumorNuclei", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=pretreatment_history = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=pretreatment_history", "boolean", "pretreatment_history", "Custom Field: pretreatment_history", "Custom Field: pretreatment_history", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=prior_glioma = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=prior_glioma", "boolean", "prior_glioma", "Custom Field: prior_glioma", "Custom Field: prior_glioma", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=radiation_therapy = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=radiation_therapy", "boolean", "radiation_therapy", "Custom Field: radiation_therapy", "Custom Field: radiation_therapy", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=secondaryorrecurrent = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=secondaryorrecurrent", "string", "SecondaryOrRecurrent", "Custom Field: SecondaryOrRecurrent", "Custom Field: SecondaryOrRecurrent", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=sil_width = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=sil_width", "float", "sil_width", "Custom Field: sil_width", "Custom Field: sil_width", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=survivaldays = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=survivaldays", "integer", "SurvivalDays", "Custom Field: SurvivalDays", "Custom Field: SurvivalDays", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=targeted_molecular_therapy = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=targeted_molecular_therapy", "boolean", "targeted_molecular_therapy", "Custom Field: targeted_molecular_therapy", "Custom Field: targeted_molecular_therapy", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=vasari_rater_status = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=vasari_rater_status", "integer", "VASARI_RATER_STATUS", "Custom Field: VASARI_RATER_STATUS", "Custom Field: VASARI_RATER_STATUS", false, 1);
//		public static final SearchField XNAT_SUBJECTDATA_FIELD_MAP=vitalstatus = new SearchField("xnat:subjectData", "XNAT_SUBJECTDATA_FIELD_MAP=vitalstatus", "string", "VITALSTATUS", "Custom Field: VITALSTATUS", "Custom Field: VITALSTATUS", false, 1);
//		public static final SearchField SUB_PROJECT_IDENTIFIER=HF_BRN_TUMOR = new SearchField("xnat:subjectData", "SUB_PROJECT_IDENTIFIER=HF_BRN_TUMOR", "string", "HF_BRN_TUMOR", "Label within the HF_BRN_TUMOR project.", "Label within the HF_BRN_TUMOR project.", false, 2);
//		public static final SearchField SUB_PROJECT_IDENTIFIER=NBIA_TCGA = new SearchField("xnat:subjectData", "SUB_PROJECT_IDENTIFIER=NBIA_TCGA", "string", "NBIA_TCGA", "Label within the NBIA_TCGA project.", "Label within the NBIA_TCGA project.", false, 2);

	}
	
	/** 
	 * Keys, search fields, etc for sessions (currently MR session only).
	 * <p><strong>Do not mutate the <code>SearchField</code> constants.</strong></p>
	 */
	public static final class Sessions {
		/** The xNAT schema element name. */
		public static final String ELEMENT = "xnat:mrSessionData";
		
		/** Columns returned by a non-customized experiments REST request. */
		public static final List<String> COLUMNS = unmodifiableList(asList(
			"ID","project","date","xsiType","label","insert_date","URI"));
		
		// this one is present but not reflected
		public static final SearchField LABEL = 
			new SearchField("xnat:mrSessionData", "LABEL", "string", "MR ID", "MR Session Label", "xnat:mrSessionData/LABEL", false, 0);
		
		// available fields, generated from the /search/elements/xnat:mrSessionData response
		public static final SearchField SESSION_ID = new SearchField("xnat:mrSessionData", "SESSION_ID", "string", "Session", "MR Session Accession Number", "xnat:mrSessionData/ID", false, 0);
		public static final SearchField SUBJECT_ID = new SearchField("xnat:mrSessionData", "SUBJECT_ID", "string", "Subject", "Subject Accession Number", "xnat:mrSessionData/subject_ID", false, 0);
		public static final SearchField DATE = new SearchField("xnat:mrSessionData", "DATE", "date", "Date", "MR Session Date (DATE)", "xnat:mrSessionData/date", false, 0);
		public static final SearchField INVEST_SEARCH = new SearchField("xnat:mrSessionData", "INVEST_SEARCH", "string", "PI", "Primary Investigator (first and last name)", "xnat:mrSessionData/investigator/firstname || ' ' || xnat:mrSessionData/investigator/lastname", false, 0);
		public static final SearchField INVEST = new SearchField("xnat:mrSessionData", "INVEST", "string", "PI", "Primary Investigator (last name)", "xnat:mrSessionData/investigator/lastname", false, 0);
		public static final SearchField TYPE = new SearchField("xnat:mrSessionData", "TYPE", "string", "Type", "Type", "xnat:mrSessionData/session_type", false, 0);
		public static final SearchField OPERATOR_CSV = new SearchField("xnat:mrSessionData", "OPERATOR_CSV", "string", "Operator", "Operator", "xnat:mrSessionData/operator", false, 0);
		public static final SearchField SCANNER_CSV = new SearchField("xnat:mrSessionData", "SCANNER_CSV", "string", "Scanner", "Scanner", "xnat:mrSessionData/scanner", false, 0);
		public static final SearchField MARKER_CSV = new SearchField("xnat:mrSessionData", "MARKER_CSV", "string", "Marker", "Marker", "xnat:mrSessionData/marker", false, 0);
		public static final SearchField STABILIZATION_CSV = new SearchField("xnat:mrSessionData", "STABILIZATION_CSV", "string", "Stabilization", "Stabilization", "xnat:mrSessionData/stabilization", false, 0);
		public static final SearchField GEN_AGE = new SearchField("xnat:mrSessionData", "GEN_AGE", "float", "Age", "Age", "CAST(ROUND(CAST(COALESCE(xnat:mrSessionData/age,EXTRACT(YEAR FROM AGE(xnat:mrSessionData/date, xnat:demographicData/dob)),(EXTRACT(YEAR FROM xnat:mrSessionData/date)) - (xnat:demographicData/yob)) AS numeric),2) AS numeric)", false, 0);
		public static final SearchField AGE = new SearchField("xnat:mrSessionData", "AGE", "float", "Age", "Age", "CAST(FLOOR(CAST(COALESCE(xnat:mrSessionData/age,xnat:demographicData/age,EXTRACT(YEAR FROM AGE(xnat:mrSessionData/date, xnat:demographicData/dob)),(EXTRACT(YEAR FROM xnat:mrSessionData/date)) - (xnat:demographicData/yob)) AS numeric)) AS numeric)", false, 0);
		public static final SearchField DTI_COUNT = new SearchField("xnat:mrSessionData", "DTI_COUNT", "integer", "DTI Count", "DTI Count", "DTI_COUNT.DTI_COUNT", false, 0);
		public static final SearchField INSERT_DATE = new SearchField("xnat:mrSessionData", "INSERT_DATE", "date", "Inserted", "Date session was archived", "xnat:mrSessionData/meta/insert_date", false, 0);
		public static final SearchField INSERT_USER = new SearchField("xnat:mrSessionData", "INSERT_USER", "string", "Creator", "User who archived session", "xnat:mrSessionData/meta/insert_user/login", false, 0);
		public static final SearchField LAST_MODIFIED = new SearchField("xnat:mrSessionData", "LAST_MODIFIED", "date", "Modified", "Modified", "COALESCE(xnat:mrSessionData/meta/last_modified, xnat:mrSessionData/meta/insert_date)", false, 0);
		public static final SearchField MR_SCAN_COUNT_AGG = new SearchField("xnat:mrSessionData", "MR_SCAN_COUNT_AGG", "string", "Scans", "Aggregated Scan Counts", "MR_SCAN_COUNT_AGG.TYPE_COUNT", false, 0);
		public static final SearchField MPRAGE_COUNT = new SearchField("xnat:mrSessionData", "MPRAGE_COUNT", "integer", "MPRAGE", "Count of usable MPRAGE scans for this session.", "MPRAGE_COUNT.MPRAGE_COUNT", false, 0);
		public static final SearchField PROJECT = new SearchField("xnat:mrSessionData", "PROJECT", "string", "Project", "Primary Project for this session", "xnat:mrSessionData/project", false, 0);
		public static final SearchField XNAT_COL_MRSESSIONDATASHARINGSHAREPROJECT = new SearchField("xnat:mrSessionData", "XNAT_COL_MRSESSIONDATASHARINGSHAREPROJECT", "string", "project", "project", "xnat:mrSessionData/sharing/share/project", false, 0);
//		public static final SearchField XNAT_MRSESSIONDATA_FIELD_MAP=clean_tag_id = new SearchField("xnat:mrSessionData", "XNAT_MRSESSIONDATA_FIELD_MAP=clean_tag_id", "string", "CLEAN_TAG_ID", "Custom Field: CLEAN_TAG_ID", "Custom Field: CLEAN_TAG_ID", false, 1);
//		public static final SearchField XNAT_MRSESSIONDATA_FIELD_MAP=tarun_annotated = new SearchField("xnat:mrSessionData", "XNAT_MRSESSIONDATA_FIELD_MAP=tarun_annotated", "boolean", "TARUN_ANNOTATED", "Custom Field: TARUN_ANNOTATED", "Custom Field: TARUN_ANNOTATED", false, 1);
//		public static final SearchField MR_PROJECT_IDENTIFIER=HF_BRN_TUMOR = new SearchField("xnat:mrSessionData", "MR_PROJECT_IDENTIFIER=HF_BRN_TUMOR", "string", "HF_BRN_TUMOR", "Label within the HF_BRN_TUMOR project.", "Label within the HF_BRN_TUMOR project.", false, 2);
//		public static final SearchField MR_PROJECT_IDENTIFIER=NBIA_TCGA = new SearchField("xnat:mrSessionData", "MR_PROJECT_IDENTIFIER=NBIA_TCGA", "string", "NBIA_TCGA", "Label within the NBIA_TCGA project.", "Label within the NBIA_TCGA project.", false, 2);
	}
	
	/**
	 * Keys, search fields, etc for scans (currently MR scans only).
	 * <p><strong>Do not mutate the <code>SearchField</code> constants.</strong></p>
	 */
	public static final class Scans {
		/** The xNAT schema element. */
		public static final String ELEMENT = "xnat:mrScanData";
		
		// present but not reflected
		public static final SearchField LABEL = 
			new SearchField("xnat:mrScanData", "LABEL", "string", "SCAN ID", "Scan Label", "xnat:mrScanData/LABEL", false, 0);
		
		// available fields, these were generated from the /search/elements/xnat:mrScanData response 
      public static final SearchField IMAGE_SESSION_ID = new SearchField("xnat:mrScanData", "IMAGE_SESSION_ID", "string", "image_session_ID", "image_session_ID", "xnat:mrScanData/image_session_ID", false, 0);
      public static final SearchField NOTE = new SearchField("xnat:mrScanData", "NOTE", "string", "note", "note", "xnat:mrScanData/note", false, 0);
      public static final SearchField QUALITY = new SearchField("xnat:mrScanData", "QUALITY", "string", "quality", "quality", "xnat:mrScanData/quality", false, 0);
      public static final SearchField CONDITION = new SearchField("xnat:mrScanData", "CONDITION", "string", "condition", "condition", "xnat:mrScanData/condition", false, 0);
      public static final SearchField SERIES_DESCRIPTION = new SearchField("xnat:mrScanData", "SERIES_DESCRIPTION", "string", "series_description", "series_description", "xnat:mrScanData/series_description", false, 0);
      public static final SearchField DOCUMENTATION = new SearchField("xnat:mrScanData", "DOCUMENTATION", "string", "documentation", "documentation", "xnat:mrScanData/documentation", false, 0);
      public static final SearchField SCANNER = new SearchField("xnat:mrScanData", "SCANNER", "string", "scanner", "scanner", "xnat:mrScanData/scanner", false, 0);
      public static final SearchField SCANNER_MANUFACTURER = new SearchField("xnat:mrScanData", "SCANNER_MANUFACTURER", "string", "manufacturer", "manufacturer", "xnat:mrScanData/scanner/manufacturer", false, 0);
      public static final SearchField SCANNER_MODEL = new SearchField("xnat:mrScanData", "SCANNER_MODEL", "string", "model", "model", "xnat:mrScanData/scanner/model", false, 0);
      public static final SearchField MODALITY = new SearchField("xnat:mrScanData", "MODALITY", "string", "modality", "modality", "xnat:mrScanData/modality", false, 0);
      public static final SearchField FRAMES = new SearchField("xnat:mrScanData", "FRAMES", "integer", "frames", "frames", "xnat:mrScanData/frames", false, 0);
      public static final SearchField OPERATOR = new SearchField("xnat:mrScanData", "OPERATOR", "string", "operator", "operator", "xnat:mrScanData/operator", false, 0);
      public static final SearchField ID = new SearchField("xnat:mrScanData", "ID", "string", "ID", "ID", "xnat:mrScanData/ID", false, 0);
      public static final SearchField TYPE = new SearchField("xnat:mrScanData", "TYPE", "string", "type", "type", "xnat:mrScanData/type", false, 0);
      public static final SearchField UID = new SearchField("xnat:mrScanData", "UID", "string", "UID", "UID", "xnat:mrScanData/UID", false, 0);
      public static final SearchField PROJECT = new SearchField("xnat:mrScanData", "PROJECT", "string", "project", "project", "xnat:mrScanData/project", false, 0);
      public static final SearchField INSERT_DATE = new SearchField("xnat:mrScanData", "INSERT_DATE", "string", "Inserted", "Inserted", "xnat:mrScanData/meta/insert_date", false, 0);
      public static final SearchField INSERT_USER = new SearchField("xnat:mrScanData", "INSERT_USER", "string", "Creator", "Creator", "xnat:mrScanData/meta/insert_user/login", false, 0);
      public static final SearchField COIL = new SearchField("xnat:mrScanData", "COIL", "string", "coil", "coil", "xnat:mrScanData/coil", false, 0);
      public static final SearchField FIELDSTRENGTH = new SearchField("xnat:mrScanData", "FIELDSTRENGTH", "string", "fieldStrength", "fieldStrength", "xnat:mrScanData/fieldStrength", false, 0);
      public static final SearchField MARKER = new SearchField("xnat:mrScanData", "MARKER", "string", "marker", "marker", "xnat:mrScanData/marker", false, 0);
      public static final SearchField STABILIZATION = new SearchField("xnat:mrScanData", "STABILIZATION", "string", "stabilization", "stabilization", "xnat:mrScanData/stabilization", false, 0);
      public static final SearchField PARAMETERS_VOXELRES_UNITS = new SearchField("xnat:mrScanData", "PARAMETERS_VOXELRES_UNITS", "string", "units", "units", "xnat:mrScanData/parameters/voxelRes/units", false, 0);
      public static final SearchField PARAMETERS_VOXELRES_X = new SearchField("xnat:mrScanData", "PARAMETERS_VOXELRES_X", "float", "x", "x", "xnat:mrScanData/parameters/voxelRes/x", false, 0);
      public static final SearchField PARAMETERS_VOXELRES_Y = new SearchField("xnat:mrScanData", "PARAMETERS_VOXELRES_Y", "float", "y", "y", "xnat:mrScanData/parameters/voxelRes/y", false, 0);
      public static final SearchField PARAMETERS_VOXELRES_Z = new SearchField("xnat:mrScanData", "PARAMETERS_VOXELRES_Z", "float", "z", "z", "xnat:mrScanData/parameters/voxelRes/z", false, 0);
      public static final SearchField PARAMETERS_ORIENTATION = new SearchField("xnat:mrScanData", "PARAMETERS_ORIENTATION", "string", "orientation", "orientation", "xnat:mrScanData/parameters/orientation", false, 0);
      public static final SearchField PARAMETERS_FOV_X = new SearchField("xnat:mrScanData", "PARAMETERS_FOV_X", "integer", "x", "x", "xnat:mrScanData/parameters/fov/x", false, 0);
      public static final SearchField PARAMETERS_FOV_Y = new SearchField("xnat:mrScanData", "PARAMETERS_FOV_Y", "integer", "y", "y", "xnat:mrScanData/parameters/fov/y", false, 0);
      public static final SearchField PARAMETERS_MATRIX_X = new SearchField("xnat:mrScanData", "PARAMETERS_MATRIX_X", "integer", "x", "x", "xnat:mrScanData/parameters/matrix/x", false, 0);
      public static final SearchField PARAMETERS_MATRIX_Y = new SearchField("xnat:mrScanData", "PARAMETERS_MATRIX_Y", "integer", "y", "y", "xnat:mrScanData/parameters/matrix/y", false, 0);
      public static final SearchField PARAMETERS_PARTITIONS = new SearchField("xnat:mrScanData", "PARAMETERS_PARTITIONS", "integer", "partitions", "partitions", "xnat:mrScanData/parameters/partitions", false, 0);
      public static final SearchField PARAMETERS_TR = new SearchField("xnat:mrScanData", "PARAMETERS_TR", "float", "tr", "tr", "xnat:mrScanData/parameters/tr", false, 0);
      public static final SearchField PARAMETERS_TE = new SearchField("xnat:mrScanData", "PARAMETERS_TE", "float", "te", "te", "xnat:mrScanData/parameters/te", false, 0);
      public static final SearchField PARAMETERS_TI = new SearchField("xnat:mrScanData", "PARAMETERS_TI", "float", "ti", "ti", "xnat:mrScanData/parameters/ti", false, 0);
      public static final SearchField PARAMETERS_FLIP = new SearchField("xnat:mrScanData", "PARAMETERS_FLIP", "integer", "flip", "flip", "xnat:mrScanData/parameters/flip", false, 0);
      public static final SearchField PARAMETERS_SEQUENCE = new SearchField("xnat:mrScanData", "PARAMETERS_SEQUENCE", "string", "sequence", "sequence", "xnat:mrScanData/parameters/sequence", false, 0);
      public static final SearchField PARAMETERS_ORIGIN = new SearchField("xnat:mrScanData", "PARAMETERS_ORIGIN", "string", "origin", "origin", "xnat:mrScanData/parameters/origin", false, 0);
      public static final SearchField PARAMETERS_SCANTIME = new SearchField("xnat:mrScanData", "PARAMETERS_SCANTIME", "time", "scanTime", "scanTime", "xnat:mrScanData/parameters/scanTime", false, 0);
      public static final SearchField PARAMETERS_IMAGETYPE = new SearchField("xnat:mrScanData", "PARAMETERS_IMAGETYPE", "string", "imageType", "imageType", "xnat:mrScanData/parameters/imageType", false, 0);
      public static final SearchField PARAMETERS_SCANSEQUENCE = new SearchField("xnat:mrScanData", "PARAMETERS_SCANSEQUENCE", "string", "scanSequence", "scanSequence", "xnat:mrScanData/parameters/scanSequence", false, 0);
      public static final SearchField PARAMETERS_SEQVARIANT = new SearchField("xnat:mrScanData", "PARAMETERS_SEQVARIANT", "string", "seqVariant", "seqVariant", "xnat:mrScanData/parameters/seqVariant", false, 0);
      public static final SearchField PARAMETERS_SCANOPTIONS = new SearchField("xnat:mrScanData", "PARAMETERS_SCANOPTIONS", "string", "scanOptions", "scanOptions", "xnat:mrScanData/parameters/scanOptions", false, 0);
      public static final SearchField PARAMETERS_ACQTYPE = new SearchField("xnat:mrScanData", "PARAMETERS_ACQTYPE", "string", "acqType", "acqType", "xnat:mrScanData/parameters/acqType", false, 0);
      public static final SearchField PARAMETERS_COIL = new SearchField("xnat:mrScanData", "PARAMETERS_COIL", "string", "coil", "coil", "xnat:mrScanData/parameters/coil", false, 0);
      public static final SearchField PARAMETERS_DTIACQCOUNT = new SearchField("xnat:mrScanData", "PARAMETERS_DTIACQCOUNT", "integer", "dtiAcqCount", "dtiAcqCount", "xnat:mrScanData/parameters/dtiAcqCount", false, 0);
      public static final SearchField DCMVALIDATION = new SearchField("xnat:mrScanData", "DCMVALIDATION", "string", "dcmValidation", "dcmValidation", "xnat:mrScanData/dcmValidation", false, 0);
      public static final SearchField DCMVALIDATION_STATUS = new SearchField("xnat:mrScanData", "DCMVALIDATION_STATUS", "boolean", "status", "status", "xnat:mrScanData/dcmValidation/status", false, 0);		
	}	
}
