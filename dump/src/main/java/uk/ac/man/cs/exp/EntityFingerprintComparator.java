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
public class EntityFingerprintComparator {

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));

    public static void main(String[] args) throws IOException , Exception{

        String ontFilePath = args[0];
        String patFilePath = args[1];
        String outputPath = args[2]; 

        EntityFingerprintComparator comparator = new EntityFingerprintComparator(); 

        comparator.run(ontFilePath, patFilePath, outputPath);

    }

    private void run(String ontFilePath, String patDirPath, String outputPath) throws Exception {

        File ontFile = new File(ontFilePath);
        //File patFile = new File(patFilePath);
        File patDirFile = new File(patDirPath);

        log.info("\tLoading Ontology : " + ontFile.getName()); 
        OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
        OWLOntology ontology = ontLoader.getOntology();
        Set<OWLAxiom> ontologyAxioms = getLogicalAxioms(ontology, true);
        AxiomAnalyser ontologyAnalyser = new AxiomAnalyser(ontologyAxioms);

        for (File patFile : patDirFile.listFiles()) {

            log.info("\tLoading Pattern : " + patFile.getName()); 
            OntologyLoader patLoader = new OntologyLoader(patFile, true);
            OWLOntology pattern = patLoader.getOntology();
            Set<OWLAxiom> patternAxioms = getLogicalAxioms(pattern, true);
            AxiomAnalyser patternAnalyser = new AxiomAnalyser(patternAxioms);

            boolean allEntitiesCoverageCovered = true;

            for(OWLEntity entityPat : pattern.getSignature(Imports.EXCLUDED)){
                patternAnalyser.setSeed(entityUsage(entityPat, patternAxioms));
                //System.out.println("====Pattern Entity Fingerprint===="); 
                //System.out.println(entityPat.toString());
                //System.out.println(patternAnalyser.fingerprint());
                boolean entityUsageCovered = false;
                for(OWLEntity entityOnt: ontology.getSignature(Imports.INCLUDED)){
                    ontologyAnalyser.setSeed(entityUsage(entityOnt, ontologyAxioms));
                    //System.out.println("====Ontology Entity Fingerprint===="); 
                    //System.out.println(entityOnt.toString());
                    //System.out.println(ontologyAnalyser.fingerprint());
                    if(ontologyAnalyser.coversFingerprint(patternAnalyser)){
                        entityUsageCovered = true; 
                        //System.out.println("Entity " + entityPat.toString() + " covered by " + entityOnt.toString());
                        //System.out.println("====Pattern Entity Fingerprint===="); 
                        //System.out.println(patternAnalyser.fingerprint());
                        //System.out.println("====Ontology Entity Fingerprint===="); 
                        //System.out.println(ontologyAnalyser.fingerprint());
                        break;
                    } 
                }
                if(!entityUsageCovered){
                    allEntitiesCoverageCovered = false; 
                    break;
                }
            }

            String outputPathForPattern = outputPath + "/" + patFile.getName(); 
            IOHelper.createFolder(outputPathForPattern); 

            String outputPathForViolations = outputPathForPattern + "/violations";
            String outputPathForMatches = outputPathForPattern + "/matches";


            if(allEntitiesCoverageCovered){
                IOHelper.writeAppend(ontFile.getName(), outputPathForMatches); 
            } else { 
                IOHelper.writeAppend(ontFile.getName(), outputPathForViolations); 
            } 
        } 
    }

    private Set<OWLAxiom> getLogicalAxioms(OWLOntology ont, boolean includeClosure) {
        Set<OWLLogicalAxiom> logicalAxioms = ont.getLogicalAxioms(includeClosure);
        Set<OWLAxiom> axioms = new HashSet<>();  
        for(OWLLogicalAxiom axiom : logicalAxioms){
            axioms.add((OWLAxiom) axiom);
        }

        return axioms;
    }


    private Set<OWLAxiom> entityUsage(OWLEntity entity, Set<OWLAxiom> axioms){
        Set<OWLAxiom> res = new HashSet<>(); 
        for(OWLAxiom axiom : axioms){
            if((axiom.getSignature()).contains(entity)){
                    res.add(axiom);
            }
        }
        return res; 
    }

}
