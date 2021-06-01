package uk.ac.man.cs.lodp2;

import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.ont.*;

import org.semanticweb.owlapi.model.*;
import java.io.*;
import java.util.*;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Created by chris 21/11/18
 */
public class OneOfAllDifferentFinder {

    private OWLOntology ontology;
    private OWLDataFactory factory;
    private OWLReasoner reasoner;

    private Set<OWLAxiom> allDifferenAxioms;
    private Set<OWLAxiom> assertedOneOfAllDifferent;
    private Set<OWLAxiom> entailedOneOfAllDifferent;

    public OneOfAllDifferentFinder(OWLOntology ont) throws Exception {

        this.ontology = ont;
        this.reasoner = ReasonerLoader.initReasoner(this.ontology);
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

        this.allDifferenAxioms = new HashSet<>();
        assertedOneOfAllDifferent = new HashSet<>();
        entailedOneOfAllDifferent = new HashSet<>();
    }

    public Set<OWLAxiom> getAsserted(){
        return this.assertedOneOfAllDifferent; 
    }

    public Set<OWLAxiom> getEntailed(){ 
        return this.entailedOneOfAllDifferent;
    }

    public void run(boolean includeEntailments){

        this.computeAllDifferent(); 

        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);
        Set<OWLAxiom> equivAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.EQUIVALENT_CLASSES); 

        for(OWLAxiom a : equivAxioms){
            boolean asserted = isAssertedOneOfAllDifferent(a);
            if(includeEntailments && !asserted){
                isEntailedOneOfAllDifferent(a);
            }
        }
    }

    private void computeAllDifferent(){
        Set<OWLAxiom> aBox = this.ontology.getABoxAxioms(Imports.EXCLUDED);
        this.allDifferenAxioms = AxiomType.getAxiomsOfTypes(aBox, AxiomType.DIFFERENT_INDIVIDUALS);
    }


    private boolean isEntailedOneOfAllDifferent(OWLAxiom a){
        List<OWLClassExpression> classExpressions = (((OWLEquivalentClassesAxiom) a).getClassExpressionsAsList());

        //an equivalence axiom has a "left hand side" and a "right hand side" 
        Iterator<OWLClassExpression> it = classExpressions.iterator(); 
        OWLClassExpression LHS = it.next();
        OWLClassExpression RHS = it.next();

        if(LHS instanceof OWLObjectOneOf){
            Set<OWLIndividual> operands = ((OWLObjectOneOf) LHS).getIndividuals();

            OWLDifferentIndividualsAxiom dif = this.factory.getOWLDifferentIndividualsAxiom(operands); 
            if(this.reasoner.isEntailed(dif)){
                this.entailedOneOfAllDifferent.add(a);
                return true;
            }
        }

        if(RHS instanceof OWLObjectOneOf){ 
            Set<OWLIndividual> operands = ((OWLObjectOneOf) RHS).getIndividuals();

            OWLDifferentIndividualsAxiom dif = this.factory.getOWLDifferentIndividualsAxiom(operands);
            if(this.reasoner.isEntailed(dif)){
                this.entailedOneOfAllDifferent.add(a);
                return true;
            }
        }
        return false;
    }

    private boolean isAssertedOneOfAllDifferent(OWLAxiom a){
        for(OWLAxiom d : this.allDifferenAxioms){
            Set<OWLEntity> as = a.getSignature();
            Set<OWLEntity> ds = d.getSignature();

            Set<OWLIndividual> individuals = new HashSet<>();
            for(OWLEntity e : ds){
                if(e.isOWLNamedIndividual())
                    individuals.add(e.asOWLNamedIndividual()); 
            }

            OWLClass name = null;
            for(OWLEntity e : as){
                if(!ds.contains(e) && e.isOWLClass())
                    name=e.asOWLClass(); 
            }

            if(!individuals.isEmpty() && name != null){
                OWLObjectOneOf oneof = this.factory.getOWLObjectOneOf(individuals);
                OWLEquivalentClassesAxiom equiv = this.factory.getOWLEquivalentClassesAxiom(name, oneof);
                if(a.equalsIgnoreAnnotations(equiv)){
                    this.assertedOneOfAllDifferent.add(a);
                    return true; 
                }
            }
        }
            return false;
    } 
}
