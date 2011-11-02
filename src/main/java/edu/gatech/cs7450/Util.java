package edu.gatech.cs7450;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

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
}
