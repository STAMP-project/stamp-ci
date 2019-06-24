package eu.stamp_project.cicd.utils.git;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gitlab4j.api.Constants;
import org.gitlab4j.api.Constants.IssueState;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Assignee;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.IssueFilter;
import org.gitlab4j.api.models.Note;

/**
 * Manage Gitlab issues.
 * @author Pierre-Yves Gibello - OW2
 */
public class GitlabIssueManager
{
	public static final String ISSUE_OPENED = "OPENED";
	public static final String ISSUE_CLOSED = "CLOSED";
	public static final String ISSUE_REOPENED = "REOPENED";
	
    public static void main(String[] args) throws Exception {
    	String gitlabUrl = "http://localhost";
    	String privateToken = "i_rjH-MM3YuFsvAxCTsH";
    	int iid = 1;
    	List<Issue> issues = GitlabIssueManager.listIssues(gitlabUrl, privateToken, 1, GitlabIssueManager.ISSUE_CLOSED);
    	for (Issue issue : issues) {
    		BufferedReader in = new BufferedReader(new StringReader(issue.getDescription()));
    		List<String> exceptions = eu.stamp_project.cicd.utils.botsing.ExceptionExtractor.extractExceptions(in);
    		if(exceptions != null && exceptions.size() > 0) {
    			System.out.println("Issue:" + issue.getTitle());
    			for(String exception : exceptions) {
    	        	System.out.println("\n========== EXCEPTION FOUND issue=" + issue.getIid() + ": ================\n"
    	        			+ exception);
    	        }
    			System.out.println("\nDetails:" + issue.getDescription());
    			
    			GitlabIssueManager.commentIssue(gitlabUrl, privateToken, 1, iid, "This is a generated comment:\n\n```\nHello world\nHow are you ?\n```");
    		}
    	}
    }


    /**
     * List gitlab issues for a given project
     * @param gitlabUrl Gitlab server URL
     * @param privateToken Gitlab private token for authentication
     * @param projectIdOrPath Gitlab project ID or path
     * @param state Gitlab issue state (one of OPENED, CLOSED, REOPENED)
     * @return The list of gitlabs issues that meet the criteria
     * @throws IOException
     */
    public static List<Issue> listIssues(String gitlabUrl, String privateToken, Object projectIdOrPath, String state) throws IOException {
    	try {
    		return listIssues(new GitLabApi(gitlabUrl, privateToken), projectIdOrPath, IssueState.forValue(state));
    	} catch(GitLabApiException e) {
    		throw new IOException(e);
    	}
    }
    
    /**
     * List gitlab issues for a given project
     * @param gitlabUrl Gitlab server URL
     * @param user Gitlab user for authentication
     * @param password Gitlab password for authentication
     * @param projectIdOrPath Gitlab project ID or path
     * @param state Gitlab issue state (one of OPENED, CLOSED, REOPENED)
     * @return The list of gitlabs issues that meet the criteria
     * @throws IOException
     */
    public static List<Issue> listIssues(String gitlabUrl, String user, String password, Object projectIdOrPath, String state) throws IOException {
    	try {
    		return listIssues(new GitLabApi(gitlabUrl, user, password), projectIdOrPath, IssueState.forValue(state));
    	} catch(GitLabApiException e) {
    		throw new IOException(e);
    	}
    }
    
    /**
     * List gitlab issues for a given project
     * @param properties Gitlab config with gitlab.url, gitlab.token and gitlab.project expected
     * @param state Gitlab issue state (one of OPENED, CLOSED, REOPENED)
     * @return The list of gitlabs issues that meet the criteria
     * @throws IOException
     */
    public static List<Issue> listIssues(Properties gitlabConfig, String state) throws IOException {
    	String gitlabUrl = gitlabConfig.getProperty("gitlab.url");
		String gitlabToken = gitlabConfig.getProperty("gitlab.token");
		String gitlabProject = gitlabConfig.getProperty("gitlab.project");
		if(gitlabUrl == null || gitlabToken == null || gitlabProject == null) {
			throw new IOException("Missing Gitlab URL and/or private token and/or project ID or path in gitlab.properties");
		}
		return listIssues(gitlabUrl, gitlabToken, gitlabProject, state);
    }

