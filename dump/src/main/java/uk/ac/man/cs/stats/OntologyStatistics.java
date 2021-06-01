package uk.ac.man.cs.stats;

import uk.ac.man.cs.codp.CODP;
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.util.StringMangler;
import uk.ac.man.cs.data.*;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.ont.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.concurrent.*;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import java.util.HashSet;
import java.util.HashMap;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.*;
import java.util.Set;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.io.File;
import java.io.FileWriter;
import org.semanticweb.owlapi.search.EntitySearcher;
import java.util.stream.*;
import java.io.FileReader;

import java.io.*;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by chris on 24/10/18.
 */

public class OntologyStatistics {

    private OWLOntology ontology; 
    private OWLOntology instance;

    private HashMap<OWLEntity,OWLEntity> substitutionMapping; 

    private Set<OWLAxiom> declarationAxioms;

    private Set<OWLAxiom> instantiatedAxioms;              //reuse def 2
    private Set<OWLAxiom> instantiatedEquivalentAxioms;    //reuse def 3
    private Set<OWLAxiom> instantiatedEntailedAxiomsWeak;  //reuse def 4
    private Set<OWLAxiom> instantiatedEntailedAxiomsStrong;//reuse def 5
    private Set<OWLAxiom> entailedAxioms;                  //reuse def 6

    public int abs_entailedAxioms;
    public int abs_declarationAxioms; 
    public int abs_instantiatedAxioms;
    public int abs_instantiatedEquivalentAxioms;
    public int abs_instantiatedEntailedAxiomsWeak;
    public int abs_instantiatedEntailedStrongAxioms;

    public double rel_declarationAxioms;
    public double rel_instantiatedAxioms;
    public double rel_instantiatedEquivalentAxioms;
    public double rel_instantiatedEntailedAxiomsWeak;
    public double rel_instantiatedEntailedStrongAxioms;

    public OntologyStatistics(OWLOntology o, OWLOntology inst, HashMap<OWLEntity,OWLEntity> mapping){
        this.ontology = o;
        this.instance = inst;
        this.substitutionMapping = mapping;
        //this.substitutionMapping = new HashMap<>(); 
        //this.substitutionMapping.putAll(mapping);

        this.declarationAxioms = new HashSet<>();

        this.instantiatedAxioms = new HashSet<>();
        this.entailedAxioms = new HashSet<>();
        this.instantiatedEquivalentAxioms = new HashSet<>();
        this.instantiatedEntailedAxiomsWeak = new HashSet<>();
        this.instantiatedEntailedAxiomsStrong = new HashSet<>();

        abs_entailedAxioms = 0;
        abs_instantiatedAxioms = 0;
        abs_instantiatedEquivalentAxioms = 0;
        abs_instantiatedEntailedAxiomsWeak = 0;
        abs_instantiatedEntailedStrongAxioms = 0;

        rel_instantiatedAxioms = 0;
        rel_instantiatedEquivalentAxioms = 0;
        rel_instantiatedEntailedAxiomsWeak = 0;
        rel_instantiatedEntailedStrongAxioms = 0;
    }

