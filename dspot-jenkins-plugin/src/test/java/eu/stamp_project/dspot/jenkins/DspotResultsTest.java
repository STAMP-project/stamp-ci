package eu.stamp_project.dspot.jenkins;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import eu.stamp_project.dspot.jenkins.report.DSpotResults;
import hudson.FilePath;


public class DspotResultsTest {

	
	@Test
	public void testParsing() throws Exception{
		File f = getResourceAsFile("dspot-out/");
		DSpotResults res = new DSpotResults(new FilePath(f));
		
		Assert.assertEquals("change reports empty", 0, res.getChangeReport().size());
		Assert.assertEquals("coverave reports empty", 0, res.getCoverageReport().size());
		Assert.assertNotEquals("change reports not empty", 0, res.getMutantsReport().size());
	}
	
	private static File getResourceAsFile(String resource) throws Exception {
		URL url = DspotResultsTest.class.getResource(resource);
		Assert.assertNotNull("Resource " + resource + " could not be found", url);
		File f = new File(url.toURI());
		return f;
	}
}
