package uk.ac.man.cs.exp;

import uk.ac.man.cs.metrics.SignatureMetric;
import uk.ac.man.cs.metrics.StringPattern; 
import uk.ac.man.cs.analytics.*; 
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.util.IOHelper; 
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

import org.semanticweb.owlapi.util.AbstractOWLStorer;
import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

public class LexicalPatternDetection {

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));

    public static void main(String[] args) throws IOException, OWLOntologyStorageException
    {

        File ontDir = new File(args[0]);//ontologies to be searched
        File patternDir = new File(args[1]);//owl ontologies for patterns
        String keysPath = args[2];
        File keysDir = new File(keysPath);//signature of patterns
        String outputPath = args[3];//where results are written to

        LexicalPatternDetection exp = new LexicalPatternDetection();
        exp.runDetector(ontDir, keysDir, patternDir, outputPath); 
        exp.runAnalytics(outputPath, outputPath + "/Analytics", keysPath);
    }

    private void runDetector(File ontDir,
                            File patternKeysDir,
                            File patternArtefactDir,
                            String outputPath) throws IOException {

        log.info("Detecting Ontology Design Patterns (lexicographically)");
        int ontCount = 0;//counter for ontologies
        IOHelper io = new IOHelper();

        //setup output folder structure
        String path = outputPath + "/lexical/";
        io.createFolder(path);

        //for all ontologies
        for (File ontFile : ontDir.listFiles()) {
            log.info("\tLoading Ontology " + ++ontCount + " : " + ontFile.getName());
            int patCount = 0;//counter for patterns

            //get ontology (without including imports)
            OntologyLoader loader = new OntologyLoader(ontFile, true);
            OWLOntology ont = loader.getOntology();

            //set for patterns that exihibit "high indication" of usage
            Set<String> highIndication = new HashSet<>();

            //for all patterns
            for (File patternFile : patternKeysDir.listFiles()) {
                log.info("\tChecking pattern " + ++patCount + " : " + patternFile.getName());

                //get pattern keys (i.e. signature of pattern ontology)
                CODP pattern = new CODP(patternFile.getName(), patternFile);

                //configure pattern detector
                LexicalPatternDetector detector = new LexicalPatternDetector(ont, pattern);
                detector.setCoOccurrenceLevel(2);//>= 2 key terms occurr in the usage of an entity 
                detector.run();

                //write results
                String outputFile =  path + patternFile.getName() + "/" + ontFile.getName();
                io.createFolder(outputFile);
                detector.writeEvidence(outputFile);

                if(detector.strongEvidence()){
                    highIndication.add(patternFile.getName()); 
                }
            }

            //summary of high evidence for patterns in an ontology
            if(!highIndication.isEmpty()){
                String outputFile = outputPath + "/highLexicalIndication";
                io.createFolder(outputFile);
                io.writeStringSet(outputFile + "/" + ontFile.getName(), highIndication); 
            }

        }
    }

   private void runAnalytics(String results, String analytics, String patternKeys) throws IOException {
       LexicalAnalyser analyser = new LexicalAnalyser(results, analytics, patternKeys);
       analyser.analyseResults(); 
   }
}
