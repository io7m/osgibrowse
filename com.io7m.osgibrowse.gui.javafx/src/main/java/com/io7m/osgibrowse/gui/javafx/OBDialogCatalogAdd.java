package com.io7m.osgibrowse.gui.javafx;

import javafx.scene.control.TextInputDialog;

import java.util.Objects;

public final class OBDialogCatalogAdd
{
  private final TextInputDialog dialog;

  private OBDialogCatalogAdd(
    final TextInputDialog in_dialog)
  {
    this.dialog = Objects.requireNonNull(in_dialog, "dialog");
  }

  public static OBDialogCatalogAdd create()
  {
    final TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Add Catalog");
    dialog.setHeaderText("Add Catalog");
    dialog.setGraphic(OBImages.imageViewOf("root.png"));
    dialog.setContentText("Catalog URI");
    return new OBDialogCatalogAdd(dialog);
  }

  public TextInputDialog dialog()
  {
    return this.dialog;
  }
}
