package com.io7m.osgibrowse.client.api;

import java.util.Objects;

public abstract class OBException extends Exception
{
  public OBException(final String message)
  {
    super(Objects.requireNonNull(message, "message"));
  }

  public OBException(
    final String message,
    final Throwable cause)
  {
    super(Objects.requireNonNull(message, "message"),
          Objects.requireNonNull(cause, "cause"));
  }

  public OBException(final Throwable cause)
  {
    super(Objects.requireNonNull(cause, "cause"));
  }
}
