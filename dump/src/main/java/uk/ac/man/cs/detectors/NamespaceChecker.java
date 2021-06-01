package uk.ac.man.cs.detectors;

import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.util.*;

import java.io.*;
import java.util.*;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.apibinding.OWLManager;
import java.util.Iterator;

import org.semanticweb.owlapi.model.*;
import uk.ac.man.cs.strings.*;

import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
//import org.semanticweb.owlapi.reasoner.OWLOntologyCreationException;
import org.semanticweb.owlapi.util.AbstractOWLStorer;
import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.*;

import java.util.regex.Matcher;
import uk.ac.man.cs.util.IOHelper;
import java.util.regex.Pattern;
import uk.ac.man.cs.metrics.NamespaceMetric;

import java.io.File;

/**
 * 
 *
 * @author Christian Kindermann
 * @version 1.0
 * @since 2018-26-08
 *
 */

public class NamespaceChecker {

    private OWLOntology ontology;

    private Set<String> keys; //check for these keys in the import closure
    private HashMap<String, Set<String>> key2directMatch;
    private HashMap<String, Set<String>> key2fuzzyMatch;
    private HashMap<String, Set<String>> key2similar;
    private double THRESHOLD = 0.8;

    public NamespaceChecker(OWLOntology o, Set<String> k){
        this.ontology = o;
        this.keys = k; 
        this.key2directMatch = new HashMap<>();
        this.key2fuzzyMatch = new HashMap<>();
        this.key2similar = new HashMap<>();
    }


    public void setKeys(Set<String> ks){
        this.resetKeys();
        this.keys = ks; 
    }

    private void resetKeys(){
        this.keys.clear();
        this.key2directMatch.clear();
        this.key2fuzzyMatch.clear();
        this.key2similar.clear(); 
    }

    public HashMap<String, Set<String>> getDirectEvidence(){
        return this.key2directMatch; 
    }
    public HashMap<String, Set<String>> getFuzzyEvidence(){
        return this.key2fuzzyMatch; 
    }
    public HashMap<String, Set<String>> getSimilarEvidence(){
        return this.key2similar; 
    }

    public void run(){
        this.checkSignature();
    }

    public void checkSignature(){
        Set<String> signature = new HashSet<>();
        for(OWLEntity e : this.ontology.getSignature(Imports.INCLUDED)){
            signature.add(e.toStringID()); 
        } 
        computeEvidence(signature); 
    }

    public void checkAnnotation(){
        Set<String> annotationValues = new HashSet<>();
        for(OWLEntity e : this.ontology.getSignature(Imports.INCLUDED)){
            //Stream<OWLAnnotation> annotationStream = EntitySearcher.getAnnotationObjects(e, this.ontology);
            Stream<OWLAnnotationAssertionAxiom> annotationStream = EntitySearcher.getAnnotationAssertionAxioms(e, this.ontology);
            Set<OWLAnnotationAssertionAxiom> annotations = annotationStream.collect(Collectors.toSet());

            for(OWLAnnotationAssertionAxiom axiom : annotations){
                if(axiom.getValue().isLiteral() && axiom.getValue().asLiteral().isPresent()){
                    String value = axiom.getValue().asLiteral().get().getLiteral();
                    annotationValues.add(value);
                } 
                if(axiom.getValue().isIRI() && axiom.getValue().asIRI().isPresent()){
                    String value = axiom.getValue().asIRI().get().toString();
                    annotationValues.add(value);
                }
            }

            //for(OWLAnnotation ann : annotations) { 
            //    if(ann.getValue().isLiteral() && ann.getValue().asLiteral().isPresent()){
            //        String value = ann.getValue().asLiteral().get().getLiteral();
            //        annotationValues.add(value);
            //    }
            //}
        }
        computeEvidence(annotationValues);
    }

    public void checkLogicalAxioms(){
        Set<String> logicalSignature = new HashSet<>();
        Set<OWLLogicalAxiom> axioms = this.ontology.getLogicalAxioms(true);
        for(OWLLogicalAxiom axiom : axioms){
            for(OWLEntity entity : axiom.getSignature()){
                logicalSignature.add(entity.toStringID()); 
            } 
        }
        computeEvidence(logicalSignature); 
    }

    public void checkDeclarationAxioms(){
        Set<String> declarationSignature = new HashSet<>();
        Set<OWLDeclarationAxiom> axioms = this.ontology.getAxioms(AxiomType.DECLARATION);
        for(OWLDeclarationAxiom axiom : axioms){
            for(OWLEntity entity : axiom.getSignature()){
                declarationSignature.add(entity.toStringID()); 
            } 
        } 
        computeEvidence(declarationSignature); 
    }

    public void setSimilarityThreshold(double t){
        this.THRESHOLD = t; 
    }

    public void computeEvidence(Set<String> corpus){
        for(String k : this.keys){

            StringMatcher keyMatcher = new StringMatcher(k);

            Set<String> direct = new HashSet<>(); 
            Set<String> fuzzy = new HashSet<>(); 
            Set<String> similar = new HashSet<>(); 

            for(String i : corpus){
                if(keyMatcher.occursIn(i))
                    direct.add(i);
                if(keyMatcher.matchIn(i))
                    fuzzy.add(i);
                if(StringMatcher.similarity(k, i) > THRESHOLD)
                    similar.add(i);
            } 
            if(!direct.isEmpty()){
                this.key2directMatch.put(k,direct); 
            }
            if(!fuzzy.isEmpty()){
                this.key2fuzzyMatch.put(k,fuzzy); 
            }
            if(!similar.isEmpty()){
                this.key2similar.put(k,similar); 
            }
        }
    }

    private boolean stringCompare(String a, String b){
        return a.toLowerCase().contains(b.toLowerCase()); 
    } 

    public void write(String ontologyName, String path, String select){
        writeMap(this.key2directMatch, ontologyName, path, select, "/direct");
        writeMap(this.key2fuzzyMatch, ontologyName, path, select, "/fuzzy");
        writeMap(this.key2similar, ontologyName, path, select, "/similar"); 
    }

    public void writeStatistics(String ontologyName, String path, String about){
            String format = about + ": Direct " + this.key2directMatch.size()
                + " Fuzzy " + this.key2fuzzyMatch.size()
                + " Similar " + this.key2similar.size();

            IOHelper.writeAppend(format, path + "/" + ontologyName); 
    }

    private void writeMap(HashMap<String,Set<String>> mp, String ontologyName, String path, String select, String type) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String s = (String) pair.getKey();
            Set<String> matches = mp.get(s); 

            IOHelper.writeAppend(ontologyName + " " + matches, path + "/" + s + select + "/" + type);
            it.remove(); // avoids a ConcurrentModificationException
        }
    } 

}
