/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Directory;
import com.fernsroth.squashfs.model.SFSSquashedFile;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class WalkerMock implements WalkHandler {

    /**
     * the visit data.
     */
    public List<VisitData> visitData = new ArrayList<VisitData>();

    /**
     * the reader.
     */
    private SquashFSReader reader;

    /**
     * @param reader
     */
    public WalkerMock(SquashFSReader reader) {
        this.reader = reader;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(Directory[] path, BaseFile file) throws Exception {
        byte[] buffer = null;
        if (file instanceof SFSSquashedFile) {
            ByteArrayOutputStream dest = new ByteArrayOutputStream();
            this.reader.writeFile((SFSSquashedFile) file, dest);
            dest.close();
            buffer = dest.toByteArray();
        }
        this.visitData.add(new VisitData(path, file, buffer));
    }

    /**
     *
     */
    public class VisitData {

        /**
         * 
         */
        public Directory[] path;

        /**
         * 
         */
        public BaseFile file;

        /**
         * 
         */
        public byte[] buffer;

        /**
         * @param path
         * @param file
         * @param buffer 
         */
        public VisitData(Directory[] path, BaseFile file, byte[] buffer) {
            this.path = path;
            this.file = file;
            this.buffer = buffer;
        }
    }

}
