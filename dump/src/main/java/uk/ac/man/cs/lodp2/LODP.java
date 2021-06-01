package uk.ac.man.cs.lodp2;

import uk.ac.man.cs.util.Pair;
import java.util.Set;
import java.util.HashSet;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLAxiom;

public abstract class LODP {

    protected OWLOntology ontology;

    public abstract void computeReuse(); 
    public abstract void writeEvidence(String destFile);
    public abstract void printDescription(); 
    public abstract void setOntology(OWLOntology o);
    public abstract void reset();

    public OWLOntology getOntology(){
        return this.ontology; 
    }
}
