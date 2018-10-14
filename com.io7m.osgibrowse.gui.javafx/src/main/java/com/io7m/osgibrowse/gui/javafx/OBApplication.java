package com.io7m.osgibrowse.gui.javafx;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OBApplication extends Application
{
  private static final Logger LOG = LoggerFactory.getLogger(OBApplication.class);
  private OBBrowserWindow window;
  private OBController controller;

  public OBApplication()
  {

  }

  @Override
  public void init()
  {
    this.controller = new OBController();
  }

  @Override
  public void stop()
  {

  }

  @Override
  public void start(final Stage stage)
  {
    LOG.debug("start");

    this.window = OBBrowserWindow.create(this.controller, stage);
  }

}
