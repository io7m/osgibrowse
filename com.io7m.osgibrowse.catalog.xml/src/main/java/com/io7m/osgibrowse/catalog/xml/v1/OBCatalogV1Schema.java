package com.io7m.osgibrowse.catalog.xml.v1;

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jxe.core.JXESchemaDefinition;

import java.net.URI;

public final class OBCatalogV1Schema
{
  /**
   * The schema definition.
   */

  public static final JXESchemaDefinition SCHEMA =
    JXESchemaDefinition.builder()
      .setNamespace(URI.create("osgibrowse:com.io7m.osgibrowse:xml:1.0"))
      .setFileIdentifier("file::schema-1.xsd")
      .setLocation(OBCatalogV1Schema.class.getResource(
        "/com/io7m/osgibrowse/catalog/xml/schema-1.xsd"))
      .build();

  private OBCatalogV1Schema()
  {
    throw new UnreachableCodeException();
  }
}
