package uk.ac.man.cs.lodp2;

import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.ont.*;

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
public class NaryRelationFinder {

    private OWLOntology ontology;
    private OWLDataFactory factory;
    private OWLReasoner reasoner;

    //private Set<NaryRelation> naryRelations;
    private Set<NaryRelation> naryRelationsViaIntersection;
    private Set<NaryRelation> naryRelationsViaSubsumptions; 

    public NaryRelationFinder(OWLOntology o) throws Exception {
        this.ontology = o;
        this.reasoner = ReasonerLoader.initReasoner(this.ontology);
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

        //this.naryRelationsViaSubsumptions = new HashMap<>();

        //this.naryRelations = new HashSet<>(); 
        this.naryRelationsViaIntersection = new HashSet<>();
        this.naryRelationsViaSubsumptions = new HashSet<>();

        this.getNaryRelations();
        this.getIntersectionsOfRestrictions();
    }

    public Set<NaryRelation> getNaryRelationsViaSubsumptions(){
        return this.naryRelationsViaSubsumptions; 
    }

    public Set<NaryRelation> getNaryRelationsViaIntersections(){
        return this.naryRelationsViaIntersection; 
    }

    private Set<OWLAxiom> getSubsumptionAxioms(){ 
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


    private void getIntersectionsOfRestrictions(){ 
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);
        Set<OWLAxiom> equivAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.EQUIVALENT_CLASSES);

        for(OWLAxiom a : equivAxioms){

            List<OWLClassExpression> classExpressions = (((OWLEquivalentClassesAxiom) a).getClassExpressionsAsList());
            Iterator<OWLClassExpression> it = classExpressions.iterator(); 
            OWLClassExpression LHS = it.next();
            OWLClassExpression RHS = it.next();
            isIntersectionOfRestrictions(LHS, RHS);
            isIntersectionOfRestrictions(RHS, LHS); 
        } 
    } 


    private boolean isIntersectionOfRestrictions(OWLClassExpression cl, OWLClassExpression i){ 
        if(i instanceof OWLObjectIntersectionOf){
            Set<OWLClassExpression> operands = ((OWLObjectIntersectionOf) i).getOperands();
            Set<OWLClassExpression> restrictions = new HashSet<>();
            for (OWLClassExpression o : operands){
                if(o instanceof OWLRestriction){
                    restrictions.add(o); 
                } 
            }
            if(restrictions.size() > 1){
                naryRelationsViaIntersection.add(new NaryRelation(cl, restrictions));
                return true;
            }
        }
        return false;
    }

    private void getNaryRelations(){

        Set<OWLAxiom> subsumptionAxioms = this.getSubsumptionAxioms();

        HashMap<OWLClassExpression, Set<OWLClassExpression>> class2restrictionMap = new HashMap<>();

        for(OWLAxiom a : subsumptionAxioms){

            OWLClassExpression subclass = ((OWLSubClassOfAxiom) a).getSubClass();
            OWLClassExpression superclass = ((OWLSubClassOfAxiom) a).getSuperClass();

            Set<OWLClassExpression> restrictions = this.getRestrictions(subclass, superclass);

            if(!restrictions.isEmpty()){
                if(class2restrictionMap.containsKey(subclass)){
                    class2restrictionMap.get(subclass).addAll(restrictions); 
                } else {
                    class2restrictionMap.put(subclass, restrictions); 
                }
            }
        } 
        this.filterNaryRelations(class2restrictionMap);
    }

    private void filterNaryRelations(HashMap<OWLClassExpression, Set<OWLClassExpression>> class2restrictionMap){
        Iterator it = class2restrictionMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Set<OWLClassExpression> restrictions = (Set<OWLClassExpression>) pair.getValue();
            OWLClassExpression relation = (OWLClassExpression) pair.getKey();

            if(restrictions.size() > 1){
                NaryRelation nary = new NaryRelation(relation, restrictions);
                naryRelationsViaSubsumptions.add(nary); 
            }
        } 
    } 

    private Set<OWLClassExpression> getRestrictions(OWLClassExpression subclass,
                                                    OWLClassExpression superclass){
            Set<OWLClassExpression> restrictions = new HashSet<>();

            if((subclass instanceof OWLClass) && (subclass instanceof OWLNamedObject)){ 
                for(OWLClassExpression c : superclass.getNestedClassExpressions()){
                    if(c instanceof OWLRestriction){
                        restrictions.add(c);
                    }
                }
            }
            return restrictions; 
    }
}
