package eu.stamp_project.dspot.jenkins.report;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import eu.stamp_project.dspot.jenkins.utils.AnnotatedDeserializer;
import eu.stamp_project.dspot.jenkins.utils.JSONFileFilter;
import eu.stamp_project.dspot.jenkins.DSpotResultsAction;
import eu.stamp_project.dspot.jenkins.json.ProjectTimeJSON;
import hudson.FilePath;

public class DSpotResults implements Serializable {

	private static final String JAVA_EXT = ".java";

	private static final long serialVersionUID = -8639672864330736615L;

	private transient DSpotResultsAction buildAction;
	private FilePath outputDir;
	private ProjectTimeJSON report;
	private Map<String, eu.stamp_project.dspot.jenkins.json.mutant.TestClassJSON> mutantsReport = new HashMap<>();
	private Map<String, eu.stamp_project.dspot.jenkins.json.coverage.TestClassJSON> coverageReport = new HashMap<>();
	private Map<String, eu.stamp_project.dspot.jenkins.json.change.TestClassJSON> changeReport = new HashMap<>();
	private Map<String, FilePath> outputFiles = new HashMap<>();
	

	public DSpotResults() {
	}

	public DSpotResults(FilePath dir) {
		this.setOutputDir(dir);
		List<FilePath> jsonFiles;
		try {
			jsonFiles = outputDir.list(new JSONFileFilter());
			Gson parser = new GsonBuilder()
					.registerTypeAdapter(ProjectTimeJSON.class,
							new AnnotatedDeserializer<ProjectTimeJSON>())
					.registerTypeAdapter(eu.stamp_project.dspot.jenkins.json.coverage.TestClassJSON.class,
							new AnnotatedDeserializer<eu.stamp_project.dspot.jenkins.json.coverage.TestClassJSON>())
					.registerTypeAdapter(eu.stamp_project.dspot.jenkins.json.change.TestClassJSON.class,
							new AnnotatedDeserializer<eu.stamp_project.dspot.jenkins.json.change.TestClassJSON>())
					.registerTypeAdapter(eu.stamp_project.dspot.jenkins.json.mutant.TestClassJSON.class,
							new AnnotatedDeserializer<eu.stamp_project.dspot.jenkins.json.mutant.TestClassJSON>())
					.create();
			for (FilePath filePath : jsonFiles) {
				String txt = filePath.readToString();

				try {
					eu.stamp_project.dspot.jenkins.json.mutant.TestClassJSON t = parser.fromJson(txt,
							eu.stamp_project.dspot.jenkins.json.mutant.TestClassJSON.class);
					mutantsReport.put(t.name, t);
					FilePath f = new FilePath(outputDir, toFile(t.name));
					outputFiles.put(t.name, f);
				} catch (JsonParseException e1) {
					;
				}
				try {
					eu.stamp_project.dspot.jenkins.json.change.TestClassJSON t = parser.fromJson(txt,
							eu.stamp_project.dspot.jenkins.json.change.TestClassJSON.class);
					changeReport.put(t.name, t);
				} catch (JsonParseException e1) {
					;
				}
				try {
					eu.stamp_project.dspot.jenkins.json.coverage.TestClassJSON t = parser.fromJson(txt,
							eu.stamp_project.dspot.jenkins.json.coverage.TestClassJSON.class);
					coverageReport.put(t.name, t);
				} catch (JsonParseException e1) {
					;
				}
				try {
					report = parser.fromJson(txt, ProjectTimeJSON.class);
				} catch (JsonParseException e) {
					;
				}
			}
		} catch (IOException | InterruptedException e1) {
			e1.printStackTrace();
		}

	}

	private String toFile(String name) {
		return name.replaceAll("\\.", "/")+JAVA_EXT;
	}

	public void setBuildAction(DSpotResultsAction buildAction) {
		this.buildAction = buildAction;
	}

	public DSpotResultsAction getBuildAction() {
		return buildAction;
	}

	public FilePath getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(FilePath outputDir) {
		this.outputDir = outputDir;
	}

	public ProjectTimeJSON getProjectTime() {
		return report;
	}

	public Map<String, eu.stamp_project.dspot.jenkins.json.change.TestClassJSON> getChangeReport() {
		return changeReport;
	}

	public Map<String, eu.stamp_project.dspot.jenkins.json.coverage.TestClassJSON> getCoverageReport() {
		return coverageReport;
	}

	public Map<String, eu.stamp_project.dspot.jenkins.json.mutant.TestClassJSON> getMutantsReport() {
		return mutantsReport;
	}
	
	public Map<String, FilePath> getOutputFiles() {
		return outputFiles;
	}
	
	public FilePath getVirtualFile(String name){
		return outputFiles.get(name);
	}

}
