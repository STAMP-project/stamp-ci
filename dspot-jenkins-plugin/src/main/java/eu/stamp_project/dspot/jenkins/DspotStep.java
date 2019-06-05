package eu.stamp_project.dspot.jenkins;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.jenkins.report.DSpotResults;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.utils.options.AmplifierEnum;
import eu.stamp_project.utils.options.BudgetizerEnum;
import eu.stamp_project.utils.options.SelectorEnum;
import eu.stamp_project.utils.program.ConstantsProperties;
import eu.stamp_project.utils.program.InputConfiguration;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;

public class DspotStep extends Builder implements SimpleBuildStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(DspotStep.class);

	@Nonnull
	private String projectPath = DescriptorImpl.defaultProjectPath;
	@Nonnull
	private String srcCode = DescriptorImpl.defaultSrcCode;
	@Nonnull
	private String testCode = DescriptorImpl.defaultTestCode;
	@Nonnull
	private String srcClasses = DescriptorImpl.defaultSrcClasses;
	@Nonnull
	private String testClasses = DescriptorImpl.defaultTestClasses;
	@Nonnull
	private String testFilter = DescriptorImpl.defaultTestFilter;
	@Nonnull
	private String outputDir = DescriptorImpl.defaultOutputDir;
	@Nonnull
	private String secondFolder = DescriptorImpl.defaultSecondFolder;
	@Nonnull
	private String mvnHome = DescriptorImpl.defaultMvnHome;

	private boolean onlyChanges = false;

	private boolean showReports = true;

	@Nonnull
	private String selector = DescriptorImpl.defaultSelector;

	@Nonnull
	private String budgetizer = DescriptorImpl.defaultBudgetizer;

	@Nonnull
	private int numIterations = DescriptorImpl.defaultNumIterations;

	@Nonnull
	private String amplifiers = DescriptorImpl.defaultAmplifiers;

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

	public boolean isShowReports() {
		return showReports;
	}

	public String getBudgetizer() {
		return budgetizer;
	}

	public String getSecondFolder() {
		return secondFolder;
	}

	public String getSelector() {
		return selector;
	}

	public String getAmplifiers() {
		return amplifiers;
	}

	public int getNumIterations() {
		return numIterations;
	}

	public String getMvnHome() {
		return mvnHome;
	}

	@DataBoundSetter
	public void setOnlyChanges(boolean onlyChanges) {
		this.onlyChanges = onlyChanges;
	}

	@DataBoundSetter
	public void setShowReports(boolean showReports) {
		this.showReports = showReports;
	}

	@DataBoundSetter
	public void setOutputDir(@Nonnull String outputDir) {
		this.outputDir = outputDir;
	}

	@DataBoundSetter
	public void setProjectPath(@Nonnull String filepath) {
		projectPath = filepath;
	}

	@DataBoundSetter
	public void setSrcClasses(@Nonnull String srcClasses) {
		this.srcClasses = srcClasses;
	}

	@DataBoundSetter
	public void setSrcCode(@Nonnull String srcCode) {
		this.srcCode = srcCode;
	}

	@DataBoundSetter
	public void setTestClasses(@Nonnull String testClasses) {
		this.testClasses = testClasses;
	}

	@DataBoundSetter
	public void setTestCode(@Nonnull String testCode) {
		this.testCode = testCode;
	}

	@DataBoundSetter
	public void setTestFilter(@Nonnull String testFilter) {
		this.testFilter = testFilter;
	}

	@DataBoundSetter
	public void setBudgetizer(@Nonnull String budgetizer) {
		this.budgetizer = budgetizer;
	}

	@DataBoundSetter
	public void setSelector(@Nonnull String selector) {
		this.selector = selector;
	}

	@DataBoundSetter
	public void setAmplifiers(@Nonnull String amplifiers) {
		this.amplifiers = amplifiers;
	}

	@DataBoundSetter
	public void setNumIterations(int numIterations) {
		this.numIterations = numIterations;
	}

	@DataBoundSetter
	public void setSecondFolder(@Nonnull String secondFolder) {
		this.secondFolder = secondFolder;
	}

	@DataBoundSetter
	public void setMvnHome(@Nonnull String mvnHome) {
		this.mvnHome = mvnHome;
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void perform(Run<?, ?> run, FilePath wsp, Launcher arg2, TaskListener listener)
			throws InterruptedException, IOException {
		init_properties.setProperty(ConstantsProperties.PROJECT_ROOT_PATH.getName(),
				new FilePath(wsp, projectPath).getRemote());
		init_properties.setProperty(ConstantsProperties.SRC_CODE.getName(), new FilePath(wsp, srcCode).getRemote());
		init_properties.setProperty(ConstantsProperties.TEST_SRC_CODE.getName(),
				new FilePath(wsp, testCode).getRemote());
		init_properties.setProperty(ConstantsProperties.OUTPUT_DIRECTORY.getName(),
				new FilePath(wsp, outputDir).getRemote());
		init_properties.setProperty(ConstantsProperties.FILTER.getName(), testFilter);
		init_properties.setProperty(ConstantsProperties.TEST_CLASSES.getName(),
				new FilePath(wsp, testClasses).getRemote());
		init_properties.setProperty(ConstantsProperties.SRC_CLASSES.getName(),
				new FilePath(wsp, srcClasses).getRemote());
		init_properties.setProperty(ConstantsProperties.PATH_TO_SECOND_VERSION.getName(),
				new FilePath(wsp, secondFolder).getRemote());
		init_properties.setProperty(ConstantsProperties.MAVEN_HOME.getName(),
				new FilePath(wsp, getMvnHome()).getRemote());

		InputConfiguration.initialize(init_properties).setUseWorkingDirectory(true).setVerbose(true);

		// analyze changes in workspace
		List<String> testList = new ArrayList<>();

		if (onlyChanges) {
			ChangeLogSet<? extends Entry> changes = null;
			if (AbstractBuild.class.isInstance(run)) {
				AbstractBuild build = (AbstractBuild) run;
				changes = build.getChangeSet();
			} else {
				try {
					// checking for WorkflowRun's getChangeSets method
					List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeSets = (List<ChangeLogSet<? extends ChangeLogSet.Entry>>) run
							.getClass().getMethod("getChangeSets").invoke(run);
					if (!changeSets.isEmpty()) {
						changes = changeSets.get(0);
					}
				} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
					// ignore
				}
			}
			if (changes != null) {
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
			} else {
				listener.getLogger().println("could not get last changes. DSpot will run on the whole suite.");
				onlyChanges = false;
			}
		}

		try {
			String[] amplifiersArray = this.amplifiers.split(":");
			List<Amplifier> amplifiers = AmplifierEnum.buildAmplifiersFromString(Arrays.asList(amplifiersArray));
			TestSelector testSelector = SelectorEnum.valueOf(selector).buildSelector();
			DSpot dspot = new DSpot(numIterations, amplifiers, testSelector, BudgetizerEnum.valueOf(budgetizer));
			InputConfiguration.get().getFactory().getEnvironment()
					.setInputClassLoader(DspotStep.class.getClassLoader());
			if (onlyChanges) {
				dspot.amplifyTestClasses(testList.stream().map(this::pathToQualifiedName).collect(Collectors.toList()));
			} else
				dspot.amplifyAllTests();
		} catch (Exception e) {
			listener.getLogger()
					.println("There was an error running DSpot on your project. Check the logs for details.");
			LOGGER.error("Build Failed", e);
			run.setResult(Result.UNSTABLE);
		}

		DSpotResults results = new DSpotResults(new FilePath(wsp, outputDir));
		DSpotResultsAction action = new DSpotResultsAction(run, results);
		run.addAction(action);
	}

	public String pathToQualifiedName(String path) {
		String regex = File.separator.equals("/") ? "/" : "\\\\";
		return path.substring(getTestCode().length(), path.length() - ".java".length()).replaceAll(regex, ".");
	}

	public static final Function<String, String> shouldUpdatePath = string -> File.separator.equals("/") ? string
			: string.replaceAll("/", "\\\\");

	@Symbol("dspot")
	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
		public static final String defaultMvnHome = ConstantsProperties.MAVEN_HOME.getDefaultValue();
		public static final String defaultSecondFolder = "";
		public static final String defaultAmplifiers = "None";
		public static final String defaultSelector = SelectorEnum.PitMutantScoreSelector.toString();
		public static final int defaultNumIterations = 3;
		public static final String defaultBudgetizer = BudgetizerEnum.NoBudgetizer.toString();
		public static final String defaultProjectPath = "";
		public static final String defaultSrcCode = ConstantsProperties.SRC_CODE.getDefaultValue();
		public static final String defaultTestCode = ConstantsProperties.TEST_SRC_CODE.getDefaultValue();
		public static final String defaultSrcClasses = ConstantsProperties.SRC_CLASSES.getDefaultValue();
		public static final String defaultTestClasses = ConstantsProperties.TEST_CLASSES.getDefaultValue();
		public static final String defaultTestFilter = ConstantsProperties.FILTER.getDefaultValue();
		public static final String defaultOutputDir = "dspot-out";
		public static final boolean defaultOnlyChanges = false;
		public static final boolean defaultShowReports = true;

		public DescriptorImpl() {
			super();
			load();
		}

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