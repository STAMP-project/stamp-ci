package eu.stamp_project.dspot.jenkins;

import java.io.File;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;


import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.program.ConstantsProperties;
import hudson.model.FreeStyleProject;

public class DspotUtilTest {

	public JenkinsRule jenkins = new JenkinsRule();
	JenkinsRule.WebClient wc;

	public static final String TEST_CODE = "src/test/java";

	public static final String WIN_TEST_PATH = "src\\test\\java";

	public static final String PATH = "src\\test\\java\\eu\\pkg\\MyTestClass.java";

	public static final String QNAME = "eu.pkg.MyTestClass";

	@Test
	public void testDefaultConfigRoundtrip() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject();
		DspotStep step = new DspotStep();
		step.setTestCode(TEST_CODE);
		project.getBuildersList().add(step);
		project = jenkins.configRoundtrip(project);
		jenkins.assertEqualDataBoundBeans(step, project.getBuildersList().get(0));
	}

	@Test
	public void testReplacement() {
		Properties init_properties = new Properties();
		if (!File.separator.equals("/"))
			init_properties.setProperty(ConstantsProperties.TEST_CLASSES.getName(),
					ConstantsProperties.TEST_CLASSES.getDefaultValue().replaceAll("/", "\\\\"));
		Assert.assertEquals("replacement ok", init_properties.getProperty(ConstantsProperties.TEST_CLASSES.getName()),
				"target\\test-classes\\");
		System.out.println(init_properties.getProperty(ConstantsProperties.TEST_CLASSES.getName()));
	}

	@Test
	public void winAddSeparator() {
		Assert.assertEquals("added separator", DSpotUtils.shouldAddSeparator.apply("test"), "test\\");
	}

	@Test
	public void testOSSeparatorChanger() {
		if (File.separator.equals("/"))
			Assert.assertEquals("path not updated", DspotStep.shouldUpdatePath.apply(TEST_CODE), TEST_CODE);
		else
			Assert.assertEquals("path not updated", DspotStep.shouldUpdatePath.apply(TEST_CODE), WIN_TEST_PATH);
	}

	@Test
	public void testFileNameToQualifiedName() {
		DspotStep step = new DspotStep();
		step.setTestCode(TEST_CODE);
		Assert.assertEquals("path to name not working", step.pathToQualifiedName(PATH), QNAME);

	}
}
