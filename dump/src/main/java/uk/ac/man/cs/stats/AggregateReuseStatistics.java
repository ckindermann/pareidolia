package uk.ac.man.cs.stats;

import uk.ac.man.cs.codp.CODP;
import uk.ac.man.cs.data.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.metrics.*;
import uk.ac.man.cs.ont.*;

import org.semanticweb.owlapi.reasoner.OWLReasoner; 
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import java.util.stream.*;

import java.io.*;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by chris on 31/10/18.
 */

public class AggregateReuseStatistics {

    String ontologyName;
    String patternName;
    LinkedList<ReuseStatistics> reuse;

    public double mean_substitutionPerEntity;
    public int abs_entitiyCoverage;
    public int numberOfInstancesCandidates;

    public ReuseStatistics max_stats;

    public AggregateReuseStatistics(String o, String p, LinkedList<ReuseStatistics> r){
        this.ontologyName = o;
        this.patternName = p;
        this.reuse = r; 

        numberOfInstancesCandidates = this.reuse.size();

        mean_substitutionPerEntity = 0.0;
        abs_entitiyCoverage = 0;

        this.max_stats = new ReuseStatistics();

        this.setMax();
    } 

    public AggregateReuseStatistics(String o, String p, LinkedList<ReuseStatistics> r, double mean, int absCoverage){
        this.ontologyName = o;
        this.patternName = p;
        this.reuse = r; 

        mean_substitutionPerEntity = mean;
        abs_entitiyCoverage = absCoverage;

        this.max_stats = new ReuseStatistics();

        this.setMax();
    } 

    private void setMax(){ 
        Iterator it = this.reuse.iterator();
        while(it.hasNext()){
            ReuseStatistics r = (ReuseStatistics) it.next();
            this.max_stats.getMaxStatistics(r);
        }
    }

    public void printAggregate(){
        ;
    }

    public void write(String path){
        IOHelper io = new IOHelper();

        OntologyStatistics os = this.max_stats.getOntologyStats(); 

        PatternStatistics ps = this.max_stats.getPatternStats();
        EntityCoverage ec = ps.getEntityCoverage();
        AxiomCoverage ac = ps.getAxiomCoverage();

        io.writeAppend(this.patternName + "," + 
                Double.toString(ec.rel_coveredEntities) + "," +
                Double.toString(ec.rel_coveredEntitiesInLogicalAxioms) + "," +
                Double.toString(ec.rel_coveredEntitiesInDeclarationAxioms) + "," +

                Double.toString(ac.rel_coveredAxioms) + "," +
                Double.toString(ac.rel_coveredLogicalAxioms) + "," +
                Double.toString(ac.rel_coveredDeclarationAxioms) + "," +

                Double.toString(os.rel_declarationAxioms) + "," +
                Double.toString(os.rel_instantiatedAxioms) + "," +
                Double.toString(os.rel_instantiatedEquivalentAxioms) + "," +
                Double.toString(os.rel_instantiatedEntailedAxiomsWeak) + "," +
                Double.toString(os.rel_instantiatedEntailedStrongAxioms) + "," +

                Double.toString(mean_substitutionPerEntity) + "," +
                Integer.toString(abs_entitiyCoverage) + "," +
                Integer.toString(numberOfInstancesCandidates), path); 
    }

    public boolean isEmpty(){

        OntologyStatistics os = this.max_stats.getOntologyStats(); 

        PatternStatistics ps = this.max_stats.getPatternStats();
        EntityCoverage ec = ps.getEntityCoverage();
        AxiomCoverage ac = ps.getAxiomCoverage();

        return (ec.rel_coveredEntities == 0 &&
                ec.rel_coveredEntitiesInLogicalAxioms == 0 &&
                ec.rel_coveredEntitiesInDeclarationAxioms == 0 && 
                ac.rel_coveredAxioms == 0 &&
                ac.rel_coveredLogicalAxioms == 0 &&
                ac.rel_coveredDeclarationAxioms == 0 && 

                os.rel_declarationAxioms == 0 &&
                os.rel_instantiatedAxioms == 0 &&
                os.rel_instantiatedEquivalentAxioms == 0 &&
                os.rel_instantiatedEntailedAxiomsWeak == 0 &&
                os.rel_instantiatedEntailedStrongAxioms == 0);
    }
}
