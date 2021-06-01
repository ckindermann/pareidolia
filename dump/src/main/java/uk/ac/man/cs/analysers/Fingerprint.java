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
public class Fingerprint {

    private String name;

    public int classAxioms; 
    public int disjointUnionAxioms;
    public int subClassOfAxioms;
    public int naryClassAxioms;
    public int equivalentClassesAxioms;
    public int disjointClassesAxioms;

    public int propertyAxioms; 
    public int dataPropertyAxioms;
    public int naryPropertyAxioms;
    public int subPropertyAxioms;
    public int unaryPropertyAxioms;
    public int objectPropertyAxiom;
    public int dataPropertyCharacteristicAxiom;
    public int propertyDomainAxiom;
    public int propertyRangeAxiom;
    public int disjointDataPropertiesAxiom ;
    public int equivalentDataPropertiesAxiom;
    public int subDataPropertyOfAxiom;
    public int disjointObjectPropertiesAxiom;
    public int equivalentObjectPropertiesAxiom;
    public int inverseObjectPropertiesAxiom;
    public int subObjectPropertyOfAxiom;
    public int subPropertyChainOfAxiom; 
    public int objectPropertyCharacteristicAxiom; 
    public int inverseFunctionalObjectPropertyAxiom;
    public int asymmetricObjectPropertyAxiom;
    public int functionalObjectPropertyAxiom;
    public int irreflexiveObjectPropertyAxiom;
    public int reflexiveObjectPropertyAxiom;
    public int symmetricObjectPropertyAxiom;
    public int transitiveObjectPropertyAxiom;
    public int objectPropertyRangeAxiom;
    public int dataPropertyDomainAxiom;
    public int objectPropertyDomainAxiom;
    public int functionalDataPropertyAxiom;
    public int dataPropertyRangeAxiom;

    public int individualAxioms; 
    public int classAssertionAxioms;
    public int naryIndividualAxioms;
    public int propertyAssertionAxioms;
    public int negativeObjectPropertyAssertionAxiom;
    public int differentIndividualsAxiom;
    public int sameIndividualAxiom;
    public int dataPropertyAssertionAxiom;
    public int negativeDataPropertyAssertionAxiom;
    public int objectPropertyAssertionAxiom;

    public Fingerprint(File fp) throws Exception {
        this.name = fp.getName(); 

        LinkedList<String[]> fingerprint  = parseCSV(fp); 
        Iterator<String[]> it = fingerprint.iterator();
        String [] classFingerprint = it.next();
        String [] propertyFingerprint = it.next();
        String [] individualFingerprint = it.next();

        classAxioms = Integer.parseInt(classFingerprint[0]);
        disjointUnionAxioms = Integer.parseInt(classFingerprint[1]);
        subClassOfAxioms = Integer.parseInt(classFingerprint[2]);
        naryClassAxioms = Integer.parseInt(classFingerprint[3]);
        equivalentClassesAxioms = Integer.parseInt(classFingerprint[4]);
        disjointClassesAxioms = Integer.parseInt(classFingerprint[5]);

        propertyAxioms = Integer.parseInt(propertyFingerprint[0]);
        dataPropertyAxioms = Integer.parseInt(propertyFingerprint[1]);
        naryPropertyAxioms = Integer.parseInt(propertyFingerprint[2]);
        subPropertyAxioms = Integer.parseInt(propertyFingerprint[3]);
        unaryPropertyAxioms = Integer.parseInt(propertyFingerprint[4]);
        objectPropertyAxiom = Integer.parseInt(propertyFingerprint[5]);
        dataPropertyCharacteristicAxiom = Integer.parseInt(propertyFingerprint[6]);
        propertyDomainAxiom = Integer.parseInt(propertyFingerprint[7]);
        propertyRangeAxiom = Integer.parseInt(propertyFingerprint[8]);
        disjointDataPropertiesAxiom = Integer.parseInt(propertyFingerprint[9]);
        equivalentDataPropertiesAxiom = Integer.parseInt(propertyFingerprint[10]);
        subDataPropertyOfAxiom = Integer.parseInt(propertyFingerprint[11]);
        disjointObjectPropertiesAxiom = Integer.parseInt(propertyFingerprint[12]);
        equivalentObjectPropertiesAxiom = Integer.parseInt(propertyFingerprint[13]);
        inverseObjectPropertiesAxiom = Integer.parseInt(propertyFingerprint[14]);
        subObjectPropertyOfAxiom = Integer.parseInt(propertyFingerprint[15]);
        subPropertyChainOfAxiom = Integer.parseInt(propertyFingerprint[16]);
        objectPropertyCharacteristicAxiom = Integer.parseInt(propertyFingerprint[17]);
        inverseFunctionalObjectPropertyAxiom = Integer.parseInt(propertyFingerprint[18]);
        asymmetricObjectPropertyAxiom = Integer.parseInt(propertyFingerprint[19]);
        functionalObjectPropertyAxiom = Integer.parseInt(propertyFingerprint[20]);
        irreflexiveObjectPropertyAxiom = Integer.parseInt(propertyFingerprint[21]);
        reflexiveObjectPropertyAxiom = Integer.parseInt(propertyFingerprint[22]);
        symmetricObjectPropertyAxiom = Integer.parseInt(propertyFingerprint[23]);
        transitiveObjectPropertyAxiom = Integer.parseInt(propertyFingerprint[24]);
        objectPropertyRangeAxiom = Integer.parseInt(propertyFingerprint[25]);
        dataPropertyDomainAxiom = Integer.parseInt(propertyFingerprint[26]);
        objectPropertyDomainAxiom = Integer.parseInt(propertyFingerprint[27]);
        functionalDataPropertyAxiom = Integer.parseInt(propertyFingerprint[28]);
        dataPropertyRangeAxiom = Integer.parseInt(propertyFingerprint[29]); 

        individualAxioms = Integer.parseInt(individualFingerprint[0]);
        classAssertionAxioms = Integer.parseInt(individualFingerprint[1]);
        naryIndividualAxioms = Integer.parseInt(individualFingerprint[2]);
        propertyAssertionAxioms = Integer.parseInt(individualFingerprint[3]); 
        negativeObjectPropertyAssertionAxiom = Integer.parseInt(individualFingerprint[4]);
        differentIndividualsAxiom = Integer.parseInt(individualFingerprint[5]);
        sameIndividualAxiom = Integer.parseInt(individualFingerprint[6]);
        dataPropertyAssertionAxiom = Integer.parseInt(individualFingerprint[7]);
        negativeDataPropertyAssertionAxiom = Integer.parseInt(individualFingerprint[8]);
        objectPropertyAssertionAxiom = Integer.parseInt(individualFingerprint[9]); 

    } 

