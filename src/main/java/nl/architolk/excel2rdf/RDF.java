package nl.architolk.excel2rdf;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class RDF {

  public final static String prefix = "rdf";
  public final static String namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  public final static Property type = ResourceFactory.createProperty(namespace, "type");

}
