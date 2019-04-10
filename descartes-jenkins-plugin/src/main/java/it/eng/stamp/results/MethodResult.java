package it.eng.stamp.results;

import java.io.Serializable;
import java.util.Collection;

import org.kohsuke.stapler.export.ExportedBean;

import com.google.gson.annotations.SerializedName;

/**
 * Deserialization of Method Results in method.json report
 * @author Valentina Di Giacomo
 *
 */
@ExportedBean
public class MethodResult  implements Serializable {
	
	private static final long serialVersionUID = 6267907206895444252L;
	private String name;
	private String description;
	@SerializedName("class")
	private String className;
	@SerializedName("package")
	private String pakg;
	
	private MethodClassification classification;
	
	private Collection<String> detected;
	@SerializedName("not-detected")
	private Collection<String> notDetected;
	private Collection<String> tests;
	
	private Collection<MutationResult> mutations;

	public MethodResult() {
	}
	
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getClassName() {
		return className;
	}

	public String getPakg() {
		return pakg;
	}

	public MethodClassification getClassification() {
		return classification;
	}

	public Collection<String> getDetected() {
		return detected;
	}

	public Collection<String> getNotDetected() {
		return notDetected;
	}

	public Collection<String> getTests() {
		return tests;
	}

	public Collection<MutationResult> getMutations() {
		return mutations;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setPakg(String pakg) {
		this.pakg = pakg;
	}

	public void setClassification(MethodClassification classification) {
		this.classification = classification;
	}

	public void setDetected(Collection<String> detected) {
		this.detected = detected;
	}

	public void setNotDetected(Collection<String> notDetected) {
		this.notDetected = notDetected;
	}

	public void setTests(Collection<String> tests) {
		this.tests = tests;
	}

	public void setMutations(Collection<MutationResult> mutations) {
		this.mutations = mutations;
	}

}
