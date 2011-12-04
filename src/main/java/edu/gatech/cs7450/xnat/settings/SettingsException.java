package edu.gatech.cs7450.xnat.settings;

/**
 * Runtime exception for persistent settings classes.  Generally wraps an 
 * underlying store exception of some sort.
 */
public class SettingsException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public SettingsException() { }
	public SettingsException(String message, Throwable cause) {
		super(message, cause);
	}
	public SettingsException(String message) {
		super(message);
	}
	public SettingsException(Throwable cause) {
		super(cause);
	}
}