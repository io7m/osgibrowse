package com.io7m.osgibrowse.gui.javafx;

interface OBTableNodeType
{
  OBTableNodeKind kind();

  String name();

  String version();

  String status();

  enum OBTableNodeKind
  {
    ROOT,
    REPOSITORY,
    BUNDLE
  }
}
