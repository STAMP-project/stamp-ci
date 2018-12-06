package eu.stamp_project.dspot.jenkins.report.view;

import java.util.ArrayList;
import java.util.List;

import eu.stamp_project.dspot.jenkins.json.ClassTimeJSON;
import eu.stamp_project.dspot.jenkins.json.mutant.TestClassJSON;
import hudson.model.Run;
import eu.stamp_project.dspot.jenkins.json.mutant.TestCaseJSON;

public class MutantSelectorDisplay extends AbstractDSpotDisplay {
	
	public int killedBefore = 0;
	public int killedAfter = 0;
	public List<TestCaseJSON> newTestCases = new ArrayList<>();
	    
	public MutantSelectorDisplay() {
	}

	public MutantSelectorDisplay(Run<?,?> run, ClassTimeJSON clazz, TestClassJSON m) {
		super(run, clazz);
		killedBefore = m.nbMutantKilledOriginally;
		newTestCases = m.testCases;
		newTestCases.forEach(t -> killedAfter += t.nbMutantKilled);
		numOriginalTestCases = m.nbOriginalTestCases;
	}
	
	@Override
	public int getNumNewTestCases(){
		return newTestCases.size();
	}

}
