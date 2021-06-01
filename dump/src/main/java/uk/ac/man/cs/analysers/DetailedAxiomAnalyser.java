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
public class DetailedAxiomAnalyser {

    protected OWLOntology ontology;

    private int OWLClassAxiom;
    //contains
    private int OWLDisjointUnionAxiom;
    private int OWLSubClassOfAxiom;
    //contains
    private int OWLEquivalentClassesAxiom;
    private int OWLNaryClassAxiom;
    private int OWLDisjointClassesAxiom;

    private int OWLDataPropertyAxiom;
    //contains
    private int OWLDataPropertyCharacteristicAxiom;
    //contains
    private int OWLFunctionalDataPropertyAxiom;

    private int OWLAsymmetricObjectPropertyAxiom;
    private int OWLClassAssertionAxiom;
    private int OWLDataPropertyAssertionAxiom;
    private int OWLDataPropertyDomainAxiom;
    private int OWLDataPropertyRangeAxiom;
    private int OWLDatatypeDefinitionAxiom;
    private int OWLDifferentIndividualsAxiom;
    private int OWLDisjointDataPropertiesAxiom;
    private int OWLDisjointObjectPropertiesAxiom;
    private int OWLEquivalentDataPropertiesAxiom;
    private int OWLEquivalentObjectPropertiesAxiom;
    private int OWLFunctionalObjectPropertyAxiom;
    private int OWLHasKeyAxiom;
    private int OWLIndividualAxiom;
    private int OWLInverseFunctionalObjectPropertyAxiom;
    private int OWLInverseObjectPropertiesAxiom;
    private int OWLIrreflexiveObjectPropertyAxiom;
    private int OWLNaryIndividualAxiom;
    private int OWLNaryPropertyAxiom;
    private int OWLNegativeDataPropertyAssertionAxiom;
    private int OWLNegativeObjectPropertyAssertionAxiom;
    private int OWLObjectPropertyAssertionAxiom;
    private int OWLObjectPropertyAxiom;
    private int OWLObjectPropertyCharacteristicAxiom;
    private int OWLObjectPropertyDomainAxiom;
    private int OWLObjectPropertyRangeAxiom;
    private int OWLPropertyAssertionAxiom;
    private int OWLPropertyAxiom;
    private int OWLPropertyDomainAxiom;
    private int OWLPropertyRangeAxiom;
    private int OWLReflexiveObjectPropertyAxiom;
    private int OWLSameIndividualAxiom;
    private int OWLSubDataPropertyOfAxiom;
    private int OWLSubObjectPropertyOfAxiom;
    private int OWLSubPropertyAxiom;
    private int OWLSubPropertyChainOfAxiom;
    private int OWLSymmetricObjectPropertyAxiom;
    private int OWLTransitiveObjectPropertyAxiom;
    private int OWLUnaryPropertyAxiom;
    private int SWRLRule;

    public DetailedAxiomAnalyser(OWLOntology o){
        this.ontology = o; 
        this.OWLAsymmetricObjectPropertyAxiom = 0;
        this.OWLClassAssertionAxiom = 0;
        this.OWLClassAxiom = 0;
        this.OWLDataPropertyAssertionAxiom = 0;
        this.OWLDataPropertyAxiom = 0;
        this.OWLDataPropertyCharacteristicAxiom = 0;
        this.OWLDataPropertyDomainAxiom = 0;
        this.OWLDataPropertyRangeAxiom = 0;
        this.OWLDatatypeDefinitionAxiom = 0;
        this.OWLDifferentIndividualsAxiom = 0;
        this.OWLDisjointClassesAxiom = 0;
        this.OWLDisjointDataPropertiesAxiom = 0;
        this.OWLDisjointObjectPropertiesAxiom = 0;
        this.OWLDisjointUnionAxiom = 0;
        this.OWLEquivalentClassesAxiom = 0;
        this.OWLEquivalentDataPropertiesAxiom = 0;
        this.OWLEquivalentObjectPropertiesAxiom = 0;
        this.OWLFunctionalDataPropertyAxiom = 0;
        this.OWLFunctionalObjectPropertyAxiom = 0;
        this.OWLHasKeyAxiom = 0;
        this.OWLIndividualAxiom = 0;
        this.OWLInverseFunctionalObjectPropertyAxiom = 0;
        this.OWLInverseObjectPropertiesAxiom = 0;
        this.OWLIrreflexiveObjectPropertyAxiom = 0;
        this.OWLNaryClassAxiom = 0;
        this.OWLNaryIndividualAxiom = 0;
        this.OWLNaryPropertyAxiom = 0;
        this.OWLNegativeDataPropertyAssertionAxiom = 0;
        this.OWLNegativeObjectPropertyAssertionAxiom = 0;
        this.OWLObjectPropertyAssertionAxiom = 0;
        this.OWLObjectPropertyAxiom = 0;
        this.OWLObjectPropertyCharacteristicAxiom = 0;
        this.OWLObjectPropertyDomainAxiom = 0;
        this.OWLObjectPropertyRangeAxiom = 0;
        this.OWLPropertyAssertionAxiom = 0;
        this.OWLPropertyAxiom = 0;
        this.OWLPropertyDomainAxiom= 0;
        this.OWLPropertyRangeAxiom = 0;
        this.OWLReflexiveObjectPropertyAxiom = 0;
        this.OWLSameIndividualAxiom = 0;
        this.OWLSubClassOfAxiom = 0;
        this.OWLSubDataPropertyOfAxiom = 0;
        this.OWLSubObjectPropertyOfAxiom = 0;
        this.OWLSubPropertyAxiom = 0;
        this.OWLSubPropertyChainOfAxiom = 0;
        this.OWLSymmetricObjectPropertyAxiom = 0;
        this.OWLTransitiveObjectPropertyAxiom = 0;
        this.OWLUnaryPropertyAxiom = 0;
        this.SWRLRule = 0;
    }

