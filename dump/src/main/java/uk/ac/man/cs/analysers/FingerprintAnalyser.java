package uk.ac.man.cs.analysers;

import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.lodp.*;
import uk.ac.man.cs.util.*;

import java.io.*;
import java.util.*;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.apibinding.OWLManager;
import java.util.Iterator;

import org.semanticweb.owlapi.model.*;

import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
//import org.semanticweb.owlapi.reasoner.OWLOntologyCreationException;
import org.semanticweb.owlapi.util.AbstractOWLStorer;
import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;

/**
 * The Dector defines the basic operations of a
 * pattern detctor.
 *
 * @author Christian Kindermann
 * @version 1.0
 * @since 2018-10-08
 *
 */
public class FingerprintAnalyser {

    private String ontologyFingerprintsPath;
    private String patternFingerprintsPath;
    private String outputPath;

    public FingerprintAnalyser(String resultPath, String outputPath) {
        this.outputPath = outputPath;
        this.ontologyFingerprintsPath = resultPath + "/ontologies";
        this.patternFingerprintsPath = resultPath + "/patterns";
    }

    public void run() throws Exception {

        File ontFile = new File(this.ontologyFingerprintsPath);
        File patFile = new File(this.patternFingerprintsPath);

        String outputPathForViolations = this.outputPath + "/violations";
        String outputPathForMatches = this.outputPath + "/matches";
        String outputSummary = this.outputPath + "/summary";
        //setup output folder structure
        IOHelper.createFolder(outputPathForViolations);
        IOHelper.createFolder(outputPathForMatches);

        int patternCount = 0;
        int aggregateClassViolations = 0;
        int aggregatePropertyViolations = 0;
        int aggregateIndividualViolations = 0;

        for (File pat : patFile.listFiles()) {
            Fingerprint patFP = new Fingerprint(pat); 
            patternCount++;
            String notCovered = patternCount + "Pattern " + patFP.getName() + " is not covered in: \n" ;
            int violations = 0;
            int matches = 0;
            int countClassViolations = 0;
            int countPropertyViolations = 0;
            int countIndividualViolations = 0;

            for(File ont : ontFile.listFiles()){
                Fingerprint ontFP = new Fingerprint(ont); 
                if(!ontFP.covers(patFP)){
                    violations++;

                    if(!ontFP.coversClassAxioms(patFP))
                        countClassViolations++;
                    if(!ontFP.coversPropertyAxioms(patFP))
                        countPropertyViolations++;
                    if(!ontFP.coversIndividualAxioms(patFP))
                        countIndividualViolations++;

                    //output violations + reasons
                    IOHelper.writeAppend(ontFP.getName() + " : "  + getViolations(ontFP, patFP),
                            outputPathForViolations + "/" + patFP.getName());

                } else {
                    matches++; 
                    //output matches
                    IOHelper.writeAppend(ontFP.getName(), outputPathForMatches + "/" + patFP.getName());
                }
            }
            //output summary
            //String summary = patFP.getName() + " covered in " + matches + " " + matches/360.0 + " ontologies - " 
            //    + " not covered in " + violations + " ontologies ["
            //    + countClassViolations + ","
            //    + countPropertyViolations + ","
            //    + countIndividualViolations + "]";
            String summary = patFP.getName() + " " + matches/360.0;
            IOHelper.writeAppend(summary, outputSummary);
            aggregateClassViolations += countClassViolations;
            aggregatePropertyViolations += countPropertyViolations;
            aggregateIndividualViolations += countIndividualViolations;
        } 

        IOHelper.writeAppend("Aggregate violations : [" + aggregateClassViolations +  "," +
                aggregatePropertyViolations + "," +
                aggregateIndividualViolations + "]"
                , outputSummary);
    }


