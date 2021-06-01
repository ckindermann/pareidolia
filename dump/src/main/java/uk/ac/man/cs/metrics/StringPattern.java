package uk.ac.man.cs.metrics;

import org.semanticweb.owlapi.model.OWLOntology;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringPattern {

    private Pattern pattern;
    private String BAD_CHARS = "[^a-z]";

    public StringPattern(String p){
        this.pattern=Pattern.compile(normalise(p));
    }

    private String normalise(String s){
        String res = s.toLowerCase();
        res = res.replaceAll(BAD_CHARS,"");
        return res; 
    }

    public boolean findIn(String s){
        Matcher ma = this.pattern.matcher(normalise(s));
        return ma.find(); 
    }

}
