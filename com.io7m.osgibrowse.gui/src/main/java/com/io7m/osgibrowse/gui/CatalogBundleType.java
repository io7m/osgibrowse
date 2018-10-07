package com.io7m.osgibrowse.gui;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

@ImmutablesStyleType
@Value.Immutable
public interface CatalogBundleType
{
  CatalogBundleIdentifier identifier();
}
