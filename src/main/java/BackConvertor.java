import org.apache.jena.rdf.model.*;
import org.sbml.jsbml.*;
import org.sbml.jsbml.Model;
import vocabulary.SBMLRDF;

/**
 * The Class Convertor extract the relationships between biological entities as described in a SBML modeling file, and
 * add them to a RDF model, using biomodels schema.
 * @author cfrainay
 */
public class BackConvertor {


    private Resource sbmlRessource;

    // the sbml model
    private Model sbmlModel;
    // the rdf model
    private org.apache.jena.rdf.model.Model rdfModel;

    /**
     * The Convertor requires a JSBML sbml model and a JENA rdf model (which can be empty).
     * The Convertor also requires a URI (Uniform Resource Identifiers) that uniquely identify the model
     * Examples:
     * https://metexplore.toulouse.inra.fr/metexplore2/?idBioSource=1363
     * https://www.ebi.ac.uk/biomodels/MODEL1311110001
     *
     * @param sbmlModel a sbml model
     * @param rdfModel a rdf model
     */
    public BackConvertor(Model sbmlModel, org.apache.jena.rdf.model.Model rdfModel, String modelURI ) {
        this.sbmlModel = sbmlModel;
        this.rdfModel = rdfModel;
        this.sbmlRessource = rdfModel.getResource(modelURI); //check if absent
    }

    public BackConvertor(org.apache.jena.rdf.model.Model rdfModel, String modelURI ) {
        this.sbmlModel = new Model();
        this.rdfModel = rdfModel;

        this.sbmlRessource = rdfModel.getResource(modelURI); //check if absent

    }

    /**
     * @return the RDF model with the extracted SBML information
     */
    public Model getSbmlModel(){
        return this.sbmlModel;
    }

    /**
     * sequentially convert the compartments, the species, then the reactions and finally the genes.
     */
    public void run(){
        for (ResIterator it = rdfModel.listSubjectsWithProperty(SBMLRDF.HAS_REACTION); it.hasNext(); ) {
            System.out.println(it.next().asResource());
        }
        this.convertCompartments();
        this.convertSpecies();
        this.convertReactions();
        this.convertGenes();
    }

    private void convertCompartments(){
        for (NodeIterator it = rdfModel.listObjectsOfProperty(sbmlRessource, SBMLRDF.HAS_COMPARTMENT); it.hasNext(); ) {
            Resource n = it.next().asResource();
            String id = n.getLocalName();
            String name = rdfModel.listObjectsOfProperty(n,SBMLRDF.NAME).next().asLiteral().getString();
            Compartment c = new Compartment(id);
            c.setName(name);
            convertAnnotation(n, c);
            this.sbmlModel.addCompartment(c);
        }
    }

    private void convertSpecies(){
        for (NodeIterator it = rdfModel.listObjectsOfProperty(sbmlRessource, SBMLRDF.HAS_SPECIE); it.hasNext(); ) {
            Resource n = it.next().asResource();
            String id = n.getLocalName();
            String name = rdfModel.listObjectsOfProperty(n,SBMLRDF.NAME).next().asLiteral().getString();
            Species s = new Species(id);
            s.setName(name);

            //retrieve compartment
            String compId = rdfModel.listObjectsOfProperty(n,SBMLRDF.HAS_COMPARTMENT).next().asResource().getLocalName();
            Compartment c =this.sbmlModel.getCompartment(compId);
            s.setCompartment(c);
            convertAnnotation(n, s);
            this.sbmlModel.addSpecies(s);
        }
    }

    private void convertReactions(){
        for (NodeIterator it = rdfModel.listObjectsOfProperty(sbmlRessource, SBMLRDF.HAS_REACTION); it.hasNext(); ) {
            Resource n = it.next().asResource();
            String id = n.getLocalName();
            String name = rdfModel.listObjectsOfProperty(n,SBMLRDF.NAME).next().asLiteral().getString();
            Reaction r = new Reaction(id);
            boolean rev = rdfModel.listObjectsOfProperty(n,SBMLRDF.REVERSIBLE).next().asLiteral().getBoolean();
            r.setName(name);
            r.setReversible(rev);

            for (NodeIterator itreactant = rdfModel.listObjectsOfProperty(n,SBMLRDF.REACTANT); itreactant.hasNext();){
                Resource s = itreactant.next().asResource();
                double stochio = s.listProperties(SBMLRDF.STOICHIOMETRY).next().getObject().asLiteral().getDouble();
                String spID = s.listProperties(SBMLRDF.HAS_SPECIE).next().getObject().asResource().getLocalName();
                Species sp = this.sbmlModel.getSpecies(spID);

                SpeciesReference sref = new SpeciesReference(sp);
                sref.setStoichiometry(stochio);
                r.addReactant(sref);
            }

            for (NodeIterator itreactant = rdfModel.listObjectsOfProperty(n,SBMLRDF.PRODUCT); itreactant.hasNext();){
                Resource p = itreactant.next().asResource();
                double stochio = p.listProperties(SBMLRDF.STOICHIOMETRY).next().getObject().asLiteral().getDouble();
                String spID = p.listProperties(SBMLRDF.HAS_SPECIE).next().getObject().asResource().getLocalName();
                Species sp = this.sbmlModel.getSpecies(spID);

                SpeciesReference sref = new SpeciesReference(sp);
                sref.setStoichiometry(stochio);
                r.addProduct(sref);
            }

            convertAnnotation(n, r);
            this.sbmlModel.addReaction(r);
        }
    }

