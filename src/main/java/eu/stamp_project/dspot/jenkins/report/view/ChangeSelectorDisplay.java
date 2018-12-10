package eu.stamp_project.dspot.jenkins.report.view;

import java.util.List;

import eu.stamp_project.dspot.jenkins.json.ClassTimeJSON;
import eu.stamp_project.dspot.jenkins.json.change.TestCaseJSON;
import hudson.model.Run;

public class ChangeSelectorDisplay extends AbstractDSpotDisplay {
	private List<TestCaseJSON> newTestCases;
	public long numTestAmplified;

	public ChangeSelectorDisplay() {
	}

	public ChangeSelectorDisplay(Run<?,?> run, ClassTimeJSON clazz, eu.stamp_project.dspot.jenkins.json.change.TestClassJSON ch) {
		super(run, clazz);
		newTestCases = ch.testCases;
		numOriginalTestCases = ch.nbOriginalTestCases;
		numTestAmplified = ch.nbTestAmplified;
	}

	@Override
	public int getNumNewTestCases() {
		return newTestCases.size();
	}

}
