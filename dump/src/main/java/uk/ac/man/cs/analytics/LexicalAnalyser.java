package uk.ac.man.cs.analytics;

import uk.ac.man.cs.metrics.SignatureMetric;
import uk.ac.man.cs.metrics.StringPattern;
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.util.StringMangler;
import uk.ac.man.cs.util.IOHelper;

import com.opencsv.CSVWriter;
import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.codp.CODP;
import uk.ac.man.cs.detectors.LexicalPatternDetector;
import uk.ac.man.cs.detectors.LogicalPatternDetector;

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

public class LexicalAnalyser {

    private String resultPath;
    private String analyticsPath;
    private String patternKeyPath;

    public LexicalAnalyser(String rPath, String aPath){
        this.resultPath = rPath;
        this.analyticsPath = aPath + "/LexicalAnalytics";

        //create Folder structure
        IOHelper io = new IOHelper();
        io.createFolder(aPath + "/LexicalAnalytics");
    }

    public LexicalAnalyser(String rPath, String aPath, String kPath){
        this.resultPath = rPath;
        this.analyticsPath = aPath + "/LexicalAnalytics";
        this.patternKeyPath = kPath;

        //create Folder structure
        IOHelper io = new IOHelper();
        io.createFolder(aPath + "/LexicalAnalytics");
    }

    public void analyseResults() throws IOException {
        this.analyseMetricTypeCounts();
        this.writeCoverageOfKeys();
    }

    //currently only extracts keys (DEAL WITH IT)
    //this potentially over estimates the coverage
    //because the entity "thatTime" in an ontology
    //will match both "thatTime" and "time" in a
    //key set for a pattern. So the 'coverage'
    //is larger than it truly is.
    private double computeCoverage(File file, Set<String> keys) throws IOException {
        Set<String> potentialHits = parseKeysFromFile(file);
        int count = 0;
        for (String s : potentialHits){
            for (String k : keys){
                if(k.toLowerCase().equals(s.toLowerCase()))
                    count++; 
            } 
        } 

        return (double) count / keys.size();
    }

    private Set<String> parseKeysFromFile(File file) throws IOException{
       Set<String> coveredKeys = new HashSet<>();
       try (BufferedReader br = new BufferedReader(new FileReader(file))) {
           String line = br.readLine();//get rid of first line (which only specifies the format and does not contain data) 
           while ((line = br.readLine()) != null) {
               String [] parts = line.split(",");
               coveredKeys.add(parts[0].toLowerCase());
           }
       }
       return coveredKeys;
    }


    //for each pattern: count number of ontologies wrt the 3 different evidence types
    //(1) annotation
    //(2) signature
    //(3) coOccurrence
    private void analyseMetricTypeCounts(){

        //look at 'lexical' result data
        String path = this.resultPath + "/lexical/"; 
        File lexicalResultDir = new File(path);

        //iterate over data on all patterns
        for (File patDir : lexicalResultDir.listFiles()) {

            //create information object for statistics of a pattern
            File patternDir = new File(path + patDir.getName()); 
            PatternStatistics ps = new PatternStatistics(patDir.getName());

            //iterate over all ontologies with evidence data
            for (File ontDir : patternDir.listFiles()) {

                File evidenceDir = new File(path + patternDir.getName() + "/" + ontDir.getName());

                //iterate over all evidence files
                for (File evidenceFile : evidenceDir.listFiles()) {

                    String evidenceType = evidenceFile.getName();
                    String ontologyName = evidenceDir.getName();

                    //check presence of evidence files
                    //and get data
                    if(evidenceType.equals("annotation"))
                        ps.addAnnotation(ontologyName);
                    if(evidenceType.equals("signature"))
                        ps.addSignature(ontologyName);
                    if(evidenceType.equals("coOccurrence"))
                        ps.addCoOccurrence(ontologyName); 
                }
            }

            ps.writeStatistics(this.analyticsPath);
        }
    }

    private void writeCoverageOfKeys() throws IOException {

        //look at 'lexical' result data
        String path = this.resultPath + "/lexical/"; 
        File lexicalResultDir = new File(path);

        //iterate over data on all patterns
        for (File patDir : lexicalResultDir.listFiles()) {

            //create information object for statistics of a pattern
            File patternDir = new File(path + patDir.getName()); 
            PatternStatistics ps = new PatternStatistics(patDir.getName());

            //get keySet for pattern
            CODP pattern = new CODP(patDir.getName(), this.patternKeyPath +"/"+ patDir.getName());
            Set<String> patternKeys = pattern.getKeys();

            //iterate over all ontologies with evidence data
            for (File ontDir : patternDir.listFiles()) {
                double annotationCoverage = 0;
                double signatureCoverage = 0;

                File evidenceDir = new File(path + patternDir.getName() + "/" + ontDir.getName());

                //iterate over all evidence files
                for (File evidenceFile : evidenceDir.listFiles()) {
                    String evidence = evidenceFile.getName();

                    if(evidence.equals("annotation")){
                        annotationCoverage = computeCoverage(evidenceFile, patternKeys);

                    }
                    if(evidence.equals("signature")){
                        signatureCoverage = computeCoverage(evidenceFile, patternKeys);
                    } 
                } 
                writeCoverage(patDir.getName(), ontDir.getName(), annotationCoverage, signatureCoverage); 
            }
        }
    }

    private void writeCoverage(String patternName, String ontologyName, double annotationCoverage, double signatureCoverage){
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(analyticsPath+"/"+patternName, true));
                //Iterator it = this.evidence.entrySet().iterator();
                String output = ontologyName + ","+Double.toString(annotationCoverage)+","+Double.toString(signatureCoverage);
                //output format
                bw.write(output);
                bw.newLine();
                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } 


    }
}
