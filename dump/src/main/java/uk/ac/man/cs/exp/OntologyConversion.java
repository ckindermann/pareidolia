package uk.ac.man.cs.exp;

import com.opencsv.CSVWriter;
import uk.ac.man.cs.ont.OntologyLoader;
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

public class OntologyConversion {

    private String outputPath;

    public OntologyConversion(String out){
        this.outputPath = out; 
    }

    private static final Logger log = Logger.getLogger(String.valueOf(OntologyConversion.class));


    public static void main(String[] args) throws IOException, OWLOntologyStorageException
    {
        File ontDir = new File(args[0]);
        String outputTo = args[1];


        OntologyConversion exp = new OntologyConversion(outputTo);
        exp.convertOntologies(ontDir);
    }

    private void convertOntologies(File ontDir) throws IOException, OWLOntologyStorageException
    {
        log.info("Checking ontologies");
        int ontCount = 0;

        File destDir = new File(this.outputPath);
        destDir.mkdir();

        for (File ontFile : ontDir.listFiles()) {

            log.info("\tLoading Ontology " + ++ontCount + " : " + ontFile.getName());

            OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
            OWLOntology ontology = ontLoader.getOntology();

            String file = this.outputPath + "/" + ontFile.getName();
            File output = new File(file);
            IRI documentIRI2 = IRI.create(output.toURI());

            //m.saveOntology(ontology, new OWLXMLDocumentFormat(), documentIRI2);
            ontLoader.getManager().saveOntology(ontology, new RDFXMLDocumentFormat(), documentIRI2);
        }
    }

}

