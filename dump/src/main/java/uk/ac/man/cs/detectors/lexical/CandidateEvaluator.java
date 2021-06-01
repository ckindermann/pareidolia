package uk.ac.man.cs.detectors.lexical;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.man.cs.ont.*;
import java.util.*;

/*
 * Created by chris on 03/10/18.
 */
public class CandidateEvaluator {

    private OWLOntology ontology;
    private OWLOntology patternCandidate;
    private OWLReasoner reasoner;

    private boolean contained;
    private boolean entailed;

    public CandidateEvaluator(OWLOntology o) throws Exception {
        this.ontology = o;
        reasoner = ReasonerLoader.initReasoner(this.ontology); 
    }


    public CandidateEvaluator(OWLOntology o, OWLOntology pc) throws Exception{
        this.ontology = o;
        this.patternCandidate = pc;
        reasoner = ReasonerLoader.initReasoner(this.ontology); 

        Set<OWLAxiom> ontologyAxioms = getLogicalAxioms(this.ontology, true); 
        Set<OWLAxiom> patternAxioms = getLogicalAxioms(this.ontology, true);

        this.contained = true;
        this.entailed = true;
        for(OWLAxiom axiom : patternAxioms){
            if(!ontologyAxioms.contains(axiom)){
                this.contained = false; 
                if(!this.reasoner.isEntailed(axiom)){
                    this.entailed = false;
                }
            }
        }
    }

    public boolean isContained(){
        return this.contained; 
    }

    public boolean isEntailed(){
        return this.entailed; 
    }

    public void setCandidate(OWLOntology pc){
        this.patternCandidate = pc; 

        Set<OWLAxiom> ontologyAxioms = getLogicalAxioms(this.ontology, true); 
        Set<OWLAxiom> patternAxioms = getLogicalAxioms(this.ontology, true);

        this.contained = true;
        this.entailed = true;
        for(OWLAxiom axiom : patternAxioms){
            if(!ontologyAxioms.contains(axiom)){
                this.contained = false; 
                if(!this.reasoner.isEntailed(axiom)){
                    this.entailed = false;
                }
            }
        }
    }

    private Set<OWLAxiom> getLogicalAxioms(OWLOntology ont, boolean includeClosure) {
        Set<OWLLogicalAxiom> logicalAxioms = ont.getLogicalAxioms(includeClosure);
        Set<OWLAxiom> axioms = new HashSet<>();  
        for(OWLLogicalAxiom axiom : logicalAxioms){
            axioms.add((OWLAxiom) axiom);
        }
        return axioms;
    }

}
