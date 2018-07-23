package eu.stamp_project.dspot.jenkins;

import java.io.IOException;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.program.InputConfiguration;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

public class DspotStep extends Builder {
	private InputConfiguration input;
	private String dspotConf = "dspot.config";
//	private File dspotConfFile;

	@DataBoundConstructor
	public DspotStep(String what) {

	}
	
	@DataBoundSetter
	public void setDspotConf(String filepath) {
		dspotConf = filepath;
	}
	
	public String getDspotConf() {
		return dspotConf;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		String fileLocation = new FilePath(build.getWorkspace(), dspotConf).getRemote();
		listener.getLogger().println("file location: " + fileLocation);
		input = InputConfiguration.initialize(fileLocation);
		input.setAbsolutePathToProjectRoot(build.getWorkspace().getRemote());
		try {
			DSpot dspot = new DSpot(input);
			dspot.amplifyAllTests();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}
	
	@Symbol("dspot")
	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

		@Override
		public String getDisplayName() {
			return "STAMP DSpot";
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}
		
		/*
		 * Performs on-the-fly validation on the file mask wildcard. It allows
		 * the empty string as it assumes the default value.
		 */
		public FormValidation doCheckDspotConf(@AncestorInPath AbstractProject project, @QueryParameter String value)
				throws IOException {
			if (project != null && !value.isEmpty()) {
				return FilePath.validateFileMask(project.getSomeWorkspace(), value);
			}
			return FormValidation.ok();
		}
	}

}