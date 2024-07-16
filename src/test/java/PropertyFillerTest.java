import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import vocabulary.SBMLRDF;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PropertyFillerTest {

    org.apache.jena.rdf.model.Model rdf;

    Resource cmp1, cmp2, cmp;
    Resource a1, a2, b1, b2, c1, c2, d, e;
    Resource r1, r1_2, r2, r3, rta, rtc;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() throws XMLStreamException {
        String baseUri = "org.mytest";
        rdf = ModelFactory.createDefaultModel();
        //build metab node
        a1 = ResourceFactory.createResource((baseUri + "#a1"));rdf.add(a1, RDF.type,SBMLRDF.SPECIE);
        a2 = ResourceFactory.createResource((baseUri+"#a2"));rdf.add(a2, RDF.type,SBMLRDF.SPECIE);
        b1 = ResourceFactory.createResource((baseUri+"#b1"));rdf.add(b1, RDF.type,SBMLRDF.SPECIE);
        b2 = ResourceFactory.createResource((baseUri+"#b2"));rdf.add(b2, RDF.type,SBMLRDF.SPECIE);
        c1 = ResourceFactory.createResource((baseUri+"#c1"));rdf.add(c1, RDF.type,SBMLRDF.SPECIE);
        c2 = ResourceFactory.createResource((baseUri+"#c2"));rdf.add(c2, RDF.type,SBMLRDF.SPECIE);
        d = ResourceFactory.createResource((baseUri+"#d"));rdf.add(d, RDF.type,SBMLRDF.SPECIE);
        e = ResourceFactory.createResource((baseUri+"#e"));rdf.add(e, RDF.type,SBMLRDF.SPECIE);
        //build compartment node
        cmp1 = ResourceFactory.createResource((baseUri+"#cmp1"));rdf.add(cmp1, RDF.type,SBMLRDF.COMPARTMENT);
        cmp2 = ResourceFactory.createResource((baseUri+"#cmp2"));rdf.add(cmp2, RDF.type,SBMLRDF.COMPARTMENT);
        cmp = ResourceFactory.createResource((baseUri+"#cmp3"));rdf.add(cmp, RDF.type,SBMLRDF.COMPARTMENT);
        //build reaction node
        r1 = ResourceFactory.createResource((baseUri+"#r1"));rdf.add(r1, RDF.type,SBMLRDF.REACTION);
        r1_2 = ResourceFactory.createResource((baseUri+"#r1_2"));rdf.add(r1_2, RDF.type,SBMLRDF.REACTION);
        r2 = ResourceFactory.createResource((baseUri+"#r2"));rdf.add(r2, RDF.type,SBMLRDF.REACTION);
        r3 = ResourceFactory.createResource((baseUri+"#r3"));rdf.add(r3, RDF.type,SBMLRDF.REACTION);
        rtc = ResourceFactory.createResource((baseUri+"#rtc"));rdf.add(rtc, RDF.type,SBMLRDF.REACTION);
        rta = ResourceFactory.createResource((baseUri+"#rta"));rdf.add(rta, RDF.type,SBMLRDF.REACTION);
        rdf.addLiteral(r1, SBMLRDF.REVERSIBLE,false);
        rdf.addLiteral(r1, SBMLRDF.REVERSIBLE, false);
        rdf.addLiteral(r1_2, SBMLRDF.REVERSIBLE,false);
        rdf.addLiteral(r2, SBMLRDF.REVERSIBLE,true);
        rdf.addLiteral(r3, SBMLRDF.REVERSIBLE,false);
        rdf.addLiteral(rta, SBMLRDF.REVERSIBLE,true);
        rdf.addLiteral(rtc, SBMLRDF.REVERSIBLE,true);

        rdf.add(a1, SBMLRDF.NAME, "A");
        rdf.add(a2, SBMLRDF.NAME, "A");
        rdf.add(b1, SBMLRDF.NAME, "B");
        rdf.add(b2, SBMLRDF.NAME, "B");
        rdf.add(c1, SBMLRDF.NAME, "C");
        rdf.add(c2, SBMLRDF.NAME, "C");
        rdf.add(d, SBMLRDF.NAME, "D");
        rdf.add(e, SBMLRDF.NAME, "E");

        rdf.add(a1, RDFS.label, "a1");
        rdf.add(a2, RDFS.label, "a2");
        rdf.add(b1, RDFS.label, "b1");
        rdf.add(b2, RDFS.label, "b2");
        rdf.add(c1, RDFS.label, "c1");
        rdf.add(c2, RDFS.label, "c2");
        rdf.add(d, RDFS.label, "d");
        rdf.add(e, RDFS.label, "e");


        rdf.add(a1,SBMLRDF.HAS_COMPARTMENT, cmp1);
        rdf.add(a2,SBMLRDF.HAS_COMPARTMENT, cmp2);
        rdf.add(b1,SBMLRDF.HAS_COMPARTMENT, cmp1);
        rdf.add(b2,SBMLRDF.HAS_COMPARTMENT, cmp2);
        rdf.add(c1,SBMLRDF.HAS_COMPARTMENT, cmp1);
        rdf.add(c2,SBMLRDF.HAS_COMPARTMENT, cmp2);
        rdf.add(d,SBMLRDF.HAS_COMPARTMENT, cmp1);
        rdf.add(e,SBMLRDF.HAS_COMPARTMENT, cmp2);

        Resource a1r1= ResourceFactory.createResource();rdf.add(a1r1, RDF.type,SBMLRDF.SPECIESREF);
        rdf.add(a1r1, SBMLRDF.HAS_SPECIE, a1);
        rdf.add(r1, SBMLRDF.REACTANT,a1r1);
        rdf.addLiteral(a1r1, SBMLRDF.STOICHIOMETRY,2.0);
        Resource a2r1_2= ResourceFactory.createResource();rdf.add(a2r1_2, RDF.type,SBMLRDF.SPECIESREF);
        rdf.add(a2r1_2, SBMLRDF.HAS_SPECIE, a2);
        rdf.add(r1_2, SBMLRDF.REACTANT,a2r1_2);
        rdf.addLiteral(a2r1_2, SBMLRDF.STOICHIOMETRY,2.0);
        Resource b1r1= ResourceFactory.createResource();rdf.add(b1r1, RDF.type,SBMLRDF.SPECIESREF);
        rdf.add(b1r1, SBMLRDF.HAS_SPECIE, b1);
        rdf.add(r1, SBMLRDF.PRODUCT,b1r1);
        rdf.addLiteral(b1r1, SBMLRDF.STOICHIOMETRY,1.0);
        Resource b2r1_2= ResourceFactory.createResource();rdf.add(b2r1_2, RDF.type,SBMLRDF.SPECIESREF);
        rdf.add(b2r1_2, SBMLRDF.HAS_SPECIE, b2);
        rdf.add(r1_2, SBMLRDF.PRODUCT,b2r1_2);
        rdf.addLiteral(b2r1_2, SBMLRDF.STOICHIOMETRY,1.0);
        Resource c1r1= ResourceFactory.createResource();rdf.add(c1r1, RDF.type,SBMLRDF.SPECIESREF);
        rdf.add(c1r1, SBMLRDF.HAS_SPECIE, c1);
        rdf.add(r1, SBMLRDF.PRODUCT,c1r1);
        rdf.addLiteral(c1r1, SBMLRDF.STOICHIOMETRY,1.0);
        Resource c1r2= ResourceFactory.createResource();rdf.add(c1r2, RDF.type,SBMLRDF.SPECIESREF);
        rdf.add(c1r2, SBMLRDF.HAS_SPECIE, c1);
        rdf.add(r2, SBMLRDF.REACTANT,c1r2);
        rdf.addLiteral(c1r2, SBMLRDF.STOICHIOMETRY,4.0);
        Resource c2r1_2= ResourceFactory.createResource();rdf.add(c2r1_2, RDF.type,SBMLRDF.SPECIESREF);
        rdf.add(c2r1_2, SBMLRDF.HAS_SPECIE, c2);
        rdf.add(r1_2, SBMLRDF.PRODUCT,c2r1_2);
        rdf.addLiteral(c2r1_2, SBMLRDF.STOICHIOMETRY,1.0);
        Resource c2r3= ResourceFactory.createResource();rdf.add(c2r3, RDF.type,SBMLRDF.SPECIESREF);
        rdf.add(c2r3, SBMLRDF.HAS_SPECIE, c2);
        rdf.add(r3, SBMLRDF.REACTANT,c2r3);
        rdf.addLiteral(c2r3, SBMLRDF.STOICHIOMETRY,4.0);
        Resource dr2= ResourceFactory.createResource();rdf.add(dr2, RDF.type,SBMLRDF.SPECIESREF);
        rdf.add(dr2, SBMLRDF.HAS_SPECIE, d);
        rdf.add(r2, SBMLRDF.PRODUCT,dr2);
        rdf.addLiteral(dr2, SBMLRDF.STOICHIOMETRY,4.0);
        Resource er3= ResourceFactory.createResource();rdf.add(er3, RDF.type,SBMLRDF.SPECIESREF);
        rdf.add(er3, SBMLRDF.HAS_SPECIE, e);
        rdf.add(r3, SBMLRDF.PRODUCT,er3);
        rdf.addLiteral(er3, SBMLRDF.STOICHIOMETRY,4.0);

        Resource c1rtc= ResourceFactory.createResource();rdf.add(c1rtc, RDF.type,SBMLRDF.SPECIESREF);
        rdf.add(c1rtc, SBMLRDF.HAS_SPECIE, c1);
        rdf.add(rtc, SBMLRDF.REACTANT,c1rtc);
        rdf.addLiteral(c1r1, SBMLRDF.STOICHIOMETRY,1.0);
        Resource c2rtc= ResourceFactory.createResource();rdf.add(c1rtc, RDF.type,SBMLRDF.SPECIESREF);
        rdf.add(c2rtc, SBMLRDF.HAS_SPECIE, c2);
        rdf.add(rtc, SBMLRDF.PRODUCT,c2rtc);
        rdf.addLiteral(c2rtc, SBMLRDF.STOICHIOMETRY,1.0);

        Resource a1rta= ResourceFactory.createResource();rdf.add(c1rtc, RDF.type,SBMLRDF.SPECIESREF);
        rdf.add(a1rta, SBMLRDF.HAS_SPECIE, a1);
        rdf.add(rta, SBMLRDF.PRODUCT,a1rta);
        rdf.addLiteral(a1rta, SBMLRDF.STOICHIOMETRY,1.0);
        Resource a2rta= ResourceFactory.createResource();rdf.add(c1rtc, RDF.type,SBMLRDF.SPECIESREF);
        rdf.add(a2rta, SBMLRDF.HAS_SPECIE, a2);
        rdf.add(rta, SBMLRDF.REACTANT,a2rta);
        rdf.addLiteral(a2rta, SBMLRDF.STOICHIOMETRY,1.0);
    }

    @Test
    public void testMetaboLinks(){

        PropertyFiller fill = new PropertyFiller();
        fill.addMetaboLinks(rdf,false);

        //property
        Property derivesInto = ResourceFactory.createProperty(SBMLRDF.SIOURI, "SIO_000246");
        assertEquals(11,rdf.listStatements(null, derivesInto, (RDFNode) null).toList().size());

        //test R1 comp1
        assertTrue(rdf.contains(a1,derivesInto, b1));
        assertTrue(rdf.contains(a1,derivesInto, c1));

        //test R1 comp2
        assertTrue(rdf.contains(a2,derivesInto, b2));
        assertTrue(rdf.contains(a2,derivesInto, c2));

        //test R2
        assertTrue(rdf.contains(c1,derivesInto, d));
        assertTrue(rdf.contains(d,derivesInto, c1));

        //test R3
        assertTrue(rdf.contains(c2,derivesInto, e));

        //test R transport C
        assertTrue(rdf.contains(c2,derivesInto, c1));
        assertTrue(rdf.contains(c1,derivesInto, c2));

        //test R transport A
        assertTrue(rdf.contains(a1,derivesInto, a2));
        assertTrue(rdf.contains(a2,derivesInto, a1));
    }

    @Test
    public void testHarmonizeCompartment(){
        PropertyFiller fill = new PropertyFiller();
        fill.harmonizeCompartments(rdf,false);

        //property
        Property isComparableTo = ResourceFactory.createProperty(SBMLRDF.SIOURI, "SIO_000272");

        assertEquals(6,rdf.listStatements(null, isComparableTo, (RDFNode) null).toList().size());

        assertTrue(rdf.contains(a1,isComparableTo, a2));
        assertTrue(rdf.contains(a2,isComparableTo, a1));
        assertTrue(rdf.contains(b1,isComparableTo, b2));
        assertTrue(rdf.contains(b2,isComparableTo, b1));
        assertTrue(rdf.contains(c1,isComparableTo, c2));
        assertTrue(rdf.contains(c2,isComparableTo, c1));

    }

    @Test
    public void testImportSideCompounds(){
        ArrayList<String> sideCompoundsIds = new ArrayList<>();
        sideCompoundsIds.add("b1");
        sideCompoundsIds.add("b2");
        PropertyFiller fill = new PropertyFiller();
        fill.importSideCompounds(rdf,sideCompoundsIds);

        List<Resource> speciesRefs = rdf.listSubjectsWithProperty(RDF.type,ResourceFactory.createProperty(SBMLRDF.SBOURI, "SBO_0000603")).toList();
        assertEquals(2,speciesRefs.size());

        List<String> labels = speciesRefs.stream().map(r -> rdf.getProperty(r,SBMLRDF.HAS_SPECIE).getObject().asResource().getLocalName()).collect(Collectors.toList());
        assertTrue(labels.contains("b1"));
        assertTrue(labels.contains("b2"));
    }
}
