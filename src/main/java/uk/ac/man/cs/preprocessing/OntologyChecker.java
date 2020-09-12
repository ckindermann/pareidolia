package uk.ac.man.cs.preprocessing;

import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.ont.*;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import org.semanticweb.owlapi.model.IRI;
import java.io.IOException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.util.AutoIRIMapper;


/**
 *A class that tests whether an Ontology can be 
 *- loaded
 *- classified
 *- converted to RDF/XML format
 */

//tests if an ontology can be loaded and reasoned with

public class OntologyChecker {

    private File ontologyFile;
    private String outputPath;

    private OWLOntology ontology;
    private OWLReasoner reasoner;
    private OWLOntologyManager manager;

    private boolean loadable;
    private boolean classifiable;
    private boolean convertible;

    public OntologyChecker(String ontPath, String outPath){
        this.outputPath = outPath; 
        this.manager = OWLManager.createOWLOntologyManager();
        this.ontologyFile = new File(ontPath);

        this.loadable = false;
        this.classifiable = false;
        this.convertible = false;
    }

    public void check() throws Exception {
        this.loadable = isLoadable();
        if(loadable){
            this.classifiable = isClassifiable();
            this.convertible = isConvertible();
        } 
    }

    //rename to write results
    public String printResults(){ 
        //format:
        //ontologyName,loadbale,classifiable,convertible
        String res = this.ontologyFile.getName() + "," +
                     this.loadable + "," +
                     this.classifiable + "," +
                     this.convertible;

        IOHelper.writeAppend(res, outputPath + "/summary"); 

        return res; 
    }

    public boolean isLoadable(){

        try {
            this.ontology = this.manager.loadOntologyFromOntologyDocument(ontologyFile); 
        } catch (Exception e) {
            return false;
        } 
        return true;
    }

    public boolean isClassifiable(){
        try {
            reasoner = ReasonerLoader.initReasoner(this.ontology);
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        } catch (Exception e) {
            return false; 
        }
        return true; 
    }

    public boolean isConvertible() throws Exception {

        IOHelper.createFolder(outputPath+"/RDFXML"); 
        String file = this.outputPath + "/RDFXML/" + ontologyFile.getName();
        File output = new File(file);
        IRI documentIRI = IRI.create(output.toURI());

        try{
            manager.saveOntology(ontology, new RDFXMLDocumentFormat(), documentIRI); 
        } catch (Exception e){
            return false; 
        }
        return true;
    }
}
