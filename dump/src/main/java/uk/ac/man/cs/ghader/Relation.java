package uk.ac.man.cs.ghader;

import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;

import org.semanticweb.owlapi.model.*;
import java.io.*;
import java.util.Set;
import java.util.*;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Created by chris on 23/11/18.
 */
public class Relation {

    private String name; 

    private HashSet<RelationMatch> matches;

    public Relation(String r) { 
        this.name = r;
        this.matches = new HashSet<>();

    } 

    public String getName(){
        return this.name; 
    }

    public HashSet<RelationMatch> getMatches(){
        return this.matches; 
    }

    public void addMatch(RelationMatch m){
        this.matches.add(m); 
    }
}

