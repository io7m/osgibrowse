package com.io7m.osgibrowse.gui;

import aQute.lib.promise.PromiseCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class Controller implements ControllerType
{
  private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

  Controller()
  {

  }

  @Override
  public CompletableFuture<List<CatalogRepositoryType>> repositories()
  {
    return CompletableFuture.completedFuture(List.of(
      CatalogRepository.builder()
        .setName("com.io7m/main")
        .addBundles(
          CatalogBundle.builder()
            .setIdentifier(
              CatalogBundleIdentifier.builder()
                .setName("com.io7m.junreachable.core")
                .setVersion("1.0.0")
                .build())
            .build())
        .addBundles(
          CatalogBundle.builder()
            .setIdentifier(
              CatalogBundleIdentifier.builder()
                .setName("com.io7m.junreachable.core")
                .setVersion("1.1.0")
                .build())
            .build())
        .addBundles(
          CatalogBundle.builder()
            .setIdentifier(
              CatalogBundleIdentifier.builder()
                .setName("com.io7m.junreachable.core")
                .setVersion("2.0.0")
                .build())
            .build())
        .build(),
      CatalogRepository.builder()
        .setName("com.io7m/aux")
        .build(),
      CatalogRepository.builder()
        .setName("com.io7m/red")
        .build()
    ));
  }

  @Override
  public void quit()
  {
    LOG.info("quit");
  }
}
