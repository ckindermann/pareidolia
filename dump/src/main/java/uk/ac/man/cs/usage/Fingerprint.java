package uk.ac.man.cs.usage;

import uk.ac.man.cs.codp.*;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.stats.*;
import uk.ac.man.cs.data.*;
import uk.ac.man.cs.util.*;
import java.io.*;
import java.util.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.HermiT.ReasonerFactory;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;

        //long starTime = System.nanoTime();
        //long endTime = System.nanoTime();
        //double duration = (endTime - starTime) / 1000000000.0;
        //System.out.println("Timing Cartesian: " + duration);
/**
 * Created by chris on 03/09/18.
 */

//this is not a check, this is an analysis!
public class Fingerprint {

    public OWLEntity entity; 
    public int equivalent;

    public Set<OWLAxiom> transitiveObjectProperty;
    public Set<OWLAxiom> functionalObjectProperty;
    public Set<OWLAxiom> sameIndividual;
    public Set<OWLAxiom> reflexiveObjectProperty;
    public Set<OWLAxiom> subObjectPropertyOf;
    public Set<OWLAxiom> symmetricObjectProperty;
    public Set<OWLAxiom> subDataPropertyOf;
    public Set<OWLAxiom> dataPropertyRange;
    public Set<OWLAxiom> disjointUnion;
    public Set<OWLAxiom> subClassOf;
    public Set<OWLAxiom> objectPropertyRange;
    public Set<OWLAxiom> dataPropertyAssertion;
    public Set<OWLAxiom> disjointObjectProperties;
    public Set<OWLAxiom> equivalentObjectProperties;
    public Set<OWLAxiom> disjointClasses;
    public Set<OWLAxiom> dataPropertyDomain;
    public Set<OWLAxiom> functionalDataProperty;
    public Set<OWLAxiom> inverseObjectProperties;
    public Set<OWLAxiom> inverseFunctionalObjectProperty;
    public Set<OWLAxiom> irrefexiveObjectProperty;
    public Set<OWLAxiom> objectPropertyAssertion;
    public Set<OWLAxiom> equivalentClasses;
    public Set<OWLAxiom> datatypeDefinition;
    public Set<OWLAxiom> disjointDataProperties;
    public Set<OWLAxiom> subPropertyChainOf;
    public Set<OWLAxiom> classAssertion;
    public Set<OWLAxiom> hasKey;
    public Set<OWLAxiom> negativeObjectPropertyAssertion;
    public Set<OWLAxiom> asymmetricObjectProperty;
    public Set<OWLAxiom> negativeDataPropertyAssertion;
    public Set<OWLAxiom> objectPropertyDomain;
    public Set<OWLAxiom> equivalentDataProperties;
    public Set<OWLAxiom> rule;
    public Set<OWLAxiom> differentIndividuals; 

    public Set<OWLAxiom> axiomUsage;
    public Set<OWLEntity> similar;

    public Fingerprint(OWLEntity e, OWLOntology o){

        this.entity = e;

        //initialise sets
        transitiveObjectProperty = new HashSet<>();
        functionalObjectProperty = new HashSet<>();
        sameIndividual = new HashSet<>();
        reflexiveObjectProperty = new HashSet<>();
        subObjectPropertyOf = new HashSet<>();
        symmetricObjectProperty = new HashSet<>();
        subDataPropertyOf = new HashSet<>();
        dataPropertyRange = new HashSet<>();
        disjointUnion = new HashSet<>();
        subClassOf = new HashSet<>();
        objectPropertyRange = new HashSet<>();

        dataPropertyAssertion = new HashSet<>();
        disjointObjectProperties = new HashSet<>();
        equivalentObjectProperties = new HashSet<>();

        disjointClasses = new HashSet<>();
        dataPropertyDomain = new HashSet<>();
        functionalDataProperty = new HashSet<>();
        inverseObjectProperties = new HashSet<>();
        inverseFunctionalObjectProperty = new HashSet<>();
        irrefexiveObjectProperty = new HashSet<>();
        objectPropertyAssertion = new HashSet<>();
        equivalentClasses = new HashSet<>();
        datatypeDefinition = new HashSet<>();
        disjointDataProperties = new HashSet<>();
        subPropertyChainOf = new HashSet<>();
        classAssertion = new HashSet<>();
        hasKey = new HashSet<>();
        negativeObjectPropertyAssertion = new HashSet<>();
        asymmetricObjectProperty = new HashSet<>();
        negativeDataPropertyAssertion = new HashSet<>();
        objectPropertyDomain = new HashSet<>();
        equivalentDataProperties = new HashSet<>();
        rule = new HashSet<>();
        differentIndividuals = new HashSet<>(); 

        axiomUsage = new HashSet<>(); 
        similar = new HashSet<>(); 

        usage(o);

        equivalent = 0;
    } 
    
