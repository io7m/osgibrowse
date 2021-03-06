package com.io7m.osgibrowse.client.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.net.URI;

@Value.Immutable
@ImmutablesStyleType
public interface OBClientEventCatalogAddFailedType extends OBClientEventType
{
  @Override
  default OBClientEventKind kind()
  {
    return OBClientEventKind.CATALOG_ADD_FAILED;
  }

  @Value.Parameter
  URI uri();

  @Value.Parameter
  Exception exception();

  @Override
  default String describe()
  {
    final Exception ex = this.exception();
    return new StringBuilder(64)
      .append("Failed to add catalog ")
      .append(this.uri())
      .append(": ")
      .append(ex.getClass().getName())
      .append(": ")
      .append(ex.getMessage())
      .toString();
  }
}
