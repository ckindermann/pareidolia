package uk.ac.man.cs.lodp;

import uk.ac.man.cs.util.Pair;
import java.util.Set;
import java.util.HashSet;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLAxiom;

/** A structural pattern consists of a set of axioms
 */
public abstract class StructuralPattern {

    protected OWLOntology ontology; 
    protected Set<Set<OWLAxiom>> evidence;

    public abstract void computeEvidence(); 
    public abstract boolean writeEvidence(String destFile);
    public abstract void printDescription(); 
    public abstract void setOntology(OWLOntology o);
    public abstract void reset();

    public OWLOntology getOntology(){
        return this.ontology; 
    }

    public Set<Set<OWLAxiom>> getEvidence(){
        return this.evidence; 
    }
}
