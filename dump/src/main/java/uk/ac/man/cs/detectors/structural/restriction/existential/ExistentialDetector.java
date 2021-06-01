package uk.ac.man.cs.detectors.structural.restriction.existential;

import uk.ac.man.cs.detectors.*;
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.detectors.structural.restriction.*;

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
public class ExistentialDetector extends Detector {

    private OWLDataFactory factory;
    private OWLReasoner reasoner;

    //private HashMap<OWLClassExpression, Set<OWLClassExpression>> class2Restriction;
    private HashMap<OWLClassExpression, ExistentialRestriction> class2Restriction;

    //private HashMap<OWLClassExpression, Set<OWLClassExpression>> class2directRestriction;
    //private HashMap<OWLClassExpression, Set<OWLClassExpression>> class2intersectionOfRestriction;
    //private HashMap<OWLClassExpression, Set<OWLClassExpression>> class2indirectRestriction;

    //private HashMap<OWLClassExpression, Set<OWLClassExpression>> class2MultipleDirectRestrictions;
    //private HashMap<OWLClassExpression, Set<OWLClassExpression>> class2MultipleIndirectRestriction;

    public ExistentialDetector(OWLOntology o) throws Exception {
        super(o);
        this.reasoner = ReasonerLoader.initReasoner(this.ontology);
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();


        this.class2Restriction = new HashMap<>();
        //this.class2directRestriction= new HashMap<>();
        //this.class2indirectRestriction = new HashMap<>();
    }

    public void detectExistentialRestrictions(){
        this.detectDirectRestrictions();
        this.detectIndirectRestriction();
        this.detectIntersectionOfRestriction();
        //this.detectIndirectRestrictions(); 
    }

    public HashMap<OWLClassExpression, ExistentialRestriction> getRestrictions(){
        return this.class2Restriction;
    }

    /**
     *---------------------
     *| DIRECT RESTRICTIONS|
     *---------------------
     */

    private void detectDirectRestrictions(){
        Set<OWLAxiom> subsumptionAxioms = this.getSubsumptionAxioms();

        for(OWLAxiom a : subsumptionAxioms){
            OWLClassExpression subclass = ((OWLSubClassOfAxiom) a).getSubClass();
            OWLClassExpression superclass = ((OWLSubClassOfAxiom) a).getSuperClass();
            if(isDirectExistentialRestriction(subclass, superclass)){
                if(!class2Restriction.containsKey(subclass)){
                    //class2Restriction.put(subclass, new ExistentialRestriction(subclass.asOWLClass())); 
                } 
                //class2Restriction.get(subclass).add2direct(superclass);
                //add2map(subclass, superclass, class2directRestriction); 
            } 
        } 
    } 

    private boolean isDirectExistentialRestriction(OWLClassExpression subclass,
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

    private void detectIntersectionOfRestriction(){
        Set<OWLAxiom> subsumptionAxioms = this.getSubsumptionAxioms();
        for(OWLAxiom a : subsumptionAxioms){
            OWLClassExpression subclass = ((OWLSubClassOfAxiom) a).getSubClass();
            OWLClassExpression superclass = ((OWLSubClassOfAxiom) a).getSuperClass();

            Set<OWLClassExpression> existentialRestrictionsFromIntersections = 
                getExistentialRestrictionFromIntersection(subclass, superclass);

            if(!existentialRestrictionsFromIntersections.isEmpty()){
                if(!class2Restriction.containsKey(subclass)){
                    //class2Restriction.put(subclass, new ExistentialRestriction(subclass.asOWLClass())); 
                } 
                //class2Restriction.get(subclass).add2intersection(existentialRestrictionsFromIntersections);
            }

            //add2map(subclass,
            //        existentialRestrictionsFromIntersections,
            //        class2intersectionOfRestriction); 
        } 
    }

    private Set<OWLClassExpression> getExistentialRestrictionFromIntersection(OWLClassExpression subclass,
            OWLClassExpression superclass){
        Set<OWLClassExpression> restrictions = new HashSet<>();
        if((subclass instanceof OWLClass) && (subclass instanceof OWLNamedObject)){
            if(superclass instanceof OWLObjectIntersectionOf){
                Set<OWLClassExpression> operands = ((OWLObjectIntersectionOf) superclass).getOperands();
                for (OWLClassExpression o : operands){
                    if(o instanceof OWLObjectSomeValuesFrom)
                        restrictions.add(o); 
                }
            }
        }
        return restrictions; 
    }

    /**
     *-----------------------
     *|INDIRECT RESTRICTIONS |
     *-----------------------
     */
    private void detectIndirectRestriction(){
        Set<OWLAxiom> subsumptionAxioms = this.getSubsumptionAxioms();
        for(OWLAxiom a : subsumptionAxioms){
            OWLClassExpression subclass = ((OWLSubClassOfAxiom) a).getSubClass();
            OWLClassExpression superclass = ((OWLSubClassOfAxiom) a).getSuperClass();

            Set<OWLClassExpression> indirectExistentialRestrictions = 
                getIndirectExistentialRestriction(subclass, superclass);

            if(!indirectExistentialRestrictions.isEmpty()){
                if(!class2Restriction.containsKey(subclass)){
                    //class2Restriction.put(subclass, new ExistentialRestriction(subclass.asOWLClass())); 
                } 
                //class2Restriction.get(subclass).add2indirect(indirectExistentialRestrictions);
            }

            //add2map(subclass, indirectExistentialRestrictions, class2indirectRestriction); 
        } 
    }

    private Set<OWLClassExpression> getIndirectExistentialRestriction(OWLClassExpression subclass,
            OWLClassExpression superclass){

        Set<OWLClassExpression> restrictions = new HashSet<>();

        if((subclass instanceof OWLClass) && (subclass instanceof OWLNamedObject)){ 
            for(OWLClassExpression c : superclass.getNestedClassExpressions()){
                if(c instanceof OWLObjectSomeValuesFrom){
                    restrictions.add(c);
                }
            }
        }
        return restrictions; 
    }

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
