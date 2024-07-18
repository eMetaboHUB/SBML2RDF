package vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Class to access the vocabulary of the biomodels SBMLRDF schema
 */
public class SBMLRDF {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String BQURI ="http://biomodels.net/biology-qualifiers#";
    public static final String SBOURI ="http://biomodels.net/SBO#";
    public static final String SIOURI = "http://semanticscience.org/resource#";
    public static final String SIOPREFIX = "sio";
    public static final String NS = "http://identifiers.org/biomodels.vocabulary#";
    public static final String PREFIX = "SBMLrdf";

    /**
     * returns the URI for this schema
     *
     * @return the URI for this schema
     */
    public static String getURI() {
        return NS;
    }
    public static String getPREFIX() {
        return PREFIX;
    }

    protected static final Resource resource(String local) {
        return ResourceFactory.createResource(NS + local);
    }

    protected static final Property property(String local) {
        return ResourceFactory.createProperty(NS, local);
    }



    public final static Resource SBMLMODEL = resource("SBMLModel");
    public final static Resource COMPARTMENT = resource("Compartment");
    public final static Resource SPECIE = resource("Species");
    public final static Resource REACTION = resource("Reaction");
    public final static Resource SPECIESREF = resource("SpeciesReference");

    public final static Property NAME = property("name");
    public final static Property NOTES = property("notes");

    public final static Property HAS_COMPARTMENT = property( "compartment" );
    public final static Property HAS_SPECIE = property( "species" );
    public final static Property HAS_REACTION = property( "reaction" );

    public final static Property MODIFIER = property( "modifier" );
    public final static Property PRODUCT = property( "product" );
    public final static Property REACTANT = property( "reactant" );
    public final static Property REVERSIBLE = property( "isReversible" );
    public final static Property STOICHIOMETRY = property( "stoichiometry" );

    //non-sbml types and properties for model enhancement
    public final static Property DERIVES_INTO = ResourceFactory.createProperty(SIOURI, "SIO_000245");
    public final static Property IMMEDIATELY_DERIVES_INTO = ResourceFactory.createProperty(SIOURI, "SIO_000246");
    public final static Property IS_VARIANT_OF = ResourceFactory.createProperty(SIOURI, "SIO_000272");

    public static Resource SIDEREACTANT = ResourceFactory.createResource(SBOURI+"SBO_0000604");
    public static Resource SIDEPRODUCT = ResourceFactory.createResource(SBOURI+"SBO_0000603");
}