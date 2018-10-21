/*
 * Copyright © 2017 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.osgibrowse.catalog.xml;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.osgibrowse.catalog.api.OBCatalogParseError;
import io.vavr.collection.Vector;
import org.xml.sax.SAXParseException;

import java.net.URI;
import java.util.Objects;

/**
 * An error log.
 */

public final class OBCatalogXMLErrorLog
{
  private Vector<OBCatalogParseError> errors;

  /**
   * Construct an empty log.
   */

  public OBCatalogXMLErrorLog()
  {
    this.errors = Vector.empty();
  }

  /**
   * Create a parse error from the given parse exception.
   *
   * @param e The exception
   *
   * @return A parse error
   */

  public static OBCatalogParseError createErrorFromParseException(
    final SAXParseException e)
  {
    Objects.requireNonNull(e, "Exception");

    return OBCatalogParseError.builder()
      .setException(e)
      .setLexical(
        LexicalPosition.<URI>builder()
          .setFile(URI.create(e.getSystemId()))
          .setColumn(e.getColumnNumber())
          .setLine(e.getLineNumber())
          .build())
      .setSeverity(OBCatalogParseError.Severity.ERROR)
      .setMessage(e.getMessage())
      .build();
  }

  /**
   * Create a parse error from the given exception.
   *
   * @param uri The URI of the source file
   * @param e   The exception
   *
   * @return A parse error
   */

  public static OBCatalogParseError createErrorFromException(
    final URI uri,
    final Exception e)
  {
    Objects.requireNonNull(uri, "URI");
    Objects.requireNonNull(e, "Exception");

    return OBCatalogParseError.builder()
      .setException(e)
      .setLexical(
        LexicalPosition.<URI>builder()
          .setFile(uri)
          .setColumn(-1)
          .setLine(-1)
          .build())
      .setSeverity(OBCatalogParseError.Severity.ERROR)
      .setMessage(e.getMessage())
      .build();
  }

  private void logError(
    final OBCatalogParseError e)
  {
    this.errors = this.errors.append(
      Objects.requireNonNull(e, "Error"));
  }

  /**
   * Add the given parse error.
   *
   * @param e The parse error
   */

  public void addError(
    final OBCatalogParseError e)
  {
    this.logError(Objects.requireNonNull(e, "Error"));
  }

  /**
   * @return The current (immutable) vector of errors
   */

  public Vector<OBCatalogParseError> errors()
  {
    return this.errors;
  }

  /**
   * Add a warning based on the given exception
   *
   * @param e The exception
   */

  public void warning(
    final SAXParseException e)
  {
    this.logError(
      OBCatalogParseError.builder()
        .setException(e)
        .setLexical(
          LexicalPosition.<URI>builder()
            .setFile(URI.create(e.getSystemId()))
            .setColumn(e.getColumnNumber())
            .setLine(e.getLineNumber())
            .build())
        .setSeverity(OBCatalogParseError.Severity.WARNING)
        .setMessage(e.getMessage())
        .build());
  }

  /**
   * Add an error based on the given exception
   *
   * @param e The exception
   */

  public void error(
    final SAXParseException e)
  {
    this.errors = this.errors.append(createErrorFromParseException(e));
  }

  /**
   * Add a fatal error based on the given exception
   *
   * @param e The exception
   */

  public void fatalError(
    final SAXParseException e)
  {
    this.errors = this.errors.append(createErrorFromParseException(e));
  }
}
