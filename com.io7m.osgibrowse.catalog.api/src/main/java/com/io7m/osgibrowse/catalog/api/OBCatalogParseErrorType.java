package com.io7m.osgibrowse.catalog.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jlexing.core.LexicalType;
import org.immutables.value.Value;

import java.net.URI;
import java.util.Optional;

/**
 * The type of parse errors.
 */

@ImmutablesStyleType
@Value.Immutable
public interface OBCatalogParseErrorType extends LexicalType<URI>
{
  @Override
  @Value.Parameter
  LexicalPosition<URI> lexical();

  /**
   * @return The severity of the error
   */

  @Value.Parameter
  Severity severity();

  /**
   * @return The error message
   */

  @Value.Parameter
  String message();

  /**
   * @return The exception raised, if any
   */

  @Value.Parameter
  Optional<Exception> exception();

  /**
   * The severity of the error.
   */

  enum Severity
  {
    /**
     * A warning.
     */

    WARNING,

    /**
     * An error.
     */

    ERROR
  }

}
