package eu.stamp_project.dspot.jenkins.json.mutant;

import java.io.Serializable;

/**
 * created by VALENTINA DI GIACOMO
 * Extension of original classes by Benjamin DANGLOT
 * Needed public constructors and non final public fields for deserialization.
 * Consider a pull request for this
 * on 22/11/2018
 */
public class MutantJSON implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3166060523194702204L;
	public String ID;
    public int lineNumber;
    public String locationMethod;

    public MutantJSON() {
	}
    
}
