package eu.stamp_project.descartes.gitlab;

import static org.junit.Assert.*;

import org.junit.Test;

public class GitlabIssueListenerFactoryTest {

	@Test
	public void testName() {
		GitlabIssueListenerFactory factory = new GitlabIssueListenerFactory();
		assertEquals("GITLAB-ISSUES", factory.name());
	}

}
