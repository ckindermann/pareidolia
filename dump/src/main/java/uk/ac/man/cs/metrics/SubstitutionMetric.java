package uk.ac.man.cs.metrics;

import uk.ac.man.cs.codp.CODP;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.data.*;
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

public class SubstitutionMetric extends Metric {

    //private OWLOntology ontology;
    //private CODP pattern;

    //possible substitutions sets:
    //(1) based on local name matching
    //(2) based on annotation matching
    private LexicalAssociation lexicalAssociations;

    //selection mapping from lexicalAssociations
    private HashMap<OWLEntity, Set<LexicalMatch>> substitutionMappings;

    //all possible substitutions given by substitutionMappings 
    private Set<List<LexicalMatch>> substitutions;

    //substitution is done by stringhacking
    private LinkedList<String> patternAsText;

    //private HashMap<String, String> commentsAbrv;
    private HashMap<String, String> rdfAbrv;

    private String outputPath;

    public SubstitutionMetric(OWLOntology ont, File patFile) throws IOException {

        //parse pattern as text file
        this.patternAsText = IOHelper.readTextFile(patFile);

        //read pattern as ontology
        CODP pattern = new CODP(patFile.getName(), patFile);

        //get the ontology
        //OntologyLoader loader = new OntologyLoader(ontFile, true);
        //OWLOntology ont = loader.getOntology(); 

        //calculate lexicalAssociations
        this.lexicalAssociations = new LexicalAssociation(ont, pattern);

        //stores XML/RDF abbreviations 
        this.rdfAbrv = new HashMap<>();
        this.parseNamespaceAbbreviations(patFile); 

        //that's a bit weird - I can't initialise this.substitutionMappings with subSet
        //it will be empty in subsequent calls
        this.substitutionMappings = new HashMap<>();
        this.substitutionMappings.putAll(this.lexicalAssociations.getMatchingAssociations()); 


        this.substitutions = new HashSet<>();
        this.substitutions.addAll(this.createCandidateInstances());

        this.outputPath = "";
    }


    //TODO
    public void compute(){
        //get substitution selection for 
        
    }

    public void write(String s){ 
        writeSubstitutions(s);
    }

    public void reset(){
        ;
        //TODO:
        //what else needs to be reset?
    }

    //TODO
    public void setOntology(OWLOntology o){
        ; 
    }

    public OWLOntology getOntology(){
        return this.lexicalAssociations.getOntology();
    }

    public HashMap<String, String> getRDFAbbreviations(){
        return this.rdfAbrv; 
    }

    public LexicalAssociation getLexicalAssociations(){
        return this.lexicalAssociations; 
    }

    public void cleanUp(){
        File instances = new File(this.outputPath + "/instance");
        if(instances.exists()){ 
            for(File inst : instances.listFiles()){
                inst.delete(); 
            } 
            instances.delete();
        } 
    }

    public void computeSubstitutions(){
        this.substitutionMappings.clear();
        this.substitutions.clear();

        this.substitutionMappings.putAll(this.lexicalAssociations.getMatchingAssociations());
        this.substitutions.addAll(this.createCandidateInstances()); 
    }

    public void computeSubstitutionsWithAnnotations(){
        this.substitutionMappings.clear();
        this.substitutions.clear();

        this.substitutionMappings.putAll(this.lexicalAssociations.getTypeMatchingUnion());
        this.substitutions.addAll(this.createCandidateInstances()); 
    }

    public LinkedList<String> substitute(List<LexicalMatch> mapping){

        LinkedList<String> substitution = new LinkedList<String>(); 
        Iterator<String> patTextIterator = this.patternAsText.iterator();

        while(patTextIterator.hasNext()){
            String line = patTextIterator.next();

            Iterator<LexicalMatch> mappingIterator = mapping.iterator();

            while(mappingIterator.hasNext()){
                LexicalMatch match = mappingIterator.next();
                String entity = match.getSource().getIRI().toString();

                Pattern entityPattern = Pattern.compile(entity);

                Matcher ma = entityPattern.matcher(line); 
                if(ma.find()){ 
                    //PERFORM SUBSTITUTION
                    line = line.replaceAll(entity, match.getAssociation().getIRI().toString());
                }
            }
            substitution.add(line);
        }
        return substitution; 
    }

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

