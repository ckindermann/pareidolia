package uk.ac.man.cs.detectors.structural.restriction.existential;

import uk.ac.man.cs.detectors.*;
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.detectors.structural.restriction.*;
import uk.ac.man.cs.detectors.structural.intersection.*;

import org.semanticweb.owlapi.model.*;
import java.io.*;
import java.util.Set;
import java.util.*;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Created by chris on 23/11/18.
 */
public class AssertedExistentialDetector extends Detector {

    private OWLDataFactory factory;
    private OWLReasoner reasoner;

    private HashMap<OWLClassExpression, ExistentialRestriction> class2Restriction;

    public AssertedExistentialDetector(OWLOntology o) throws Exception {
        super(o);
        this.reasoner = ReasonerLoader.initReasoner(this.ontology);
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

        this.class2Restriction = new HashMap<>();
    }

    public void detectExistentialRestrictions(){
        this.detectViaDirectSubsumption();
        this.detectViaIntersection();
    }

    public HashMap<OWLClassExpression, ExistentialRestriction> getRestrictions(){
        return this.class2Restriction;
    }

    /**
     *------------------------------
     *| RESTRICTIONS Via Subsumptions|
     *------------------------------
     */

    private void detectViaDirectSubsumption(){
        Set<OWLAxiom> subsumptionAxioms = this.getSubsumptionAxioms();

        for(OWLAxiom a : subsumptionAxioms){
            OWLClassExpression subclass = ((OWLSubClassOfAxiom) a).getSubClass();
            OWLClassExpression superclass = ((OWLSubClassOfAxiom) a).getSuperClass();
            if(isExistentialRestrictionViaSubsumption(subclass, superclass)){
                if(!class2Restriction.containsKey(subclass)){
                    class2Restriction.put(subclass, new ExistentialRestriction(subclass.asOWLClass())); 
                } 
                class2Restriction.get(subclass).add2subsumption(superclass);
            } 
        } 
    } 

    private boolean isExistentialRestrictionViaSubsumption(OWLClassExpression subclass,
            OWLClassExpression superclass){

        boolean isExistentialRestriction = false;

        if((subclass instanceof OWLClass) && (subclass instanceof OWLNamedObject)){
            if(superclass instanceof OWLObjectSomeValuesFrom){
                isExistentialRestriction = true; 
            } 
        }

        return isExistentialRestriction;
    }

    /**
     *--------------------------------
     *| RESTRICTIONS via INTERSECTIONS|
     *--------------------------------
     */

    private void detectViaIntersection(){
        Set<OWLAxiom> subsumptionAxioms = this.getSubsumptionAxioms();
        for(OWLAxiom a : subsumptionAxioms){
            OWLClassExpression subclass = ((OWLSubClassOfAxiom) a).getSubClass();
            OWLClassExpression superclass = ((OWLSubClassOfAxiom) a).getSuperClass();
            if(isExistentialRestrictionViaIntersection(subclass, superclass)){
                if(!class2Restriction.containsKey(subclass)){
                    class2Restriction.put(subclass, new ExistentialRestriction(subclass.asOWLClass()));
                } 
                class2Restriction.get(subclass).add2intersection(new Intersection(a, subclass.asOWLClass(), axiomUsage(a))); 
            }

            //Set<OWLClassExpression> existentialRestrictionsFromIntersections = 
            //    getExistentialRestrictionFromIntersection(subclass, superclass);

            //if(!existentialRestrictionsFromIntersections.isEmpty()){
            //    if(!class2Restriction.containsKey(subclass)){
            //        class2Restriction.put(subclass, new ExistentialRestriction(subclass.asOWLClass())); 
            //    } 
            //    class2Restriction.get(subclass).add2intersection(existentialRestrictionsFromIntersections);
            //} 
        } 
    }

    private boolean isExistentialRestrictionViaIntersection(OWLClassExpression subclass, OWLClassExpression superclass){
        if(isNamedClass(subclass) && isIntersection(superclass)){
            Set<OWLClassExpression> operands = ((OWLObjectIntersectionOf) superclass).getOperands();
            for (OWLClassExpression o : operands){
                if(o instanceof OWLObjectSomeValuesFrom)
                    return true;
            }
        }
        return false;
    }

    private boolean isIntersection(OWLClassExpression e){
            return(e instanceof OWLObjectIntersectionOf); 
    }

    private boolean isNamedClass(OWLClassExpression e){
        return(e instanceof OWLClass) && (e instanceof OWLNamedObject);
    }

    //private Set<OWLClassExpression> getExistentialRestrictionFromIntersection(OWLClassExpression subclass,
    //        OWLClassExpression superclass){
    //    Set<OWLClassExpression> restrictions = new HashSet<>();
    //    if((subclass instanceof OWLClass) && (subclass instanceof OWLNamedObject)){
    //        if(superclass instanceof OWLObjectIntersectionOf){
    //            Set<OWLClassExpression> operands = ((OWLObjectIntersectionOf) superclass).getOperands();
    //            for (OWLClassExpression o : operands){
    //                if(o instanceof OWLObjectSomeValuesFrom)
    //                    restrictions.add(o); 
    //            }
    //        }
    //    }
    //    return restrictions; 
    //}


    /**
     *------------------------------------
     *|Filters for more than 1 restriction |
     *------------------------------------
     */

    private void add2map(OWLClassExpression namedClass,
            Set<OWLClassExpression> restrictions, 
            HashMap<OWLClassExpression, Set<OWLClassExpression>> map){

            if(!restrictions.isEmpty()){
                if(map.containsKey(namedClass)){
                    map.get(namedClass).addAll(restrictions); 
                } else {
                    map.put(namedClass, restrictions); 
                }
            } 
    }


    private void add2map(OWLClassExpression namedClass,
            OWLClassExpression restriction, 
            HashMap<OWLClassExpression, Set<OWLClassExpression>> map){

        Set<OWLClassExpression> auxiliarySet = new HashSet<>(); 
        auxiliarySet.add(restriction);
        add2map(namedClass, auxiliarySet, map); 
    } 

    /** Implement abstract classes from Detector */
    public void reset(){
        ;
    }

    public void setOntology(OWLOntology o){
        this.reset();
        this.ontology = o; 
    }

    public void run(){ 
        ; 
    }

    public void write(String destFile){ 
        ;
    }
}
