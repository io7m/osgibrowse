package com.io7m.osgibrowse.gui.javafx;

import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main
{
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  private Main()
  {

  }

  public static void main(
    final String[] args)
  {
    LOG.debug("main start");
    Application.launch(OBApplication.class);
  }
}
