package nl.architolk.excel2rdf;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class CSVW {

  public final static String prefix = "csvw";
  public final static String namespace = "http://www.w3.org/ns/csvw#";
  public final static Resource TableGroup = ResourceFactory.createResource(namespace + "TableGroup");
  public final static Resource Table = ResourceFactory.createResource(namespace + "Table");
  public final static Resource Row = ResourceFactory.createResource(namespace + "Row");
  public final static Property table = ResourceFactory.createProperty(namespace, "table");
  public final static Property describes = ResourceFactory.createProperty(namespace, "describes");
  public final static Property rownum = ResourceFactory.createProperty(namespace, "rownum");

}
