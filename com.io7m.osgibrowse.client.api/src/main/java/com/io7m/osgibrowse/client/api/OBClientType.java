package com.io7m.osgibrowse.client.api;

import io.reactivex.Observable;
import io.vavr.collection.SortedMap;
import io.vavr.collection.SortedSet;
import io.vavr.collection.Vector;
import org.osgi.resource.Resource;
import org.osgi.service.repository.Repository;

import java.io.Closeable;
import java.net.URI;
import java.util.Objects;

public interface OBClientType extends Closeable
{
  Observable<OBClientEventType> events();

  void repositoryAdd(
    String uri)
    throws OBExceptionRepositoryFailed;

  void repositoryAdd(
    URI uri)
    throws OBExceptionRepositoryFailed;

  void repositoryAdd(
    URI uri,
    Repository repository)
    throws OBExceptionRepositoryFailed;

  default void repositoryAdd(
    final OBRepositoryInputType repository)
    throws OBExceptionRepositoryFailed
  {
    Objects.requireNonNull(repository, "repository");
    this.repositoryAdd(repository.uri(), repository.repository());
  }

  void repositoryRemove(
    URI uri);

  SortedMap<URI, OBRepositoryType> repositoryList();

  void bundleSelectToggle(
    OBBundleIdentifier bundle)
    throws OBExceptionBundleNotFound;

  default boolean bundleIsSelected(
    final OBBundleIdentifier bundle)
  {
    Objects.requireNonNull(bundle, "bundle");
    return this.bundlesSelected().contains(bundle);
  }

  SortedSet<OBBundleIdentifier> bundlesSelected();

  Vector<Resource> bundlesResolved()
    throws OBExceptionResolutionFailed;
}
