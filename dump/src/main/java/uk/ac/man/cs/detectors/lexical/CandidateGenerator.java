package uk.ac.man.cs.detectors.lexical;

import uk.ac.man.cs.util.*;
import uk.ac.man.cs.ont.*;

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
import com.google.common.collect.Sets;

import java.io.*;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by chris on 03/08/18.
 */

public class CandidateGenerator {

    //possible substitutions sets:
    //(1) based on local name matching
    //(2) based on annotation matching
    //private LexicalAssociation lexicalAssociations;

    //selection mapping from lexicalAssociations
    private HashMap<OWLEntity, Set<LexicalMatch>> substitutionMappings;

    //all possible substitutions given by substitutionMappings 
    private Set<List<LexicalMatch>> substitutions;

    //substitution is done by stringhacking
    private LinkedList<String> patternAsText;

    private String outputPath;

    public CandidateGenerator(File patFile) throws IOException {

        //parse pattern as text file
        this.patternAsText = IOHelper.readTextFile(patFile);

        //that's a bit weird - I can't initialise this.substitutionMappings with subSet
        //it will be empty in subsequent calls
        this.substitutionMappings = new HashMap<>();
        //this.substitutionMappings.putAll(this.lexicalAssociations.getMatchingAssociations()); 

        this.substitutions = new HashSet<>();
        //this.substitutions.addAll(this.createCandidateInstances());

        this.outputPath = "";
    }

    public void computeSubstitutions(HashMap<OWLEntity, Set<OWLEntity>> map){
        this.substitutionMappings.clear();
        this.substitutions.clear();

        //if(this.lexicalAssociations.existsAssociation()){ 
            this.setSubstitutionMappings(map);
            this.substitutions.addAll(this.createCandidateInstances()); 
            //this.substitutions = createCandidateInstances();
        //} 
    }

    //given lexical associations in a map [OWLEntity 2 Set<OWLEntity>]
    //and put them in substitutionMappings [OWLEntity 2 Lexicalmatch]
    //(this is necessary to easily create all possible substitutions
    //via the cartesian product)
    public void setSubstitutionMappings(HashMap<OWLEntity, Set<OWLEntity>> map){
        for(Map.Entry<OWLEntity, Set<OWLEntity>> entry : map.entrySet()){ 
            OWLEntity key = entry.getKey();
            Set<OWLEntity> values = map.get(key);
            Set<LexicalMatch> lexicalMatches = new HashSet<>();
            for(OWLEntity e : values){
                lexicalMatches.add(new LexicalMatch(key, e)); 
            }
            this.substitutionMappings.put(key, lexicalMatches); 
        } 
    }

    //gets all possible substitutions 
    public Set<List<LexicalMatch>> createCandidateInstances(){

        //get all potential substitutions for entities as sets
        LinkedList<Set<LexicalMatch>> subs = new LinkedList<Set<LexicalMatch>>(); 

        for(Map.Entry<OWLEntity, Set<LexicalMatch>> entry : this.substitutionMappings.entrySet()){ 
            subs.add(((Set<LexicalMatch>) entry.getValue())); 
        }

        //get all possibile instantiations by constructing the cartesian product
        Set<List<LexicalMatch>> cartesian = Sets.cartesianProduct(subs); 

        return cartesian; 
    }

    public LinkedList<String> substitute(List<LexicalMatch> mapping){

        //this will be the result of the performed substitution
        LinkedList<String> substitution = new LinkedList<String>(); 

        //this is the pattern in which we will replace entities
        Iterator<String> patTextIterator = this.patternAsText.iterator();

        while(patTextIterator.hasNext()){
            String line = patTextIterator.next();

            //a mapping consists of a substitution function for
            //all entities occuring in logical axioms
            Iterator<LexicalMatch> mappingIterator = mapping.iterator();

            while(mappingIterator.hasNext()){
                LexicalMatch match = mappingIterator.next();

                //this is the entity to be replaced in the pattern
                String entity = match.getSource().getIRI().toString(); 
                Pattern entityPattern = Pattern.compile(entity);

                //look for the entity in the line of the file
                Matcher ma = entityPattern.matcher(line); 
                if(ma.find()){ 
                    //and PERFORM SUBSTITUTION
                    line = line.replaceAll(entity, match.getAssociation().getIRI().toString());
                }
            }
            substitution.add(line);
        }
        return substitution; 
    }

    //write all possible substitutions to a file 
    //and assign a number to each substitution
    //(which is returned as a HashMap) 
    public HashMap<Integer,List<LexicalMatch>> writeSubstitutions(String outputPath){
        this.outputPath = outputPath;
        int counter = 0;
        IOHelper.createFolder(outputPath);

        //assign a number to each instantiation
        //which is to be returned
        HashMap<Integer,List<LexicalMatch>> writingOrder = new HashMap();

        for(List<LexicalMatch> sub : this.substitutions){
            counter++;

            Iterator<LexicalMatch> it = sub.iterator();
            if(it.hasNext()){//just check whether sub is not empty here ...
                LinkedList<String> instance = this.substitute(sub);
                IOHelper.writeApenndList(instance, outputPath + "/" + counter); 
                //this.writeSubstitutionInstance(sub, outputPath + "/substitution/" + counter);
            }
            writingOrder.put(new Integer(counter), sub);
        } 
        return writingOrder;
    }

    //public void writeSubstitutionInstance(List<LexicalMatch> l, String outputPath){ 
    //        Iterator<LexicalMatch> subIterator = l.iterator();

    //        while(subIterator.hasNext()){
    //            LexicalMatch lm = subIterator.next();
    //            String sub = lm.getSource().getIRI().toString() + " --> " + lm.getAssociation().getIRI().toString(); 
    //            IOHelper.writeAppend(sub, outputPath); 
    //        } 
    //}

    public HashMap<OWLEntity, Set<LexicalMatch>> getSubstitutionMappings(){
        return this.substitutionMappings; 
    }

    public void cleanUp(String path){
        File instances = new File(path);
        if(instances.exists()){ 
            for(File inst : instances.listFiles()){
                inst.delete(); 
            } 
            instances.delete();
        } 
    }

}

