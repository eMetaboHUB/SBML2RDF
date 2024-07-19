import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The CLI for the conversion from sbml to turtle RDF
 * @author cfrainay
 */
public class App {

    @Option(name = "-i", aliases = {"--sbml"}, usage = "input SBML file", required = true)
    public String inputPath = null;

    @Option(name = "-u", aliases = {"--uri"}, usage = "URI that uniquely identify the model", required = true)
    public String uri = null;

    @Option(name = "-o", aliases = {"--ttl"}, usage = "path to RDF turtle output file", required = true)
    public String outputPath = null;

    @Option(name = "-s", aliases = {"--silent"},usage = "disable console print", required = false)
    private Boolean silent = false;

    @Option(name = "-lc", aliases = {"--linkCompartments"},usage = "[enhance] add links between same compounds in different compartments (must share same sbml.name)", required = false)
    private Boolean linkCompartments = false;

    @Option(name = "-ml", aliases = {"--addMetaboLinks"},usage = "[enhance] add direct \"derives into\" links between reactants and products of the same reaction", required = false)
    private Boolean addMetaboLink = false;

    @Option(name = "-sc", aliases = {"--importSideCompounds"},usage = "[enhance] add side compounds typing, which are ignored when using --addMetaboLink (recommended). Requires a file with one side compound sbml identifier per line", required = false)
    private String importSideCompounds = null;

    @Option(name = "-h", aliases = {"--help"},usage = "prints the help", required = false)
    private Boolean h = false;

    private static Set<String> parseSideCompoundsFile(String inputpath) throws IOException {
        Set<String> sideCompounds = Files.lines(Paths.get(inputpath)).collect(Collectors.toSet());
        return sideCompounds;
    }

