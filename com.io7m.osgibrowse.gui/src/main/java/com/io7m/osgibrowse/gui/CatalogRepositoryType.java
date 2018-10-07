package com.io7m.osgibrowse.gui;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.util.List;

@ImmutablesStyleType
@Value.Immutable
public interface CatalogRepositoryType
{
  String name();

  List<CatalogBundleType> bundles();
}
