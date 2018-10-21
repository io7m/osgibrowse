package com.io7m.osgibrowse.catalog.api;

import java.util.Objects;

public class OBCatalogParserConfigurationException extends Exception
{
  public OBCatalogParserConfigurationException(
    final String message)
  {
    super(Objects.requireNonNull(message, "message"));
  }

  public OBCatalogParserConfigurationException(
    final String message,
    final Throwable cause)
  {
    super(
      Objects.requireNonNull(message, "message"),
      Objects.requireNonNull(cause, "cause"));
  }

  public OBCatalogParserConfigurationException(
    final Throwable cause)
  {
    super(Objects.requireNonNull(cause, "cause"));
  }
}
