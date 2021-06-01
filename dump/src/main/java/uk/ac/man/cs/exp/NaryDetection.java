//package net.sf.extjwnl.utilities;
package uk.ac.man.cs.exp;

//import uk.ac.man.cs.detectors.structural.*;
import uk.ac.man.cs.detectors.structural.disjointUnion.*;
import uk.ac.man.cs.detectors.structural.union.*;
import uk.ac.man.cs.detectors.structural.intersection.*;
import uk.ac.man.cs.detectors.structural.restriction.*;
import uk.ac.man.cs.detectors.structural.restriction.existential.*;
import uk.ac.man.cs.detectors.*;
import uk.ac.man.cs.detectors.lexical.*;

import uk.ac.man.cs.analysers.*;

import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.strings.*;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.stats.*;
//import uk.ac.man.cs.data.*;
import uk.ac.man.cs.util.*;
//import uk.ac.man.cs.lodp2.*;
import uk.ac.man.cs.ghader.*;

import java.io.*;
import java.util.*;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
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
public class NaryDetection {

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));

    public static void main(String[] args) throws IOException , Exception{

        File ontDir = new File(args[0]);//ontologies to be searched

        String ontFilePath = args[0];
        String outputPath = args[1];

        NaryDetection exp = new NaryDetection(); 
        exp.run(ontFilePath, outputPath);

    }

    private void run(String ontFilePath, String outputPath) throws Exception {

        File ontFile = new File(ontFilePath);

        log.info("\tLoading Ontology : " + ontFile.getName());
        OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
        OWLOntology ont = ontLoader.getOntology();

        Set<OWLAxiom> tBox = ont.getTBoxAxioms(Imports.INCLUDED); 
        Set<OWLAxiom> disjointClassesAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.DISJOINT_UNION); 

        if(!disjointClassesAxioms.isEmpty())
            IOHelper.writeAppend(disjointClassesAxioms.size() + "", outputPath + "/" + ontFile.getName());

        //AssertedRestrictionDetector detector = new AssertedRestrictionDetector(ont);
        //detector.detectRestrictions();
        ////HashMap<OWLClassExpression, Restriction> data = detector.getRestrictions();
        //detector.write(outputPath + "/" + ontFile.getName());


    }

    private void evaluate(File instanceDir, HashMap<Integer,List<LexicalMatch>> mapping, String outputPath, CandidateEvaluator evaluator) throws Exception{ 
        int contained = 0;
        int entailed = 0;

        for(File candidateFile : instanceDir.listFiles()){
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology candidate = manager.loadOntologyFromOntologyDocument(candidateFile); 
            evaluator.setCandidate(candidate); 

            if(evaluator.isContained()){
                contained++;
                List<LexicalMatch> substitution = mapping.get(Integer.parseInt(candidateFile.getName()));
                writeSubstitution(substitution, outputPath + "/substitution/contained/",  candidateFile.getName()); 

            } else {
                if(evaluator.isEntailed()){
                    entailed++; 
                    List<LexicalMatch> substitution = mapping.get(Integer.parseInt(candidateFile.getName()));
                    writeSubstitution(substitution, outputPath + "/substitution/entailed/",  candidateFile.getName());

                }
            }
        }
    }


    private void writeSubstitution(List<LexicalMatch> sub, String output, String candidateName){
        IOHelper.createFolder(output);
        Iterator<LexicalMatch> matchIterator = sub.iterator();
        while(matchIterator.hasNext()){
            LexicalMatch match = matchIterator.next();
            OWLEntity original = match.getSource();
            OWLEntity substitution = match.getAssociation();
            String out = original.getIRI().toString() + " -> " + substitution.getIRI().toString(); 
            IOHelper.writeAppend(out, output + "/" + candidateName); 
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


}
