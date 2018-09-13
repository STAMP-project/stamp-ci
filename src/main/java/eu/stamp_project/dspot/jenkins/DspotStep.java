package eu.stamp_project.dspot.jenkins;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Properties;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.program.ConstantsProperties;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.program.InputConfigurationProperty;
import eu.stamp_project.testrunner.runner.test.TestRunner;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;

public class DspotStep extends Builder {
	private static final Logger LOGGER = LoggerFactory.getLogger(DspotStep.class);
	// DEFAULTS
	private String projectPath = ConstantsProperties.PROJECT_ROOT_PATH.getDefaultValue();
	private String srcCode = ConstantsProperties.SRC_CODE.getDefaultValue();
	private String testCode = ConstantsProperties.TEST_SRC_CODE.getDefaultValue();
	private String testFilter = ConstantsProperties.FILTER.getDefaultValue();
	private String outputDir = ConstantsProperties.OUTPUT_DIRECTORY.getDefaultValue();

	private Properties init_properties;

	@DataBoundConstructor
	public DspotStep() {
		init_properties = new Properties();
	}

	@DataBoundSetter
	public void setProjectPath(String filepath) {
		projectPath = filepath;
	}

	@DataBoundSetter
	public void setTestFilter(String testFilter) {
		this.testFilter = testFilter;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public String getTestFilter() {
		return testFilter;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		init_properties.setProperty(ConstantsProperties.PROJECT_ROOT_PATH.getName(),
				new FilePath(build.getWorkspace(), projectPath).getRemote());
		init_properties.setProperty(ConstantsProperties.SRC_CODE.getName(),
				new FilePath(build.getWorkspace(), srcCode).getRemote());
		init_properties.setProperty(ConstantsProperties.TEST_SRC_CODE.getName(),
				new FilePath(build.getWorkspace(), testCode).getRemote());
		init_properties.setProperty(ConstantsProperties.OUTPUT_DIRECTORY.getName(),
				new FilePath(build.getWorkspace(), outputDir).getRemote());
		init_properties.setProperty(ConstantsProperties.FILTER.getName(), testFilter);

		// temp solve
		if (!TestRunner.FILE_SEPARATOR.equals("/")) {
			init_properties.setProperty(ConstantsProperties.TEST_CLASSES.getName(),
					ConstantsProperties.TEST_CLASSES.getDefaultValue().replaceAll("/", "\\\\"));
			init_properties.setProperty(ConstantsProperties.SRC_CLASSES.getName(),
					ConstantsProperties.SRC_CLASSES.getDefaultValue().replaceAll("/", "\\\\"));
		}
		InputConfiguration input = InputConfiguration.initialize(init_properties);
		input.setUseWorkingDirectory(true);
		input.setVerbose(true);
		try {
			DSpot dspot = new DSpot(input);
			dspot.amplifyAllTests();
		} catch (Exception e) {
			build.setResult(Result.UNSTABLE);
			LOGGER.error("Build Failed", e);
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

	}

}