package edu.gatech.cs7450;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/** Static utility methods. */
public class Util {
	/**
	 * Reads a resource stream into a string.
	 * 
	 * @param clazz the class to use
	 * @param path  the resource path
	 * @return      the contents as a string
	 * @throws IOException if thrown by the underlying method or the resource is not found
	 */
	public static String classString(Class<?> clazz, String path) throws IOException {
		InputStream in = null;
		try {
			in = clazz.getResourceAsStream(path);
			if( in == null )
				throw new IOException("Resource not found: " + path);
			return slurpStream(in);
		} finally {
			if( in != null ) 
				in.close();
		}
	}
	
	/**
	 * Reads an input stream into a string.  The stream is not closed.
	 * 
	 * @param in the input stream
	 * @return the contents as a string
	 * @throws IOException if thrown by the stream
	 * @throws NullPointerException if <code>in</code> is <code>null</code>
	 */
	public static String slurpStream(InputStream in) throws IOException {
		if( in == null ) throw new NullPointerException("in is null");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int read = -1;
		while( -1 < (read = in.read(buffer)) )
			out.write(buffer, 0, read);
		return new String(out.toByteArray(), Charset.forName("UTF-8"));
	}
	
	/**
	 * Returns the SHA-1 hash of the input string (UTF-8 encoded) or <code>null</code> 
	 * if SHA-1 is not supported.
	 * 
	 * @param input the input string, may be <code>null</code>
	 * @return the hash (or <code>null</code> if unsupported)
	 */
	public static byte[] sha1(String input) {
		if( input == null || input.isEmpty() )
			return new byte[0];
		try {
			Charset utf8 = Charset.forName("UTF-8");
			return MessageDigest.getInstance("SHA-1").digest(input.getBytes(utf8));
		} catch( NoSuchAlgorithmException e ) {
			return null;
		}
	}
	
	/**
	 * Converts bytes into a hexadecimal string (with no prefix).
	 * 
	 * @param bytes the bytes to convert
	 * @return the string or <code>""</code> if <code>bytes</code> is <code>null</code> or empty
	 */
	public static String hex(byte[] bytes) {
		return hex(null, bytes);
	}
	
	/**
	 * Converts bytes into a hexadecimal string with the specified prefix.
	 * 
	 * @param prefix the prefix, may be <code>null</code>
	 * @param bytes  the bytes to convert
	 * @return the string or <code>""</code> if <code>bytes</code> is <code>null</code> or empty
	 */
	public static String hex(String prefix, byte[] bytes) {
		if( bytes == null || bytes.length == 0 )
			return "";
		
		String format = "%02x";
		if( prefix != null )
			format = prefix + format;
		
		Formatter f = new Formatter();
		for( byte b : bytes )
			f.format(format, b);
		return f.toString();
	}

	/**
	 * Try to close <code>c</code>, suppressing any exception (but printing the stack trace) and ignoring <code>null</code>.
	 * 
	 * @param c the thing to close, may be <code>null</code>
	 * @return <code>true</code> if an exception occurred
	 */
	public static boolean tryClose(Closeable c) { return tryClose(c, true); }
	
	/**
	 * Try to close <code>c</code>, suppressing any exception and ignoring <code>null</code>.
	 * 
	 * @param c the thing to close, may be <code>null</code>
	 * @param printStackTrace if the stack trace of an exception should be printed
	 * @return <code>true</code> if an exception occurred
	 */
	public static boolean tryClose(Closeable c, boolean printStackTrace) {
		try {
			if( c != null )
				c.close();
			return true;
		} catch( IOException e ) {
			if( printStackTrace )
				e.printStackTrace();
			return false;
		}
	}
}
