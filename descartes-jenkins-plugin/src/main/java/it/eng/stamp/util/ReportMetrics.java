package it.eng.stamp.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ReportMetrics {
	COVERAGE("tested"), 
	PSEUDO_TESTED("pseudo-tested"), 
	PARTIALLY_TESTED("partially-tested"), 
	NOT_COVERED("not-covered");

	ReportMetrics(String name) {
		this.name = name;
	}

	private String name;

	public String toString() {
		return name;
	}

	public static List<String> stringValues() {
		return Stream.of(ReportMetrics.values())
                .map(ReportMetrics::name)
                .collect(Collectors.toList());
	}
}
