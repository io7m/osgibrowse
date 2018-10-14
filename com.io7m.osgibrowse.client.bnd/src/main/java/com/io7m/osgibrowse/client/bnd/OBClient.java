package com.io7m.osgibrowse.client.bnd;

import aQute.bnd.build.model.EE;
import aQute.bnd.version.VersionRange;
import biz.aQute.resolve.BndResolver;
import biz.aQute.resolve.GenericResolveContext;
import com.io7m.jaffirm.core.Invariants;
import com.io7m.osgibrowse.client.api.OBBundleIdentifier;
import com.io7m.osgibrowse.client.api.OBClientEventBundleDeselected;
import com.io7m.osgibrowse.client.api.OBClientEventBundleSelected;
import com.io7m.osgibrowse.client.api.OBClientEventRepositoryAddFailed;
import com.io7m.osgibrowse.client.api.OBClientEventRepositoryAdded;
import com.io7m.osgibrowse.client.api.OBClientEventRepositoryRemoved;
import com.io7m.osgibrowse.client.api.OBClientEventType;
import com.io7m.osgibrowse.client.api.OBClientType;
import com.io7m.osgibrowse.client.api.OBExceptionBundleNotFound;
import com.io7m.osgibrowse.client.api.OBExceptionRepositoryFailed;
import com.io7m.osgibrowse.client.api.OBExceptionResolutionFailed;
import com.io7m.osgibrowse.client.api.OBRepositoryInputType;
import com.io7m.osgibrowse.client.api.OBRepositoryLoaderProviderType;
import com.io7m.osgibrowse.client.api.OBRepositoryLoaderType;
import com.io7m.osgibrowse.client.api.OBRepositoryType;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.vavr.Tuple;
import io.vavr.collection.SortedMap;
import io.vavr.collection.SortedSet;
import io.vavr.collection.Stream;
import io.vavr.collection.TreeMap;
import io.vavr.collection.TreeSet;
import io.vavr.collection.Vector;
import org.osgi.framework.Version;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;
import org.osgi.service.repository.IdentityExpression;
import org.osgi.service.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

final class OBClient implements OBClientType
{
  private static final Logger LOG = LoggerFactory.getLogger(OBClient.class);
  private final OBRepositoryLoaderProviderType loaders;
  private final BehaviorSubject<OBClientEventType> subject;
  private final AtomicBoolean closed;
  private volatile TreeMap<URI, RepositoryWithBundles> repositories;
  private volatile TreeSet<OBBundleIdentifier> selected;

