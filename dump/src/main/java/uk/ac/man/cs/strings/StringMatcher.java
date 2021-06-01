package uk.ac.man.cs.strings;

import org.semanticweb.owlapi.model.OWLOntology;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringMatcher { 

    //I don't really make use of regexes here ...
    //the Pattern pattern could just be a string
    private String target;
    private Pattern pattern;
    private String BAD_CHARS = "[^a-z]";

    public StringMatcher(String p){
        this.target = p;
        //create a regular expression for the target string to
        //to be looked for (ignorig capitalisation & special
        //characters)
        this.pattern=Pattern.compile(normalise(p));
    }

    //strip all special characters 
    private String normalise(String s){
        String res = s.toLowerCase();
        res = res.replaceAll(BAD_CHARS,"");
        return res; 
    }

    public boolean equal(String s){
        return target.equals(s); 
    }

    public boolean matchIn(String s){
        Matcher ma = this.pattern.matcher(normalise(s));
        return ma.find(); 
    }

    public boolean occursIn(String s){
        return s.contains(this.target); 
    }

    public boolean searchIn(String s){
        if(this.matchIn(s))
            return true;
        if(this.similarity(this.target, s) > 0.8)
            return true;

        return false; 
    }

/**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     */
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater
                                            // length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0;
            /* both strings are zero length */ }
        /*
         * // If you have Apache Commons Text, you can use it to calculate the
         * edit distance: LevenshteinDistance levenshteinDistance = new
         * LevenshteinDistance(); return (longerLength -
         * levenshteinDistance.apply(longer, shorter)) / (double) longerLength;
         */
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }
    // Example implementation of the Levenshtein Edit Distance
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
}
