package com.io7m.osgibrowse.gui.javafx;

import com.io7m.osgibrowse.client.api.OBBundleIdentifier;
import com.io7m.osgibrowse.client.api.OBRepositoryType;
import io.vavr.collection.SortedMap;
import io.vavr.collection.SortedSet;

import java.net.URI;
import java.util.Objects;

final class OBTableNodeRoot implements OBTableNodeType
{
  private final SortedMap<URI, OBRepositoryType> repositories;
  private SortedSet<OBBundleIdentifier> selected;

  OBTableNodeRoot(
    final SortedMap<URI, OBRepositoryType> in_repositories,
    final SortedSet<OBBundleIdentifier> in_selected)
  {
    this.repositories =
      Objects.requireNonNull(in_repositories, "repositories");
    this.selected =
      Objects.requireNonNull(in_selected, "selected");
  }

  public SortedSet<OBBundleIdentifier> selected()
  {
    return this.selected;
  }

  public void setSelected(
    final SortedSet<OBBundleIdentifier> values)
  {
    this.selected = Objects.requireNonNull(values, "values");
  }

  @Override
  public OBTableNodeKind kind()
  {
    return OBTableNodeKind.ROOT;
  }

  @Override
  public String name()
  {
    return "";
  }

  @Override
  public String version()
  {
    return "";
  }

  @Override
  public String status()
  {
    return "";
  }
}
