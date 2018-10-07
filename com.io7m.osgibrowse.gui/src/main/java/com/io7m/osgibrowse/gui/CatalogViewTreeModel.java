package com.io7m.osgibrowse.gui;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class CatalogViewTreeModel extends AbstractTreeTableModel
{
  private static final Logger LOG = LoggerFactory.getLogger(CatalogViewTreeModel.class);

  private final static String[] COLUMN_NAMES = {"Bundle", "Version"};
  private final List<CatalogRepositoryType> repositories;
  private final Set<CatalogBundleIdentifier> selected;

  CatalogViewTreeModel()
  {
    super(new Object());
    this.repositories = new ArrayList<>(32);
    this.selected = new HashSet<>(32);
  }

  void setRepositories(
    final List<CatalogRepositoryType> repositories)
  {
    Objects.requireNonNull(repositories, "repositories");
    this.repositories.clear();
    this.repositories.addAll(repositories);
    this.modelSupport.fireNewRoot();
  }

  void selectBundle(
    final CatalogBundleType bundle)
  {
    Objects.requireNonNull(bundle, "bundle");

    if (this.selected.contains(bundle.identifier())) {
      this.selected.remove(bundle.identifier());
    } else {
      this.selected.add(bundle.identifier());
    }
  }

  public boolean isBundleSelected(
    final CatalogBundleIdentifier identifier)
  {
    return this.selected.contains(identifier);
  }

  @Override
  public int getColumnCount()
  {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(
    final int column)
  {
    return COLUMN_NAMES[column];
  }

  @Override
  public boolean isLeaf(final Object node)
  {
    return node instanceof CatalogBundleType;
  }

  @Override
  public boolean isCellEditable(
    final Object node,
    final int column)
  {
    return false;
  }

  @Override
  public Object getValueAt(
    final Object node,
    final int column)
  {
    if (node instanceof CatalogRepositoryType) {
      final CatalogRepositoryType repository = (CatalogRepositoryType) node;
      switch (column) {
        case 0:
          return repository.name();
        case 1:
          return null;
        case 2:
          return null;
        default:
          throw new IllegalStateException();
      }
    }

    if (node instanceof CatalogBundleType) {
      final CatalogBundleType bundle = (CatalogBundleType) node;
      switch (column) {
        case 0:
          return bundle.identifier().name();
        case 1:
          return bundle.identifier().version();
        default:
          throw new IllegalStateException();
      }
    }

    return null;
  }

  @Override
  public Object getChild(
    final Object parent,
    final int index)
  {
    if (parent instanceof CatalogRepositoryType) {
      final CatalogRepositoryType repository = (CatalogRepositoryType) parent;
      return repository.bundles().get(index);
    }
    return this.repositories.get(index);
  }

  @Override
  public int getChildCount(final Object parent)
  {
    if (parent instanceof CatalogRepositoryType) {
      final CatalogRepositoryType repository = (CatalogRepositoryType) parent;
      return repository.bundles().size();
    }
    return this.repositories.size();
  }

  @Override
  public int getIndexOfChild(
    final Object parent,
    final Object child)
  {
    final CatalogRepositoryType repository = (CatalogRepositoryType) parent;
    final CatalogBundleType bundle = (CatalogBundleType) child;
    return repository.bundles().indexOf(bundle);
  }
}
