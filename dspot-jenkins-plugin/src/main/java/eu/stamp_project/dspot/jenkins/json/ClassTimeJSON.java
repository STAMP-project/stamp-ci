package eu.stamp_project.dspot.jenkins.json;

import java.io.Serializable;

/**
 * created by VALENTINA DI GIACOMO
 * Extension of original classes by Benjamin DANGLOT
 * Needed public constructors and non final public fields for deserialization.
 * Consider a pull request for this
 * on 22/11/2018
 */
public class ClassTimeJSON implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6319392203628095756L;
	public String fullQualifiedName;
    public long timeInMs;

    public ClassTimeJSON() {
    	
	}

}
