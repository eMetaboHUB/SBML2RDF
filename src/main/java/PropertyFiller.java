import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import vocabulary.SBMLRDF;

import java.util.Collection;

/**
 *  This class constructs statements beyond what is typically explicitly reported in SBML files, which still may be proven
 *  relevant in a knowledge graph context
 */
public class PropertyFiller {

    /**
     * Add statements about the relatedness of two SBML entries that describe the same compound in different compartments.
     * The link construction assume that each located version of a given compound share the same name.
     * @param rdfModel the model
     * @param useSameAs if the relatedness should use the owl:sameAs property
     */
    public static void harmonizeCompartments(org.apache.jena.rdf.model.Model rdfModel, boolean useSameAs){
        //define variables
        Var compound1 = Var.alloc("c1");
        Var compound2 = Var.alloc("c2");
        Var name = Var.alloc("n");

        //select property to use in construct
        if (!useSameAs) rdfModel.setNsPrefix(SBMLRDF.SIOPREFIX,SBMLRDF.SIOURI);
        Property link = useSameAs ? OWL.sameAs : SBMLRDF.IS_VARIANT_OF;

        //build query
        ConstructBuilder qb = new ConstructBuilder()
                .addConstruct(compound1,link,compound2) //define statements to build
                .addWhere(compound1, RDF.type, SBMLRDF.SPECIE) //looks for one resource that is a sbml specie
                .addWhere(compound2, RDF.type, SBMLRDF.SPECIE) //looks for a second resource that is a sbml species
                .addWhere(compound1, SBMLRDF.NAME, name) //looks for first specie's name
                .addWhere(compound2, SBMLRDF.NAME, name) //looks for second species' name that match first's one
                .addFilter("?c1 != ?c2"); // filter cases where the first and second species are the same

        //execute query and add results to current model
        QueryExecution qexec = QueryExecutionFactory.create(qb.build(),rdfModel);
        Model res = qexec.execConstruct();
        rdfModel.add(res);
        qexec.close();

    }

    /**
     * Add statements about compound-to-compound metabolic relatedness. Two compounds are related if one is consumed by
     * a reaction that produce the other. This is equivalent to building a compound graph, and conveniently bypass the
     * [specie <- specieRef <- reaction -> specieRef -> specie] property paths.
     * @param rdfModel the model
     * @param useTransitive if a transitive property should be use (prior flagging of side compounds is recommended)
     */
    public static void addMetaboLinks(org.apache.jena.rdf.model.Model rdfModel, Boolean useTransitive){
        //select property to use in construct
        rdfModel.setNsPrefix(SBMLRDF.SIOPREFIX,SBMLRDF.SIOURI);
        Property metabolink = useTransitive ? SBMLRDF.DERIVES_INTO : SBMLRDF.IMMEDIATELY_DERIVES_INTO;

        //define variables
        Var reaction = Var.alloc("r");
        Var reactant = Var.alloc("s");
        Var product = Var.alloc("p");
        Var reactantParticipant = Var.alloc("sp");
        Var productParticipant = Var.alloc("pp");

        //build query
        //  define side compounds filtering (ignoring metabolic relatedness between side compounds and other reactants/products)
        SelectBuilder findSidesProd = new SelectBuilder()
                .addWhere(productParticipant,RDF.type,SBMLRDF.SIDEPRODUCT);
        SelectBuilder findSidesReact = new SelectBuilder()
                .addWhere(reactantParticipant,RDF.type,SBMLRDF.SIDEREACTANT);

        //  define new statements pattern
        ConstructBuilder qb = new ConstructBuilder()
            .addConstruct(reactant,metabolink,product);

        //define
        WhereBuilder wb = new WhereBuilder()
        .addWhere(reaction,SBMLRDF.REACTANT,reactantParticipant) //looks for the reactant specieRef of a reaction
        .addWhere(reactantParticipant,SBMLRDF.HAS_SPECIE,reactant) //looks for its referenced specie
        .addWhere(reaction,SBMLRDF.PRODUCT,productParticipant) //looks for the product specieRef of a reaction
        .addWhere(productParticipant,SBMLRDF.HAS_SPECIE,product) //looks for its referenced specie
        .addFilter(new E_NotExists(new ElementSubQuery(findSidesProd.build()))) //ensure product specieRef isn't a "side"
        .addFilter(new E_NotExists(new ElementSubQuery(findSidesReact.build()))); //ensure reactant specieRef isn't a "side"

        qb.addWhere(wb);

        //execute query and add results to current model
        QueryExecution qexec = QueryExecutionFactory.create(qb.build(),rdfModel);
        Model res = qexec.execConstruct(rdfModel);
        qexec.close();

        //build new query to add reversed statement when a reaction is reversible
        qb = new ConstructBuilder()
                .addConstruct(product,metabolink,reactant);
        wb.addWhere(reaction,SBMLRDF.REVERSIBLE,true);

        qb.addWhere(wb);

        //execute query and add results to current model
        qexec = QueryExecutionFactory.create(qb.build(),rdfModel);
        res = qexec.execConstruct();
        rdfModel.add(res);
        qexec.close();
    }