    public void computeReuse() throws Exception {

        OWLReasoner reasoner = ReasonerLoader.initReasoner(this.ontology);
        //reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        for(OWLAxiom a : this.instance.getAxioms()){ 

            if(a.getAxiomType() == AxiomType.DECLARATION){
                if(this.ontology.getSignature().containsAll(a.getSignature())){ 
                    //reuse by instantiation (def 2)
                    if(this.ontology.containsAxiom(a))
                        this.declarationAxioms.add(a); 
                }
            }

            if(AxiomType.LOGICAL_AXIOM_TYPES.contains(a.getAxiomType())){

                //only test for reuse if the whole signature of an axiom
                //is present in the ontology
                if(this.ontology.getSignature().containsAll(a.getSignature())){ 
                    //reuse by instantiation (def 2)
                    if(this.ontology.containsAxiom(a))
                        this.instantiatedAxioms.add(a);
                    //reuse by equivalent instantiation
                    for(OWLAxiom i : this.ontology.getAxioms()){
                        if(i.getSignature().containsAll(a.getSignature())){

                            //def(3)
                            if(!this.instantiatedEquivalentAxioms.contains(a) && this.isEquivalent(a,i))
                                this.instantiatedEquivalentAxioms.add(a); 

                            //def(4)
                            if(!this.instantiatedEntailedAxiomsWeak.contains(a) && this.entails(a,i))
                                this.instantiatedEntailedAxiomsWeak.add(a); 

                            //def(5)
                            if(!this.instantiatedEntailedAxiomsStrong.contains(a) && this.entails(i,a))
                                this.instantiatedEntailedAxiomsStrong.add(a); 
                        }
                    }
                    //reuse by "entailment" (def 6)
                    if(reasoner.isEntailed(a))
                        this.entailedAxioms.add(a); 
                }
            }
        } 
        this.calculateStatistics(); 
    }

    private void calculateStatistics(){

        abs_entailedAxioms = this.entailedAxioms.size();
        abs_instantiatedAxioms = this.instantiatedAxioms.size();
        abs_instantiatedEquivalentAxioms = this.instantiatedEquivalentAxioms.size();
        abs_instantiatedEntailedAxiomsWeak = this.instantiatedEntailedAxiomsWeak.size();
        abs_instantiatedEntailedStrongAxioms = this.instantiatedEntailedAxiomsStrong.size(); 

        HashSet<OWLAxiom> axioms = new HashSet<>();

        for(AxiomType<?> t : AxiomType.LOGICAL_AXIOM_TYPES){
            axioms.addAll(this.instance.getAxioms(t)); 
        } 

        abs_declarationAxioms = this.declarationAxioms.size();
        rel_declarationAxioms = (double) this.abs_declarationAxioms / this.instance.getAxioms(AxiomType.DECLARATION).size();

        int totalNumberOfAxioms = axioms.size();

        rel_instantiatedAxioms = (double) abs_instantiatedAxioms / totalNumberOfAxioms;
        rel_instantiatedEquivalentAxioms = (double) abs_instantiatedEquivalentAxioms / totalNumberOfAxioms;
        rel_instantiatedEntailedAxiomsWeak = (double) abs_instantiatedEntailedAxiomsWeak / totalNumberOfAxioms ;
        rel_instantiatedEntailedStrongAxioms = abs_instantiatedEntailedStrongAxioms / totalNumberOfAxioms ;
    }

    private boolean isEquivalent(OWLAxiom a1, OWLAxiom a2) throws Exception{
        Set<OWLAxiom> A1 = new HashSet<>();
        Set<OWLAxiom> A2 = new HashSet<>();

        A1.add(a1);
        A2.add(a2);

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ont1 = manager.createOntology(A1);
        OWLOntology ont2 = manager.createOntology(A2);


        boolean res = false;
        OWLReasoner reasoner1 = ReasonerLoader.initReasoner(ont1);
        OWLReasoner reasoner2 = ReasonerLoader.initReasoner(ont2);
        res = reasoner1.isEntailed(a2) && reasoner2.isEntailed(a1); 

        //return res;
        return res;

    }

    public void getMaxStatistics(OntologyStatistics os){

        if(this.abs_declarationAxioms < os.abs_declarationAxioms)
            this.abs_declarationAxioms = os.abs_declarationAxioms;
        if(this.abs_entailedAxioms < os.abs_entailedAxioms)
            this.abs_entailedAxioms = os.abs_entailedAxioms;
        if(this.abs_instantiatedAxioms < os.abs_instantiatedAxioms)
            this.abs_instantiatedAxioms = os.abs_instantiatedAxioms;

        if(this.abs_instantiatedAxioms < os.abs_instantiatedAxioms)
            this.abs_instantiatedAxioms = os.abs_instantiatedAxioms;
        if(this.abs_instantiatedEquivalentAxioms < os.abs_instantiatedEquivalentAxioms)
            this.abs_instantiatedEquivalentAxioms = os.abs_instantiatedEquivalentAxioms;
        if(this.abs_instantiatedEntailedAxiomsWeak < os.abs_instantiatedEntailedAxiomsWeak)
            this.abs_instantiatedEntailedAxiomsWeak = os.abs_instantiatedEntailedAxiomsWeak;
        if(this.abs_instantiatedEntailedStrongAxioms < os.abs_instantiatedEntailedStrongAxioms)
            this.abs_instantiatedEntailedStrongAxioms = os.abs_instantiatedEntailedStrongAxioms;

        if(this.rel_declarationAxioms < os.rel_declarationAxioms)
            this.rel_declarationAxioms = os.rel_declarationAxioms;
        if(this.rel_instantiatedAxioms < os.rel_instantiatedAxioms)
            this.rel_instantiatedAxioms = os.rel_instantiatedAxioms;
        if(this.rel_instantiatedEquivalentAxioms < os.rel_instantiatedEquivalentAxioms)
            this.rel_instantiatedEquivalentAxioms = os.rel_instantiatedEquivalentAxioms;
        if(this.rel_instantiatedEntailedAxiomsWeak < os.rel_instantiatedEntailedAxiomsWeak)
            this.rel_instantiatedEntailedAxiomsWeak = os.rel_instantiatedEntailedAxiomsWeak;
        if(this.rel_instantiatedEntailedStrongAxioms < os.rel_instantiatedEntailedStrongAxioms)
            this.rel_instantiatedEntailedStrongAxioms = os.rel_instantiatedEntailedStrongAxioms;

    }

    private boolean entails(OWLAxiom a1, OWLAxiom a2) throws Exception{
        Set<OWLAxiom> A1 = new HashSet<>();

        A1.add(a1);

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ont1 = manager.createOntology(A1);

        OWLReasoner reasoner1 = ReasonerLoader.initReasoner(ont1);

        boolean res = false;
        try{
            res = reasoner1.isEntailed(a2); 
        } catch (Exception e) {
            System.out.println("Reasoner EXCEPTION)"); 
        } 
        return res;
    }

    public void print(){
        System.out.println("Declarations count : " + abs_declarationAxioms + " " + rel_declarationAxioms);
        System.out.println("Instantiated count : " + abs_instantiatedAxioms + " " + rel_instantiatedAxioms);
        System.out.println("Entailed count : " + this.entailedAxioms.size());
        System.out.println("Instantiated equivalent : " + this.instantiatedEquivalentAxioms.size());
        System.out.println("INstantiated weak : " + this.instantiatedEntailedAxiomsWeak.size());
        System.out.println("Instantiated strong : " + this.instantiatedEntailedAxiomsStrong.size());
        if(!this.instantiatedAxioms.isEmpty()){
            for(OWLAxiom a : instantiatedAxioms){
               System.out.println("INSTANTIATED AXIOM: " + a.toString()); 
               System.out.println("Signature: " + a.getSignature().toString()); 
            } 
        } 
    } 

    public void dropData(){
        this.ontology = null;
        this.instance = null;
        this.substitutionMapping.clear();
        this.substitutionMapping = null;

        this.declarationAxioms.clear();

        this.instantiatedAxioms.clear();
        this.entailedAxioms.clear();
        this.instantiatedEquivalentAxioms.clear();
        this.instantiatedEntailedAxiomsWeak.clear();
        this.instantiatedEntailedAxiomsStrong.clear(); 
    }
}
