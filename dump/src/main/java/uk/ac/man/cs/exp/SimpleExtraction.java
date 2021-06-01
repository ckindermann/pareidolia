package uk.ac.man.cs.exp;

import uk.ac.man.cs.metrics.SignatureMetric;
import uk.ac.man.cs.metrics.StringPattern;
import uk.ac.man.cs.analytics.*;

import com.opencsv.CSVWriter;
import com.opencsv.CSVReader;
import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.codp.CODP;
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
 * Created by chris on 07/04/18.
 */

public class SimpleExtraction {

    boolean init;
    //String outputPath = "/home/chris/PhD/1year/ODP_study/patternExtraction";

    public SimpleExtraction() {
        this.init = true;
    }

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));


    //public static void main(String[] args) {
    //    OWLOntologyManager man = OWLManager.createOWLOntologyManager();
    //    try {
    //        OWLOntology o = man.loadOntologyFromOntologyDocument(new File("/home/chris/Downloads/agentrole.owl"));
    //        man = null;
    //        testClosure(o);
    //    } catch (OWLOntologyCreationException e) {
    //        e.printStackTrace();
    //    }

    //}

    //public static void testClosure(OWLOntology o){
    //        System.out.println("Closure");
    //        for(OWLOntology imp:o.getImportsClosure()) {
    //            System.out.println(imp.getOntologyID().getOntologyIRI());
    //        }
    //        System.out.println("Imports");
    //        for(OWLOntology imp:o.getImports()) {
    //            System.out.println(imp.getOntologyID().getOntologyIRI());
    //        }
    //        System.out.println("Direct imports");
    //        for(OWLOntology imp:o.getDirectImports()) {
    //            System.out.println(imp.getOntologyID().getOntologyIRI());
    //        }
    //}

    public static void main(String[] args) throws IOException, OWLOntologyStorageException
    {

        File ontDir = new File(args[0]);//ontologies to be searched
        File patternDir = new File(args[1]);//owl ontologies for patterns
        File keysDir = new File(args[2]);//signature of patterns
        String outputPath = args[3];//where results are written to

        SimpleExtraction exp = new SimpleExtraction();

        exp.detectLexicalPatterns(ontDir, keysDir, patternDir, outputPath);
        //exp.detectLogicalPatterns(ontDir, keysDir, patternDir, outputPath);
        //exp.checkNamespace(ontDir, outputPath);
        //exp.runAnalytics(path + "/results", path + "/results/analytics", outputPath);
   }

   //private void runAnalytics(String results, String analytics, String patternKeys) throws IOException {
   //    Analyser analyser = new Analyser(results, analytics, patternKeys);
   //    analyser.analyseResults(); 
   //}

    private void detectLogicalPatterns(File ontDir, File patternKeysDir, File patternArtefactDir, String outputPath) throws IOException {
        log.info("Detecting Logical Ontology Design Patterns");
        int ontCount = 0;

        String path = outputPath + "/results/LODP";
        createFolder(path);

        for (File ontFile : ontDir.listFiles()) {
            int patCount = 0;
            log.info("\tLoading Ontology " + ++ontCount + " : " + ontFile.getName());


            //get ontology
            OntologyLoader loader = new OntologyLoader(ontFile, false);
            OWLOntology ont = loader.getOntology();
            LogicalPatternDetector detector = new LogicalPatternDetector(ont);
            detector.run();

            String outputFile = path + "/" + ontFile.getName();
            detector.writeEvidence(outputFile); 
        }
    }

    private void generateSPARQLforLODP(File ontDir, File disjointAxiomDir, File queryDir) throws IOException {
        File destDir = new File(ontDir.getParentFile().getPath() + "/SPARQL/LODP");
        destDir.mkdir();
        //TODO: formulate query


    }

    private void detectLexicalPatterns(File ontDir, File patternKeysDir, File patternArtefactDir, String outputPath) throws IOException {

        log.info("Detecting Ontology Design Patterns (lexicographically)");
        int ontCount = 0;//counter for ontologies

        //setup output folder structure
        String path = outputPath + "/results/lexical/";
        createFolder(path);

        //for all ontologies
        for (File ontFile : ontDir.listFiles()) {
            log.info("\tLoading Ontology " + ++ontCount + " : " + ontFile.getName());
            int patCount = 0;//counter for patterns

            //get ontology (without including imports)
            OntologyLoader loader = new OntologyLoader(ontFile, false);
            OWLOntology ont = loader.getOntology();


            //set for patterns that exihibit high indication of usage
            Set<String> highIndication = new HashSet<>();

            //for all patterns
            for (File patternFile : patternKeysDir.listFiles()) {
                log.info("\tChecking pattern " + ++patCount + " : " + patternFile.getName());

                //get pattern keys
                CODP pattern = new CODP(patternFile.getName(), patternFile);

                //configure pattern detector and run it
                LexicalPatternDetector detector = new LexicalPatternDetector(ont, pattern);
                //detector.setNamespace("ontologydesignpattern");
                detector.setCoOccurrenceLevel(2);
                detector.run();

                //write results
                String outputFile =  path + patternFile.getName() + "/" + ontFile.getName();
                createFolder(outputFile);
                detector.writeEvidence(outputFile);

                if(detector.strongEvidence()){
                    highIndication.add(patternFile.getName()); 
                }
            }

            //summary of high evidence for patterns in an ontology
            if(!highIndication.isEmpty()){
                String outputFile = path + "/results/highLexicalIndication";
                createFolder(outputFile);
                writeStringSet(outputFile + "/" + ontFile.getName(), highIndication); 
            }

        }
    }

    private void checkNamespace(File ontDir, String outputPath) throws IOException {

        log.info("Detecting Ontology Design Patterns (lexicographically)");
        int ontCount = 0;//counter for ontologies

        //setup output folder structure
        String path = outputPath + "/results/lexical/";
        createFolder(path);

        //for all ontologies
        for (File ontFile : ontDir.listFiles()) {
            log.info("\tLoading Ontology " + ++ontCount + " : " + ontFile.getName());
            int patCount = 0;//counter for patterns

            //get ontology (without including imports)
            OntologyLoader loader = new OntologyLoader(ontFile, false);
            OWLOntology ont = loader.getOntology();

            //TODO: read these from files
            //for all namespaces
            Set<String> namespaces = new HashSet<>();
            namespaces.add("ontologydesignpattern");//webpage
            namespaces.add("purl.org/dc/terms");//w3c 
            namespaces.add("purl.org/dc/dcam");
            namespaces.add("purl.org/dc/elements/1.1/subject");

            LexicalPatternDetector detector = new LexicalPatternDetector(ont);
            String outputFile = path + "namespace" + "/" + ontFile.getName();
            createFolder(outputFile);

            for(String s : namespaces){
                //detector.checkNamespace(s, outputFile + "/" + s.replaceAll("/","_")); 
            }
        }
    }


    private void createFolder(String path){
        File destDir = new File(path);
        destDir.mkdirs(); 
    }

    private void writeStringSet(String destFile, Set<String> patterns){
        if(!patterns.isEmpty()){
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(destFile));
                Iterator it = patterns.iterator();

                while(it.hasNext()){
                    bw.write((String) it.next());
                    bw.newLine();
                }
                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }
}
