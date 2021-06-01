package uk.ac.man.cs.detectors;

import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.codp.CODP;
import uk.ac.man.cs.metrics.*;
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
 * Ontology Design Patterns (ODP) in Ontologies based on
 * *lexical* indications
 *
 * @author Christian Kindermann
 * @version 1.0
 * @since 2018-04-06
 *
 */
public class LexicalPatternDetector extends PatternDetector {

    //Pattern to look for
    private CODP ontologyPattern;

    //2 types of evidence:
    private SignatureMetric signatureMetric; //(1) annotation
    private AnnotationMetric annotationMetric; //(2) signature

    /**
     * Constructor
     */
    public LexicalPatternDetector(OWLOntology o, CODP p){
        this.ontology = o;
        this.ontologyPattern = p;

        //lexical metrics
        this.signatureMetric = new SignatureMetric(o,p);
        this.annotationMetric = new AnnotationMetric(o,p);

        this.description = "The 'LexicalPatternDetector' looks for evidence " +
            "of Ontology Design Patterns using the following metrics"; 
    }

    public LexicalPatternDetector(OWLOntology o){
        this.ontology = o;
        this.ontologyPattern = null;
        this.description = "The 'LexicalPatternDetector' looks for evidence " +
            "of Ontology Design Patterns using the following metrics"; 
    }

    /**
     * Evidence 
     */

    public void run(){
        this.annotationMetric.compute();
        this.signatureMetric.compute();
        //this.signatureMetric.computeCoOccurrence(); 
    }


    public void writeEvidence(String path){
        this.annotationMetric.write(path);
        this.signatureMetric.write(path);
    }

    public void reset(){
        this.signatureMetric.reset();
        this.annotationMetric.reset();
    }


    /**
     * Getter Methods
     */

    public HashMap<String, Set<String>> getSignatureEvidence(){
        return this.signatureMetric.getEvidence();
    } 

    public HashMap<String, Pair<OWLEntity, Set<String>>> getAnnotationEvidence(){
        return this.annotationMetric.getEvidence();
    }

    public HashMap<Pair<String,OWLEntity>, Set<Pair<String,OWLEntity>>> getCoOccurrence(){
        return this.signatureMetric.getCoOccurrence();
    }

    public CODP getPattern(){
        return this.ontologyPattern; 
    }

    public AnnotationMetric getAnnotationMetric(){
        return this.annotationMetric; 
    }

    public SignatureMetric getSignatureMetric(){
        return this.signatureMetric; 
    }

    public boolean strongEvidence(){
        return this.signatureMetric.strongEvidence();
    }

    /**
     * Setter Methods
     */
    public void setPattern(CODP p){
        this.ontologyPattern = p; 
    }

    public void setOntology(OWLOntology o){
        this.ontology = o;
        this.reset();
    }

    public void setCoOccurrenceLevel(int l){
        this.signatureMetric.setCoOccurrenceLevel(l); 
    }
    
}
