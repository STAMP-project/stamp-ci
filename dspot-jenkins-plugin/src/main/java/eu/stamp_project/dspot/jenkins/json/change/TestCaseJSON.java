package eu.stamp_project.dspot.jenkins.json.change;

import java.io.Serializable;

/**
 * created by VALENTINA DI GIACOMO
 * Extension of original classes by Benjamin DANGLOT
 * Needed public constructors and non final public fields for deserialization.
 * Consider a pull request for this
 * on 22/11/2018
 */
public class TestCaseJSON implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7714545047063964488L;
	public String name;
    public long nbInputAmplification;
    public long nbAssertionAmplification;

    public TestCaseJSON() {
	}
}
