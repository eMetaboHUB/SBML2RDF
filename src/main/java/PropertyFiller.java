import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import vocabulary.SBMLRDF;

import java.util.Collection;

public class PropertyFiller {

    public static void harmonizeCompartments(org.apache.jena.rdf.model.Model rdfModel, boolean useSameAs){
        if (!useSameAs) rdfModel.setNsPrefix(SBMLRDF.SIOPREFIX,SBMLRDF.SIOURI);
        Var compound1 = Var.alloc("c1");
        Var compound2 = Var.alloc("c2");
        Var name = Var.alloc("n");
        Property link = useSameAs ? OWL.sameAs : ResourceFactory.createProperty(SBMLRDF.SIOURI,"SIO_000272");

        ConstructBuilder qb = new ConstructBuilder()
                .addConstruct(compound1,link,compound2)
                .addWhere(compound1, RDF.type, SBMLRDF.SPECIE)
                .addWhere(compound2, RDF.type, SBMLRDF.SPECIE)
                .addWhere(compound1, SBMLRDF.NAME, name)
                .addWhere(compound2, SBMLRDF.NAME, name)
                .addFilter("?c1 != ?c2");

        QueryExecution qexec = QueryExecutionFactory.create(qb.build(),rdfModel);
        Model res = qexec.execConstruct();
        rdfModel.add(res);
        qexec.close();

    }

    public static void addMetaboLinks(org.apache.jena.rdf.model.Model rdfModel, Boolean useTransitive){
        rdfModel.setNsPrefix(SBMLRDF.SIOPREFIX,SBMLRDF.SIOURI);
        Property metabolink = useTransitive ? ResourceFactory.createProperty(SBMLRDF.SIOURI, "SIO_000245") : ResourceFactory.createProperty(SBMLRDF.SIOURI, "SIO_000246");

        Var reaction = Var.alloc("r");
        Var reactant = Var.alloc("s");
        Var product = Var.alloc("p");
        Var reactantParticipant = Var.alloc("sp");
        Var productParticipant = Var.alloc("pp");

        ConstructBuilder qb = new ConstructBuilder()
            .addConstruct(reactant,metabolink,product);

        WhereBuilder wb = new WhereBuilder()
        .addWhere(reaction,SBMLRDF.REACTANT,reactantParticipant)
        .addWhere(reactantParticipant,SBMLRDF.HAS_SPECIE,reactant)
        .addWhere(reaction,SBMLRDF.PRODUCT,productParticipant)
        .addWhere(productParticipant,SBMLRDF.HAS_SPECIE,product);

        //TODO filter side compounds
        
        qb.addWhere(wb);

        QueryExecution qexec = QueryExecutionFactory.create(qb.build(),rdfModel);
        Model res = qexec.execConstruct(rdfModel);
        qexec.close();

        qb = new ConstructBuilder()
                .addConstruct(product,metabolink,reactant);
        wb.addWhere(reaction,SBMLRDF.REVERSIBLE,true);

        qb.addWhere(wb);

        qexec = QueryExecutionFactory.create(qb.build(),rdfModel);
        res = qexec.execConstruct();
        rdfModel.add(res);
        qexec.close();
    }

    public static void importSideCompounds(org.apache.jena.rdf.model.Model rdfModel, Collection<String> sideCompoundIds){

        Var participant = Var.alloc("p");
        Var metabolite = Var.alloc("m");
        Var compoundId = Var.alloc("l");
        Resource sideReactant = ResourceFactory.createResource(SBMLRDF.SBOURI+"SBO_0000604");
        Resource sideProduct = ResourceFactory.createResource(SBMLRDF.SBOURI+"SBO_0000603");

        ExprList sideCompoundsIdsSet = new ExprList();
        for(String id : sideCompoundIds){
            sideCompoundsIdsSet.add(new NodeValueString(id));
        }
        E_OneOf labelInList = new E_OneOf(new ExprVar(compoundId),sideCompoundsIdsSet);

        ConstructBuilder sideProductBuilderTemplate = new ConstructBuilder()
                .addConstruct(participant,RDF.type,sideProduct);
        ConstructBuilder sideReactantBuilderTemplate = new ConstructBuilder()
                .addConstruct(participant,RDF.type,sideReactant);

        sideReactantBuilderTemplate.addWhere(metabolite, RDFS.label, compoundId)
                .addWhere(participant,SBMLRDF.HAS_SPECIE,metabolite)
                .addWhere(null,SBMLRDF.REACTANT,participant)
                .addFilter(labelInList);
        sideProductBuilderTemplate.addWhere(metabolite, RDFS.label, compoundId)
                .addWhere(participant,SBMLRDF.HAS_SPECIE,metabolite)
                .addWhere(null,SBMLRDF.PRODUCT,participant)
                .addFilter(labelInList);

        System.out.println(sideProductBuilderTemplate.buildString());
        for(Statement s : rdfModel.listStatements(null,RDFS.label,(String)null).toList()){
            System.out.println(s.getSubject().asResource().getLocalName()+" : "+s.getObject().asLiteral().getString());
        }

        QueryExecution qexec = QueryExecutionFactory.create(sideProductBuilderTemplate.build(),rdfModel);
        Model res = qexec.execConstruct(rdfModel);
        qexec.close();
        qexec = QueryExecutionFactory.create(sideReactantBuilderTemplate.build(),rdfModel);
        res = qexec.execConstruct();
        rdfModel.add(res);
        qexec.close();
    }

}
