package it.eng.stamp.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Analysis implements Serializable {

	private static final long serialVersionUID = 6267907206895444252L;

	private int time;

	private List<String> mutators = new ArrayList<String>();

	public Analysis() {
	}

	/**
	 * List of all mutators.
	 * @return the list of mutators
	 **/
	public List<String> getMutators() {
		return mutators;
	}

	/**
	 * duration of the run
	 * @return the duration of the Descartes run
	 */
	public int getTime() {
		return time;
	}

	public void setMutators(List<String> mutators) {
		this.mutators = mutators;
	}

	public void setTime(int time) {
		this.time = time;
	}
}