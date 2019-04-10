package eu.stamp_project.dspot.jenkins.report.view;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import eu.stamp_project.dspot.jenkins.json.ClassTimeJSON;
import eu.stamp_project.dspot.jenkins.utils.Constants;
import hudson.FilePath;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.ModelObject;
import hudson.model.Run;

public abstract class AbstractDSpotDisplay implements ModelObject {

	private String name;
	private long time;
	public long numOriginalTestCases = 0;
	private Run<?, ?> build;
	private FilePath sourceFile;

	public AbstractDSpotDisplay() {

	}

	public AbstractDSpotDisplay(Run<?, ?> run, ClassTimeJSON clazz) {
		build = run;
		name = clazz.fullQualifiedName;
		time = clazz.timeInMs;
	}

	public String getName() {
		return name;
	}

	public long getTime() {
		return time;
	}

	@Override
	public String getDisplayName() {
		return name;
	}

	public String getUrlName() {
		return Constants.safe(getName());
	}

	public Run<?, ?> getBuild() {
		return build;
	}

	abstract public int getNumNewTestCases();

	public FilePath getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(FilePath sourceFile) {
		this.sourceFile = sourceFile;
	}

	/**
     * Serves the workspace files.
     */
    public DirectoryBrowserSupport doJavaSource( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException, InterruptedException {
        FilePath ws  = sourceFile.getParent();
        if ((ws == null) || (!ws.exists())) {
            return null;
        } else {
            return new DirectoryBrowserSupport(getBuild(), ws, name, "document.png", true);
        }
    }
}