    public boolean coversClassAxioms(Fingerprint f){
        if(this.classAxioms < f.classAxioms) 
            return false;
        if(this.disjointUnionAxioms < f.disjointUnionAxioms)
            return false;
        if(this.subClassOfAxioms < f.subClassOfAxioms)
            return false;
        if(this.naryClassAxioms < f.naryClassAxioms)
            return false;
        if(this.equivalentClassesAxioms < f.equivalentClassesAxioms)
            return false;
        if(this.disjointClassesAxioms < f.disjointClassesAxioms)
            return false;

        return true; 
    }

    public boolean coversPropertyAxioms(Fingerprint f){
        if(this.propertyAxioms < f.propertyAxioms)
            return false;
        if(this.dataPropertyAxioms < f.dataPropertyAxioms)
            return false;
        if(this.naryPropertyAxioms < f.naryPropertyAxioms)
            return false;
        if(this.subPropertyAxioms < f.subPropertyAxioms)
            return false;
        if(this.unaryPropertyAxioms < f.unaryPropertyAxioms)
            return false;
        if(this.objectPropertyAxiom < f.objectPropertyAxiom)
            return false;
        if(this.dataPropertyCharacteristicAxiom < f.dataPropertyCharacteristicAxiom)
            return false;
        if(this.propertyDomainAxiom < f.propertyDomainAxiom)
            return false;
        if(this.propertyRangeAxiom < f.propertyRangeAxiom)
            return false;
        if(this.disjointDataPropertiesAxiom  < f.disjointDataPropertiesAxiom)
            return false;
        if(this.equivalentDataPropertiesAxiom < f.equivalentDataPropertiesAxiom)
            return false;
        if(this.subDataPropertyOfAxiom < f.subDataPropertyOfAxiom)
            return false;
        if(this.disjointObjectPropertiesAxiom < f.disjointObjectPropertiesAxiom)
            return false;
        if(this.equivalentObjectPropertiesAxiom < f.equivalentObjectPropertiesAxiom)
            return false;
        if(this.inverseObjectPropertiesAxiom < f.inverseObjectPropertiesAxiom)
            return false;
        if(this.subObjectPropertyOfAxiom < f.subObjectPropertyOfAxiom)
            return false;
        if(this.subPropertyChainOfAxiom < f.subPropertyChainOfAxiom)
            return false;
        if(this.objectPropertyCharacteristicAxiom < f.objectPropertyCharacteristicAxiom)
            return false; 
        if(this.inverseFunctionalObjectPropertyAxiom < f.inverseFunctionalObjectPropertyAxiom)
            return false; 
        if(this.asymmetricObjectPropertyAxiom < f.asymmetricObjectPropertyAxiom)
            return false; 
        if(this.functionalObjectPropertyAxiom < f.functionalObjectPropertyAxiom)
            return false; 
        if(this.irreflexiveObjectPropertyAxiom < f.irreflexiveObjectPropertyAxiom)
            return false; 
        if(this.reflexiveObjectPropertyAxiom < f.reflexiveObjectPropertyAxiom)
            return false; 
        if(this.symmetricObjectPropertyAxiom < f.symmetricObjectPropertyAxiom)
            return false; 
        if(this.transitiveObjectPropertyAxiom < f.transitiveObjectPropertyAxiom)
            return false; 
        if(this.objectPropertyRangeAxiom < f.objectPropertyRangeAxiom)
            return false; 
        if(this.dataPropertyDomainAxiom < f.dataPropertyDomainAxiom)
            return false; 
        if(this.objectPropertyDomainAxiom < f.objectPropertyDomainAxiom)
            return false; 
        if(this.functionalDataPropertyAxiom < f.functionalDataPropertyAxiom)
            return false; 
        if(this.dataPropertyRangeAxiom < f.dataPropertyRangeAxiom)
            return false; 

        return true;
    }

