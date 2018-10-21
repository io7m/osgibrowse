package com.io7m.osgibrowse.tests.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public final class ResourceURLHandlers implements URLStreamHandlerFactory
{
  private static final Logger LOG = LoggerFactory.getLogger(ResourceURLHandlers.class);

  public ResourceURLHandlers()
  {

  }

  @Override
  public URLStreamHandler createURLStreamHandler(final String protocol) {
    if ("resource".equals(protocol)) {
      return new Handler();
    }

    return null;
  }

  private static final class Handler extends URLStreamHandler
  {
    Handler()
    {

    }

    @Override
    protected URLConnection openConnection(final URL u)
      throws IOException
    {
      LOG.debug("openConnection: {}", u);
      final String path = u.toString().replace("resource:/", "");
      LOG.debug("openConnection: path {}", path);
      return ResourceURLHandlers.class.getResource(path).openConnection();
    }
  }
}