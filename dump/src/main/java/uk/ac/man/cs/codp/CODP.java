package uk.ac.man.cs.codp;

import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.util.IOHelper;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.semanticweb.owlapi.model.OWLEntity;
import static java.util.stream.Collectors.toSet;

import java.io.FileReader;
import java.util.HashSet;
import java.io.BufferedReader;
import java.util.List;
import java.io.IOException;
import java.util.Set;
import java.util.stream.*;

import java.io.File;

/**
 * Created by chris on 06/04/18.
 */
public class CODP {

    private String name;
    private OWLOntology ontology;
    private Stream<OWLEntity> signature;
    private Set<String> excludeSet;
    private IOHelper io = new IOHelper();

    /**
     * Constructors
     */


    public CODP(String n, File ontologyFile){
       this.name = n; 

       //initialise elements from signature to be exluded
       this.initialiseExcludeSet();

       OntologyLoader loader = new OntologyLoader(ontologyFile, false);
       this.ontology = loader.getOntology();
       this.setSignature();

    }

    public OWLOntology getOntology(){
        return this.ontology; 
    }

    private void initialiseExcludeSet(){
       this.excludeSet = new HashSet<>(); 
       //put this in an external file?
       this.excludeSet.add("label");
       this.excludeSet.add("literal");
       this.excludeSet.add("langstring");
       this.excludeSet.add("string");
       this.excludeSet.add("versioninfo");
       this.excludeSet.add("thing");
       this.excludeSet.add("plainliteral");
       this.excludeSet.add("comment");
       this.excludeSet.add("hasintent");
       this.excludeSet.add("hasconsquences");
       this.excludeSet.add("hasauthor");
       this.excludeSet.add("extractedfrom");
       this.excludeSet.add("relatedcps");
       this.excludeSet.add("reengineeredfrom");
       this.excludeSet.add("coversrequirements");
       this.excludeSet.add("isdefinedby");
       this.excludeSet.add("isabout");
       this.excludeSet.add("isspecializationof"); 
    }

    private void setSignature(){
        this.signature = this.ontology.signature().filter(e ->
                !this.excludeSet.contains(e.getIRI()
                                            .getShortForm()
                                            .toLowerCase()));
    }

    public Set<OWLEntity> getSignatureAsSet(){
        if(this.signature == null)
            setSignature(); 
        return this.signature.collect(Collectors.toSet());
    }

    public Stream<OWLEntity> getSignatureAsStream(){
        if(this.signature == null)
            setSignature(); 
        return this.signature;

    }

    public Set<String> extractKeys() throws IOException {

        Set<String> terms = new HashSet<>();
        Set<OWLEntity> entities = this.ontology.getSignature();
        for(OWLEntity e : entities){
            if(!excludeSet.contains(e.getIRI().getShortForm().toLowerCase()))
                terms.add(e.getIRI().getShortForm());
        }

        return terms;
    }

    //remove
    //TODO: get rid of this
    public Set<String> getKeys(){
        return null; 
    }

    public CODP(String n, String keyFile) throws IOException {
        this.name = n;
       this.signature = null;
        this.ontology = null;
    }

    public CODP(File keyFile) throws IOException {
        this.name = null;
       this.signature = null;
        this.ontology = null;
    }

    public OWLOntology getOWLArtefact() {
        return this.ontology;
    }

    public void setOWLArtefact(OWLOntology artefact){
        this.ontology = artefact; 
    }
}
