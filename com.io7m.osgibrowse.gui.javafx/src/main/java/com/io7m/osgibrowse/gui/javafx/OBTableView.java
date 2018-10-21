package com.io7m.osgibrowse.gui.javafx;

import com.io7m.osgibrowse.client.api.OBBundleIdentifier;
import com.io7m.osgibrowse.client.api.OBClientEventType;
import com.io7m.osgibrowse.client.api.OBRepositoryType;
import io.vavr.Tuple2;
import io.vavr.collection.SortedMap;
import io.vavr.collection.SortedSet;
import io.vavr.collection.TreeMap;
import io.vavr.collection.TreeSet;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static javafx.application.Platform.runLater;

public final class OBTableView
{
  private final TreeTableView<OBTableNodeType> tree_table;

  private OBTableView(
    final TreeTableView<OBTableNodeType> tree_table)
  {
    this.tree_table = Objects.requireNonNull(tree_table, "tree_table");
  }

  /**
   * Create a context menu for the table.
   */

  private static ContextMenu treeTableMenu(
    final OBController controller)
  {
    final MenuItem add_repos =
      new MenuItem("Add repository…", OBImages.imageViewOf("repository.png"));

    add_repos.setOnAction(event -> {
      final OBDialogRepositoryAdd dialog = OBDialogRepositoryAdd.create();
      final Optional<String> result = dialog.dialog().showAndWait();
      result.ifPresent(controller::repositoryAdd);
    });

    final MenuItem add_catalog =
      new MenuItem("Add catalog…", OBImages.imageViewOf("root.png"));

    add_catalog.setOnAction(event -> {
      final OBDialogCatalogAdd dialog = OBDialogCatalogAdd.create();
      final Optional<String> result = dialog.dialog().showAndWait();
      result.ifPresent(controller::catalogAdd);
    });

    return new ContextMenu(add_repos, add_catalog);
  }

  /**
   * A bundle has been selected or deselected. Reconfigure the table view.
   */

  private static void configureTableBundleSelections(
    final TreeTableView<OBTableNodeType> tree_table,
    final SortedSet<OBBundleIdentifier> identifiers)
  {
    final TreeItem<OBTableNodeType> root_node = tree_table.getRoot();
    final OBTableNodeRoot root = (OBTableNodeRoot) root_node.getValue();
    root.setSelected(identifiers);
    tree_table.refresh();
  }

  /**
   * The set of repositories has been updated in some way. Repopulate the table view with the new
   * data.
   */

  private static void configureTableData(
    final TreeTableView<OBTableNodeType> tree_table,
    final Tuple2<SortedSet<OBBundleIdentifier>, SortedMap<URI, OBRepositoryType>> data)
  {
    final SortedMap<URI, OBRepositoryType> repositories = data._2();
    final SortedSet<OBBundleIdentifier> selected = data._1;

    final OBTableNodeRoot root = new OBTableNodeRoot(repositories, selected);
    final TreeItem<OBTableNodeType> tree_root = new TreeItem<>(root);
    final ObservableList<TreeItem<OBTableNodeType>> nodes = tree_root.getChildren();

    for (final URI uri : repositories.keySet()) {
      final OBRepositoryType repository = repositories.get(uri).get();
      final OBTableNodeRepository node_repos = new OBTableNodeRepository(root, repository);
      final TreeItem<OBTableNodeType> node_repos_item = new TreeItem<>(node_repos);
      node_repos_item.setGraphic(OBImages.imageViewOf("repository.png"));
      node_repos_item.setExpanded(true);
      nodes.add(node_repos_item);

      for (final OBBundleIdentifier identifier : repository.bundles().keySet()) {
        final OBTableNodeBundle node_bundle = new OBTableNodeBundle(root, identifier);
        final TreeItem<OBTableNodeType> node_bundle_item = new TreeItem<>(node_bundle);
        node_bundle_item.setGraphic(OBImages.imageViewOf("bundle.png"));
        node_repos_item.getChildren().add(node_bundle_item);
      }
    }

    tree_table.setRoot(tree_root);
  }

