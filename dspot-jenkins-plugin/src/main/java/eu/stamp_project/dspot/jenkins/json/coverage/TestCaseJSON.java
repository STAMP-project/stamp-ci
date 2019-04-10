package eu.stamp_project.dspot.jenkins.json.coverage;

/**
 * created by VALENTINA DI GIACOMO
 * Extension of original classes by Benjamin DANGLOT
 * Needed public constructors and non final public fields for deserialization.
 * Consider a pull request for this
 * on 22/11/2018
 */
public class TestCaseJSON {

	public String name;
	public int nbAssertionAdded;
	public int nbInputAdded;
	public int instructionCovered;
	public int instructionTotal;

	public TestCaseJSON() {
	}
}