    public static void main(String[] args) throws IOException {

        App app = new App();
        app.parseArguments(args);

        if(!app.silent) System.out.println(App.getLabel());
        try {
            //parse SBML using JSBML library
            //------------------------------
            Instant start = Instant.now();
            if(!app.silent) System.out.println("parsing model...");
            SBMLDocument doc = new SBMLReader().readSBMLFromFile(app.inputPath);
            Model sbmlModel = doc.getModel(); //JSBML model stores all data from SBML file
            if(!app.silent){
                System.out.println("SBML file parsed.");
                System.out.println(sbmlModel.getCompartmentCount()+" Compartment");
                System.out.println(sbmlModel.getSpeciesCount()+" Species");
                System.out.println(sbmlModel.getReactionCount()+" Reactions");
            }

            //create RDF model using Jena library
            //-----------------------------------
            if(!app.silent) System.out.println("\ncreating RDF statements...");
            Convertor convert = new Convertor(sbmlModel, app.uri);
            convert.run();
            org.apache.jena.rdf.model.Model rdf = convert.getRdfModel();

            if(!app.silent) System.out.println(rdf.listStatements().toList().size()+" triples");

            // [optional] add extra links:
            //----------------------------
            int n = rdf.listStatements().toList().size();
            if(!app.silent && (app.linkCompartments || app.importSideCompounds!=null || app.addMetaboLink)) System.out.println("[enhance] adding extra triples:");

            //      [optional] add links between compartments' compounds
            //----------------------------------------------------------
            if(app.linkCompartments){
                if(!app.silent) System.out.println("[enhance] Harmonizing compartmentalized compound versions...");
                PropertyFiller.harmonizeCompartments(rdf,false);
                if(!app.silent) System.out.println((rdf.listStatements().toList().size()-n)+" triples added");
                n = rdf.listStatements().toList().size();
            }
            //      [optional] tag side compounds from file
            //---------------------------------------------
            if(app.importSideCompounds!=null){
                if(!app.silent) System.out.println("[enhance] Importing side compounds...");
                Collection<String> sideCompounds = parseSideCompoundsFile(app.importSideCompounds);
                if(!app.silent) System.out.println("[enhance] "+sideCompounds.size()+" side compounds imported.");
                if(!app.silent) System.out.println("[enhance] Tagging reactions' side reactants and side products...");
                PropertyFiller.importSideCompounds(rdf,sideCompounds);
                if(!app.silent) System.out.println((rdf.listStatements().toList().size()-n)+" triples added");
                n = rdf.listStatements().toList().size();
            }
            //      [optional] add compound-to-compound metabolic relationship
            //----------------------------------------------------------------
            if(app.addMetaboLink){
                if(!app.silent && app.importSideCompounds!=null) System.out.println("[enhance] Adding compound-to-compound metabolic links, ignoring side compounds...");
                if(!app.silent && app.importSideCompounds==null) System.out.println("[enhance] Adding compound-to-compound metabolic links...");
                PropertyFiller.addMetaboLinks(rdf,false);
                if(!app.silent) System.out.println((rdf.listStatements().toList().size()-n)+" triples added");
            }

            rdf.setNsPrefix("cid","http://identifiers.org/pubchem.compound/");
            rdf.setNsPrefix("chebi","http://identifiers.org/chebi/CHEBI:");
            rdf.setNsPrefix("mnxCHEM", "http://identifiers.org/metanetx.chemical/");
            if(!app.silent) System.out.println("RDF model created.");
            if(!app.silent) System.out.println(rdf.listStatements().toList().size()+" triples");

            //write RDF model in turtle
            //-------------------------
            OutputStream out = new FileOutputStream(new File(app.outputPath));
            RDFDataMgr.write(out, rdf, RDFFormat.TURTLE);
            if(!app.silent)System.out.println("\nRDF model exported : "+app.outputPath);
            Instant end = Instant.now();
            Duration elapsedTime = Duration.between(start, end);

            System.out.println("Execution time: " + elapsedTime.toSeconds()+"s");

        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static String getLabel() {return "\n" +
            " _____ _____ _____ __       ___    _____ ____  _____ \n" +
            "|   __| __  |     |  |     |_  |  | __  |    \\|   __|\n" +
            "|__   | __ -| | | |  |__   |  _|  |    -|  |  |   __|\n" +
            "|_____|_____|_|_|_|_____|  |___|  |__|__|____/|__|   \n";}

    public static String getDescription() {return "" +
            "A tool that converts a metabolic network in SBML format into RDF (turtle synthax).\n" +
            "SBML2RDF also include an optional model enhancement for knowledge graph, adding links between same compounds" +
            " in different compartments, direct links between reactants and products of the same reaction " +
            "(bypassing [specie <- specieRef <- reaction -> specieRef -> specie] paths), " +
            "and side compounds (also known as, or closely related to : ubiquitous/auxiliary/currency/ancillary compounds) typing from a provided list.\n"+
            "SBML2RDF use the [JSBML](http://sbml.org/Software/JSBML) library for SBML file parsing and the [JENA](https://jena.apache.org/documentation/rdf/index.html) RDF API for building the triples.  \n" +
            "SBML2RDF use biomodels' [SBML vocabulary](https://registry.identifiers.org/registry/biomodels.vocabulary) to describe the SBML content.  \n";
    }

    public static String getUsage() {return "" +
            "The SBML2RDF convertor requires a metabolic network in SBML file format and a URI (Uniform Resource Identifiers) that uniquely identify this model\n" +
            "Examples: " +
            "https://metexplore.toulouse.inra.fr/metexplore2/?idBioSource=1363, " +
            "https://www.ebi.ac.uk/biomodels/MODEL1311110001\n" +
            "\n\t```" +
            "\n\tjava -jar SBML2RDF.jar -i path/to/sbml.xml -u 'http://my.model.uri#id' -o path/to/output.ttl" +
            "\n\t```\n"+
            "The final model can be enhanced with extra triples using the following options:\n" +
            "\n\t```" +
            "\n\tjava -jar SBML2RDF.jar -i path/to/sbml.xml -u 'http://my.model.uri#id' -o path/to/output.ttl --linkCompartments --addMetaboLinks --importSideCompounds path/to/side_compounds_file.txt" +
            "\n\t```\n" +
            "The side compounds file must contains one entry per line, using the same identifier system as the input sbml. Such list can be defined manually or obtained using the Met4J toolbox.\n" +
            "The linkCompartments option requires that the SBML's entries of the same compound in different compartments share the same names.\n\n";
    }


    public void printHeader()
    {
        System.out.println(this.getLabel());
        System.out.println(this.getDescription());
        System.out.println(this.getUsage());
    }

    protected void parseArguments(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            if(this.h == false) {
                System.err.println("Error in arguments\n");
                System.err.println(this.getUsage());
                parser.printUsage(System.err);
                System.exit(0);
            }
            else {
                System.out.println(this.getUsage());
                parser.printUsage(System.out);
                System.exit(0);
            }
        }

        if (this.h == true) {
            System.out.println(this.getUsage());
            parser.printUsage(System.out);
            System.exit(0);
        }
    }

}
