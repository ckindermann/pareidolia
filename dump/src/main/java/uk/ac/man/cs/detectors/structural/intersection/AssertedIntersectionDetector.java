package uk.ac.man.cs.detectors.structural.intersection;

import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.ont.*;

import org.semanticweb.owlapi.model.*;
import java.io.*;
import java.util.Set;
import java.util.*;

import uk.ac.man.cs.detectors.*;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Created by chris on 23/11/18.
 */
public class AssertedIntersectionDetector extends Detector {

    private OWLDataFactory factory; 
    private Set<Intersection> intersections;

    public AssertedIntersectionDetector(OWLOntology o) throws Exception {
        super(o);
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

        this.intersections = new HashSet<>();
    }

    /** Implement abstract classes from Detector */
    public void reset(){
        this.intersections.clear();
    }

    public void setOntology(OWLOntology o){
        this.reset();
        this.ontology = o; 
    }

    public void run(){ 
        ; 
    }

    public void write(String destFile){ 
        ;
    }

    /** asserted intersections =======  **/

    public void detectAssertedIntersections(){
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);
        Set<OWLAxiom> equivAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.EQUIVALENT_CLASSES); 
        //Set<OWLAxiom> disjointClassesAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.DISJOINT_CLASSES); 

        for(OWLAxiom axiom : equivAxioms){
            Set<OWLEntity> axiomSignature = axiom.getSignature();

            List<OWLClassExpression> classExpressions = (((OWLEquivalentClassesAxiom) axiom).getClassExpressionsAsList());

            //an equivalence axiom has a "left hand side" and a "right hand side" 
            Iterator<OWLClassExpression> it = classExpressions.iterator(); 
            OWLClassExpression LHS = it.next();
            OWLClassExpression RHS = it.next();

            if(isIntersection(LHS)){
                this.intersections.add(new Intersection(axiom, RHS.asOWLClass(), axiomUsage(axiom))); 
            }
            if(isIntersection(RHS)){
                this.intersections.add(new Intersection(axiom, LHS.asOWLClass(), axiomUsage(axiom))); 
            } 
        }
    } 

    private boolean isIntersection(OWLClassExpression expression){
        boolean isUnion = false;
        if(expression instanceof OWLObjectIntersectionOf){
            isUnion = true;
        }
        return isUnion; 
    }

    private Set<OWLClass> getClasses(Set<OWLEntity> s){
        Set<OWLClass> classes = new HashSet<>(); 
        for(OWLEntity e : s){
            if(e.isOWLClass())
                classes.add(e.asOWLClass()); 
        } 
        return classes;
    } 

    public Set<Intersection> getAssertedIntersections(){
        return this.intersections; 
    } 
}
