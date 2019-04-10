package it.eng.stamp.util;

import org.apache.commons.lang.StringUtils;


public final class Constants {
	public static final String STAMP_LOGO_LOCATION = "/plugin/stamp-report-plugin/img/stamp-logo.png";
	public static final String PLUGIN_NAME = "stamp-dashboard";
	
	public static String safe(String s) {
		if (StringUtils.isEmpty(s)) {
			return "(empty)";
		} else {
			// this still seems to be a bit faster than a single replace with
			// regexp
			return s.replace('/', '_').replace('\\', '_').replace(':', '_').replace('?', '_').replace('#', '_')
					.replace('%', '_').replace('<', '_').replace('>', '_');
			// Note: we probably should some helpers like Commons URIEscapeUtils
			// here to escape all invalid URL chars, but then we
			// still would have to escape /, ? and so on
		}
	}
	
	public static String display(String s) {
		if (StringUtils.isEmpty(s)) {
			return "(empty)";
		} else {
			// this still seems to be a bit faster than a single replace with
			// regexp
			return s.replace('/', '.');
			// Note: we probably should some helpers like Commons URIEscapeUtils
			// here to escape all invalid URL chars, but then we
			// still would have to escape /, ? and so on
		}
	}
	
	public static long percent (int count, int total){
		return Math.round((divide(count, total)*100));
	}

	public static double divide(int num1, int num2) {
		return ((double) num1 /(double) num2);
	}
	
	public static long toPercent (double val){
		return Math.round(val*100);
	}
	
}
