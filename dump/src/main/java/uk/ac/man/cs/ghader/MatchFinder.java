package uk.ac.man.cs.ghader;
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;

import org.semanticweb.owlapi.model.*;
import java.io.*;
import java.util.Set;
import java.util.*;
import java.util.stream.*;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import info.debatty.java.stringsimilarity.*;

import org.semanticweb.owlapi.search.EntitySearcher;
import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

/**
 * Created by chris on 23/11/18.
 */
public class MatchFinder {

    private OWLOntology ontology;
    EntityTriple triple;
    //gets a set of entity triples
    //private Set<EntityTriple> entityTriples; 
    //looks for matches in the ontology
    //private Set<EntityTriple> hits; 

    private static final double THRESHOLD = 0.7;
    private static final int kShingling = 2;
    private static String BAD_CHARS = "[^a-z]";

    private Cosine cosine;


    public MatchFinder(OWLOntology o, EntityTriple t){
        this.ontology = o;
        this.triple = t; 
        this.cosine = new Cosine(kShingling);
    }

    public boolean run(){

        Set<OWLClass> classes = this.ontology.getClassesInSignature();
        Set<OWLObjectProperty> properties = this.ontology.getObjectPropertiesInSignature(false);

        //for(EntityTriple triple : this.entityTriples){
            boolean foundSubject = false;
            boolean foundObject = false;
            boolean foundRelation = false;

            Entity subject = triple.getSubject();
            Entity object = triple.getObject();
            Relation relation = triple.getRelation();

            for(OWLClass c : classes){
                foundSubject = matchEntity(subject, c);
                foundObject = matchEntity(object, c);
            }

            for(OWLObjectProperty p : properties){
                foundRelation = matchRelation(relation, p); 
            } 

            if(foundSubject && foundObject && foundRelation){
                //hits.add(triple); 
                return true;
            }
        //} 
        return false;
    }

    private boolean stringMatch(String s1, String s2){ 
        boolean res = (cosine.similarity(s1, s2) > THRESHOLD);
        return res;
    }

    private boolean matchRelation(Relation r, OWLObjectProperty property){
        String propertyIRI = property.getIRI().getShortForm().toLowerCase();
        //if(cosine.similarity(r.getName(), propertyIRI) > THRESHOLD){
        if(stringMatch(r.getName(), propertyIRI) || matchIn(r.getName(), propertyIRI)){
            RelationMatch m = new RelationMatch(property);
            r.addMatch(m); 
            return true;
        } 
        if(checkAnnotationsForProperty(r, property))
            return true;
        return false;

    }

    private boolean checkAnnotationsForProperty(Relation r, OWLObjectProperty p){
        Stream<OWLAnnotation> annotationStream = EntitySearcher.getAnnotationObjects(p, this.ontology);
        //check annotations
        Set<OWLAnnotation> annotations = annotationStream.collect(Collectors.toSet());
        for(OWLAnnotation ann : annotations) {
            String property = ann.getProperty().toString().toLowerCase();
            if(property.contains("label") ||
               property.contains("synonym") || 
               property.contains("name") || 
               property.contains("term"))
            {
                if(ann.getValue().isLiteral() && ann.getValue().asLiteral().isPresent()){
                    String value = ann.getValue().asLiteral().get().getLiteral().toLowerCase();
                    if(cosine.similarity(r.getName(), value) > THRESHOLD || matchIn(r.getName(), value)){
                        RelationMatch m = new RelationMatch(p);
                        m.setAnnotationProperty(value);
                        r.addMatch(m); 
                        return true;
                    } 
                }
            }
        }
        return false;
    }

    private boolean matchEntity(Entity e, OWLClass c){
        boolean found = match(e, e.getName(), c);
        if(!found)
            found = match(e, e.getConceptName(), c);
        if(!found)
            found = match(e, e.getPreferredName(), c);
        if(!found)
            found = matchID(e, e.getConceptID(), c);
        return found;
    } 

    private boolean match(Entity entity, String key, OWLClass c){
        if(checkNameDirect(entity, c))
            return true; 

        if(checkAnnotation(entity, key, c))
            return true;

        return false; 
    }

    private boolean checkNameDirect(Entity entity, OWLClass c){
        String classIRI = c.getIRI().getShortForm().toLowerCase();

        if(cosine.similarity(entity.getName(), classIRI) > THRESHOLD || matchIn(entity.getName(), classIRI)){
            ClassMatch m = new ClassMatch(c, entity.getName());
            entity.addMatch(entity.getName(), m);
            return true;
        } 
        return false; 
    }

    private boolean checkAnnotation(Entity entity, String key, OWLClass c){

        Stream<OWLAnnotation> annotationStream = EntitySearcher.getAnnotationObjects(c, this.ontology);
        //check annotations
        Set<OWLAnnotation> annotations = annotationStream.collect(Collectors.toSet());
        for(OWLAnnotation ann : annotations) {
            if(checkAnnotationFor(entity, key, ann, c, "preferred", "label"))
                return true;
            if(checkAnnotationFor(entity, key, ann, c, "alternative", "label"))
                return true;
            if(checkAnnotationFor(entity, key, ann, c, "label"))
                return true;
            if(checkAnnotationFor(entity, key, ann, c, "synonym"))
                return true;
            if(checkAnnotationFor(entity, key, ann, c, "name"))
                return true;
            if(checkAnnotationFor(entity, key, ann, c, "term"))
                return true;
        }
        return false;
    }

    private boolean checkAnnotationFor(Entity entity, String key, OWLAnnotation ann, OWLClass c, String s1, String s2){
        String property = ann.getProperty().toString().toLowerCase();
        if(property.contains(s1) && property.contains(s2)){
            if(ann.getValue().isLiteral() && ann.getValue().asLiteral().isPresent()){
                String value = ann.getValue().asLiteral().get().getLiteral().toLowerCase();
                if(cosine.similarity(key, value) > THRESHOLD){
                    ClassMatch m = new ClassMatch(c, key);
                    m.setPreferredLabel(value);
                    entity.addMatch(key, m);
                    return true;
                }
            } 
        }
        return false;
    }

    private boolean checkAnnotationFor(Entity entity, String key, OWLAnnotation ann, OWLClass c, String s){
        return checkAnnotationFor(entity, key, ann, c, s, s);
    }

    private boolean matchID(Entity entity, String id, OWLClass c){
        Stream<OWLAnnotation> annotationStream = EntitySearcher.getAnnotationObjects(c, this.ontology);
        //check annotations
        Set<OWLAnnotation> annotations = annotationStream.collect(Collectors.toSet());
        for(OWLAnnotation ann : annotations) {
            if(checkAnnotationFor(entity, id, ann, c, "umls", "cui"))
                return true;
            if(checkAnnotationFor(entity, id, ann, c, "umls", "id"))
                return true;
            if(checkAnnotationFor(entity, id, ann, c, "cross", "reference"))
                return true;
            if(checkAnnotationFor(entity, id, ann, c, "database", "reference"))
                return true;
            if(checkAnnotationFor(entity, id, ann, c, "hasDbXref"))
                return true;
        }
        return false;
    }

    //strip all special characters 
    private static String normalise(String s){
        String res = s.toLowerCase();
        res = res.replaceAll(BAD_CHARS,"");
        return res; 
    }

    public static boolean matchIn(String s, String t){
        String normS = normalise(s);
        String normT = normalise(t);
        if(normS.contains(normT))
            return true;
        if(normT.contains(normS))
            return true;
        return false;
    }

}
