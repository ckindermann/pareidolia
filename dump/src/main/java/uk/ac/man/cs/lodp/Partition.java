package uk.ac.man.cs.lodp;

import uk.ac.man.cs.util.Pair;

import org.semanticweb.owlapi.model.*;
import java.io.*;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Created by chris on 08/08/18.
 */
public class Partition extends LODP {

    //native axioms "disjointUnionOf"
    private Set<OWLAxiom> disjointUnionAxioms;

    private Set<OWLAxiom> disjointClassesAxioms;
    private Set<OWLAxiom> partitionAxioms; 
    private Set<String> namedPartitions;

    private Set<OWLAxiom> allDifferenAxioms;
    private Set<OWLAxiom> partitionByIndividualsAxioms;

    private Set<Pair<OWLAxiom,OWLAxiom>> restrictionToElementOfPartition;


    private OWLDataFactory factory;
    private String description;

    public Partition(OWLOntology o){
        this.ontology = o; 
        this.evidence = new HashSet<>();

        this.disjointUnionAxioms = new HashSet<>();

        this.allDifferenAxioms = new HashSet<>();
        this.partitionByIndividualsAxioms = new HashSet<>();

        this.partitionAxioms = new HashSet<>();
        this.disjointClassesAxioms = new HashSet<>();
        this.namedPartitions = new HashSet<>();

        this.restrictionToElementOfPartition = new HashSet<>();

        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        this.description = "The pattern 'Partition' " +
               "consists of a named class for a set of disjoint classes or individuals."; 
    }

    /**
     * Computation
     */

