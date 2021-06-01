package uk.ac.man.cs.lodp;

import uk.ac.man.cs.util.Pair;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.OWLNaryClassAxiom;
import java.io.*;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Created by chris on 08/08/18.
 */
public class NaryRelation extends LODP {

    private String description;
    private Set<OWLAxiom> equivalentToIntersectionAxioms;
    private HashMap<OWLClassExpression, Set<OWLClassExpression>> class2restrictions;

    //according to 
    //http://ontologydesignpatterns.org/wiki/Submissions:N-Ary_Relation_Pattern_%28OWL_2%29
    public NaryRelation(OWLOntology o){
        this.ontology = o; 
        this.evidence = new HashSet<>(); 
        this.equivalentToIntersectionAxioms = new HashSet<>();
        this.class2restrictions = new HashMap<>();
        this.description = "The pattern 'Nary-Relation' " +
            "consists of a named intersection of existential restrictions." +
            "Each existential restriction corresponds to one attribute of the n-ary relation.";
    }

    public void computeEvidence(){
        this.reset();
        this.computeIntersectionOfExistentials(); 
        this.computeClassWithMultipleRestrictions();

        for(OWLAxiom a : this.equivalentToIntersectionAxioms){
            int numberOfRestrictions = 0;
            for(OWLClassExpression c : a.getNestedClassExpressions()){
                if(c instanceof OWLRestriction){
                    numberOfRestrictions++; 
                } 
            }
            if(numberOfRestrictions > 1){
                Set<OWLAxiom> NaryInstance = new HashSet<>();
                NaryInstance.add(a);
                this.evidence.add(patternInstance("Equivalent Class to Intersection with " + Integer.toString(numberOfRestrictions) + " restrictions", NaryInstance));
            }
        }

    }

    //computes the set of all axioms with the following properties
    //-equivalency axioms
    //-one top level class expression is an intersection of classes
    //-the operands of the intersection are all existential restrictions
    private void computeIntersectionOfExistentials(){
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.INCLUDED);
        Set<OWLAxiom> equivAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.EQUIVALENT_CLASSES);

        for(OWLAxiom a : equivAxioms){
            OWLEquivalentClassesAxiom b = (OWLEquivalentClassesAxiom) a;
            for(OWLClassExpression c : b.getClassExpressions()){
                if(c instanceof OWLObjectIntersectionOf){
                   Set<OWLClassExpression> operands = ((OWLObjectIntersectionOf) c).getOperands();
                   boolean check = true;
                   for (OWLClassExpression o : operands){
                       if(!(o instanceof OWLObjectSomeValuesFrom)){
                           check = false; 
                       } 
                   }
                   if(check){
                       this.equivalentToIntersectionAxioms.add(a); 
                   }
                }
            }
        }
    }

    private void computeClassWithMultipleRestrictions(){
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.INCLUDED);
        Set<OWLAxiom> equivAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.EQUIVALENT_CLASSES);
        Set<OWLAxiom> subClassAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.SUBCLASS_OF);

        Set<OWLAxiom> subsumptionAxioms = new HashSet<>();
        subsumptionAxioms.addAll(subClassAxioms);
        for(OWLAxiom a : equivAxioms){
            subsumptionAxioms.addAll(((OWLEquivalentClassesAxiom) a).asOWLSubClassOfAxioms()); 
        } 

        //HashMap<OWLClassExpression, Set<OWLClassExpression>> class2restrictions = new HashMap<>();

        for(OWLAxiom a : subsumptionAxioms){
            OWLClassExpression subclass = ((OWLSubClassOfAxiom) a).getSubClass();
            OWLClassExpression superclass = ((OWLSubClassOfAxiom) a).getSuperClass();
            Set<OWLClassExpression> restrictions = new HashSet<>();

            int numberOfRestrictions = 0;

            if((subclass instanceof OWLClass) && (subclass instanceof OWLNamedObject)){
                
                for(OWLClassExpression c : superclass.getNestedClassExpressions()){
                    if(c instanceof OWLRestriction){
                        restrictions.add(c);
                    } 
                }
                    //Set<OWLAxiom> NaryInstance = new HashSet<>();
                    //NaryInstance.add(a);
                    //this.evidence.add(patternInstance("Class with " + Integer.toString(numberOfRestrictions) + " restrictions", NaryInstance));
            }

            if(!restrictions.isEmpty()){
                if(this.class2restrictions.containsKey(subclass)){
                    this.class2restrictions.get(subclass).addAll(restrictions); 
                } else {
                    this.class2restrictions.put(subclass, restrictions); 
                }
            }
        } 
    }

    public Set<Pair<String, Set<OWLAxiom>>> getEvidence(){
        return this.evidence;

    }

    public boolean writeEvidence(String destFile){

        createFolder(destFile); 
        String evidenceFile = destFile + "/evidence"; 
        
        if(!this.evidence.isEmpty()){
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(evidenceFile));
                for(Pair<String, Set<OWLAxiom>> p : this.evidence){
                    bw.write(p.getFirst() + " : " + p.getSecond().toString());
                    bw.newLine();
                }
                bw.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }


        String restrictionFile = destFile + "/class2restriction"; 
        if(!this.class2restrictions.isEmpty()){
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(restrictionFile));
                for(OWLClassExpression e : this.class2restrictions.keySet()){
                    if(this.class2restrictions.get(e).size() > 1){
                        bw.write(e.toString() + " : " + this.class2restrictions.get(e).toString());
                        bw.newLine();
                    }
                }
                bw.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            } 
        } 
    
        /*

        String intersectionFile = destFile + "/intersections"; 

        if(!this.equivalentToIntersectionAxioms.isEmpty()){
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(intersectionFile));
                for(OWLAxiom a : this.equivalentToIntersectionAxioms){
                    bw.write(a.toString());
                    bw.newLine();
                }
                bw.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        */

        return true;
    }

    public void printDescription(){
        ; 
    }

    public void setOntology(OWLOntology o){
        this.ontology = o;
        this.reset(); 
    }

    public void reset(){
        this.evidence.clear();
        this.equivalentToIntersectionAxioms.clear(); 
    }

    private void createFolder(String path){
        File destDir = new File(path);
        destDir.mkdirs(); 
    }











}
