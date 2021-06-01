package uk.ac.man.cs.ghader;

import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;

import org.semanticweb.owlapi.model.*;
import java.io.*;
import java.util.Set;
import java.util.*;
import java.util.stream.*;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import info.debatty.java.stringsimilarity.*;

import org.semanticweb.owlapi.search.EntitySearcher;
import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

/**
 * Created by chris on 23/11/18.
 */
public class TripleDetector {

    private OWLOntology ontology;
    private EntityTriple triple; 
    private OWLReasoner reasoner;
    private OWLDataFactory factory;

    private Set<Restriction> assertedRestrictions;
    private Set<Restriction> entailedRestrictions;

    private boolean entailments;

    public TripleDetector(OWLOntology o, EntityTriple t) throws Exception {
        this.ontology = o;
        this.triple = t; 
        this.reasoner = ReasonerLoader.initReasoner(this.ontology);
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

        this.assertedRestrictions = new HashSet<>();
        this.entailedRestrictions = new HashSet<>();

        entailments = true;
    }

    public TripleDetector(OWLOntology o) throws Exception {
        this.ontology = o;
        this.reasoner = ReasonerLoader.initReasoner(this.ontology);
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

        this.assertedRestrictions = new HashSet<>();
        this.entailedRestrictions = new HashSet<>();

        entailments = true; 
    }

    public void setTriple(EntityTriple t){
        this.assertedRestrictions.clear();
        this.entailedRestrictions.clear();
        this.triple = t; 
    }

    public Set<Restriction> getAsserted(){
        return this.assertedRestrictions; 
    }

    public Set<Restriction> getEntailed(){
        return this.entailedRestrictions; 
    }

    public void run(){
        //get subject matches
        Set<ClassMatch> subMatches = triple.getSubject().getMatches();
        //get object matches
        Set<ClassMatch> objMatches = triple.getObject().getMatches();
        //get property matches
        Set<RelationMatch>  relMatches = triple.getRelation().getMatches();

        for(ClassMatch subject : subMatches){
            OWLClass s = subject.getOWLClass();
            for(ClassMatch object : objMatches){
                OWLClass o = object.getOWLClass();
                for(RelationMatch relation : relMatches){
                    OWLObjectPropertyExpression p = relation.getProperty();
                    checkRestrictions(s, p, o); 
                }
            } 
        }

    }

    private void checkRestrictions(OWLClass A, OWLObjectPropertyExpression p, OWLClass B){
        checkExistentialRestriction(A,p,B);
        //checkExistentialRestriction(B,p,A);
        checkUniversalRestriction(A,p,B);
        //checkUniversalRestriction(B,p,A);
        checkMinCardinalityRestriction(A,p,B);
        //checkMinCardinalityRestriction(B,p,A);
        checkMaxCardinalityRestriction(A,p,B);
        //checkMaxCardinalityRestriction(B,p,A); 
    }

    private void checkExistentialRestriction(OWLClass A, OWLObjectPropertyExpression p, OWLClass B){
        OWLClassExpression some_p_B = factory.getOWLObjectSomeValuesFrom(p,B);
        OWLSubClassOfAxiom A_subclass_some_p_B = factory.getOWLSubClassOfAxiom(A,some_p_B);
        OWLSubClassOfAxiom some_p_B_subclass_A = factory.getOWLSubClassOfAxiom(some_p_B,A);

        if(ontology.containsAxiom(A_subclass_some_p_B))
            this.assertedRestrictions.add(new Restriction(A, B, A_subclass_some_p_B));
        if(ontology.containsAxiom(some_p_B_subclass_A))
            this.assertedRestrictions.add(new Restriction(A, B, some_p_B_subclass_A));

        if(entailments){
            if(reasoner.isEntailed(A_subclass_some_p_B))
                this.entailedRestrictions.add(new Restriction(A, B, A_subclass_some_p_B));
            if(reasoner.isEntailed(some_p_B_subclass_A))
                this.entailedRestrictions.add(new Restriction(A, B, some_p_B_subclass_A));
        }
    }

    private void checkUniversalRestriction(OWLClass A, OWLObjectPropertyExpression p, OWLClass B){
        OWLClassExpression all_p_B = factory.getOWLObjectAllValuesFrom(p,B);
        OWLSubClassOfAxiom A_subclass_all_p_B = factory.getOWLSubClassOfAxiom(A,all_p_B);
        OWLSubClassOfAxiom all_p_B_subclass_A = factory.getOWLSubClassOfAxiom(all_p_B,A);

        if(ontology.containsAxiom(A_subclass_all_p_B))
            this.assertedRestrictions.add(new Restriction(A, B, A_subclass_all_p_B));
        if(ontology.containsAxiom(all_p_B_subclass_A))
            this.assertedRestrictions.add(new Restriction(A, B, all_p_B_subclass_A));

        if(entailments){
            if(reasoner.isEntailed(A_subclass_all_p_B))
                this.entailedRestrictions.add(new Restriction(A, B, A_subclass_all_p_B));
            if(reasoner.isEntailed(all_p_B_subclass_A))
                this.entailedRestrictions.add(new Restriction(A, B, all_p_B_subclass_A));
        }
    }

    private void checkMinCardinalityRestriction(OWLClass A, OWLObjectPropertyExpression p, OWLClass B){
        OWLClassExpression lt1_p_B = factory.getOWLObjectMinCardinality(1,p,B);
        OWLSubClassOfAxiom A_subclass_lt1_p_B = factory.getOWLSubClassOfAxiom(A,lt1_p_B);
        OWLSubClassOfAxiom lt1_p_B_subclass_A = factory.getOWLSubClassOfAxiom(lt1_p_B,A);

        if(ontology.containsAxiom(A_subclass_lt1_p_B))
            this.assertedRestrictions.add(new Restriction(A, B, A_subclass_lt1_p_B));
        if(ontology.containsAxiom(lt1_p_B_subclass_A))
            this.assertedRestrictions.add(new Restriction(A, B, lt1_p_B_subclass_A));

        if(entailments){
            if(reasoner.isEntailed(A_subclass_lt1_p_B))
                this.entailedRestrictions.add(new Restriction(A, B, A_subclass_lt1_p_B));
            if(reasoner.isEntailed(lt1_p_B_subclass_A))
                this.entailedRestrictions.add(new Restriction(A, B, lt1_p_B_subclass_A));
        }
    }

    private void checkMaxCardinalityRestriction(OWLClass A, OWLObjectPropertyExpression p, OWLClass B){
        OWLClassExpression gt1_p_B = factory.getOWLObjectMaxCardinality(1,p,B);
        OWLSubClassOfAxiom A_subclass_gt1_p_B = factory.getOWLSubClassOfAxiom(A,gt1_p_B);
        OWLSubClassOfAxiom gt1_p_B_sublcass_A = factory.getOWLSubClassOfAxiom(gt1_p_B,A);

        if(ontology.containsAxiom(A_subclass_gt1_p_B))
            this.assertedRestrictions.add(new Restriction(A, B, A_subclass_gt1_p_B));
        if(ontology.containsAxiom(gt1_p_B_sublcass_A))
            this.assertedRestrictions.add(new Restriction(A, B, gt1_p_B_sublcass_A));

        if(entailments){
            if(reasoner.isEntailed(A_subclass_gt1_p_B))
                this.entailedRestrictions.add(new Restriction(A, B, A_subclass_gt1_p_B));
            if(reasoner.isEntailed(gt1_p_B_sublcass_A))
                this.entailedRestrictions.add(new Restriction(A, B, gt1_p_B_sublcass_A));
        }
    }




}
