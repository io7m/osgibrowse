package com.io7m.osgibrowse.client.bnd;

import aQute.bnd.build.model.EE;
import aQute.bnd.service.resolve.hook.ResolverHook;
import aQute.bnd.version.VersionRange;
import biz.aQute.resolve.BndResolver;
import biz.aQute.resolve.GenericResolveContext;
import biz.aQute.resolve.ResolverLogger;
import com.io7m.jaffirm.core.Invariants;
import com.io7m.osgibrowse.client.api.OBBundleIdentifier;
import com.io7m.osgibrowse.client.api.OBClientEventRepositoryAdded;
import com.io7m.osgibrowse.client.api.OBClientEventRepositoryRemoved;
import com.io7m.osgibrowse.client.api.OBClientEventType;
import com.io7m.osgibrowse.client.api.OBClientType;
import com.io7m.osgibrowse.client.api.OBExceptionBundleNotFound;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import org.osgi.framework.Version;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.service.repository.IdentityExpression;
import org.osgi.service.repository.Repository;
import org.osgi.service.resolver.ResolutionException;
import org.osgi.service.resolver.ResolveContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class OBClient implements OBClientType
{
  private static final Logger LOG = LoggerFactory.getLogger(OBClient.class);

  private final HashMap<URI, RepositoryWithBundles> repositories;
  private final BndResolver resolver;
  private final BehaviorSubject<OBClientEventType> subject;
  private final ExecutorService executor;
  private final HashSet<OBBundleIdentifier> selected;

  OBClient()
  {
    this.executor = Executors.newFixedThreadPool(1, runnable -> {
      final Thread thread = new Thread(runnable);
      thread.setName("com.io7m.osgibrowse.client.bnd.OBClient." + thread.getId());
      return thread;
    });
    this.repositories = new HashMap<>(128);
    this.selected = new HashSet<>(128);
    this.resolver = new BndResolver(new OBResolverLogger(LOG));
    this.subject = BehaviorSubject.create();
  }

  @Override
  public Observable<OBClientEventType> events()
  {
    return this.subject;
  }

  private Void opRepositoryAdd(
    final URI uri,
    final Repository repository)
    throws Exception
  {
    final IdentityExpression req =
      repository.newRequirementBuilder("osgi.identity")
        .buildExpression();

    final Collection<Resource> results =
      repository.findProviders(req)
        .getValue();

    final HashMap<OBBundleIdentifier, Resource> bundles = new HashMap<>(results.size());
    for (final Resource resource : results) {
      bundles.put(makeBundleIdentifierForResource(uri, resource), resource);
    }

    final RepositoryWithBundles processed_repository =
      new RepositoryWithBundles(repository, Collections.unmodifiableMap(bundles));

    this.repositories.put(uri, processed_repository);
    this.subject.onNext(OBClientEventRepositoryAdded.of(uri));
    return null;
  }

  private static OBBundleIdentifier makeBundleIdentifierForResource(
    final URI uri,
    final Resource resource)
  {
    final List<Capability> caps = resource.getCapabilities("osgi.identity");
    Invariants.checkInvariantI(
      caps.size(),
      caps.size() == 1,
      s -> "Resource must have exactly one identity");

    final Capability cap = caps.get(0);

    final Map<String, Object> attributes = cap.getAttributes();

    Invariants.checkInvariant(
      attributes,
      attributes.containsKey("osgi.identity"),
      a -> "Resource must define osgi.identity attribute");

    Invariants.checkInvariant(
      attributes,
      attributes.containsKey("version"),
      a -> "Resource must define version attribute");

    final String name = (String) attributes.get("osgi.identity");
    final Version version = (Version) attributes.get("version");

    if (LOG.isDebugEnabled()) {
      LOG.debug("repository {}: bundle {} {}", uri, name, version);
    }

    return OBBundleIdentifier.builder()
      .setName(name)
      .setVersion(version)
      .build();
  }

  private Void opBundleSelect(
    final OBBundleIdentifier bundle)
    throws OBExceptionBundleNotFound
  {
    boolean found = false;
    for (final RepositoryWithBundles repository : this.repositories.values()) {
      if (repository.resources.containsKey(bundle)) {
        found = true;
        break;
      }
    }

    if (!found) {
      throw new OBExceptionBundleNotFound(bundle);
    }

    this.selected.add(bundle);
    return null;
  }

  private Boolean opBundleIsSelected(
    final OBBundleIdentifier bundle)
  {
    return Boolean.valueOf(this.selected.contains(bundle));
  }

  private Void opRepositoryRemove(
    final URI uri)
  {
    if (this.repositories.containsKey(uri)) {
      this.repositories.remove(uri);
      this.subject.onNext(OBClientEventRepositoryRemoved.of(uri));
    }
    return null;
  }

  private List<Resource> opBundlesResolved()
    throws Exception
  {
    final GenericResolveContext context = new GenericResolveContext(new OBResolverLogger(LOG));
    context.addEE(EE.JavaSE_9_0);

    for (final RepositoryWithBundles repository : this.repositories.values()) {
      context.addRepository(repository.repository);
    }

    for (final OBBundleIdentifier identifier : this.selected) {
      context.addRequireBundle(
        identifier.name(),
        new VersionRange(identifier.version().toString(),
                         identifier.version().toString()));
    }

    context.addFramework("org.apache.felix.framework", null);
    context.done();

    final Map<Resource, List<Wire>> results = this.resolver.resolve(context);
    return new ArrayList<>(results.keySet());
  }

  private <T> CompletableFuture<T> submit(
    final Callable<T> callable)
  {
    final CompletableFuture<T> future = new CompletableFuture<>();
    this.executor.execute(() -> {
      try {
        future.complete(callable.call());
      } catch (final Throwable e) {
        future.completeExceptionally(e);
      }
    });
    return future;
  }

  @Override
  public CompletableFuture<Void> repositoryAdd(
    final URI uri,
    final Repository repository)
  {
    Objects.requireNonNull(uri, "uri");
    Objects.requireNonNull(repository, "repository");
    return this.submit(() -> this.opRepositoryAdd(uri, repository));
  }

  @Override
  public CompletableFuture<Void> repositoryRemove(
    final URI uri)
  {
    Objects.requireNonNull(uri, "uri");
    return this.submit(() -> this.opRepositoryRemove(uri));
  }

  @Override
  public CompletableFuture<Void> bundleSelect(
    final OBBundleIdentifier bundle)
  {
    Objects.requireNonNull(bundle, "bundle");
    return this.submit(() -> this.opBundleSelect(bundle));
  }

  @Override
  public CompletableFuture<Boolean> bundleIsSelected(
    final OBBundleIdentifier bundle)
  {
    Objects.requireNonNull(bundle, "bundle");
    return this.submit(() -> this.opBundleIsSelected(bundle));
  }

  @Override
  public CompletableFuture<List<Resource>> bundlesResolved()
  {
    return this.submit(() -> this.opBundlesResolved());
  }

  @Override
  public void close()
  {
    this.executor.shutdown();
  }

  private static final class RepositoryWithBundles
  {
    private final Repository repository;
    private final Map<OBBundleIdentifier, Resource> resources;

    private RepositoryWithBundles(
      final Repository in_repository,
      final Map<OBBundleIdentifier, Resource> in_resources)
    {
      this.repository = Objects.requireNonNull(in_repository, "repository");
      this.resources = Objects.requireNonNull(in_resources, "resources");
    }
  }
}
