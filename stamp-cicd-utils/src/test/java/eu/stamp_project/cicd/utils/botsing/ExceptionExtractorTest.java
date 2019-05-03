package eu.stamp_project.cicd.utils.botsing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test exception parsing and preprocessing.
 * @author Pierre-Yves Gibello - OW2
 *
 */
public class ExceptionExtractorTest extends TestCase {

	public void testExtractExceptionsFile() {
		File input = new File(
				this.getClass().getClassLoader().getResource("catalina.out").getFile());
		
		List<String> exceptions = null;
		try {
			exceptions = ExceptionExtractor.extractExceptions(input);
		} catch (IOException e) {
			fail("Test failed with IOException: " + e);
		}
		Assert.assertNotNull(exceptions);
		Assert.assertEquals(17, exceptions.size());
	}

	public void testExplodeExceptionsFile() {
		File input = new File(
				this.getClass().getClassLoader().getResource("catalina.out").getFile());
		
		List<String> exceptions = null;
		try {
			exceptions = ExceptionExtractor.explodeExceptions(input);
		} catch (IOException e) {
			fail("Test failed with IOException: " + e);
		}
		Assert.assertNotNull(exceptions);
		Assert.assertEquals(36, exceptions.size());
	}

}
