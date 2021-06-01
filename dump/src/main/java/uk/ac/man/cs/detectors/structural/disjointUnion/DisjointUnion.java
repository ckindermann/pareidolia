package uk.ac.man.cs.detectors.structural.disjointUnion;

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
public class DisjointUnion {

    private OWLClass namedClass;
    private OWLAxiom unionAxiom;
    private OWLAxiom disjointnessConstraint; 
    private HashMap<OWLEntity,Set<OWLAxiom>> usage; 

    private Set<OWLClass> equivalentClasses; 
    private Set<OWLAxiom> justification; //can't do this atm

    boolean stated;

    public DisjointUnion(OWLAxiom union, OWLAxiom disjoint, OWLClass name, boolean e, HashMap<OWLEntity, Set<OWLAxiom>> u ){
        this.unionAxiom = union; 
        this.disjointnessConstraint = disjoint; 
        this.namedClass = name;
        this.stated = e;

        this.equivalentClasses = new HashSet<OWLClass>();
        this.usage = new HashMap<OWLEntity, Set<OWLAxiom>>();
        this.usage.putAll(u);
        this.justification = new HashSet<OWLAxiom>();
    }

    public DisjointUnion(OWLAxiom union, OWLAxiom disjoint){

        this.unionAxiom = union; 
        this.disjointnessConstraint = disjoint; 
        this.namedClass = retrieveNamedClass(); 


        this.stated = true;
        this.equivalentClasses = new HashSet<OWLClass>();
        this.justification = new HashSet<OWLAxiom>();
    }

    public OWLClass getUnionName(){
        return this.namedClass; 
    }

    public OWLAxiom getUnionAxiom(){ 
        return this.unionAxiom;
    }

    public void setJustification(boolean j){
        stated = j; 
    }

    public void addEquivalentClasses(Set<OWLClass> equiv){
        this.equivalentClasses.addAll(equiv); 
    }

    private OWLClass retrieveNamedClass(){ 
        for(OWLEntity e : this.unionAxiom.getSignature()){
            if(!disjointnessConstraint.getSignature().contains(e) && e.isOWLClass())
                 return(e.asOWLClass());
        }
        return null;
    }

    public void print(){
        System.out.println("Named Class: " + this.namedClass.toString());
        System.out.println("Union Axiom " + this.unionAxiom.toString());
        System.out.println("Disjointness Axiom(s): " + this.disjointnessConstraint); 
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
