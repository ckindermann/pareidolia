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

public class ImportChecker {

    private OWLOntology ontology;
    private OWLOntologyManager manager;
    private Set<String> imports;
    private Set<String> keys; //check for these keys in the import closure
    private HashMap<String, Set<String>> key2directMatch;
    private HashMap<String, Set<String>> key2fuzzyMatch;
    private HashMap<String, Set<String>> key2similar;
    private double THRESHOLD = 0.8;

    public ImportChecker(OWLOntology o, Set<String> k){
        this.ontology = o;
        this.manager = OWLManager.createOWLOntologyManager();
        this.keys = k;
        this.imports = new HashSet<>();
        this.key2directMatch = new HashMap<>();
        this.key2fuzzyMatch = new HashMap<>();
        this.key2similar = new HashMap<>();
        this.getImportClosure();
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
        this.getImportClosure();
        this.computeEvidence();
    }

    private void getImportClosure(){ 
        Set<OWLOntology> importClosure = this.ontology.getImportsClosure();
        for(OWLOntology o : importClosure){
            if(o.getOntologyID().getOntologyIRI().isPresent()){ 
                this.imports.add(o.getOntologyID().getOntologyIRI().get().toString()); 
            }
        }
    }

    public void setSimilarityThreshold(double t){
        this.THRESHOLD = t; 
    }

    public void computeEvidence(){
        for(String k : this.keys){

            StringMatcher keyMatcher = new StringMatcher(k);

            Set<String> direct = new HashSet<>(); 
            Set<String> fuzzy = new HashSet<>(); 
            Set<String> similar = new HashSet<>(); 

            for(String i : this.imports){
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

    public void write(String ontologyName, String path){
            writeMap(this.key2directMatch, ontologyName, path, "/direct");
            writeMap(this.key2fuzzyMatch, ontologyName, path, "/fuzzy");
            writeMap(this.key2similar, ontologyName, path, "/similar"); 
    }

    public void writeGeneral(String ontologyName, String path){
        if(this.imports.size() > 1){
            writeImportsClosure(ontologyName, path+"/closure");
            writeStats(ontologyName, path+"/statistics");
        } 
    }


    public void writeImportsClosure(String ontologyName, String path){
        for(String s : imports){
            IOHelper.writeAppend(s, path + "/" + ontologyName);
        } 
    }

    public void writeStats(String ontologyName, String path){
            Set<OWLImportsDeclaration> direct = this.ontology.getImportsDeclarations(); 
            String format = "Direct: " + direct.size() + " Closure: " + this.imports.size();
            IOHelper.writeAppend(format, path + "/" + ontologyName); 
    }

    private void writeMap(HashMap<String,Set<String>> mp, String ontologyName, String path, String type) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String s = (String) pair.getKey();
            Set<String> matches = mp.get(s); 

            IOHelper.writeAppend(ontologyName + " " + matches, path + "/" + s + "/" + type);
            it.remove(); // avoids a ConcurrentModificationException
        }
    } 
}
