package it.eng.stamp.view;


import it.eng.stamp.STAMPReportBuildAction;
import it.eng.stamp.results.DescartesReport;
import it.eng.stamp.results.MethodResult;

public class ClassSummaryDisplay extends AbstractMutationDisplay {

	public ClassSummaryDisplay(AbstractMutationDisplay parent, String className, STAMPReportBuildAction action) {
		super(parent,  className, action);
		
	}


	@Override
	protected void tally() {
		DescartesReport report = getDescartesReport();
		for (MethodResult mr : report.getMethods()) {
			if (mr.getPakg().equals(getParentName()) && mr.getClassName().equals(getName())) {
				if(!getChildrenNames().contains(mr.getName()))
					addChild(new MethodSummaryDisplay(this, mr.getName(), getBuildAction()));
			}
		}
	}

}
