package uk.ac.man.cs.data;

import uk.ac.man.cs.codp.CODP;
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.util.StringMangler;
import uk.ac.man.cs.metrics.*;
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
    private CODP pattern;

    private HashMap<OWLEntity, Set<LexicalMatch>> associations;
    private HashMap<OWLEntity, Set<LexicalMatch>> typeMatchingAssociations;
    private HashMap<OWLEntity, Set<LexicalMatch>> associationsByAnnotation;
    private HashMap<OWLEntity, Set<LexicalMatch>> typeMatchingAssociationsByAnnotation;

    public LexicalAssociation(OWLOntology o, CODP p){
        this.ontology = o;
        this.pattern = p; 
        this.associations = new HashMap<>();
        this.typeMatchingAssociations = new HashMap<>();
        this.associationsByAnnotation = new HashMap<>();
        this.typeMatchingAssociationsByAnnotation = new HashMap<>();
        this.computeAssociations();
    }

    public HashMap<OWLEntity, Set<LexicalMatch>> getAssociations(){
        return this.associations; 
    }

    public HashMap<OWLEntity, Set<LexicalMatch>> getTypeMatchingUnion(){
        HashMap<OWLEntity, Set<LexicalMatch>> union = new HashMap<>();

        for(Map.Entry<OWLEntity, Set<LexicalMatch>> entry : this.typeMatchingAssociations.entrySet()){
            if(this.typeMatchingAssociationsByAnnotation.containsKey(entry.getKey())){
                Set<LexicalMatch> u = new HashSet<>();
                u.addAll(typeMatchingAssociationsByAnnotation.get(entry.getKey()));
                u.addAll(entry.getValue()); 
                union.put(entry.getKey(), u);
            } else {
                union.put(entry.getKey(), entry.getValue()); 
            } 
        }
        return union; 
    }
    
    public HashMap<OWLEntity, Set<LexicalMatch>> getMatchingAssociations(){
        return this.typeMatchingAssociations; 
    }

    public HashMap<OWLEntity, Set<LexicalMatch>> getAnnotationAssociations(){
        return this.associationsByAnnotation; 
    }

    public HashMap<OWLEntity, Set<LexicalMatch>> getTypeMatchingAnnotationAssociations(){
        return this.typeMatchingAssociationsByAnnotation;
    }


    private void computeAssociations(){
        Set<OWLEntity> ontSig = this.ontology.getSignature();
        Set<OWLEntity> patSig = this.pattern.getSignatureAsSet();
        StringMangler stringMangler = new StringMangler();


        for(OWLEntity pEntity : patSig){

            StringPattern stringPattern = new StringPattern(getShortIRI(pEntity));
            Set<LexicalMatch> association = new HashSet<>();
            Set<LexicalMatch> typeMatchingAssociation = new HashSet<>();
            Set<LexicalMatch> assocByAnnotation = new HashSet<>();
            Set<LexicalMatch> typeMatchingAssocByAnnotation = new HashSet<>();

            //check entity names
            for(OWLEntity oEntity : ontSig){
                String entityName = stringMangler.flattenLineBreaks(getShortIRI(oEntity));

                if(stringPattern.findIn(entityName)){
                    LexicalMatch m = new LexicalMatch(pEntity, oEntity); 
                    association.add(m); 
                    if(pEntity.getEntityType() == oEntity.getEntityType())
                        typeMatchingAssociation.add(m);
                }
                else { 
                //check entity annotations
                    Stream<OWLAnnotation> aStream = EntitySearcher.getAnnotations(oEntity, this.ontology);
                    Set<OWLAnnotation> aProps = aStream.collect(Collectors.toSet());

                    for (OWLAnnotation a : aProps){
                        OWLAnnotationValue val = a.getValue();

                        //stupid way of getting to annottation Strings
                        if(val instanceof OWLLiteral){
                            String annotation = ((OWLLiteral) val).getLiteral();
                            if(stringPattern.findIn(annotation)){
                                LexicalMatch m = new LexicalMatch(pEntity,
                                                                  oEntity,
                                                                  a.getProperty(),
                                                                  annotation); 
                                assocByAnnotation.add(m); 

                                if(pEntity.getEntityType() == oEntity.getEntityType())
                                    typeMatchingAssocByAnnotation.add(m); 
                            } 
                        }
                    } 
                }
            }

            //store results
            if(!association.isEmpty())
                this.associations.put(pEntity, association); 
            if(!typeMatchingAssociation.isEmpty())
                this.typeMatchingAssociations.put(pEntity, typeMatchingAssociation);
            if(!assocByAnnotation.isEmpty())
                this.associationsByAnnotation.put(pEntity, assocByAnnotation);
            if(!typeMatchingAssocByAnnotation.isEmpty())
                this.typeMatchingAssociationsByAnnotation.put(pEntity, typeMatchingAssocByAnnotation);
        }
    } 

    private String getShortIRI(OWLEntity e){
        return e.getIRI().getShortForm().toString(); 
    }

    public OWLOntology getOntology(){
        return this.ontology; 
    }

    public CODP getPattern(){
        return this.pattern; 
    }
}
