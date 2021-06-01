package uk.ac.man.cs.lodp;

import uk.ac.man.cs.util.Pair;

import org.semanticweb.owlapi.model.*;
import java.io.*;
import java.util.Set;
import java.util.HashSet;

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
    private Set<OWLAxiom> disjointnessConstraints; 

    private Set<OWLClass> equivalentClasses; 
    boolean stated;

    public DisjointUnion(OWLClass cl, OWLAxiom union, Set<OWLAxiom> disjoint){
        this.namedClass = cl;
        this.unionAxiom = union; 
        this.disjointnessConstraints = disjoint; 
        this.stated = false;
        this.equivalentClasses = new HashSet<OWLClass>();
    }

    public void setJustification(boolean j){
        stated = j; 
    }

    public void addEquivalentClasses(Set<OWLClass> equiv){
        this.equivalentClasses.addAll(equiv); 
    }
}
