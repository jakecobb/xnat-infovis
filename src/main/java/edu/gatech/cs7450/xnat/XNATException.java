package edu.gatech.cs7450.xnat;

/**
 * Generic runtime exception for interactions with xNAT.
 */
public class XNATException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public XNATException() { }

	public XNATException(String message) {
		super(message);
	}

	public XNATException(Throwable cause) {
		super(cause);
	}

	public XNATException(String message, Throwable cause) {
		super(message, cause);
	}
}
