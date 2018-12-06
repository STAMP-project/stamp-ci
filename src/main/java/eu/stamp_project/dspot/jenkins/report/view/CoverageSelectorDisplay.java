package eu.stamp_project.dspot.jenkins.report.view;

import java.util.List;

import eu.stamp_project.dspot.jenkins.json.ClassTimeJSON;
import eu.stamp_project.dspot.jenkins.json.coverage.TestCaseJSON;
import eu.stamp_project.dspot.jenkins.json.coverage.TestClassJSON;
import hudson.model.Run;

public class CoverageSelectorDisplay extends AbstractDSpotDisplay {
	public long numInitialInstructuions;
	private List<TestCaseJSON> newTestCases;
	
	public CoverageSelectorDisplay() {
	}

	public CoverageSelectorDisplay(Run<?,?> run, ClassTimeJSON clazz, TestClassJSON co) {
		super(run, clazz);
		numOriginalTestCases = co.nbOriginalTestCases;
		numInitialInstructuions = co.initialInstructionCovered;
		newTestCases = co.testCases;
		
	}

	@Override
	public int getNumNewTestCases() {
		return newTestCases.size();
	}

}
