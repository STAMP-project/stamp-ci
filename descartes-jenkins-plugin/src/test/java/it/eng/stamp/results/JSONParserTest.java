package it.eng.stamp.results;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import it.eng.stamp.results.DescartesReport;
import it.eng.stamp.results.DescartesReportParser;
import it.eng.stamp.util.ReportMetrics;

public class JSONParserTest {
	
    @Test
	public void testParsing() throws Exception {
		DescartesReportParser parser = new DescartesReportParser();
		
		File f = getResourceAsFile("MutationsResult/mutations.json");
		
		List<File> files = new ArrayList<>();
		files.add(f);
		
		DescartesReport testresult = parser.parse(files);
		testresult.doIndex();
		
		Assert.assertFalse("Methods parsed", testresult.getMethods().isEmpty());
		Assert.assertTrue("Coverage value", testresult.getAverageForMetric(ReportMetrics.COVERAGE)<0.7);
		Assert.assertTrue("First result classification check", testresult.getMethods().get(0).getClassification().equals(MethodClassification.TESTED));
		//TODO to improve.

	}

	private static File getResourceAsFile(String resource) throws Exception {
		URL url = JSONParserTest.class.getResource(resource);
		Assert.assertNotNull("Resource " + resource + " could not be found", url);
		File f = new File(url.toURI());
		return f;
	}
}
