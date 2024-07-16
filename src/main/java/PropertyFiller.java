import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import vocabulary.SBMLRDF;

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

        System.out.println(qb.buildString());

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

        qb.addWhere(wb);
        String q = qb.buildString();
        System.out.println(q);

        QueryExecution qexec = QueryExecutionFactory.create(qb.build(),rdfModel);
        Model res = qexec.execConstruct(rdfModel);
        qexec.close();

        qb = new ConstructBuilder()
                .addConstruct(product,metabolink,reactant);
        wb.addWhere(reaction,SBMLRDF.REVERSIBLE,true);

        qb.addWhere(wb);
        q = qb.buildString();
        System.out.println(q);

        qexec = QueryExecutionFactory.create(qb.build(),rdfModel);
        res = qexec.execConstruct();
        rdfModel.add(res);
        qexec.close();
    }

}
