package com.io7m.osgibrowse.gui;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ControllerType
{
  CompletableFuture<List<CatalogRepositoryType>> repositories();

  void quit();
}
