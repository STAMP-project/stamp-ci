package eu.stamp_project.cicd.utils.dspot;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.PrintStreamHandler;

import eu.stamp_project.cicd.utils.misc.FileUtils;

/**
 * Invoke dspot maven plugin, using java maven invoker.
 * @author Pierre-Yves Gibello - OW2
 */
public class DspotInvoker {

	protected String pomPath;
	protected String testCriterion = null;	// Default PitMutantScoreSelector
	protected boolean newTestClass = true;	// Test classes to have new names (no overwriting)
	protected String test;					// Test class(es) DSpot will apply to (default all)
	protected int iterations = -1;			// Number of DSpot iterations
	protected Properties configuration;
	protected String dspotVersion;

	/**
	 * Create a new DSpot invoker.
	 * @param pomPath Path to the pom.xml file
	 * @param dspotVersion DSpot version (eg. "2.1.1-SNAPSHOT"), null for default installed version.
	 */
	public DspotInvoker(String pomPath, String dspotVersion) {
		this(pomPath, dspotVersion, true);
	}

	/**
	 * Create a new DSpot invoker.
	 * @param pomPath Path to the pom.xml file
	 * @param dspotVersion DSpot version (eg. "2.1.1-SNAPSHOT"), null for default installed version.
	 * @param newTestClass true to generate test classes with new names (no risk of file overwriting)
	 */
	public DspotInvoker(String pomPath, String dspotVersion, boolean newTestClass) {
		this.pomPath = pomPath;
		this.dspotVersion = dspotVersion;
		this.newTestClass = newTestClass;
		this.configuration = new Properties();
		this.configuration.setProperty("src", "src/main/java");
		this.configuration.setProperty("testSrc", "src/test/java");
	}
    
    /**
     * Sets target module for DSpot
     * @param targetModule The target module
     * @return this invoker
     */
    public DspotInvoker withTargetModule(String targetModule) {
    	this.configuration.setProperty("targetModule", targetModule);
    	return this;
    }
    
    /**
     * Sets output directory for DSpot (override default target/dspot)
     * @param outDir The directory where DSpot outputs will be stored
     * @return this invoker
     */
    public DspotInvoker withOutputDir(String outDir) {
    	this.configuration.setProperty("outputDirectory", outDir);
    	return this;
    }
    
    /**
     * Sets test directory for DSpot (override default src/test/java)
     * @param testDir The test dir, root of JUnit tests
     * @return this invoker
     */
    public DspotInvoker withTestDir(String testDir) {
    	this.configuration.setProperty("testSrc", testDir);
    	return this;
    }
    
    /**
     * Sets source directory for DSpot (override default src/main/java)
     * @param testDir The source dir, root of java source code
     * @return this invoker
     */
    public DspotInvoker withSourceDir(String sourceDir) {
    	this.configuration.setProperty("src", sourceDir);
    	return this;
    }
    
    /**
     * Sets Jacoco coverage selector for DSpot (override default PitMutantScoreSelector)
     * @return this invoker
     */
    public DspotInvoker withJacocoCriterion() {
    	this.testCriterion = "JacocoCoverageSelector";
    	return this;
    }

    /**
     * Focuses DSpot on specific test class(es)
     * @param test The test class(es) to focus DSpot on - pipe-separated if multiple
     * @return this invoker
     */
    public DspotInvoker withTest(String test) {
    	this.test = test;
    	return this;
    }
    
    /**
     * Sets the number of DSpot iterations
     * @param iterations Number of iterations
     * @return this invoker
     */
    public DspotInvoker withIterations(int iterations) {
    	this.iterations = (iterations > 0 ? iterations : -1);
    	return this;
    }
    
