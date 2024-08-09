package nl.architolk.excel2rdf;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class ExtendedProperty  {

  protected Property fullProperty;
  protected Property lineProperty;

  public ExtendedProperty(Property property) {
    fullProperty = property;
    lineProperty = ResourceFactory.createProperty(property.getNameSpace(),property.getLocalName() + "_line");
  }

  public Property getFullProperty() {
    return fullProperty;
  }

  public Property getLineProperty() {
    return lineProperty;
  }

}
