//package net.sf.extjwnl.utilities;
package uk.ac.man.cs.exp;

//import uk.ac.man.cs.detectors.structural.*;
import uk.ac.man.cs.detectors.structural.disjointUnion.*;
import uk.ac.man.cs.detectors.structural.union.*;
import uk.ac.man.cs.detectors.structural.intersection.*;
import uk.ac.man.cs.detectors.structural.restriction.*;
import uk.ac.man.cs.detectors.structural.restriction.existential.*;
import uk.ac.man.cs.detectors.*;

import uk.ac.man.cs.analysers.*;

import uk.ac.man.cs.codp.*;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.strings.*;
import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.stats.*;
import uk.ac.man.cs.data.*;
import uk.ac.man.cs.util.*;
//import uk.ac.man.cs.lodp2.*;
import uk.ac.man.cs.ghader.*;

import java.io.*;
import java.util.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.HermiT.ReasonerFactory;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;

/**
 * A class to demonstrate the functionality of the library.
 */
public class OntologyCheck {

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));

    public static void main(String[] args) throws IOException , Exception{

        String ontDirPath = args[0];
        String outputPath = args[1];

        OntologyCheck exp = new OntologyCheck(); 
        exp.run(ontDirPath, outputPath);

    }

    private void run(String ontDirPath, String outputPath) throws Exception {

        File ontFile = new File(ontDirPath);
        log.info("\tLoading Ontology : " + ontFile.getName());
        //OntologyLoader ontLoader = new OntologyLoader(ontFile, false);
        //OWLOntology ont = ontLoader.getOntology();

        OntologyChecker checker = new OntologyChecker(ontDirPath, outputPath);
        checker.check();

    }

}
