package com.io7m.osgibrowse.catalog.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.net.URI;
import java.util.Comparator;
import java.util.Objects;

@Value.Immutable
@ImmutablesStyleType
public interface OBCatalogRepositoryLinkType extends Comparable<OBCatalogRepositoryLinkType>
{
  URI uri();

  @Override
  default int compareTo(
    final OBCatalogRepositoryLinkType other)
  {
    Objects.requireNonNull(other, "other");
    return Comparator.comparing(OBCatalogRepositoryLinkType::uri)
      .compare(this, other);
  }
}
