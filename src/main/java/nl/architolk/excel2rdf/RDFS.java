package nl.architolk.excel2rdf;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class RDFS {

  public final static String prefix = "rdfs";
  public final static String namespace = "http://www.w3.org/2000/01/rdf-schema#";
  public final static Property label = ResourceFactory.createProperty(namespace, "label");

}
