import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sbml.jsbml.*;
import vocabulary.SBMLRDF;

import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConvertorTest {

    public SBMLDocument doc;
    Model model;
    Compartment cmp1, cmp2, cmp3;
    Species a1, a2, b1, b2, c1, c2, d , e;
    Reaction r1, r1_2, r2, r3, rta, rtc;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() throws XMLStreamException {
        doc = new SBMLDocument(3, 1);
        initModel();
    }

    private void initModel() {

        model = doc.createModel();

        model.setId("modelId");
        model.setMetaId("modelId");
        model.setName("modelName");

        cmp1 = model.createCompartment("cmp1");
        cmp1.setName("compartment1");
        cmp1.setMetaId(cmp1.getId());
        cmp2 = model.createCompartment("cmp2");
        cmp2.setName("compartment2");
        cmp2.setMetaId(cmp2.getId());
        cmp3 = model.createCompartment("cmp3");
        cmp3.setMetaId(cmp3.getId());

        CompartmentType compartmentType = new CompartmentType("cType");
        model.addCompartmentType(compartmentType);
        cmp1.setCompartmentType(compartmentType);

        cmp1.setOutside(cmp2);
        cmp1.setOutside(cmp1);

        cmp1.setSize(2.0);
        cmp1.setSpatialDimensions(4.0);

        a1 = model.createSpecies("a1", "A", cmp1);a1.setMetaId(a1.getId());
        a2 = model.createSpecies("a2", "A", cmp2);a2.setMetaId(a2.getId());
        b1 = model.createSpecies("b1", "B", cmp1);b1.setMetaId(b1.getId());
        b2 = model.createSpecies("b2", "B", cmp2);b2.setMetaId(b2.getId());
        c1 = model.createSpecies("c1", "C", cmp1);c1.setMetaId(c1.getId());
        c2 = model.createSpecies("c2", "C", cmp2);c2.setMetaId(c2.getId());
        e = model.createSpecies("e", "E", cmp2);e.setMetaId(e.getId());
        d = model.createSpecies("d", "D", cmp1);d.setMetaId(d.getId());

        a1.setConstant(true);
        c2.setConstant(false);
        e.setInitialAmount(2.0);
        d.setInitialAmount(3.0);

        Annotation annotation = new Annotation();
        CVTerm cvterm = new CVTerm();
        cvterm.addResource("https://identifiers.org/SBO_0000299");
        cvterm.setQualifierType(org.sbml.jsbml.CVTerm.Type.BIOLOGICAL_QUALIFIER);
        cvterm.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS);
        annotation.addCVTerm(cvterm);

        a1.setAnnotation(annotation);
        a1.setSBOTerm("SBO:0000299");
        b1.setSBOTerm("SBO:0000299");
        c1.setSBOTerm("SBO:0000299");
        d.setSBOTerm("SBO:0000299");
        e.setSBOTerm("SBO:0000299");


        SpeciesReference a1r1= new SpeciesReference(a1);a1r1.setStoichiometry(2.0);
        SpeciesReference a2r1_2= new SpeciesReference(a2);a2r1_2.setStoichiometry(2.0);
        SpeciesReference b1r1= new SpeciesReference(b1);
        SpeciesReference b2r1_2= new SpeciesReference(b2);b2r1_2.setStoichiometry(1.0);
        SpeciesReference c1r1= new SpeciesReference(c1);
        SpeciesReference c1r2= new SpeciesReference(c1);c1r2.setStoichiometry(4.0);
        SpeciesReference c2r1_2= new SpeciesReference(c2);
        SpeciesReference c2r3= new SpeciesReference(c2);
        SpeciesReference dr2= new SpeciesReference(d);dr2.setStoichiometry(4.0);
        SpeciesReference er3= new SpeciesReference(e);
        dr2.setConstant(true);
        er3.setConstant(false);

        r1 = model.createReaction("r1");
        r1.setName("name1");
        r1.setMetaId(r1.getId());
        r1.setReversible(false);
        r1.setSBOTerm("SBO:0000176");
        r1.setFast(true);
        r1.addReactant(a1r1);
        r1.addProduct(b1r1);
        r1.addProduct(c1r1);

        r1_2 = model.createReaction("r1_2");
        r1_2.setName("name1");
        r1_2.setMetaId(r1_2.getId());
        r1_2.setReversible(false);
        r1_2.setSBOTerm("SBO:0000176");
        r1_2.setFast(true);
        r1_2.addReactant(a2r1_2);
        r1_2.addProduct(b2r1_2);
        r1_2.addProduct(c2r1_2);

        r2 = model.createReaction("r2");
        r2.setMetaId(r2.getId());
        r2.addReactant(c1r2);
        r2.addProduct(dr2);
        r2.setReversible(true);
        r2.setSBOTerm(0000167);

        r3 = model.createReaction("r3");
        r3.setMetaId(r3.getId());
        r3.addReactant(c2r3);
        r3.addProduct(er3);

        rta = model.createReaction("rta");
        rta.setMetaId(rta.getId());
        rta.setName("transport-a");
        rta.setReversible(true);
        rta.setSBOTerm("SBO:0000167");
        rta.addReactant(new SpeciesReference(a1r1));
        rta.addProduct(new SpeciesReference(a2r1_2));

        rtc = model.createReaction("rtc");
        rtc.setMetaId(rtc.getId());
        rtc.setName("transport-c");
        rtc.setReversible(true);
        rtc.setSBOTerm(0000167);
        rtc.addProduct(new SpeciesReference(c1r1));
        rtc.addReactant(new SpeciesReference(c2r1_2));



        // This metabolite must not be taken into account
       /* SpeciesReference m4Ref = new SpeciesReference(m4);
        m4Ref.setStoichiometry(0.0);
        r1.addReactant(m1Ref);
        r1.addReactant(m4Ref);
        r1.addProduct(m2Ref);
        r1.addProduct(m1RefBis);*/


    }

    @Test
    public void testRun(){
        String baseUri = "org.mytest";
        Convertor conv = new Convertor(model,baseUri);
        conv.run();
        org.apache.jena.rdf.model.Model rdf = conv.getRdfModel();

        assertFalse(rdf.containsResource(ResourceFactory.createResource((baseUri+"#foo"))));
        //build metab node
        Resource a1node = ResourceFactory.createResource((baseUri + "#" + a1.getId()));
        Resource a2node = ResourceFactory.createResource((baseUri+"#"+a2.getId()));
        Resource b1node = ResourceFactory.createResource((baseUri+"#"+b1.getId()));
        Resource b2node = ResourceFactory.createResource((baseUri+"#"+b2.getId()));
        Resource c1node = ResourceFactory.createResource((baseUri+"#"+c1.getId()));
        Resource c2node = ResourceFactory.createResource((baseUri+"#"+c2.getId()));
        Resource dnode = ResourceFactory.createResource((baseUri+"#"+d.getId()));
        Resource enode = ResourceFactory.createResource((baseUri+"#"+e.getId()));
        //build compartment node
        Resource cmp1node = ResourceFactory.createResource((baseUri+"#"+cmp1.getId()));
        Resource cmp2node = ResourceFactory.createResource((baseUri+"#"+cmp2.getId()));
        Resource cmp3node = ResourceFactory.createResource((baseUri+"#"+cmp3.getId()));
        //build reaction node
        Resource r1node = ResourceFactory.createResource((baseUri+"#"+r1.getId()));
        Resource r1_2node = ResourceFactory.createResource((baseUri+"#"+r1_2.getId()));
        Resource r2node = ResourceFactory.createResource((baseUri+"#"+r2.getId()));
        Resource r3node = ResourceFactory.createResource((baseUri+"#"+r3.getId()));
        Resource rtcnode = ResourceFactory.createResource((baseUri+"#"+rtc.getId()));
        Resource rtanode = ResourceFactory.createResource((baseUri+"#"+rta.getId()));

        //CHECK EXISTENCE OF METAB + COMP + location
        assertTrue(rdf.contains(cmp1node, RDF.type,SBMLRDF.COMPARTMENT));
        assertTrue(rdf.contains(cmp2node, RDF.type,SBMLRDF.COMPARTMENT));
        assertTrue(rdf.contains(cmp3node, RDF.type,SBMLRDF.COMPARTMENT));
        assertTrue(rdf.contains(a1node, RDF.type,SBMLRDF.SPECIE));
        assertTrue(rdf.contains(a2node, RDF.type,SBMLRDF.SPECIE));
        assertTrue(rdf.contains(b1node, RDF.type,SBMLRDF.SPECIE));
        assertTrue(rdf.contains(b2node, RDF.type,SBMLRDF.SPECIE));
        assertTrue(rdf.contains(c1node, RDF.type,SBMLRDF.SPECIE));
        assertTrue(rdf.contains(c2node, RDF.type,SBMLRDF.SPECIE));
        assertTrue(rdf.contains(dnode, RDF.type,SBMLRDF.SPECIE));
        assertTrue(rdf.contains(enode, RDF.type,SBMLRDF.SPECIE));
        assertTrue(rdf.contains(r1node, RDF.type,SBMLRDF.REACTION));
        assertTrue(rdf.contains(r1_2node, RDF.type,SBMLRDF.REACTION));
        assertTrue(rdf.contains(r2node, RDF.type,SBMLRDF.REACTION));
        assertTrue(rdf.contains(r3node, RDF.type,SBMLRDF.REACTION));
        assertTrue(rdf.contains(rtanode, RDF.type,SBMLRDF.REACTION));
        assertTrue(rdf.contains(rtcnode, RDF.type,SBMLRDF.REACTION));

        assertTrue(rdf.contains(a1node,SBMLRDF.NAME,"A"));
        assertTrue(rdf.contains(a2node,SBMLRDF.NAME,"A"));
        assertTrue(rdf.contains(b1node,SBMLRDF.NAME,"B"));
        assertTrue(rdf.contains(b2node,SBMLRDF.NAME,"B"));
        assertTrue(rdf.contains(c1node,SBMLRDF.NAME,"C"));
        assertTrue(rdf.contains(c2node,SBMLRDF.NAME,"C"));
        assertTrue(rdf.contains(dnode,SBMLRDF.NAME,"D"));
        assertTrue(rdf.contains(enode,SBMLRDF.NAME,"E"));

        assertTrue(rdf.contains(a1node,RDFS.label,"a1"));
        assertTrue(rdf.contains(a2node,RDFS.label,"a2"));
        assertTrue(rdf.contains(b1node,RDFS.label,"b1"));
        assertTrue(rdf.contains(b2node,RDFS.label,"b2"));
        assertTrue(rdf.contains(c1node,RDFS.label,"c1"));
        assertTrue(rdf.contains(c2node,RDFS.label,"c2"));
        assertTrue(rdf.contains(dnode,RDFS.label,"d"));
        assertTrue(rdf.contains(enode,RDFS.label,"e"));

        assertTrue(rdf.contains(a1node,SBMLRDF.HAS_COMPARTMENT,cmp1node));
        assertTrue(rdf.contains(a2node,SBMLRDF.HAS_COMPARTMENT,cmp2node));
        assertTrue(rdf.contains(b1node,SBMLRDF.HAS_COMPARTMENT,cmp1node));
        assertTrue(rdf.contains(b2node,SBMLRDF.HAS_COMPARTMENT,cmp2node));
        assertTrue(rdf.contains(c1node,SBMLRDF.HAS_COMPARTMENT,cmp1node));
        assertTrue(rdf.contains(c2node,SBMLRDF.HAS_COMPARTMENT,cmp2node));
        assertTrue(rdf.contains(dnode,SBMLRDF.HAS_COMPARTMENT,cmp1node));
        assertTrue(rdf.contains(enode,SBMLRDF.HAS_COMPARTMENT,cmp2node));
        assertFalse(rdf.contains(a1node,SBMLRDF.HAS_COMPARTMENT,cmp2node));
        assertFalse(rdf.contains(a2node,SBMLRDF.HAS_COMPARTMENT,cmp1node));
        assertFalse(rdf.contains(b1node,SBMLRDF.HAS_COMPARTMENT,cmp2node));
        assertFalse(rdf.contains(b2node,SBMLRDF.HAS_COMPARTMENT,cmp1node));
        assertFalse(rdf.contains(c1node,SBMLRDF.HAS_COMPARTMENT,cmp2node));
        assertFalse(rdf.contains(c2node,SBMLRDF.HAS_COMPARTMENT,cmp1node));
        assertFalse(rdf.contains(dnode,SBMLRDF.HAS_COMPARTMENT,cmp2node));
        assertFalse(rdf.contains(enode,SBMLRDF.HAS_COMPARTMENT,cmp1node));

        assertTrue(rdf.containsResource(cmp3node));

        //test R1 comp1
        List<RDFNode> stmnts = rdf.listObjectsOfProperty(r1node,SBMLRDF.REACTANT).toList();
        assertTrue(stmnts.size()==1);
        assertTrue(rdf.contains(stmnts.get(0).asResource(),SBMLRDF.HAS_SPECIE,a1node));
        for(Statement s : rdf.listStatements(null,SBMLRDF.NAME, (String) null).toList()){
            System.out.println(s.getSubject().asResource().getLocalName()+" : "+s.getObject());
        }

        stmnts = rdf.listObjectsOfProperty(r1node,SBMLRDF.PRODUCT).toList();
        assertTrue(stmnts.size()==2);
        List<RDFNode> objects = stmnts.stream().map(n -> rdf.listObjectsOfProperty(n.asResource(), SBMLRDF.HAS_SPECIE).toList()).flatMap(List::stream).collect(Collectors.toList());
        assertTrue(objects.contains(b1node));
        assertTrue(objects.contains(c1node));

        assertFalse(rdf.containsLiteral(r1node,SBMLRDF.REVERSIBLE,true));
        assertTrue(rdf.containsLiteral(r1node,SBMLRDF.REVERSIBLE,false));

        //test R1 comp2
        stmnts = rdf.listObjectsOfProperty(r1_2node,SBMLRDF.REACTANT).toList();
        assertTrue(stmnts.size()==1);
        assertTrue(rdf.contains(stmnts.get(0).asResource(),SBMLRDF.HAS_SPECIE,a2node));

        stmnts = rdf.listObjectsOfProperty(r1_2node,SBMLRDF.PRODUCT).toList();
        assertTrue(stmnts.size()==2);
        objects = stmnts.stream().map(n -> rdf.listObjectsOfProperty(n.asResource(), SBMLRDF.HAS_SPECIE).toList()).flatMap(List::stream).collect(Collectors.toList());
        assertTrue(objects.contains(b2node));
        assertTrue(objects.contains(c2node));

        assertFalse(rdf.containsLiteral(r1_2node,SBMLRDF.REVERSIBLE,true));
        assertTrue(rdf.containsLiteral(r1_2node,SBMLRDF.REVERSIBLE,false));

        //test R2
        stmnts = rdf.listObjectsOfProperty(r2node,SBMLRDF.REACTANT).toList();
        assertTrue(stmnts.size()==1);
        assertTrue(rdf.contains(stmnts.get(0).asResource(),SBMLRDF.HAS_SPECIE,c1node));

        stmnts = rdf.listObjectsOfProperty(r2node,SBMLRDF.PRODUCT).toList();
        assertTrue(stmnts.size()==1);
        assertTrue(rdf.contains(stmnts.get(0).asResource(),SBMLRDF.HAS_SPECIE,dnode));

        assertTrue(rdf.containsLiteral(r2node,SBMLRDF.REVERSIBLE,true));
        assertFalse(rdf.containsLiteral(r2node,SBMLRDF.REVERSIBLE,false));

        //test R3
        stmnts = rdf.listObjectsOfProperty(r3node,SBMLRDF.REACTANT).toList();
        assertTrue(stmnts.size()==1);
        assertTrue(rdf.contains(stmnts.get(0).asResource(),SBMLRDF.HAS_SPECIE,c2node));

        stmnts = rdf.listObjectsOfProperty(r3node,SBMLRDF.PRODUCT).toList();
        assertTrue(stmnts.size()==1);
        assertTrue(rdf.contains(stmnts.get(0).asResource(),SBMLRDF.HAS_SPECIE,enode));

        assertTrue(rdf.containsLiteral(r3node,SBMLRDF.REVERSIBLE,true));
        assertFalse(rdf.containsLiteral(r3node,SBMLRDF.REVERSIBLE,false));

        //test R transport C
        stmnts = rdf.listObjectsOfProperty(rtcnode,SBMLRDF.REACTANT).toList();
        assertTrue(stmnts.size()==1);
        assertTrue(rdf.contains(stmnts.get(0).asResource(),SBMLRDF.HAS_SPECIE,c2node));

        stmnts = rdf.listObjectsOfProperty(rtcnode,SBMLRDF.PRODUCT).toList();
        assertTrue(stmnts.size()==1);
        assertTrue(rdf.contains(stmnts.get(0).asResource(),SBMLRDF.HAS_SPECIE,c1node));

        assertTrue(rdf.containsLiteral(rtcnode,SBMLRDF.REVERSIBLE,true));
        assertFalse(rdf.containsLiteral(rtcnode,SBMLRDF.REVERSIBLE,false));

        //test R transport A
        stmnts = rdf.listObjectsOfProperty(rtanode,SBMLRDF.REACTANT).toList();
        assertTrue(stmnts.size()==1);
        assertTrue(rdf.contains(stmnts.get(0).asResource(),SBMLRDF.HAS_SPECIE,a1node));

        stmnts = rdf.listObjectsOfProperty(rtanode,SBMLRDF.PRODUCT).toList();
        assertTrue(stmnts.size()==1);
        assertTrue(rdf.contains(stmnts.get(0).asResource(),SBMLRDF.HAS_SPECIE,a2node));

        assertTrue(rdf.containsLiteral(rtanode,SBMLRDF.REVERSIBLE,true));
        assertFalse(rdf.containsLiteral(rtanode,SBMLRDF.REVERSIBLE,false));
    }
}
