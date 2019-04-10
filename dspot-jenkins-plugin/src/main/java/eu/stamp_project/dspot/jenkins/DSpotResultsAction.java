package eu.stamp_project.dspot.jenkins;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerProxy;

import com.thoughtworks.xstream.XStream;

import eu.stamp_project.dspot.jenkins.report.DSpotResults;
import eu.stamp_project.dspot.jenkins.report.view.DspotResultDisplay;
import hudson.XmlFile;
import hudson.model.Action;
import hudson.model.Run;
import hudson.util.HeapSpaceStringConverter;
import hudson.util.XStream2;

public class DSpotResultsAction implements Action, StaplerProxy {
	public static final String STAMP_LOGO_LOCATION = "/plugin/dspot-jenkins-plugin/img/stamp-logo.png";
	public static final String PLUGIN_NAME = "dspot-dashboard";

	private transient WeakReference<DSpotResults> report;
	private transient WeakReference<DspotResultDisplay> display;

	private transient static final Logger logger = Logger.getLogger(DSpotResultsAction.class.getName());
	private static final XStream XSTREAM = new XStream2();
	private Run<?, ?> run;
	static {
		XSTREAM.alias("dspotresult", DSpotResults.class);
		XSTREAM.registerConverter(new HeapSpaceStringConverter(), 100);
	}

	public DSpotResultsAction(Run<?, ?> r, DSpotResults results) {
		run = r;
		setReport(results);
	}

	@Override
	public String getDisplayName() {
		return "STAMP DSpot";
	}

	@Override
	public String getIconFileName() {
		return STAMP_LOGO_LOCATION;
	}

	@Override
	public String getUrlName() {
		return PLUGIN_NAME;
	}
	
	public DspotResultDisplay getTarget() {
		return getDisplay();
	}

	public synchronized void setReport(DSpotResults report) {
		if (run != null) {
			// persist the data
			try {
				getDataFile().write(report);
			} catch (IOException e) {
				logger.severe("Failed to save the Amplification results");
				e.printStackTrace();
			}
		}
		report.setBuildAction(this);
		this.report = new WeakReference<DSpotResults>(report);
	}

	/**
	 * Loads a {@link DSpotResults item} from disk.
	 */
	private DSpotResults load() {
		DSpotResults r;
		try {
			r = (DSpotResults) getDataFile().read();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load " + getDataFile(), e);
			r = new DSpotResults(); // return a dummy
		}
		return r;
	}

	private XmlFile getDataFile() {
		return new XmlFile(XSTREAM, new File(run.getRootDir(), "dspotResult.xml"));
	}

	public synchronized DSpotResults getReport() {
		DSpotResults r;
		if (report == null) {
			r = load();
			report = new WeakReference<DSpotResults>(r);
		} else {
			r = report.get();
		}

		if (r == null) {
			r = load();
			report = new WeakReference<DSpotResults>(r);
		}
		return r;
	}

	public DspotResultDisplay getDisplay() {
		DspotResultDisplay buildDisplay = null;
		WeakReference<DspotResultDisplay> wr = this.display;
		if (wr != null) {
			buildDisplay = wr.get();
			if (buildDisplay != null)
				return buildDisplay;
		}
		try {
			buildDisplay = new DspotResultDisplay(this);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error creating new DspotResultDisplay()", e);
		}
		this.display = new WeakReference<>(buildDisplay);
		return buildDisplay;
	}

	public void setDisplay(WeakReference<DspotResultDisplay> display) {
		this.display = display;
	}
	
	public Run<?, ?> getRun() {
		return run;
	}

}
