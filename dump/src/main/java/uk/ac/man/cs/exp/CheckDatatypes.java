package uk.ac.man.cs.exp;

import uk.ac.man.cs.codp.*;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.data.*;
import uk.ac.man.cs.util.*;
import java.io.*;
import java.util.*;
import org.semanticweb.owlapi.model.*;
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
public class CheckDatatypes {

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));

    public static void main(String[] args) throws IOException , Exception
    {

        String ontDirPath = args[0];//ontologies to be searched
        String outputPath = args[1];//ontologies to be searched
        CheckDatatypes exp = new CheckDatatypes();
        System.out.println("Logical + Declaration " + AxiomType.LOGICAL_AXIOMS_AND_DECLARATIONS_TYPES.toString());
        exp.doSomeReasoning(ontDirPath, outputPath);
    }

    private void doSomeReasoning(String ontDirPath, String outputPath) throws IOException, Exception {
        IOHelper io = new IOHelper();

        File ontDir = new File(ontDirPath);
        int ontCount = 0;

        Set<File> reasonerProblems = new HashSet<>(); 

        for (File ontFile : ontDir.listFiles()) {

            log.info("\tLoading Ontology " + ++ontCount + " : " + ontFile.getName());
            OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
            OWLOntology ont = ontLoader.getOntology(); 

            OWLAxiom axiom = null;
            for(OWLAxiom a : ont.getAxioms()){
                axiom = a; 
            }



            try{
                OWLReasoner reasoner = ReasonerLoader.initReasoner(ont);
                reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            } catch (Exception e){
                reasonerProblems.add(ontFile);
                io.writeAppend(ontFile.getName(), outputPath); 
            } 
        }

        System.out.println("Ontologies with problems");
        for(File f : reasonerProblems){

            System.out.println(f.getName()); 
        }
    }
}
