//package net.sf.extjwnl.utilities;
package uk.ac.man.cs.exp;

//import uk.ac.man.cs.detectors.structural.*;
import uk.ac.man.cs.detectors.structural.disjointUnion.*;
import uk.ac.man.cs.detectors.structural.union.*;
import uk.ac.man.cs.detectors.structural.intersection.*;
import uk.ac.man.cs.detectors.structural.restriction.*;
import uk.ac.man.cs.detectors.structural.restriction.existential.*;
import uk.ac.man.cs.detectors.*;

import uk.ac.man.cs.analysers.*;

import com.opencsv.CSVReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import uk.ac.man.cs.codp.*;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.stats.*;
import uk.ac.man.cs.data.*;
import uk.ac.man.cs.util.*;
//import uk.ac.man.cs.lodp2.*;
import uk.ac.man.cs.ghader.*;

import java.io.*;
import java.util.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.HermiT.ReasonerFactory;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;

/**
 * A class to demonstrate the functionality of the library.
 */
public class FingerprintGenerator {

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));

    public static void main(String[] args) throws IOException , Exception{

        String ontFilePath = args[0];
        String outputPath = args[1]; 

        FingerprintGenerator generator = new FingerprintGenerator(); 

        generator.run(ontFilePath, outputPath);

    }

    private void run(String ontFilePath, String outputPath) throws Exception {

        File ontFile = new File(ontFilePath);

        log.info("\tLoading Ontology : " + ontFile.getName()); 
        OntologyLoader ontLoader = new OntologyLoader(ontFile, false);
        OWLOntology ont = ontLoader.getOntology();

        //Set<OWLAxiom> tBox = ont.getTBoxAxioms(Imports.EXCLUDED); 
        Set<OWLLogicalAxiom> logicalAxioms = ont.getLogicalAxioms(false);
        //convert OWLLogicalAxioms to OWLAxioms (because I have implemented 
        //everything using OWLAxioms - might change it some time)
        Set<OWLAxiom> axioms = new HashSet<>();  
        for(OWLLogicalAxiom axiom : logicalAxioms){
            axioms.add((OWLAxiom) axiom);
        }

        AxiomAnalyser analyser = new AxiomAnalyser(axioms);
        String format = analyser.fingerprint();

        IOHelper.createFolder(outputPath); 

        IOHelper.writeAppend(format, outputPath + "/" + ontFile.getName());

    }

}
