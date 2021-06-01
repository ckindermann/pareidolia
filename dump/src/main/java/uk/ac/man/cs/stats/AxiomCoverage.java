package uk.ac.man.cs.stats;

import uk.ac.man.cs.codp.CODP;
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.util.StringMangler;
import uk.ac.man.cs.data.*;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.ont.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import java.util.HashSet;
import java.util.HashMap;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.*;
import java.util.Set;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.io.File;
import java.io.FileWriter;
import org.semanticweb.owlapi.search.EntitySearcher;
import java.util.stream.*;
import java.io.FileReader;

import java.io.*;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by chris on 24/10/18.
 */

public class AxiomCoverage {

    private OWLOntology pattern; 
    private OWLOntology instance;

    private HashMap<OWLEntity,OWLEntity> substitutionMapping;

    public int abs_coveredAxioms;
    public int abs_coveredLogicalAxioms;
    public int abs_coveredDeclarationAxioms;

    public double rel_coveredAxioms;
    public double rel_coveredLogicalAxioms;
    public double rel_coveredDeclarationAxioms; 

    private Set<OWLAxiom> coveredLogicalAxioms;
    private Set<OWLAxiom> coveredDeclarationAxioms;


    public AxiomCoverage(OWLOntology p, OWLOntology inst, HashMap<OWLEntity,OWLEntity> mapping){
        this.pattern = p;
        this.instance = inst;
        this.substitutionMapping = mapping;

        this.coveredLogicalAxioms = new HashSet<>();
        this.coveredDeclarationAxioms = new HashSet<>(); 
    }

    public void compute(){ 
        boolean covered;
        for(OWLAxiom a : this.pattern.getAxioms()){

            if(a.getAxiomType() == AxiomType.DECLARATION){
                covered = true; 
                for(OWLEntity e : a.getSignature()){
                    if(!this.substitutionMapping.containsKey(e)){
                        covered = false;
                        break;
                    }
                }
                if(covered)
                    this.coveredDeclarationAxioms.add(a); 
            } 

            if(AxiomType.LOGICAL_AXIOM_TYPES.contains(a.getAxiomType())){

                covered = true; 
                for(OWLEntity e : a.getSignature()){
                    if(!this.substitutionMapping.containsKey(e)){
                        covered = false;
                        break;
                    }
                }
                if(covered)
                    this.coveredLogicalAxioms.add(a); 
            }
        }

        this.abs_coveredLogicalAxioms = this.coveredLogicalAxioms.size();
        this.abs_coveredDeclarationAxioms = this.coveredDeclarationAxioms.size();

        this.abs_coveredAxioms = abs_coveredLogicalAxioms + abs_coveredDeclarationAxioms;

        HashSet<OWLAxiom> logicalAxioms = new HashSet<>();
        for(AxiomType<?> t : AxiomType.LOGICAL_AXIOM_TYPES){
            logicalAxioms.addAll(this.instance.getAxioms(t)); 
        }

        HashSet<OWLAxiom> declarationAxioms = new HashSet<>();
        declarationAxioms.addAll(this.instance.getAxioms(AxiomType.DECLARATION));

        this.rel_coveredLogicalAxioms = (double) abs_coveredLogicalAxioms / logicalAxioms.size();
        this.rel_coveredDeclarationAxioms = (double) abs_coveredDeclarationAxioms / declarationAxioms.size();

        this.rel_coveredAxioms = (double) this.abs_coveredAxioms / (logicalAxioms.size() + declarationAxioms.size() );

    }

    public void getMaxStatistics(AxiomCoverage ac){
        if(this.abs_coveredAxioms < ac.abs_coveredAxioms)
            this.abs_coveredAxioms = ac.abs_coveredAxioms;
        if(this.abs_coveredLogicalAxioms < ac.abs_coveredLogicalAxioms)
            this.abs_coveredLogicalAxioms = ac.abs_coveredLogicalAxioms;
        if(this.abs_coveredDeclarationAxioms < ac.abs_coveredDeclarationAxioms)
            this.abs_coveredDeclarationAxioms = ac.abs_coveredDeclarationAxioms;

        if(this.rel_coveredAxioms < ac.rel_coveredAxioms)
            this.rel_coveredAxioms = ac.rel_coveredAxioms;
        if(this.rel_coveredLogicalAxioms < ac.rel_coveredLogicalAxioms)
            this.rel_coveredLogicalAxioms = ac.rel_coveredLogicalAxioms;
        if(this.rel_coveredDeclarationAxioms < ac.rel_coveredDeclarationAxioms)
            this.rel_coveredDeclarationAxioms = ac.rel_coveredDeclarationAxioms; 
    }

    public void dropData(){
        this.pattern = null;
        this.instance = null;
        this.substitutionMapping.clear(); 
        this.coveredLogicalAxioms.clear();
        this.coveredDeclarationAxioms.clear(); 
    }
}
