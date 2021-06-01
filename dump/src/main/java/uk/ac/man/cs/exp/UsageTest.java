package uk.ac.man.cs.exp;

import uk.ac.man.cs.codp.*;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.stats.*;
import uk.ac.man.cs.data.*;
import uk.ac.man.cs.usage.*;
import uk.ac.man.cs.util.*;
import java.io.*;
import java.util.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.HermiT.ReasonerFactory;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;

        //long starTime = System.nanoTime();
        //long endTime = System.nanoTime();
        //double duration = (endTime - starTime) / 1000000000.0;
        //System.out.println("Timing Cartesian: " + duration);
/**
 * Created by chris on 03/09/18.
 */

//this is not a check, this is an analysis!
public class UsageTest {

    public static void main(String[] args) throws IOException , Exception
    {

        File ontDir = new File(args[0]);//ontologies to be searched
        System.out.println("HI");

        String ontDirPath = args[0];
        UsageTest exp = new UsageTest();
        exp.computeUsage(args[0]);
        //for(AxiomType<?> t : AxiomType.LOGICAL_AXIOM_TYPES){
        //    System.out.println(t.toString()); 
        //}
    }

    private void computeUsage(String ontologyPath){ 
        File ontFile = new File(ontologyPath);
            OntologyLoader ontLoader = new OntologyLoader(ontFile, false);
            OWLOntology ont = ontLoader.getOntology();

            Set<Fingerprint> fps = new HashSet<>();

            for(OWLEntity e : ont.getSignature()){
                Fingerprint fp = new Fingerprint(e, ont);
                fps.add(fp);
            }

            Set<Fingerprint> equiv = new HashSet<>();

            for(Fingerprint fpOrig : fps){
                boolean found = false;
                for(Fingerprint fpEquiv : equiv){
                    if(fpOrig.equals2(fpEquiv)){
                        fpEquiv.similar.add(fpOrig.entity);
                        fpEquiv.equivalent += 1; 
                        found = true;
                    }
                }
                if(!found)
                    equiv.add(fpOrig); 
            }

            System.out.println(fps.size());
            System.out.println(equiv.size());

            for(Fingerprint fp : equiv){
                System.out.println("NUMBER: " + fp.equivalent); 
                fp.print();
            }
    }

    private Set<OWLAxiom> usage(OWLOntology ontology, OWLEntity entity){

        //set of axioms containing the pattern
        Set<OWLAxiom> entityUsage = new HashSet<>();

        for(OWLAxiom axiom : ontology.getAxioms(Imports.EXCLUDED)){
            if((axiom.getSignature()).contains(entity)){
                    entityUsage.add(axiom);
            }
        }
        return entityUsage;
    } 

}
