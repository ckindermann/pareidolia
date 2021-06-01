package uk.ac.man.cs.exp;

import uk.ac.man.cs.codp.*;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.stats.*;
import uk.ac.man.cs.data.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.lodp2.*;
import uk.ac.man.cs.ghader.*;

import com.opencsv.CSVReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

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
public class Ghader {

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));

    public static void main(String[] args) throws IOException , Exception
    {

        File ontDir = new File(args[0]);//ontologies to be searched

        String ontDirPath = args[0];
        String csvDirPath = args[1];
        String relation = args[2];
        String outputPath = args[3];

        Ghader exp = new Ghader();

        //exp.testParse(csvDirPath);
        exp.collect(ontDirPath, csvDirPath, relation, outputPath);

    }


    private void collect(String ontDirPath, String csvFilePath, String relationString, String outputPath) throws Exception{

        File ontFile = new File(ontDirPath);
        log.info("\tLoading Ontology : " + ontFile.getName());
        OntologyLoader ontLoader = new OntologyLoader(ontFile, false);
        OWLOntology ont = ontLoader.getOntology();


        //File CSVDir = new File(csvFilePath);
        File csv = new File(csvFilePath);
        log.info("\tLoading csv : " + csv.getName());

        String output = outputPath + "/" + ontFile.getName() +
            "_" + csv.getName() + "_" + relationString;

        try (Reader reader = Files.newBufferedReader(Paths.get(csvFilePath), StandardCharsets.ISO_8859_1);
                CSVReader csvReader = new CSVReader(reader);)
        {
            String[] nextRecord; 
            int count = 1;

            TripleDetector tripleDetector = new TripleDetector(ont);

            while ((nextRecord = csvReader.readNext()) != null) {
                System.out.println("Parse line " + count++);

                Entity sub = new Entity(nextRecord[3], nextRecord[4], nextRecord[5], nextRecord[6]);
                Entity obj = new Entity(nextRecord[7], nextRecord[8], nextRecord[9], nextRecord[10]);
                Relation relation = new Relation(relationString);

                EntityTriple triple = new EntityTriple(sub, obj, relation);

                long starTime = System.nanoTime();
                MatchFinder matchFinder = new MatchFinder(ont, triple);
                matchFinder.run();
                long endTime = System.nanoTime();
                double duration = (endTime - starTime) / 1000000000.0;
                System.out.println("Timing matcher: " + duration);

                AssertedTripleDetector detector = new AssertedTripleDetector(ont, triple);
                detector.setRelation(relationString);
                Set<OWLAxiom> results = detector.run();

                String subject = sub.toString();
                String object = obj.toString();
                String format = subject + "," + object + "," + results;
                IOHelper.writeAppend(format, output);

                //starTime = System.nanoTime();
                ////TripleDetector tripleDetector = new TripleDetector(ont, triple);
                //tripleDetector.setTriple(triple);
                //tripleDetector.run();
                //endTime = System.nanoTime();
                //duration = (endTime - starTime) / 1000000000.0;
                //System.out.println("Timing detector: " + duration);

                //if(!tripleDetector.getAsserted().isEmpty() ||
                //        !tripleDetector.getEntailed().isEmpty()){
                //    System.out.println("found EVIDENCE! ");
                //    System.out.println("Match type subject: " + sub.getMatchType());
                //    for(ClassMatch cm : sub.getMatches()){
                //        System.out.println("Match classes: " + cm.getOWLClass().toString()); 
                //    } 
                //    System.out.println("Match type object: " + obj.getMatchType());
                //    for(ClassMatch cm : obj.getMatches()){
                //        System.out.println("Match classes: " + cm.getOWLClass().toString()); 
                //    }
                //    System.out.println("Axioms");
                //    for(Restriction r : tripleDetector.getAsserted()){ 
                //        System.out.println(r.getRelation().toString()); 
                //    } 
                

                //    String subject = sub.toString();
                //    String object = obj.toString();

                //    String subjectMatchType = sub.getMatchType();
                //    String subjectMatches = "[";
                //    for(ClassMatch cm : sub.getMatches()){
                //        subjectMatches += cm.getOWLClass().toString() + ",";
                //    } 
                //    subjectMatches += "]";

                //    String objectMatchType = obj.getMatchType();
                //    String objectMatches = "[";
                //    for(ClassMatch cm : obj.getMatches()){
                //        objectMatches += cm.getOWLClass().toString() + ",";
                //    } 
                //    objectMatches += "]";

                //    String relationMatches = "[";
                //    for(RelationMatch rm : relation.getMatches()){
                //        relationMatches += rm.getProperty().toString() + ","; 
                //    }
                //    relationMatches += "]";

                //    String AxiomsAsserted = "[";
                //    for(Restriction r : tripleDetector.getAsserted()){ 
                //        AxiomsAsserted += r.getRelation().toString() + ",";
                //    } 
                //    AxiomsAsserted += "]";

                //    String AxiomsEntailed = "[";
                //    for(Restriction r : tripleDetector.getEntailed()){ 
                //        AxiomsEntailed += r.getRelation().toString() + ",";
                //    } 
                //    AxiomsEntailed += "]";

                //    String format = subject + ","
                //        + object + ","
                //        + relationString + ","
                //        + subjectMatchType + ","
                //        + subjectMatches + ","
                //        + objectMatchType + ","
                //        + objectMatches + ","
                //        + relationMatches + ","
                //        + AxiomsAsserted + ","
                //        + AxiomsEntailed;

                //    //System.out.println(format);
                //    IOHelper.writeAppend(format, output);
                //}

                //System.out.println("Row number " + count++);
                //System.out.println("Found matchse for Subject: " + triple.getSubject().hasMatch());
                //Set<ClassMatch> matches = triple.getSubject().getMatch(triple.getSubject().getName());
                //for(Match m : matches){
                //    System.out.println("Match " + m.getOWLClass().toString());
                //}
                //System.out.println("Found matchse for Object: " + triple.getObject().hasMatch()); 
            }
        }
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
}
