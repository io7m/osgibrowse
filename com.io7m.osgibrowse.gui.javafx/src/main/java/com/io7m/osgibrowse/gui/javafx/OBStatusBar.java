package com.io7m.osgibrowse.gui.javafx;

import javafx.geometry.Insets;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.Objects;

final class OBStatusBar
{
  private final HBox status_bar;

  private OBStatusBar(
    final HBox in_bar)
  {
    this.status_bar = Objects.requireNonNull(in_bar, "status_bar");
  }

  public static OBStatusBar create(
    final OBController controller)
  {
    Objects.requireNonNull(controller, "controller");

    final ProgressIndicator progress = new ProgressIndicator();
    progress.setPrefSize(16.0, 16.0);
    progress.setVisible(false);

    final Text text = new Text("");
    controller.events().subscribe(event -> {
      text.setText(event.describe());
      progress.setVisible(event.progress());
    });

    final HBox status_bar = new HBox();
    status_bar.getChildren().add(text);
    status_bar.getChildren().add(progress);
    status_bar.setPadding(new Insets(8.0, 8.0, 8.0, 8.0));
    status_bar.setSpacing(8.0);
    return new OBStatusBar(status_bar);
  }

  public HBox view()
  {
    return this.status_bar;
  }
}
