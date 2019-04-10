package eu.stamp_project.dspot.jenkins.report.view;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import eu.stamp_project.dspot.jenkins.DSpotResultsAction;
import eu.stamp_project.dspot.jenkins.json.ClassTimeJSON;
import eu.stamp_project.dspot.jenkins.report.DSpotResults;
import eu.stamp_project.dspot.jenkins.utils.Constants;
import hudson.FilePath;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.util.Graph;

public class DspotResultDisplay implements ModelObject {

	private transient DSpotResultsAction buildAction;
	private String projectName;
	private long overallTime = 0;
	private Map<String, AbstractDSpotDisplay> resultsByName = new HashMap<>();
	private FilePath rootOutputFolder;

	public DspotResultDisplay(DSpotResultsAction action) throws IOException {
		this.buildAction = action;
		compute();
	}

	private void compute() {
		DSpotResults report = getCurrentReport();
		projectName = getCurrentReport().getProjectTime().projectName;
		rootOutputFolder = report.getOutputDir();
		for (ClassTimeJSON clazz : report.getProjectTime().classTimes) {
			overallTime += clazz.timeInMs; 
			//only one type will contain reports depending on chosen selector
			String fullQualifiedName = clazz.fullQualifiedName;
			if (report.getMutantsReport().containsKey(fullQualifiedName))
				resultsByName.put(fullQualifiedName,
						new MutantSelectorDisplay(getBuild(), clazz, report.getMutantsReport().get(fullQualifiedName)));
			if (report.getChangeReport().containsKey(fullQualifiedName))
				resultsByName.put(fullQualifiedName,
						new ChangeSelectorDisplay(getBuild(), clazz, report.getChangeReport().get(fullQualifiedName)));
			if (report.getCoverageReport().containsKey(fullQualifiedName))
				resultsByName.put(fullQualifiedName,
						new CoverageSelectorDisplay(getBuild(), clazz, report.getCoverageReport().get(fullQualifiedName)));
			try {
				resultsByName.get(fullQualifiedName).setSourceFile(report.getVirtualFile(fullQualifiedName));
			} catch (NullPointerException e) {
				//no file associated, may be normal;
			}
		}
	}

	/*
	 * Graph of metric points over time, metric to plot set as request
	 * parameter.
	 */
	public void doSummarizerPieGraph(final StaplerRequest request, final StaplerResponse response) throws IOException {
		final Graph graph = new PieGraph("Tests Amplification computing times") {
			@Override
			protected PieDataset createDataSet() {
				DefaultPieDataset dataset = new DefaultPieDataset();
				for (AbstractDSpotDisplay classification : resultsByName.values()) {
					dataset.setValue(classification.getName(), classification.getTime());
				}
				return dataset;
			}
		};

		graph.doPng(request, response);
	}

	//used to redirect to sub pages
	public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
		if (token.equals(getUrlName())) {
			return this;
		}
		AbstractDSpotDisplay result = getResultsByName(token);
		if (result != null) {
			return result;
		}
		else {
			return null;
		}
	}
	
	/**
     * Serves the workspace files.
     */
    public DirectoryBrowserSupport doOutput( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException, InterruptedException {
        FilePath ws  = rootOutputFolder;
        if ((ws == null) || (!ws.exists())) {
            return null;
        } else {
            return new DirectoryBrowserSupport(getBuild(), ws, "DSpot Output", "folder.png", true);
        }
    }
    
	private Object getUrlName() {
		return Constants.safe(getProjectName());
	}

	public DSpotResults getCurrentReport() {
		return buildAction.getReport();
	}

	public DSpotResultsAction getBuildAction() {
		return buildAction;
	}

	public Run<?, ?> getBuild() {
		return buildAction.getRun();
	}

	public String getProjectName() {
		return projectName;
	}

	public Collection<AbstractDSpotDisplay> getResults() {
		return resultsByName.values();
	}

	public AbstractDSpotDisplay getResultsByName(String name) {
		return resultsByName.get(name);
	}

	public Collection<String> getClassNames() {
		return resultsByName.keySet();
	}

	public long getOverallTime() {
		return overallTime;
	}

	private abstract class PieGraph extends Graph {
		private final String graphTitle;

		protected PieGraph(final String metricKey) {
			super(-1, 400, 300); // cannot use timestamp, since ranges may
									// change
			this.graphTitle = stripTitle(metricKey);
		}

		private String stripTitle(final String metricKey) {
			return metricKey.substring(metricKey.lastIndexOf("|") + 1);
		}

		protected abstract PieDataset createDataSet();

		protected JFreeChart createGraph() {
			final PieDataset dataset = createDataSet();

			final JFreeChart chart = ChartFactory.createPieChart(graphTitle, dataset, false, true, false);// title
			chart.setBackgroundPaint(Color.white);

			return chart;
		}
	}

	@Override
	public String getDisplayName() {
		return "DSpot Results report";
	}

}
