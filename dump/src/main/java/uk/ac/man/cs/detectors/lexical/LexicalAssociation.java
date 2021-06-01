package uk.ac.man.cs.detectors.lexical;

import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.util.StringMangler;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.strings.*;
import uk.ac.man.cs.ont.Ontology;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import java.util.HashSet;
import java.util.HashMap;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.*;
import java.util.Set;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.io.File;
import java.io.FileWriter;
import org.semanticweb.owlapi.search.EntitySearcher;
import java.util.stream.*;
import java.io.FileReader;

import java.io.*;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by chris on 03/08/18.
 */

//public class SubstitutionSet {
public class LexicalAssociation {

    private OWLOntology ontology;
    private OWLOntology pattern;

    private HashMap<OWLEntity, Set<OWLEntity>> associations;
    private HashMap<OWLEntity, Set<OWLEntity>> associationsWithAnnotations;

    private boolean hasSubstitution;
    private boolean hasStrongSubstitution;//without annotations
    private boolean hasWeakSubstitution;//with annotations

    public LexicalAssociation(OWLOntology o, OWLOntology p){
        this.ontology = o;
        this.pattern = p; 
        this.associations = new HashMap<>();
        this.associationsWithAnnotations = new HashMap<>();
        this.hasSubstitution = true;
        this.hasStrongSubstitution = true;
        this.hasWeakSubstitution = true;
    }

    public boolean hasSubstitution(){ 
        return this.hasSubstitution;
    }

    public boolean hasStrongSubstitution(){
        return this.hasStrongSubstitution; 
    }

    public boolean hasWeakSubstitution(){
        return this.hasWeakSubstitution; 
    }

    public boolean existsAssociation(){
        //Set<OWLEntity> ontSig = this.ontology.getSignature(Imports.INCLUDED);
        //Set<OWLEntity> patSig = this.pattern.getSignature(Imports.INCLUDED);
        Set<OWLEntity> ontSig = getEntitiesInLogicalAxioms(this.ontology, true);
        Set<OWLEntity> patSig = getEntitiesInLogicalAxioms(this.pattern, true);

       // System.out.println("Signature size of pattern: "  + patSig.size());
       // System.out.println("Signature size of ontology: "  + ontSig.size());

        //for(OWLEntity e : ontSig)
            //System.out.println(getShortIRI(e));

        StringMangler stringMangler = new StringMangler();

        for(OWLEntity pEntity : patSig){

            Set<OWLEntity> association = new HashSet<>();
            Set<OWLEntity> associationWithAnnotation = new HashSet<>(); 

            //StringPattern stringPattern = new StringPattern(getShortIRI(pEntity)); 
            StringMatcher stringPattern = new StringMatcher(getShortIRI(pEntity));

            //check entity names
            for(OWLEntity oEntity : ontSig){
                //String entityName = stringMangler.flattenLineBreaks(getShortIRI(oEntity)); 
                String entityName = getShortIRI(oEntity);

                if(stringPattern.searchIn(entityName)){
                    //System.out.println("FOund String match between " + getShortIRI(pEntity) + " and " + getShortIRI(oEntity));
                    if(pEntity.getEntityType() == oEntity.getEntityType()){
                        association.add(oEntity); 
                        associationWithAnnotation.add(oEntity);
                    }
                }
                else { 
                //check entity annotations
                    if(matchesAnnotation(stringPattern, oEntity)){
                        if(pEntity.getEntityType() == oEntity.getEntityType())
                            associationWithAnnotation.add(oEntity); 
                    }
                }
            }

            if(association.isEmpty()){
                //System.out.println("Violating entity: " + pEntity.toString());
                this.hasStrongSubstitution = false;
            }
            if(associationWithAnnotation.isEmpty())
                this.hasWeakSubstitution = false;

            if(association.isEmpty() && associationWithAnnotation.isEmpty()){
                //System.out.println("Missing entity is: "  +  getShortIRI(pEntity));
                this.hasSubstitution = false;
                return false; 
            }


            //store results
            if(!association.isEmpty())
                this.associations.put(pEntity, association); 
            if(!associationWithAnnotation.isEmpty())
                this.associationsWithAnnotations.put(pEntity, associationWithAnnotation); 
        }
        return true;
    } 

    private boolean matchesAnnotation(StringMatcher matcher, OWLEntity e){
        String label = getLabel(e, this.ontology);

        if(matcher.searchIn(label)){
            return true;
        } 

        return false; 
    }

    private boolean matchesAnyAnnotation(StringMatcher matcher, OWLEntity e){
        //get annotations
        Stream<OWLAnnotation> aStream = EntitySearcher.getAnnotations(e, this.ontology);
        Set<OWLAnnotation> annotations = aStream.collect(Collectors.toSet());

        for (OWLAnnotation a : annotations){
            OWLAnnotationValue val = a.getValue();

            //stupid way of getting to annottation Strings
            if(val instanceof OWLLiteral){
                String annotation = ((OWLLiteral) val).getLiteral();
                if(matcher.searchIn(annotation)){
                    return true;
                } 
            }
        } 
        return false;
    }

    private String getShortIRI(OWLEntity e){
        return e.getIRI().getShortForm().toString(); 
    }

    private Set<OWLAxiom> getLogicalAxioms(OWLOntology ont, boolean includeClosure) {
        Set<OWLLogicalAxiom> logicalAxioms = ont.getLogicalAxioms(includeClosure);
        Set<OWLAxiom> axioms = new HashSet<>();  
        for(OWLLogicalAxiom axiom : logicalAxioms){
            axioms.add((OWLAxiom) axiom);
            //System.out.println(axiom.toString());
        }

        return axioms;
    }

    private Set<OWLEntity> getEntitiesInLogicalAxioms(OWLOntology ont, boolean includeClosure){
        Set<OWLEntity> logicalSignature = new HashSet<>();
        Set<OWLAxiom> logicalAxioms = getLogicalAxioms(ont, includeClosure);
        for(OWLAxiom axiom : logicalAxioms){
            logicalSignature.addAll(axiom.getSignature()); 
        }

        return logicalSignature; 
    }

    public OWLOntology getOntology(){
        return this.ontology; 
    }

    public HashMap<OWLEntity, Set<OWLEntity>> getAssociations(){
        return this.associations; 
    }

    public static String getLabel(OWLEntity c, OWLOntology o) {
        Stream<OWLAnnotation> stream = EntitySearcher.getAnnotations(c, o, o.getOWLOntologyManager().getOWLDataFactory().getRDFSLabel());
        Set<OWLAnnotation> setAnnotations = stream.collect(Collectors.toSet()); 

        for(OWLAnnotation a : setAnnotations){
            OWLAnnotationValue val = a.getValue();
            if (val instanceof OWLLiteral) {
                return ((OWLLiteral) val).getLiteral();
            }
        }
        return "";
    }

    public HashMap<OWLEntity, Set<OWLEntity>> getAssociationsWithAnnotations(){
        return this.associationsWithAnnotations; 
    }

}
