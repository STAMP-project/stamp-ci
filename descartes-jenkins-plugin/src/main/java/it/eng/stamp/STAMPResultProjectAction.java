package it.eng.stamp;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import it.eng.stamp.results.DescartesReport;
import it.eng.stamp.util.Constants;
import it.eng.stamp.util.ReportMetrics;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class STAMPResultProjectAction implements Action {

	private static final Logger LOGGER = Logger.getLogger(STAMPResultProjectAction.class.getName());

	private AbstractProject<?, ?> project;
	
	private static final ReportMetrics MAIN_METRIC = ReportMetrics.COVERAGE;

	public STAMPResultProjectAction(AbstractProject<?, ?> project) {
		this.project = project;
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return Messages.STAMPReportProjectAction_DisplayName();
	}

	@Override
	public String getUrlName() {
		return Constants.PLUGIN_NAME;
	}

	/**
	 * Method necessary to get the side-panel included in the Jelly file
	 * 
	 * @return this {@link AbstractProject}
	 */
	public AbstractProject<?, ?> getProject() {
		return this.project;
	}

	public boolean isTrendVisibleOnProjectDashboard() {
		return getExistingReportsList().size() > 1;
	}

	public List<String> getMetricsList(){
		return ReportMetrics.stringValues();
	}
	/*
	 * Graph of metric points over time.
	 */
	public void doSummarizerGraphCoverage(final StaplerRequest request, final StaplerResponse response)
			throws IOException {
		final Map<ChartUtil.NumberOnlyBuildLabel, Double> averagesFromReports = getAveragesFromAllReports(
				getExistingReportsList(), MAIN_METRIC.name());

		final Graph graph = new GraphImpl("Mutation Coverage Trend") {

			protected DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> createDataSet() {
				DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

				for (Map.Entry<ChartUtil.NumberOnlyBuildLabel, Double> entry : averagesFromReports.entrySet()) {
					dataSetBuilder.add(entry.getValue(), "coverage", entry.getKey());
				}

				return dataSetBuilder;
			}
		};

		graph.doPng(request, response);
	}

	/**
	   * Graph of metric points over time, metric to plot set as request parameter.
	   */
	  public void doSummarizerGraphForMetric(final StaplerRequest request,
	                                          final StaplerResponse response) throws IOException {
	    final String metricKey = request.getParameter("metricDataKey");
	    final Map<ChartUtil.NumberOnlyBuildLabel, Double> averagesFromReports =
	        getAveragesFromAllReports(getExistingReportsList(), metricKey);

	    final Graph graph = new GraphImpl(metricKey + " Overall Graph") {

	      protected DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> createDataSet() {
	        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
	            new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

	        for (Map.Entry<ChartUtil.NumberOnlyBuildLabel,Double> entry : averagesFromReports.entrySet()) {
	          dataSetBuilder.add(entry.getValue(), metricKey, entry.getKey());
	        }

	        return dataSetBuilder;
	      }
	    };

	    graph.doPng(request, response);
	  }

	
	private abstract class GraphImpl extends Graph {
		private final String graphTitle;

		protected GraphImpl(final String metricKey) {
			super(-1, 400, 300); // cannot use timestamp, since ranges may
									// change
			this.graphTitle = stripTitle(metricKey);
		}

		private String stripTitle(final String metricKey) {
			return metricKey.substring(metricKey.lastIndexOf("|") + 1);
		}

		protected abstract DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> createDataSet();

		protected JFreeChart createGraph() {
			final CategoryDataset dataset = createDataSet().build();

			final JFreeChart chart = ChartFactory.createLineChart(graphTitle, // title
					"Build Number #", // category axis label
					null, // value axis label
					dataset, // data
					PlotOrientation.VERTICAL, // orientation
					false, // include legend
					true, // tooltips
					false // urls
			);

			chart.setBackgroundPaint(Color.white);

			return chart;
		}
	}

	private List<DescartesReport> getExistingReportsList() {
		final List<DescartesReport> adReportList = new ArrayList<DescartesReport>();

		if (null == this.project) {
			return adReportList;
		}

		final List<? extends AbstractBuild<?, ?>> builds = project.getBuilds();
		for (AbstractBuild<?, ?> currentBuild : builds) {
			final STAMPReportBuildAction performanceBuildAction = currentBuild.getAction(STAMPReportBuildAction.class);
			if (performanceBuildAction == null) {
				continue;
			}
			final DescartesReport report = performanceBuildAction.getBuildActionResultsDisplay().getDescartesReport();
			if (report == null) {
				continue;
			}

			adReportList.add(report);
		}

		return adReportList;
	}

	private Map<ChartUtil.NumberOnlyBuildLabel, Double> getAveragesFromAllReports(final List<DescartesReport> reports, String metricKey) {
		Map<ChartUtil.NumberOnlyBuildLabel, Double> averages = new TreeMap<ChartUtil.NumberOnlyBuildLabel, Double>();
		for (DescartesReport report : reports) {
			double value = -1;
			try {
				value = report.getAverageForMetric(ReportMetrics.valueOf(metricKey));
			} catch (IllegalArgumentException e) {
				// Report might not have custom metric, silently skip in that
				// case
				LOGGER.info(
						String.format("Build %s does not contain %s value, silently skipping", report.getName(), metricKey));
			}

			if (value >= 0) {
				ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(
						(Run<?, ?>) report.getBuild());
				averages.put(label, value);
			}
		}

		return averages;
	}
}
