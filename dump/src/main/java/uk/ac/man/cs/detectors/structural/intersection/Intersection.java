package uk.ac.man.cs.detectors.structural.intersection;

import uk.ac.man.cs.util.Pair;

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
public class Intersection {

    private OWLClass namedClass;
    private OWLAxiom intersectionAxiom;
    private HashMap<OWLEntity,Set<OWLAxiom>> usage; 

    private Set<OWLClass> equivalentClasses; 
    private Set<OWLAxiom> justification; //can't do this atm


    public Intersection(OWLAxiom intersection, OWLClass name, HashMap<OWLEntity, Set<OWLAxiom>> u){
        this.intersectionAxiom = intersection; 
        this.namedClass = name;

        this.equivalentClasses = new HashSet<OWLClass>();
        this.usage = new HashMap<OWLEntity, Set<OWLAxiom>>();
        this.usage.putAll(u);
        this.justification = new HashSet<OWLAxiom>();
    }

    public Intersection(OWLAxiom intersection){

        this.intersectionAxiom = intersection; 

        this.equivalentClasses = new HashSet<OWLClass>();
        this.justification = new HashSet<OWLAxiom>();
    }

    public OWLClass getIntersectionName(){
        return this.namedClass; 
    }

    public OWLAxiom getIntersectionAxiom(){ 
        return this.intersectionAxiom;
    } 

    public void addEquivalentClasses(Set<OWLClass> equiv){
        this.equivalentClasses.addAll(equiv); 
    }

    public void print(){
        System.out.println("Named Class: " + this.namedClass.toString());
        System.out.println("Intersection Axiom " + this.intersectionAxiom.toString());
        printMap(this.usage);
        System.out.println("========================================");
    }

    public static void printMap(HashMap<OWLEntity,Set<OWLAxiom>> mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            OWLEntity e = (OWLEntity) pair.getKey();
            Set<OWLAxiom> as = mp.get(e); 

            System.out.println(e.toString() + " = " + as.toString());
            it.remove(); // avoids a ConcurrentModificationException
        }
}


}
