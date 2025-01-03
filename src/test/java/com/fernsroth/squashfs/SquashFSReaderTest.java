/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.fernsroth.easyio.EasyIOFormatter;
import com.fernsroth.easyio.IRandomAccessSource;
import com.fernsroth.easyio.exception.EasyIOException;
import com.fernsroth.squashfs.WalkerMock.VisitData;
import com.fernsroth.squashfs.model.Directory;
import com.fernsroth.squashfs.model.SymLink;
import com.fernsroth.squashfs.model.squashfs.squashfs_constants;
import com.fernsroth.squashfs.model.squashfs.stat;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class SquashFSReaderTest extends SquashFSTestBase {

    /**
     * tests the empty_noI-noD-noF.sfs.
     * @throws IOException 
     * @throws EasyIOException 
     */
    public void testEmpty_noI_noD_noF() throws IOException, EasyIOException {
        IRandomAccessSource in = loadResource("empty_noI-noD-noF.sfs");
        try {
            @SuppressWarnings("unused")
            SquashFSReader reader = new SquashFSReader(in);
        } catch (IOException e) {
            assertEquals("squashfs_opendir: directory block 0 not found!", e
                    .getMessage());
        }
    }

    /**
     * tests the one-file_noI-noD-noF.sfs.
     * @throws Exception 
     */
    public void testOneFile_noI_noD_noF() throws Exception {
        IRandomAccessSource in = loadResource("one-file_noI-noD-noF.sfs");
        SquashFSReader reader = new SquashFSReader(in);
        Directory rootDirectory = reader.getRootDirectory();
        assertNotNull(rootDirectory);
        assertEquals(1, rootDirectory.getSubentries().size());

        WalkerMock testWalker = new WalkerMock(reader);
        SquashFSUtils.walk(rootDirectory, testWalker);
        assertEquals(2, testWalker.visitData.size());

        VisitData data = testWalker.visitData.get(0);
        assertEquals(0, data.path.length);

        Date date = new GregorianCalendar(2006, Calendar.JULY, 7, 23, 23, 46)
                .getTime();
        data = testWalker.visitData.get(1);
        assertEquals(1, data.path.length);
        assertEquals(0, data.file.getUid());
        assertEquals(0, data.file.getGuid());
        assertEquals(SquashFSUtils.getMTimeFromDate(date), data.file.getMTime());
        assertEquals("testfile", data.file.getName());
        assertEquals(stat.S_IRUSR | stat.S_IWUSR | stat.S_IRGRP | stat.S_IROTH,
                data.file.getMode());
        assertNotNull(data.buffer);
        assertEquals(EasyIOFormatter.print(getClass().getResourceAsStream(
                "one-file/testfile")), EasyIOFormatter.print(data.buffer));
    }

    /**
     * tests the multi-test_noI-noD-noF.sfs.
     * @throws Exception 
     */
    public void testMultiTest_noI_noD_noF() throws Exception {
        IRandomAccessSource in = loadResource("multi-test_noI-noD-noF.sfs");
        SquashFSReader reader = new SquashFSReader(in);
        Directory rootDirectory = reader.getRootDirectory();
        assertNotNull(rootDirectory);
        assertEquals(6, rootDirectory.getSubentries().size());

        WalkerMock testWalker = new WalkerMock(reader);
        SquashFSUtils.walk(rootDirectory, testWalker);
        assertEquals(23, testWalker.visitData.size());

        int fileNo = 0;

        VisitData data = testWalker.visitData.get(fileNo++);
        assertEquals(0, data.path.length);

        data = testWalker.visitData.get(fileNo++);
        assertEquals("bigfile", data.file.getName());
        assertEquals(stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR | stat.S_IRGRP
                | stat.S_IXGRP | stat.S_IROTH | stat.S_IXOTH, data.file
                .getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("file1", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("-rw-r--r--")), data.file.getMode());
        assertEquals(EasyIOFormatter.print(getClass().getResourceAsStream(
                "multi-file/tar")), EasyIOFormatter.print(data.buffer));

        data = testWalker.visitData.get(fileNo++);
        assertEquals("dir1", data.file.getName());
        assertEquals(stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR | stat.S_IRGRP
                | stat.S_IXGRP | stat.S_IROTH | stat.S_IXOTH, data.file
                .getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("dir2", data.file.getName());
        assertEquals(stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR | stat.S_IRGRP
                | stat.S_IXGRP | stat.S_IROTH | stat.S_IXOTH, data.file
                .getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("link-file", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("lrwxrwxrwx")), data.file.getMode());
        assertTrue(data.file instanceof SymLink);
        assertEquals("../test-dest", ((SymLink) data.file).getLinkName());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("dir3", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("-rwxr-xr-x")), data.file.getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("dir3-file", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("-rw-r--r--")), data.file.getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("empty_dir", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("-rwxr-xr-x")), data.file.getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("owner", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("-rwxr-xr-x")), data.file.getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("root", data.file.getName());
        assertEquals(0, data.file.getGuid());
        assertEquals(0, data.file.getUid());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("root_user1", data.file.getName());
        assertEquals(1, data.file.getGuid());
        assertEquals(0, data.file.getUid());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("user1", data.file.getName());
        assertEquals(1, data.file.getGuid());
        assertEquals(1, data.file.getUid());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("user1_root", data.file.getName());
        assertEquals(0, data.file.getGuid());
        assertEquals(1, data.file.getUid());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("permissions", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("-rwxr-xr-x")), data.file.getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("f---------", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("----------")), data.file.getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("f------rwx", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("-------rwx")), data.file.getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("f---rwx---", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("----rwx---")), data.file.getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("f--x--x--x", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("---x--x--x")), data.file.getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("f-w--w--w-", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("--w--w--w-")), data.file.getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("fr--r--r--", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("-r--r--r--")), data.file.getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("frwx------", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("-rwx------")), data.file.getMode());

        data = testWalker.visitData.get(fileNo++);
        assertEquals("root-file", data.file.getName());
        assertEquals(squashfs_constants.SQUASHFS_MODE(SquashFSUtils
                .getModeFromString("-rw-r--r--")), data.file.getMode());
    }
}
