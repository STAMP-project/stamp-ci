/**
 * 
 */
package it.eng.stamp;

import java.io.IOException;
import java.io.PrintStream;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.google.common.base.Strings;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import it.eng.stamp.results.DescartesReport;
import it.eng.stamp.results.DescartesReportParser;
import it.eng.stamp.util.Constants;
import it.eng.stamp.util.ReportMetrics;

/**
 * @author VDIGIACO
 *
 */
@SuppressWarnings("rawtypes")
public class STAMPReportCollector extends Recorder {

	private static final String defaultLocation = "**/target/pit-reports/*/mutations.json";
	private String filePath = defaultLocation;
	private int treshold;

	@DataBoundConstructor
	public STAMPReportCollector() {

	}

	public String getFilePath() {
		return filePath;
	}

	@DataBoundSetter
	public void setFilePath(String filePath) {
		if (Strings.isNullOrEmpty(filePath))
			this.filePath = STAMPReportCollector.defaultLocation;
		else
			this.filePath = filePath;
	}

	@DataBoundSetter
	public final void setTreshold(int treshold) {
		this.treshold = treshold;
	}

	public int getTreshold() {
		return treshold;
	}

	private String getReportLocation() {
		if (Strings.isNullOrEmpty(getFilePath()))
			return STAMPReportCollector.defaultLocation;
		else
			return getFilePath();
	}

	public boolean isFilePathSet() {
		return !(Strings.isNullOrEmpty(getFilePath()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.tasks.BuildStep#getRequiredMonitorService()
	 */
	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return new STAMPResultProjectAction(project);
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {

		PrintStream logger = listener.getLogger();
		DescartesReportParser parser = new DescartesReportParser();
		DescartesReport report = parser.parseResult(getReportLocation(), build, launcher, listener);

		if (report == null) {
			logger.print("Build WARNING. STAMP Report file was not found or it is not well-formed.");

			Result result = build.getResult();
			if (result != null && result.isBetterOrEqualTo(Result.UNSTABLE))
				build.setResult(Result.UNSTABLE);
			return true;
		}
		report.doIndex();
		STAMPReportBuildAction buildAction = new STAMPReportBuildAction(build, report);
		build.addAction(buildAction);

		int buildCoverage = (int) Constants.toPercent(report.getAverageForMetric(ReportMetrics.COVERAGE));;

		if (treshold > 0 && (buildCoverage < treshold)) {
			Result result = Result.UNSTABLE;
			Result buildresult = build.getResult();
			if (buildresult == null || result.isWorseThan(buildresult)) {
				build.setResult(result);
			}
		}
		return true;
	}

	@Extension
	public static class STAMPDescriptor extends BuildStepDescriptor<Publisher> {

		@Override
		public String getDisplayName() {
			return Messages.STAMPReportResultsPublisher_DisplayName();
		}

		/*
		 * Performs on-the-fly validation on the file mask wildcard. It allows
		 * the empty string as it assumes the default value.
		 */
		public FormValidation doCheckFilePath(@AncestorInPath AbstractProject project, @QueryParameter String value)
				throws IOException {
			if (project != null && !value.isEmpty()) {
				return FilePath.validateFileMask(project.getSomeWorkspace(), value);
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckTreshold(@QueryParameter int value) throws IOException {
			if (value < 0)
				return FormValidation.error("Invalid value. Must be a positive integer");
			if (value > 100)
				return FormValidation.error("Invalid value. Must be a percentage. Max value is 100");
			return FormValidation.ok();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return FreeStyleProject.class.isAssignableFrom(jobType);
		}
	}
}
