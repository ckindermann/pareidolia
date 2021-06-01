package uk.ac.man.cs.exp;

import uk.ac.man.cs.metrics.SignatureMetric;
import uk.ac.man.cs.metrics.StringPattern;
import uk.ac.man.cs.analytics.*;

import com.opencsv.CSVWriter;
import com.opencsv.CSVReader;
import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.detectors.LexicalPatternDetector;
import uk.ac.man.cs.detectors.LogicalPatternDetector;
import uk.ac.man.cs.detectors.NamespaceChecker;
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.util.IOHelper;

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

public class NamespaceCheck {

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));

    public static void main(String[] args) throws IOException, OWLOntologyStorageException
    {

        //expect ontologies to be in directory
        //expect patterns to be in directory
        //expect upper Level ontologies to be in directory
        //expect keywords to be in a file

        String ontFilePath = args[0];//ontology to be searched
        String patternKeywords = args[1];//pattern keys to be checked
        String upperLevelOntologyKeywords = args[2];//upper level ontology names
        String keywordFilePath = args[3];//keywords to be checked
        String outputPath = args[4];//where results are written to

        NamespaceCheck exp = new NamespaceCheck();
        exp.runCheck(ontFilePath, patternKeywords, upperLevelOntologyKeywords, keywordFilePath, outputPath);
    }

    private void runCheck(String ontFilePath,
                        String patternFilePath,
                        String upperLevelOntologyPath,
                        String keywordFilePath,
                        String outputPath) throws IOException {

        log.info("Checking Ontology's signature");
        log.info("Getting ontologies from directory " + ontFilePath);
        log.info("Getting pattern keywords from file " + patternFilePath);
        log.info("Getting upper level ontology keywords from file " + upperLevelOntologyPath);
        log.info("Getting general keywords from file " + keywordFilePath);
        log.info("Writing output to file " + outputPath);


        //get ontology
        File ontFile = new File(ontFilePath);
        log.info("\tLoading Ontology "  + ontFile.getName());
        OntologyLoader loader = new OntologyLoader(ontFile, true);
        OWLOntology ont = loader.getOntology();

        //get pattern keywords
        Set<String> patternKeys = IOHelper.readFile(patternFilePath);

        //get names of upper ontologies
        Set<String> upperLevelOntologyKeys = IOHelper.readFile(upperLevelOntologyPath);

        //get general keywords 
        Set<String> generalKeys = IOHelper.readFile(keywordFilePath);

        //setup output folder structure
        /**/ String path = outputPath + "/signature";
        /**/ String outputPatternKeys = path + "/patternKeys";
        /**/ String outputULOKeys = path + "/upperLevelOntologyKeys";
        /**/ String outputGeneralKeys = path + "/generalKeys";
        /**/
        /**/ IOHelper.createFolder(path);
        /**/ IOHelper.createFolder(outputPatternKeys);
        /**/ IOHelper.createFolder(outputULOKeys);
        /**/ IOHelper.createFolder(outputGeneralKeys);
        /**/ 
        /**/ for(String key : patternKeys){ 
        /**/     IOHelper.createFolder(outputPatternKeys + "/" + key + "/logicalAxioms"); 
        /**/     IOHelper.createFolder(outputPatternKeys + "/" + key + "/annotations"); 
        /**/     IOHelper.createFolder(outputPatternKeys + "/" + key + "/declaration"); 
        /**/ }
        /**/ 
        /**/ for(String key : upperLevelOntologyKeys){ 
        /**/     IOHelper.createFolder(outputULOKeys + "/" + key + "/logicalAxioms"); 
        /**/     IOHelper.createFolder(outputULOKeys + "/" + key + "/annotations"); 
        /**/     IOHelper.createFolder(outputULOKeys + "/" + key + "/declaration"); 
        /**/ }
        /**/ 
        /**/ for(String key : generalKeys){ 
        /**/     IOHelper.createFolder(outputGeneralKeys + "/" + key + "/logicalAxioms"); 
        /**/     IOHelper.createFolder(outputGeneralKeys + "/" + key + "/annotations"); 
        /**/     IOHelper.createFolder(outputGeneralKeys + "/" + key + "/declaration"); 
        /**/ }

        //run the import checker
        NamespaceChecker checker = new NamespaceChecker(ont, patternKeys);
        //checker.checkSignature();
        checker.checkLogicalAxioms();
        checker.write(ontFile.getName(), outputPatternKeys, "/logicalAxioms");
        checker.checkAnnotation();
        checker.write(ontFile.getName(), outputPatternKeys, "/annotations");
        checker.checkDeclarationAxioms();
        checker.write(ontFile.getName(), outputPatternKeys, "/declaration");

        checker.setKeys(upperLevelOntologyKeys);
        //checker.checkSignature();
        //checker.write(ontFile.getName(), outputULOKeys);
        checker.checkLogicalAxioms();
        checker.write(ontFile.getName(), outputULOKeys, "/logicalAxioms");
        checker.checkAnnotation();
        checker.write(ontFile.getName(), outputULOKeys, "/annotations");
        checker.checkDeclarationAxioms();
        checker.write(ontFile.getName(), outputULOKeys, "/declaration");

        checker.setKeys(generalKeys);
        //checker.checkSignature();
        //checker.write(ontFile.getName(), outputGeneralKeys);
        checker.checkLogicalAxioms();
        checker.write(ontFile.getName(), outputGeneralKeys, "/logicalAxioms");
        checker.checkAnnotation();
        checker.write(ontFile.getName(), outputGeneralKeys, "/annotations");
        checker.checkDeclarationAxioms();
        checker.write(ontFile.getName(), outputGeneralKeys, "/declaration");
    }
}

