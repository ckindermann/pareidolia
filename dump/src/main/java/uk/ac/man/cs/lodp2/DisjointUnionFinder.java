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
 * Created by chris on 23/11/18.
 */
public class DisjointUnionFinder {

    private OWLOntology ontology;
    private OWLDataFactory factory;
    private OWLReasoner reasoner;

    private Set<DisjointUnion> assertedDisjointUnions;
    private Set<DisjointUnion> entailedDisjointUnions;

    public DisjointUnionFinder(OWLOntology o) throws Exception {
        this.ontology = o;
        this.reasoner = ReasonerLoader.initReasoner(this.ontology);
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

        this.assertedDisjointUnions = new HashSet<>();
        this.entailedDisjointUnions = new HashSet<>();
    }

    public Set<DisjointUnion> getAssertedDisjointUnions(){
        return this.assertedDisjointUnions; 
    }

    public Set<DisjointUnion> getEntailedDisjointUnions(){
        return this.entailedDisjointUnions; 
    }

    public void run(boolean includeEntailments){
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);
        Set<OWLAxiom> equivAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.EQUIVALENT_CLASSES); 

        for(OWLAxiom a : equivAxioms){
            //only checks for explicitly stated disjointness axioms
            boolean stated = isAssertedDisjointUnion(a);
            if(includeEntailments && !stated){
                isEntailedDisjointUnion(a);
            }
        }
    }

    private boolean isEntailedDisjointUnion(OWLAxiom a){
        List<OWLClassExpression> classExpressions = (((OWLEquivalentClassesAxiom) a).getClassExpressionsAsList());

        //an equivalence axiom has a "left hand side" and a "right hand side" 
        Iterator<OWLClassExpression> it = classExpressions.iterator(); 
        OWLClassExpression LHS = it.next();
        OWLClassExpression RHS = it.next();

        if(LHS instanceof OWLObjectUnionOf){
            Set<OWLClassExpression> operands = ((OWLObjectUnionOf) LHS).getOperands();

            OWLDisjointClassesAxiom dis = this.factory.getOWLDisjointClassesAxiom(operands); 
            if(this.reasoner.isEntailed(dis)){
                this.entailedDisjointUnions.add(new DisjointUnion(a, dis, RHS.asOWLClass(),false));
                return true;
            }
        }

        if(RHS instanceof OWLObjectUnionOf){
            Set<OWLClassExpression> operands = ((OWLObjectUnionOf) RHS).getOperands();

            OWLDisjointClassesAxiom dis = this.factory.getOWLDisjointClassesAxiom(operands); 
            if(this.reasoner.isEntailed(dis)){
                this.entailedDisjointUnions.add(new DisjointUnion(a, dis, LHS.asOWLClass(), false));
                return true;
            }
        }
        return false;
    }


    //axiom d: disjoint axiom for a set of classes
    //axiom a: axiom that contains the signature of d PLUS one more term
    //check whether axiom a introduces a named class for d
    private boolean isAssertedDisjointUnion(OWLAxiom n){
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);
        Set<OWLAxiom> disjointClassesAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.DISJOINT_CLASSES); 

        for(OWLAxiom d : disjointClassesAxioms){
            //get signatures of axioms
            Set<OWLEntity> ns = n.getSignature();
            Set<OWLEntity> ds = d.getSignature();

            //partition axiom has to contain all elements of the disjoint axioms
            if(!ns.containsAll(ds))
                continue;

            //the signature should differ only by 1
            //i.e. the introduced name for the disjoint union
            if(ns.size() != ds.size() + 1)
                continue;

            //check whether Axiom n introduces a name for a union of classes
            //(which are disjoint according to d)

            //get classes from disjointness axioms
            Set<OWLClass> classes = new HashSet<>();
            for(OWLEntity e : ds){
                if(e.isOWLClass())
                    classes.add(e.asOWLClass()); 
            }

            //get candidate name for the named union
            OWLClass name = null;
            for(OWLEntity e : ns){
                if(!ds.contains(e) && e.isOWLClass())
                    name = e.asOWLClass();
            }

            if(!classes.isEmpty() && name != null){

                //construct axiom for a named union of classes
                OWLObjectUnionOf union = this.factory.getOWLObjectUnionOf(classes);
                OWLEquivalentClassesAxiom equiv = this.factory.getOWLEquivalentClassesAxiom(name, union);
                //test whether the constructed axiom is equal to n
                if(n.equalsIgnoreAnnotations(equiv)){
                    assertedDisjointUnions.add(new DisjointUnion(n,d,name,true));
                    return true;
                }
            }
        }
        return false;
    }
}
