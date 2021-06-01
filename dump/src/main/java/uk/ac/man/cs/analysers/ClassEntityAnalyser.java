package uk.ac.man.cs.analysers;

import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.lodp.*;
import uk.ac.man.cs.util.*;

import java.io.*;
import java.util.*;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.apibinding.OWLManager;
import java.util.Iterator;

import org.semanticweb.owlapi.model.*;

import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
//import org.semanticweb.owlapi.reasoner.OWLOntologyCreationException;
import org.semanticweb.owlapi.util.AbstractOWLStorer;
import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;

/**
 * The Dector defines the basic operations of a
 * pattern detctor.
 *
 * @author Christian Kindermann
 * @version 1.0
 * @since 2018-10-08
 *
 */
public class ClassEntityAnalyser {

    protected OWLOntology ontology;
    private Set<OWLAxiom> classAxioms;
    private Set<ClassAxiomCluster> clusters;

    public ClassEntityAnalyser(OWLOntology o){
        this.ontology = o;
        this.clusters = new HashSet<>(); 
        this.classAxioms = new HashSet<>(); 
        this.classAxioms.addAll(this.getClassAxioms());
    }

    public Set<ClassAxiomCluster> getClusters(){
        return this.clusters; 
    }

    public void run(){
        this.computerClusters(); 
    }

    private void computerClusters(){ 
        ;
    }

    public void printUsage(HashMap<OWLEntity, Set<OWLAxiom>> map){
        System.out.println("Size of hashmap " + map.size());
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            OWLEntity e = (OWLEntity) pair.getKey();
            Set<OWLAxiom> usage = map.get(e); 

            if(usage.size() > 5){
                System.out.println("On class: " + e.toString());
                System.out.println("usage: has " + usage.size());
                for(OWLAxiom a : usage){
                    System.out.println(a.toString()); 
                }
            }
                    
            it.remove(); // avoids a ConcurrentModificationException
        }

    }

    public HashMap<OWLEntity, Set<OWLAxiom>> entity2usage(){
        Set<OWLAxiom> classAxioms = getClassAxioms();
        Set<OWLEntity> signature = getSignature(classAxioms);
        HashMap<OWLEntity, Set<OWLAxiom>> entity2usage = new HashMap<>();

        for(OWLEntity e : signature){
            HashSet<OWLAxiom> usage = new HashSet<>();
            for(OWLAxiom a : classAxioms){
                if(a.getSignature().contains(e))
                    usage.add(a); 
            } 
            entity2usage.put(e, usage);
        }

        return entity2usage;
    }


    private Set<OWLEntity> getSignature(Set<OWLAxiom> axioms){
        Set<OWLEntity> result = new HashSet<>();
        for(OWLAxiom axiom : axioms){
            result.addAll(axiom.getSignature()); 
        }
        return result; 
    }


    private Set<OWLAxiom> getClassAxioms(){
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);
        Set<OWLAxiom> classAxioms = new HashSet<>();

        for(OWLAxiom axiom : tBox){ 
            if(axiom instanceof OWLClassAxiom){
                classAxioms.add(axiom);
            }
        } 
        return classAxioms;
    }

    private boolean hasCluster(OWLAxiom a){
        for(ClassAxiomCluster cluster : this.clusters){
            if(cluster.contains(a))
                return true;
        } 
        return false;
    }

    private ClassAxiomCluster generateCluster(OWLAxiom a){ 
        //get k
        ClassAxiomCluster cluster = new ClassAxiomCluster(a);
        return getFixpoint(cluster); 
    }

    private ClassAxiomCluster getFixpoint(ClassAxiomCluster cluster){
        int iterations = 0;
        //while(!cluster.isFixpoint() && iterations < 1){
        while(!cluster.isFixpoint()){
            iterations++;
            expand(cluster); 
        }
        return cluster; 
    }

    public void expand(ClassAxiomCluster cluster){
            //do one expansion set on signature
            Set<OWLEntity> entities = new HashSet<>();
            entities.addAll(cluster.getAllExpansionPoints()); 

            for(OWLEntity entity : entities){
                cluster.add(entityUsage(entity)); 
                cluster.addExpansionPoint(entity);
            }
    }

    public boolean checkClustering(){
        HashSet<ClassAxiomCluster> help = new HashSet<>();
        help.addAll(this.clusters);

        for(ClassAxiomCluster cluster1 : this.clusters){
            Set<OWLEntity> intersection = new HashSet<OWLEntity>(cluster1.getSignature());
            help.remove(cluster1);
            for(ClassAxiomCluster cluster2 : help){
                intersection.retainAll(cluster2.getSignature());
                if(!intersection.isEmpty())
                    return false; 
            } 
        } 
        return true;
    }


    //protected HashMap<OWLEntity,Set<OWLAxiom>> axiomUsage(OWLAxiom a){
    //    HashMap<OWLEntity,Set<OWLAxiom>> res = new HashMap<>();
    //    for(OWLEntity e : a.getSignature()){
    //        res.put(e, entityUsage(e));
    //    }
    //    return res; 
    //}

    protected Set<OWLAxiom> axiomUsage(OWLAxiom a){
        Set<OWLAxiom> axiomsWithSharedSignature = new HashSet<>();
        Set<OWLEntity> aSig = a.getSignature();
        for(OWLEntity e : aSig){
            Set<OWLAxiom> usage = entityUsage(e);
            axiomsWithSharedSignature.addAll(usage); 
        } 
        return axiomsWithSharedSignature;
    }

    protected Set<OWLAxiom> entityUsage(OWLEntity entity){
        Set<OWLAxiom> res = new HashSet<>(); 
        //for(OWLAxiom axiom : ontology.getAxioms(Imports.EXCLUDED)){
        for(OWLAxiom axiom : this.classAxioms){
            if((axiom.getSignature()).contains(entity)){
                    res.add(axiom);
            }
        }
        return res; 
    }

    protected Set<OWLAxiom> getSubsumptionAxioms(){ 
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);

        Set<OWLAxiom> subClassAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.SUBCLASS_OF); 
        Set<OWLAxiom> subsumptionAxioms = new HashSet<>(); 
        subsumptionAxioms.addAll(subClassAxioms);

        Set<OWLAxiom> equivAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.EQUIVALENT_CLASSES);
        for(OWLAxiom a : equivAxioms){
            subsumptionAxioms.addAll(((OWLEquivalentClassesAxiom) a).asOWLSubClassOfAxioms()); 
        } 
        return subsumptionAxioms;
    }

    public void printClusters(){
        int i = 1;
        for(ClassAxiomCluster cluster : this.clusters){
            System.out.println("Cluster: " + i + " has size " + cluster.getSize());
            cluster.print(); 
            i++;
        } 
    }

    public void printClassAxioms(){
        System.out.println("Class Axioms: " + getClassAxioms()); 
        for(OWLAxiom a : getClassAxioms())
            System.out.println("Signautre: " + a.getSignature());

    }

}

