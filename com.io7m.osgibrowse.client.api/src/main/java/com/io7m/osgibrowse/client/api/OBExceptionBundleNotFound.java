package com.io7m.osgibrowse.client.api;

import java.util.Objects;

public final class OBExceptionBundleNotFound extends OBException
{
  private final OBBundleIdentifier identifier;

  public OBExceptionBundleNotFound(
    final OBBundleIdentifier in_identifier)
  {
    super(new StringBuilder(64)
            .append("Bundle ")
            .append(Objects.requireNonNull(in_identifier, "identifier").toSymbolicString())
            .append(" not found")
            .toString());
    this.identifier = in_identifier;
  }

  public OBBundleIdentifier identifier()
  {
    return this.identifier;
  }
}