    /**
     * 
     * @param gitlabUrl Gitlab server URL
     * @param privateToken Gitlab private token for authentication
     * @param projectIdOrPath Gitlab project ID or path
     * @param iid Issue ID
     * @return The requested issue, null if not found
     * @throws IOException
     */
    public static Issue getIssue(String gitlabUrl, String privateToken, Object projectIdOrPath, int iid) throws IOException {
    	return getIssue(new GitLabApi(gitlabUrl, privateToken), projectIdOrPath, iid);
    }
    
    /**
     * 
     * @param properties Gitlab config with gitlab.url, gitlab.token and gitlab.project expected
     * @param iid Issue ID
     * @return The requested issue, null if not found
     * @throws IOException
     */
    public static Issue getIssue(Properties gitlabConfig, int iid) throws IOException {
    	String gitlabUrl = gitlabConfig.getProperty("gitlab.url");
		String gitlabToken = gitlabConfig.getProperty("gitlab.token");
		String gitlabProject = gitlabConfig.getProperty("gitlab.project");
		if(gitlabUrl == null || gitlabToken == null || gitlabProject == null) {
			throw new IOException("Missing Gitlab URL and/or private token and/or project ID or path in gitlab.properties");
		}
		return getIssue(gitlabUrl, gitlabToken, gitlabProject, iid);
    }
    
    /**
     * Comment an issue (adding notes)
     * @param gitlabUrl Gitlab server URL
     * @param privateToken Gitlab private token for authentication
     * @param projectIdOrPath Gitlab project ID or path
     * @param iid Issue ID
     * @param body Comment body (text)
     * @return true if success, false otherwise
     * @throws IOException
     */
    public static boolean commentIssue(String gitlabUrl, String privateToken, Object projectIdOrPath, int iid, String comment) throws IOException {
    	return commentIssue(new GitLabApi(gitlabUrl, privateToken), projectIdOrPath, iid, comment);
    }

    /**
     * Comment an issue (adding notes)
     * @param properties Gitlab config with gitlab.url, gitlab.token and gitlab.project expected
     * @param iid Issue ID
     * @param body Comment body (text)
     * @return true if success, false otherwise
     * @throws IOException
     */
    public static boolean commentIssue(Properties gitlabConfig, int iid, String body) throws IOException {
    	String gitlabUrl = gitlabConfig.getProperty("gitlab.url");
		String gitlabToken = gitlabConfig.getProperty("gitlab.token");
		String gitlabProject = gitlabConfig.getProperty("gitlab.project");
		if(gitlabUrl == null || gitlabToken == null || gitlabProject == null) {
			throw new IOException("Missing Gitlab URL and/or private token and/or project ID or path in gitlab.properties");
		}
		return commentIssue(gitlabUrl, gitlabToken, gitlabProject, iid, body);
    }
    
    /**
     * Create a Gitlab issue for a given project
     * @param gitlabUrl Gitlab server URL
     * @param privateToken Gitlab private token for authentication
     * @param projectIdOrPath Gitlab project ID or path
     * @param title Issue title
     * @param description Issue content
     * @return The issue created
     * @throws IOException
     */
    public static Issue createIssue(String gitlabUrl, String privateToken, Object projectIdOrPath, String title, String description) throws IOException {
    	return createIssue(new GitLabApi(gitlabUrl, privateToken), projectIdOrPath, title, description);
    }

    /**
     * Create a Gitlab issue for a given project
     * @param gitlabConfig Gitlab config with gitlab.url, gitlab.token and gitlab.project expected
     * @param title Issue title
     * @param description Issue content
     * @return The issue created
     * @throws IOException
     */
    public static Issue createIssue(Properties gitlabConfig, String title, String description) throws IOException {
    	String gitlabUrl = gitlabConfig.getProperty("gitlab.url");
		String gitlabToken = gitlabConfig.getProperty("gitlab.token");
		String gitlabProject = gitlabConfig.getProperty("gitlab.project");
		if(gitlabUrl == null || gitlabToken == null || gitlabProject == null) {
			throw new IOException("Missing Gitlab URL and/or private token and/or project ID or path in gitlab.properties");
		}
		return createIssue(gitlabUrl, gitlabToken, gitlabProject, title, description);
    }
    
