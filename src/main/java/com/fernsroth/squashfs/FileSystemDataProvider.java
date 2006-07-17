/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.fernsroth.easyio.EasyIORandomAccessFile;
import com.fernsroth.easyio.IRandomAccessSource;
import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Manifest;
import com.fernsroth.squashfs.model.SFSSourceFile;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class FileSystemDataProvider implements DataProvider {

    /**
     * the source directory.
     */
    private File sourceDir;

    /**
     * ino counter.
     */
    private static int ino = 1;

    /**
     * @param sourceDir
     */
    public FileSystemDataProvider(File sourceDir) {
        this.sourceDir = sourceDir;
    }

    /**
     * {@inheritDoc}
     */
    public IRandomAccessSource getData(Manifest source, BaseFile bf)
            throws IOException {
        File f = getFile(source, bf);
        if (f == null) {
            return null;
        }
        return new EasyIORandomAccessFile(f, "r");
    }

    /**
     * gets the file from the source and base file.
     * @param source the source.
     * @param bf the base file.
     * @return the file.
     * @throws FileNotFoundException 
     */
    private File getFile(Manifest source, BaseFile bf)
            throws FileNotFoundException {
        if (bf == null || bf.getName() == null) {
            return null;
        }

        File f = null;
        if (bf instanceof SFSSourceFile
                && ((SFSSourceFile) bf).getSourceFile() != null) {
            f = ((SFSSourceFile) bf).getSourceFile();
        } else {
            String path = source.getPath(bf);
            if (path == null) {
                throw new FileNotFoundException("could not file '"
                        + bf.getName() + "'");
            }
            f = new File(this.sourceDir, path);
        }

        if (f == null) {
            throw new FileNotFoundException("could not file '" + bf.getName()
                    + "'");
        }
        return f;
    }

    /**
     * {@inheritDoc}
     */
    public int getIno(Manifest source, BaseFile bf) {
        return ino++;
    }

    /**
     * {@inheritDoc}
     */
    public long getLength(Manifest source, BaseFile bf) throws IOException {
        File f = getFile(source, bf);
        if (f == null) {
            return 0;
        }
        return f.length();
    }
}
