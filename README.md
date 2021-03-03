# SBML2RDF

A simple tool that converts a metabolic network in SBML format into RDF (turtle synthax).
SBML2RDF use the [JSBML](http://sbml.org/Software/JSBML) library for SBML file parsing and the [JENA](https://jena.apache.org/documentation/rdf/index.html) RDF API for building the triples.  
SBML2RDF use biomodels' [SBML vocabulary](https://registry.identifiers.org/registry/biomodels.vocabulary) to describe the SBML content.

## Usage

the SBML2RDF convertor requires a metabolic network in SBML file format and a URI (Uniform Resource Identifiers) that uniquely identify this model
Examples: https://metexplore.toulouse.inra.fr/metexplore2/?idBioSource=1363, https://www.ebi.ac.uk/biomodels/MODEL1311110001

```
java -jar SBML2RDF.jar -i path/tp/sbml.xml -u 'http://my.model.uri#id' -o path/to/output.ttl


 -h (--help)     : prints the help (default: false)
 -i (--sbml) VAL : input SBML file
 -o (--ttl) VAL  : path to RDF turtle output file
 -s (--silent)   : disable console print (default: false)
 -u (--uri) VAL  : URI that uniquely identify the model
```

## Acknowledgment

This project happen to be a simpler re-implementation of another project that can be found here: [ricordo-rdfconverter](https://github.com/sarala/ricordo-rdfconverter)