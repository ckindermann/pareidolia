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
public class Restriction {
    OWLClass class1;
    OWLClass class2;
    OWLAxiom relation;

    public Restriction(OWLClass a, OWLClass b, OWLAxiom r){
       this.class1 = a;
       this.class2 = b;
       this.relation = r; 
    }

    public OWLClass getClass1(){ 
        return this.class1;
    }

    public OWLClass getClass2(){ 
        return this.class2;
    }

    public OWLAxiom getRelation(){ 
        return this.relation; 
    }

}
