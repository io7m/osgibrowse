package com.io7m.osgibrowse.gui.javafx;

import com.io7m.osgibrowse.client.api.OBBundleIdentifier;
import com.io7m.osgibrowse.client.api.OBClientType;
import com.io7m.osgibrowse.client.api.OBExceptionBundleNotFound;
import com.io7m.osgibrowse.client.api.OBExceptionCatalogFailed;
import com.io7m.osgibrowse.client.api.OBRepositoryType;
import com.io7m.osgibrowse.client.bnd.OBClients;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.SortedMap;
import io.vavr.collection.SortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class OBController
{
  private static final Logger LOG = LoggerFactory.getLogger(OBController.class);

  private final ExecutorService exec;
  private final BehaviorSubject<OBGuiEventType> events;
  private final OBClients clients;
  private final OBClientType client;

  OBController()
  {
    this.clients = new OBClients();
    this.client = this.clients.createEmptyClient();

    this.events = BehaviorSubject.create();
    this.client.events().subscribe(event -> this.events.onNext(OBGuiEventClient.of(event)));

    this.exec = Executors.newFixedThreadPool(4, runnable -> {
      final Thread thread = new Thread(runnable);
      thread.setName(
        new StringBuilder(64)
          .append(OBController.class.getCanonicalName())
          .append('.')
          .append(thread.getId())
          .toString());
      return thread;
    });

    this.events.onNext(OBGuiEventInitialized.builder().build());
  }

  private OBRepositoryType opRepositoryAdd(
    final String uri_text)
    throws Exception
  {
    this.events.onNext(OBGuiEventRepositoryAdding.of(uri_text));
    this.client.repositoryAdd(uri_text);
    final URI uri = URI.create(uri_text);
    return this.client.repositoryList().get(uri).get();
  }

  private Void opCatalogAdd(
    final String uri_text)
    throws Exception
  {
    this.events.onNext(OBGuiEventRepositoryAdding.of(uri_text));
    this.client.catalogAdd(uri_text);
    final URI uri = URI.create(uri_text);
    return null;
  }

  public CompletableFuture<Tuple2<
    SortedSet<OBBundleIdentifier>,
    SortedMap<URI, OBRepositoryType>>> repositoryList()
  {
    return this.submit(() -> {
      final SortedMap<URI, OBRepositoryType> repositories = this.client.repositoryList();
      final SortedSet<OBBundleIdentifier> selected = this.client.bundlesSelected();
      return Tuple.of(selected, repositories);
    });
  }

  private <T> CompletableFuture<T> submit(
    final Callable<T> callable)
  {
    final CompletableFuture<T> future = new CompletableFuture<T>();

    this.exec.execute(() -> {
      try {
        future.complete(callable.call());
      } catch (final Throwable ex) {
        LOG.error("error: ", ex);
        future.completeExceptionally(ex);
      }
    });

    return future;
  }

  public Observable<OBGuiEventType> events()
  {
    return this.events;
  }

  public void quit()
  {
    LOG.debug("quit");
    this.exec.shutdown();
    this.events.onComplete();
    System.exit(0);
  }

  public CompletableFuture<OBRepositoryType> repositoryAdd(
    final String uri)
  {
    return this.submit(() -> this.opRepositoryAdd(uri));
  }

  public CompletableFuture<Void> catalogAdd(
    final String uri)
  {
    return this.submit(() -> this.opCatalogAdd(uri));
  }

  public CompletableFuture<SortedSet<OBBundleIdentifier>> bundleSelect(
    final OBBundleIdentifier identifier)
  {
    return this.submit(() -> this.opBundleSelect(identifier));
  }

  private SortedSet<OBBundleIdentifier> opBundleSelect(
    final OBBundleIdentifier identifier)
    throws OBExceptionBundleNotFound
  {
    this.client.bundleSelectToggle(identifier);
    return this.client.bundlesSelected();
  }
}
