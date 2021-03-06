package com.io7m.osgibrowse.client.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.net.URI;

@Value.Immutable
@ImmutablesStyleType
public interface OBClientEventRepositoryRemovedType extends OBClientEventType
{
  @Override
  default OBClientEventKind kind()
  {
    return OBClientEventKind.REPOSITORY_REMOVED;
  }

  @Value.Parameter
  URI uri();

  @Override
  default String describe()
  {
    return new StringBuilder(64)
      .append("Removed repository ")
      .append(this.uri())
      .toString();
  }
}
