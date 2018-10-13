package com.io7m.osgibrowse.client.api;

import java.util.Objects;

public final class OBExceptionResolutionFailed extends OBException
{
  public OBExceptionResolutionFailed(
    final Throwable cause)
  {
    super(Objects.requireNonNull(cause, "cause"));
  }
}
