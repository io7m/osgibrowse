package com.io7m.osgibrowse.gui.javafx;

import com.io7m.osgibrowse.client.api.OBRepositoryType;

import java.util.Objects;

final class OBTableNodeRepository implements OBTableNodeType
{
  private final OBRepositoryType repository;
  private final OBTableNodeRoot root;

  OBTableNodeRepository(
    final OBTableNodeRoot in_root,
    final OBRepositoryType in_repos)
  {
    this.root = Objects.requireNonNull(in_root, "root");
    this.repository = Objects.requireNonNull(in_repos, "repos");
  }

  @Override
  public OBTableNodeKind kind()
  {
    return OBTableNodeKind.REPOSITORY;
  }

  @Override
  public String name()
  {
    return this.repository.uri().toString();
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
