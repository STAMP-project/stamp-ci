package eu.stamp_project.dspot.jenkins;

import java.io.IOException;
import java.io.PrintStream;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import eu.stamp_project.dspot.jenkins.report.DSpotResults;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;

/**
 * @author VDIGIACO
 *
 */
@SuppressWarnings("rawtypes")
public class DSpotReportCollector extends Notifier implements SimpleBuildStep {

	@Nonnull
	private String outputDir = STAMPDescriptor.defaultOutputDir;

	@DataBoundConstructor
	public DSpotReportCollector() {

	}

	public String getOutputDir() {
		return outputDir;
	}

	@DataBoundSetter
	public void setOutputDir(@Nonnull String outputDir) {
		this.outputDir = outputDir;
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
	public void perform(Run<?, ?> run, FilePath wsp, Launcher arg2, TaskListener listener)
			throws InterruptedException, IOException {

		PrintStream logger = listener.getLogger();
		if(run.getAction(DSpotResultsAction.class) == null){
			logger.print("DSpot report already configured from Build step. skipping post build step config.");
			return;
		}
		DSpotResults results = new DSpotResults(new FilePath(wsp, outputDir));

		if (results.getProjectTime() == null) {
			logger.print("Build WARNING. STAMP Report file was not found or it is not well-formed.");
			return;
		}

		DSpotResultsAction action = new DSpotResultsAction(run, results);
		run.addAction(action);
		return;
	}
	
	@Override
	public boolean needsToRunAfterFinalized() {
		return true;
	}

	@Symbol("dspot-report")
	@Extension
	public static class STAMPDescriptor extends BuildStepDescriptor<Publisher> {

		public static final String defaultOutputDir = "dspot-out";

		@Override
		public String getDisplayName() {
			return "STAMP DSpot Reports";
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}
	}
}
