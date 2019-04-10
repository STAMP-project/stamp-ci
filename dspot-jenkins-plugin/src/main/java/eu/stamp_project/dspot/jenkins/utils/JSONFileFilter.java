package eu.stamp_project.dspot.jenkins.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;

public class JSONFileFilter implements FileFilter, Serializable {

	private static final long serialVersionUID = -3888174172310577684L;

	public JSONFileFilter() {
	}
	
	@Override
	public boolean accept(File pathname) {
		return pathname.getName().endsWith(".json");
	}

}
