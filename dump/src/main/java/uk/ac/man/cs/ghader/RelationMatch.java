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
public class RelationMatch {

    private OWLObjectProperty owlRelation;
    //annotations to check
    private String AnnotationProperty;

    public RelationMatch(OWLObjectProperty p) { 
        this.owlRelation = p;
        this.AnnotationProperty = null;
    }

    public OWLObjectProperty getProperty(){
        return this.owlRelation; 
    }

    public void setAnnotationProperty(String s){
        this.AnnotationProperty = s; 
    }

    public String getAnnotationProperty(){
        return this.AnnotationProperty; 
    }

}

