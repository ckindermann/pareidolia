package uk.ac.man.cs.detectors.structural.restriction;

import uk.ac.man.cs.detectors.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.ont.*;
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
public class AssertedRestrictionDetector extends Detector {

    private OWLDataFactory factory;
    private OWLReasoner reasoner;

    private HashMap<OWLClassExpression, Restriction> class2Restriction;

    public AssertedRestrictionDetector(OWLOntology o) throws Exception {
        super(o);
        this.reasoner = ReasonerLoader.initReasoner(this.ontology);
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

        this.class2Restriction = new HashMap<>();
    }

    public void detectRestrictions(){
        this.detectViaDirectSubsumption();
        this.detectViaIntersection();
    }

    public HashMap<OWLClassExpression, Restriction> getRestrictions(){
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
            if(isRestrictionViaSubsumption(subclass, superclass)){
                if(!class2Restriction.containsKey(subclass)){
                    class2Restriction.put(subclass, new Restriction(subclass.asOWLClass())); 
                } 
                class2Restriction.get(subclass).add2subsumption(superclass);
            } 
        } 
    } 

    private boolean isRestrictionViaSubsumption(OWLClassExpression subclass,
            OWLClassExpression superclass){

        boolean isRestriction = false;

        if((subclass instanceof OWLClass) && (subclass instanceof OWLNamedObject)){
            //if(superclass instanceof OWLObjectRestriction){
            if(superclass instanceof OWLRestriction){
                isRestriction = true; 
            } 
        }

        return isRestriction;
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
            if(isRestrictionViaIntersection(subclass, superclass)){
                if(!class2Restriction.containsKey(subclass)){
                    class2Restriction.put(subclass, new Restriction(subclass.asOWLClass()));
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

    private boolean isRestrictionViaIntersection(OWLClassExpression subclass, OWLClassExpression superclass){
        if(isNamedClass(subclass) && isIntersection(superclass)){
            Set<OWLClassExpression> operands = ((OWLObjectIntersectionOf) superclass).getOperands();
            for (OWLClassExpression o : operands){
                //if(o instanceof OWLObjectRestriction)
                if(o instanceof OWLRestriction)
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

    //private HashMap<OWLClassExpression, Restriction> class2Restriction;

    public void write(String output){
        Iterator it = this.class2Restriction.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            OWLClassExpression s = (OWLClassExpression) pair.getKey();
            Restriction restriction = this.class2Restriction.get(s); 
            Set<OWLClassExpression> matches = restriction.getAll();
            if(matches.size() > 1)
                IOHelper.writeAppend(s.toString(), output); 
            it.remove(); // avoids a ConcurrentModificationException
        }
    } 


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

}
