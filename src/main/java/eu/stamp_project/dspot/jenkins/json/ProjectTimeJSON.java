package eu.stamp_project.dspot.jenkins.json;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import eu.stamp_project.dspot.jenkins.utils.JsonRequired;

/**
 * created by VALENTINA DI GIACOMO Extension of original classes by Benjamin
 * DANGLOT Needed public constructors and non final public fields for
 * deserialization. Consider a pull request for this on 22/11/2018
 */
public class ProjectTimeJSON implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6985663602945573679L;
	public Set<ClassTimeJSON> classTimes = new LinkedHashSet<>();
	@JsonRequired
	public String projectName;

	public ProjectTimeJSON() {

	}

}
