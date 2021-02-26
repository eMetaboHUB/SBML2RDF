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
}