    private String getViolations(Fingerprint ont, Fingerprint pat){
        String result = "";

        if(ont.classAxioms < pat.classAxioms) 
            result += "Class Axioms : [";
        if(ont.disjointUnionAxioms < pat.disjointUnionAxioms)
            result += "disjointUnionAxioms,";
        if(ont.subClassOfAxioms < pat.subClassOfAxioms)
            result += "subClassOfAxioms,";
        if(ont.naryClassAxioms < pat.naryClassAxioms)
            result += "naryClassAxioms,";
        if(ont.equivalentClassesAxioms < pat.equivalentClassesAxioms)
            result += "equivalentClassesAxioms,";
        if(ont.disjointClassesAxioms < pat.disjointClassesAxioms)
            result += "disjointClassesAxioms,";

        if(ont.classAxioms < pat.classAxioms) 
            result += "]";

        if(ont.propertyAxioms < pat.propertyAxioms)
            result += "PropertyAxioms : [";
        if(ont.dataPropertyAxioms < pat.dataPropertyAxioms)
            result += "dataPropertyAxioms,";
        if(ont.naryPropertyAxioms < pat.naryPropertyAxioms)
            result += "naryPropertyAxioms,";
        if(ont.subPropertyAxioms < pat.subPropertyAxioms)
            result += "subPropertyAxioms,";
        if(ont.unaryPropertyAxioms < pat.unaryPropertyAxioms)
            result += "unaryPropertyAxioms,";
        if(ont.objectPropertyAxiom < pat.objectPropertyAxiom)
            result += "objectPropertyAxiom,";
        if(ont.dataPropertyCharacteristicAxiom < pat.dataPropertyCharacteristicAxiom)
            result += "dataPropertyCharacteristicAxiom,";
        if(ont.propertyDomainAxiom < pat.propertyDomainAxiom)
            result += "propertyDomainAxiom,";
        if(ont.propertyRangeAxiom < pat.propertyRangeAxiom)
            result += "propertyRangeAxiom,";
        if(ont.disjointDataPropertiesAxiom  < pat.disjointDataPropertiesAxiom)
            result += "disjointDataPropertiesAxiom,";
        if(ont.equivalentDataPropertiesAxiom < pat.equivalentDataPropertiesAxiom)
            result += "equivalentDataPropertiesAxiom,";
        if(ont.subDataPropertyOfAxiom < pat.subDataPropertyOfAxiom)
            result += "subDataPropertyOfAxiom,";
        if(ont.disjointObjectPropertiesAxiom < pat.disjointObjectPropertiesAxiom)
            result += "disjointObjectPropertiesAxiom,";
        if(ont.equivalentObjectPropertiesAxiom < pat.equivalentObjectPropertiesAxiom)
            result += "equivalentObjectPropertiesAxiom,";
        if(ont.inverseObjectPropertiesAxiom < pat.inverseObjectPropertiesAxiom)
            result += "inverseObjectPropertiesAxiom,";
        if(ont.subObjectPropertyOfAxiom < pat.subObjectPropertyOfAxiom)
            result += "subObjectPropertyOfAxiom,";
        if(ont.subPropertyChainOfAxiom < pat.subPropertyChainOfAxiom)
            result += "subPropertyChainOfAxiom,";
        if(ont.objectPropertyCharacteristicAxiom < pat.objectPropertyCharacteristicAxiom)
            result += "objectPropertyCharacteristicAxiom,";
        if(ont.inverseFunctionalObjectPropertyAxiom < pat.inverseFunctionalObjectPropertyAxiom)
            result += "inverseFunctionalObjectPropertyAxiom,";
        if(ont.asymmetricObjectPropertyAxiom < pat.asymmetricObjectPropertyAxiom)
            result += "asymmetricObjectPropertyAxiom,";
        if(ont.functionalObjectPropertyAxiom < pat.functionalObjectPropertyAxiom)
            result += "functionalObjectPropertyAxiom,";
        if(ont.irreflexiveObjectPropertyAxiom < pat.irreflexiveObjectPropertyAxiom)
            result += "irreflexiveObjectPropertyAxiom,";
        if(ont.reflexiveObjectPropertyAxiom < pat.reflexiveObjectPropertyAxiom)
            result += "reflexiveObjectPropertyAxiom,";
        if(ont.symmetricObjectPropertyAxiom < pat.symmetricObjectPropertyAxiom)
            result += "symmetricObjectPropertyAxiom,";
        if(ont.transitiveObjectPropertyAxiom < pat.transitiveObjectPropertyAxiom)
            result += "transitiveObjectPropertyAxiom,";
        if(ont.objectPropertyRangeAxiom < pat.objectPropertyRangeAxiom)
            result += "objectPropertyRangeAxiom,";
        if(ont.dataPropertyDomainAxiom < pat.dataPropertyDomainAxiom)
            result += "dataPropertyDomainAxiom,";
        if(ont.objectPropertyDomainAxiom < pat.objectPropertyDomainAxiom)
            result += "objectPropertyDomainAxiom,";
        if(ont.functionalDataPropertyAxiom < pat.functionalDataPropertyAxiom)
            result += "functionalDataPropertyAxiom,";
        if(ont.dataPropertyRangeAxiom < pat.dataPropertyRangeAxiom)
            result += "dataPropertyRangeAxiom,"; 
        if(ont.propertyAxioms < pat.propertyAxioms)
            result += "]";


        if(ont.individualAxioms < pat.individualAxioms)
            result += "Individual Axioms : [";
        if(ont.classAssertionAxioms < pat.classAssertionAxioms)
            result += "classAssertionAxioms,";
        if(ont.naryIndividualAxioms < pat.naryIndividualAxioms)
            result += "naryIndividualAxioms,";
        if(ont.propertyAssertionAxioms < pat.propertyAssertionAxioms)
            result += "propertyAssertionAxioms,";
        if(ont.negativeObjectPropertyAssertionAxiom < pat.negativeObjectPropertyAssertionAxiom)
            result += "negativeObjectPropertyAssertionAxiom,";
        if(ont.differentIndividualsAxiom < pat.differentIndividualsAxiom)
            result += "differentIndividualsAxiom,";
        if(ont.sameIndividualAxiom < pat.sameIndividualAxiom)
            result += "sameIndividualAxiom,";
        if(ont.dataPropertyAssertionAxiom < pat.dataPropertyAssertionAxiom)
            result += "dataPropertyAssertionAxiom,";
        if(ont.negativeDataPropertyAssertionAxiom < pat.negativeDataPropertyAssertionAxiom)
            result += "negativeDataPropertyAssertionAxiom,";
        if(ont.objectPropertyAssertionAxiom < pat.objectPropertyAssertionAxiom)
            result += "objectPropertyAssertionAxiom,"; 
        if(ont.individualAxioms < pat.individualAxioms)
            result += "]";

        return result;
    }

}

