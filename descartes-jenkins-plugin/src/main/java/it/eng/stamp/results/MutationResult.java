package it.eng.stamp.results;

import java.io.Serializable;
import java.util.Collection;

import com.google.gson.annotations.SerializedName;


public class MutationResult implements Serializable {

	private static final long serialVersionUID = 4353747910455363006L;
	private MutationStatus status;
	private String mutator;
	@SerializedName("tests-run")
	private int testsRun;
	@SerializedName("detected-by")
	private String detectedBy;
	public MutationStatus getStatus() {
		return status;
	}

	public String getMutator() {
		return mutator;
	}

	public int getTestsRun() {
		return testsRun;
	}

	public String getDetectedBy() {
		return detectedBy;
	}

	public Collection<String> getTests() {
		return tests;
	}

	public void setStatus(MutationStatus status) {
		this.status = status;
	}

	public void setMutator(String mutator) {
		this.mutator = mutator;
	}

	public void setTestsRun(int testsRun) {
		this.testsRun = testsRun;
	}

	public void setDetectedBy(String detectedBy) {
		this.detectedBy = detectedBy;
	}

	public void setTests(Collection<String> tests) {
		this.tests = tests;
	}

	private Collection<String> tests;
		
	public MutationResult() {
	
	}
	


}
