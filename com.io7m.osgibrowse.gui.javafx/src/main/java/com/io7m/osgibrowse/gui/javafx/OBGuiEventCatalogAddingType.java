package com.io7m.osgibrowse.gui.javafx;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

@ImmutablesStyleType
@Value.Immutable
public interface OBGuiEventCatalogAddingType extends OBGuiEventType
{
  @Override
  default OBGuiEventKind kind()
  {
    return OBGuiEventKind.REPOSITORY_ADDING;
  }

  @Override
  default boolean progress()
  {
    return true;
  }

  @Value.Parameter
  String uri();

  @Override
  default String describe()
  {
    return new StringBuilder(32)
      .append("Adding catalog ")
      .append(this.uri())
      .append('…')
      .toString();
  }
}
