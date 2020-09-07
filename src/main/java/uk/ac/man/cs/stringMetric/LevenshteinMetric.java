package uk.ac.man.cs.stringMetric;

import org.semanticweb.owlapi.model.OWLOntology;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import org.apache.commons.text.similarity.*;
import org.apache.commons.text.similarity.LevenshteinDistance;


//(lexial) pattern metric
//-uses string metrics for 
//--exact matches (always the same)
//--approximate (freedom)
//--similar (even more freedom)
//
//TODO: write wrapper for 
//org.apache.commons.text.similarity
//and accept editDistances / similarity scores
//that can be cast into a "lexical" metric in here

public class LevenshteinMetric implements StringMetric { 

    private String stringPattern;

    private Pattern originalPattern;
    private Pattern approximatePattern;// by Latin characters contained in the pattern

    private String NON_LATIN_CHARS = "[^a-z]";

    public LevenshteinMetric(String pattern){
        this.stringPattern = pattern; 
        //NOTE : pattern compilation is an expensive operation
        this.originalPattern = Pattern.compile(pattern);
        this.approximatePattern = Pattern.compile(approximate2latinChars(pattern));
    }

    public LevenshteinMetric(){
    }

    //TODO
    public boolean approximate(String s1, String s2, double threshold){
        return false;
    }

    //for this we'd need a threshold (define a default?)
    //TODO
    public boolean similar(String s1, String s2, double threshold){

        return false;
    }

    public double approximateScore(String s1, String s2){
        return 0.0;
    }

    //TODO  (isn't this already finished?)
    public double similarScore(String s1, String s2){
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater
                                            // length
            longer = s2;
            shorter = s1;
        }

        int longerLength = longer.length(); 
        //both strings are zero length
        if (longerLength == 0) {
            return 1.0;
        }

        LevenshteinDistance levenshteinDistance = LevenshteinDistance.getDefaultInstance();
        return (longerLength - levenshteinDistance.apply(longer, shorter).intValue()) / (double) longerLength;
    }

    //TODO 
    private String approximate2latinChars(String s){
        String res = s.toLowerCase();
        res = res.replaceAll(NON_LATIN_CHARS,"");
        return res; 
    } 
}
