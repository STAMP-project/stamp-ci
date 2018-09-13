package eu.stamp_project.dspot.jenkins;

import java.io.File;
import java.util.Properties;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import eu.stamp_project.program.ConstantsProperties;
import eu.stamp_project.testrunner.runner.test.TestRunner;
import eu.stamp_project.utils.DSpotUtils;

public class DspotUtilTest {
	@Test
	public void copyresources() {
		try {
			DSpotUtils.copyPackageFromResources();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertTrue(true);
	}

	@Test
	public void testReplacement() {
		Properties init_properties = new Properties();
		if (!TestRunner.FILE_SEPARATOR.equals("/"))
			init_properties.setProperty(ConstantsProperties.TEST_CLASSES.getName(),
					ConstantsProperties.TEST_CLASSES.getDefaultValue().replaceAll("/", "\\\\"));
		Assert.assertEquals("replacement ok", init_properties.getProperty(ConstantsProperties.TEST_CLASSES.getName()),
				"target\\test-classes\\");
		System.out.println(init_properties.getProperty(ConstantsProperties.TEST_CLASSES.getName()));
	}

	@Test
	public void winAddSeparator() {
		Assert.assertEquals("added separator", shouldAddSeparator.apply("test"), "test\\");
	}
	
    public static final Function<String, String> shouldAddSeparator = string -> string + (string != null && string.endsWith(File.separator) ? "" : File.separator);
}
