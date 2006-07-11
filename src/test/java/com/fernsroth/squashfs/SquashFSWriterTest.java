/**
 * 
 */
package com.fernsroth.squashfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fernsroth.easyio.EasyIOFormatter;
import com.fernsroth.easyio.IRandomAccessSource;
import com.fernsroth.easyio.RandomAccessByteArray;
import com.fernsroth.squashfs.WalkerMock.VisitData;
import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Directory;
import com.fernsroth.squashfs.model.Manifest;
import com.fernsroth.squashfs.model.SFSSourceFile;
import com.fernsroth.squashfs.model.squashfs.stat;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class SquashFSWriterTest extends SquashFSTestBase {

	/**
	 * logging.
	 */
	private static Log log = LogFactory.getLog(SquashFSWriterTest.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		SquashFSWriter.setSystem(new SquashFSSystem() {

			public Date getCurrentDate() {
				return new GregorianCalendar(2006, Calendar.JULY, 7, 23, 24, 2)
						.getTime();
			}
		});
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public void testWriteOneFile() throws Exception {
		Manifest manifest = SquashFSManifest.load(getClass()
				.getResourceAsStream("one-file/Manifest.xml"), new File("."));

		DataProvider mockDataProvider = new MockDataProvider() {

			public IRandomAccessSource getData(Manifest source, BaseFile bf)
					throws IOException {
				if (bf instanceof SFSSourceFile) {
					SFSSourceFile sf = (SFSSourceFile) bf;
					return loadResource("one-file/" + sf.getSourceFile());
				}
				throw new IOException("unknown file '" + bf + "'");
			}

			public long getLength(Manifest source, BaseFile bf)
					throws IOException {
				if (bf instanceof SFSSourceFile) {
					SFSSourceFile sf = (SFSSourceFile) bf;
					return loadResource(
							"one-file/" + sf.getSourceFile().getName())
							.length();
				}
				return 0;
			}

		};
		SquashFSWriter writer = new SquashFSWriter(manifest, mockDataProvider);
		byte[] buffer = new byte[10 * 1024];
		RandomAccessByteArray dest = new RandomAccessByteArray(buffer);
		writer.write(dest);

		{
			File f = new File("c:/temp/sfs/one-file.test.sfs");
			FileOutputStream out = new FileOutputStream(f);
			out.write(buffer);
			out.close();
		}

		log.debug("------------- BEGIN READ -------------");

		{
			IRandomAccessSource source = dest;
			SquashFSReader reader = new SquashFSReader(source);
			Directory rootDirectory = reader.getRootDirectory();
			assertNotNull(rootDirectory);
			assertEquals(1, rootDirectory.getSubentries().size());

			WalkerMock testWalker = new WalkerMock(reader);
			SquashFSUtils.walk(rootDirectory, testWalker);
			assertEquals(2, testWalker.visitData.size());

			VisitData data = testWalker.visitData.get(0);
			assertEquals(0, data.path.length);

			Date date = new GregorianCalendar(2006, Calendar.JULY, 7, 23, 23,
					46).getTime();
			data = testWalker.visitData.get(1);
			assertEquals(1, data.path.length);
			assertEquals(0, data.file.getUid());
			assertEquals(0, data.file.getGuid());
			assertEquals(SquashFSUtils.getMTimeFromDate(date), data.file
					.getMTime());
			assertEquals("testfile", data.file.getName());
			assertEquals(stat.S_IRUSR | stat.S_IWUSR | stat.S_IRGRP
					| stat.S_IROTH, data.file.getMode());
			assertNotNull(data.buffer);
			assertEquals(EasyIOFormatter.print(getClass().getResourceAsStream(
					"one-file/testfile")), EasyIOFormatter.print(data.buffer));
		}
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public void testMultiFile() throws Exception {
		Manifest manifest = SquashFSManifest.load(getClass()
				.getResourceAsStream("multi-file/Manifest.xml"), new File("."));

		DataProvider mockDataProvider = new MockDataProvider() {

			public IRandomAccessSource getData(Manifest source, BaseFile bf)
					throws IOException {
				if (bf instanceof SFSSourceFile) {
					SFSSourceFile sf = (SFSSourceFile) bf;
					return loadResource("multi-file/"
							+ sf.getSourceFile().getName());
				}
				throw new IOException("unknown file '" + bf + "'");
			}

			public long getLength(Manifest source, BaseFile bf)
					throws IOException {
				if (bf instanceof SFSSourceFile) {
					SFSSourceFile sf = (SFSSourceFile) bf;
					return loadResource(
							"multi-file/" + sf.getSourceFile().getName())
							.length();
				}
				return 0;
			}
		};
		SquashFSWriter writer = new SquashFSWriter(manifest, mockDataProvider);
		byte[] buffer = new byte[10 * 1024];
		RandomAccessByteArray dest = new RandomAccessByteArray(buffer);
		writer.write(dest);

		{
			File f = new File("c:/temp/sfs/multi-file.test.sfs");
			FileOutputStream out = new FileOutputStream(f);
			out.write(buffer);
			out.close();
		}

		log.debug("------------- BEGIN READ -------------");

		{
			IRandomAccessSource source = dest;
			SquashFSReader reader = new SquashFSReader(source);
			Directory rootDirectory = reader.getRootDirectory();
			assertNotNull(rootDirectory);
			assertEquals(3, rootDirectory.getSubentries().size());

			WalkerMock testWalker = new WalkerMock(reader);
			SquashFSUtils.walk(rootDirectory, testWalker);
			assertEquals(15, testWalker.visitData.size());

		}
	}
}