    private void usage(OWLOntology ontology){

        //set of axioms containing the pattern
        Set<OWLAxiom> entityUsage = new HashSet<>();

        for(OWLAxiom axiom : ontology.getAxioms(Imports.EXCLUDED)){ 
            if((axiom.getSignature()).contains(this.entity)){

                axiomUsage.add(axiom);

                AxiomType<?> t = axiom.getAxiomType();

                if(t == AxiomType.TRANSITIVE_OBJECT_PROPERTY) 
                    this.transitiveObjectProperty.add(axiom);
                if(t == AxiomType.FUNCTIONAL_OBJECT_PROPERTY) 
                    this.functionalObjectProperty.add(axiom);
                if(t == AxiomType.SAME_INDIVIDUAL) 
                    this.sameIndividual.add(axiom);
                if(t == AxiomType.REFLEXIVE_OBJECT_PROPERTY) 
                    this.reflexiveObjectProperty.add(axiom);
                if(t == AxiomType.SUB_OBJECT_PROPERTY) 
                    this.subObjectPropertyOf.add(axiom);
                if(t == AxiomType.SYMMETRIC_OBJECT_PROPERTY) 
                    this.symmetricObjectProperty.add(axiom);
                if(t == AxiomType.DATA_PROPERTY_RANGE) 
                    this.dataPropertyRange.add(axiom);
                if(t == AxiomType.DISJOINT_UNION)
                    this.disjointUnion.add(axiom);
                if(t == AxiomType.SUBCLASS_OF)
                    this.subClassOf.add(axiom);
                if(t == AxiomType.OBJECT_PROPERTY_RANGE)
                    this.objectPropertyRange.add(axiom);
                if(t == AxiomType.DATA_PROPERTY_ASSERTION)
                    this.dataPropertyAssertion.add(axiom);
                if(t == AxiomType.DISJOINT_OBJECT_PROPERTIES)
                    this.disjointObjectProperties.add(axiom);
                if(t == AxiomType.EQUIVALENT_OBJECT_PROPERTIES)
                    this.equivalentObjectProperties.add(axiom);
                if(t == AxiomType.DISJOINT_CLASSES)
                    this.disjointClasses.add(axiom);
                if(t == AxiomType.DATA_PROPERTY_DOMAIN)
                    this.dataPropertyDomain.add(axiom);
                if(t == AxiomType.FUNCTIONAL_DATA_PROPERTY)
                    this.functionalDataProperty.add(axiom);
                if(t == AxiomType.INVERSE_OBJECT_PROPERTIES)
                    this.inverseObjectProperties.add(axiom);
                if(t == AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY)
                    this.inverseFunctionalObjectProperty.add(axiom);
                if(t == AxiomType.IRREFLEXIVE_OBJECT_PROPERTY)
                    this.irrefexiveObjectProperty.add(axiom);
                if(t == AxiomType.OBJECT_PROPERTY_ASSERTION)
                    this.objectPropertyAssertion.add(axiom);
                if(t == AxiomType.EQUIVALENT_CLASSES)
                    this.equivalentClasses.add(axiom);
                if(t == AxiomType.DATATYPE_DEFINITION)
                    this.datatypeDefinition.add(axiom);
                if(t == AxiomType.DISJOINT_DATA_PROPERTIES)
                    this.disjointDataProperties.add(axiom);
                if(t == AxiomType.SUB_PROPERTY_CHAIN_OF)
                    this.subPropertyChainOf.add(axiom);
                if(t == AxiomType.CLASS_ASSERTION)
                    this.classAssertion.add(axiom);
                if(t == AxiomType.HAS_KEY)
                    this.hasKey.add(axiom);
                if(t == AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION)
                    this.negativeObjectPropertyAssertion.add(axiom);
                if(t == AxiomType.ASYMMETRIC_OBJECT_PROPERTY)
                    this.asymmetricObjectProperty.add(axiom);
                if(t == AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION)
                    this.negativeDataPropertyAssertion.add(axiom);
                if(t == AxiomType.OBJECT_PROPERTY_DOMAIN)
                    this.objectPropertyDomain.add(axiom);
                if(t == AxiomType.EQUIVALENT_DATA_PROPERTIES)
                    this.equivalentDataProperties.add(axiom);
                if(t == AxiomType.SWRL_RULE)
                    this.rule.add(axiom);
                if(t == AxiomType.DIFFERENT_INDIVIDUALS)
                    this.differentIndividuals.add(axiom);
            }
        }
    } 

