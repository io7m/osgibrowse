package com.io7m.osgibrowse.client.api;

import java.util.concurrent.CompletableFuture;

public interface OBRepositoryLoaderType
{
  CompletableFuture<OBRepositoryInputType> load();
}
