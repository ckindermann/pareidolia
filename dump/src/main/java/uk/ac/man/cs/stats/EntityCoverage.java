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

public class EntityCoverage {

    private OWLOntology pattern; 
    private OWLOntology instance;

    private HashMap<OWLEntity,OWLEntity> substitutionMapping;

    //make this Entity coverage class 
    public int abs_coveredEntities;
    public int abs_coveredEntitiesInLogicalAxioms;
    public int abs_coveredEntitiesInDeclarationAxioms;

    public double rel_coveredEntities;
    public double rel_coveredEntitiesInLogicalAxioms;
    public double rel_coveredEntitiesInDeclarationAxioms;


    public EntityCoverage(OWLOntology p, OWLOntology inst, HashMap<OWLEntity,OWLEntity> mapping){
        this.pattern = p;
        this.instance = inst;
        this.substitutionMapping = mapping; 
    }

    /*TODO: getter and setter methods?*/

    public void compute(){
        Set<OWLEntity> entitiesInLogicalAxioms = new HashSet<>();
        Set<OWLEntity> coveredEntitiesInLogicalAxioms = new HashSet<>();
        Set<OWLEntity> entitiesInDeclarationAxioms = new HashSet<>();
        Set<OWLEntity> coveredEntitiesInDeclarationAxioms = new HashSet<>();

        for(OWLAxiom a : this.pattern.getAxioms()){

            if(a.getAxiomType() == AxiomType.DECLARATION){
                for(OWLEntity e : a.getSignature()){
                    entitiesInDeclarationAxioms.add(e);
                    if(this.substitutionMapping.containsKey(e)){
                        coveredEntitiesInDeclarationAxioms.add(e);
                    }
                }
            }

            if(AxiomType.LOGICAL_AXIOM_TYPES.contains(a.getAxiomType())){
                for(OWLEntity e : a.getSignature()){
                    entitiesInLogicalAxioms.add(e);
                    if(!this.substitutionMapping.containsKey(e)){
                        coveredEntitiesInLogicalAxioms.add(e);
                    }
                }
            }
        }

        //compute numbers
        this.abs_coveredEntities = this.substitutionMapping.size();
        this.rel_coveredEntities = (double) this.abs_coveredEntities / this.pattern.getSignature().size();

        this.abs_coveredEntitiesInDeclarationAxioms = coveredEntitiesInDeclarationAxioms.size();
        this.rel_coveredEntitiesInDeclarationAxioms = coveredEntitiesInDeclarationAxioms.size() / entitiesInDeclarationAxioms.size();

        this.abs_coveredEntitiesInLogicalAxioms = coveredEntitiesInLogicalAxioms.size();
        this.rel_coveredEntitiesInLogicalAxioms = coveredEntitiesInLogicalAxioms.size() / entitiesInLogicalAxioms.size(); 
    }

    public void getMaxStatistics(EntityCoverage ec){
        if(this.abs_coveredEntities < ec.abs_coveredEntities)
            this.abs_coveredEntities = ec.abs_coveredEntities;
        if(this.abs_coveredEntitiesInLogicalAxioms < ec.abs_coveredEntitiesInLogicalAxioms)
            this.abs_coveredEntitiesInLogicalAxioms = ec.abs_coveredEntitiesInLogicalAxioms;
        if(this.abs_coveredEntitiesInDeclarationAxioms < ec.abs_coveredEntitiesInDeclarationAxioms)
            this.abs_coveredEntitiesInDeclarationAxioms = ec.abs_coveredEntitiesInDeclarationAxioms;

        if(this.rel_coveredEntities < ec.rel_coveredEntities)
            this.rel_coveredEntities = ec.rel_coveredEntities;
        if(this.rel_coveredEntitiesInLogicalAxioms < ec.rel_coveredEntitiesInLogicalAxioms)
            this.rel_coveredEntitiesInLogicalAxioms = ec.rel_coveredEntitiesInLogicalAxioms;
        if(this.rel_coveredEntitiesInDeclarationAxioms < ec.rel_coveredEntitiesInDeclarationAxioms) 
            this.rel_coveredEntitiesInDeclarationAxioms = ec.rel_coveredEntitiesInDeclarationAxioms;
    }

    public void dropData(){
        this.pattern = null;
        this.instance = null;
        this.substitutionMapping.clear(); 
    }

}
