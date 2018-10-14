package com.io7m.osgibrowse.gui.javafx;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.util.Objects;

@ImmutablesStyleType
@Value.Immutable
public interface OBGuiEventInitializedType extends OBGuiEventType
{
  @Override
  default OBGuiEventKind kind()
  {
    return OBGuiEventKind.INITIALIZED;
  }

  @Override
  default boolean progress()
  {
    return false;
  }

  @Override
  default String describe()
  {
    final Package pack = OBGuiEventInitializedType.class.getPackage();
    final String version = pack.getImplementationVersion();
    final String name = pack.getImplementationTitle();
    return new StringBuilder(32)
      .append(Objects.requireNonNullElse(name, "OBBrowser"))
      .append(' ')
      .append(Objects.requireNonNullElse(version, "0.0.0"))
      .append(" initialized")
      .toString();
  }
}
