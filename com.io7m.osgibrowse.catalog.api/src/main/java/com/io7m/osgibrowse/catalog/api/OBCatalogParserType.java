package com.io7m.osgibrowse.catalog.api;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.io.Closeable;

public interface OBCatalogParserType extends Closeable
{
  Validation<Seq<OBCatalogParseError>, OBCatalogType> parse();
}
