package uk.ac.man.cs.metrics;

import uk.ac.man.cs.codp.CODP;

import org.semanticweb.owlapi.model.OWLOntology;
import java.util.HashSet;
import java.util.Set;
import java.io.*;

/** Holds information about the import closure of an ontology
 *
 * @author Chris  on 02/08/18.
 *
 */

public class ImportMetric extends Metric {

    private Set<String> imports;//set of imported ontologies

    public ImportMetric(OWLOntology o){
        this.ontology = o;
        this.imports = new HashSet<>(); 
    }

    //implement abstract methods for Metric
    public void compute(){
        this.computeImportClosure(); 
    }

    public void write(String destFile){
        writeImportClosure(destFile + "/ImportClosure");
    }

    public void reset(){
        this.imports.clear(); 
    }

    public void setOntology(OWLOntology o){
        this.ontology = o;
        this.reset(); 
    }


    //implement functions specific for imports
    
    /**
     * Computes all imported ontologies
     */
    private void computeImportClosure(){
        Set<OWLOntology> importClosure = this.ontology.getImportsClosure();
        for(OWLOntology o : importClosure){
            if(o.getOntologyID().getOntologyIRI().isPresent()){ 
                this.imports.add(o.getOntologyID().getOntologyIRI().get().toString()); 
            }
        }
    }

    //tests whether the name of an uppler level ontology "ulo"
    //is in the import closure
    public boolean includesULO(String ulo){
        for(String s : imports){
            if(stringCompare(s, ulo))
                return true;
        }
        return false; 
    }

    //tests whether the given pattern p (as an ontology)
    //is in the import closure
    public boolean includesPattern(CODP p){
        OWLOntology OWLArtefact = p.getOWLArtefact();
        if(OWLArtefact != null){
            String reuse = OWLArtefact.getOntologyID().getOntologyIRI().get().toString();
            for(String s : imports){
                if(stringCompare(s, reuse))
                    return true;
            }
        } 
        return false; 
    }


    private void writeImportClosure(String destFile){
        if(!this.imports.isEmpty()){
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(destFile));

                for(String s : imports){
                    bw.write(s);
                    bw.newLine(); 
                }

                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } 
    }

    public Set<String> getImports(){
        return this.imports; 
    }

    private boolean stringCompare(String a, String b){
        return a.toLowerCase().contains(b.toLowerCase()); 
    } 
}
