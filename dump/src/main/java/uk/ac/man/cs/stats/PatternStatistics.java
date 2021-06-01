package uk.ac.man.cs.stats;

import uk.ac.man.cs.codp.CODP;
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.util.StringMangler;
import uk.ac.man.cs.data.*;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.ont.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import java.util.stream.*;

import java.io.*;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by chris on 24/10/18.
 */

public class PatternStatistics {

    private OWLOntology pattern; 
    private OWLOntology instance;

    private HashMap<OWLEntity,OWLEntity> substitutionMapping;

    public EntityCoverage entityCoverage;
    public AxiomCoverage axiomCoverage;

    public PatternStatistics(OWLOntology p, OWLOntology inst, HashMap<OWLEntity,OWLEntity> mapping){
        this.pattern = p;
        this.instance = inst;
        this.substitutionMapping = mapping;

        this.entityCoverage = new EntityCoverage(p, inst, mapping);
        this.axiomCoverage = new AxiomCoverage(p, inst, mapping); 
    }

    public EntityCoverage getEntityCoverage(){
        return this.entityCoverage; 
    }

    public AxiomCoverage getAxiomCoverage(){
        return this.axiomCoverage; 
    } 

    public void compute(){
        this.entityCoverage.compute();
        this.axiomCoverage.compute();
    }

    public void getMaxStatistics(PatternStatistics ps){
        this.entityCoverage.getMaxStatistics(ps.getEntityCoverage());
        this.axiomCoverage.getMaxStatistics(ps.getAxiomCoverage()); 
    }

    //TODO
    public void print(){
        ;
    }

    //TODO
    public void dropData(){
        this.entityCoverage.dropData();
        this.axiomCoverage.dropData();
    }
}



