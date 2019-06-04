package eu.stamp_project.cicd.utils.dspot;

import junit.framework.Assert;
import junit.framework.TestCase;

public class DspotInvokerTest extends TestCase {

	public void testDspotInvoker() {
		DspotInvoker invoker = new DspotInvoker("/tmp/pom.xml", "1.0");
		Assert.assertEquals("/tmp/pom.xml", invoker.pomPath);
		Assert.assertEquals("1.0", invoker.dspotVersion);
		Assert.assertNotNull(invoker.configuration);
		Assert.assertEquals(2, invoker.configuration.size());
		Assert.assertEquals("src/main/java", invoker.configuration.get("src"));
		Assert.assertEquals("src/test/java", invoker.configuration.get("testSrc"));
		Assert.assertEquals(-1, invoker.iterations);
		Assert.assertTrue(invoker.newTestClass);
		Assert.assertNull(invoker.testCriterion);
		
		invoker = new DspotInvoker("/tmp/pom.xml", "1.0", false);
		Assert.assertFalse(invoker.newTestClass);
	}

	public void testWithTargetModule() {
		DspotInvoker invoker = new DspotInvoker("/tmp/pom.xml", "1.0");
		invoker.withTargetModule("test");
		Assert.assertEquals("test", invoker.configuration.get("targetModule"));
	}

	public void testWithOutputDir() {
		DspotInvoker invoker = new DspotInvoker("/tmp/pom.xml", "1.0");
		invoker.withOutputDir("output");
		Assert.assertEquals("output", invoker.configuration.get("outputDirectory"));
	}

	public void testWithTest() {
		DspotInvoker invoker = new DspotInvoker("/tmp/pom.xml", "1.0");
		invoker.withTest("a.test.Class");
		Assert.assertEquals("a.test.Class", invoker.test);
	}

	public void testWithTestDir() {
		DspotInvoker invoker = new DspotInvoker("/tmp/pom.xml", "1.0");
		invoker.withTestDir("testDir");
		Assert.assertEquals("testDir", invoker.configuration.get("testSrc"));
	}

	public void testWithSourceDir() {
		DspotInvoker invoker = new DspotInvoker("/tmp/pom.xml", "1.0");
		invoker.withSourceDir("sourceDir");
		Assert.assertEquals("sourceDir", invoker.configuration.get("src"));
	}
	
	public void testWithJacocoCriterion() {
		DspotInvoker invoker = new DspotInvoker("/tmp/pom.xml", "1.0");
		invoker.withJacocoCriterion();
		Assert.assertEquals(invoker.testCriterion, "JacocoCoverageSelector");
	}
	
	public void testWithIterations() {
		DspotInvoker invoker = new DspotInvoker("/tmp/pom.xml", "1.0");
		invoker.withIterations(2);
		Assert.assertEquals(2, invoker.iterations);
	}

}
