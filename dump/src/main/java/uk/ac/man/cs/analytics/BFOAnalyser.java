package uk.ac.man.cs.analytics;

import uk.ac.man.cs.metrics.SignatureMetric;
import uk.ac.man.cs.metrics.StringPattern;
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.util.StringMangler;
import uk.ac.man.cs.util.IOHelper;

import com.opencsv.CSVWriter;
import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.detectors.LexicalPatternDetector;
import uk.ac.man.cs.detectors.LogicalPatternDetector;

import java.io.*;


import java.util.*;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;

import org.semanticweb.owlapi.model.*;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;


//import org.semanticweb.owlapi.reasoner.OWLOntologyCreationException;
import org.semanticweb.owlapi.util.AbstractOWLStorer;
import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

/**
 * Created by chris on 21/08/18.
 */

public class BFOAnalyser {

    private static final Logger log = Logger.getLogger(String.valueOf(BFOAnalyser.class));

    private File resultPath;
    //counts import sources and termNames for entities from BFO accross ontologies
    private HashMap<String, Integer> termStats;
    private HashMap<String, Integer> importStats;
    private IOHelper io;

    public BFOAnalyser(File rPath){
        this.resultPath = rPath;
        this.termStats = new HashMap<>();
        this.importStats= new HashMap<>();
        this.io = new IOHelper();
    }

    public void run() throws IOException {
        this.computeDistribution(this.resultPath); 
    }

    public void write(String outputPath){
        //setup output folder structure
        String path = outputPath + "/BFOstatistics";
        io.createFolder(path);

        //io.writeString(path + "/termDistribution", termStats.toString());
        io.writeStringSet(path + "/termDistribution", formatOutput(this.termStats));
        io.writeStringSet(path + "/importDistribution", formatOutput(this.importStats)); 
    }

    public HashMap<String, Integer> getTermStats(){
        return this.termStats; 
    }

    public HashMap<String, Integer> getImportStats(){
        return this.importStats; 
    }

    public void computeBFOImport(File resDir, String output) throws IOException {
        io.createFolder(output);

        for (File ont : resDir.listFiles()) { 
            String path = output + "/obo/imports/" + ont.getName() + "/" + "ImportClosure";
            Set<String> imports = io.readFile(new File(path));

            for(String s : imports){
                if(s.toLowerCase().contains("bfo")){
                    io.writeAppend(ont.getName() + " : " + s, output + "/imports"); 
                } 
            } 
        } 
    }

    private void computeDistribution(File ontDir) throws IOException {

        log.info("Checking Ontology for BFO terms");
        int ontCount = 0;//counter for ontologies

        //HashMap<String, Integer> termStats = new HashMap<>();
        //HashMap<String, Integer> importStats = new HashMap<>();

        //once we found a match for "bfo" in a string we look for two patterns:
        //(1) <importSource/bfo/#EntityName>
        //(2) <importSource/BFO_XXXXXXXX>
        //
        //Examples:
        //(1)<http://www.ifomis.org/bfo/1.1/span#Occurrent>
        //(2) <http://purl.obolibrary.org/obo/BFO_0000054>
        //
        //the *pattern* splits such strings in 3 groups
        //Group 1 the importSoruce
        //Group 2 the start of an entity (either '#' or 'BFO_')
        //Group 3 the actual name of the bfo entity
        Pattern pattern = Pattern.compile("<(.+)(#|BFO_)(.+)>");

        //iterate over ontologies with BFO terms
        for (File ont : ontDir.listFiles()) { 
            log.info("\tOntology " + ++ontCount + " : " + ont.getName());

            //since we are working with a *set* here,
            //duplicates will not be counted twice
            Set<String> terms = io.readFile(ont);

            for (String s : terms){
                Matcher matcher = pattern.matcher(s);
                if(matcher.find()){
                    String importSource = matcher.group(1);
                    String termName = matcher.group(3);

                    //map OWL entities to their labels
                    //if(termName.charAt(0) == '0'){
                    //    //continue;
                    //    termName = translateBFOcode(termName); 
                    //}
                    //termName = termName.replaceAll("\\s+","").toLowerCase();
                    //
                    increment(this.importStats, importSource);
                    increment(this.termStats, termName);
                }
            } 
        } 
    } 

    private void increment(HashMap<String, Integer> hm, String s){
        if(hm.containsKey(s)){
            hm.replace(s, new Integer(hm.get(s).intValue() + 1));
        }else{
            hm.put(s, new Integer(1));
        } 
    }

