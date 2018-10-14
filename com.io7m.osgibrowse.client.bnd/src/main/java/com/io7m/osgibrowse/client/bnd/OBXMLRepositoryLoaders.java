package com.io7m.osgibrowse.client.bnd;

import aQute.bnd.osgi.repository.ResourcesRepository;
import aQute.bnd.osgi.repository.XMLResourceParser;
import com.io7m.osgibrowse.client.api.OBRepositoryInput;
import com.io7m.osgibrowse.client.api.OBRepositoryInputType;
import com.io7m.osgibrowse.client.api.OBRepositoryLoaderProviderType;
import com.io7m.osgibrowse.client.api.OBRepositoryLoaderType;
import org.osgi.resource.Resource;
import org.osgi.service.repository.Repository;

import java.net.URI;
import java.util.List;
import java.util.Objects;

public final class OBXMLRepositoryLoaders implements OBRepositoryLoaderProviderType
{
  public OBXMLRepositoryLoaders()
  {

  }

  @Override
  public OBRepositoryLoaderType forURI(final URI uri)
  {
    return new Loader(uri);
  }

  private static final class Loader implements OBRepositoryLoaderType
  {
    private final URI uri;

    Loader(final URI in_uri)
    {
      this.uri = Objects.requireNonNull(in_uri, "uri");
    }

    @Override
    public OBRepositoryInputType load()
      throws Exception
    {
      Objects.requireNonNull(this.uri, "uri");

      final List<Resource> resources = XMLResourceParser.getResources(this.uri);
      final Repository repository = new ResourcesRepository(resources);
      return OBRepositoryInput.builder()
        .setUri(this.uri)
        .setRepository(repository)
        .build();
    }
  }
}
