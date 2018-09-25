package eu.stamp_project.dspot.jenkins;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.program.ConstantsProperties;
import eu.stamp_project.program.InputConfiguration;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;

public class DspotStep extends Builder {

	private static final Logger LOGGER = LoggerFactory.getLogger(DspotStep.class);

	private String projectPath, srcCode, testCode, srcClasses, testClasses, testFilter, outputDir;

	private boolean onlyChanges;

	private Properties init_properties;

	@DataBoundConstructor
	public DspotStep() {
		init_properties = new Properties();
	}

	public String getOutputDir() {
		return outputDir;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public String getSrcClasses() {
		return srcClasses;
	}

	public String getSrcCode() {
		return srcCode;
	}

	public String getTestClasses() {
		return testClasses;
	}

	public String getTestCode() {
		return testCode;
	}

	public String getTestFilter() {
		return testFilter;
	}

	public boolean isOnlyChanges() {
		return onlyChanges;
	}

	@DataBoundSetter
	public void setOnlyChanges(boolean onlyChanges) {
		this.onlyChanges = onlyChanges;
	}

	@DataBoundSetter
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	@DataBoundSetter
	public void setProjectPath(String filepath) {
		projectPath = filepath;
	}

	@DataBoundSetter
	public void setSrcClasses(String srcClasses) {
		this.srcClasses = srcClasses;
	}

	@DataBoundSetter
	public void setSrcCode(String srcCode) {
		this.srcCode = srcCode;
	}

	@DataBoundSetter
	public void setTestClasses(String testClasses) {
		this.testClasses = testClasses;
	}

	@DataBoundSetter
	public void setTestCode(String testCode) {
		this.testCode = testCode;
	}

	@DataBoundSetter
	public void setTestFilter(String testFilter) {
		this.testFilter = testFilter;
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
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
		init_properties.setProperty(ConstantsProperties.TEST_CLASSES.getName(),
				new FilePath(build.getWorkspace(), testClasses).getRemote());
		init_properties.setProperty(ConstantsProperties.SRC_CLASSES.getName(),
				new FilePath(build.getWorkspace(), srcClasses).getRemote());

		InputConfiguration input;
		input = InputConfiguration.initialize(init_properties);
		input.setUseWorkingDirectory(true);
		input.setVerbose(true);
		

		// analyze changes in workspace
		List<String> testList = new ArrayList<>();
		if (onlyChanges) {
			ChangeLogSet<? extends Entry> changes = build.getChangeSet();
			Iterator<? extends ChangeLogSet.Entry> itrChangeSet = changes.iterator();
			while (itrChangeSet.hasNext()) {
				ChangeLogSet.Entry str = itrChangeSet.next();
				Collection<? extends ChangeLogSet.AffectedFile> affectedFiles = str.getAffectedFiles();
				Iterator<? extends ChangeLogSet.AffectedFile> affectedFilesItr = affectedFiles.iterator();
				while (affectedFilesItr.hasNext()) {
					AffectedFile file = affectedFilesItr.next();
					if (file.getEditType().equals(EditType.ADD) || file.getEditType().equals(EditType.EDIT)) {
						String path = file.getPath();
						if (path.startsWith(shouldUpdatePath.apply(testCode))) {
							testList.add(path);
							listener.getLogger().println("New test found at: " + path);
						}
					}
				}
			}
			if (testList.isEmpty())
				listener.getLogger().println("no tests changed. DSpot will not be run.");
		}

		try {
			DSpot dspot = new DSpot(input);
			input.getFactory().getEnvironment().setInputClassLoader(DspotStep.class.getClassLoader());
			if (onlyChanges) {
				dspot.amplifyAllTestsNames(
						testList.stream()
						.map(this::pathToQualifiedName)
						.collect(Collectors.toList()));
			} else
				dspot.amplifyAllTests();
		} catch (Exception e) {
			listener.getLogger().println("There was an error running DSpot on your project. Check the logs for details.");
			LOGGER.error("Build Failed", e);
			build.setResult(Result.UNSTABLE);
		}
		return true;
	}

	public String pathToQualifiedName(String path) {
		String regex = File.separator.equals("/") ? "/" : "\\\\";
		return path.substring(getTestCode().length(), path.length()-".java".length()).replaceAll(regex, ".");
	}

	public static final Function<String, String> shouldUpdatePath = string -> File.separator.equals("/") ? string
			: string.replaceAll("/", "\\\\");

	@Symbol("dspot")
	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

		public static final String defaultProjectPath = "";
		public static final String defaultSrcCode = ConstantsProperties.SRC_CODE.getDefaultValue();
		public static final String defaultTestCode = ConstantsProperties.TEST_SRC_CODE.getDefaultValue();
		public static final String defaultSrcClasses = ConstantsProperties.SRC_CLASSES.getDefaultValue();
		public static final String defaultTestClasses = ConstantsProperties.TEST_CLASSES.getDefaultValue();
		public static final String defaultTestFilter = ConstantsProperties.FILTER.getDefaultValue();
		public static final String defaultOutputDir = "dspot-out";
		public static final boolean defaultOnlyChanges = false;

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