package uk.ac.man.cs.ont;

import org.semanticweb.owlapi.util.*;
import java.io.*;
import java.util.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import org.semanticweb.owlapi.search.EntitySearcher;

import java.io.File;
import java.util.*;
import java.util.stream.*;

/**
 * Created by chris on 12/09/17.
 */
public class Ontology {

    private OWLOntology ontology;
    private Set<String> imports;
    private Set<OWLEntity> signature;

    private Set<OWLAxiom> axioms;
    private Set<OWLLogicalAxiom> logicalAxioms;
    private Set<OWLSubClassOfAxiom> subsumptionAxioms;
    private Set<OWLEquivalentClassesAxiom> equivalenceAxioms;
    private Set<OWLDeclarationAxiom> declarationAxioms;
    private Set<OWLAnnotationAxiom> annotationAxioms; 

    private Map<OWLEntity, Set<OWLAxiom>> entity2Usage;

    public Ontology (OWLOntology o) {
        this.ontology = o;
        this.signature = this.ontology.getSignature();

        this.axioms = new HashSet<>();
        this.logicalAxioms = new HashSet<>();
        this.subsumptionAxioms = new HashSet<>();
        this.equivalenceAxioms = new HashSet<>();
        this.declarationAxioms = new HashSet<>();
        this.annotationAxioms = new HashSet<>();

        this.initialise();
    } 

    public Ontology (File ontFile) throws Exception {
        this(OntologyLoader.load(ontFile));
    }

    public Ontology (String ontFilePath) throws Exception {
        this(new File(ontFilePath));
    } 

    private void initialise(){
        this.axioms = this.ontology.getAxioms(Imports.INCLUDED);//1
        this.logicalAxioms = this.ontology.getLogicalAxioms(Imports.INCLUDED);//2
        for(OWLAxiom a : this.axioms){
            if(a instanceof OWLDeclarationAxiom)
                this.declarationAxioms.add((OWLDeclarationAxiom) a);
            if(a instanceof OWLAnnotationAxiom)
                this.annotationAxioms.add((OWLAnnotationAxiom) a); 
            if(a instanceof OWLSubClassOfAxiom)
                this.subsumptionAxioms.add((OWLSubClassOfAxiom) a);
            if(a instanceof OWLEquivalentClassesAxiom)
                this.equivalenceAxioms.add((OWLEquivalentClassesAxiom) a);
        }
    }

    public OWLOntology getOntology() {
        return ontology;
    }

    public Set<OWLEntity> getSignature(){
        return this.signature;
    }

    public Set<OWLAxiom> getAxioms(){
        return this.axioms; 
    }

    public Set<OWLLogicalAxiom> getLogicalAxioms(){
        return this.logicalAxioms; 
    }

    public Set<OWLSubClassOfAxiom> getSubsumptions(){
        return this.subsumptionAxioms; 
    }

    public Set<OWLEquivalentClassesAxiom> getEquivalenceAxioms(){
        return this.equivalenceAxioms; 
    }

    public Set<OWLDeclarationAxiom> getDeclarationAxioms(){
        return this.declarationAxioms; 
    }

    public Set<OWLAnnotationAxiom> getAnnotationAxioms(){
        return this.annotationAxioms; 
    }

    private boolean checkEntity(OWLEntity entity){
        return (this.ontology.getSignature().contains(entity));
    }

    public Set<String> getImportClosure(){
        if(this.imports.isEmpty())
            this.computeImportClosure();
        return this.imports;
    }
    
    private void computeImportClosure(){
        Set<OWLOntology> importClosure = this.ontology.getImportsClosure();
        for(OWLOntology o : importClosure){
            if(o.getOntologyID().getOntologyIRI().isPresent()){ 
                this.imports.add(o.getOntologyID().getOntologyIRI().get().toString()); 
            }
        }
    } 

    private void init(){ 
        this.declarationAxioms = new HashSet<>();
        this.annotationAxioms = new HashSet<>();
        this.logicalAxioms = new HashSet<>();

        for(OWLAxiom a : this.ontology.getAxioms(true)){
            if(a instanceof OWLLogicalAxiom)
                this.logicalAxioms.add((OWLLogicalAxiom) a);
            if(a instanceof OWLDeclarationAxiom)
                this.declarationAxioms.add((OWLDeclarationAxiom) a);
            if(a instanceof OWLAnnotationAxiom)
                this.annotationAxioms.add((OWLAnnotationAxiom) a);
        } 
    }

    private void initUsage(){
        for(OWLEntity e : this.ontology.getSignature()){
            Set<OWLAxiom> usage = this.entityUsage(e);
            this.entity2Usage.put(e, usage); 
        }
    }

    public Set<OWLLogicalAxiom> getLogicalAxioms(boolean includeClosure) {
        return this.logicalAxioms;
    } 

    public Set<OWLAxiom> entityUsage(OWLEntity entity){
        Set<OWLAxiom> axioms = this.ontology.getAxioms(true);
        Set<OWLAxiom> res = new HashSet<>(); 
        //for(OWLAxiom axiom : ontology.getAxioms(Imports.EXCLUDED)){
        for(OWLAxiom axiom : axioms){
            if((axiom.getSignature()).contains(entity)){
                    res.add(axiom);
            }
        }
        return res; 
    } 

    //public boolean isWithinEL(){ 
    //    Set<OWLOntology> ontSet = new HashSet<>();
    //    ontSet.add(this.ontology);
    //    DLExpressivityChecker checker = new DLExpressivityChecker(ontSet);
    //    if(checker.isWithin(Languages.ELPLUSPLUS))
    //        return true;
    //    return false;
    //}

}
