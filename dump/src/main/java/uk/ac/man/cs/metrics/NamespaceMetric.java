package uk.ac.man.cs.metrics;


import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Set;
import java.util.*;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Created by chris on 02/08/18.
 */
public class NamespaceMetric extends Metric {

    private OWLOntology ontology;
    private String namespace;
    private Set<OWLEntity> evidence;

    public NamespaceMetric(OWLOntology o){
        this.ontology = o;
        this.evidence = new HashSet<>();
        this.namespace = "";
    }

    public NamespaceMetric(OWLOntology o, String n){
        this.ontology = o;
        this.namespace = n; 
        this.evidence = new HashSet<>();
    }

    /**
     * Compute the evidence to be found in the namespace of an ontology
     * Example: check for "ontologydesignpattern"
     */
    public void compute(){

        this.reset();

        //check namespace of all entities
        Set<OWLEntity> entities = this.ontology.getSignature();
        String iri;

        for(OWLEntity e : entities){ 
            if (e.getIRI().toString().toLowerCase().contains(this.namespace.toLowerCase())){
                this.evidence.add(e);
            }
        } 
    }

    public void compute(String n){
        this.reset();
        this.namespace = n;
        this.compute(); 
    }

    public void write(String destFile){
        if(!this.evidence.isEmpty()){
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(destFile));
                Iterator it = this.evidence.iterator();

                while(it.hasNext()){
                    OWLEntity e = (OWLEntity) it.next();
                    bw.write((String) e.toString());
                    bw.newLine();
                }
                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } 
    }

    public void reset(){
        this.evidence.clear();
    }

    public Set<OWLEntity> getEvidence(){
        return this.evidence;
    }

    public void setNamespace(String n){
        this.namespace = n; 
    }

    public void setOntology(OWLOntology o){
        this.reset();
        this.ontology = o; 
    }

}
