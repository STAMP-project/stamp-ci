/*
 * The MIT License
 *
 * Copyright (c) 2013, Cisco Systems, Inc., a California corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package it.eng.stamp.results;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import it.eng.stamp.util.JSONParser;
import jenkins.MasterToSlaveFileCallable;

/**
 * Parser that understands Pit Descartes JSON standard reports notation and will
 * generate {@link it.eng.stamp.results.DescartesReport} so that
 * Jenkins will display the results.
 */
public class DescartesReportParser implements Serializable {

	private static Logger LOGGER = Logger.getLogger(DescartesReportParser.class.getName());
	private static final long serialVersionUID = -296964473181541824L;

	public DescartesReportParser() {

	}

	public String getDisplayName() {
		return "Descartes Report parser";
	}

	protected DescartesReport parse(List<File> reportFile)
			throws InterruptedException, IOException {

		LOGGER.info("[Descartes Report] Parsing results.");

		if (reportFile.isEmpty())
			return null;
		try {
			File f = reportFile.get(0);
			String s = FileUtils.readFileToString(f, "UTF-8");
			
			if (s.isEmpty()) {
				LOGGER.info("[Descartes Report] ignoring empty file (" + f.getName() + ")");
				return null;
			}
			LOGGER.info("[Descartes Report] parsing " + f.getName());
			
			JSONParser jsonParser = JSONParser.get();
			jsonParser.setDeserializer(new ClassificationDeserializer(), MethodClassification.class);
			return jsonParser.parseMapping(s, DescartesReport.class);

		} catch (Exception ccm) {
			throw new AbortException("Failed to parse JSON: " + ccm.getMessage());
		}

	}
	
	class ClassificationDeserializer implements JsonDeserializer<MethodClassification>
	{
		@Override
		public MethodClassification deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			String value = json.getAsString();
			return MethodClassification.fromString(value);
		}
	  
	}

	static final class ParseResultCallable extends MasterToSlaveFileCallable<DescartesReport> {

		private static final long serialVersionUID = -5438084460911132640L;
		private DescartesReportParser parserImpl;
		private long buildTime;
		private long nowMaster;
		private String testResultLocations;



		public ParseResultCallable(DescartesReportParser parserImpl, String testResultLocations,
				long buildTime, long nowMaster, TaskListener listener) {
			this.parserImpl = parserImpl;
			this.testResultLocations = testResultLocations;
			this.buildTime = buildTime;
			this.nowMaster = nowMaster;

	
		}

		public DescartesReport invoke(File dir, VirtualChannel channel) throws IOException, InterruptedException {
			final long nowSlave = System.currentTimeMillis();

			// files older than this timestamp is considered stale
			long localBuildTime = buildTime + (nowSlave - nowMaster);

			FilePath[] paths = new FilePath(dir).list(testResultLocations);
			if (paths.length == 0)
				throw new AbortException(
						"No test reports that matches " + testResultLocations + " found. Configuration error?");

			// since dir is local, paths all point to the local files
			List<File> files = new ArrayList<File>(paths.length);
			for (FilePath path : paths) {
				File report = new File(path.getRemote());
				files.add(report);

			}

			if (files.isEmpty()) {
				// none of the files were new
				throw new AbortException(String.format(
						"Test reports were found but none of them are new. Did tests run? %n"
								+ "For example, %s is %s old%n",
						paths[0].getRemote(), Util.getTimeSpanString(localBuildTime - paths[0].lastModified())));
			}

			return parserImpl.parse(files);
		}
	}

	 @SuppressFBWarnings(value={"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"}, justification="whatever")
	public DescartesReport parseResult(final String testResultLocations,final AbstractBuild<?, ?> build,
			final Launcher launcher, final TaskListener listener)
			throws InterruptedException, IOException {

		long buildTime = build.getTimestamp().getTimeInMillis();
		long nowMaster = System.currentTimeMillis();

		ParseResultCallable callable = new ParseResultCallable(this, testResultLocations,
				buildTime, nowMaster, listener);

		return build.getWorkspace().act(callable);
	}
}
