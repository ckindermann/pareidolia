package uk.ac.man.cs.detectors.structural.disjointUnion;

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
public class EntailedDisjointUnionDetector extends Detector {

    private OWLDataFactory factory;
    private OWLReasoner reasoner;

    private Set<DisjointUnion> disjointUnions;

    public EntailedDisjointUnionDetector(OWLOntology o) throws Exception {
        super(o);
        this.reasoner = ReasonerLoader.initReasoner(this.ontology);
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

        this.disjointUnions = new HashSet<>();
    }

    /** Implement abstract classes from Detector */
    public void reset(){
        this.disjointUnions.clear(); 
    }

    public void setOntology(OWLOntology o){
        this.reset();
        this.ontology = o; 
    }

    public void run(){ 
        //this.detectAssertedDisjointUnions();

    }

    public void write(String destFile){ 
        ;
    }


    /** entailed Disjoint Unions =======  **/

    public void detectEntailedDisjointUnions(){
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);
        Set<OWLAxiom> equivAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.EQUIVALENT_CLASSES); 

        for(OWLAxiom a : equivAxioms){
            List<OWLClassExpression> classExpressions = (((OWLEquivalentClassesAxiom) a).getClassExpressionsAsList());

            //an equivalence axiom has a "left hand side" and a "right hand side" 
            Iterator<OWLClassExpression> it = classExpressions.iterator(); 
            OWLClassExpression LHS = it.next();
            OWLClassExpression RHS = it.next();

            probeUnionForEntailedDisjointness(LHS, RHS, a);
            probeUnionForEntailedDisjointness(RHS, LHS, a); 
        } 
    }

    private boolean probeUnionForEntailedDisjointness(OWLClassExpression candidateName, OWLClassExpression candidateUnion, OWLAxiom a){
        boolean assertedUnionEntailedDisjoint = false;
        if(isUnion(candidateUnion)){
            OWLObjectUnionOf union = (OWLObjectUnionOf) candidateUnion;
            if(isEntailedDisjoint(union)){
                assertedUnionEntailedDisjoint = true;
                OWLDisjointClassesAxiom disjointUnion = makeDisjoint(union); 
                this.disjointUnions.add(new DisjointUnion(a, disjointUnion, candidateName.asOWLClass(),false,axiomUsage(a)));
            } 
        }
        return assertedUnionEntailedDisjoint; 
    }

    private boolean isUnion(OWLClassExpression expression){
        boolean isUnion = false;
        if(expression instanceof OWLObjectUnionOf){
            isUnion = true;
        }
        return isUnion; 
    }

    //tests whether a union of classes is a *disjoint* union of classes
    private boolean isEntailedDisjoint(OWLObjectUnionOf union){

        boolean isDisjoint = false;

        OWLDisjointClassesAxiom candidate = makeDisjoint(union);

        if(this.reasoner.isEntailed(candidate))
            isDisjoint = true;

        return isDisjoint; 
    }

    private OWLDisjointClassesAxiom makeDisjoint(OWLObjectUnionOf union){
        Set<OWLClassExpression> operands = union.getOperands(); 
        return this.factory.getOWLDisjointClassesAxiom(operands); 
    }


    /** utility functions */


    public Set<DisjointUnion> getEntailedDisjointUnions(){
        return this.disjointUnions; 
    }
}
