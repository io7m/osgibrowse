package com.io7m.osgibrowse.gui.javafx;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

final class OBImages
{
  private OBImages()
  {

  }

  static Image imageOf(
    final String name)
  {
    return new Image(OBBrowserWindow.class.getResource(name).toString());
  }

  static ImageView imageViewOf(
    final String name)
  {
    return new ImageView(imageOf(name));
  }
}
