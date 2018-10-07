package com.io7m.osgibrowse.gui;

import org.jdesktop.swingx.SwingXUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.WindowConstants;

public final class Main
{
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  private Main()
  {

  }

  public static void main(
    final String[] args)
  {
    final Controller controller = new Controller();

    SwingXUtilities.invokeLater(() -> {
      try {
        final CatalogBrowserWindow frame = new CatalogBrowserWindow(controller);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        return null;
      } catch (final Exception e) {
        LOG.error("ui error: ", e);
        return null;
      }
    });
  }
}