    public boolean equals2(Fingerprint fp){

        return ((this.transitiveObjectProperty.size() == fp.transitiveObjectProperty.size()) &&
    (this.functionalObjectProperty.size() == fp.functionalObjectProperty.size()) &&
    (this.sameIndividual.size() == fp.sameIndividual.size()) &&
    (this.reflexiveObjectProperty.size() == fp.reflexiveObjectProperty.size())&&
    (this.subObjectPropertyOf.size() == fp.subObjectPropertyOf.size())&&
    (this.symmetricObjectProperty.size() == fp.symmetricObjectProperty.size())&&
    (this.subDataPropertyOf.size() == fp.subDataPropertyOf.size())&&
    (this.dataPropertyRange.size() == fp.dataPropertyRange.size())&&
    (this.disjointUnion.size() == fp.disjointUnion.size())&&
    (this.subClassOf.size() == fp.subClassOf.size())&&
    (this.objectPropertyRange.size() == fp.objectPropertyRange.size())&&
    (this.dataPropertyAssertion.size() == fp.dataPropertyAssertion.size())&&
    (this.disjointObjectProperties.size() == fp.disjointObjectProperties.size())&&
    (this.equivalentObjectProperties.size() == fp.equivalentObjectProperties.size())&&
    (this.disjointClasses.size() == fp.disjointClasses.size())&&
    (this.dataPropertyDomain.size() == fp.dataPropertyDomain.size())&&
    (this.functionalDataProperty.size() == fp.functionalDataProperty.size())&&
    (this.inverseObjectProperties.size() == fp.inverseObjectProperties.size())&&
    (this.inverseFunctionalObjectProperty.size() == fp.inverseFunctionalObjectProperty.size())&&
    (this.irrefexiveObjectProperty.size() == fp.irrefexiveObjectProperty.size())&&
    (this.objectPropertyAssertion.size() == fp.objectPropertyAssertion.size())&&
    (this.equivalentClasses.size() == fp.equivalentClasses.size())&&
    (this.datatypeDefinition.size() == fp.datatypeDefinition.size())&&
    (this.disjointDataProperties.size() == fp.disjointDataProperties.size())&&
    (this.subPropertyChainOf.size() == fp.subPropertyChainOf.size())&&
    (this.classAssertion.size() == fp.classAssertion.size())&&
    (this.hasKey.size() == fp.hasKey.size())&&
    (this.negativeObjectPropertyAssertion.size() == fp.negativeObjectPropertyAssertion.size())&&
    (this.asymmetricObjectProperty.size() == fp.asymmetricObjectProperty.size())&&
    (this.negativeDataPropertyAssertion.size() == fp.negativeDataPropertyAssertion.size())&&
    (this.objectPropertyDomain.size() == fp.objectPropertyDomain.size())&&
    (this.equivalentDataProperties.size() == fp.equivalentDataProperties.size())&&
    (this.rule.size() == fp.rule.size())&&
    (this.differentIndividuals.size() ==  fp.differentIndividuals.size()));

    }

    public void print(){
    System.out.println("FINGERPRINT");
    System.out.println("Entity: " + this.entity.getIRI().toString());
    System.out.println("Usage: " + this.axiomUsage);
    System.out.println("Similar: " + this.similar);
     System.out.println("1 " + transitiveObjectProperty.size());
     System.out.println("2 " + functionalObjectProperty.size());
     System.out.println("3 " + sameIndividual.size());
     System.out.println("4 " + reflexiveObjectProperty.size());
     System.out.println("5 " + subObjectPropertyOf.size());
     System.out.println("6 " + symmetricObjectProperty.size());
     System.out.println("7 " + subDataPropertyOf.size());
     System.out.println("8 " + dataPropertyRange.size());
     System.out.println("9 " + disjointUnion.size());
     System.out.println("10 " + subClassOf.size());
     System.out.println("11 " + objectPropertyRange.size());
     System.out.println("12 " + dataPropertyAssertion.size());
     System.out.println("13 " + disjointObjectProperties.size());
     System.out.println("14 " + equivalentObjectProperties.size());
     System.out.println("15 " + disjointClasses.size());
     System.out.println("16 " + dataPropertyDomain.size());
     System.out.println("17 " + functionalDataProperty.size());
     System.out.println("18 " + inverseObjectProperties.size());
     System.out.println("19 " + inverseFunctionalObjectProperty.size());
     System.out.println("20 " + irrefexiveObjectProperty.size());
     System.out.println("21 " + objectPropertyAssertion.size());
     System.out.println("22 " + equivalentClasses.size());
     System.out.println("23 " + datatypeDefinition.size());
     System.out.println("24 " + disjointDataProperties.size());
     System.out.println("25 " + subPropertyChainOf.size());
     System.out.println("26 " + classAssertion.size());
     System.out.println("27 " + hasKey.size());
     System.out.println("28 " + negativeObjectPropertyAssertion.size());
     System.out.println("29 " + asymmetricObjectProperty.size());
     System.out.println("30 " + negativeDataPropertyAssertion.size());
     System.out.println("31 " + objectPropertyDomain.size());
     System.out.println("32 " + equivalentDataProperties.size());
     System.out.println("33 " + rule.size());
     System.out.println("34 " + differentIndividuals.size()); 
    }
}

