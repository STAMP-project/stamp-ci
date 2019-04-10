package it.eng.stamp.view;

import it.eng.stamp.STAMPReportBuildAction;
import it.eng.stamp.results.DescartesReport;
import it.eng.stamp.results.MethodResult;

public class PackageSummaryDisplay extends AbstractMutationDisplay {

	public PackageSummaryDisplay(AbstractMutationDisplay parent, String name, STAMPReportBuildAction action) {
		super(parent, name, action);	
	}

	@Override
	protected void tally() {
		DescartesReport descartesReport = getDescartesReport();
		for (MethodResult mr : descartesReport.getMethods()) {
			if (mr.getPakg().equals(getName()))
				if (!getChildrenNames().contains(mr.getClassName())) {
					addChild(new ClassSummaryDisplay(this, mr.getClassName(), getBuildAction()));
				}
		}
	}

}