    /**
     * Update a Gitlab issue for a given project
     * @param gitlabUrl Gitlab server URL
     * @param privateToken Gitlab private token for authentication
     * @param projectIdOrPath Gitlab project ID or path
     * @param iid ID of issue to update
     * @param title Issue title
     * @param description Issue content
     * @return The issue updated
     * @throws IOException
     */
    public static Issue updateIssue(String gitlabUrl, String privateToken, Object projectIdOrPath, int iid, String title, String description) throws IOException {
    	return updateIssue(new GitLabApi(gitlabUrl, privateToken), projectIdOrPath, iid, title, description);
    }
    
    /**
     * Update a Gitlab issue for a given project
     * @param gitlabConfig Gitlab config with gitlab.url, gitlab.token and gitlab.project expected
     * @param iid ID of issue to update
     * @param title Issue title
     * @param description Issue content
     * @return The issue updated
     * @throws IOException
     */
    public static Issue updateIssue(Properties gitlabConfig, int iid, String title, String description) throws IOException {
    	String gitlabUrl = gitlabConfig.getProperty("gitlab.url");
		String gitlabToken = gitlabConfig.getProperty("gitlab.token");
		String gitlabProject = gitlabConfig.getProperty("gitlab.project");
		if(gitlabUrl == null || gitlabToken == null || gitlabProject == null) {
			throw new IOException("Missing Gitlab URL and/or private token and/or project ID or path in gitlab.properties");
		}
		return updateIssue(gitlabUrl, gitlabToken, gitlabProject, iid, title, description);
    }
    
    /**
     * Delete an issue for a given project
     * @param gitlabUrl Gitlab server URL
     * @param privateToken Gitlab private token for authentication
     * @param projectIdOrPath Gitlab project ID or path
     * @param iid Issue ID
     * @throws IOException
     */
    public static void deleteIssue(String gitlabUrl, String privateToken, Object projectIdOrPath, int iid) throws IOException {
    	deleteIssue(new GitLabApi(gitlabUrl, privateToken), projectIdOrPath, iid);
    }
    
    /**
     * Delete an issue for a given project
     * @param gitlabConfig Gitlab config with gitlab.url, gitlab.token and gitlab.project expected
     * @param iid Issue ID
     * @throws IOException
     */
    public static void deleteIssue(Properties gitlabConfig, int iid) throws IOException {
    	String gitlabUrl = gitlabConfig.getProperty("gitlab.url");
		String gitlabToken = gitlabConfig.getProperty("gitlab.token");
		String gitlabProject = gitlabConfig.getProperty("gitlab.project");
		if(gitlabUrl == null || gitlabToken == null || gitlabProject == null) {
			throw new IOException("Missing Gitlab URL and/or private token and/or project ID or path in gitlab.properties");
		}
		deleteIssue(gitlabUrl, gitlabToken, gitlabProject, iid);
    }

    /**
     * Decide whether an issue is likely to contain an exception stack (or not)
     * @param issue The issue to check
     * @return true if likely, false if not
     */
    public static boolean isExceptionLikely(Issue issue) {
    	String title = issue.getTitle().toLowerCase();
    	if(title.contains("exception") || title.contains("npe")) return true;
    	String description = issue.getDescription();
    	Pattern atMore = Pattern.compile("(\\t|\\s\\s+)at |(\\t|\\s\\s+)... \\d+ more");
    	Matcher matcher = atMore.matcher(description);
    	return matcher.find();
    	/*try {
			return (ExceptionExtractor.extractExceptions(new BufferedReader(new StringReader(description))) != null);
		} catch (IOException e) {
			return false;
		}*/
    }

