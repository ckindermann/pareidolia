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
public class ClassAxiomClusterGenerator {

    protected OWLOntology ontology;
    private Set<OWLAxiom> classAxioms;
    private Set<ClassAxiomCluster> clusters;

    //first tier
    private int OWLClassAxiom; 
    //second tier
    private int OWLDisjointUnionAxiom;
    private int OWLSubClassOfAxiom;
    private int OWLNaryClassAxiom;
    //third tier
    private int OWLEquivalentClassesAxiom;
    private int OWLDisjointClassesAxiom;

    public ClassAxiomClusterGenerator(OWLOntology o){
        this.ontology = o;
        this.clusters = new HashSet<>(); 
        this.classAxioms = new HashSet<>(); 
        this.classAxioms.addAll(this.getClassAxioms());

        OWLClassAxiom = 0;
        OWLDisjointUnionAxiom = 0;
        OWLSubClassOfAxiom = 0;
        OWLNaryClassAxiom = 0;
        OWLEquivalentClassesAxiom = 0;
        OWLDisjointClassesAxiom = 0;
    }

    public Set<ClassAxiomCluster> getClusters(){
        return this.clusters; 
    }

    public void run(){
        this.computerClusters(); 
    }

    private void computerClusters(){ 
        Set<OWLAxiom> classAxioms = getClassAxioms();
        for(OWLAxiom a : classAxioms){
            if(!hasCluster(a)){
                ClassAxiomCluster cluster = generateCluster(a);
                this.clusters.add(cluster);
            }
        } 
    }

    public void printClassAxioms(){
        System.out.println("Class Axioms: " + getClassAxioms()); 
        for(OWLAxiom a : getClassAxioms())
            System.out.println("Signautre: " + a.getSignature());

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

    //private ClassAxiomCluster getFixpointHeuristic(ClassAxiomCluster cluster){ 
    //    if(!cluster.isFixpoint()){ 
    //        OWLAxiom axiom = cluster.getExpansionPoint();
    //        cluster.add(axiomUsage(axiom));
    //        getFixpointHeuristic(cluster);
    //    } 
    //    return cluster;
    //}




    //recursive
    //private ClassAxiomCluster getFixpoint(ClassAxiomCluster cluster, Set<OWLEntity> prevSig){
    //    if(!cluster.isFixpoint(prevSig)){ 
    //        Set<OWLAxiom> axioms = cluster.getAllExpansionPoints();
    //        for(OWLAxiom axiom : axioms){
    //            cluster.add(axiomUsage(axiom));
    //            prevSig.addAll(axiom.getSignature());
    //        } 
    //        getFixpoint(cluster, prevSig);
    //    } 
    //    return cluster; 
    //}

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
}

