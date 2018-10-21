package com.io7m.osgibrowse.client.api;

import com.io7m.osgibrowse.catalog.api.OBCatalogParseError;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;

import java.util.Objects;

public final class OBExceptionCatalogFailed extends OBException
{
  private final Seq<OBCatalogParseError> errors;

  public OBExceptionCatalogFailed(
    final Throwable cause)
  {
    super(Objects.requireNonNull(cause, "cause"));
    this.errors = Vector.empty();
  }

  public OBExceptionCatalogFailed(
    final Seq<OBCatalogParseError> in_errors)
  {
    super("Unable to parse catalog");
    this.errors = Objects.requireNonNull(in_errors, "errors");
  }
}
