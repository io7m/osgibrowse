package com.io7m.osgibrowse.tests.client;

import com.io7m.osgibrowse.client.api.OBBundleIdentifier;
import com.io7m.osgibrowse.client.api.OBClientEventRepositoryAdded;
import com.io7m.osgibrowse.client.api.OBClientEventRepositoryRemoved;
import com.io7m.osgibrowse.client.api.OBClientEventType;
import com.io7m.osgibrowse.client.api.OBClientProviderType;
import com.io7m.osgibrowse.client.api.OBClientType;
import com.io7m.osgibrowse.client.api.OBExceptionBundleNotFound;
import com.io7m.osgibrowse.client.api.OBExceptionRepositoryFailed;
import com.io7m.osgibrowse.client.api.OBExceptionResolutionFailed;
import com.io7m.osgibrowse.client.api.OBRepositoryInputType;
import com.io7m.osgibrowse.client.bnd.OBXMLRepositoryLoaders;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.vavr.collection.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.Version;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;
import org.osgi.service.repository.IdentityExpression;
import org.osgi.service.repository.Repository;
import org.osgi.service.repository.RequirementBuilder;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.osgi.framework.Version.parseVersion;

public abstract class OBClientContract
{
  @BeforeAll
  public static void setupOnce()
  {
    URL.setURLStreamHandlerFactory(new ResourceURLHandlers());
  }

  private static OBRepositoryInputType xmlRepository(
    final String name)
    throws Exception
  {
    final URI uri = resourceURI(name);
    return new OBXMLRepositoryLoaders().forURI(uri).load();
  }

  private static URI resourceURI(
    final String name)
    throws URISyntaxException
  {
    return OBClientContract.class.getResource(name).toURI();
  }

  protected abstract OBClientProviderType clients();

  protected abstract Logger logger();

  private static final class LoggingEventReceiver implements Observer<OBClientEventType>
  {
    private final Logger logger;
    private final ArrayList<OBClientEventType> events;
    private final ArrayList<Throwable> errors;
    private boolean complete;

    LoggingEventReceiver(final Logger in_logger)
    {
      this.logger = Objects.requireNonNull(in_logger, "logger");
      this.events = new ArrayList<>(128);
      this.errors = new ArrayList<>(128);
    }

    @Override
    public void onSubscribe(final Disposable d)
    {

    }

    @Override
    public void onNext(final OBClientEventType event)
    {
      this.events.add(event);
    }

    @Override
    public void onError(final Throwable e)
    {
      this.errors.add(e);
    }

    @Override
    public void onComplete()
    {
      this.complete = true;
    }
  }

  @Test
  public final void testRepositoryAddOK()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    final LoggingEventReceiver events = new LoggingEventReceiver(this.logger());

    try (OBClientType client = clients.createEmptyClient()) {
      client.events().subscribe(events);

      final OBRepositoryInputType repos = xmlRepository("knoplerfish-6.1.0.xml");

      client.repositoryAdd(repos);

      Assertions.assertTrue(client.repositoryList().containsKey(repos.uri()));

      Assertions.assertEquals(1, events.events.size());
      Assertions.assertEquals(
        OBClientEventRepositoryAdded.of(resourceURI("knoplerfish-6.1.0.xml")),
        events.events.get(0));
    }