    /**
     * Runs DSpot using maven
     * @param additionalOpts Additional maven options (-Doption=value ...)
     * @param out Stream where to print logs (null for silent)
     * @return The maven invocation exit code (0 for success), -1 upon invocation error.
     * @throws IOException
     */
    public int runDspot(String additionalOpts, PrintStream out) throws IOException {
    	if(out == null) out = System.out;
    	InvocationRequest request = new DefaultInvocationRequest();
    	request.setPomFile(new File(pomPath));
    	request.setBaseDirectory((new File(pomPath)).getParentFile());
    	// "project" property has to be set... otherwise a "null" is inserted by DSpot in the classpath.
    	this.configuration.setProperty("project", request.getBaseDirectory().getAbsolutePath());
    	request.setBatchMode(true);

    	request.setGoals(Collections.singletonList(buildMavenGoal()));

    	String opts = buildMavenOpts(additionalOpts);
    	if(opts != null) request.setMavenOpts(opts);

    	Invoker invoker = new DefaultInvoker();
    	invoker.setOutputHandler(new PrintStreamHandler(out, true));
    	try {
    		InvocationResult result = invoker.execute(request);
			return result.getExitCode();
		} catch (MavenInvocationException e) {
			e.printStackTrace(out);
			return -1;
		}
    }

    /**
     * Provides maven command that corresponds to this DSpot invoker's state
     * @param additionalOpts Additional java opts (-Doption=value ...)
     * @return The maven command for this DSpot instance
     */
    public String buildMavenCommand(String additionalOpts) {
    	String opts = null;
    	try {
			opts = buildMavenOpts(additionalOpts);
		} catch (IOException e) {
			opts = null;
		}
    	return "mvn " + buildMavenGoal() + (opts == null ? "" : " " + opts);
    }

    /**
     * Builds DSpot maven goal for this invoker instance
     * @return A DSpot maven goal
     */
    private String buildMavenGoal() {
    	StringBuilder goal = new StringBuilder("eu.stamp-project:dspot-maven:");
    	if(this.dspotVersion != null && this.dspotVersion.trim().length() > 0) {
    		goal.append(dspotVersion + ":");
    	}
    	goal.append("amplify-unit-tests");
    	return goal.toString();
    }
    
    /**
     * Builds DSpot maven options string for this invoker instance
     * @param additionalOpts Additional java opts (-Doption=value ...)
     * @return DSpot maven options
     * @throws IOException
     */
    private String buildMavenOpts(String additionalOpts) throws IOException {
    	StringWriter confString = new StringWriter(128);
		this.configuration.store(confString, null);
    	String confFile = FileUtils.tempFile(confString.toString());
    	StringBuilder opts = new StringBuilder("-Dpath-to-properties=" + confFile);
    	if(this.test != null) opts.append(" -Dtest=" + this.test);
    	if(this.testCriterion != null) opts.append(" -Dtest-criterion=" + this.testCriterion);
    	if(this.newTestClass) opts.append(" -Dgenerate-new-test-class=true");
    	if(this.iterations > 0) opts.append(" -Diteration=" + this.iterations);
    	opts.append(" -Dproject=" + (new File(pomPath)).getParent());

    	if(additionalOpts != null && additionalOpts.length() >= 5) { // Minimal length -Dx=y (5 characters)
    		opts.append(" " + additionalOpts);
    	}
    	
    	return opts.toString();
    }
	
	//TODO remove main
	public static void main(String args[]) throws Exception {
		//DspotInvoker invoker = new DspotInvoker("/home/gibello/STAMP/STAMP-project/stamp-ci/stamp-cicd-utils/pom.xml", "2.1.1-SNAPSHOT");
		DspotInvoker invoker = new DspotInvoker("/tmp/joram-test/joram/joram/mom/core/pom.xml", "2.1.1-SNAPSHOT")
				.withOutputDir("src/test/java")
				.withJacocoCriterion()
				.withIterations(1)
				.withTest("org.objectweb.joram.mom.notifications.TopicForwardNotTest");
		System.out.println("MAVEN COMMAND=" + invoker.buildMavenCommand(null));
		//invoker.runDspot("-Djavax.xml.accessExternalSchema=all", System.out);
		invoker.runDspot(null, System.out);
	}

}
