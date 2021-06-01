package uk.ac.man.cs.detectors;

import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.lodp.*;
import uk.ac.man.cs.util.*;

import java.io.*;
import java.util.*;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.apibinding.OWLManager;
import java.util.Iterator;

import org.semanticweb.owlapi.model.*;

import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
//import org.semanticweb.owlapi.reasoner.OWLOntologyCreationException;
import org.semanticweb.owlapi.util.AbstractOWLStorer;
import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;

/**
 * The PatternDector implements an algorithm for detecting
 * *Logical* Ontology DesignPatterns (ODP) in Ontologies.
 *
 * @author Christian Kindermann
 * @version 1.0
 * @since 2018-08-08
 *
 */
public class LogicalPatternDetector extends PatternDetector {

    private Partition partition;
    private NaryRelation nary;
    

    /**
     * Constructor
     */
    public LogicalPatternDetector(OWLOntology o){
        this.ontology = o; 
        this.partition = new Partition(this.ontology);
        this.nary = new NaryRelation(this.ontology);

        this.description = "The 'LogicalPatternDetector' " +
           "looks for evidence of the following patterns: \n";
           //"\t Partition : " + this.partition.getDescription();
    }

    public void run(){
        this.partition.computeEvidence();
        this.nary.computeEvidence();
    }

    public void writeEvidence(String destFile){
        this.partition.writeEvidence(destFile + "/partition"); 
        this.nary.writeEvidence(destFile + "/nary");
    }

    public void setOntology(OWLOntology o){
        this.ontology = o;
        this.partition.reset();

    }

    public void reset(){
        this.partition.reset(); 
    }
}
