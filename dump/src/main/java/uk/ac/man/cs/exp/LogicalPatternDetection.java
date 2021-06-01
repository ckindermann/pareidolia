package uk.ac.man.cs.exp;

import uk.ac.man.cs.metrics.SignatureMetric;
import uk.ac.man.cs.metrics.StringPattern;
import uk.ac.man.cs.analytics.*;

import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.util.IOHelper;

import com.opencsv.CSVWriter;
import uk.ac.man.cs.ont.OntologyLoader;
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

/**
 * Created by chris on 27/08/18.
 */

public class LogicalPatternDetection {

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));

    public static void main(String[] args) throws IOException, OWLOntologyStorageException
    {

        File ontDir = new File(args[0]);//ontologies to be searched
        String outputPath = args[1];//where results are written to

        LogicalPatternDetection exp = new LogicalPatternDetection();

        exp.runDetector(ontDir, outputPath);
    }

    private void runDetector(File ontFile,
                            String outputPath) throws IOException {
        log.info("Detecting Logical Ontology Design Patterns");
        int ontCount = 0;
        IOHelper io = new IOHelper();

        String path = outputPath + "/results/LODP";
        io.createFolder(path);

        //for (File ontFile : ontDir.listFiles()) {
            int patCount = 0;
            log.info("\tLoading Ontology " + ++ontCount + " : " + ontFile.getName());

            //get ontology
            OntologyLoader loader = new OntologyLoader(ontFile, true);
            OWLOntology ont = loader.getOntology();
            LogicalPatternDetector detector = new LogicalPatternDetector(ont);
            detector.run();

            String outputFile = path + "/" + ontFile.getName();
            detector.writeEvidence(outputFile); 
        //}
    }

    //TODO: implement Analytics

}
