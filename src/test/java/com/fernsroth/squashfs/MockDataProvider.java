/**
 * 
 */
package com.fernsroth.squashfs;

import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Manifest;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public abstract class MockDataProvider implements DataProvider {

	/**
	 * 
	 */
	private static int ino = 0;

	/**
	 * {@inheritDoc}
	 */
	public int getIno(Manifest source, BaseFile bf) {
		return ino++;
	}

}