    public boolean coversIndividualAxioms(Fingerprint f){
        if(this.individualAxioms < f.individualAxioms)
            return false;
        if(this.classAssertionAxioms < f.classAssertionAxioms)
            return false;
        if(this.naryIndividualAxioms < f.naryIndividualAxioms)
            return false;
        if(this.propertyAssertionAxioms < f.propertyAssertionAxioms)
            return false; 
        if(this.negativeObjectPropertyAssertionAxiom < f.negativeObjectPropertyAssertionAxiom)
            return false; 
        if(this.differentIndividualsAxiom < f.differentIndividualsAxiom)
            return false; 
        if(this.sameIndividualAxiom < f.sameIndividualAxiom)
            return false; 
        if(this.dataPropertyAssertionAxiom < f.dataPropertyAssertionAxiom)
            return false; 
        if(this.negativeDataPropertyAssertionAxiom < f.negativeDataPropertyAssertionAxiom)
            return false; 
        if(this.objectPropertyAssertionAxiom < f.objectPropertyAssertionAxiom)
            return false; 

        return true;
    }

    public boolean covers(Fingerprint f){

        if(!coversClassAxioms(f))
            return false;

        if(!coversPropertyAxioms(f))
            return false;

        if(!coversIndividualAxioms(f))
            return false; 

        return true;

    }

    public void print(){ 
        System.out.println("Class numbers");
        System.out.println(classAxioms);
        System.out.println(disjointUnionAxioms);
        System.out.println(subClassOfAxioms);
        System.out.println(naryClassAxioms);
        System.out.println(equivalentClassesAxioms);
        System.out.println(disjointClassesAxioms);

        System.out.println("Property numbers");
        System.out.println(propertyAxioms); 
        System.out.println(dataPropertyAxioms);
        System.out.println(naryPropertyAxioms);
        System.out.println(subPropertyAxioms);
        System.out.println(unaryPropertyAxioms);
        System.out.println(objectPropertyAxiom);
        System.out.println(dataPropertyCharacteristicAxiom);
        System.out.println(propertyDomainAxiom);
        System.out.println(propertyRangeAxiom);
        System.out.println(disjointDataPropertiesAxiom );
        System.out.println(equivalentDataPropertiesAxiom);
        System.out.println(subDataPropertyOfAxiom);
        System.out.println(disjointObjectPropertiesAxiom);
        System.out.println(equivalentObjectPropertiesAxiom);
        System.out.println(inverseObjectPropertiesAxiom);
        System.out.println(subObjectPropertyOfAxiom);
        System.out.println(subPropertyChainOfAxiom); 
        System.out.println(objectPropertyCharacteristicAxiom); 

        System.out.println(" Individual numbers");
        System.out.println(individualAxioms); 
        System.out.println(classAssertionAxioms);
        System.out.println(naryIndividualAxioms);
        System.out.println(propertyAssertionAxioms);
    }

    public String getName(){
        return this.name; 
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

