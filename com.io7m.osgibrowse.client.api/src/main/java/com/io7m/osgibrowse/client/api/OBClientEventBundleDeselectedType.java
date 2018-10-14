package com.io7m.osgibrowse.client.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

@Value.Immutable
@ImmutablesStyleType
public interface OBClientEventBundleDeselectedType extends OBClientEventType
{
  @Override
  default OBClientEventKind kind()
  {
    return OBClientEventKind.BUNDLE_DESELECTED;
  }

  @Value.Parameter
  OBBundleIdentifier bundle();

  @Override
  default String describe()
  {
    return "Deselected bundle " + this.bundle().toSymbolicString();
  }
}
