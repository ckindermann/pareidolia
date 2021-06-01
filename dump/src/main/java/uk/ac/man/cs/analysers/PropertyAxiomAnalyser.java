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
public class PropertyAxiomAnalyser {

    //protected OWLOntology ontology;
    protected Set<OWLAxiom> seedAxioms;

    //tier 1
    private Set<OWLAxiom> propertyAxioms; 
    private Set<OWLAxiom> dataPropertyAxioms;//doesn't really belong here
    //tier 2
    private Set<OWLAxiom> naryPropertyAxioms;
    private Set<OWLAxiom> subPropertyAxioms;
    private Set<OWLAxiom> unaryPropertyAxioms;
    private Set<OWLAxiom> objectPropertyAxiom;
    //tier 3
    private Set<OWLAxiom> dataPropertyCharacteristicAxiom;
    private Set<OWLAxiom> propertyDomainAxiom;
    private Set<OWLAxiom> propertyRangeAxiom;
    private Set<OWLAxiom> disjointDataPropertiesAxiom ;
    private Set<OWLAxiom> equivalentDataPropertiesAxiom;
    private Set<OWLAxiom> subDataPropertyOfAxiom;
    private Set<OWLAxiom> disjointObjectPropertiesAxiom;
    private Set<OWLAxiom> equivalentObjectPropertiesAxiom;
    private Set<OWLAxiom> inverseObjectPropertiesAxiom;
    private Set<OWLAxiom> subObjectPropertyOfAxiom;
    private Set<OWLAxiom> subPropertyChainOfAxiom; 
    private Set<OWLAxiom> objectPropertyCharacteristicAxiom; 
    //tier 4
    private Set<OWLAxiom> inverseFunctionalObjectPropertyAxiom;
    private Set<OWLAxiom> asymmetricObjectPropertyAxiom;
    private Set<OWLAxiom> functionalObjectPropertyAxiom;
    private Set<OWLAxiom> irreflexiveObjectPropertyAxiom;
    private Set<OWLAxiom> reflexiveObjectPropertyAxiom;
    private Set<OWLAxiom> symmetricObjectPropertyAxiom;
    private Set<OWLAxiom> transitiveObjectPropertyAxiom;
    private Set<OWLAxiom> objectPropertyRangeAxiom;
    private Set<OWLAxiom> dataPropertyDomainAxiom;
    private Set<OWLAxiom> objectPropertyDomainAxiom;
    private Set<OWLAxiom> functionalDataPropertyAxiom;
    private Set<OWLAxiom> dataPropertyRangeAxiom;

    private int[] fingerprint;

    public PropertyAxiomAnalyser(Set<OWLAxiom> s){
        //this.ontology = o;
        this.seedAxioms = s;
        this.fingerprint = new int[30];

        propertyAxioms = new HashSet<>();
        dataPropertyAxioms = new HashSet<>();
        naryPropertyAxioms = new HashSet<>();
        subPropertyAxioms = new HashSet<>();
        unaryPropertyAxioms = new HashSet<>();
        objectPropertyAxiom = new HashSet<>(); 

        dataPropertyCharacteristicAxiom = new HashSet<>();
        propertyDomainAxiom = new HashSet<>();
        propertyRangeAxiom = new HashSet<>();
        disjointDataPropertiesAxiom  = new HashSet<>();
        equivalentDataPropertiesAxiom = new HashSet<>();
        subDataPropertyOfAxiom = new HashSet<>();
        disjointObjectPropertiesAxiom = new HashSet<>();
        equivalentObjectPropertiesAxiom = new HashSet<>();
        inverseObjectPropertiesAxiom = new HashSet<>();
        subObjectPropertyOfAxiom = new HashSet<>();
        subPropertyChainOfAxiom = new HashSet<>();
        objectPropertyCharacteristicAxiom = new HashSet<>();

        inverseFunctionalObjectPropertyAxiom = new HashSet<>();
        asymmetricObjectPropertyAxiom = new HashSet<>();
        functionalObjectPropertyAxiom = new HashSet<>();
        irreflexiveObjectPropertyAxiom = new HashSet<>();
        reflexiveObjectPropertyAxiom = new HashSet<>();
        symmetricObjectPropertyAxiom = new HashSet<>();
        transitiveObjectPropertyAxiom = new HashSet<>();
        objectPropertyRangeAxiom = new HashSet<>();
        dataPropertyDomainAxiom = new HashSet<>();
        objectPropertyDomainAxiom = new HashSet<>();
        functionalDataPropertyAxiom = new HashSet<>();
        dataPropertyRangeAxiom = new HashSet<>();

        this.initialise();
        this.setFingerprint();
    }

    public int[] getFingerprint(){
        return this.fingerprint; 
    }

    public void setSeed(Set<OWLAxiom> seed){
        //this.seedAxioms.clear();
        this.reset();
        this.seedAxioms = seed;
        this.initialise();
        this.setFingerprint(); 
    }