    /**
     * From a list of side compounds (also known as, or closely related to : ubiquitous/auxiliary/ancillary compounds or currency metabolites), types speciesRefs as sideReactant or sideProduct.
     * @param rdfModel the model
     * @param sideCompoundIds a collection of side compounds identifiers, using the same identifier system as the input sbml model
     */
    public static void importSideCompounds(org.apache.jena.rdf.model.Model rdfModel, Collection<String> sideCompoundIds){

        //define variables
        Var participant = Var.alloc("p");
        Var metabolite = Var.alloc("m");
        Var compoundId = Var.alloc("l");

        //build query
        // create sub-query for checking if a compound's id is in the side compound id list
        ExprList sideCompoundsIdsSet = new ExprList();
        for(String id : sideCompoundIds){
            sideCompoundsIdsSet.add(new NodeValueString(id));
        }
        E_OneOf labelInList = new E_OneOf(new ExprVar(compoundId),sideCompoundsIdsSet);

        // create construct clause adding side product/reactant type to specieref
        ConstructBuilder sideProductBuilderTemplate = new ConstructBuilder()
                .addConstruct(participant,RDF.type,SBMLRDF.SIDEPRODUCT);
        ConstructBuilder sideReactantBuilderTemplate = new ConstructBuilder()
                .addConstruct(participant,RDF.type,SBMLRDF.SIDEREACTANT);

        // create where clauses to retrieve side compounds
        sideReactantBuilderTemplate.addWhere(metabolite, RDFS.label, compoundId) //looks for metabolites with sbml identifiers
                .addWhere(participant,SBMLRDF.HAS_SPECIE,metabolite) //looks for specieRef of metabolite
                .addWhere(null,SBMLRDF.REACTANT,participant) //looks if reactant of a reaction
                .addFilter(labelInList); //check if identifier is in side compounds id list
        sideProductBuilderTemplate.addWhere(metabolite, RDFS.label, compoundId) //looks for metabolites with sbml identifiers
                .addWhere(participant,SBMLRDF.HAS_SPECIE,metabolite) //looks for specieRef of metabolite
                .addWhere(null,SBMLRDF.PRODUCT,participant) //looks if product of a reaction
                .addFilter(labelInList); //check if identifier is in side compounds id list

        //execute queries and add results to current model
        QueryExecution qexec = QueryExecutionFactory.create(sideProductBuilderTemplate.build(),rdfModel);
        Model res = qexec.execConstruct(rdfModel);
        qexec.close();
        qexec = QueryExecutionFactory.create(sideReactantBuilderTemplate.build(),rdfModel);
        res = qexec.execConstruct();
        rdfModel.add(res);
        qexec.close();
    }

}
