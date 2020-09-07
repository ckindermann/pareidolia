package uk.ac.man.cs.stringMetric;

interface StringMetric {
    //TODO: the idea here is that any 'StringMetric' can be used for pattern matching
    //people will only need to choose what they prefer.

    //exact match
    public static boolean equal(String s1, String s2){
        return s1.equals(s2); 
    }

    public boolean approximate(String s1, String s2, double threshold);
    public boolean similar(String s1, String s2, double threshold);

    public double approximateScore(String s1, String s2);
    public double similarScore(String s1, String s2);
}
