package eu.stamp_project.cicd.utils.botsing;

import java.io.File;
import java.io.PrintStream;
import java.util.Collections;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.PrintStreamHandler;

/**
 * Invoke botsing maven plugin, using java maven invoker.
 * @author Pierre-Yves Gibello - OW2
 */
public class BotsingInvoker {

    /**
     * Run botsing (by invoking maven plugin)
     * @param pomPath Path to the project pom
     * @param botsingVersion Botsing version (eg. 1.0.4-SNAPSHOT), null for default
     * @param crashLog Path to crash log file
     * @param nbFrames Number of exception frames to analyze (ignored if 0 or less)
     * @param testDir Directory where tests are generated (default "crash-reproduction-tests" in POM dir)
     * @param out Output stream for maven invocation logs (null for stdout)
     * @return The maven invocation exit code (0 for success), -1 upon invocation error.
     */
    public static int runBotsing(String pomPath, String botsingVersion,
    		String crashLog, int nbFrames, String testDir, PrintStream out) {
    	if(out == null) out = System.out;
    	InvocationRequest request = new DefaultInvocationRequest();
    	request.setPomFile(new File(pomPath));
    	request.setBatchMode(true);
    	StringBuilder goal = new StringBuilder("eu.stamp-project:botsing-maven:");
    	if(botsingVersion != null && botsingVersion.trim().length() > 0) {
    		goal.append(botsingVersion + ":");
    	}
    	goal.append("botsing");
    	request.setGoals(Collections.singletonList(goal.toString()));
    	StringBuilder opts = new StringBuilder("-Dcrash_log=" + crashLog);
    	if(nbFrames > 0) {
    		opts.append(" -Dtarget_frame=" + nbFrames);
    	}
    	if(testDir != null && testDir.trim().length() > 0) {
    		opts.append(" -Dtest_dir=" + testDir);
    	}
    	request.setMavenOpts(opts.toString());
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
	 * Find first test in directory tree (ends with "Test.java")
	 * @param testdir The root dir for search
	 * @return The 1st test file found, null if no test.
	 */
	public static File findGeneratedTest(File testdir) {
		File list[] = testdir.listFiles();
		if(list == null) return null;
		File ret = null;
		for(File f : list) {
			if(f.isFile() && f.getName().endsWith("Test.java")) ret = f;
			else if(f.isDirectory()) ret = findGeneratedTest(f);
			if (ret != null) return ret;
		}
		return null;
	}

}
