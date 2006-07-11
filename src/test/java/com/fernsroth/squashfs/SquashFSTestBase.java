/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.fernsroth.easyio.IRandomAccessSource;
import com.fernsroth.easyio.RandomAccessByteArray;

import junit.framework.TestCase;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public abstract class SquashFSTestBase extends TestCase {
	/**
	 * @param resourceName
	 * @return the resource.
	 * @throws IOException 
	 */
	protected IRandomAccessSource loadResource(String resourceName)
			throws IOException {
		InputStream in = getClass().getResourceAsStream(resourceName);
		if (in == null) {
			throw new FileNotFoundException("resource '" + resourceName
					+ "' not found");
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b;
		while ((b = in.read()) != -1) {
			baos.write(b);
		}
		return new RandomAccessByteArray(baos.toByteArray());
	}
}
