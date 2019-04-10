package it.eng.stamp.view;

import java.util.Collection;

import it.eng.stamp.STAMPReportBuildAction;
import it.eng.stamp.results.MethodClassification;
import it.eng.stamp.results.MethodResult;

public class MethodSummaryDisplay extends AbstractMutationDisplay {

	private transient MethodResult result;
	
	public MethodSummaryDisplay(AbstractMutationDisplay parent, String name, STAMPReportBuildAction action) {
		super(parent, name, action);
		
	}

	public Collection<String> getTests() {
		return result.getTests();
	}

	public MethodClassification getClassification() {
		return result.getClassification();
	}
	
	public MethodResult getResult() {
		return result;
	}

	@Override
	protected void tally() {
		result = getDescartesReport().getByFullName(getAncestorName(), getParentName(), getName());
		mutationCount = result.getMutations().size();
		detectedCount = result.getDetected().size();
		notDetectedCount = result.getNotDetected().size(); 		
	}

}
