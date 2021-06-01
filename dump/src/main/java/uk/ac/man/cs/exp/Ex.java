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
public class Ex {

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));

    public static void main(String[] args) throws IOException , Exception{

        File ontDir = new File(args[0]);//ontologies to be searched

        String ontFilePath = args[0];
        String patDirPath = args[1];
        //String instanceDirPath = args[2];
        String outputPath = args[2];

        Ex exp = new Ex(); 
        exp.run(ontFilePath, patDirPath, outputPath);

        //System.out.println("Hellow");
    }

    private void run(String ontFilePath, String patDirPath, String outputPath) throws Exception {

        File ontFile = new File(ontFilePath);
        File patDirFile = new File(patDirPath);

        log.info("\tLoading Ontology : " + ontFile.getName());
        OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
        OWLOntology ont = ontLoader.getOntology();

        CandidateEvaluator evaluator = new CandidateEvaluator(ont);


        //WANTED OUTPUT:
        //folder for pattern
        // -> folder for strong/weak
    //    -> folder for ontology
        //       --> files with different substitutions

        // -> folder for direct/entailed
        
        for (File patFile : patDirFile.listFiles()) {

            log.info("\tLoading Pattern : " + patFile.getName()); 
            OntologyLoader patLoader = new OntologyLoader(patFile, false);
            OWLOntology pattern = patLoader.getOntology(); 

            LexicalAssociation la = new LexicalAssociation(ont, pattern);
            String output = "";

            if(la.existsAssociation()){
                if(la.hasStrongSubstitution()){
                    //System.out.println("Inside has STROG");
                    CandidateGenerator generator = new CandidateGenerator(patFile);
                    generator.computeSubstitutions(la.getAssociations());

                    output = outputPath + "/" +
                            patFile.getName() +
                            "/strong/" +
                            ontFile.getName();

                    HashMap<Integer,List<LexicalMatch>> mapping = generator.writeSubstitutions(output + "/instance");

                    //iterate over all candidate files
                    File instanceDir = new File(output + "/instance");

                    evaluate(instanceDir, mapping, output, evaluator);
                    generator.cleanUp(output + "/instance");

                }
                else {
                    if(la.hasWeakSubstitution()){
                        CandidateGenerator generator = new CandidateGenerator(patFile);
                        generator.computeSubstitutions(la.getAssociationsWithAnnotations());

                        output = outputPath + "/" +
                                patFile.getName() +
                                "/weak/" +
                                ontFile.getName();

                        HashMap<Integer,List<LexicalMatch>> mapping = generator.writeSubstitutions(output + "/instance");

                        //iterate over all candidate files
                        File instanceDir = new File(output + "/instance");

                        evaluate(instanceDir, mapping, output, evaluator); 
                        generator.cleanUp(output + "/instance");

                    }
                }
            }

            //if(la.existsAssociation()){
            //    System.out.println("Substitution exists for " + patFile.getName() + " in Ontology " +ontFile.getName() + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            //    for(Map.Entry<OWLEntity, Set<OWLEntity>> entry : la.getAssociationsWithAnnotations().entrySet()){ 
            //        OWLEntity key = entry.getKey();
            //        Set<OWLEntity> values = la.getAssociationsWithAnnotations().get(key);
            //        System.out.println(key.toString() + " will be substitutied by " + values.toString());
            //    }

            //}
//
        }



        //Set<OWLAxiom> logicalAxioms = getLogicalAxioms(ont, true); 
        //for(OWLAxiom axiom : logicalAxioms){
        //    //OWLAxiom axiom2 = axiom.getAxiomWithoutAnnotations();
        //    //OWLAxiom axiom2 = OWLAxiom.getAxiomWithoutAnnotations(axiom);
        //    for(OWLEntity e : OWLAxiom.getAxiomWithoutAnnotations(axiom).getSignature()){
        //        System.out.println(e.getIRI().getShortForm()); 
        //    } 
        //}

        //DisjointUnionDetector detector = new DisjointUnionDetector(ont); 
        //detector.detectEntailedDisjointUnions();
        //for(DisjointUnion d : detector.getEntailedDisjointUnions()){
        //    d.print(); 
        //}
        //

        //AssertedUnionDetector detector = new AssertedUnionDetector(ont);
        //detector.detectAssertedUnions();
        //for(Union u : detector.getAssertedUnions()){
        //    u.print(); 
        //}

        //AssertedIntersectionDetector detector = new AssertedIntersectionDetector(ont);
        //detector.detectAssertedIntersections();
        //for(Intersection i : detector.getAssertedIntersections()){
        //    i.print(); 
        //}

        //AssertedRestrictionDetector detector = new AssertedRestrictionDetector(ont);
        //detector.detectRestrictions();
        //HashMap<OWLClassExpression, Restriction> data = detector.getRestrictions();

        //Iterator it = data.entrySet().iterator();
        //while (it.hasNext()) {
        //    Map.Entry pair = (Map.Entry)it.next();
        //    OWLClassExpression e = (OWLClassExpression) pair.getKey();
        //    Restriction eR = data.get(e); 

        //    System.out.println("On class: " + eR.getNamedClass().toString());
        //    for(OWLClassExpression expr : eR.getAsserted()){
        //        System.out.println("Direct: " + expr.toString()); 
        //    }
        //    for(Intersection intersection : eR.getIntersections()){
        //        System.out.println("Intersection: " + intersection.getIntersectionAxiom().toString()); 
        //    }
        //            
        //    it.remove(); // avoids a ConcurrentModificationException
        //}
        //

        //AxiomAnalyser analyser = new AxiomAnalyser(ont);
        //analyser.getStatistics();
        //analyser.print();
        //

        //ClassAxiomAnalyser analyser = new ClassAxiomAnalyser(ont);
        //analyser.run();
        //System.out.println("Number of Clusters: " + analyser.getClusters().size());
        //System.out.println("Is valid clustering: " + analyser.checkClustering());
        //analyser.printClusters();
        //analyser.printClassAxioms();

        //ClassEntityAnalyser analyser = new ClassEntityAnalyser(ont);
        //analyser.printUsage(analyser.entity2usage());

        //Set<OWLAxiom> tBox = ont.getTBoxAxioms(Imports.EXCLUDED); 

        //AxiomAnalyser analyser = new AxiomAnalyser(tBox);
        //analyser.fingerprint();

        //ClassAxiomAnalyser classAnalyser = new ClassAxiomAnalyser(tBox); 
        //classAnalyser.print();

        //PropertyAxiomAnalyser propertyAnalyser = new PropertyAxiomAnalyser(tBox); 
        //propertyAnalyser.print();

        //IndividualAxiomAnalyser individualAnalyser = new IndividualAxiomAnalyser(tBox);
        //individualAnalyser.print();

        //FingerprintAnalyser analyser = new FingerprintAnalyser(ontDirPath, outputPath);

        //OntologyChecker checker = new OntologyChecker(ontDirPath, outputPath);
        //checker.check();
        //

        //String bfoString = "bfo";
        //StringMatcher bfoMatcher = new StringMatcher(bfoString);


        //System.out.println(bfoMatcher.matchIn("BF+OOOO"));
        //System.out.println(bfoMatcher.matchIn("BaF+OOOO"));
        //System.out.println(bfoMatcher.matchIn("sdasBF+OOOO"));
        //System.out.println(bfoMatcher.occursIn("bfaasd"));
        //System.out.println(bfoMatcher.occursIn("bfoasd"));
        //System.out.println(bfoMatcher.occursIn("sdbfoasd"));
        //System.out.println(StringMatcher.similarity("bfo", "bfu"));

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
