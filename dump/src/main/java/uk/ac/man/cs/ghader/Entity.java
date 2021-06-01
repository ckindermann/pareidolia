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
public class Entity {

    private String name; 
    private String conceptName; 
    private String preferredName;
    private String conceptID;

    private HashMap<String, Set<ClassMatch>> matches;

    public Entity(String n, String cn, String pn, String id) throws Exception { 
        this.name = n.toLowerCase();
        this.conceptName = cn.toLowerCase();
        this.preferredName = pn.toLowerCase();
        this.conceptID = id.toLowerCase();

        this.matches = new HashMap<>();

        //initialise hashMap
        matches.put(this.name, new HashSet<>());
        matches.put(this.conceptName, new HashSet<>());
        matches.put(this.preferredName, new HashSet<>());
        matches.put(this.conceptID, new HashSet<>());
    } 

    public Set<ClassMatch> getMatch(String key){
        return matches.get(key); 
    }

    public Set<ClassMatch> getMatches(){

        if(!matches.get(this.name).isEmpty()) 
            return matches.get(this.name);
        if(!matches.get(this.conceptName).isEmpty()) 
            return matches.get(this.conceptName);
        if(!matches.get(this.preferredName).isEmpty()) 
            return matches.get(this.preferredName);
        if(!matches.get(this.conceptID).isEmpty()) 
            return matches.get(this.conceptID); 

        return new HashSet<>();
    }

    public String getMatchType(){

        if(!matches.get(this.name).isEmpty()) 
            return "name";
        if(!matches.get(this.conceptName).isEmpty()) 
            return "conceptName";
        if(!matches.get(this.preferredName).isEmpty()) 
            return "preferredName";
        if(!matches.get(this.conceptID).isEmpty()) 
            return "conceptID";
        return null;
    }

    public String getName(){
        return this.name; 
    }
    public String getConceptName(){
        return this.conceptName; 
    }
    public String getPreferredName(){
        return this.preferredName; 
    }
    public String getConceptID(){
        return this.conceptID; 
    }

    public void addMatch(String typeOfMatch, ClassMatch m){
        this.matches.get(typeOfMatch).add(m); 
    }

    public String toString(){ 
        return "[" + this.name +
            "|" + this.conceptName +
            "|" + this.preferredName +
            "|" + this.conceptID + "]";
    }

    public boolean hasMatch(){
        if(!this.matches.get(this.name).isEmpty())
            return true;
        if(!this.matches.get(this.conceptName).isEmpty())
            return true;
        if(!this.matches.get(this.preferredName).isEmpty())
            return true;
        if(!this.matches.get(this.conceptID).isEmpty())
            return true;
        return false;
    }
}

