package uk.ac.man.cs.detectors.structural.union;

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
public class UnionDetector extends Detector {

    private OWLDataFactory factory;
    private OWLReasoner reasoner;

    private Set<Union> assertedDisjointUnions;
    private Set<Union> entailedDisjointUnions;

    public UnionDetector(OWLOntology o) throws Exception {
        super(o);
        this.reasoner = ReasonerLoader.initReasoner(this.ontology);
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

        this.assertedDisjointUnions = new HashSet<>();
        this.entailedDisjointUnions = new HashSet<>();
    }

    /** Implement abstract classes from Detector */
    public void reset(){
        this.assertedDisjointUnions.clear();
        this.entailedDisjointUnions.clear(); 
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

    /** asserted Disjoint Unions =======  **/

    public void detectAssertedDisjointUnions(){
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);
        Set<OWLAxiom> equivAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.EQUIVALENT_CLASSES); 
        Set<OWLAxiom> disjointClassesAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.DISJOINT_CLASSES); 

        for(OWLAxiom axiom : equivAxioms){
            Set<OWLEntity> axiomSignature = axiom.getSignature();

            for(OWLAxiom disjointAxiom : disjointClassesAxioms){ 
                Set<OWLEntity> disjointAxiomSignature = disjointAxiom.getSignature();

                if(!satisfiesSignatureConstraints(axiomSignature, disjointAxiomSignature))
                    continue;

                //get classes from signature of disjointness axiom
                Set<OWLClass> classes = getClasses(disjointAxiomSignature);

                //get candidate name for the named union
                OWLClass name = getCandidateDisjointUnionName(axiomSignature, disjointAxiomSignature);

                //if(isNamedDisjointUnion(axiom, name, classes))
                        //assertedDisjointUnions.add(new DisjointUnion(axiom,disjointAxiom,name,true,axiomUsage(axiom))); 
            }
        } 
    }

    private boolean isNamedDisjointUnion(OWLAxiom a, OWLClass name, Set<OWLClass> classes){
        boolean isNamedDisjointUnion = false;
        if(!classes.isEmpty() && name != null){

            //construct axiom for a named union of classes
            OWLObjectUnionOf union = this.factory.getOWLObjectUnionOf(classes);
            OWLEquivalentClassesAxiom equiv = this.factory.getOWLEquivalentClassesAxiom(name, union);
            //test whether the constructed axiom is equal to a
            isNamedDisjointUnion = a.equalsIgnoreAnnotations(equiv);
        }

        return isNamedDisjointUnion; 
    }

    private boolean satisfiesSignatureConstraints(Set<OWLEntity> as, Set<OWLEntity> ds){
        boolean satisfiesConstraints = true;

        //partition axiom has to contain all elements of the disjoint axioms
        if(!as.containsAll(ds))
            satisfiesConstraints = false;

        //the signature should differ only by 1
        //i.e. the introduced name for the disjoint union
        if(as.size() != ds.size() + 1)
            satisfiesConstraints = false;

        return satisfiesConstraints;
    }

    private Set<OWLClass> getClasses(Set<OWLEntity> s){
        Set<OWLClass> classes = new HashSet<>(); 
        for(OWLEntity e : s){
            if(e.isOWLClass())
                classes.add(e.asOWLClass()); 
        } 
        return classes;
    }

    private OWLClass getCandidateDisjointUnionName(Set<OWLEntity> as, Set<OWLEntity> ds){
        OWLClass name = null;
        for(OWLEntity e : as){
            if(!ds.contains(e) && e.isOWLClass())
                name = e.asOWLClass();
        }
        return name; 
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
                //this.entailedDisjointUnions.add(new DisjointUnion(a, disjointUnion, candidateName.asOWLClass(),false,axiomUsage(a)));
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


//    private HashMap<OWLEntity,Set<OWLAxiom>> axiomUsage(OWLAxiom a){
//        HashMap<OWLEntity,Set<OWLAxiom>> res = new HashMap<>();
//        for(OWLEntity e : a.getSignature()){
//            res.put(e, entityUsage(e));
//        }
//        return res; 
//    }
//
//    private Set<OWLAxiom> entityUsage(OWLEntity entity){
//        Set<OWLAxiom> res = new HashSet<>(); 
//        for(OWLAxiom axiom : ontology.getAxioms(Imports.EXCLUDED)){
//            if((axiom.getSignature()).contains(entity)){
//                    res.add(axiom);
//            }
//        }
//        return res; 
//    }


    public Set<Union> getAssertedDisjointUnions(){
        return this.assertedDisjointUnions; 
    }

    public Set<Union> getEntailedDisjointUnions(){
        return this.entailedDisjointUnions; 
    }
}
