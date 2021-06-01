package uk.ac.man.cs.stats;

import uk.ac.man.cs.codp.CODP;
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.util.StringMangler;
import uk.ac.man.cs.data.*;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.ont.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import java.util.HashSet;
import java.util.HashMap;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.*;
import java.util.Set;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.io.File;
import java.io.FileWriter;
import org.semanticweb.owlapi.search.EntitySearcher;
import java.util.stream.*;
import java.io.FileReader;

import java.io.*;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by chris on 10/08/18.
 */

public class ReuseStatistics {

    private OntologyStatistics ontologyStatistics;
    private PatternStatistics patternStatistics;

    private OWLOntology ontology; 

    private List<LexicalMatch> instanceSubstitution;
    private HashMap<OWLEntity,OWLEntity> substitutionMapping;

    private OWLOntology originalPattern;
    private OWLOntology instantiatedPattern;

    public ReuseStatistics(List<LexicalMatch> instanceSub, OWLOntology o, OWLOntology orig, OWLOntology inst) throws Exception {

        this.ontology = o;
        this.originalPattern = orig;
        this.instantiatedPattern = inst;
        this.instanceSubstitution = instanceSub;
        this.substitutionMapping = new HashMap();
        this.initMapping();

        this.ontologyStatistics = new OntologyStatistics(o, inst, this.substitutionMapping);
        this.patternStatistics = new PatternStatistics(orig, inst, this.substitutionMapping);
    }

    public ReuseStatistics(){
        this.ontology = null;
        this.originalPattern = null;
        this.instantiatedPattern = null;
        this.instanceSubstitution = null;
        this.substitutionMapping = new HashMap();

        this.ontologyStatistics = new OntologyStatistics(null, null, null);
        this.patternStatistics = new PatternStatistics(null, null, null); 
    }

    public void getMaxStatistics(ReuseStatistics rs){
        this.ontologyStatistics.getMaxStatistics(rs.getOntologyStats());
        this.patternStatistics.getMaxStatistics(rs.getPatternStats()); 
    }

    public void compute() throws Exception {
        this.ontologyStatistics.computeReuse();
        this.patternStatistics.compute(); 
    }


    public void initMapping(){ 
        Iterator<LexicalMatch> it = this.instanceSubstitution.iterator();
        if(it.hasNext()){ 
            LexicalMatch lm = it.next();
            this.substitutionMapping.put(lm.getSource(), lm.getAssociation());
        } 
    }

    public void dropData(){
        this.ontologyStatistics.dropData();
        this.patternStatistics.dropData(); 
    }

    public void printStats(){
        this.ontologyStatistics.print();
        this.patternStatistics.print(); 
    } 

    public OntologyStatistics getOntologyStats(){ 
        return this.ontologyStatistics;
    }

    public PatternStatistics getPatternStats(){ 
        return this.patternStatistics;
    }
}