   // public void writeSubstitutions(String outputPath, Set<List<LexicalMatch>> subs){
   //     this.outputPath = outputPath;
   //     int counter = 0;
   //     this.IOHelper.createFolder(outputPath + "/instance");
   //     this.IOHelper.createFolder(outputPath + "/substitution");

   //     for(List<LexicalMatch> sub : subs){
   //         counter++;

   //         Iterator<LexicalMatch> it = sub.iterator();
   //         if(it.hasNext()){ 
   //             LinkedList<String> instance = this.substitute(sub);
   //             this.IOHelper.writeApenndList(instance, outputPath + "/instance/" + counter); 
   //             this.writeSubstitutionInstance(sub, outputPath + "/substitution/" + counter);
   //         }
   //     } 
   // }

    public HashMap<Integer,List<LexicalMatch>> writeSubstitutions(String outputPath){
        this.outputPath = outputPath;
        int counter = 0;
        IOHelper.createFolder(outputPath + "/instance");
        //IOHelper.createFolder(outputPath + "/substitution");

        HashMap<Integer,List<LexicalMatch>> writingOrder = new HashMap();

        for(List<LexicalMatch> sub : this.substitutions){
            counter++;

            Iterator<LexicalMatch> it = sub.iterator();
            if(it.hasNext()){//just check whether sub is not empty here ...
                LinkedList<String> instance = this.substitute(sub);
                IOHelper.writeApenndList(instance, outputPath + "/instance/" + counter); 
                //this.writeSubstitutionInstance(sub, outputPath + "/substitution/" + counter);
            }
            writingOrder.put(new Integer(counter), sub);
        } 
        return writingOrder;
    }

    public void writeSubstitutionInstance(List<LexicalMatch> l, String outputPath){ 
            Iterator<LexicalMatch> subIterator = l.iterator();

            while(subIterator.hasNext()){
                LexicalMatch lm = subIterator.next();
                String sub = lm.getSource().getIRI().toString() + " --> " + lm.getAssociation().getIRI().toString(); 
                IOHelper.writeAppend(sub, outputPath); 
            } 
    }

    public boolean hasSubstitution(OWLEntity e){

        //for(Map.Entry<OWLEntity, Set<LexicalMatch>> entry : this.substitutionMappings.entrySet()){ 
        //    if(((OWLEntity) entry.getKey()).getIRI().toString().equals(e.getIRI().toString()))
        //        return true;
        //}
        //return false;
        return this.substitutionMappings.containsKey(e); 
    }

    public double meanSubstitutionsPerEntity(){
        if(this.substitutionMappings.isEmpty())
            return 0;

        double sum = 0;
        for(Map.Entry<OWLEntity, Set<LexicalMatch>> entry : this.substitutionMappings.entrySet()){ 
            sum += ((Set<LexicalMatch>) entry.getValue()).size();
        }

        return sum / this.substitutionMappings.keySet().size(); 
    }

    public int numberOfCoveredEntities(){ 
        return this.substitutionMappings.keySet().size();
    }

    public HashMap<OWLEntity, Set<LexicalMatch>> getSubstitutionMappings(){
        return this.substitutionMappings; 
    }

    private void parseNamespaceAbbreviations(File patFile) throws IOException { 

        Iterator<String> ontTextIterator = this.patternAsText.iterator();

        //group 6: abbreviation
        //group 7: IRI
        String namespaceInRDF = "^((<rdf:RDF\\s)|(\\s*))(xml(ns)?):(\\w*)=(.*)";


        //Pattern commentPattern = Pattern.compile(namespaceInComment);
        Pattern rdfPattern = Pattern.compile(namespaceInRDF);

        int lineNr = 0;


        while(ontTextIterator.hasNext()) {
             
            lineNr++;

            String line = ontTextIterator.next();

            Matcher ma2 = rdfPattern.matcher(line);
            if(ma2.find()){ 
                //System.out.println(lineNr + patFile.getName() + "Match: " + line);

                if(!this.rdfAbrv.containsKey(ma2.group(6))){
                    this.rdfAbrv.put(ma2.group(6), ma2.group(7));
                } else {
                    System.out.println(lineNr + patFile.getName() + " -- Clash! in RDF abbreviations: " + ma2.group(6));

                }
            }
        }
    }


}

