package uk.ac.man.cs.analysers;

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
public class IndividualAxiomAnalyser {

    //protected OWLOntology ontology;
    protected Set<OWLAxiom> seedAxioms;

    //first tier
    private Set<OWLAxiom> individualAxioms; 
    //second tier
    private Set<OWLAxiom> classAssertionAxioms;
    private Set<OWLAxiom> naryIndividualAxioms;
    private Set<OWLAxiom> propertyAssertionAxioms;
    //third tier
    private Set<OWLAxiom> negativeObjectPropertyAssertionAxiom;
    private Set<OWLAxiom> differentIndividualsAxiom;
    private Set<OWLAxiom> sameIndividualAxiom;
    private Set<OWLAxiom> dataPropertyAssertionAxiom;
    private Set<OWLAxiom> negativeDataPropertyAssertionAxiom;
    private Set<OWLAxiom> objectPropertyAssertionAxiom;

    private int[] fingerprint;

    public IndividualAxiomAnalyser(Set<OWLAxiom> s){
        //this.ontology = o;
        this.seedAxioms = s;
        this.fingerprint = new int[10];

        individualAxioms = new HashSet<>();
        classAssertionAxioms = new HashSet<>();
        naryIndividualAxioms = new HashSet<>();
        propertyAssertionAxioms = new HashSet<>();

        negativeObjectPropertyAssertionAxiom = new HashSet<>();
        differentIndividualsAxiom = new HashSet<>();
        sameIndividualAxiom = new HashSet<>();
        dataPropertyAssertionAxiom = new HashSet<>();
        negativeDataPropertyAssertionAxiom = new HashSet<>();
        objectPropertyAssertionAxiom = new HashSet<>();


        this.initialise();
        this.setFingerprint(); 
    }

    public void setSeed(Set<OWLAxiom> seed){
        //this.seedAxioms.clear();
        this.reset();
        this.seedAxioms = seed;
        this.initialise();
        this.setFingerprint(); 
    }

    private void reset(){
        this.individualAxioms.clear(); 
        this.classAssertionAxioms.clear();
        this.naryIndividualAxioms.clear();
        this.propertyAssertionAxioms.clear();
        this.negativeObjectPropertyAssertionAxiom.clear();
        this.differentIndividualsAxiom.clear();
        this.sameIndividualAxiom.clear();
        this.dataPropertyAssertionAxiom.clear();
        this.negativeDataPropertyAssertionAxiom.clear();
        this.objectPropertyAssertionAxiom.clear(); 
    }

    private void setFingerprint(){
        this.fingerprint[0] = individualAxioms.size();

        this.fingerprint[1] = classAssertionAxioms.size();
        this.fingerprint[2] = naryIndividualAxioms.size();
        this.fingerprint[3] = propertyAssertionAxioms.size();

        this.fingerprint[4] = negativeObjectPropertyAssertionAxiom.size();
        this.fingerprint[5] = differentIndividualsAxiom.size();
        this.fingerprint[6] = sameIndividualAxiom.size();
        this.fingerprint[7] = dataPropertyAssertionAxiom.size();
        this.fingerprint[8] = negativeDataPropertyAssertionAxiom.size();
        this.fingerprint[9] = objectPropertyAssertionAxiom.size(); 
    }

    public int[] getFingerprint(){
        return this.fingerprint; 
    }

    private void initialise(){
        //Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED); 
        for(OWLAxiom axiom : this.seedAxioms){ 
            if(axiom instanceof OWLIndividualAxiom)
                individualAxioms.add(axiom); 
            if(axiom instanceof OWLClassAssertionAxiom)
                classAssertionAxioms.add(axiom);
            if(axiom instanceof OWLNaryIndividualAxiom)
                naryIndividualAxioms.add(axiom);
            if(axiom instanceof OWLPropertyAssertionAxiom)
                propertyAssertionAxioms.add(axiom); 

            if(axiom instanceof OWLNegativeObjectPropertyAssertionAxiom)
                negativeObjectPropertyAssertionAxiom.add(axiom);
            if(axiom instanceof OWLDifferentIndividualsAxiom)
                differentIndividualsAxiom.add(axiom);
            if(axiom instanceof OWLSameIndividualAxiom)
                sameIndividualAxiom.add(axiom);
            if(axiom instanceof OWLDataPropertyAssertionAxiom)
                dataPropertyAssertionAxiom.add(axiom);
            if(axiom instanceof OWLNegativeDataPropertyAssertionAxiom)
                negativeDataPropertyAssertionAxiom.add(axiom);
            if(axiom instanceof OWLObjectPropertyAssertionAxiom)
                objectPropertyAssertionAxiom.add(axiom);
        } 
    }


    protected Set<OWLAxiom> axiomUsage(OWLAxiom a){
        Set<OWLAxiom> axiomsWithSharedSignature = new HashSet<>();
        Set<OWLEntity> aSig = a.getSignature();
        for(OWLEntity e : aSig){
            Set<OWLAxiom> usage = entityUsage(e);
            axiomsWithSharedSignature.addAll(usage); 
        } 
        return axiomsWithSharedSignature;
    }

    protected Set<OWLAxiom> entityUsage(OWLEntity entity){
        Set<OWLAxiom> res = new HashSet<>(); 
        //for(OWLAxiom axiom : ontology.getAxioms(Imports.EXCLUDED)){
        for(OWLAxiom axiom : this.individualAxioms){
            if((axiom.getSignature()).contains(entity)){
                    res.add(axiom);
            }
        }
        return res; 
    }

    public String fingerprint(){
        return(individualAxioms.size() + ","
            + classAssertionAxioms.size() + ","
            + naryIndividualAxioms.size() + ","
            + propertyAssertionAxioms.size() + "," 
            + negativeObjectPropertyAssertionAxiom.size() + ","
            + differentIndividualsAxiom.size() + ","
            + sameIndividualAxiom.size() + ","
            + dataPropertyAssertionAxiom.size() + ","
            + negativeDataPropertyAssertionAxiom.size() + ","
            + objectPropertyAssertionAxiom.size()); 
    }

    public void print(){
        System.out.println("===Individual Axiom Summary===");
        System.out.println("Individual axioms " + individualAxioms.size());
        System.out.println("Class Assertion " + classAssertionAxioms.size());
        System.out.println("Nary individual  " + naryIndividualAxioms.size());
        System.out.println("Property Assertion " + propertyAssertionAxioms.size());
    }

    //protected Set<OWLAxiom> getSubsumptionAxioms(){ 
    //    Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);

    //    Set<OWLAxiom> subClassAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.SUBCLASS_OF); 
    //    Set<OWLAxiom> subsumptionAxioms = new HashSet<>(); 
    //    subsumptionAxioms.addAll(subClassAxioms);

    //    Set<OWLAxiom> equivAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.EQUIVALENT_CLASSES);
    //    for(OWLAxiom a : equivAxioms){
    //        subsumptionAxioms.addAll(((OWLEquivalentClassesAxiom) a).asOWLSubClassOfAxioms()); 
    //    } 
    //    return subsumptionAxioms;
    //} 
}

