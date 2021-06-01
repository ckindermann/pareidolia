package uk.ac.man.cs.lodp2;

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
public class NaryRelation{
    private OWLClassExpression relation;
    private Set<OWLClassExpression> restrictions;

    public NaryRelation(OWLClassExpression r, Set<OWLClassExpression> restr){
        this.relation = r;
        this.restrictions = restr; 
    }

    public OWLClassExpression getRelation(){
        return this.relation; 
    }

    public Set<OWLClassExpression> getRestrictions(){
        return this.restrictions; 
    }

    public Set<OWLClassExpression> getExistentialRestrictions(){
        Set<OWLClassExpression> existentials = new HashSet<>();
        for(OWLClassExpression c : this.restrictions){
            if(c instanceof OWLObjectSomeValuesFrom)
                existentials.add(c); 
        }
        return existentials; 
    }

    public Set<OWLClassExpression> getUniversalRestrictions(){
        Set<OWLClassExpression> universals = new HashSet<>();
        for(OWLClassExpression c : this.restrictions){
            if(c instanceof OWLObjectAllValuesFrom)
                universals.add(c); 
        }
        return universals; 
    }


    public void print(){
        System.out.println("Relation: " + this.relation);
        System.out.println("Restrictions: " + this.restrictions); 
    }
}
