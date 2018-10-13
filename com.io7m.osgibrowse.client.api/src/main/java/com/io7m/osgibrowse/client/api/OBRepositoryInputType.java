package com.io7m.osgibrowse.client.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;
import org.osgi.service.repository.Repository;

import java.net.URI;

@ImmutablesStyleType
@Value.Immutable
public interface OBRepositoryInputType
{
  URI uri();

  Repository repository();
}
