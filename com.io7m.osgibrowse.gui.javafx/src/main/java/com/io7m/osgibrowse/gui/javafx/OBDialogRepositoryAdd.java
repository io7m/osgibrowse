package com.io7m.osgibrowse.gui.javafx;

import javafx.scene.control.TextInputDialog;

import java.util.Objects;

public final class OBDialogRepositoryAdd
{
  private final TextInputDialog dialog;

  private OBDialogRepositoryAdd(
    final TextInputDialog in_dialog)
  {
    this.dialog = Objects.requireNonNull(in_dialog, "dialog");
  }

  public static OBDialogRepositoryAdd create()
  {
    final TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Add Repository");
    dialog.setHeaderText("Add Repository");
    dialog.setGraphic(OBImages.imageViewOf("repository.png"));
    dialog.setContentText("Repository URI");
    return new OBDialogRepositoryAdd(dialog);
  }

  public TextInputDialog dialog()
  {
    return this.dialog;
  }
}
