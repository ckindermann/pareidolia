package uk.ac.man.cs.analytics;

import uk.ac.man.cs.metrics.SignatureMetric;
import uk.ac.man.cs.metrics.StringPattern;

import com.opencsv.CSVWriter;
import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.detectors.LexicalPatternDetector;
import uk.ac.man.cs.detectors.LogicalPatternDetector;
import uk.ac.man.cs.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;


import java.util.*;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;

import org.semanticweb.owlapi.model.*;


//import org.semanticweb.owlapi.reasoner.OWLOntologyCreationException;
import org.semanticweb.owlapi.util.AbstractOWLStorer;
import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

/**
 * Created by chris on 21/08/18.
 */

public class PatternStatistics {
    String patternName;
    Set<String> annotationEvidence;
    Set<String> signatureEvidence;
    Set<String> coOccurrenceEvidence;

    public PatternStatistics(String n){
        this.patternName = n;
        this.annotationEvidence = new HashSet<>();
        this.signatureEvidence = new HashSet<>();
        this.coOccurrenceEvidence = new HashSet<>();
        }

    public void addAnnotation(String ontologyName){
        this.annotationEvidence.add(ontologyName); 
    }

    public void addSignature(String ontologyName){
        this.signatureEvidence.add(ontologyName); 
    }

    public void addCoOccurrence(String ontologyName){
        this.coOccurrenceEvidence.add(ontologyName); 
    }

    public void writeStatistics(String destFile){
        this.writeNumbers(destFile); 
    }

    public boolean writeNumbers(String destFile){

        boolean success = false;
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(destFile+"/numbers", true));
                //Iterator it = this.evidence.entrySet().iterator();
                String output = this.patternName+","+String.valueOf(this.annotationEvidence.size())+","+String.valueOf(this.signatureEvidence.size())+","+String.valueOf(this.coOccurrenceEvidence.size());
                //output format
                bw.write(output);
                bw.newLine();
                bw.close();
                success = true;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } 
        return success; 
    }
}