    public OWLOntology getOntology(){
        return this.ontology;
    } 

    public void getStatistics(){
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);
        for(OWLAxiom axiom : tBox){ 
            if(axiom instanceof OWLAsymmetricObjectPropertyAxiom){
                this.OWLAsymmetricObjectPropertyAxiom++;
            }
            if(axiom instanceof OWLClassAssertionAxiom){
                this.OWLClassAssertionAxiom++;
            }
            if(axiom instanceof OWLClassAxiom){
                this.OWLClassAxiom++;
            }
            if(axiom instanceof OWLDataPropertyAssertionAxiom){
                this.OWLDataPropertyAssertionAxiom++;
            }
            if(axiom instanceof OWLDataPropertyAxiom){
                this.OWLDataPropertyAxiom++;
            }
            if(axiom instanceof OWLDataPropertyCharacteristicAxiom){
                this.OWLDataPropertyCharacteristicAxiom++;
            }
            if(axiom instanceof OWLDataPropertyDomainAxiom){
                this.OWLDataPropertyDomainAxiom++;
            }
            if(axiom instanceof OWLDataPropertyRangeAxiom){
                this.OWLDataPropertyRangeAxiom++;
            }
            if(axiom instanceof OWLDatatypeDefinitionAxiom){
                this.OWLDatatypeDefinitionAxiom++;
            }
            if(axiom instanceof OWLDifferentIndividualsAxiom){
                this.OWLDifferentIndividualsAxiom++;
            }
            if(axiom instanceof OWLDisjointClassesAxiom){
                this.OWLDisjointClassesAxiom++;
            }
            if(axiom instanceof OWLDisjointDataPropertiesAxiom){
                this.OWLDisjointDataPropertiesAxiom++;
            }
            if(axiom instanceof OWLDisjointObjectPropertiesAxiom){
                this.OWLDisjointObjectPropertiesAxiom++;
            }
            if(axiom instanceof OWLDisjointUnionAxiom){
                this.OWLDisjointUnionAxiom++;
            }
            if(axiom instanceof OWLEquivalentClassesAxiom){
                this.OWLEquivalentClassesAxiom++;
            }
            if(axiom instanceof OWLEquivalentDataPropertiesAxiom){
                this.OWLEquivalentDataPropertiesAxiom++;
            }
            if(axiom instanceof OWLEquivalentObjectPropertiesAxiom){
                this.OWLEquivalentObjectPropertiesAxiom++;
            }
            if(axiom instanceof OWLFunctionalDataPropertyAxiom){
                this.OWLFunctionalDataPropertyAxiom++;
            }
            if(axiom instanceof OWLFunctionalObjectPropertyAxiom){
                this.OWLFunctionalObjectPropertyAxiom++;
            }
            if(axiom instanceof OWLHasKeyAxiom){
                this.OWLHasKeyAxiom++;
            }
            if(axiom instanceof OWLIndividualAxiom){
                this.OWLIndividualAxiom++;
            }
            if(axiom instanceof OWLInverseFunctionalObjectPropertyAxiom){
                this.OWLInverseFunctionalObjectPropertyAxiom++;
            }
            if(axiom instanceof OWLInverseObjectPropertiesAxiom){
                this.OWLInverseObjectPropertiesAxiom++;
            }
            if(axiom instanceof OWLIrreflexiveObjectPropertyAxiom){
                this.OWLIrreflexiveObjectPropertyAxiom++;
            }
            if(axiom instanceof OWLNaryClassAxiom){
                this.OWLNaryClassAxiom++;
            }
            if(axiom instanceof OWLNaryIndividualAxiom){
                this.OWLNaryIndividualAxiom++;
            }
            if(axiom instanceof OWLNaryPropertyAxiom){
                this.OWLNaryPropertyAxiom++;
            }
            if(axiom instanceof OWLNegativeDataPropertyAssertionAxiom){
                this.OWLNegativeDataPropertyAssertionAxiom++;
            }
            if(axiom instanceof OWLNegativeObjectPropertyAssertionAxiom){
                this.OWLNegativeObjectPropertyAssertionAxiom++;
            }
            if(axiom instanceof OWLObjectPropertyAssertionAxiom){
                this.OWLObjectPropertyAssertionAxiom++;
            }
            if(axiom instanceof OWLObjectPropertyAxiom){
                this.OWLObjectPropertyAxiom++;
            }
            if(axiom instanceof OWLObjectPropertyCharacteristicAxiom){
                this.OWLObjectPropertyCharacteristicAxiom++;
            }
            if(axiom instanceof OWLObjectPropertyDomainAxiom){
                this.OWLObjectPropertyDomainAxiom++;
            }
            if(axiom instanceof OWLObjectPropertyRangeAxiom){
                this.OWLObjectPropertyRangeAxiom++;
            }
            if(axiom instanceof OWLPropertyAssertionAxiom){
                this.OWLPropertyAssertionAxiom++;
            }
            if(axiom instanceof OWLPropertyAxiom){
                this.OWLPropertyAxiom++;
            }
            if(axiom instanceof OWLPropertyDomainAxiom){
                this.OWLPropertyDomainAxiom++;
            }
            if(axiom instanceof OWLPropertyRangeAxiom){
                this.OWLPropertyRangeAxiom++;
            }
            if(axiom instanceof OWLReflexiveObjectPropertyAxiom){
                this.OWLReflexiveObjectPropertyAxiom++;
            }
            if(axiom instanceof OWLSameIndividualAxiom){
                this.OWLSameIndividualAxiom++;
            }
            if(axiom instanceof OWLSubClassOfAxiom){
                this.OWLSubClassOfAxiom++;
            }
            if(axiom instanceof OWLSubDataPropertyOfAxiom){
                this.OWLSubDataPropertyOfAxiom++;
            }
            if(axiom instanceof OWLSubObjectPropertyOfAxiom){
                this.OWLSubObjectPropertyOfAxiom++;
            }
            if(axiom instanceof OWLSubPropertyAxiom){
                this.OWLSubPropertyAxiom++;
            }
            if(axiom instanceof OWLSubPropertyChainOfAxiom){
                this.OWLSubPropertyChainOfAxiom++;
            }
            if(axiom instanceof OWLSymmetricObjectPropertyAxiom){
                this.OWLSymmetricObjectPropertyAxiom++;
            }
            if(axiom instanceof OWLTransitiveObjectPropertyAxiom){
                this.OWLTransitiveObjectPropertyAxiom++;
            }
            if(axiom instanceof OWLUnaryPropertyAxiom){
                this.OWLUnaryPropertyAxiom++;
            }
            if(axiom instanceof SWRLRule){
                this.SWRLRule++;
            } 
        } 
    }

    public void printClassAxioms(){
        if(OWLClassAxiom > 0){
            System.out.println("OWLClassAxiom " + OWLClassAxiom);

            if(OWLDisjointUnionAxiom > 0){
                System.out.println("OWLDisjointUnionAxiom " + OWLDisjointUnionAxiom);
            }
            if(OWLSubClassOfAxiom > 0){
                System.out.println("OWLSubClassOfAxiom " + OWLSubClassOfAxiom);
            }
            if(OWLNaryClassAxiom > 0){
                System.out.println("OWLNaryClassAxiom " + OWLNaryClassAxiom);

                if(OWLDisjointClassesAxiom > 0){
                    System.out.println("OWLDisjointClassesAxiom " + OWLDisjointClassesAxiom);
                }

                if(OWLEquivalentClassesAxiom > 0){
                    System.out.println("OWLEquivalentClassesAxiom " + OWLEquivalentClassesAxiom);
                }
            }
        } 
    }

    public void print(){ 

        printClassAxioms();

        if(OWLAsymmetricObjectPropertyAxiom > 0){
            System.out.println("OWLAsymmetricObjectPropertyAxiom " + OWLAsymmetricObjectPropertyAxiom);
        }
        if(OWLClassAssertionAxiom > 0){
            System.out.println("OWLClassAssertionAxiom " + OWLClassAssertionAxiom);
        }
        if(OWLDataPropertyAssertionAxiom > 0){
            System.out.println("OWLDataPropertyAssertionAxiom " + OWLDataPropertyAssertionAxiom);
        }
        if(OWLDataPropertyAxiom > 0){
            System.out.println("OWLDataPropertyAxiom " + OWLDataPropertyAxiom);
        }
        if(OWLDataPropertyCharacteristicAxiom > 0){
            System.out.println("OWLDataPropertyCharacteristicAxiom " + OWLDataPropertyCharacteristicAxiom);
        }
        if(OWLDataPropertyDomainAxiom > 0){
            System.out.println("OWLDataPropertyDomainAxiom " + OWLDataPropertyDomainAxiom);
        }
        if(OWLDataPropertyRangeAxiom > 0){
            System.out.println("OWLDataPropertyRangeAxiom " + OWLDataPropertyRangeAxiom);
        }
        if(OWLDatatypeDefinitionAxiom > 0){
            System.out.println("OWLDatatypeDefinitionAxiom " + OWLDatatypeDefinitionAxiom);
        }
        if(OWLDifferentIndividualsAxiom > 0){
            System.out.println("OWLDifferentIndividualsAxiom " + OWLDifferentIndividualsAxiom);
        }
        if(OWLDisjointDataPropertiesAxiom > 0){
            System.out.println("OWLDisjointDataPropertiesAxiom " + OWLDisjointDataPropertiesAxiom);
        }
        if(OWLDisjointObjectPropertiesAxiom > 0){
            System.out.println("OWLDisjointObjectPropertiesAxiom " + OWLDisjointObjectPropertiesAxiom);
        }
        if(OWLEquivalentDataPropertiesAxiom > 0){
            System.out.println("OWLEquivalentDataPropertiesAxiom " + OWLEquivalentDataPropertiesAxiom);
        }
        if(OWLEquivalentObjectPropertiesAxiom > 0){
            System.out.println("OWLEquivalentObjectPropertiesAxiom " + OWLEquivalentObjectPropertiesAxiom);
        }
        if(OWLFunctionalDataPropertyAxiom > 0){
            System.out.println("OWLFunctionalDataPropertyAxiom " + OWLFunctionalDataPropertyAxiom);
        }
        if(OWLFunctionalObjectPropertyAxiom > 0){
            System.out.println("OWLFunctionalObjectPropertyAxiom " + OWLFunctionalObjectPropertyAxiom);
        }
        if(OWLHasKeyAxiom > 0){
            System.out.println("OWLHasKeyAxiom " + OWLHasKeyAxiom);
        }
        if(OWLIndividualAxiom > 0){
            System.out.println("OWLIndividualAxiom " + OWLIndividualAxiom);
        }
        if(OWLInverseFunctionalObjectPropertyAxiom > 0){
            System.out.println("OWLInverseFunctionalObjectPropertyAxiom " + OWLInverseFunctionalObjectPropertyAxiom);
        }
        if(OWLInverseObjectPropertiesAxiom > 0){
            System.out.println("OWLInverseObjectPropertiesAxiom " + OWLInverseObjectPropertiesAxiom);
        }
        if(OWLIrreflexiveObjectPropertyAxiom > 0){
            System.out.println("OWLIrreflexiveObjectPropertyAxiom " + OWLIrreflexiveObjectPropertyAxiom);
        }
        if(OWLNaryIndividualAxiom > 0){
            System.out.println("OWLNaryIndividualAxiom " + OWLNaryIndividualAxiom);
        }
        if(OWLNaryPropertyAxiom > 0){
            System.out.println("OWLNaryPropertyAxiom " + OWLNaryPropertyAxiom);
        }
        if(OWLNegativeDataPropertyAssertionAxiom > 0){
            System.out.println("OWLNegativeDataPropertyAssertionAxiom " + OWLNegativeDataPropertyAssertionAxiom);
        }
        if(OWLNegativeObjectPropertyAssertionAxiom > 0){
            System.out.println("OWLNegativeObjectPropertyAssertionAxiom " + OWLNegativeObjectPropertyAssertionAxiom);
        }
        if(OWLObjectPropertyAssertionAxiom > 0){
            System.out.println("OWLObjectPropertyAssertionAxiom " + OWLObjectPropertyAssertionAxiom);
        }
        if(OWLObjectPropertyAxiom > 0){
            System.out.println("OWLObjectPropertyAxiom " + OWLObjectPropertyAxiom);
        }
        if(OWLObjectPropertyCharacteristicAxiom > 0){
            System.out.println("OWLObjectPropertyCharacteristicAxiom " + OWLObjectPropertyCharacteristicAxiom);
        }
        if(OWLObjectPropertyDomainAxiom > 0){
            System.out.println("OWLObjectPropertyDomainAxiom " + OWLObjectPropertyDomainAxiom);
        }
        if(OWLObjectPropertyRangeAxiom > 0){
            System.out.println("OWLObjectPropertyRangeAxiom " + OWLObjectPropertyRangeAxiom);
        }
        if(OWLPropertyAssertionAxiom > 0){
            System.out.println("OWLPropertyAssertionAxiom " + OWLPropertyAssertionAxiom);
        }
        if(OWLPropertyAxiom > 0){
            System.out.println("OWLPropertyAxiom " + OWLPropertyAxiom);
        }
        if(OWLPropertyDomainAxiom > 0){
            System.out.println("OWLPropertyDomainAxiom " + OWLPropertyDomainAxiom);
        }
        if(OWLPropertyRangeAxiom > 0){
            System.out.println("OWLPropertyRangeAxiom " + OWLPropertyRangeAxiom);
        }
        if(OWLReflexiveObjectPropertyAxiom > 0){
            System.out.println("OWLReflexiveObjectPropertyAxiom " + OWLReflexiveObjectPropertyAxiom);
        }
        if(OWLSameIndividualAxiom > 0){
            System.out.println("OWLSameIndividualAxiom " + OWLSameIndividualAxiom);
        }
        if(OWLSubDataPropertyOfAxiom > 0){
            System.out.println("OWLSubDataPropertyOfAxiom " + OWLSubDataPropertyOfAxiom);
        }
        if(OWLSubObjectPropertyOfAxiom > 0){
            System.out.println("OWLSubObjectPropertyOfAxiom " + OWLSubObjectPropertyOfAxiom);
        }
        if(OWLSubPropertyAxiom > 0){
            System.out.println("OWLSubPropertyAxiom " + OWLSubPropertyAxiom);
        }
        if(OWLSubPropertyChainOfAxiom > 0){
            System.out.println("OWLSubPropertyChainOfAxiom " + OWLSubPropertyChainOfAxiom);
        }
        if(OWLSymmetricObjectPropertyAxiom > 0){
            System.out.println("OWLSymmetricObjectPropertyAxiom " + OWLSymmetricObjectPropertyAxiom);
        }
        if(OWLTransitiveObjectPropertyAxiom > 0){
            System.out.println("OWLTransitiveObjectPropertyAxiom " + OWLTransitiveObjectPropertyAxiom);
        }
        if(OWLUnaryPropertyAxiom > 0){
            System.out.println("OWLUnaryPropertyAxiom " + OWLUnaryPropertyAxiom);
        }
        if(SWRLRule > 0){
            System.out.println("SWRLRule " + SWRLRule);
        } 
    }

    protected HashMap<OWLEntity,Set<OWLAxiom>> axiomUsage(OWLAxiom a){
        HashMap<OWLEntity,Set<OWLAxiom>> res = new HashMap<>();
        for(OWLEntity e : a.getSignature()){
            res.put(e, entityUsage(e));
        }
        return res; 
    }

    protected Set<OWLAxiom> entityUsage(OWLEntity entity){
        Set<OWLAxiom> res = new HashSet<>(); 
        for(OWLAxiom axiom : ontology.getAxioms(Imports.EXCLUDED)){
            if((axiom.getSignature()).contains(entity)){
                    res.add(axiom);
            }
        }
        return res; 
    }

    protected Set<OWLAxiom> getSubsumptionAxioms(){ 
        Set<OWLAxiom> tBox = this.ontology.getTBoxAxioms(Imports.EXCLUDED);

        Set<OWLAxiom> subClassAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.SUBCLASS_OF); 
        Set<OWLAxiom> subsumptionAxioms = new HashSet<>(); 
        subsumptionAxioms.addAll(subClassAxioms);

        Set<OWLAxiom> equivAxioms = AxiomType.getAxiomsOfTypes(tBox, AxiomType.EQUIVALENT_CLASSES);
        for(OWLAxiom a : equivAxioms){
            subsumptionAxioms.addAll(((OWLEquivalentClassesAxiom) a).asOWLSubClassOfAxioms()); 
        } 
        return subsumptionAxioms;
    }
}

