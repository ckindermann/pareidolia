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
public class AxiomAnalyser {

    protected Set<OWLAxiom> seedAxioms;

    private ClassAxiomAnalyser classAxiomAnalyser;
    private PropertyAxiomAnalyser propertyAnalyser;
    private IndividualAxiomAnalyser individualAnalyser;

    public AxiomAnalyser(Set<OWLAxiom> seed){
        this.seedAxioms = seed;
        this.classAxiomAnalyser = new ClassAxiomAnalyser(seed);
        this.propertyAnalyser = new PropertyAxiomAnalyser(seed);
        this.individualAnalyser = new IndividualAxiomAnalyser(seed); 
    }

    public void setSeed(Set<OWLAxiom> seed){
        //this.seedAxioms.clear();
        this.seedAxioms = seed;
        this.classAxiomAnalyser.setSeed(seed);
        this.propertyAnalyser.setSeed(seed);
        this.individualAnalyser.setSeed(seed); 
    }

    public boolean coversFingerprint(AxiomAnalyser analyser){
        if(!coversClassFingerprint(analyser))
            return false;
        if(!coversPropertyFingerprint(analyser))
            return false;
        if(!coversIndividualFingerprint(analyser))
            return false; 

        return true;
    }

    public boolean coversClassFingerprint(AxiomAnalyser analyser){
        int[] classFingerprint = this.classAxiomAnalyser.getFingerprint();
        int[] otherClassFingerprint = analyser.getClassAxiomAnalyser().getFingerprint();
        for(int i=0;i<6;i++){
            if(classFingerprint[i] < otherClassFingerprint[i])
                return false;
        }
        return true; 
    }

    public boolean coversPropertyFingerprint(AxiomAnalyser analyser){
        int[] propertyFingerprint = this.propertyAnalyser.getFingerprint();
        int[] otherPropertyFingerprint = analyser.getPropertyAxiomAnalyser().getFingerprint();
        for(int i=0;i<30;i++){
            if(propertyFingerprint[i] < otherPropertyFingerprint[i])
                return false;
        }
        return true; 
    }

    public boolean coversIndividualFingerprint(AxiomAnalyser analyser){
        int[] individualFingerprint = this.individualAnalyser.getFingerprint();
        int[] otherIndividualFingerprint = analyser.getIndividualAxiomAnalyser().getFingerprint();
        for(int i=0;i<10;i++){
            if(individualFingerprint[i] < otherIndividualFingerprint[i])
                return false;
        }
        return true; 
    }

    public ClassAxiomAnalyser getClassAxiomAnalyser(){
        return this.classAxiomAnalyser; 
    }

    public PropertyAxiomAnalyser getPropertyAxiomAnalyser(){ 
        return this.propertyAnalyser;
    }

    public IndividualAxiomAnalyser getIndividualAxiomAnalyser(){
        return this.individualAnalyser; 
    }

    public void print(){ 
        this.classAxiomAnalyser.print();
        this.propertyAnalyser.print();
        this.individualAnalyser.print();
    }

    public String fingerprint(){
        return(this.classAxiomAnalyser.fingerprint() + "\n" 
        + this.propertyAnalyser.fingerprint() + "\n"
        + this.individualAnalyser.fingerprint());
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
        for(OWLAxiom axiom : this.seedAxioms){
            if((axiom.getSignature()).contains(entity)){
                    res.add(axiom);
            }
        }
        return res; 
    }
}

