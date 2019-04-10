package eu.stamp_project.dspot.jenkins.json.mutant;

import java.io.Serializable;
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
public class TestClassJSON implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6272923406186110973L;
	@JsonRequired public int nbMutantKilledOriginally;
    public String name;
    public long nbOriginalTestCases;
    public List<TestCaseJSON> testCases = new ArrayList<>();

    public TestClassJSON() {
	}
}
