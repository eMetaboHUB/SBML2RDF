import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.fbc.*;
import vocabulary.SBMLRDF;

import java.util.HashSet;
import java.util.Set;

/**
 * The Class Convertor extract the relationships between biological entities as described in a SBML modeling file, and
 * add them to a RDF model, using biomodels schema.
 * @author cfrainay
 */
public class Convertor {

    //Namespaces
    private String modelNamespace;
    private final String modelPrefix = "model";
    private final String biomodelPrefix = "bqbiol";

    // the sbml model
    private org.sbml.jsbml.Model sbmlModel;
    // the rdf model
    private org.apache.jena.rdf.model.Model rdfModel;
    //the resource representing the sbml model;
    Resource sbmlResource;

    /**
     * The Convertor requires a JSBML sbml model and a JENA rdf model (which can be empty).
     * The Convertor also requires a URI (Uniform Resource Identifiers) that uniquely identify the model
     * Examples:
     * https://metexplore.toulouse.inra.fr/metexplore2/?idBioSource=1363
     * https://www.ebi.ac.uk/biomodels/MODEL1311110001
     *
     * @param sbmlModel a sbml model
     * @param rdfModel a rdf model
     * @param modelURI the URI of the model
     */
    public Convertor(org.sbml.jsbml.Model sbmlModel, org.apache.jena.rdf.model.Model rdfModel, String modelURI ) {
        this.sbmlModel = sbmlModel;
        this.modelNamespace = modelURI+"#";

        this.rdfModel =  rdfModel;
        this.rdfModel.setNsPrefix(modelPrefix,modelNamespace);
        this.rdfModel.setNsPrefix(biomodelPrefix,SBMLRDF.BQURI);

        this.sbmlResource=createModelResource(sbmlModel);
    }

    /**
     * The Convertor requires a JSBML sbml model and a JENA rdf model (which can be empty).
     * The Convertor also requires a URI (Uniform Resource Identifiers) that uniquely identify the model
     * Examples:
     * https://metexplore.toulouse.inra.fr/metexplore2/?idBioSource=1363
     * https://www.ebi.ac.uk/biomodels/MODEL1311110001
     *
     * @param sbmlModel a sbml model
     * @param modelURI the URI of the model
     */
     public Convertor(org.sbml.jsbml.Model sbmlModel, String modelURI) {
        this.sbmlModel = sbmlModel;
         modelNamespace = modelURI+"#";

        this.rdfModel =  ModelFactory.createDefaultModel();
         this.rdfModel.setNsPrefix(SBMLRDF.getPREFIX(),SBMLRDF.getURI());
         this.rdfModel.setNsPrefix(biomodelPrefix,SBMLRDF.BQURI);
         this.rdfModel.setNsPrefix(modelPrefix,modelNamespace);

         this.sbmlResource=createModelResource(sbmlModel);
    }

    // Generate a Resource from a sbml id, using predefined base URI
    private Resource initResource(AbstractSBase sbmlEntry){
        Resource node = rdfModel.createResource(modelNamespace + sbmlEntry.getMetaId());
        node.addProperty(RDFS.label,sbmlEntry.getId());
        return node;
    }

    private Resource createModelResource(Model sbmlModel){
        Resource sbmlResource = initResource(sbmlModel);
        sbmlResource.addProperty(RDF.type,SBMLRDF.SBMLMODEL);
        sbmlResource.addProperty(SBMLRDF.NAME,sbmlModel.getName());
        createAnnotation(sbmlResource,sbmlModel);
        return sbmlResource;
    }

    private Resource createCompartmentResource(Compartment sbmlCompartment){
        Resource compartment = initResource(sbmlCompartment);
        compartment.addProperty(RDF.type, SBMLRDF.COMPARTMENT);
        compartment.addProperty(SBMLRDF.NAME, sbmlCompartment.getName());
        createAnnotation(compartment, sbmlCompartment);
        return compartment;
    }

    private Resource createSpeciesResource(Species sbmlSpecie){
        Resource specie = initResource(sbmlSpecie);
        specie.addProperty(RDF.type, SBMLRDF.SPECIE);
        specie.addProperty(SBMLRDF.NAME,sbmlSpecie.getName());
        specie.addProperty(SBMLRDF.HAS_COMPARTMENT,rdfModel.createResource(modelNamespace + sbmlSpecie.getCompartmentInstance().getMetaId()));
        createAnnotation(specie,sbmlSpecie);
        return specie;
    }

    private Resource createSpeciesReferenceResource(SpeciesReference sbmlSpecieRef){
        Resource specieRef;
        if(sbmlSpecieRef.getMetaId() == null || sbmlSpecieRef.getMetaId().isBlank()){
            //create blank node
            specieRef=rdfModel.createResource();
        }else{
            specieRef = initResource(sbmlSpecieRef);

        }
        specieRef.addProperty(RDF.type, SBMLRDF.SPECIESREF);
        specieRef.addLiteral(SBMLRDF.STOICHIOMETRY, sbmlSpecieRef.getStoichiometry());
        specieRef.addProperty(SBMLRDF.HAS_SPECIE,rdfModel.createResource(modelNamespace + sbmlSpecieRef.getSpeciesInstance().getMetaId()));
        return specieRef;
    }

