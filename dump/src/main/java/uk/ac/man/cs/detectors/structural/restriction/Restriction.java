package uk.ac.man.cs.detectors.structural.restriction;

import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.detectors.structural.intersection.*;

import org.semanticweb.owlapi.model.*;
import java.io.*;
import java.util.*;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Created by chris 21/11/18
 */
public class Restriction {

    private OWLClass namedClass;
    //directly asserted restrictions
    private Set<OWLClassExpression> viaSubsumption;
    //intersection vith restrictions
    private Set<Intersection> intersections; 
    //restrictions due to intersections
    private Set<OWLClassExpression> viaIntersection;
    //entailed restrictions
    private Set<OWLClassExpression> viaEntailment;

    public Restriction(OWLClass name){
        this.namedClass = name;
        this.viaSubsumption = new HashSet<>();
        this.viaIntersection = new HashSet<>();
        this.viaEntailment = new HashSet<>(); 
        this.intersections = new HashSet<>(); 

    }

    public Set<OWLClassExpression> getAsserted(){
        return this.viaSubsumption; 
    }
    public Set<Intersection> getIntersections(){
        return this.intersections; 
    }
    public Set<OWLClassExpression> getViaIntersections(){
        return this.viaIntersection; 
    }
    public Set<OWLClassExpression> getEntailed(){
        return this.viaEntailment; 
    }

    public OWLClass getNamedClass(){
        return this.namedClass;
    }

    public void add2subsumption(OWLClassExpression e){
        this.viaSubsumption.add(e); 
    }
    public void add2intersection(Intersection i){
        this.intersections.add(i); 
    }
    public void add2entailed(OWLClassExpression e){
        this.viaEntailment.add(e); 
    }

    public void add2asserted(Set<OWLClassExpression> e){
        this.viaSubsumption.addAll(e); 
    }
    public void add2Intersection(Set<Intersection> e){
        this.intersections.addAll(e); 
    }
    public void add2entailed(Set<OWLClassExpression> e){
        this.viaEntailment.addAll(e); 
    } 

    public Set<OWLClassExpression> getAll(){
        Set<OWLClassExpression> all = new HashSet<>();
        all.addAll(this.viaSubsumption);
        all.addAll(this.viaIntersection);
        return all; 
    }
    
        

    //public void print(){
    //    System.out.println("Named Class: " + this.namedClass.toString());
    //    System.out.println("Direct existentials :");
    //    for(OWLClassExpression e : this.direct){
    //        System.out.println(e.asOWLClass().toString());
    //    }
    //    System.out.println("========================================");
    //}

    //public static void printMap(HashMap<OWLEntity,Set<OWLAxiom>> mp) {
    //    Iterator it = mp.entrySet().iterator();
    //    while (it.hasNext()) {
    //        Map.Entry pair = (Map.Entry)it.next();
    //        OWLEntity e = (OWLEntity) pair.getKey();
    //        Set<OWLAxiom> as = mp.get(e); 

    //        System.out.println(e.toString() + " = " + as.toString());
    //        it.remove(); // avoids a ConcurrentModificationException
    //    }
    //}


}
