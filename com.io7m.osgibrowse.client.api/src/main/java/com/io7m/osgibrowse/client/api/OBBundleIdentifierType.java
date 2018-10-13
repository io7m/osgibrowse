package com.io7m.osgibrowse.client.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;
import org.osgi.framework.Version;

import java.util.Comparator;

@ImmutablesStyleType
@Value.Immutable
public interface OBBundleIdentifierType extends Comparable<OBBundleIdentifierType>
{
  @Value.Parameter
  String name();

  @Value.Parameter
  Version version();

  default String toSymbolicString()
  {
    return new StringBuilder(32)
      .append(this.name())
      .append(':')
      .append(this.version().toString())
      .toString();
  }

  @Override
  default int compareTo(final OBBundleIdentifierType other)
  {
    return Comparator.comparing(OBBundleIdentifierType::name)
      .thenComparing(OBBundleIdentifierType::version)
      .compare(this, other);
  }
}
