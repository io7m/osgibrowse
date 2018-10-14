package com.io7m.osgibrowse.gui.javafx;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.osgibrowse.client.api.OBClientEventType;
import org.immutables.value.Value;

@ImmutablesStyleType
@Value.Immutable
public interface OBGuiEventClientType extends OBGuiEventType
{
  @Override
  default OBGuiEventKind kind()
  {
    return OBGuiEventKind.CLIENT;
  }

  @Override
  default boolean progress()
  {
    return false;
  }

  @Value.Parameter
  OBClientEventType event();

  @Override
  default String describe()
  {
    return this.event().describe();
  }
}
