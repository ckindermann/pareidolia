package uk.ac.man.cs.detectors;

import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.lodp.*;
import uk.ac.man.cs.util.*;

import java.io.*;
import java.util.*;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.apibinding.OWLManager;
import java.util.Iterator;

import org.semanticweb.owlapi.model.*;

import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
//import org.semanticweb.owlapi.reasoner.OWLOntologyCreationException;
import org.semanticweb.owlapi.util.AbstractOWLStorer;
import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;

/**
 * The Dector defines the basic operations of a
 * pattern detctor.
 *
 * @author Christian Kindermann
 * @version 1.0
 * @since 2018-10-08
 *
 */
public abstract class Detector {

    protected OWLOntology ontology;

    public abstract void setOntology(OWLOntology o);
    public abstract void run();
    public abstract void reset();
    public abstract void write(String destFile);

    public Detector(OWLOntology o){
        this.ontology = o; 
    }

    public OWLOntology getOntology(){
        return this.ontology;
    } 

    protected HashMap<OWLEntity,Set<OWLAxiom>> axiomUsage(OWLAxiom a){
        HashMap<OWLEntity,Set<OWLAxiom>> res = new HashMap<>();
        for(OWLEntity e : a.getSignature()){
            res.put(e, entityUsage(e));
        }
        return res; 
    }

    protected Set<OWLAxiom> entityUsage(OWLEntity entity){
        Set<OWLAxiom> res = new HashSet<>(); 
        for(OWLAxiom axiom : ontology.getAxioms(Imports.EXCLUDED)){
            if((axiom.getSignature()).contains(entity)){
                    res.add(axiom);
            }
        }
        return res; 
    }

    protected Set<OWLAxiom> getSubsumptionAxioms(){ 
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);

        Set<OWLAxiom> subClassAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.SUBCLASS_OF); 
        Set<OWLAxiom> subsumptionAxioms = new HashSet<>(); 
        subsumptionAxioms.addAll(subClassAxioms);

        Set<OWLAxiom> equivAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.EQUIVALENT_CLASSES);
        for(OWLAxiom a : equivAxioms){
            subsumptionAxioms.addAll(((OWLEquivalentClassesAxiom) a).asOWLSubClassOfAxioms()); 
        } 
        return subsumptionAxioms;
    }
}
