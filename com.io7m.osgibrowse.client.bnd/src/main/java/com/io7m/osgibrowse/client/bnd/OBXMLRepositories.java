package com.io7m.osgibrowse.client.bnd;

import aQute.bnd.osgi.repository.ResourcesRepository;
import aQute.bnd.osgi.repository.XMLResourceParser;
import com.io7m.osgibrowse.client.api.OBRepositoryInput;
import com.io7m.osgibrowse.client.api.OBRepositoryInputType;
import com.io7m.osgibrowse.client.api.OBRepositoryLoaderType;
import org.osgi.resource.Resource;
import org.osgi.service.repository.Repository;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public final class OBXMLRepositories implements OBRepositoryLoaderType
{
  private final URI uri;
  private final ExecutorService exec;

  private OBXMLRepositories(
    final ExecutorService in_exec,
    final URI in_uri)
  {
    this.exec = Objects.requireNonNull(in_exec, "executor");
    this.uri = Objects.requireNonNull(in_uri, "uri");
  }

  @Override
  public CompletableFuture<OBRepositoryInputType> load()
  {
    final CompletableFuture<OBRepositoryInputType> future = new CompletableFuture<>();
    this.exec.execute(() -> {
      try {
        future.complete(fromURI(this.uri));
      } catch (final Throwable t) {
        future.completeExceptionally(t);
      }
    });
    return future;
  }

  /**
   * Load an XML repository from the given URI.
   *
   * @param uri The URI
   *
   * @return A parsed repository
   *
   * @throws Exception On errors
   */

  public static OBRepositoryInputType fromURI(final URI uri)
    throws Exception
  {
    Objects.requireNonNull(uri, "uri");

    final List<Resource> resources = XMLResourceParser.getResources(uri);
    final Repository repository = new ResourcesRepository(resources);
    return OBRepositoryInput.builder()
      .setUri(uri)
      .setRepository(repository)
      .build();
  }
}
