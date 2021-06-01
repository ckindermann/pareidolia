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
public class GhaderUsage {

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));

    public static void main(String[] args) throws IOException , Exception
    {


        //File ontDir = new File(args[0]);//ontologies to be searched
        String ontDirPath = args[0]; 
        File csvFile = new File(args[1]);
        String outputPath = args[2];

        GhaderUsage gUsage = new GhaderUsage();

        LinkedList<String[]> test = gUsage.parseCSV(csvFile); 

        gUsage.computeUsage(test, ontDirPath, outputPath);


        //UsageTest exp = new UsageTest();
        //exp.computeUsage(args[0]);
        //for(AxiomType<?> t : AxiomType.LOGICAL_AXIOM_TYPES){
        //    System.out.println(t.toString()); 
        //}
    }

    private void computeUsage(LinkedList<String[]> data, String ontDirPath, String outputPath){

        File ontFile = new File(ontDirPath);

        //File ontDir = new File(ontDirPath);
        //for (File ontFile : ontDir.listFiles()) {

            OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
            OWLOntology ont = ontLoader.getOntology();

            log.info("\tLoading Ontology : " + ontFile.getName() + " " + ont.getSignature().size());


            Iterator<String[]> it = data.iterator();

            String [] line;
            while(it.hasNext()){
                line = it.next();

                String ontName = line[1];
                String relation = line[2];
                String relationId = line[3]; 

                if(ontFile.getName().equals(ontName)){
                    for(OWLEntity e : ont.getSignature()){
                        String output = outputPath + "/" + ontName;
                        //if(relationId.equals(e.getIRI().toString()))
                        if(relationId.equals(e.toStringID())){
                            IOHelper.createFolder(output);
                            output += "/" + relation;
                            for(OWLAxiom a : usage(ont, e)){
                                IOHelper.writeAppend(a.toString(), output); 
                            }
                            //System.out.println("MATCH ID " + relationId); 
                            //System.out.println(usage(ont, e));
                        }
                        //if(e.toString().contains(relation))
                            //System.out.println("MATCH Substring " + relation + " " + e.toStringID() + " "  + relationId); 
                    } 
                }
            }
        //}
    }

    private LinkedList<String[]> parseCSV(File file) throws IOException{

        LinkedList<String[]> res = new LinkedList<String[]>(); 

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String [] parts = line.split(",");
                res.add(parts);
            }
        }
        return res; 
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
