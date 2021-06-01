package uk.ac.man.cs.ont;

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


//tests if an ontology can be loaded and reasoned with

public class OntologyChecker {

    private String ontologyPath;
    private File ontologyFile;
    private String outputPath;

    private OWLOntology ontology;
    private OWLReasoner reasoner;
    private OWLOntologyManager manager;

    public OntologyChecker(String ontPath, String outPath){
        this.ontologyPath = ontPath;
        this.outputPath = outPath; 
        this.manager = OWLManager.createOWLOntologyManager();
        this.ontologyFile = new File(this.ontologyPath);
    }

    public void check(){
        if(loadable()){
            IOHelper.writeAppend(this.ontologyFile.getName(), outputPath + "/loadable"); 
            if(reasonable()){
                IOHelper.writeAppend(this.ontologyFile.getName(), outputPath + "/reasonable"); 
            }
            if(convertable()){
                IOHelper.writeAppend(this.ontologyFile.getName(), outputPath + "/convertable"); 
            }
        } 
    }

    public boolean loadable(){

        try {
            this.ontology = this.manager.loadOntologyFromOntologyDocument(ontologyFile); 
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        } 
        return true;
    }

    public boolean reasonable(){
        try {
            reasoner = ReasonerLoader.initReasoner(this.ontology);
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        } catch (Exception e) {
            return false; 
        }
        return true; 
    }

    public boolean convertable(){

        IOHelper.createFolder(outputPath+"/RDFXML"); 
        String file = this.outputPath + "/RDFXML/" + ontologyFile.getName();
        File output = new File(file);
        IRI documentIRI2 = IRI.create(output.toURI());

        try{
            //m.saveOntology(ontology, new OWLXMLDocumentFormat(), documentIRI2);
            manager.saveOntology(ontology, new RDFXMLDocumentFormat(), documentIRI2); 
        } catch (Exception e){
            return false; 
        }
        return true;
    }
}
