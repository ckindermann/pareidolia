package uk.ac.man.cs.detectors.lexical;
import org.semanticweb.owlapi.model.*;
import java.util.*;

/*
 * Created by chris on 03/10/18.
 */
public class LexicalMatch {

    private OWLEntity source;
    private OWLEntity association;


    public LexicalMatch(OWLEntity s, OWLEntity assoc){
        this.source = s;
        this.association = assoc;
    }

    public LexicalMatch(OWLEntity s, OWLEntity assoc, OWLEntity aProp, String ann){
        this.source = s;
        this.association = assoc;
    }

    public OWLEntity getSource(){
        return this.source; 
    }
    public OWLEntity getAssociation(){
        return this.association; 
    } 
}
