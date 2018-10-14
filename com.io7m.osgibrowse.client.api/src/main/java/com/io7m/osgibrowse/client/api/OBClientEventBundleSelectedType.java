package com.io7m.osgibrowse.client.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.net.URI;

@Value.Immutable
@ImmutablesStyleType
public interface OBClientEventBundleSelectedType extends OBClientEventType
{
  @Override
  default OBClientEventKind kind()
  {
    return OBClientEventKind.BUNDLE_SELECTED;
  }

  @Value.Parameter
  OBBundleIdentifier bundle();

  @Override
  default String describe()
  {
    return "Selected bundle " + this.bundle().toSymbolicString();
  }
}
