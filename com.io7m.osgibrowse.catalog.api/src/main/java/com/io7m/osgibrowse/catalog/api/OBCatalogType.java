package com.io7m.osgibrowse.catalog.api;

import io.vavr.collection.SortedSet;

import java.net.URI;

public interface OBCatalogType
{
  URI uri();

  SortedSet<OBCatalogRepositoryLink> repositories();
}