    private void computeDisjointAxioms(){
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.INCLUDED);
        this.disjointUnionAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.DISJOINT_UNION);
        this.disjointClassesAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.DISJOINT_CLASSES); 

        if(!disjointUnionAxioms.isEmpty()){
            for(OWLAxiom a : this.disjointUnionAxioms){
                Set<OWLAxiom> partitionInstance = new HashSet<>();
                partitionInstance.add(a);
                this.evidence.add(patternInstance("Disjoint Union Axiom", partitionInstance));
            } 
        }
    }

    private void computeAllDifferent(){
        Set<OWLAxiom> aBox = this.ontology.getABoxAxioms(Imports.INCLUDED);
        this.allDifferenAxioms = AxiomType.getAxiomsOfTypes(aBox, AxiomType.DIFFERENT_INDIVIDUALS);

        if(!allDifferenAxioms.isEmpty()){
            for(OWLAxiom a : this.allDifferenAxioms){
                Set<OWLAxiom> partitionInstance = new HashSet<>();
                partitionInstance.add(a);
                this.evidence.add(patternInstance("Different Individuals", partitionInstance));
            } 
        }
    }

    public void computeRestrictions(){
        Set<OWLAxiom> valuePartitions = new HashSet<>();
        valuePartitions.addAll(this.partitionAxioms);
        valuePartitions.addAll(this.partitionByIndividualsAxioms);
        for (OWLAxiom a : valuePartitions){
            for(OWLEntity e : a.getSignature()){
                for(OWLAxiom u : usage(e)){
                    for(OWLClassExpression c : u.getNestedClassExpressions()){
                        if(c instanceof OWLRestriction){
                            this.restrictionToElementOfPartition.add(new Pair<OWLAxiom,OWLAxiom>(u,a)); 
                        } 
                    } 
                } 
            } 
        } 
    }

    //"logical pattern": partition
    //Def. for Partition P: P = disjointUnion(A,...,Z)
    private void computePartition(){
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.INCLUDED);

        for(OWLAxiom a : tBox){ //look at all axioms in TBox
            if(a.isOfType(AxiomType.EQUIVALENT_CLASSES)){//that are definitorial
                for(OWLAxiom d : this.disjointClassesAxioms){//check whether..
                    Set<OWLEntity> as = a.getSignature();
                    Set<OWLEntity> ds = d.getSignature();
                    if(as.containsAll(ds) && //..a disjoint union is part of the equivalency
                            as.size() == ds.size() + 1){//the signatures differ by 1

                        //difference by 1 of signatures is only an indication..
                        //..lets verify this indication
                        if(checkPartitionAxiom(a,d)){
                            partitionAxioms.add(a);
                            Set<OWLAxiom> partitionInstance = new HashSet<>();
                            partitionInstance.add(a);
                            this.evidence.add(patternInstance("Union of disjoint classes", partitionInstance));
                        }
                    }
                }
                for(OWLAxiom d : this.allDifferenAxioms){
                    Set<OWLEntity> as = a.getSignature();
                    Set<OWLEntity> ds = d.getSignature();
                    if(as.containsAll(ds) && //..a disjoint union is part of the equivalency
                            as.size() == ds.size() + 1){//the signatures differ by 1
                        if(isOneOf(a,d)){
                            partitionByIndividualsAxioms.add(a);
                            Set<OWLAxiom> partitionInstance = new HashSet<>();
                            partitionInstance.add(a);
                            this.evidence.add(patternInstance("Partition by idividuals", partitionInstance)); 
                        }
                    } 
                }
            }
        }
    }

    //axiom d: disjoint axiom for a set of classes
    //axiom a: axiom that contains the signature of d PLUS one more term
    //check whether axiom a introduces a named class for d
    private boolean checkPartitionAxiom(OWLAxiom a, OWLAxiom d){
        //get signatures of axioms
        Set<OWLEntity> as = a.getSignature();
        Set<OWLEntity> ds = d.getSignature();

        //convert entities to classes
        Set<OWLClass> classes = new HashSet<>();
        for(OWLEntity e : ds){
            if(e.isOWLClass())
                classes.add(e.asOWLClass()); 
        }

        //get name for the partition to be tested
        OWLClass name = null;
        for(OWLEntity e : as){
            if(!ds.contains(e) && e.isOWLClass())
                name = e.asOWLClass();
        }

        if(!classes.isEmpty() && name != null){

            //construct axiom for a named partition
            OWLObjectUnionOf union = this.factory.getOWLObjectUnionOf(classes);
            OWLEquivalentClassesAxiom equiv = this.factory.getOWLEquivalentClassesAxiom(name, union);
            //test wheather the constructed axiom is equal to a
            if(a.equalsIgnoreAnnotations(equiv)){
                namedPartitions.add(name.toString()); 
                return true;
            }
        }
        return false;
    }

    private boolean isOneOf(OWLAxiom a, OWLAxiom d){
        Set<OWLEntity> as = a.getSignature();
        Set<OWLEntity> ds = d.getSignature();

        Set<OWLIndividual> individuals = new HashSet<>();
        for(OWLEntity e : ds){
            if(e.isOWLNamedIndividual())
                individuals.add(e.asOWLNamedIndividual()); 
        }

        OWLClass name = null;
        for(OWLEntity e : as){
            if(!ds.contains(e) && e.isOWLClass())
                name=e.asOWLClass(); 
        }

        if(!individuals.isEmpty() && name != null){
            OWLObjectOneOf oneof = this.factory.getOWLObjectOneOf(individuals);
            OWLEquivalentClassesAxiom equiv = this.factory.getOWLEquivalentClassesAxiom(name, oneof);
            if(a.equalsIgnoreAnnotations(equiv)){
                return true; 
            }

        }
        return false;

    }

    public void computeEvidence(){
        this.reset(); 
        this.computeDisjointAxioms();
        this.computeAllDifferent();
        this.computePartition();
        this.computeRestrictions(); 
    }

    public Set<Pair<String, Set<OWLAxiom>>> getEvidence(){
        return this.evidence;
    }

    public void reset(){
        this.disjointUnionAxioms.clear();
        this.disjointClassesAxioms.clear();
        this.namedPartitions.clear();
        this.allDifferenAxioms.clear();
        this.partitionAxioms.clear();
        this.partitionByIndividualsAxioms.clear();
        this.restrictionToElementOfPartition.clear();
        this.evidence.clear();
    }

    public void setOntology(OWLOntology o){
        this.ontology = o;
        this.reset();
    }
    public boolean writeEvidence(String destFile){

        createFolder(destFile);
        String evidenceFile = destFile + "/evidence"; 
        if(!this.evidence.isEmpty()){
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(evidenceFile));
                for(Pair<String, Set<OWLAxiom>> p : this.evidence){
                    bw.write(p.getFirst() + " : " + p.getSecond().toString());
                    bw.newLine();
                }
                bw.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        writeSet(this.disjointUnionAxioms, destFile + "/disjointUnion");
        writeSet(this.partitionAxioms, destFile + "/unionOfDisjointClasses");
        writeSet(this.partitionByIndividualsAxioms, destFile + "/partitionByIndividuals");
        writeSet(this.allDifferenAxioms, destFile + "/differentIndividuals");


        //writeSet(this.restrictionToElementOfPartition, destFile + "/restrictios");

        
        String restrictionFile = destFile + "/restrictions"; 
        if(!this.restrictionToElementOfPartition.isEmpty()){
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(restrictionFile));
                for(Pair<OWLAxiom,OWLAxiom> a : this.restrictionToElementOfPartition){
                    bw.write(a.toString());
                    bw.newLine();
                }
                bw.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Usage(e): set of axioms an entity e occurrs in
     */
    private Set<OWLAxiom> usage(OWLEntity entity){

        //set of axioms containing the pattern
        Set<OWLAxiom> entityUsage = new HashSet<>();

        for(OWLAxiom axiom : this.ontology.getAxioms(Imports.INCLUDED)){
            if((axiom.getSignature()).contains(entity)){
                    entityUsage.add(axiom);
            }
        }
        return entityUsage;
    } 

    private void writeSet(Set<OWLAxiom> set, String output){
        if(!set.isEmpty()){
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(output));
                for(OWLAxiom a : set){
                    bw.write(a.toString());
                    bw.newLine();
                }
                bw.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } 
    }

    public void printDescription(){
        System.out.println(this.description);
    } 

    private void createFolder(String path){
        File destDir = new File(path);
        destDir.mkdirs(); 
    }

    public String getDescription(){
        return this.description;
    }
}
