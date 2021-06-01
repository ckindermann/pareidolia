package uk.ac.man.cs.exp;

import uk.ac.man.cs.codp.*;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.stats.*;
import uk.ac.man.cs.data.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.lodp2.*;

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
public class Reuse {

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));

    public static void main(String[] args) throws IOException , Exception
    {

        File ontDir = new File(args[0]);//ontologies to be searched
        File patDir = new File(args[1]);//ontologies to be searched

        String ontDirPath = args[0];
        String patDirPath = args[1];
        String instanceDirPath = args[2];
        String outputPath = args[3];

        Reuse exp = new Reuse();

        exp.testLODP(ontDirPath, outputPath);

        //exp.computeReuse(ontDirPath, patDirPath, outputPath); 
        //IOHelper.writeAppend(ontDirPath, outputPath + "/success"); 
    }

    private void testLODP(String ontDirPath, String outputPath) throws IOException, Exception {

        File ontFile = new File(ontDirPath);
        log.info("\tLoading Ontology : " + ontFile.getName());
        OntologyLoader ontLoader = new OntologyLoader(ontFile, false);
        OWLOntology ont = ontLoader.getOntology();

        
        NaryRelationFinder naryRelationFinder = new NaryRelationFinder(ont);
        Set<NaryRelation> rels = naryRelationFinder.getNaryRelationsViaSubsumptions();
        Set<NaryRelation> relInter = naryRelationFinder.getNaryRelationsViaIntersections();

        for(NaryRelation r : relInter){
            r.print(); 
        }

        //for(NaryRelation r : rels){
        //    System.out.println(r.getRestrictions().size());
        //    System.out.println(r.getExistentialRestrictions().size()); 
        //}



        //HashMap<OWLClassExpression, Set<OWLClassExpression>> test = naryRelationFinder.getNaryRelations();
        //System.out.println(test);





       //Partition par = new Partition(ont); 
       //OneOfAllDifferentFinder ooadf = par.getOneOfAllDifferentFinder();
       //ooadf.run(true);
       //System.out.println("ASSERTED: " + ooadf.getAsserted());
       //System.out.println("ENTAILED: " + ooadf.getEntailed());


        //par.computeReuse(true);
        

        //DisjointUnionFinder uodc = par.getUnionOfDisjointClasses(); 
        //System.out.println("ASSERTED: " + uodc.getAssertedDisjointUnions().size());
        //System.out.println("ENTAILED: " + uodc.getEntailedDisjointUnions().size());
    }

    private void computeReuse(String ontDirPath, String patDirPath, String outputPath) throws IOException, Exception {

        //File ontDir = new File(ontDirPath);
        File ontFile = new File(ontDirPath);
        File patDir = new File(patDirPath);

        IOHelper io = new IOHelper();


        //loading ontology
        log.info("\tLoading Ontology : " + ontFile.getName());
        OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
        OWLOntology ont = ontLoader.getOntology();

        //statistics for different patterns
        LinkedList<AggregateReuseStatistics> statistics = new LinkedList<AggregateReuseStatistics>(); 

        //iterate over patterns
        int patCount = 0; 
        for (File patFile : patDir.listFiles()){

            log.info("\tPattern " + ++patCount + " : " + patFile.getName());
            OntologyLoader patLoader = new OntologyLoader(patFile, false);
            OWLOntology pat = patLoader.getOntology(); 

            //define output location for pattern instances (given as ontologies)
            String path = outputPath + "/" + ontFile.getName() + "/" + patFile.getName();
            io.createFolder(path);

            //compute pattern instantiations
            //(based on lexical associations of entities)
            SubstitutionMetric sub = new SubstitutionMetric(ont, patFile);
            sub.computeSubstitutions();

            //write these substitutions to a folder
            //and remember writing order 
            HashMap<Integer,List<LexicalMatch>> writingOrder =
                sub.writeSubstitutions(path + "/entities"); 

            //compute statistics
            File instances = new File (path + "/entities/instance"); 
            LinkedList<ReuseStatistics> reuseList =
                calculateReuseStatistics(ont, pat, instances, writingOrder);

            //aggregate statistics for a single pattern
            //(there exist multiple substitutions for a single pattern)
            AggregateReuseStatistics as =
                new AggregateReuseStatistics(ontFile.getName(),
                                            patFile.getName(),
                                            reuseList,
                                            sub.meanSubstitutionsPerEntity(),
                                            sub.numberOfCoveredEntities());

            if(!as.isEmpty()){
                statistics.add(as); 
            } 
            sub.cleanUp(); 
        }

        //write when done
        write(statistics, ontFile.getName(), outputPath);
    }

    private void write(LinkedList<AggregateReuseStatistics> statistics, String ontName, String outputPath){
        String statsOutput = outputPath + "/stats";
        IOHelper io = new IOHelper();
        io.createFolder(statsOutput);

        Iterator it = statistics.iterator();
        while(it.hasNext()){
            ((AggregateReuseStatistics) it.next()).write(statsOutput + "/" + ontName);
        } 
    }

    private LinkedList<ReuseStatistics> calculateReuseStatistics(OWLOntology ont, OWLOntology pat, File instances, HashMap<Integer,List<LexicalMatch>> writingOrder) throws IOException, Exception{
        LinkedList<ReuseStatistics> reuseList = new LinkedList<ReuseStatistics>();
        int instCount = 0;
        //iterate over written instances
        for (File instFile : instances.listFiles()){ 
            instCount++;
            OntologyLoader instLoader = new OntologyLoader(instFile, false);
            OWLOntology inst = instLoader.getOntology();

            //retrieve substitution for given instance
            Integer i = new Integer(Integer.parseInt(instFile.getName()));

            List<LexicalMatch> instanceSubstitution = writingOrder.get(i);

            //calculate statistics
            ReuseStatistics r = new ReuseStatistics(instanceSubstitution, ont, pat, inst); 
            r.compute();
            r.dropData();
            reuseList.add(r);
        }
        return reuseList; 
    }
}




