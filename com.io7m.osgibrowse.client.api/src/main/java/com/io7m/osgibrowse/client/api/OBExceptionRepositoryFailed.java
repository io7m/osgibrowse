package com.io7m.osgibrowse.client.api;

import java.util.Objects;

public final class OBExceptionRepositoryFailed extends OBException
{
  public OBExceptionRepositoryFailed(
    final Throwable cause)
  {
    super(Objects.requireNonNull(cause, "cause"));
  }
}
