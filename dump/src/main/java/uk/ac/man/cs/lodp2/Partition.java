package uk.ac.man.cs.lodp2;

import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.ont.*;

import org.semanticweb.owlapi.model.*;
import java.io.*;
import java.util.Set;
import java.util.*;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Created by chris on 08/08/18.
 */
public class Partition extends LODP {

    private Set<OWLAxiom> owlDisjointUnion;//using (native) OWL axiom

    private DisjointUnionFinder disjointUnionFinder;//union of classes + disjointness axioms
    private OneOfAllDifferentFinder oneOfAllDifferentFinder; 

    private String description;

    public Partition(OWLOntology o) throws Exception {
        this.ontology = o; 

        this.owlDisjointUnion = new HashSet<>();
        this.disjointUnionFinder = new DisjointUnionFinder(this.ontology);
        this.oneOfAllDifferentFinder = new OneOfAllDifferentFinder(this.ontology); 

        this.description = "The pattern 'Partition' " +
               "consists of a named class for a set of disjoint classes or individuals."; 
    }

    public void computeDisjointAxioms(){
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);
        this.owlDisjointUnion = AxiomType.getAxiomsOfTypes(tBox, AxiomType.DISJOINT_UNION);
    }

    public void computeUnionOfDisjointClasses(boolean b) throws Exception {
        this.disjointUnionFinder.run(b);
    }

    public void computeOneOfAllDifferentIndividuals(boolean b) throws Exception {
        this.oneOfAllDifferentFinder.run(b); 
    }

    public DisjointUnionFinder getUnionOfDisjointClasses(){
        return this.disjointUnionFinder; 
    }

    public OneOfAllDifferentFinder getOneOfAllDifferentFinder(){
        return this.oneOfAllDifferentFinder; 
    }

    public void computeReuse(){
        ; 
    }

    public void computeReuse(boolean b) throws Exception {
        computeDisjointAxioms();
        computeUnionOfDisjointClasses(b); 
    }

    public void writeEvidence(String destFile){
        ;
    }
    public void printDescription(){}
    public void setOntology(OWLOntology o){}
    public void reset(){}
}
