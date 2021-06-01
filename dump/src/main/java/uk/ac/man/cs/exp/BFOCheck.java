package uk.ac.man.cs.exp;

import uk.ac.man.cs.metrics.SignatureMetric;
import uk.ac.man.cs.metrics.StringPattern;
import uk.ac.man.cs.metrics.ImportMetric;
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
 * Created by chris on 03/09/18.
 */

//this is not a check, this is an analysis!
public class BFOCheck {

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));

    public static void main(String[] args) throws IOException, OWLOntologyStorageException
    {

        File ontDir = new File(args[0]);//ontologies to be searched
        String outputPath = args[1];//where results are written to

        BFOAnalyser bfoAnalyser = new BFOAnalyser(ontDir);
        //bfoAnalyser.computeBFOImport(ontDir, outputPath);
        bfoAnalyser.run();
        bfoAnalyser.write(outputPath);
    }
}

