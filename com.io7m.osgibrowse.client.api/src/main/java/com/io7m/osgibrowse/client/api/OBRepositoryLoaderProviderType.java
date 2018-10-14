package com.io7m.osgibrowse.client.api;

import java.net.URI;
import java.net.URISyntaxException;

public interface OBRepositoryLoaderProviderType
{
  default OBRepositoryLoaderType forURI(
    final String uri)
    throws URISyntaxException
  {
    return this.forURI(new URI(uri));
  }

  OBRepositoryLoaderType forURI(URI uri);
}
