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
public class AssertedTripleDetector {

    private OWLOntology ontology;
    private EntityTriple triple; 
    private OWLDataFactory factory;
    private String relation;

    private static final double THRESHOLD = 0.7;
    private static final int kShingling = 2;
    private static String BAD_CHARS = "[^a-z]";

    private Cosine cosine;


    public AssertedTripleDetector(OWLOntology o, EntityTriple t) throws Exception {
        this.ontology = o;
        this.triple = t; 
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory(); 
        this.cosine = new Cosine(kShingling);
    }

    public AssertedTripleDetector(OWLOntology o) throws Exception {
        this.ontology = o;
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory(); 
        this.cosine = new Cosine(kShingling);
    }

    public void setTriple(EntityTriple t){
        this.triple = t; 
    }

    public void setRelation(String r){
        this.relation = r;
    }

    private Set<OWLEntity> getRelations(){
        HashSet<OWLEntity> res = new HashSet<>();
        Set<OWLEntity> signature = this.ontology.getSignature();
        for(OWLEntity  e : signature){
            if(e.isOWLObjectProperty()){
                String propertyIRI = e.getIRI().getShortForm().toLowerCase();
                if(stringMatch(propertyIRI, this.relation)){
                    res.add(e); 
                }
            } 
        }
        return res;
    } 

    public Set<OWLAxiom> run(){ 
        Set<OWLEntity> relations = getRelations();
        System.out.println(relations.size() + "  many relations");
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);
        Set<OWLAxiom> res = new HashSet<>();
        for(OWLEntity r : relations){ 
            Set<OWLAxiom> usage = entityUsage(r, tBox);
            System.out.println(usage.size() + " many axioms");
            double i = 0;
            for(OWLAxiom a : usage){
                if(analyseAxiom(a)){
                    res.add(a);
                } 
                System.out.println( i++/usage.size());
            } 
        }
        System.out.println("For loop");
        return res;
    }

    private boolean analyseAxiom(OWLAxiom a){
        Entity subject = this.triple.getSubject();
        Entity object = this.triple.getObject();

        if(!subject.hasMatch() || !object.hasMatch())
            return false;

        Set<ClassMatch> subjectMatches = subject.getMatches();
        Set<ClassMatch> objectMatches = subject.getMatches();

        Set<OWLEntity> signature = a.getSignature();

        boolean containsSubject = false;
        boolean containsObject = false;

        for(ClassMatch m : subjectMatches){
            OWLEntity match = m.getOWLClass();
            if(signature.contains(match))
                containsSubject = true;
        }

        for(ClassMatch m : objectMatches){
            OWLEntity match = m.getOWLClass();
            if(signature.contains(match))
                containsObject = true;
        }

        if(containsSubject && containsObject)
            return true;
        return false;
    }

    //private boolean matchEntity(Entity e, OWLEntity target){
    //    if(!e.isOWLClass())
    //        return false;

    //    boolean found = check(e, e.getName(), c); 
    //    if(!found)
    //        found = check(e, e.getConceptName(), c);
    //    if(!found)
    //        found = check(e, e.getPreferredName(), c);
    //    if(!found)
    //        found = check(e, e.getConceptID(), c);
    //    return found;
    //}

    //private boolean check(Entity e, String entityKey, OWLEntity target){
    //    //check name direct
    //    if(stringMatch(entityName, target.getIRI().getShortForm())){ 
    //        ClassMatch match = new ClassMatch(target.asOWLClass(), entityKey); 
    //        return true;
    //    } 
    //    if(checkAnnotations(target, entityKey)){ 
    //        return true; 
    //    } 
    //    return false;
    //}


    //private boolean checkAnnotations(OWLEntity e, String searchString){
    //    Stream<OWLAnnotation> annotationStream = EntitySearcher.getAnnotationObjects(e, this.ontology);
    //    //check annotations
    //    Set<OWLAnnotation> annotations = annotationStream.collect(Collectors.toSet());
    //    for(OWLAnnotation ann : annotations) {
    //        String property = ann.getProperty().toString().toLowerCase();
    //        if(property.contains("label") ||
    //           property.contains("synonym") || 
    //           property.contains("name") || 
    //           property.contains("term"))
    //        {
    //            if(ann.getValue().isLiteral() && ann.getValue().asLiteral().isPresent()){
    //                String value = ann.getValue().asLiteral().get().getLiteral().toLowerCase();
    //                if(stringMatch(value, searchString)){ 
    //                    ClassMatch match = new ClassMatch(target.asOWLClass(), entityKey); 
    //                    return true;
    //                } 
    //            }
    //        }
    //    }
    //    return false;
    //}


    private Set<OWLAxiom> entityUsage(OWLEntity entity, Set<OWLAxiom> axioms){
        Set<OWLAxiom> res = new HashSet<>(); 
        //for(OWLAxiom axiom : ontology.getAxioms(Imports.EXCLUDED)){
        for(OWLAxiom axiom : axioms){
            if((axiom.getSignature()).contains(entity)){
                    res.add(axiom);
            }
        }
        return res; 
    }

    private static String normalise(String s){
        String res = s.toLowerCase();
        res = res.replaceAll(BAD_CHARS,"");
        return res; 
    }

    private boolean stringMatch(String s1, String s2){ 
        String normS = normalise(s1);
        String normT = normalise(s2);
        if(normS.contains(normT))
            return true;
        if(normT.contains(normS))
            return true;
        //if(cosine.similarity(s1, s2) > THRESHOLD)
        //    return true;
        return false;
    }

}
