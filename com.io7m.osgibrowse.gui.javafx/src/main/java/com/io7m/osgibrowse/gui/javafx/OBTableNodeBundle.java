package com.io7m.osgibrowse.gui.javafx;

import com.io7m.osgibrowse.client.api.OBBundleIdentifier;

import java.util.Objects;

final class OBTableNodeBundle implements OBTableNodeType
{
  private final OBBundleIdentifier identifier;
  private final OBTableNodeRoot root;

  OBTableNodeBundle(
    final OBTableNodeRoot in_root,
    final OBBundleIdentifier in_identifier)
  {
    this.root = Objects.requireNonNull(in_root, "root");
    this.identifier = Objects.requireNonNull(in_identifier, "identifier");
  }

  public OBBundleIdentifier identifier()
  {
    return this.identifier;
  }

  @Override
  public OBTableNodeKind kind()
  {
    return OBTableNodeKind.BUNDLE;
  }

  @Override
  public String name()
  {
    return this.identifier.name();
  }

  @Override
  public String version()
  {
    return this.identifier.version().toString();
  }

  @Override
  public String status()
  {
    return this.root.selected().contains(this.identifier) ? "Selected" : "";
  }
}
