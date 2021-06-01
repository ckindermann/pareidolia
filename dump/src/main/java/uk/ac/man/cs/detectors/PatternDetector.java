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
 * Ontology DesignPatterns (ODP) in Ontologies.
 *
 * @author Christian Kindermann
 * @version 1.0
 * @since 2018-10-08
 *
 */
public abstract class PatternDetector {

    protected OWLOntology ontology;
    protected String description;

    public abstract void setOntology(OWLOntology o);
    public abstract void run();
    public abstract void reset();
    public abstract void writeEvidence(String destFile);

    public void printDescription(){
            System.out.println(this.description);
    }

    public String getDescription(){
        return this.description; 
    }

    public OWLOntology getOntology(){
        return this.ontology;
    } 
}