    /**
     * Retrieve a gitlab issue from a given project
     * @param api Gitlab4j session
     * @param projectIdOrPath Gitlab project ID or path
     * @param iid Issue ID
     * @return The requested issue, null if not found
     * @throws IOException
     */
    private static Issue getIssue(GitLabApi api, Object projectIdOrPath, int iid) throws IOException {
    	try {
    		return api.getIssuesApi().getIssue(projectIdOrPath, iid);
    	} catch(GitLabApiException e) {
    		throw new IOException(e);
    	}
    }
    
    /**
     * Create a Gitlab issue for a given project
     * @param api Gitlab4j session
     * @param projectIdOrPath Gitlab project ID or path
     * @param title Issue title
     * @param description Issue content
     * @return The created issue
     * @throws IOException
     */
    private static Issue createIssue(GitLabApi api, Object projectIdOrPath, String title, String description) throws IOException {
    	try {
    		return api.getIssuesApi().createIssue(projectIdOrPath, title, description);
    	} catch(GitLabApiException e) {
    		throw new IOException(e);
    	}
    }
    
    /**
     * 
     * @param api Gitlab4j session
     * @param projectIdOrPath
     * @param iid Issue ID Gitlab project ID or path
     * @param title Issue title
     * @param description Issue content
     * @return The updated issue
     * @throws IOException
     */
    private static Issue updateIssue(GitLabApi api, Object projectIdOrPath, int iid, String title, String description) throws IOException {
    	try {
    		Issue issue = getIssue(api, projectIdOrPath, iid);
    		
    		if(issue != null) {
    			List<Integer> ids = new LinkedList<Integer>();
    			List<Assignee> assignees = issue.getAssignees();
    			if(assignees != null) {
    				for(Assignee assignee : assignees) {
    				  ids.add(assignee.getId());
    				}
    			}
    			if(ids.isEmpty()) ids = null;
    			
    			return api.getIssuesApi().updateIssue(projectIdOrPath, iid, title, description,
    					issue.getConfidential(), ids,
    					(issue.getMilestone() == null ? null : issue.getMilestone().getId()),
    					null, // Label
    					Constants.StateEvent.REOPEN, issue.getUpdatedAt(), issue.getDueDate());
    		} else {
    			return null;
    		}
    	} catch(GitLabApiException e) {
    		throw new IOException(e);
    	}
    }
    
    /**
     * Delete an issue for a given project
     * @param api Gitlab4j session
     * @param projectIdOrPath Gitlab project ID or path
     * @param iid Issue ID
     * @throws IOException
     */
    private static void deleteIssue(GitLabApi api, Object projectIdOrPath, int iid) throws IOException {
    	try {
    		api.getIssuesApi().deleteIssue(projectIdOrPath, iid);
    	} catch(GitLabApiException e) {
    		throw new IOException(e);
    	}
    }

    /**
     * Comment an issue (adding notes)
     * @param gitlabUrl Gitlab server URL
     * @param privateToken Gitlab private token for authentication
     * @param projectIdOrPath Gitlab project ID or path
     * @param iid Issue ID
     * @param body Comment body (text)
     * @return true if success, false otherwise
     * @throws IOException
     */
    private static boolean commentIssue(GitLabApi api, Object projectIdOrPath, int iid, String body) throws IOException {
    	Note result = null;
    	try {
			result = api.getNotesApi().createIssueNote(projectIdOrPath, iid, body);
		} catch (GitLabApiException e) {
			throw new IOException(e);
		}
    	return (result != null);
    }
   
    /**
     * List gitlab issues for a given project
     * @param api Gitlab4j session
     * @param projectIdOrPath Gitlab project ID or path
     * @param state Gitlab4j issue state
     * @return The list of gitlabs issues that meet the criteria
     * @throws GitLabApiException
     */
    private static List<Issue> listIssues(GitLabApi api, Object projectIdOrPath, IssueState state) throws GitLabApiException {
    	IssueFilter issueFilter = new IssueFilter();
    	issueFilter.setState(state);
    	return api.getIssuesApi().getIssues(projectIdOrPath, issueFilter);
    }

}
