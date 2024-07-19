# SBML2RDF
```
 _____ _____ _____ __       ___    _____ ____  _____ 
|   __| __  |     |  |     |_  |  | __  |    \|   __|
|__   | __ -| | | |  |__   |  _|  |    -|  |  |   __|
|_____|_____|_|_|_|_____|  |___|  |__|__|____/|__|
```

A tool that converts a metabolic network in SBML format into RDF (turtle synthax).
SBML2RDF also include an optional model enhancement for knowledge graph, adding links between same compounds in different compartments, direct links between reactants and products of the same reaction (bypassing [specie <- specieRef <- reaction -> specieRef -> specie] paths), and side compounds (also known as, or closely related to : ubiquitous/auxiliary/currency/ancillary compounds) typing from a provided list.
SBML2RDF use the [JSBML](http://sbml.org/Software/JSBML) library for SBML file parsing and the [JENA](https://jena.apache.org/documentation/rdf/index.html) RDF API for building the triples.  
SBML2RDF use biomodels' [SBML vocabulary](https://registry.identifiers.org/registry/biomodels.vocabulary) to describe the SBML content.

## Usage

The SBML2RDF convertor requires a metabolic network in SBML file format and a URI (Uniform Resource Identifiers) that uniquely identify this model
Examples: https://metexplore.toulouse.inra.fr/metexplore2/?idBioSource=1363, https://www.ebi.ac.uk/biomodels/MODEL1311110001

	```
	java -jar SBML2RDF.jar -i path/to/sbml.xml -u 'http://my.model.uri#id' -o path/to/output.ttl
	```
The final model can be enhanced with extra triples using the following options:

	```
	java -jar SBML2RDF.jar -i path/to/sbml.xml -u 'http://my.model.uri#id' -o path/to/output.ttl --linkCompartments --addMetaboLinks --importSideCompounds path/to/side_compounds_file.txt
	```
The side compounds file must contains one entry per line, using the same identifier system as the input sbml. Such list can be defined manually or obtained using the Met4J toolbox.
The linkCompartments option requires that the SBML's entries of the same compound in different compartments share the same names.

    ```
    -h (--help)                     : prints the help (default: true)
    -i (--sbml) VAL                 : input SBML file
    -lc (--linkCompartments)        : [enhance] add links between same compounds
    in different compartments (must share same
    sbml.name) (default: false)
    -ml (--addMetaboLinks)          : [enhance] add direct "derives into" links
    between reactants and products of the same
    reaction (default: false)
    -o (--ttl) VAL                  : path to RDF turtle output file
    -s (--silent)                   : disable console print (default: false)
    -sc (--importSideCompounds) VAL : [enhance] add side compounds typing, which
    are ignored when using --addMetaboLink
    (recommended). Requires a file with one side
    compound sbml identifier per line
    -u (--uri) VAL                  : URI that uniquely identify the model
    ```

## Acknowledgment

This project happen to be a simpler re-implementation of another project that can be found here: [ricordo-rdfconverter](https://github.com/sarala/ricordo-rdfconverter)