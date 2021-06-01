package uk.ac.man.cs.metrics;

import uk.ac.man.cs.codp.CODP;
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.util.StringMangler;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import java.util.HashSet;
import java.util.HashMap;
import org.semanticweb.owlapi.model.parameters.Imports;
import java.util.Set;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;

import java.io.*;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by chris on 03/08/18.
 */
public class SignatureMetric extends Metric {

    private OWLOntology ontology;
    private CODP ontologyPattern;
    private HashMap<String, Set<String>> evidence;
    //maps a key (from an ODP) to entities (that resulted in a match in an
    //ontology)
    //-> key: elements in the signature of an ODP 
    //-> match: keys are compared via string matching with entities in ontologies
    private HashMap<String, Set<OWLEntity>> key2entities;
    //collect the coOccurrence of keys
    //-> coOccurrence:
    //--keys are mapped to entities in an ontology
    //--entities are used in axioms
    //--the usage of an entity is the set of all axioms in which the entity occurrs
    //--if an entity A occurrs in the usage of entity B, then A and B cooccur
    private HashMap<Pair<String,OWLEntity>, Set<Pair<String,OWLEntity>>> coOccurrence;
    private int coOccurrenceLevel;

    public SignatureMetric(OWLOntology o, CODP p){
        this.ontology = o;
        this.ontologyPattern = p;
        this.evidence = new HashMap<>(); 
        this.key2entities = new HashMap<>();
        this.coOccurrence = new HashMap<>();
        this.coOccurrenceLevel = 2;
    }

    public HashMap<String, Set<String>> getEvidence(){
        return this.evidence; 
    }

    public void compute(){
        this.computeKeyOccurrence();
        this.computeCoOccurrence();

    }

    public HashMap<Pair<String,OWLEntity>, Set<Pair<String,OWLEntity>>> getCoOccurrence(){
        return this.coOccurrence; 
    }

    public void setCoOccurrenceLevel(int l){
        this.coOccurrenceLevel = l; 
    }

    /**
     * Compute the evidence to be found in the IRI of entities
     */
    public void computeKeyOccurrence(){
        this.evidence.clear();

        Set<OWLEntity> signature = this.ontology.getSignature();
        StringMangler stringMangler = new StringMangler();

        for(String k : this.ontologyPattern.getKeys()){

            StringPattern pattern = new StringPattern(k);

            Set<String> namesWithKey = new HashSet<String>();
            Set<OWLEntity> hits = new HashSet<>();

            for(OWLEntity e : signature){ 


                String entityName = stringMangler.flattenLineBreaks(e.getIRI().toString());
                if(pattern.findIn(entityName)){
                    namesWithKey.add(entityName); 
                    hits.add(e);
                } 
            } 
            if(!namesWithKey.isEmpty()){
                this.evidence.put(k, namesWithKey); 
                this.key2entities.put(k, hits); 
            }
        }
    }

