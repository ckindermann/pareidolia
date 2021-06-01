package uk.ac.man.cs.metrics;
import org.semanticweb.owlapi.model.OWLOntology;

public abstract class Metric {

    protected OWLOntology ontology;

    public abstract void compute();
    public abstract void write(String s);
    public abstract void reset();
    public abstract void setOntology(OWLOntology o);

    public OWLOntology getOntology(){
        return this.ontology; 
    } 
}
