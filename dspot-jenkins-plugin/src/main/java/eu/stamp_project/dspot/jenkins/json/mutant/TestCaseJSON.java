package eu.stamp_project.dspot.jenkins.json.mutant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


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
	private static final long serialVersionUID = 3511100565205359166L;
	public String name;
    public int nbAssertionAdded;
    public int nbInputAdded;
    public int nbMutantKilled;
    public List<MutantJSON> mutantsKilled;

    public TestCaseJSON() {
    	mutantsKilled = new ArrayList<>();
	}
    
}