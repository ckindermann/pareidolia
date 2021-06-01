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
public class IntersectionDetector extends Detector {

    private OWLDataFactory factory;
    private OWLReasoner reasoner;

    private AssertedIntersectionDetector assertedIntersectionDetector;

    public IntersectionDetector(OWLOntology o) throws Exception {
        super(o);
        this.reasoner = ReasonerLoader.initReasoner(this.ontology);
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

        this.assertedIntersectionDetector = new AssertedIntersectionDetector(this.ontology); 
    }

    /** Implement abstract classes from Detector */
    public void reset(){
        this.assertedIntersectionDetector.reset();
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
}
