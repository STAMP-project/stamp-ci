package it.eng.stamp.view;

import java.awt.Color;
import java.awt.Paint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.AbstractBuild;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import it.eng.stamp.Messages;
import it.eng.stamp.STAMPReportBuildAction;
import it.eng.stamp.results.DescartesReport;
import it.eng.stamp.results.MethodClassification;
import it.eng.stamp.results.MethodResult;
import it.eng.stamp.util.Constants;
import it.eng.stamp.util.ReportMetrics;

public class DescartesReportResultDisplay extends AbstractMutationDisplay {

	private static AbstractBuild<?, ?> currentBuild = null;

	public DescartesReportResultDisplay(final STAMPReportBuildAction buildAction) throws IOException {
		super(null, Messages.DescartesReport_DisplayName(), buildAction);
	}

	public long getPartiallyTested() {
		return Constants.toPercent(getDescartesReport().getAverageForMetric(ReportMetrics.PARTIALLY_TESTED));
	}

	public long getPseudoTested() {
		return Constants.toPercent(getDescartesReport().getAverageForMetric(ReportMetrics.PSEUDO_TESTED));
	}

	@Override
	protected void tally() {
		addPreviousBuildReportToExistingReport();
		getDescartesReport().setBuildAction(getBuildAction());
		for (MethodResult mr : getDescartesReport().getMethods()) {
			String packageName = mr.getPakg();
			if (!getChildrenNames().contains(packageName)) {
				addChild(new PackageSummaryDisplay(this, packageName, getBuildAction()));
			}
		}
		compute();
	}

	private void addPreviousBuildReportToExistingReport() {
		// Avoid parsing all builds.
		if (DescartesReportResultDisplay.currentBuild == null) {
			DescartesReportResultDisplay.currentBuild = getBuild();
		} else {
			if (DescartesReportResultDisplay.currentBuild != getBuild()) {
				DescartesReportResultDisplay.currentBuild = null;
				return;
			}
		}

		AbstractBuild<?, ?> previousBuild = getBuild().getPreviousBuild();
		if (previousBuild == null) {
			return;
		}

		List<STAMPReportBuildAction> previousActions = previousBuild.getActions(STAMPReportBuildAction.class);
		if(previousActions.size() == 0) {
			return;
		}

		STAMPReportBuildAction previousPerformanceAction = previousActions.get(0);
		if (previousPerformanceAction == null) {
			return;
		}

		AbstractMutationDisplay previousBuildActionResults = previousPerformanceAction.getBuildActionResultsDisplay();
		if (previousBuildActionResults == null) {
			return;
		}

		DescartesReport lastReport = previousBuildActionResults.getDescartesReport();
		getDescartesReport().setLastBuildReport(lastReport);
	}

	/**
	 * Graph of metric points over time, metric to plot set as request
	 * parameter.
	 */
	public void doSummarizerPieGraph(final StaplerRequest request, final StaplerResponse response) throws IOException {
		Map<Comparable, Paint> mappings = new HashMap<>();
		mappings.put(MethodClassification.TESTED, Color.GREEN);
		mappings.put(MethodClassification.NOT_COVERED, Color.RED);
		final Graph graph = new PieGraph("Methods Classification", mappings) {
			@Override
			protected PieDataset createDataSet() {
				DefaultPieDataset dataset = new DefaultPieDataset();
				for (MethodClassification classification : MethodClassification.values()) {
					dataset.setValue(classification, getDescartesReport().getClassificationCount(classification));
				}
				return dataset;
			}
		};

		graph.doPng(request, response);
	}
	
	/**
	 * Graph of metric points over time, metric to plot set as request
	 * parameter.
	 */
	public void doDetectedPieGraph(final StaplerRequest request, final StaplerResponse response) throws IOException {
		Map<Comparable, Paint> mappings = new HashMap<>();
		mappings.put("detected", Color.GREEN);
		mappings.put("not-detected", Color.RED);
		final Graph graph = new PieGraph("Coverage", mappings) {
			@Override
			protected PieDataset createDataSet() {
				DefaultPieDataset dataset = new DefaultPieDataset();
				dataset.setValue("detected", getDescartesReport().getDetectedCount());
				dataset.setValue("not-detected", getDescartesReport().getNotDetectedCount());
				
				return dataset;
			}
		};

		graph.doPng(request, response);
	}

	private abstract class PieGraph extends Graph {
		private final String graphTitle;
		private Map<Comparable, Paint> mappings;

		protected PieGraph(final String metricKey, Map<Comparable, Paint> mappings) {
			super(-1, 400, 300); // cannot use timestamp, since ranges may
									// change
			this.graphTitle = stripTitle(metricKey);
			this.mappings = mappings;
		}

		private String stripTitle(final String metricKey) {
			return metricKey.substring(metricKey.lastIndexOf("|") + 1);
		}

		protected abstract PieDataset createDataSet();

		protected JFreeChart createGraph() {
			final PieDataset dataset = createDataSet();

			final JFreeChart chart = ChartFactory.createPieChart(graphTitle, dataset, false, true, false);// title

			if (mappings != null) {
				PiePlot plot = (PiePlot) chart.getPlot();
				for (Comparable k : mappings.keySet()) {
					plot.setSectionPaint(k, mappings.get(k));
				}
			}
			chart.setBackgroundPaint(Color.white);

			return chart;
		}
	}

}
