package eu.stamp_project.descartes.gitlab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.gitlab4j.api.models.Issue;
import org.pitest.coverage.TestInfo;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationResultListener;

import eu.stamp_project.cicd.utils.dspot.DspotInvoker;
import eu.stamp_project.cicd.utils.git.GitlabIssueManager;
import eu.stamp_project.cicd.utils.misc.FileUtils;


/**
 * Listener for Descartes/Pitest GITLAB-ISSUES reporting
 * Requires to set fullMutationMatrix=true so Descartes runs full test grid.
 * @author Pierre-Yves Gibello - OW2
 *
 */
public class MutationReportListener implements MutationResultListener {

	Properties configuration;
	Properties gitlabConfig;
	ListenerArguments listenerArguments;
	IssueLogger out;
	int coverage = 0;
	public static boolean underTest = false;

	public MutationReportListener(Properties props, ListenerArguments args) {
		this.configuration = props;
		this.listenerArguments = args;
		this.coverage = args.getCoverage().createSummary().getCoverage();
	}

	public void runStart() {
		MutationReportListener.underTest = true;
		System.out.println("**** STAMP MutationReportListener::runStart()");
		this.configuration.list(System.out);

		// Retrieve Gitlab configuration, if any
		String gitlabToken = configuration.getProperty("gitlabToken");
		String gitlabUrl = configuration.getProperty("gitlabUrl");
		String gitlabProject = configuration.getProperty("gitlabProject");
		if(gitlabToken != null && gitlabUrl != null &&gitlabProject != null) {
			this.gitlabConfig = new Properties();
			gitlabConfig.setProperty("gitlab.token", gitlabToken);
			gitlabConfig.setProperty("gitlab.url", gitlabUrl);
			gitlabConfig.setProperty("gitlab.project", gitlabProject);
		}
		
		this.out = new IssueLogger().open();
		//this.listenerArguments.isFullMutationMatrix()
	}

	public void handleMutationResult(ClassMutationResults results) {
		LinkedHashSet<String> succeedingTestClasses =new LinkedHashSet<String>();
		for (MutationResult mutation : results.getMutations()) {
			List<String> succeedingTests = null;

			// Retrieve succeeding tests list
			// In "full matrix" mode, returned by getSucceedingTests()
			// Otherwise, assume the list of tests run all pass when mutation survived...
			if(this.listenerArguments.isFullMutationMatrix()) {
				succeedingTests = mutation.getSucceedingTests();
			} else if (mutation.getStatus() == DetectionStatus.SURVIVED) {
				List<TestInfo> succeedingTestsInfo = mutation.getDetails().getTestsInOrder();
				for(TestInfo testInfo : succeedingTestsInfo) {
					if(succeedingTests == null) succeedingTests = new LinkedList<String>();
					succeedingTests.add(testInfo.getName());
				}
			}

			if(succeedingTests != null && succeedingTests.size() > 0) {
				out.log(mutation.getStatus(), "==========================================================================");
				if(mutation.getStatus() == DetectionStatus.SURVIVED) {
					out.logSurvived("CRITICAL TEST FAILURE: test suite GREEN upon code mutation");
				} else {
					out.logKilled("Minor test failure: some test(s) do not detect code mutation");
				}
				out.log(mutation.getStatus(),
						"In class " + mutation.getDetails().getClassName()
						+ ", method " +  mutation.getDetails().getMethod()
						+ " (line " + mutation.getDetails().getLineNumber()
						+ ") was updated as follows:");
				out.log(mutation.getStatus(), "\t" + mutation.getDetails().getDescription());
				out.log(mutation.getStatus(),
						"\t" + "The following test(s) still PASS:");

				for(String succeedingtest : succeedingTests) {
					out.log(mutation.getStatus(), "\t\t" + succeedingtest);

					// Keep track of succeeding test class
					// Format of "succedingtest" is testClass.testMethod(testClass)
					String testClass = succeedingtest.substring(
							succeedingtest.indexOf("(") + 1,
							succeedingtest.length() -1);
					succeedingTestClasses.add(testClass);
				}

				List<String> killingTests = mutation.getKillingTests();
				if(killingTests != null && killingTests.size() > 0) {
					out.log(mutation.getStatus(), "\t" + "The following test(s) DETECT the issue:");
					for(String killingtest : killingTests) {
						out.log(mutation.getStatus(), "\t\t" + killingtest);
					}
				}
				
				out.log(mutation.getStatus(), "==========================================================================");
				
			} else if(mutation.getStatus() == DetectionStatus.NO_COVERAGE) {
				out.logNoCoverage("==========================================================================");
				out.logNoCoverage("Missing test: no coverage");
				out.logNoCoverage("In class " + mutation.getDetails().getClassName()
						+ ", method " +  mutation.getDetails().getMethod()
						+ " (line " + mutation.getDetails().getLineNumber()
						+ ") was updated as follows:");
				out.logNoCoverage("\t" + mutation.getDetails().getDescription());
				out.logNoCoverage("\t" + "No test provided, no chance to detect any bug here.");
				out.logNoCoverage("==========================================================================");
			}
		}
		
		//TODO focus DSpot on this case ?
		if(! succeedingTestClasses.isEmpty()) {
			DspotInvoker dspot = new DspotInvoker(null, "2.1.1-SNAPSHOT")
				.withPersistentConfig(true)
				.withOutputDir("src/test/java")
				.withJacocoCriterion()
				.withIterations(1);

			out.log(DetectionStatus.SURVIVED, "Suggested DSpot fix(es):");
			for(String testClass : succeedingTestClasses) {
				dspot.withTest(testClass);
				out.log(DetectionStatus.SURVIVED,
					dspot.buildMavenCommand(null));
			}
		}
	}