    private void convertAnnotation(Resource r, SBase s){
        //TODO: handle sbo terms
        Annotation annot = new Annotation();
        for (StmtIterator itreactant = rdfModel.listStatements(r, null, (RDFNode) null); itreactant.hasNext();){
            Statement triple = itreactant.next();
            Property p = triple.getPredicate();
            String ns = p.getNameSpace();
            if(!ns.equals(SBMLRDF.NS) && !ns.equals(SBMLRDF.RDFNS) && !ns.equals(SBMLRDF.RDFSNS)) {
                RDFNode o = triple.getObject();
                String ln = p.getLocalName();
                CVTerm cv = new CVTerm();
                if (ns.equals(SBMLRDF.BQBIOLNS)) {
                    cv.setQualifierType(CVTerm.Type.BIOLOGICAL_QUALIFIER);
                    switch (ln) {
                        case "encodes": cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_ENCODES);break;
                        case "hasPart": cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_HAS_PART);break;
                        case "hasProperty": cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_HAS_PROPERTY);break;
                        case "hasTaxon": cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_HAS_TAXON);break;
                        case "hasVersion": cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_HAS_VERSION);break;
                        case "is": cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS);break;
                        case "isDescribedBy": cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS_DESCRIBED_BY);break;
                        case "isEncodedBy": cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS_ENCODED_BY);break;
                        case "isHomologTo": cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS_HOMOLOG_TO);break;
                        case "isPartOf": cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS_PART_OF);break;
                        case "isPropertyOf": cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS_PROPERTY_OF);break;
                        case "isRelatedTo": cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS_RELATED_TO);break;
                        case "isVersionOf": cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_IS_VERSION_OF);break;
                        case "occursIn": cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_OCCURS_IN);break;
                        default: cv.setBiologicalQualifierType(CVTerm.Qualifier.BQB_UNKNOWN);break;
                    }
                } else if (ns.equals(SBMLRDF.BQLMODELNS)) {
                    cv.setQualifierType(CVTerm.Type.MODEL_QUALIFIER);
                    switch (ln) {
                        case "hasInstance": cv.setModelQualifierType(CVTerm.Qualifier.BQM_HAS_INSTANCE);break;
                        case "is": cv.setModelQualifierType(CVTerm.Qualifier.BQM_IS);break;
                        case "isDerivedFrom": cv.setModelQualifierType(CVTerm.Qualifier.BQM_IS_DERIVED_FROM);break;
                        case "isDescribedBy": cv.setModelQualifierType(CVTerm.Qualifier.BQM_IS_DESCRIBED_BY);break;
                        case "isInstanceOf": cv.setModelQualifierType(CVTerm.Qualifier.BQM_IS_INSTANCE_OF);break;
                        default: cv.setModelQualifierType(CVTerm.Qualifier.BQM_UNKNOWN);break;
                    }
                } else {
                    cv.setQualifierType(CVTerm.Type.UNKNOWN_QUALIFIER);
                    cv.setUnknownQualifierName(ln);
                }
                cv.addResource(o.toString());
                annot.addCVTerm(cv);
            }
        }
        s.setAnnotation(annot);
    }

    private void convertGenes(){
        for (NodeIterator it = rdfModel.listObjectsOfProperty(sbmlRessource, SBMLRDF.HAS_REACTION); it.hasNext(); ) {
            Resource n = it.next().asResource();
            String id = n.getLocalName();
            String name = rdfModel.listObjectsOfProperty(n,SBMLRDF.NAME).next().asLiteral().getString();
        }
    }

}