    public void reset(){
        this.propertyAxioms.clear(); 
        this.dataPropertyAxioms.clear();
        this.naryPropertyAxioms.clear();
        this.subPropertyAxioms.clear();
        this.unaryPropertyAxioms.clear();
        this.objectPropertyAxiom.clear();
        this.dataPropertyCharacteristicAxiom.clear();
        this.propertyDomainAxiom.clear();
        this.propertyRangeAxiom.clear();
        this.disjointDataPropertiesAxiom .clear();
        this.equivalentDataPropertiesAxiom.clear();
        this.subDataPropertyOfAxiom.clear();
        this.disjointObjectPropertiesAxiom.clear();
        this.equivalentObjectPropertiesAxiom.clear();
        this.inverseObjectPropertiesAxiom.clear();
        this.subObjectPropertyOfAxiom.clear();
        this.subPropertyChainOfAxiom.clear();
        this.objectPropertyCharacteristicAxiom .clear();
        this.inverseFunctionalObjectPropertyAxiom.clear();
        this.asymmetricObjectPropertyAxiom.clear();
        this.functionalObjectPropertyAxiom.clear();
        this.irreflexiveObjectPropertyAxiom.clear();
        this.reflexiveObjectPropertyAxiom.clear();
        this.symmetricObjectPropertyAxiom.clear();
        this.transitiveObjectPropertyAxiom.clear();
        this.objectPropertyRangeAxiom.clear();
        this.dataPropertyDomainAxiom.clear();
        this.objectPropertyDomainAxiom.clear();
        this.functionalDataPropertyAxiom.clear();
        this.dataPropertyRangeAxiom.clear(); 
    }

    private void setFingerprint(){

        this.fingerprint[0] = propertyAxioms.size();
        this.fingerprint[1] = dataPropertyAxioms.size();
        this.fingerprint[2] = naryPropertyAxioms.size();
        this.fingerprint[3] = subPropertyAxioms.size();
        this.fingerprint[4] = unaryPropertyAxioms.size();
        this.fingerprint[5] = objectPropertyAxiom.size();
        this.fingerprint[6] = dataPropertyCharacteristicAxiom.size();
        this.fingerprint[7] = propertyDomainAxiom.size();
        this.fingerprint[8] = propertyRangeAxiom.size();
        this.fingerprint[9] = disjointDataPropertiesAxiom .size();
        this.fingerprint[10] = equivalentDataPropertiesAxiom.size();
        this.fingerprint[11] = subDataPropertyOfAxiom.size();
        this.fingerprint[12] = disjointObjectPropertiesAxiom.size();
        this.fingerprint[13] = equivalentObjectPropertiesAxiom.size();
        this.fingerprint[14] = inverseObjectPropertiesAxiom.size();
        this.fingerprint[15] = subObjectPropertyOfAxiom.size();
        this.fingerprint[16] = subPropertyChainOfAxiom.size();
        this.fingerprint[17] = objectPropertyCharacteristicAxiom.size();
        this.fingerprint[18] = inverseFunctionalObjectPropertyAxiom.size();
        this.fingerprint[19] = asymmetricObjectPropertyAxiom.size();
        this.fingerprint[20] = functionalObjectPropertyAxiom.size();
        this.fingerprint[21] = irreflexiveObjectPropertyAxiom.size();
        this.fingerprint[22] = reflexiveObjectPropertyAxiom.size();
        this.fingerprint[23] = symmetricObjectPropertyAxiom.size();
        this.fingerprint[24] = transitiveObjectPropertyAxiom.size();
        this.fingerprint[25] = objectPropertyRangeAxiom.size();
        this.fingerprint[26] = dataPropertyDomainAxiom.size();
        this.fingerprint[27] = objectPropertyDomainAxiom.size();
        this.fingerprint[28] = functionalDataPropertyAxiom.size();
        this.fingerprint[29] = dataPropertyRangeAxiom.size();
    }

