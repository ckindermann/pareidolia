package uk.ac.man.cs.analysers;

import uk.ac.man.cs.util.Pair;

import org.semanticweb.owlapi.model.*;
import java.io.*;
import java.util.*;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Created by chris 21/11/18
 */
public class ClassAxiomCluster {

    private Set<OWLAxiom> axioms;
    private Set<OWLEntity> signature;
    private Set<OWLEntity> expansionPoints;
    private Set<OWLEntity> expandedPoints;


    //public ClassAxiomCluster(Set<OWLAxiom> as){
    //    this.signature = new HashSet<>();
    //    this.axioms = new HashSet<>();
    //    this.expansionPoints = new HashSet<>();
    //    this.add(as); 
    //}

    public ClassAxiomCluster(OWLAxiom a){ 
        this.signature = new HashSet<>();
        this.axioms = new HashSet<>();
        this.expandedPoints = new HashSet<>();
        this.expansionPoints = new HashSet<>();
        this.addAxiom(a);
        this.expansionPoints.addAll(a.getSignature());
    }

    //public boolean isFixpoint(){
    //    for(OWLAxiom a : axioms){
    //        Set<OWLEntity> aSignature = a.getSignature();
    //        if(!this.signature.containsAll(aSignature))
    //            return false;
    //    } 
    //    return true;
    //}
    //
    public boolean isFixpoint(){
        return this.expansionPoints.isEmpty();
        //return this.expansionPoints.size() == this.signature.size();
    }

    public Set<OWLEntity> getAllExpansionPoints(){
        return this.expansionPoints; 
    }

    //public Set<OWLAxiom> getAllExpansionPoints(){
    //    Set<OWLAxiom> expansionPoints = new HashSet<>();
    //    for(OWLAxiom a : this.axioms){
    //        Set<OWLEntity> aSignature = a.getSignature();
    //        if(!this.signature.containsAll(aSignature))
    //            expansionPoints.add(a);
    //    } 
    //    return expansionPoints; 
    //}

    public OWLAxiom getExpansionPoint(){
        OWLAxiom mockAxiom = null;
        for(OWLAxiom a : this.axioms){
            Set<OWLEntity> aSignature = a.getSignature();
            if(!this.signature.containsAll(aSignature))
                return a; 
        } 
        return mockAxiom;
    }

    public Set<OWLAxiom> getAxioms(){
        return this.axioms; 
    }

    public Set<OWLEntity> getSignature(){
        return this.signature; 
    }

    public int getClusterSize(){
        return this.axioms.size(); 
    }

    public void addExpansionPoint(OWLEntity e){
        this.expansionPoints.remove(e);
        this.expandedPoints.add(e); 
    }

    //public void checkAxiom(OWLAxiom a){
    //    if(this.hasSomeTermsOf(a))
    //        this.add(a); 
    //}
    //

    public boolean contains(OWLAxiom a){
        if(this.axioms.contains(a))
            return true; 
        return false;
    }

    public void add(Set<OWLAxiom> axioms){
        for(OWLAxiom a : axioms){
            this.addAxiom(a); 
        } 
    }

    private void addAxiom(OWLAxiom a){
        this.axioms.add(a);
        this.signature.addAll(a.getSignature()); 
        for(OWLEntity e : a.getSignature()){
            if(!expandedPoints.contains(e))
                expansionPoints.add(e); 
        }
    }

    private boolean hasSomeTermsOf(OWLAxiom a){
        Set<OWLEntity> aSig = a.getSignature();
        for(OWLEntity e : aSig){
            if(this.signature.contains(e))
                return true; 
        } 
        return false;
    }

    public int getSize(){
        return this.axioms.size(); 
    }

    public void print(){
        System.out.println(this.axioms.toString());
    }
}