    private void computeCoOccurrence(){
        this.coOccurrence.clear();

        //if(this.key2entities.isEmpty()){
        //    this.computeKeyOccurrence();
        //}

        if(this.key2entities.isEmpty()){
            return; 
        }

        Iterator it = this.key2entities.entrySet().iterator();
        String key;
        boolean include = true;
        Set<OWLEntity> hits = new HashSet<>(); 

        //loop over all found keys
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            //key
            key = (String) pair.getKey();
            //associated entity in ontology
            hits = (Set<OWLEntity>) pair.getValue();

            //loop over all associated entities
            for(OWLEntity e : hits){

                //create result entry
                Pair<String,OWLEntity> source = new Pair(key, e);
                Set<Pair<String,OWLEntity>> cooc = new HashSet();

                //loop over usage of associated entity
                for(OWLAxiom a : this.usage(e)){
                    //loop over entities in axioms of the usage
                    for(OWLEntity i : a.getSignature()){
                        //check whether it matches another key from the pattern
                        for(String k : this.ontologyPattern.getKeys()){
                            if(i.getIRI().toString().toLowerCase().contains(k) && !(k.equals(key))){
                                //check new cooccurrence
                                include = true;
                                for(Pair<String,OWLEntity> p : cooc){
                                    if(((String)p.getFirst()).equals(k) && ((OWLEntity)p.getSecond()).getIRI().toString().equals(i.getIRI().toString()))
                                        include = false; 
                                }

                                //add it to set of cooccrrences 
                                if(include){
                                    Pair<String,OWLEntity> hit = new Pair(k, i);
                                    cooc.add(hit); 
                                }
                            }
                        } 
                    } 
                }
                //loop done for an entity
                //construct entry in cooccurrence table
                if(!cooc.isEmpty()){
                    this.coOccurrence.put(source, cooc); 
                } 
            }
        } 
    }

    /**
     * Usage(e): set of axioms an entity e occurrs in
     */
    private Set<OWLAxiom> usage(OWLEntity entity){

        //set of axioms containing the pattern
        Set<OWLAxiom> entityUsage = new HashSet<>();

        for(OWLAxiom axiom : this.ontology.getAxioms(Imports.INCLUDED)){
            if((axiom.getSignature()).contains(entity)){
                    entityUsage.add(axiom);
            }
        }
        return entityUsage;
    } 

    public void write(String destFile){
        this.writeKeyOccurrence(destFile + "/signature");
        this.writeCoOccurrence(destFile + "/coOccurrence"); 
    }

    private boolean writeKeyOccurrence(String destFile){
        boolean success = false;
        if(!this.evidence.isEmpty()){
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(destFile));
                Iterator it = this.evidence.entrySet().iterator();

                //output format
                String output = "patternKey,[Set of EntityIRI]";
                bw.write(output);
                bw.newLine();

                while(it.hasNext()){
                    Map.Entry pair = (Map.Entry)it.next();

                    output = ((String)pair.getKey()) + ","
                        + ((Set<String>) pair.getValue());

                    bw.write(output);
                    bw.newLine();
                }
                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } 
            success = true;
        } 
        return success;
    }

    private boolean writeCoOccurrence(String destFile, int level){
        boolean success = false;
        if(!this.coOccurrence.isEmpty()){
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(destFile));
                Iterator it = this.coOccurrence.entrySet().iterator();

                //output format
                String output = "(key,entity),[Set of (key,entity)]";
                bw.write(output);
                bw.newLine();

                while(it.hasNext()){
                    Map.Entry pair = (Map.Entry)it.next();
                    if(((Set<String>) pair.getValue()).size() > level){
                        //output = ((OWLEntity)pair.getKey()).getIRI().getShortForm() + ","
                            //+ ((Set<String>) pair.getValue());
                        output = "(" + ((Pair<String,OWLEntity>)pair.getKey()).getFirst() + "," +
                                ((Pair<String,OWLEntity>)pair.getKey()).getSecond().getIRI().getShortForm() + "),";
                                //((Pair<String,OWLEntity>)pair.getKey()).getSecond().getIRI().toString() + "),";

                        output = output + "[ ";
                        for(Pair<String,OWLEntity> p : ((Set<Pair<String,OWLEntity>>)pair.getValue())){
                            output = output + "(" + p.getFirst() + "," + p.getSecond().getIRI().getShortForm() + "),"; 
                            //output = output + "(" + p.getFirst() + "," + p.getSecond().getIRI().toString() + "),"; 
                        }
                        output = output + "]";

                        bw.write(output);
                        bw.newLine();
                    }
                }
                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } 
            success = true;
        }
        return success; 
    }

    public boolean writeCoOccurrence(String destFile){
        return writeCoOccurrence(destFile, this.coOccurrenceLevel);
    }

    public boolean strongEvidence(int level){
        if(!this.coOccurrence.isEmpty()){
                Iterator it = coOccurrence.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry pair = (Map.Entry)it.next();
                    if(((Set<Pair<String,OWLEntity>>)(pair.getValue())).size() > level){
                        return true; 
                    }
                }
        }
        return false;
    }

    public boolean strongEvidence(){
        return strongEvidence(this.coOccurrenceLevel); 
    }

    public void reset(){
        this.evidence.clear();
        this.key2entities.clear();
        this.coOccurrence.clear(); 
    }

    public void setOntology(OWLOntology o){
        this.reset();
        this.ontology = o; 
    }
}