  OBClient(
    final OBRepositoryLoaderProviderType loaders)
  {
    this.loaders = Objects.requireNonNull(loaders, "loaders");
    this.repositories = TreeMap.empty();
    this.selected = TreeSet.empty();
    this.subject = BehaviorSubject.create();
    this.closed = new AtomicBoolean(false);
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

  @SuppressWarnings("unchecked")
  private static <K, VBASE, VSUB extends VBASE> TreeMap<K, VBASE> castMap(
    final TreeMap<K, VSUB> m)
  {
    return (TreeMap<K, VBASE>) m;
  }

  @Override
  public Observable<OBClientEventType> events()
  {
    return this.subject;
  }

  @Override
  public void repositoryAdd(final String uri)
    throws OBExceptionRepositoryFailed
  {
    Objects.requireNonNull(uri, "uri");

    final URI t_uri;
    try {
      t_uri = new URI(uri);
    } catch (final URISyntaxException e) {
      this.subject.onNext(OBClientEventRepositoryAddFailed.of(URI.create("uri:unparseable"), e));
      throw new OBExceptionRepositoryFailed(e);
    }

    this.repositoryAdd(t_uri);
  }

  @Override
  public void repositoryAdd(final URI uri)
    throws OBExceptionRepositoryFailed
  {
    Objects.requireNonNull(uri, "uri");

    final OBRepositoryInputType loaded;
    try {
      final OBRepositoryLoaderType repos = this.loaders.forURI(uri);
      loaded = repos.load();
    } catch (final Exception e) {
      this.subject.onNext(OBClientEventRepositoryAddFailed.of(uri, e));
      throw new OBExceptionRepositoryFailed(e);
    }

    this.opRepositoryAdd(loaded.uri(), loaded.repository());
  }

  private void opRepositoryAdd(
    final URI uri,
    final Repository repository)
    throws OBExceptionRepositoryFailed
  {
    try {
      final IdentityExpression req =
        repository.newRequirementBuilder("osgi.identity")
          .addAttribute("type", "bundle")
          .buildExpression();

      final SortedMap<OBBundleIdentifier, Resource> bundles;
      try {
        bundles =
          Stream.ofAll(repository.findProviders(req).getValue())
            .toSortedMap(resource ->
                           Tuple.of(makeBundleIdentifierForResource(uri, resource), resource));
      } catch (final InvocationTargetException | InterruptedException e) {
        throw new OBExceptionRepositoryFailed(e);
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("repository has {} bundles", Integer.valueOf(bundles.size()));
      }

      final RepositoryWithBundles processed_repository =
        new RepositoryWithBundles(uri, repository, bundles);

      this.repositories = this.repositories.put(uri, processed_repository);
      this.subject.onNext(OBClientEventRepositoryAdded.of(uri));
    } catch (final Exception e) {
      this.subject.onNext(OBClientEventRepositoryAddFailed.of(uri, e));
      throw new OBExceptionRepositoryFailed(e);
    }
  }

  private void opBundleSelectToggle(
    final OBBundleIdentifier bundle)
    throws OBExceptionBundleNotFound
  {
    if (this.opBundleDeselect(bundle)) {
      this.subject.onNext(OBClientEventBundleDeselected.of(bundle));
      return;
    }

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

    this.selected = this.selected.add(bundle);
    this.subject.onNext(OBClientEventBundleSelected.of(bundle));
  }

  private boolean opBundleDeselect(
    final OBBundleIdentifier bundle)
  {
    if (this.selected.contains(bundle)) {
      this.selected = this.selected.remove(bundle);
      return true;
    }
    return false;
  }

  private boolean opBundleIsSelected(
    final OBBundleIdentifier bundle)
  {
    return this.selected.contains(bundle);
  }

  private void opRepositoryRemove(
    final URI uri)
  {
    if (this.repositories.containsKey(uri)) {
      this.repositories = this.repositories.remove(uri);
      this.subject.onNext(OBClientEventRepositoryRemoved.of(uri));
    }
  }

  private Vector<Resource> opBundlesResolved()
    throws OBExceptionResolutionFailed
  {
    try {
      final BndResolver resolver = new BndResolver(new OBResolverLogger(LOG));

      final GenericResolveContext context = new GenericResolveContext(new OBResolverLogger(LOG));
      context.addEE(EE.JavaSE_9_0);

      for (final RepositoryWithBundles repository : this.repositories.values()) {
        context.addRepository(repository.repository);
      }

      for (final OBBundleIdentifier identifier : this.selected) {
        final String version = identifier.version().toString();
        context.addRequireBundle(identifier.name(), new VersionRange(version, version));
      }

      context.done();

      return Vector.ofAll(resolver.resolve(context).keySet());
    } catch (final Exception e) {
      throw new OBExceptionResolutionFailed(e);
    }
  }

  @Override
  public void repositoryAdd(
    final URI uri,
    final Repository repository)
    throws OBExceptionRepositoryFailed
  {
    Objects.requireNonNull(uri, "uri");
    Objects.requireNonNull(repository, "repository");
    this.checkNotClosed();
    this.opRepositoryAdd(uri, repository);
  }

  @Override
  public void repositoryRemove(
    final URI uri)
  {
    Objects.requireNonNull(uri, "uri");
    this.checkNotClosed();
    this.opRepositoryRemove(uri);
  }

  @Override
  public SortedMap<URI, OBRepositoryType> repositoryList()
  {
    this.checkNotClosed();
    return castMap(this.repositories);
  }

  @Override
  public void bundleSelectToggle(
    final OBBundleIdentifier bundle)
    throws OBExceptionBundleNotFound
  {
    Objects.requireNonNull(bundle, "bundle");
    this.checkNotClosed();
    this.opBundleSelectToggle(bundle);
  }

  @Override
  public boolean bundleIsSelected(
    final OBBundleIdentifier bundle)
  {
    Objects.requireNonNull(bundle, "bundle");
    this.checkNotClosed();
    return this.opBundleIsSelected(bundle);
  }

  @Override
  public SortedSet<OBBundleIdentifier> bundlesSelected()
  {
    this.checkNotClosed();
    return this.selected;
  }

  @Override
  public Vector<Resource> bundlesResolved()
    throws OBExceptionResolutionFailed
  {
    this.checkNotClosed();
    return this.opBundlesResolved();
  }

  private void checkNotClosed()
  {
    if (this.closed.get()) {
      throw new IllegalStateException("Client has been closed");
    }
  }

  @Override
  public void close()
  {
    if (this.closed.compareAndSet(false, true)) {
      this.subject.onComplete();
    }
  }

  private static final class RepositoryWithBundles implements OBRepositoryType
  {
    private final Repository repository;
    private final SortedMap<OBBundleIdentifier, Resource> resources;
    private final URI uri;

    private RepositoryWithBundles(
      final URI in_uri,
      final Repository in_repository,
      final SortedMap<OBBundleIdentifier, Resource> in_resources)
    {
      this.uri = Objects.requireNonNull(in_uri, "uri");
      this.repository = Objects.requireNonNull(in_repository, "repository");
      this.resources = Objects.requireNonNull(in_resources, "resources");
    }

    @Override
    public URI uri()
    {
      return this.uri;
    }

    @Override
    public SortedMap<OBBundleIdentifier, Resource> bundles()
    {
      return this.resources;
    }
  }
}
