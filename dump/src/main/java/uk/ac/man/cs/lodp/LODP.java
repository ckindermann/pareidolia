package uk.ac.man.cs.lodp;

import uk.ac.man.cs.util.Pair;
import java.util.Set;
import java.util.HashSet;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLAxiom;

public abstract class LODP {

    protected OWLOntology ontology;
    protected Set<Pair<String, Set<OWLAxiom>>> evidence;

    public abstract void computeEvidence(); 
    public abstract Set<Pair<String, Set<OWLAxiom>>> getEvidence();
    public abstract boolean writeEvidence(String destFile);
    public abstract void printDescription(); 
    public abstract void setOntology(OWLOntology o);
    public abstract void reset();

    public OWLOntology getOntology(){
        return this.ontology; 
    }

    protected Pair<String, Set<OWLAxiom>> patternInstance(String str, Set<OWLAxiom> set){
        return new Pair<String, Set<OWLAxiom>>(str, set);
    }
}
