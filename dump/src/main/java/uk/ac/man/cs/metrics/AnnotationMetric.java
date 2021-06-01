package uk.ac.man.cs.metrics;

import uk.ac.man.cs.codp.CODP;
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.util.StringMangler;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLEntity;
import java.util.HashSet;
import java.util.HashMap;

import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.HasAnnotationPropertiesInSignature;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import java.util.Set;
import java.util.*;
import java.io.BufferedWriter;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Iterator;

import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;
/**
 * Created by chris on 03/08/18.
 */
public class AnnotationMetric extends Metric {

    private OWLOntology ontology;
    private CODP ontologyPattern;
    private HashMap<String, Pair<OWLEntity, Set<String>>> evidence;

    public AnnotationMetric(OWLOntology o, CODP p){
        this.ontology = o;
        this.ontologyPattern = p;
        this.evidence = new HashMap<>();
    }

    public HashMap<String, Pair<OWLEntity, Set<String>>> getEvidence(){
        return this.evidence; 
    }

    /**
     * Evidence to be found in the annotations of entities
     */
    public void compute(){
        this.evidence.clear();
        for(String k : this.ontologyPattern.getKeys()){

            StringPattern pattern = new StringPattern(k);

            Set<OWLEntity> entities = this.ontology.getSignature();
            StringMangler stringMangler = new StringMangler();

            //check all entities 
            for(OWLEntity e : entities){ 
                Set<String> annotationsWithKey = new HashSet<String>();

                /*
                Iterable<OWLAnnotation> annotations = getAnnotationObjects(e, ontology);
                //check all annotations for each entity 
                for(OWLAnnotation a : annotations){
                    String annotationString = a.toString();

                    if(pattern.findIn(annotationString)){
                        annotationsWithKey.add(stringMangler.flattenLineBreaks(a.toString())); 
                    } 
                } 
                if(!annotationsWithKey.isEmpty()){
                    Pair<OWLEntity, Set<String>> pair = new Pair(e, annotationsWithKey);
                    evidence.put(k, pair); 
                }
                */
            } 
        }
    }

    public void write(String destFile){
        destFile = destFile + "/annotation";
        if(!this.evidence.isEmpty()){
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(destFile));
                Iterator it = this.evidence.entrySet().iterator();

                //output format
                String output = "patternKey,OWLEntity,[Set of Annotations]";
                bw.write(output);
                bw.newLine();

                //StringMangler stringMangler = new StringMangler();

                while(it.hasNext()){
                    Map.Entry pair = (Map.Entry)it.next();

                    Set<String> annotations = ((Pair<OWLEntity, Set<String>>)pair.getValue()).getSecond();
                    output = ((String) pair.getKey()) + "," +
                        ((Pair<OWLEntity, Set<String>>)pair.getValue()).getFirst().toString() + ","
                        //+ stringMangler.removeLineBreaks(annotations);
                        + annotations;

                    bw.write(output);
                    bw.newLine();
                }
                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } 
        } 
    } 

    public void reset(){
        this.evidence.clear(); 
    }

    public void setOntology(OWLOntology o){
        this.reset();
        this.ontology = o; 
    }
}