  @SuppressWarnings("unchecked")
  private static void onClickedTable(
    final TreeTableView<OBTableNodeType> tree_table,
    final OBController controller,
    final MouseEvent event)
  {
    if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
      final Node node = ((Node) event.getTarget()).getParent();
      final TreeTableRow<OBTableNodeType> row;
      if (node instanceof TreeTableRow) {
        row = (TreeTableRow<OBTableNodeType>) node;
      } else {
        row = (TreeTableRow<OBTableNodeType>) node.getParent();
      }

      final OBTableNodeType item = row.getItem();
      switch (item.kind()) {
        case ROOT: {
          break;
        }
        case REPOSITORY: {
          break;
        }
        case BUNDLE: {
          final OBTableNodeBundle bundle = (OBTableNodeBundle) item;
          controller
            .bundleSelect(bundle.identifier())
            .thenAccept(data -> runLater(() -> configureTableBundleSelections(tree_table, data)));
          break;
        }
      }
    }
  }

  private static void onGuiEvent(
    final TreeTableView<OBTableNodeType> tree_table,
    final OBController controller,
    final OBGuiEventType event)
  {
    switch (event.kind()) {
      case CLIENT: {
        final OBGuiEventClient event_c = (OBGuiEventClient) event;
        onClientEvent(tree_table, controller, event_c.event());
        break;
      }

      case INITIALIZED:
      case REPOSITORY_ADDING: {
        break;
      }
    }
  }

  private static void onClientEvent(
    final TreeTableView<OBTableNodeType> tree_table,
    final OBController controller,
    final OBClientEventType event)
  {
    switch (event.kind()) {
      case REPOSITORY_ADDED:
      case REPOSITORY_REMOVED: {
        controller.repositoryList()
          .thenAccept(data -> runLater(() -> configureTableData(tree_table, data)));
        break;
      }
      case REPOSITORY_ADD_FAILED:
      case BUNDLE_SELECTED:
      case BUNDLE_DESELECTED: {
        break;
      }
    }
  }

  public TreeTableView<OBTableNodeType> view()
  {
    return this.tree_table;
  }

  public static OBTableView create(
    final OBController controller)
  {
    Objects.requireNonNull(controller, "controller");

    final TreeItem<OBTableNodeType> tree_root =
      new TreeItem<>(new OBTableNodeRoot(TreeMap.empty(), TreeSet.empty()));
    tree_root.setExpanded(true);

    final TreeTableColumn<OBTableNodeType, String> column_names =
      new TreeTableColumn<>("Name");

    column_names.setPrefWidth(500.0);
    column_names.setCellValueFactory(
      param -> new ReadOnlyStringWrapper(param.getValue().getValue().name()));

    final TreeTableColumn<OBTableNodeType, String> column_versions =
      new TreeTableColumn<>("Version");

    column_versions.setPrefWidth(300.0);
    column_versions.setCellValueFactory(
      param -> new ReadOnlyStringWrapper(param.getValue().getValue().version()));

    final TreeTableColumn<OBTableNodeType, String> column_status =
      new TreeTableColumn<>("Status");

    column_status.setPrefWidth(100.0);
    column_status.setCellValueFactory(
      param -> new ReadOnlyStringWrapper(param.getValue().getValue().status()));

    final TreeTableView<OBTableNodeType> tree_table = new TreeTableView<>(tree_root);
    final ObservableList<TreeTableColumn<OBTableNodeType, ?>> columns = tree_table.getColumns();
    columns.add(column_names);
    columns.add(column_versions);
    columns.add(column_status);

    tree_table.setShowRoot(false);
    tree_table.setContextMenu(treeTableMenu(controller));
    tree_table.setEditable(false);
    tree_table.setOnMousePressed(event -> onClickedTable(tree_table, controller, event));
    tree_table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    tree_table.getSelectionModel().setCellSelectionEnabled(false);

    /*
     * Subscribe to application events so that the table can be updated every time
     * a repository is added/removed.
     */

    controller.events().subscribe(event -> onGuiEvent(tree_table, controller, event));
    return new OBTableView(tree_table);
  }
}
