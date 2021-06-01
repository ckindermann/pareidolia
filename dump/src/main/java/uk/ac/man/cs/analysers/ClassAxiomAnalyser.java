package uk.ac.man.cs.analysers;

import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.lodp.*;
import uk.ac.man.cs.util.*;

import java.io.*;
import java.util.*;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.apibinding.OWLManager;
import java.util.Iterator;

import org.semanticweb.owlapi.model.*;

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
import java.util.regex.Pattern;

import java.io.File;

/**
 * The Dector defines the basic operations of a
 * pattern detctor.
 *
 * @author Christian Kindermann
 * @version 1.0
 * @since 2018-10-08
 *
 */
public class ClassAxiomAnalyser {

    //protected OWLOntology ontology;
    protected Set<OWLAxiom> seedAxioms;

    private int[] fingerprint;
    //first tier
    private Set<OWLAxiom> classAxioms; 
    //second tie
    private Set<OWLAxiom> disjointUnionAxioms;
    private Set<OWLAxiom> subClassOfAxioms;
    private Set<OWLAxiom> naryClassAxioms;
    //third tier
    private Set<OWLAxiom> equivalentClassesAxioms;
    private Set<OWLAxiom> disjointClassesAxioms;

    public ClassAxiomAnalyser(Set<OWLAxiom> s){
        //this.ontology = o;
        this.seedAxioms = s;

        classAxioms = new HashSet<>();
        disjointUnionAxioms = new HashSet<>();
        subClassOfAxioms = new HashSet<>();
        naryClassAxioms = new HashSet<>();
        equivalentClassesAxioms = new HashSet<>();
        disjointClassesAxioms = new HashSet<>(); 

        this.fingerprint = new int[6];
        this.initialise();
        this.setFingerprint();
    }

    public void setSeed(Set<OWLAxiom> seed){
        //this.seedAxioms.clear();
        this.reset();
        this.seedAxioms = seed;
        this.initialise();
        this.setFingerprint(); 
    }

    private void reset(){
        this.classAxioms.clear();
        this.disjointUnionAxioms.clear();
        this.subClassOfAxioms.clear();
        this.naryClassAxioms.clear();
        this.equivalentClassesAxioms.clear();
        this.disjointClassesAxioms.clear(); 
    }

    public int[] getFingerprint(){
        return this.fingerprint;
    }

    public Set<OWLAxiom> getClassAxioms(){
        return this.classAxioms; 
    } 
    public Set<OWLAxiom> getDisjointUnionAxioms(){
        return this.disjointUnionAxioms;
    }
    public Set<OWLAxiom> getSubClassOfAxioms(){
        return this.subClassOfAxioms;
    }
    public Set<OWLAxiom> getNaryClassAxioms(){
        return this.naryClassAxioms;
    }
    public Set<OWLAxiom> getEquivalentClassesAxioms(){
        return this.equivalentClassesAxioms;
    }
    public Set<OWLAxiom> getDisjointClassesAxioms(){
        return this.disjointClassesAxioms;
    }

    public boolean coversFingerprintOf(ClassAxiomAnalyser caa){
        int[] otherFingerprint = caa.getFingerprint();
        for(int i=0; i<6; i++){
            if(this.fingerprint[i] < otherFingerprint[i])
                return false;
        }

        return true; 
    }

    public void setFingerprint() {
        this.fingerprint[0] = classAxioms.size();
        this.fingerprint[1] = disjointUnionAxioms.size();
        this.fingerprint[2] = subClassOfAxioms.size();
        this.fingerprint[3] = naryClassAxioms.size();
        this.fingerprint[4] = equivalentClassesAxioms.size();
        this.fingerprint[5] = disjointClassesAxioms.size(); 
    }

    private void initialise(){
        //Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED); 
        for(OWLAxiom axiom : this.seedAxioms){ 
            if(axiom instanceof OWLClassAxiom)
                classAxioms.add(axiom); 
            if(axiom instanceof OWLDisjointUnionAxiom)
                disjointUnionAxioms.add(axiom);
            if(axiom instanceof OWLSubClassOfAxiom)
                subClassOfAxioms.add(axiom);
            if(axiom instanceof OWLNaryClassAxiom)
                naryClassAxioms.add(axiom);
            if(axiom instanceof OWLEquivalentClassesAxiom)
                equivalentClassesAxioms.add(axiom);
            if(axiom instanceof OWLDisjointClassesAxiom)
                disjointClassesAxioms.add(axiom); 
        } 
    }


    protected Set<OWLAxiom> axiomUsage(OWLAxiom a){
        Set<OWLAxiom> axiomsWithSharedSignature = new HashSet<>();
        Set<OWLEntity> aSig = a.getSignature();
        for(OWLEntity e : aSig){
            Set<OWLAxiom> usage = entityUsage(e);
            axiomsWithSharedSignature.addAll(usage); 
        } 
        return axiomsWithSharedSignature;
    }

    protected Set<OWLAxiom> entityUsage(OWLEntity entity){
        Set<OWLAxiom> res = new HashSet<>(); 
        //for(OWLAxiom axiom : ontology.getAxioms(Imports.EXCLUDED)){
        for(OWLAxiom axiom : this.classAxioms){
            if((axiom.getSignature()).contains(entity)){
                    res.add(axiom);
            }
        }
        return res; 
    }

    public String fingerprint(){
        return(classAxioms.size() + ","
                + disjointUnionAxioms.size() + ","
                + subClassOfAxioms.size() + ","
                + naryClassAxioms.size() + ","
                + equivalentClassesAxioms.size() + ","
                + disjointClassesAxioms.size()); 
    }

    public void print(){
        System.out.println("===Class Axiom Summary===");
        System.out.println("Class axioms " + classAxioms.size());
        System.out.println("Disjount Union " + disjointUnionAxioms.size());
        System.out.println("Subclass  " + subClassOfAxioms.size());
        System.out.println("Nary Class " + naryClassAxioms.size());
        System.out.println("Equivalent Class " + equivalentClassesAxioms.size());
        System.out.println("Disjoint Classs " + disjointClassesAxioms.size()); 
    }

    //protected Set<OWLAxiom> getSubsumptionAxioms(){ 
    //    Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);

    //    Set<OWLAxiom> subClassAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.SUBCLASS_OF); 
    //    Set<OWLAxiom> subsumptionAxioms = new HashSet<>(); 
    //    subsumptionAxioms.addAll(subClassAxioms);

    //    Set<OWLAxiom> equivAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.EQUIVALENT_CLASSES);
    //    for(OWLAxiom a : equivAxioms){
    //        subsumptionAxioms.addAll(((OWLEquivalentClassesAxiom) a).asOWLSubClassOfAxioms()); 
    //    } 
    //    return subsumptionAxioms;
    //} 
}

