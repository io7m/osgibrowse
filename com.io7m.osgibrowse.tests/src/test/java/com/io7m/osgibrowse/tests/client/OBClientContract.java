package com.io7m.osgibrowse.tests.client;

import com.io7m.osgibrowse.client.api.OBBundleIdentifier;
import com.io7m.osgibrowse.client.api.OBClientEventRepositoryAdded;
import com.io7m.osgibrowse.client.api.OBClientEventRepositoryRemoved;
import com.io7m.osgibrowse.client.api.OBClientEventType;
import com.io7m.osgibrowse.client.api.OBClientProviderType;
import com.io7m.osgibrowse.client.api.OBClientType;
import com.io7m.osgibrowse.client.api.OBExceptionBundleNotFound;
import com.io7m.osgibrowse.client.api.OBRepositoryType;
import com.io7m.osgibrowse.client.bnd.OBXMLRepositories;
import io.reactivex.disposables.Disposable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.osgi.framework.Version.parseVersion;

public abstract class OBClientContract
{
  private static OBRepositoryType xmlRepository(
    final String name)
    throws Exception
  {
    return OBXMLRepositories.fromURI(resourceURI(name));
  }

  private static URI resourceURI(
    final String name)
    throws URISyntaxException
  {
    return OBClientContract.class.getResource(name).toURI();
  }

  protected abstract OBClientProviderType clients();

  protected abstract Logger logger();

  @Test
  public final void testRepositoryAddOK()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    try (OBClientType client = clients.createEmptyClient()) {
      final ArrayList<OBClientEventType> events = new ArrayList<>();
      final Disposable sub = client.events().subscribe(events::add);

      final OBRepositoryType repos = xmlRepository("knoplerfish-6.1.0.xml");

      client.repositoryAdd(repos)
        .get();

      Assertions.assertEquals(1, events.size());
      Assertions.assertEquals(
        OBClientEventRepositoryAdded.of(resourceURI("knoplerfish-6.1.0.xml")),
        events.get(0));
    }
  }

  @Test
  public final void testRepositoryAddRemoveOK()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    try (OBClientType client = clients.createEmptyClient()) {
      final ArrayList<OBClientEventType> events = new ArrayList<>();
      final Disposable sub = client.events().subscribe(events::add);

      final OBRepositoryType repos = xmlRepository("knoplerfish-6.1.0.xml");

      client.repositoryAdd(repos)
        .thenCompose(ignored -> client.repositoryRemove(repos.uri()))
        .get();

      Assertions.assertEquals(2, events.size());
      Assertions.assertEquals(
        OBClientEventRepositoryAdded.of(resourceURI("knoplerfish-6.1.0.xml")),
        events.get(0));
      Assertions.assertEquals(
        OBClientEventRepositoryRemoved.of(resourceURI("knoplerfish-6.1.0.xml")),
        events.get(1));
    }
  }

  @Test
  public final void testBundleSelectOK()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    try (OBClientType client = clients.createEmptyClient()) {
      final ArrayList<OBClientEventType> events = new ArrayList<>();
      final Disposable sub = client.events().subscribe(events::add);

      final OBRepositoryType repos = xmlRepository("knoplerfish-6.1.0.xml");

      final OBBundleIdentifier bundle =
        OBBundleIdentifier.of("org.knopflerfish.bundle.desktop", parseVersion("6.0.0"));

      final Boolean result =
        client.repositoryAdd(repos)
          .thenRun(() -> client.bundleSelect(bundle))
          .thenCompose(ignored -> client.bundleIsSelected(bundle))
          .get();

      Assertions.assertTrue(result.booleanValue(), "Bundle is selected");
    }
  }

  @Test
  public final void testBundleSelectNotFound()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    try (OBClientType client = clients.createEmptyClient()) {
      final ArrayList<OBClientEventType> events = new ArrayList<>();
      final Disposable sub = client.events().subscribe(events::add);
      final OBBundleIdentifier bundle =
        OBBundleIdentifier.of("x.y.z", parseVersion("1.0.0"));

      Assertions.assertThrows(OBExceptionBundleNotFound.class, () -> {
        try {
          client.bundleSelect(bundle).get();
        } catch (final ExecutionException e) {
          throw (OBExceptionBundleNotFound) e.getCause();
        }
      });
    }
  }

  @Test
  public final void testBundleSelectNotFoundAfterRemoval()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    try (OBClientType client = clients.createEmptyClient()) {
      final ArrayList<OBClientEventType> events = new ArrayList<>();
      final Disposable sub = client.events().subscribe(events::add);

      final OBBundleIdentifier bundle =
        OBBundleIdentifier.of("org.knopflerfish.bundle.desktop", parseVersion("6.0.0"));

      final OBRepositoryType repos = xmlRepository("knoplerfish-6.1.0.xml");

      Assertions.assertThrows(OBExceptionBundleNotFound.class, () -> {
        try {
          client.repositoryAdd(repos)
            .thenCompose(ignored -> client.bundleSelect(bundle))
            .thenCompose(ignored -> client.repositoryRemove(repos.uri()))
            .thenCompose(ignored -> client.bundleSelect(bundle))
            .get();
        } catch (final ExecutionException e) {
          throw (OBExceptionBundleNotFound) e.getCause();
        }
      });
    }
  }

  @Test
  public final void testBundleResolveEmptyOK()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    try (OBClientType client = clients.createEmptyClient()) {
      final ArrayList<OBClientEventType> events = new ArrayList<>();
      final Disposable sub = client.events().subscribe(events::add);

      final List<Resource> result =
        client.bundlesResolved()
          .get();

      Assertions.assertEquals(1, result.size());
      Assertions.assertEquals(0, result.get(0).getCapabilities("osgi.identity").size());
    }
  }

  @Test
  public final void testBundleResolveSelected()
    throws Exception
  {
    final OBClientProviderType clients = this.clients();
    try (OBClientType client = clients.createEmptyClient()) {
      final ArrayList<OBClientEventType> events = new ArrayList<>();
      final Disposable sub = client.events().subscribe(events::add);

      final OBBundleIdentifier bundle =
        OBBundleIdentifier.of("org.knopflerfish.bundle.commons-logging", parseVersion("2.0.0.kf4-001"));

      final OBRepositoryType repos = xmlRepository("knoplerfish-6.1.0.xml");

      final List<Resource> result =
        client.repositoryAdd(repos)
          .thenCompose(ignored -> client.bundleSelect(bundle))
          .thenCompose(ignored -> client.bundlesResolved())
          .get();

      for (final Resource resource : result) {
        for (final Capability capability : resource.getCapabilities("osgi.identity")) {
          this.logger().debug("capability: {}", capability);
        }
      }
    }
  }
}
