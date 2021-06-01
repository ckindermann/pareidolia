package uk.ac.man.cs.exp;

import com.opencsv.CSVWriter;
import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.codp.CODP;
import uk.ac.man.cs.detectors.LexicalPatternDetector;
import uk.ac.man.cs.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.Iterator;
import java.io.BufferedWriter;
import java.io.BufferedReader;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyStorageException ;
import org.semanticweb.owlapi.model.OWLDocumentFormat;;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLAxiomIndex;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.HasAnnotationPropertiesInSignature;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.HasAxioms;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
//import org.semanticweb.owlapi.reasoner.OWLOntologyCreationException;
import org.semanticweb.owlapi.util.AbstractOWLStorer;
import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

/**
 * Created by chris on 07/04/18.
 */

public class KeyExtraction {

    private String outputPath;

    public KeyExtraction(String out) {
        this.outputPath = out;
    }

    private static final Logger log = Logger.getLogger(String.valueOf(KeyExtraction.class));

    public static void main(String[] args) throws IOException, OWLOntologyStorageException
    {

        File ontDir = new File(args[0]);
        File patternDir = new File(args[1]);
        File keysDir = new File(args[2]);

        ConsoleHandler handler = new ConsoleHandler();
        log.addHandler(handler);
        handler.setLevel(Level.ALL);

        KeyExtraction exp = new KeyExtraction(patternDir.getParentFile().getPath() + "/patternKeysExtracted2/");

        exp.collectPatternKeys(patternDir);
   }

   private void collectPatternKeys(File patternDir) throws IOException{

        int patternCount = 0;
        File destDir = new File(this.outputPath);
        destDir.mkdirs();

        String file;

        //Exceptions for extracting keys automatically
        Set<String> exclude = new HashSet<>();
        exclude.add("spatiotemporalextent.owl");

        for (File patternFile : patternDir.listFiles()) {
            log.info("\tLoading Pattern " + ++patternCount + " : " + patternFile.getName());
            if(exclude.contains(patternFile.getName())){
                log.info("\tSkip Pattern " + ++patternCount + " : " + patternFile.getName());
                continue; 
            }
            CODP pattern = new CODP(patternFile);
            Set<String> keys = pattern.extractKeys();

            //file name
            file = this.outputPath + patternFile.getName();
            writeStringSet(file, keys);

        }
            return; 
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
