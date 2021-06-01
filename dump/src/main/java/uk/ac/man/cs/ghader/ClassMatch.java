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
public class ClassMatch {

    private OWLClass owlClass;
    private String entityName;
    //annotations to check
    private String prefLabel;
    private String altLabel;
    private String label;
    //annotations involving 
    private String synonym;
    private String name;
    private String term;

    public ClassMatch(OWLClass c, String n) { 
        this.owlClass = c;
        this.entityName = n;
        this.prefLabel = null;
        this.altLabel = null;
        this.label = null;
        this.synonym = null;
        this.name = null;
        this.term = null;
    }

    public OWLClass getOWLClass(){
        return this.owlClass; 
    }

    public void setPreferredLabel(String s){ 
        this.prefLabel = s; 
    }
    public void setAlternativeLabel(String s){ 
        this.altLabel = s; 
    }
    public void setLabel(String s){ 
        this.label = s; 
    }
    public void setSynonym(String s){ 
        this.synonym = s; 
    }
    public void setName(String s){ 
        this.name = s; 
    }
    public void setTerm(String s){ 
        this.term = s; 
    }

    public void setEntityName(String s){
        this.entityName = s; 
    }
}

