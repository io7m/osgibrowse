package com.io7m.osgibrowse.gui.javafx;

import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Objects;

public final class OBBrowserWindow
{
  private final Stage stage;
  private final MenuBar menu_bar;
  private final OBTableView table;
  private final OBStatusBar status_bar;

  private OBBrowserWindow(
    final Stage in_stage,
    final MenuBar in_menu_bar,
    final OBTableView in_table,
    final OBStatusBar in_status_bar)
  {
    this.stage =
      Objects.requireNonNull(in_stage, "stage");
    this.menu_bar =
      Objects.requireNonNull(in_menu_bar, "menu_bar");
    this.table =
      Objects.requireNonNull(in_table, "table");
    this.status_bar =
      Objects.requireNonNull(in_status_bar, "status_bar");
  }

  public static OBBrowserWindow create(
    final OBController controller,
    final Stage stage)
  {
    Objects.requireNonNull(controller, "controller");
    Objects.requireNonNull(stage, "stage");

    final BorderPane pane = new BorderPane();
    final Scene scene = new Scene(pane, 800.0, 600.0);

    final MenuBar menu_bar = createMenu(controller);
    final OBStatusBar status_bar = OBStatusBar.create(controller);
    final OBTableView table = OBTableView.create(controller);

    pane.setTop(menu_bar);
    pane.setCenter(table.view());
    pane.setBottom(status_bar.view());

    stage.setScene(scene);
    stage.show();
    return new OBBrowserWindow(stage, menu_bar, table, status_bar);
  }

  private static MenuBar createMenu(
    final OBController controller)
  {
    final Menu menu_file = menuFile(controller);
    final Menu menu_edit = menuEdit(controller);

    final MenuBar menu_bar = new MenuBar();
    menu_bar.getMenus().addAll(menu_file, menu_edit);
    return menu_bar;
  }

  private static Menu menuEdit(
    final OBController controller)
  {
    return new Menu("Edit");
  }

  private static Menu menuFile(
    final OBController controller)
  {
    final MenuItem quit = new MenuItem("Quit");
    quit.setAccelerator(KeyCharacterCombination.valueOf("Shortcut+q"));
    quit.setOnAction(event -> controller.quit());

    final Menu menu = new Menu("File");
    menu.getItems().add(quit);
    return menu;
  }

}
