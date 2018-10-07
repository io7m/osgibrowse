package com.io7m.osgibrowse.client.api;

import io.reactivex.Observable;
import org.osgi.resource.Resource;
import org.osgi.service.repository.Repository;

import java.io.Closeable;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public interface OBClientType extends Closeable
{
  Observable<OBClientEventType> events();

  CompletableFuture<Void> repositoryAdd(
    URI uri,
    Repository repository);

  default CompletableFuture<Void> repositoryAdd(
    final OBRepositoryType repository)
  {
    Objects.requireNonNull(repository, "repository");
    return this.repositoryAdd(repository.uri(), repository.repository());
  }

  CompletableFuture<Void> repositoryRemove(
    URI uri);

  CompletableFuture<Void> bundleSelect(
    OBBundleIdentifier bundle);

  CompletableFuture<Boolean> bundleIsSelected(
    OBBundleIdentifier bundle);

  CompletableFuture<List<Resource>> bundlesResolved();
}
