package uk.ac.man.cs.detectors.structural.restriction.existential;

import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.detectors.structural.intersection.*;
import uk.ac.man.cs.detectors.structural.restriction.*;

import org.semanticweb.owlapi.model.*;
import java.io.*;
import java.util.*;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * Created by chris 21/11/18
 */
public class ExistentialRestriction extends Restriction {

    public ExistentialRestriction(OWLClass name){
        super(name); 
    } 
    //type the original restriction?
    //make Restriction an interface?
}
