package eu.stamp_project.dspot.jenkins;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.stamp_project.Main;
import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.jenkins.report.DSpotResults;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.utils.collector.CollectorConfig;
import eu.stamp_project.utils.options.AmplifierEnum;
import eu.stamp_project.utils.options.BudgetizerEnum;
import eu.stamp_project.utils.options.SelectorEnum;
import eu.stamp_project.utils.program.ConstantsProperties;
import eu.stamp_project.utils.program.InputConfiguration;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import spoon.reflect.declaration.CtType;

public class DspotStep extends Builder implements SimpleBuildStep {

	private static final String DEFAULT_PROJECT_PATH = "";
	private static final String OUTPUT_DIRECTORY = "dspot-out/";
	private static final int DEFAULT_NUM_ITERATIONS = 3;
	private static final Logger LOGGER = LoggerFactory.getLogger(DspotStep.class);

	// properties fields
	@Nonnull
	private String projectPath = DescriptorImpl.defaultProjectPath;
	@Nonnull
	private String srcCode = DescriptorImpl.defaultSrcCode;
	@Nonnull
	private String testCode = DescriptorImpl.defaultTestCode;
	@Nonnull
	private String pitVersion = DescriptorImpl.defaultPitVersion;
	@Nonnull
	private String testPitFilterClassesToKeep = DescriptorImpl.defaultPitFilterClassesToKeep;
	@Nonnull
	private String outputDir = DescriptorImpl.defaultOutputDir;

	// optional fields
	@Nonnull
	private String srcClasses = DescriptorImpl.defaultSrcClasses;
	@Nonnull
	private String testClasses = DescriptorImpl.defaultTestClasses;

	// advanced options
	@Nonnull
	private String secondFolder = DescriptorImpl.defaultSecondFolder;
	@Nonnull
	private String mvnHome = DescriptorImpl.defaultMvnHome;
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

	public String getPitFilterClassesToKeep() {
		return testPitFilterClassesToKeep;
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
	public void setPitFilterClassesToKeep(@Nonnull String testPitFilterClassesToKeep) {
		this.testPitFilterClassesToKeep = testPitFilterClassesToKeep;
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

	@SuppressWarnings({ "static-access" })
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
		init_properties.setProperty(ConstantsProperties.PIT_FILTER_CLASSES_TO_KEEP.getName(),
				testPitFilterClassesToKeep);

		init_properties.setProperty(ConstantsProperties.SRC_CLASSES.getName(),
				new FilePath(wsp, srcClasses).getRemote());
		init_properties.setProperty(ConstantsProperties.TEST_CLASSES.getName(),
				new FilePath(wsp, testClasses).getRemote());
		init_properties.setProperty(ConstantsProperties.PATH_TO_SECOND_VERSION.getName(),
				new FilePath(wsp, secondFolder).getRemote());
		init_properties.setProperty(ConstantsProperties.MAVEN_HOME.getName(),
				new FilePath(wsp, getMvnHome()).getRemote());

		InputConfiguration.initialize(init_properties).setUseWorkingDirectory(true).setVerbose(true);

		try {

			// amplifiers option
			String[] amplifiersArray = this.amplifiers.split(":");

			// cleaning amplifyArray
			int size = amplifiersArray.length;
			for (int i = 0; i < size; i++) {
				amplifiersArray[i] = amplifiersArray[i].trim();
				if (amplifiersArray[i].isEmpty()) {
					amplifiersArray[i] = DescriptorImpl.defaultAmplifiers;
				}
			}
			List<Amplifier> amplifiers = AmplifierEnum.buildAmplifiersFromString(Arrays.asList(amplifiersArray));

			// selector option
			TestSelector testSelector = SelectorEnum.valueOf(selector).buildSelector();

			// run DSpot
			DSpot dspot = new DSpot(numIterations, amplifiers, testSelector, BudgetizerEnum.valueOf(budgetizer));
			InputConfiguration.get().getFactory().getEnvironment()
					.setInputClassLoader(DspotStep.class.getClassLoader());

			final List<CtType<?>> amplifiedTestClasses;
			final long startTime = System.currentTimeMillis();

			if (InputConfiguration.get().getTestClasses().isEmpty()
					|| "all".equals(InputConfiguration.get().getTestClasses().get(0))) {
				amplifiedTestClasses = dspot.amplifyAllTests();
			} else {
				// TODO amplifyTestClassesTestMethods needs to
				// FULL_QUALIFIED_NAME_TEST_CLASS
				amplifiedTestClasses = dspot.amplifyTestClassesTestMethods(InputConfiguration.get().getTestClasses(),
						InputConfiguration.get().getTestCases());
			}

			LOGGER.info("Amplification {}.", amplifiedTestClasses.isEmpty() ? "failed" : "succeed");
			final long elapsedTime = System.currentTimeMillis() - startTime;
			LOGGER.info("Elapsed time {} ms", elapsedTime);

			// global report handling
			Main.GLOBAL_REPORT.output();
			Main.GLOBAL_REPORT.reset();
			// Send info collected.
			CollectorConfig.getInstance().getInformationCollector().sendInfo();

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

	@Symbol("dspot")
	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
		// properties fields
		public static final String defaultProjectPath = DEFAULT_PROJECT_PATH;
		public static final String defaultSrcCode = ConstantsProperties.SRC_CODE.getDefaultValue();
		public static final String defaultTestCode = ConstantsProperties.TEST_SRC_CODE.getDefaultValue();
		public static final String defaultPitVersion = ConstantsProperties.PIT_VERSION.getDefaultValue();
		public static final String defaultPitFilterClassesToKeep = ConstantsProperties.PIT_FILTER_CLASSES_TO_KEEP
				.getDefaultValue();
		public static final String defaultOutputDir = OUTPUT_DIRECTORY;

		// optional fields
		public static final String defaultSrcClasses = ConstantsProperties.SRC_CLASSES.getDefaultValue();
		public static final String defaultTestClasses = ConstantsProperties.TEST_CLASSES.getDefaultValue();

		// odvanced options
		public static final String defaultMvnHome = ConstantsProperties.MAVEN_HOME.getDefaultValue();
		public static final String defaultSecondFolder = "";
		public static final String defaultAmplifiers = "None";
		public static final String defaultSelector = SelectorEnum.PitMutantScoreSelector.toString();
		public static final int defaultNumIterations = DEFAULT_NUM_ITERATIONS;
		public static final String defaultBudgetizer = BudgetizerEnum.RandomBudgetizer.toString();

		public DescriptorImpl() {
			super();
			load();
		}

		@Override
		public String getDisplayName() {
			return "STAMP DSpot configuration";
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}
	}

}