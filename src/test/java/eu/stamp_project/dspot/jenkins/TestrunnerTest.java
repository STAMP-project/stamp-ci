package eu.stamp_project.dspot.jenkins;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import eu.stamp_project.testrunner.EntryPoint;

public class TestrunnerTest {
	@Test
	public void findRunnerClasses() {
		URL object = EntryPoint.class.getClassLoader().getResource("runner-classes/");
		URL object2 = ClassLoader.getSystemClassLoader().getResource("runner-classes/");
		System.out.println(object);
		Assert.assertNotNull("object", object);
		Assert.assertNotNull("object2",object2);
	}
}