    private void initialise(){
        //Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED); 
        for(OWLAxiom axiom : this.seedAxioms){ 
            //tier 1 & 2
            if(axiom instanceof OWLPropertyAxiom)
                propertyAxioms.add(axiom); 
            if(axiom instanceof OWLDataPropertyAxiom)
                dataPropertyAxioms.add(axiom);
            if(axiom instanceof OWLNaryPropertyAxiom)
                naryPropertyAxioms.add(axiom);
            if(axiom instanceof OWLSubPropertyAxiom)
                subPropertyAxioms.add(axiom);
            if(axiom instanceof OWLUnaryPropertyAxiom)
                unaryPropertyAxioms.add(axiom);
            if(axiom instanceof OWLObjectPropertyAxiom)
                objectPropertyAxiom.add(axiom); 

            //tier 3
            if(axiom instanceof OWLDataPropertyCharacteristicAxiom)
                dataPropertyCharacteristicAxiom.add(axiom);
            if(axiom instanceof OWLPropertyDomainAxiom)
                propertyDomainAxiom.add(axiom);
            if(axiom instanceof OWLPropertyRangeAxiom)
                propertyRangeAxiom.add(axiom);
            if(axiom instanceof OWLDisjointDataPropertiesAxiom)
                disjointDataPropertiesAxiom.add(axiom);
            if(axiom instanceof OWLEquivalentDataPropertiesAxiom)
                equivalentDataPropertiesAxiom.add(axiom);
            if(axiom instanceof OWLSubDataPropertyOfAxiom)
                subDataPropertyOfAxiom.add(axiom);
            if(axiom instanceof OWLDisjointObjectPropertiesAxiom)
                disjointObjectPropertiesAxiom.add(axiom);
            if(axiom instanceof OWLEquivalentObjectPropertiesAxiom)
                equivalentObjectPropertiesAxiom.add(axiom);
            if(axiom instanceof OWLInverseObjectPropertiesAxiom)
                inverseObjectPropertiesAxiom.add(axiom);
            if(axiom instanceof OWLSubObjectPropertyOfAxiom)
                subObjectPropertyOfAxiom.add(axiom);
            if(axiom instanceof OWLSubPropertyChainOfAxiom)
                subPropertyChainOfAxiom.add(axiom);
            if(axiom instanceof OWLObjectPropertyCharacteristicAxiom)
                objectPropertyCharacteristicAxiom.add(axiom);

            if(axiom instanceof OWLInverseFunctionalObjectPropertyAxiom)
                inverseFunctionalObjectPropertyAxiom.add(axiom);
            if(axiom instanceof OWLAsymmetricObjectPropertyAxiom)
                asymmetricObjectPropertyAxiom.add(axiom);
            if(axiom instanceof OWLFunctionalObjectPropertyAxiom)
                functionalObjectPropertyAxiom.add(axiom);
            if(axiom instanceof OWLIrreflexiveObjectPropertyAxiom)
                irreflexiveObjectPropertyAxiom.add(axiom);
            if(axiom instanceof OWLReflexiveObjectPropertyAxiom)
                reflexiveObjectPropertyAxiom.add(axiom);
            if(axiom instanceof OWLSymmetricObjectPropertyAxiom)
                symmetricObjectPropertyAxiom.add(axiom);
            if(axiom instanceof OWLTransitiveObjectPropertyAxiom)
                transitiveObjectPropertyAxiom.add(axiom);
            if(axiom instanceof OWLObjectPropertyRangeAxiom)
                objectPropertyRangeAxiom.add(axiom);
            if(axiom instanceof OWLDataPropertyDomainAxiom)
                dataPropertyDomainAxiom.add(axiom);
            if(axiom instanceof OWLObjectPropertyDomainAxiom)
                objectPropertyDomainAxiom.add(axiom);
            if(axiom instanceof OWLFunctionalDataPropertyAxiom)
                functionalDataPropertyAxiom.add(axiom);
            if(axiom instanceof OWLDataPropertyRangeAxiom)
                dataPropertyRangeAxiom.add(axiom);
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
        for(OWLAxiom axiom : this.propertyAxioms){
            if((axiom.getSignature()).contains(entity)){
                    res.add(axiom);
            }
        }
        return res; 
    }

    public String fingerprint(){

        return(propertyAxioms.size() + ","
        + dataPropertyAxioms.size() + ","
        + naryPropertyAxioms.size() + ","
        + subPropertyAxioms.size() + ","
        + unaryPropertyAxioms.size() + ","
        + objectPropertyAxiom.size() + ","
        + dataPropertyCharacteristicAxiom.size() + ","
        + propertyDomainAxiom.size() + ","
        + propertyRangeAxiom.size() + ","
        + disjointDataPropertiesAxiom .size() + ","
        + equivalentDataPropertiesAxiom.size() + ","
        + subDataPropertyOfAxiom.size() + ","
        + disjointObjectPropertiesAxiom.size() + ","
        + equivalentObjectPropertiesAxiom.size() + ","
        + inverseObjectPropertiesAxiom.size() + ","
        + subObjectPropertyOfAxiom.size() + ","
        + subPropertyChainOfAxiom.size()  + ","
        + objectPropertyCharacteristicAxiom.size() + ","
        + inverseFunctionalObjectPropertyAxiom.size() + ","
        + asymmetricObjectPropertyAxiom.size() + ","
        + functionalObjectPropertyAxiom.size() + ","
        + irreflexiveObjectPropertyAxiom.size() + ","
        + reflexiveObjectPropertyAxiom.size() + ","
        + symmetricObjectPropertyAxiom.size() + ","
        + transitiveObjectPropertyAxiom.size() + ","
        + objectPropertyRangeAxiom.size() + ","
        + dataPropertyDomainAxiom.size() + ","
        + objectPropertyDomainAxiom.size() + ","
        + functionalDataPropertyAxiom.size() + ","
        + dataPropertyRangeAxiom.size()); 
    }

    public void print(){
        System.out.println("===Property Axiom Summary===");
        System.out.println("Property axioms " + propertyAxioms.size());
        System.out.println("Data Property Axioms " + dataPropertyAxioms.size());
        System.out.println("Subproperty  " + subPropertyAxioms.size());
        System.out.println("Unary property " + unaryPropertyAxioms.size());
        System.out.println("Nary property " + naryPropertyAxioms.size());
        System.out.println("ObjectProperty " + objectPropertyAxiom.size()); 
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

