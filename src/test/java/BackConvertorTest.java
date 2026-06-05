import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.fbc.*;

import javax.xml.stream.XMLStreamException;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class BackConvertorTest {

    Model model;
    Compartment cmp1, cmp2, cmp3;
    Species a1, a2, b1, b2, c1, c2, d , e;
    Reaction r1, r1_2, r2, r3, rta, rtc;
    GeneProduct g1;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() throws XMLStreamException {
        initModel();
    }

    private void initModel() {

        Model model = new SBMLDocument(3, 2).createModel();

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

        a1 = model.createSpecies("a1_c", "A", cmp1);a1.setMetaId(a1.getId());
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
        cvterm.setQualifierType(CVTerm.Type.BIOLOGICAL_QUALIFIER);
        cvterm.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS);
        annotation.addCVTerm(cvterm);
        CVTerm cvterm2 = new CVTerm();
        cvterm2.addResource("https://identifiers.org/ weird uri");
        cvterm2.setQualifierType(CVTerm.Type.BIOLOGICAL_QUALIFIER);
        cvterm2.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS);
        annotation.addCVTerm(cvterm2);
        CVTerm cvterm3 = new CVTerm();
        cvterm3.addResource("really weird uri");
        cvterm3.setQualifierType(CVTerm.Type.BIOLOGICAL_QUALIFIER);
        cvterm3.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS);
        annotation.addCVTerm(cvterm3);

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
        r1.addReactant(a1r1);
        r1.addProduct(b1r1);
        r1.addProduct(c1r1);

        r1_2 = model.createReaction("r1_2");
        r1_2.setName("name1");
        r1_2.setMetaId(r1_2.getId());
        r1_2.setReversible(false);
        r1_2.setSBOTerm("SBO:0000176");
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


        FBCModelPlugin fbcModel = (FBCModelPlugin)  model.getPlugin("http://www.sbml.org/sbml/level3/version1/fbc/version2");
        g1 = fbcModel.createGeneProduct();
        g1.setId("g1");
        g1.setMetaId("g1");
        g1.setName("g1");
        g1.setLabel("g1");
        fbcModel.addGeneProduct(g1); // WARN (AbstractSBase.java:2189) - Trying to register geneProduct which is already registered under listOfGeneProducts ?

        GeneProductRef geneRef1 = new GeneProductRef("g1r");
        geneRef1.setGeneProduct("g1");

        FBCReactionPlugin rxnPlugin = (FBCReactionPlugin) r1.getPlugin("fbc");
        GeneProductAssociation GPA = rxnPlugin.createGeneProductAssociation();
        GPA.setAssociation(geneRef1);


        Convertor conv = new Convertor(model,"org.mytest");
        conv.run();
        org.apache.jena.rdf.model.Model rdf = conv.getRdfModel();
        
        
        BackConvertor backConv = new BackConvertor(rdf,"org.mytest#modelId");
        backConv.run();
        this.model = backConv.getSbmlModel();

    }

    @Test
    public void testCompartment(){
        assertNotNull(model.getCompartment(cmp1.getId()));
        assertNotNull(model.getCompartment(cmp2.getId()));
        assertNotNull(model.getCompartment(cmp3.getId()));
    }

    @Test
    public void testSpecies(){
        assertNotNull(model.getSpecies(a1.getId()));
        assertNotNull(model.getSpecies(a2.getId()));
        assertNotNull(model.getSpecies(b1.getId()));
        assertNotNull(model.getSpecies(b2.getId()));
        assertNotNull(model.getSpecies(c1.getId()));
        assertNotNull(model.getSpecies(c2.getId()));
        assertNotNull(model.getSpecies(d.getId()));
        assertNotNull(model.getSpecies(e.getId()));
    }

    @Test
    public void testReaction(){
        assertNotNull(model.getReaction(r1.getId()));
        assertNotNull(model.getReaction(r1_2.getId()));
        assertNotNull(model.getReaction(r2.getId()));
        assertNotNull(model.getReaction(r3.getId()));
        assertNotNull(model.getReaction(rta.getId()));
        assertNotNull(model.getReaction(rtc.getId()));
    }

    @Test
    public void testSpeciesNames(){
        assertEquals("A", model.getSpecies(a1.getId()).getName());
        assertEquals("A", model.getSpecies(a2.getId()).getName());
        assertEquals("B", model.getSpecies(b1.getId()).getName());
        assertEquals("B", model.getSpecies(b2.getId()).getName());
        assertEquals("C", model.getSpecies(c1.getId()).getName());
        assertEquals("C", model.getSpecies(c2.getId()).getName());
        assertEquals("D", model.getSpecies(d.getId()).getName());
        assertEquals("E", model.getSpecies(e.getId()).getName());
    }

    @Test
    public void testReactionNames(){
        assertEquals("name1", model.getReaction(r1.getId()).getName());
        assertEquals("name1", model.getReaction(r1_2.getId()).getName());
        assertEquals("", model.getReaction(r2.getId()).getName());
        assertEquals("", model.getReaction(r3.getId()).getName());
        assertEquals("transport-a", model.getReaction(rta.getId()).getName());
        assertEquals("transport-c", model.getReaction(rtc.getId()).getName());
    }

    @Test
    public void testCompartmentNames(){
        assertEquals("compartment1",model.getCompartment(cmp1.getId()).getName());
        assertEquals("compartment2", model.getCompartment(cmp2.getId()).getName());
        assertEquals("",model.getCompartment(cmp3.getId()).getName());
    }

    @Test
    public void testCompartmentAssignment(){
        assertEquals(model.getSpecies(a1.getId()).getCompartment(),cmp1.getId());
        assertEquals(model.getSpecies(a2.getId()).getCompartment(),cmp2.getId());
        assertEquals(model.getSpecies(b1.getId()).getCompartment(),cmp1.getId());
        assertEquals(model.getSpecies(b2.getId()).getCompartment(),cmp2.getId());
        assertEquals(model.getSpecies(c1.getId()).getCompartment(),cmp1.getId());
        assertEquals(model.getSpecies(c2.getId()).getCompartment(),cmp2.getId());
        assertEquals(model.getSpecies(d.getId()).getCompartment(),cmp1.getId());
        assertEquals(model.getSpecies(e.getId()).getCompartment(),cmp2.getId());
    }

    @Test
    public void testReactionProducts(){
        assertEquals(2, model.getReaction(r1.getId()).getListOfProducts().size());
        Iterator<SpeciesReference> it = model.getReaction(r1.getId()).getListOfProducts().iterator();
        SpeciesReference ref =it.next();
        assertTrue(ref.getSpecies().equals(b1.getId()) || ref.getSpecies().equals(c1.getId()));
        assertEquals(1.0, ref.getStoichiometry(), Double.MIN_VALUE);
        assertEquals(1.0, it.next().getStoichiometry(), Double.MIN_VALUE);

        assertEquals(2, model.getReaction(r1_2.getId()).getListOfProducts().size());
        it = model.getReaction(r1_2.getId()).getListOfProducts().iterator();
        ref =it.next();
        assertTrue(ref.getSpecies().equals(b2.getId()) || ref.getSpecies().equals(c2.getId()));
        assertEquals(1.0, ref.getStoichiometry(), Double.MIN_VALUE);
        assertEquals(1.0, it.next().getStoichiometry(), Double.MIN_VALUE);

        assertEquals(1, model.getReaction(r2.getId()).getListOfProducts().size());
        assertEquals(d.getId(), model.getReaction(r2.getId()).getListOfProducts().iterator().next().getSpecies());
        assertEquals(4.0, model.getReaction(r2.getId()).getListOfProducts().iterator().next().getStoichiometry(), Double.MIN_VALUE);

        assertEquals(1, model.getReaction(r3.getId()).getListOfProducts().size());
        assertEquals(e.getId(), model.getReaction(r3.getId()).getListOfProducts().iterator().next().getSpecies());
        assertEquals(1.0, model.getReaction(r3.getId()).getListOfProducts().iterator().next().getStoichiometry(), Double.MIN_VALUE);

        assertEquals(1, model.getReaction(rtc.getId()).getListOfProducts().size());
        assertEquals(c1.getId(), model.getReaction(rtc.getId()).getListOfProducts().iterator().next().getSpecies());
        assertEquals(1.0, model.getReaction(rtc.getId()).getListOfProducts().iterator().next().getStoichiometry(), Double.MIN_VALUE);

        assertEquals(1, model.getReaction(rta.getId()).getListOfProducts().size());
        assertEquals(a2.getId(), model.getReaction(rta.getId()).getListOfProducts().iterator().next().getSpecies());
        assertEquals(2.0, model.getReaction(rta.getId()).getListOfProducts().iterator().next().getStoichiometry(), Double.MIN_VALUE);
    }

    @Test
    public void testReactionReactants(){
        assertEquals(1, model.getReaction(r1.getId()).getListOfReactants().size());
        assertEquals(a1.getId(), model.getReaction(r1.getId()).getListOfReactants().iterator().next().getSpecies());
        assertEquals(2.0, model.getReaction(r1.getId()).getListOfReactants().iterator().next().getStoichiometry(), Double.MIN_VALUE);

        assertEquals(1, model.getReaction(r1_2.getId()).getListOfReactants().size());
        assertEquals(a2.getId(), model.getReaction(r1_2.getId()).getListOfReactants().iterator().next().getSpecies());
        assertEquals(2.0, model.getReaction(r1_2.getId()).getListOfReactants().iterator().next().getStoichiometry(), Double.MIN_VALUE);

        assertEquals(1, model.getReaction(r2.getId()).getListOfReactants().size());
        assertEquals(c1.getId(), model.getReaction(r2.getId()).getListOfReactants().iterator().next().getSpecies());
        assertEquals(4.0, model.getReaction(r2.getId()).getListOfReactants().iterator().next().getStoichiometry(), Double.MIN_VALUE);

        assertEquals(1, model.getReaction(r3.getId()).getListOfReactants().size());
        assertEquals(c2.getId(), model.getReaction(r3.getId()).getListOfReactants().iterator().next().getSpecies());
        assertEquals(1.0, model.getReaction(r3.getId()).getListOfReactants().iterator().next().getStoichiometry(), Double.MIN_VALUE);

        assertEquals(1, model.getReaction(rtc.getId()).getListOfReactants().size());
        assertEquals(c2.getId(), model.getReaction(rtc.getId()).getListOfReactants().iterator().next().getSpecies());
        assertEquals(1.0, model.getReaction(rtc.getId()).getListOfReactants().iterator().next().getStoichiometry(), Double.MIN_VALUE);

        assertEquals(1, model.getReaction(rta.getId()).getListOfReactants().size());
        assertEquals(a1.getId(), model.getReaction(rta.getId()).getListOfReactants().iterator().next().getSpecies());
        assertEquals(2.0, model.getReaction(rta.getId()).getListOfReactants().iterator().next().getStoichiometry(), Double.MIN_VALUE);

    }

    @Test
    public void testReactionReversibility(){

        assertFalse(model.getReaction(r1.getId()).isReversible());
        assertFalse(model.getReaction(r1_2.getId()).isReversible());
        assertTrue(model.getReaction(r2.getId()).isReversible());
        assertTrue(model.getReaction(r3.getId()).isReversible());
        assertTrue(model.getReaction(rta.getId()).isReversible());
        assertTrue(model.getReaction(rtc.getId()).isReversible());
    }

    @Test
    public void testAnnotation(){
        List<CVTerm> annots = model.getSpecies("a1_c").getAnnotation().getListOfCVTerms().stream().filter(cv -> cv.getBiologicalQualifierType() == CVTerm.Qualifier.BQB_IS).toList();
        assertEquals(2, annots.size());
        annots.forEach(cv -> assertTrue(cv.getResources().contains("https://identifiers.org/SBO_0000299") || cv.getResources().contains("https://identifiers.org/+weird+uri")));
    }


    @Test
    public void testRun(){
        //stmnts = model.listObjectsOfProperty(r1.getId(),SBMLRDF.PRODUCT).toList();
        //assertTrue(stmnts.size()==2);
        //List<RDFNode> objects = stmnts.stream().map(n -> model.listObjectsOfProperty(n.asResource(), SBMLRDF.HAS_SPECIE).toList()).flatMap(List::stream).collect(Collectors.toList());
        //assertTrue(objects.contains(b1.getId()));
        //assertTrue(objects.contains(c1.getId()));
        //
        //Resource geneProduct = model.createProperty(model.getPlugin("fbc").getURI()+"#geneProduct");
        //Property geneProductAssociation = model.createProperty(model.getPlugin("fbc").getURI()+"#geneProductAssociation");
        //assertTrue(model.contains(g1.getId(),RDF.type,geneProduct));
        //assertTrue(model.contains(r1.getId(),geneProductAssociation,g1.getId()));
    }


}
