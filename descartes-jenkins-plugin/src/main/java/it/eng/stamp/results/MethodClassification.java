package it.eng.stamp.results;

public enum MethodClassification {
	 	TESTED("tested"),
	    PSEUDO_TESTED("pseudo-tested"),
	    PARTIALLY_TESTED("partially-tested"),
	    NOT_COVERED("not-covered");

	    MethodClassification(String name) {
	        this.name = name;
	    }

	    private String name;

	    public String toString() {
	        return name;
	    }
	    
	    public static MethodClassification fromString(String text) {
	        for (MethodClassification b : MethodClassification.values()) {
	          if (b.name.equalsIgnoreCase(text)) {
	            return b;
	          }
	        }
	        throw new IllegalArgumentException();
	      }

}
