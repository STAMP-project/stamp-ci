package eu.stamp_project.cicd.utils.botsing;

import java.io.File;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test Botsing invoker.
 * @author Pierre-Yves Gibello - OW2
 *
 */
public class BotsingInvokerTest extends TestCase {

	public void testFindGeneratedTest() {
		File test = BotsingInvoker.findGeneratedTest(new File("."));
		Assert.assertTrue(test.exists());
		test = BotsingInvoker.findGeneratedTest(new File("NoTestHere"));
		Assert.assertNull(test);
	}
}
