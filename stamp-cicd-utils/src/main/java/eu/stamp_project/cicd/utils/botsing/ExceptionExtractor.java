package eu.stamp_project.cicd.utils.botsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse log stream to extract exception stacks.
 * @author Pierre-Yves Gibello - OW2
 */
public class ExceptionExtractor {
    
    /**
     * Extract exceptions from a log stream (eg. an app server log file).
     * @param in The log stream (file or so) from which exceptions must be extracted.
     * @return A list of exceptions extracted in the input stream, null if none.
     * @throws IOException
     */
    public static List<String> extractExceptions(BufferedReader in) throws IOException {
    	LinkedList<String> ret = null;   	
    	String line;
    	String previous = "";
    	// Pattern more permissive than it could : should be ^\\t and never ^(\\t|\\s+)
    	// but required when exceptions are carelessly copied/pasted (with tabs replaced by spaces)
    	Pattern pattern = Pattern.compile("^(\\t|\\s+)at |^Caused by: |^(\\t|\\s+)... \\d+ more");
    	Pattern nested = Pattern.compile("; nested exception is: *$");
    	StringBuffer currentException = null;
    	while((line = in.readLine()) != null) {
    		Matcher matcher = pattern.matcher(line);
    		if(matcher.find()) {
    			if(currentException == null) currentException = new StringBuffer(previous);
    			currentException.append("\n" + line);
    		} else if(currentException != null) {
    			Matcher nestedMatcher = nested.matcher(line);
    			if(nestedMatcher.find()) {
    				currentException.append("\n" + line);
    			} else {
    				if(ret == null) ret = new LinkedList<String>();
    				ret.add(currentException.toString().trim());
    				currentException = null;
    			}
    		}
    		previous = line;
    	}
    	// Don't forget last exception if EOF at the midst of an exception stack
    	if(currentException != null) {
			if(ret == null) ret = new LinkedList<String>();
			ret.add(currentException.toString().trim());
    	}
    	return ret;
    }
    
    /**
     * Extract exceptions from a log file (eg. an app server log file).
     * @param log The log file from which exceptions must be extracted.
     * @return A list of exceptions extracted in the input stream, null if none.
     * @throws IOException
     */
    public static List<String> extractExceptions(File log) throws IOException {
    	BufferedReader in = null;
    	try {
    		in = new BufferedReader(new FileReader(log));
    		return extractExceptions(in);
    	} catch(IOException e) {
    		throw e;
    	} finally {
    		if(in != null) in.close();
    	}
    }
    
    /**
     * Extract exceptions from a log stream (eg. an app server log file), separating nested exceptions.
     * @param in The log stream (file or so) from which exceptions must be extracted.
     * @return A list of exceptions extracted in the input stream, null if none.
     * @throws IOException
     */
    public static List<String> explodeExceptions(BufferedReader in) throws IOException {
    	LinkedList<String> ret = null;   	
    	String line;
    	String previous = "";
    	// Pattern more permissive than it could : should be ^\\t and never ^(\\t|\\s+)
    	// but required when exceptions are carelessly copied/pasted (with tabs replaced by spaces)
    	Pattern atMore = Pattern.compile("^(\\t|\\s+)at |^(\\t|\\s+)... \\d+ more");
    	Pattern nested = Pattern.compile("; nested exception is(.*)$");
    	StringBuffer currentException = null;
    	while((line = in.readLine()) != null) {
    		Matcher matcher = atMore.matcher(line);
    		if(matcher.find()) {
    			Matcher nestedMatcher = nested.matcher(previous);
    			if(nestedMatcher.find()) previous = nestedMatcher.group(1);
    			if(previous.startsWith("Caused by: ")) {
     				previous = previous.substring(11);
     			}
    			if(currentException == null) currentException = new StringBuffer(previous);
    			if(line.endsWith(")") && ! line.endsWith("Native Method)")) {
    				currentException.append("\n" + line);
    			}
    		} else if(currentException != null) {
    			if(ret == null) ret = new LinkedList<String>();
    			ret.add(currentException.toString().trim());
    			currentException = null;
    		}
    		previous = line;
    	}
    	// Don't forget last exception if EOF at the midst of an exception stack
    	if(currentException != null) {
			if(ret == null) ret = new LinkedList<String>();
			ret.add(currentException.toString().trim());
    	}
    	return ret;
    }

    /**
     * Extract exceptions from a log file (eg. an app server log file), separating nested exceptions.
     * @param log The log file from which exceptions must be extracted.
     * @return A list of exceptions extracted in the input stream, null if none.
     * @throws IOException
     */
    public static List<String> explodeExceptions(File log) throws IOException {
    	BufferedReader in = null;
    	try {
    		in = new BufferedReader(new FileReader(log));
    		return explodeExceptions(in);
    	} catch(IOException e) {
    		throw e;
    	} finally {
    		if(in != null) in.close();
    	}
    }
}
