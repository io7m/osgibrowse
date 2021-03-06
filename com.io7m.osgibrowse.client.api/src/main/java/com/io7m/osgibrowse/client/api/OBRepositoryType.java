package com.io7m.osgibrowse.client.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import io.vavr.collection.SortedMap;
import org.immutables.value.Value;
import org.osgi.resource.Resource;

import java.net.URI;

@Value.Immutable
@ImmutablesStyleType
public interface OBRepositoryType
{
  @Value.Parameter
  URI uri();

  @Value.Parameter
  SortedMap<OBBundleIdentifier, Resource> bundles();
}
