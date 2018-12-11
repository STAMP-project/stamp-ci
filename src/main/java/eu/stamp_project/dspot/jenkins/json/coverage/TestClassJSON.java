package eu.stamp_project.dspot.jenkins.json.coverage;

import java.util.ArrayList;
import java.util.List;

import eu.stamp_project.dspot.jenkins.utils.JsonRequired;

/**
 * created by VALENTINA DI GIACOMO
 * Extension of original classes by Benjamin DANGLOT
 * Needed public constructors and non final public fields for deserialization.
 * Consider a pull request for this
 * on 22/11/2018
 */
public class TestClassJSON {

	public String name;
	public long nbOriginalTestCases;
	@JsonRequired public long initialInstructionCovered;
	public long initialInstructionTotal;
	public double percentageinitialInstructionCovered;
	public long amplifiedInstructionCovered;
	public long amplifiedInstructionTotal;
	public double percentageamplifiedInstructionCovered;
	public List<TestCaseJSON> testCases = new ArrayList<>();

	public TestClassJSON() {
	}
}