    private Set<String> formatOutput(HashMap<String, Integer> hm){
        Set<String> result = new HashSet<>();
        Iterator it = hm.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            result.add(pair.getKey() + "," + pair.getValue().toString()); 
        }

        return result; 
    }

    private String translateBFOcode(String code){
        HashMap<String, String> translate = new HashMap<>();

        //CAREFUL: different ontologies use different mappings (!)
        //better use the labels in individual ontologies
        
        //got them from https://raw.githubusercontent.com/BFO-ontology/BFO/v2.0/bfo.owl
        translate.put("0000009", "two-dimensional spatial region");
        translate.put("0000015", "process");
        translate.put("0000179", "BFO OWL specification label");
        translate.put("0000035", "process boundary");
        translate.put("0000034", "function");
        translate.put("0000019", "quality");
        translate.put("0000018", "zero-dimensional spatial region");
        translate.put("0000017", "realizable entity");
        translate.put("0000016", "disposition");
        translate.put("0000038", "one-dimensional temporal region");
        translate.put("0000011", "spatiotemporal region");
        translate.put("0000031", "generically dependent continuant");
        translate.put("0000030", "object");
        translate.put("0000147", "zero-dimensional continuant fiat boundary");
        translate.put("0000004", "independent continuant");
        translate.put("0000026", "one-dimensional spatial region");
        translate.put("0000146", "two-dimensional continuant fiat boundary");
        translate.put("0000003", "occurrent");
        translate.put("0000145", "relational quality");
        translate.put("0000002", "continuant");
        translate.put("0000024", "fiat object part");
        translate.put("0000144", "process profile");
        translate.put("0000001", "entity");
        translate.put("0000023", "role");
        translate.put("0000008", "temporal region");
        translate.put("0000029", "site");
        translate.put("0000006", "spatial region");
        translate.put("0000028", "three-dimensional spatial region");
        translate.put("0000148", "zero-dimensional temporal region");
        translate.put("0000027", "object aggregate");
        translate.put("0000040", "material entity");
        translate.put("0000182", "history");
        translate.put("0000142", "one-dimensional continuant fiat boundary");
        translate.put("0000141", "immaterial entity");
        translate.put("0000020", "specifically dependent continuant");
        translate.put("0000140", "continuant fiat boundary");
        translate.put("0000180", "BFO CLIF specification label");


        translate.put("0000050", "is-part-of");
        translate.put("0000051", "has-part");
        translate.put("0000052", "inheres-in");
        translate.put("0000053", "is-bearer-of");
        translate.put("0000054", "is-realized-by");
        translate.put("0000055", "realizes");
        translate.put("0000056", "participates-in");
        translate.put("0000057", "has-participant");
        translate.put("0000058", "is-concretization-of");
        translate.put("0000059", "concretizes");
        translate.put("0000060", "is-immediately-preceded-by");
        translate.put("0000061", "immediately-precedes");
        translate.put("0000062", "is-preceded-by");
        translate.put("0000063", "precedes");
        translate.put("0000064", "is-course-of");
        translate.put("0000065", "has-course");
        translate.put("0000066", "occurs-in");
        translate.put("0000067", "has-site-of");
        translate.put("0000068", "begins-to-exist-during");
        translate.put("0000069", "ceases-to-exist-during");
        translate.put("0000070", "s-depends-on");
        translate.put("0000071", "has-granular-part");
        translate.put("0000072", "has-granular-process-part");
        translate.put("0000073", "is-granular-part-of");
        translate.put("0000074", "is-granular-part-of-process");
        translate.put("0000075", "is-aggregate-of");
        translate.put("0000076", "is-fiat-part-of");
        translate.put("0000077", "has-participant-beginning-to-exist");
        translate.put("0000078", "has-participant-ceasing-to-exist");
        translate.put("0000079", "is-function-of");
        translate.put("0000080", "is-quality-of");
        translate.put("0000081", "is-role-of");
        translate.put("0000082", "is-located-in");
        translate.put("0000083", "is-located-at");
        translate.put("0000084", "g-depends-on");
        translate.put("0000085", "has-function");
        translate.put("0000086", "has-quality");
        translate.put("0000087", "has-role");

        if(translate.containsKey(code))
            return translate.get(code); 

        return("No code for" + code);


    }
 
}
