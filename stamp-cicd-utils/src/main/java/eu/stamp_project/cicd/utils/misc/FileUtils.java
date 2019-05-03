package eu.stamp_project.cicd.utils.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utilitary methods related to files.
 * @author Pierre-Yves Gibello - OW2
 *
 */
public class FileUtils {

	/**
	 * Delete file or directory (empty or not)
	 * @param file The file or directory to delete
	 * @throws IOException
	 */
	public static void deleteIfExists(File file) throws IOException {
		if(file == null || ! file.exists()) return;
		if (file.isDirectory()) {
			File[] entries = file.listFiles();
			if (entries != null) {
				for (File entry : entries) {
					deleteIfExists(entry);
				}
			}
		}
		if (!file.delete()) {
			throw new IOException("Failed to delete " + file);
		}
	}

	/**
	 * Write data to temporary file, and return path
	 * @param data The data to write
	 * @return A temporary file absolute path
	 * @throws IOException
	 */
	public static String tempFile(String data) throws IOException {
		File temp = File.createTempFile("stamp", null);
		temp.deleteOnExit();
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(temp));
			out.print(data);
		} catch(IOException e) {
			throw(e);
		} finally {
			if(out != null) out.close();
		}
		return temp.getAbsolutePath();
	}
	
	/**
	 * Read text file contents into a String
	 * @param file The text file to read
	 * @return The file contents
	 * @throws IOException
	 */
	public static String fileToString(File file) throws IOException {
		BufferedReader in = null;
		StringBuilder result = null;
		try {
			in = new BufferedReader(new FileReader(file));
			String line;
			result = new StringBuilder();
			boolean first = true;
			while((line = in.readLine()) != null) {
				result.append((first ? "" : "\n") + line);
				first = false;
			}
		} catch(IOException e) {
			throw(e);
		} finally {
			if(in != null) in.close();
		}
		return result.toString();
	}
}
