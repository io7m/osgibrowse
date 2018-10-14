package com.io7m.osgibrowse.gui.javafx;

public interface OBGuiEventType
{
  OBGuiEventKind kind();

  boolean progress();

  String describe();
}
