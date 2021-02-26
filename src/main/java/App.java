import org.apache.jena.base.Sys;
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

    @Option(name = "-h", aliases = {"--help"},usage = "prints the help", required = false)
    private Boolean h = false;



    public static void main(String[] args) throws IOException {

        App app = new App();
        app.parseArguments(args);

        if(!app.silent) System.out.println(App.getLabel());
        try {
            //parse SBML using JSBML library
            //------------------------------
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

            rdf.setNsPrefix("cid","http://identifiers.org/pubchem.compound/");
            rdf.setNsPrefix("chebi","http://identifiers.org/chebi/CHEBI:");
            rdf.setNsPrefix("mnxCHEM", "http://identifiers.org/metanetx.chemical/");
            if(!app.silent) System.out.println("RDF model created.");
            if(!app.silent) System.out.println(rdf.listStatements().toList().size()+" triples");

            //write RDF model in turtle
            //-----------------------------------
            OutputStream out = new FileOutputStream(new File(app.outputPath));
            RDFDataMgr.write(out, rdf, RDFFormat.TURTLE);
            if(!app.silent)System.out.println("\nRDF model exported");
            if(!app.silent)System.out.println(app.outputPath);

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
            "A simple tool that converts a metabolic network in SBML format into RDF (turtle synthax).\n" +
            "SBML2RDF use the [JSBML](http://sbml.org/Software/JSBML) library for SBML file parsing and the [JENA](https://jena.apache.org/documentation/rdf/index.html) RDF API for building the triples.  \n" +
            "SBML2RDF use biomodels' [SBML vocabulary](https://registry.identifiers.org/registry/biomodels.vocabulary) to describe the SBML content.  \n";
    }

    public static String getUsage() {return "" +
            "the SBML2RDF convertor requires a metabolic network in SBML file format and a URI (Uniform Resource Identifiers) that uniquely identify this model\n" +
            "Examples: " +
            "https://metexplore.toulouse.inra.fr/metexplore2/?idBioSource=1363, " +
            "https://www.ebi.ac.uk/biomodels/MODEL1311110001\n" +
            "\n\t```" +
            "\n\tjava -jar SBML2RDF.jar -i path/tp/sbml.xml -u 'http://my.model.uri#id' -o path/to/output.ttl" +
            "\n\t```\n";
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
                this.printHeader();
                parser.printUsage(System.out);
                System.exit(0);
            }
        }
    }
}
