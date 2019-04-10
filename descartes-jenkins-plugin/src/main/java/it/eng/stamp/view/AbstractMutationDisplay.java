package it.eng.stamp.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import it.eng.stamp.STAMPReportBuildAction;
import it.eng.stamp.results.DescartesReport;
import it.eng.stamp.util.Constants;

public abstract class AbstractMutationDisplay implements ModelObject {

	protected static final Logger LOGGER = Logger.getLogger(AbstractMutationDisplay.class.getName());
	private Map<String, AbstractMutationDisplay> children = new HashMap<>();
	private String name;
	private AbstractMutationDisplay parent;
	protected transient int mutationCount = 0;
	protected transient int detectedCount= 0;
	protected transient int notDetectedCount = 0;
	private transient STAMPReportBuildAction buildAction;
	private DescartesReport currentReport;

	public AbstractMutationDisplay(AbstractMutationDisplay parent, String name, STAMPReportBuildAction action) {
		this.buildAction = action;
		currentReport = this.buildAction.getDescartesReport();
		this.parent= parent;
		this.name= name;
		tally();
		compute();
	}

	public String getParentName() {
		return getParent().getName();
	}

	public String getParentUrl() {
		return getParent().getUrlName();
	}
	
	private AbstractMutationDisplay getParent() {
		return parent;
	}

	public Collection<? extends AbstractMutationDisplay> getChildren() {
		return children.values();
	}

	public AbstractMutationDisplay getChildByName(String name) {
		return children.get(Constants.safe(name));
	}

	public Collection<String> getChildrenNames() {
		return children.keySet();
	}

	@Override
	public String getDisplayName() {
		return Constants.display(getName());
	}

	public String getName() {
		return name;
	}

	public long getMutationCoverage() {
		return 	Constants.percent(detectedCount,mutationCount);
	}
	
	public int getDetectedCount() {
		return detectedCount;
	}

	public int getNotDetectedCount() {
		return notDetectedCount;
	}

	public int getMutationCount() {
		return mutationCount;
	}

	protected void addChild(AbstractMutationDisplay value) {
		children.put(value.getUrlName(), value);
	}

	protected String getAncestorName() {
		return getParent().getParentName();
	}
	
	protected void compute() {
		for (AbstractMutationDisplay m : getChildren()) {
			mutationCount += m.getMutationCount();
			detectedCount += m.getDetectedCount();
			notDetectedCount += m.getNotDetectedCount();
		}
	}

	/**
	 * MUST initialize children and childrenNames
	 * 
	 */
	protected abstract void tally();

	public String getUrlName(){
		return Constants.safe(getName());
	}
	
	public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
		if (token.equals(getUrlName())) {
			return this;
		}
		AbstractMutationDisplay result = getChildByName(token);
		if (result != null) {
			return result;
		}
		else {
			return null;
		}
	}

	public AbstractBuild<?, ?> getBuild() {
		 return buildAction.getBuild();
	}

	public DescartesReport getDescartesReport() {
		return currentReport;
	}
	
	protected STAMPReportBuildAction getBuildAction() {
		return buildAction;
	}


}