package eu.stamp_project.cicd.utils.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.jgit.api.Git;

import junit.framework.TestCase;

/**
 * Test GIT cloner.
 * @author Pierre-Yves Gibello - OW2
 *
 */
public class GitClonerTest extends TestCase {

	public void testCloneRepository() {
		// Requires a git init (eg. "git init /tmp/repo") before test
		File repo = null, cloneDir = null;
		try {
			repo = Files.createTempDirectory("repo").toFile();
			repo.deleteOnExit();
			Git.init().setDirectory(repo).call();

			cloneDir = Files.createTempDirectory("clone").toFile();
			cloneDir.deleteOnExit();
		} catch(Exception e) {
			fail("Can\'t initialize test: " + e);
		}

		// Test now !
		try {
			GitCloner.cloneRepository("file://" + repo.getAbsolutePath(),
					cloneDir.getAbsolutePath() + File.separator + "cloned_repo", true);
		} catch (IOException e) {
			fail("cloneRepository() failed with IOException: " + e.getMessage());
		}
	}
}