	public void runEnd() {
		System.out.println("**** STAMP MutationReportListener::runEnd()");
		
		this.out.close();
		
		/*
		 * PIT APIs do not provide access to the output directory (only the output writer !)
		 * The only way to gain access to the file is writing it somewhere else...
		 * then copying it to the output dir using the provided writer ! 
		 */
		if(out.getSurvivedLog() != null) {
			copyFileToPrintWriter(out.getSurvivedLog(),
				new PrintWriter(this.listenerArguments.getOutputStrategy()
						.createWriterForFile("git_critical_issue.txt")));
		}
		if(out.getKilledLog() != null) {
			copyFileToPrintWriter(out.getKilledLog(),
				new PrintWriter(this.listenerArguments.getOutputStrategy()
						.createWriterForFile("git_minor_issue.txt")));
		}
		if(out.getNoCoverageLog() != null) {
			copyFileToPrintWriter(out.getNoCoverageLog(),
				new PrintWriter(this.listenerArguments.getOutputStrategy()
						.createWriterForFile("git_no_coverage_issue.txt")));
		}
		
		// Generate Gitlab issue(s) if requested
		Issue criticalIssue = findIssueByTitle("CRITICAL (STAMP generated)");
		Issue minorIssue = findIssueByTitle("MINOR (STAMP generated)");
		try {
			if(criticalIssue != null) {
				if(out.getSurvivedLog() != null) {
					GitlabIssueManager.updateIssue(this.gitlabConfig,
						criticalIssue.getIid(), criticalIssue.getTitle(),
						gitlabIssueSummary(FileUtils.fileToString(new File(out.getSurvivedLog()))));
				} else {
					GitlabIssueManager.deleteIssue(gitlabConfig, criticalIssue.getIid());
				}

			} else {
				if(out.getSurvivedLog() != null) {
					GitlabIssueManager.createIssue(this.gitlabConfig,
						"CRITICAL (STAMP generated): Test suite green when code removed",
						gitlabIssueSummary(FileUtils.fileToString(new File(out.getSurvivedLog()))));
				}
			}
			
			if(minorIssue != null) {
				if(out.getKilledLog() != null) {
					GitlabIssueManager.updateIssue(this.gitlabConfig,
						minorIssue.getIid(), minorIssue.getTitle(),
						gitlabIssueSummary(FileUtils.fileToString(new File(out.getKilledLog()))));
				} else {
					GitlabIssueManager.deleteIssue(gitlabConfig, minorIssue.getIid());
				}

			} else {
				if(out.getKilledLog() != null) {
					GitlabIssueManager.createIssue(this.gitlabConfig,
						"MINOR (STAMP generated): Some test(s) do(es) not detect changes in code",
						gitlabIssueSummary(FileUtils.fileToString(new File(out.getKilledLog()))));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
		
		MutationReportListener.underTest = false;
		
	}
	
	private String gitlabIssueSummary(String issueContent) {
		StringBuilder summary = new StringBuilder(
				"When some methods are emptied or their content is replaced by a single \"return\" statement, the test suite detects nothing and passes.\n\n"
				+ "In other words, massive code removal is not detected by JUnit tests: global mutation coverage is "
				+ this.listenerArguments.getCoverage().createSummary().getCoverage() + "\n\n"
				+ "Detailed report of which classe(s) / test(s) are concerned follows:\n");

		if(issueContent != null) {
			summary.append("\n```\n" + issueContent + "\n```\n");
		}
		
		return summary.toString();
	}
	
	
	private Issue findIssueByTitle(String prefix) {
		if(this.gitlabConfig != null && prefix != null && prefix.length() > 1) {
			try {
				List<Issue> issues = GitlabIssueManager.listIssues(this.gitlabConfig, GitlabIssueManager.ISSUE_OPENED);
				
				if(issues != null) {
					for(Issue issue : issues) {
						if(issue.getTitle().startsWith(prefix)) {
							return issue;
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(System.err);
			}
		}
		return null;
	}
	

	/**
	 * Utilitary method to copy a file to another location
	 * @param path The source file path
	 * @param out The destination writer where to copy the file
	 */
	private void copyFileToPrintWriter(String path, PrintWriter out) {
		if(path == null || out == null) return;
		File file = new File(path);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			String line;
			while((line = in.readLine()) != null) {
				out.println(line);
			}
		} catch (Exception ignore) {
		} finally {
			if(in != null)
				try { in.close(); } catch (IOException ignore) { }
		}
		out.close();
	}
}

/**
 * Log issues, sorted by severity level:
 * Critical, major and minor issues are logged into different files.
 * @author Pierre-Yves Gibello - OW2
 *
 */
class IssueLogger {

	boolean started;
	File survived = null, killed = null, noCoverage = null;
	PrintWriter survivedReport = null, killedReport = null, noCoverageReport = null;
	boolean hasSurvived = false, hasKilled = false, hasNoCoverage = false;
	
	/**
	 * Prepares for issues logging
	 * @return This logger
	 */
	public IssueLogger open() {
		if(! this.started) {
			try {
				this.survived = File.createTempFile("survived", null);
				this.survivedReport = new PrintWriter(this.survived);
			} catch (IOException e) {
				if(this.survivedReport != null) this.survivedReport.close();
				this.survived = null;
			}
			try {
				this.killed = File.createTempFile("killed", null);
				this.killedReport = new PrintWriter(this.killed);
			} catch (IOException e) {
				if(this.killedReport != null) this.killedReport.close();
				this.killed = null;
			}
			try {
				this.noCoverage = File.createTempFile("nocoverage", null);
				this.noCoverageReport = new PrintWriter(this.noCoverage);
			} catch (IOException e) {
				if(this.noCoverageReport != null) this.noCoverageReport.close();
				this.noCoverage = null;
			}
			this.started = true;
		}
		return this;
	}

	/**
	 * Terminates issue logging
	 */
	public void close() {
		if(this.survivedReport != null) this.survivedReport.close();
		if(this.killedReport != null) this.killedReport.close();
		if(this.noCoverageReport != null) this.noCoverageReport.close();
		this.started = false;
	}
	
	/**
	 * Logs issue to corresponding file, according to severity
	 * @param what Issue severity (according to detection status)
	 * @param data Issue content, to be logged
	 */
	public void log(DetectionStatus what, String data) {
		if(what == DetectionStatus.SURVIVED) {
			logSurvived(data);
		} else if(what == DetectionStatus.KILLED) {
			logKilled(data);
		} else if(what == DetectionStatus. NO_COVERAGE) {
			logNoCoverage(data);
		}
	}
	
	/**
	 * Logs "survived" issue (critical)
	 * @param data Issue content, to be logged
	 */
	public void logSurvived(String data) {
		log(this.survivedReport, data);
		hasSurvived = true;
	}
	
	/**
	 * Logs "killed" issue (major)
	 * @param data Issue content, to be logged
	 */
	public void logKilled(String data) {
		log(this.killedReport, data);
		hasKilled = true;
	}
	
	/**
	 * Logs "no coverage" issue (minor)
	 * @param data Issue content, to be logged
	 */
	public void logNoCoverage(String data) {
		log(this.noCoverageReport, data);
		hasNoCoverage = true;
	}
	
	/**
	 * Retrieves path to "survived" issues log
	 * @return The absolute path to the log
	 */
	public String getSurvivedLog() {
		if(this.survived != null && hasSurvived) return this.survived.getAbsolutePath();
		else return null;
	}
	
	/**
	 * Retrieves path to "killed" issues log
	 * @return The absolute path to the log
	 */
	public String getKilledLog() {
		if(this.killed != null && hasKilled) return this.killed.getAbsolutePath();
		else return null;
	}
	
	/**
	 * Retrieves path to "no coverage" issues log
	 * @return The absolute path to the log
	 */
	public String getNoCoverageLog() {
		if(this.noCoverage != null && hasNoCoverage) return this.noCoverage.getAbsolutePath();
		else return null;
	}

	/**
	 * Logs data to a given writer
	 * @param writer The writer to log data on
	 * @param data The logged data
	 */
	private void log(PrintWriter writer, String data) {
		if(writer != null) {
			writer.println(data);
		}
	}
}
