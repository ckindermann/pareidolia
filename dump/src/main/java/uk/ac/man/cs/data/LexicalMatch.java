package uk.ac.man.cs.data;
import org.semanticweb.owlapi.model.*;
import java.util.*;

/*
 * Created by chris on 03/10/18.
 */
public class LexicalMatch {

    private OWLEntity source;
    private OWLEntity association;

    private Optional<OWLEntity> annotationProperty;
    private Optional<String> annotation;

    private boolean typeMatch;

    public LexicalMatch(OWLEntity s, OWLEntity assoc){
        this.source = s;
        this.association = assoc;
        this.annotation = Optional.empty();
        this.setType();
    }

    public LexicalMatch(OWLEntity s, OWLEntity assoc, OWLEntity aProp, String ann){
        this.source = s;
        this.association = assoc;
        this.annotation = Optional.of(ann);
        this.annotationProperty = Optional.of(aProp);
        this.setType();
    }

    public OWLEntity getSource(){
        return this.source; 
    }
    public OWLEntity getAssociation(){
        return this.association; 
    }

    private void setType(){
        this.typeMatch = this.source.getEntityType() == this.association.getEntityType();
    }

    public boolean matchesType(){
        return this.typeMatch; 
    }

    public boolean associatedByAnnotation(){
        return this.annotation.isPresent();
    }

    public String getAnnotation(){
        if(this.annotation.isPresent())
            return annotation.get();
        return ""; 
    }

}