    Assertions.assertTrue(events.complete);
  }

  @Test
  public final void testRepositoryCrash()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    final LoggingEventReceiver events = new LoggingEventReceiver(this.logger());

    try (OBClientType client = clients.createEmptyClient()) {
      client.events().subscribe(events);

      final IdentityExpression req =
        Mockito.mock(IdentityExpression.class);

      final RequirementBuilder req_builder =
        Mockito.mock(RequirementBuilder.class);

      Mockito.when(req_builder.buildExpression())
        .thenReturn(req);
      Mockito.when(req_builder.addAttribute(Mockito.anyString(), Mockito.any()))
        .thenReturn(req_builder);

      final Repository osgi_repos =
        Mockito.mock(Repository.class);

      Mockito.when(osgi_repos.newRequirementBuilder(Mockito.anyString()))
        .thenReturn(req_builder);
      Mockito.when(osgi_repos.findProviders(Mockito.<IdentityExpression>any()))
        .thenReturn(crashingPromise());

      final OBRepositoryInputType repos =
        Mockito.mock(OBRepositoryInputType.class);

      Mockito.when(repos.uri())
        .thenReturn(URI.create("urn:example"));

      Mockito.when(repos.repository())
        .thenReturn(osgi_repos);

      Assertions.assertThrows(OBExceptionRepositoryFailed.class, () -> {
        client.repositoryAdd(repos);
      });
    }

    Assertions.assertTrue(events.complete);
  }

  private static Promise<Collection<Resource>> crashingPromise()
  {
    final Deferred<Collection<Resource>> deferred = new Deferred<>();
    deferred.fail(new IllegalStateException());
    return deferred.getPromise();
  }

  @Test
  public final void testRepositoryAddRemoveOK()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    final LoggingEventReceiver events = new LoggingEventReceiver(this.logger());

    try (OBClientType client = clients.createEmptyClient()) {
      client.events().subscribe(events);

      final OBRepositoryInputType repos = xmlRepository("knoplerfish-6.1.0.xml");

      client.repositoryAdd(repos);
      client.repositoryRemove(repos.uri());

      Assertions.assertEquals(2, events.events.size());
      Assertions.assertEquals(
        OBClientEventRepositoryAdded.of(resourceURI("knoplerfish-6.1.0.xml")),
        events.events.get(0));
      Assertions.assertEquals(
        OBClientEventRepositoryRemoved.of(resourceURI("knoplerfish-6.1.0.xml")),
        events.events.get(1));
    }

    Assertions.assertTrue(events.complete);
  }

  @Test
  public final void testRepositoryAddReferralOK()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    final LoggingEventReceiver events = new LoggingEventReceiver(this.logger());

    try (OBClientType client = clients.createEmptyClient()) {
      client.events().subscribe(events);

      final OBRepositoryInputType repos = xmlRepository("referral.xml");

      client.repositoryAdd(repos);

      Assertions.assertTrue(client.repositoryList().containsKey(repos.uri()));

      Assertions.assertEquals(1, events.events.size());
      Assertions.assertEquals(
        OBClientEventRepositoryAdded.of(resourceURI("referral.xml")),
        events.events.get(0));
    }

    Assertions.assertTrue(events.complete);
  }

  @Test
  public final void testBundleSelectOK()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    final LoggingEventReceiver events = new LoggingEventReceiver(this.logger());

    try (OBClientType client = clients.createEmptyClient()) {
      client.events().subscribe(events);

      final OBRepositoryInputType repos = xmlRepository("knoplerfish-6.1.0.xml");

      final OBBundleIdentifier bundle =
        OBBundleIdentifier.of("org.knopflerfish.bundle.desktop", parseVersion("6.0.0"));

      client.repositoryAdd(repos);
      client.bundleSelectToggle(bundle);

      Assertions.assertTrue(client.bundleIsSelected(bundle), "Bundle is selected");
      Assertions.assertTrue(client.bundlesSelected().contains(bundle), "Bundle is selected");
    }

    Assertions.assertTrue(events.complete);
  }

  @Test
  public final void testBundleSelectNotFound()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    final LoggingEventReceiver events = new LoggingEventReceiver(this.logger());

    try (OBClientType client = clients.createEmptyClient()) {
      client.events().subscribe(events);

      final OBBundleIdentifier bundle =
        OBBundleIdentifier.of("x.y.z", parseVersion("1.0.0"));

      Assertions.assertThrows(OBExceptionBundleNotFound.class, () -> {
        client.bundleSelectToggle(bundle);
      });
    }

    Assertions.assertTrue(events.complete);
  }

  @Test
  public final void testBundleSelectNotFoundAfterRemoval()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    final LoggingEventReceiver events = new LoggingEventReceiver(this.logger());

    try (OBClientType client = clients.createEmptyClient()) {
      client.events().subscribe(events);

      final OBBundleIdentifier bundle =
        OBBundleIdentifier.of("org.knopflerfish.bundle.desktop", parseVersion("6.0.0"));

      final OBRepositoryInputType repos = xmlRepository("knoplerfish-6.1.0.xml");

      client.repositoryAdd(repos);
      client.bundleSelectToggle(bundle);
      client.bundleSelectToggle(bundle);
      client.repositoryRemove(repos.uri());

      Assertions.assertThrows(OBExceptionBundleNotFound.class, () -> {
        client.bundleSelectToggle(bundle);
      });
    }

    Assertions.assertTrue(events.complete);
  }

  @Test
  public final void testBundleResolveEmptyOK()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    final LoggingEventReceiver events = new LoggingEventReceiver(this.logger());

    try (OBClientType client = clients.createEmptyClient()) {
      client.events().subscribe(events);

      final Vector<Resource> result = client.bundlesResolved();
      Assertions.assertEquals(1, result.size());
      Assertions.assertEquals(0, result.get(0).getCapabilities("osgi.identity").size());
    }

    Assertions.assertTrue(events.complete);
  }

  @Test
  public final void testBundleResolveSelected()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    final LoggingEventReceiver events = new LoggingEventReceiver(this.logger());

    try (OBClientType client = clients.createEmptyClient()) {
      client.events().subscribe(events);

      final OBBundleIdentifier bundle =
        OBBundleIdentifier.of(
          "org.knopflerfish.bundle.commons-logging",
          parseVersion("2.0.0.kf4-001"));

      final OBRepositoryInputType repos = xmlRepository("knoplerfish-6.1.0.xml");
      final OBRepositoryInputType felix = xmlRepository("felix.xml");

      client.repositoryAdd(repos);
      client.repositoryAdd(felix);
      client.bundleSelectToggle(bundle);

      final Vector<Resource> result = client.bundlesResolved();

      final List<Resource> bundles =
        result.filter(r -> r.getCapabilities("osgi.identity")
          .stream()
          .anyMatch(c -> capabilityIsBundle(c, bundle)))
          .collect(Collectors.toList());

      Assertions.assertEquals(1, bundles.size());
    }

    Assertions.assertTrue(events.complete);
  }

  @Test
  public final void testBundleResolveFailure()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    final LoggingEventReceiver events = new LoggingEventReceiver(this.logger());

    try (OBClientType client = clients.createEmptyClient()) {
      client.events().subscribe(events);

      final OBBundleIdentifier bundle =
        OBBundleIdentifier.of(
          "UNRESOLVABLE",
          parseVersion("1.0.0"));

      final OBRepositoryInputType repos = xmlRepository("knoplerfish-6.1.0.xml");
      final OBRepositoryInputType felix = xmlRepository("felix.xml");
      final OBRepositoryInputType unresolvable = xmlRepository("unresolvable.xml");

      client.repositoryAdd(repos);
      client.repositoryAdd(felix);
      client.repositoryAdd(unresolvable);
      client.bundleSelectToggle(bundle);

      Assertions.assertThrows(OBExceptionResolutionFailed.class, client::bundlesResolved);
    }

    Assertions.assertTrue(events.complete);
  }

  @Test
  public final void testCatalogAddOK()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    final LoggingEventReceiver events = new LoggingEventReceiver(this.logger());

    try (OBClientType client = clients.createEmptyClient()) {
      client.events().subscribe(events);

      final URI catalog = resourceURI("catalog-0.xml");

      client.catalogAdd(catalog);

      final URI repos_uri =
        URI.create("resource://com/io7m/osgibrowse/tests/client/knoplerfish-6.1.0.xml");

      Assertions.assertTrue(client.repositoryList().containsKey(repos_uri));
      Assertions.assertEquals(1, events.events.size());
      Assertions.assertEquals(
        OBClientEventRepositoryAdded.of(repos_uri),
        events.events.get(0));
    }

    Assertions.assertTrue(events.complete);
  }

  private static boolean capabilityIsBundle(
    final Capability capability,
    final OBBundleIdentifier bundle)
  {
    final Map<String, Object> attr = capability.getAttributes();
    final String identity = (String) attr.get("osgi.identity");
    final Version version = (Version) attr.get("version");
    return Objects.equals(bundle.name(), identity)
      && Objects.equals(bundle.version(), version);
  }
}
