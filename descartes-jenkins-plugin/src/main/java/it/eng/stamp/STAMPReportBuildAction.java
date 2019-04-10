/**
 * 
 */
package it.eng.stamp;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerProxy;

import com.thoughtworks.xstream.XStream;

import hudson.XmlFile;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.util.HeapSpaceStringConverter;
import hudson.util.XStream2;
import it.eng.stamp.results.DescartesReport;
import it.eng.stamp.util.Constants;
import it.eng.stamp.view.AbstractMutationDisplay;
import it.eng.stamp.view.DescartesReportResultDisplay;

/**
 * @author VDIGIACO
 *
 */
public class STAMPReportBuildAction implements Action, StaplerProxy {

	private final AbstractBuild<?, ?> build;
	private transient WeakReference<DescartesReport> report;
	private transient WeakReference<DescartesReportResultDisplay> buildActionResultsDisplay;

	private transient static final Logger logger = Logger.getLogger(STAMPReportBuildAction.class.getName());

	public STAMPReportBuildAction(AbstractBuild<?, ?> build, DescartesReport report) {
		this.build = build;
		setReport(report);
		addPreviousBuildReportToExistingReport();
	}

	@Override
	public Object getTarget() {
		return getBuildActionResultsDisplay();
	}

	@Override
	public String getIconFileName() {
		return Constants.STAMP_LOGO_LOCATION;
	}

	@Override
	public String getDisplayName() {
		return Messages.STAMPReportBuildAction_DisplayName();
	}

	@Override
	public String getUrlName() {
		return Constants.PLUGIN_NAME;
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
	}

	public synchronized DescartesReport getDescartesReport() {
		DescartesReport r;
		if (report == null) {
			r = load();
			report = new WeakReference<DescartesReport>(r);
		} else {
			r = report.get();
		}

		if (r == null) {
			r = load();
			report = new WeakReference<DescartesReport>(r);
		}
		return r;

	}

	/**
	 * Loads a {@link TestResult} from disk.
	 */
	private DescartesReport load() {
		DescartesReport r;
		try {
			r = (DescartesReport) getDataFile().read();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load " + getDataFile(), e);
			r = new DescartesReport(); // return a dummy
		}
		return r;
	}

	public synchronized void setReport(DescartesReport report) {

		if (build != null) {
			// persist the data
			try {
				getDataFile().write(report);
			} catch (IOException e) {
				logger.severe("Failed to save the JUnit test result");
			}
		}
		report.setBuildAction(this);
		this.report = new WeakReference<DescartesReport>(report);
	}

	private XmlFile getDataFile() {
		return new XmlFile(XSTREAM, new File(build.getRootDir(), "descartesResult.xml"));
	}

	public AbstractMutationDisplay getBuildActionResultsDisplay() {
		DescartesReportResultDisplay buildDisplay = null;
		WeakReference<DescartesReportResultDisplay> wr = this.buildActionResultsDisplay;
		if (wr != null) {
			buildDisplay = wr.get();
			if (buildDisplay != null)
				return buildDisplay;
		}
		try {
			buildDisplay = new DescartesReportResultDisplay(this);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error creating new DescartesReportBuildDisplay()", e);
		}
		this.buildActionResultsDisplay = new WeakReference<>(buildDisplay);
		return buildDisplay;
	}

	public void setBuildActionResultsDisplay(WeakReference<DescartesReportResultDisplay> buildActionResultsDisplay) {
		this.buildActionResultsDisplay = buildActionResultsDisplay;
	}

	private void addPreviousBuildReportToExistingReport() {
		AbstractBuild<?, ?> previousBuild = getBuild().getPreviousBuild();
		if (previousBuild == null) {
			return;
		}

		if (previousBuild.getActions(STAMPReportBuildAction.class).isEmpty()) {
			return;
		}
		STAMPReportBuildAction previousPerformanceAction = previousBuild.getActions(STAMPReportBuildAction.class)
				.get(0);

		AbstractMutationDisplay previousBuildActionResults = previousPerformanceAction.getBuildActionResultsDisplay();
		if (previousBuildActionResults == null) {
			return;
		}

		DescartesReport lastReport = previousBuildActionResults.getDescartesReport();
		getDescartesReport().setLastBuildReport(lastReport);
	}
	
	private static final XStream XSTREAM = new XStream2();

	static {
		XSTREAM.alias("result", DescartesReport.class);
		XSTREAM.registerConverter(new HeapSpaceStringConverter(), 100);
	}
}
