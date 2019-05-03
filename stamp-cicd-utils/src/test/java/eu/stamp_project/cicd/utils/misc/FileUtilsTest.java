package eu.stamp_project.cicd.utils.misc;

import java.io.File;
import java.io.IOException;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test file utilities.
 * @author Pierre-Yves Gibello - OW2
 *
 */
public class FileUtilsTest extends TestCase {

	public void testDeleteIfExists() {
		String fname = null;
		try {
			fname = FileUtils.tempFile("testDeleteIfExists");
		} catch (IOException e) {
			fail("Exception thrown: " + e);
		}
		File data = new File(fname);
		try {
			FileUtils.deleteIfExists(null);
			FileUtils.deleteIfExists(new File("/SantaClaus"));
			FileUtils.deleteIfExists(data);
		} catch (IOException e) {
			fail("Exception thrown: " + e);
		}
		Assert.assertTrue(! data.exists());
	}

	public void testTempFile() {
		String fname = null;
		try {
			fname = FileUtils.tempFile("testTempFile");
		} catch (IOException e) {
			fail("Exception thrown: " + e);
		}
		Assert.assertNotNull(fname);
		File data = new File(fname);
		Assert.assertTrue(data.exists() && data.isFile());
	}
	
	public void testFileToString() {
		String fname = null;
		String data = null;
		try {
			fname = FileUtils.tempFile("12345\n6789");
			Assert.assertNotNull(fname);
			data = FileUtils.fileToString(new File(fname));
		} catch (IOException e) {
			fail("Exception thrown: " + e);
		}
		Assert.assertNotNull(data);
		Assert.assertEquals(10, data.length());
	}

}