    private Resource createReactionResource(Reaction sbmlReaction){
        Resource reaction = initResource(sbmlReaction);
        reaction.addProperty(RDF.type, SBMLRDF.REACTION);
        reaction.addProperty(SBMLRDF.NAME,sbmlReaction.getName());
        reaction.addLiteral(SBMLRDF.REVERSIBLE,sbmlReaction.getReversible());

        for(SpeciesReference sbmlReactant : sbmlReaction.getListOfReactants()){
            Resource specieRef = createSpeciesReferenceResource(sbmlReactant);
            reaction.addProperty(SBMLRDF.REACTANT,specieRef);
        }

        for(SpeciesReference sbmlProduct : sbmlReaction.getListOfProducts()){
            Resource specieRef = createSpeciesReferenceResource(sbmlProduct);
            reaction.addProperty(SBMLRDF.PRODUCT,specieRef);
        }

//TODO    parse modifiers
//
//            for(ModifierSpeciesReference sbmlModifier : sbmlReaction.getListOfModifiers()){
//                Resource specieRef = initResource(sbmlModifier.getSpeciesInstance());
//                specieRef.addProperty(SBMLRDF.HAS_SPECIE,rdfModel.getResource(sbmlModifier.getMetaId()));
//
//                reaction.addProperty(SBMLRDF.MODIFIER,specieRef);
//            }

        createAnnotation(reaction, sbmlReaction);

        return reaction;
    }

    //parse the sbml annotations (bqbiol:is)
    private void createAnnotation(Resource resource, SBase sbmlElement){
        Annotation sbmlAnnot = sbmlElement.getAnnotation();
        for(CVTerm term : sbmlAnnot.getListOfCVTerms()){
            if(term.isBiologicalQualifier()) {
                Property p = ResourceFactory.createProperty(SBMLRDF.BQURI, term.getQualifier().getElementNameEquivalent());
                for (String resourceURI : term.getResources()) {
                    resource.addProperty(p, rdfModel.createResource(resourceURI));
                }
            }
        }
    }

    /**
     * Convert the compartment descriptions in RDF and add them in the RDF model
     */
    public void convertCompartments(){

        for( Compartment sbmlCompartment : sbmlModel.getListOfCompartments()){
            Resource compartment = createCompartmentResource(sbmlCompartment);
            sbmlResource.addProperty(SBMLRDF.HAS_COMPARTMENT, compartment);
        }
    }

    /**
     * Convert the species descriptions in RDF and add them in the RDF model
     */
    public void convertSpecies(){

        for( Species sbmlSpecie : sbmlModel.getListOfSpecies()){
            Resource specie = createSpeciesResource(sbmlSpecie);
            sbmlResource.addProperty(SBMLRDF.HAS_SPECIE, specie);
        }
    }

    /**
     * Convert the reactions descriptions in RDF and add them in the RDF model
     */
    public void convertReactions(){

        for( Reaction sbmlReaction : sbmlModel.getListOfReactions()){
            Resource reaction = createReactionResource(sbmlReaction);
            sbmlResource.addProperty(SBMLRDF.HAS_REACTION, reaction);
        }
    }

    /**
     * Convert the genes descriptions (from the fbc package) in RDF and add them in the RDF model.
     */
    public void convertGenes(){

        if(this.sbmlModel.isPackageEnabled("fbc")){
            FBCModelPlugin fbcParser = (FBCModelPlugin) this.sbmlModel.getPlugin("fbc");
            String fbcNS=fbcParser.getURI()+"#";
            rdfModel.setNsPrefix("fbc",fbcNS);
            Property geneProductAssociation = rdfModel.createProperty(fbcNS,"geneProductAssociation");
            Resource geneProduct = rdfModel.createProperty(fbcNS+"geneProduct");


            for (GeneProduct sbmlGene : fbcParser.getListOfGeneProducts()){
                Resource gene = initResource(sbmlGene);
                gene.addProperty(RDF.type, geneProduct);
                gene.addProperty(SBMLRDF.NAME,sbmlGene.getLabel());
                createAnnotation(gene,sbmlGene);
            }

            for(Reaction sbmlReaction : fbcParser.getParent().getListOfReactions()){
                FBCReactionPlugin rxnFbcParser = (FBCReactionPlugin) sbmlReaction.getPlugin("fbc");
                if(rxnFbcParser.isSetGeneProductAssociation()){
                    Association association = rxnFbcParser.getGeneProductAssociation().getAssociation();
                    Set<GeneProductRef> associatedGenes = parseGPA(association);

                    for(GeneProductRef sbmlGeneRef : associatedGenes){
                        Resource gene = rdfModel.createResource(modelNamespace + sbmlGeneRef.getGeneProductInstance().getMetaId());
                        Resource reaction = rdfModel.createResource(modelNamespace + sbmlReaction.getMetaId());
                        reaction.addProperty(geneProductAssociation, gene);
                    }
                }


            }

        }

    }

    //parse the fbc gene product associations
    private Set<GeneProductRef> parseGPA(Association association){
        Set<GeneProductRef> associatedGenes = new HashSet<>();
        if(association.getClass().getCanonicalName().equals(And.class.getCanonicalName())
        || (association.getClass().getCanonicalName().equals(Or.class.getCanonicalName()))){
            LogicalOperator operator = (LogicalOperator) association;
            for(Association nestedAssoc : operator.getListOfAssociations()){
                associatedGenes.addAll(parseGPA(nestedAssoc));
            }
        }else if(association.getClass().getCanonicalName().equals(GeneProductRef.class.getCanonicalName())){
            associatedGenes.add((GeneProductRef) association);
        }else{

        }
        return associatedGenes;
    }

    /**
     * @return the RDF model with the extracted SBML information
     */
    public org.apache.jena.rdf.model.Model getRdfModel(){
        return this.rdfModel;
    }

    /**
     * sequentially convert the compartments, the species, then the reactions and finally the genes.
     */
    public void run(){
        this.convertCompartments();
        this.convertSpecies();
        this.convertReactions();
        this.convertGenes();
    }

}
