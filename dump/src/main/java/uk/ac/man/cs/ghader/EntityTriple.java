package uk.ac.man.cs.ghader;

import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;

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
public class EntityTriple {

    Entity subject;
    Entity object;
    Relation relation;

    Set<OWLAxiom> foundAxioms;

    public EntityTriple(Entity sub, Entity obj, Relation r) throws Exception { 
        this.subject = sub;
        this.object = obj;
        this.relation = r;
        this.foundAxioms = new HashSet<>();
    } 

    public Entity getSubject(){
        return this.subject; 
    }

    public Entity getObject(){
        return this.object; 
    }

    public Relation getRelation(){
        return this.relation; 
    }
}

