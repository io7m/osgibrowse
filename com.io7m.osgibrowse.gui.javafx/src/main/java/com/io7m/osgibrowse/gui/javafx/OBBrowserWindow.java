package com.io7m.osgibrowse.gui.javafx;

import com.io7m.osgibrowse.client.api.OBBundleIdentifier;
import com.io7m.osgibrowse.client.api.OBClientEventType;
import com.io7m.osgibrowse.client.api.OBRepositoryType;
import io.vavr.Tuple2;
import io.vavr.collection.SortedMap;
import io.vavr.collection.SortedSet;
import io.vavr.collection.TreeMap;
import io.vavr.collection.TreeSet;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static javafx.application.Platform.runLater;

public final class OBBrowserWindow
{
  private static final Logger LOG = LoggerFactory.getLogger(OBBrowserWindow.class);

  private OBBrowserWindow()
  {

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
    final HBox status_bar = createStatusBar(controller);
    final TreeTableView<OBTableNodeType> tree_table = configureTableView(controller);

    pane.setTop(menu_bar);
    pane.setCenter(tree_table);
    pane.setBottom(status_bar);

    stage.setScene(scene);
    stage.show();

    controller.events().subscribe(event -> onGuiEvent(tree_table, controller, event));
    return new OBBrowserWindow();
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

  private static TreeTableView<OBTableNodeType> configureTableView(
    final OBController controller)
  {
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
    tree_table.setOnMousePressed(event -> onSelectedTableRow(tree_table, controller, event));
    tree_table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    tree_table.getSelectionModel().setCellSelectionEnabled(false);
    return tree_table;
  }

  @SuppressWarnings("unchecked")
  private static void onSelectedTableRow(
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
          controller.bundleSelect(bundle.identifier)
            .thenAccept(data -> runLater(() -> configureTableBundleSelections(tree_table, data)));
          break;
        }
      }
    }
  }

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

    return new ContextMenu(add_repos);
  }

  private static void onGuiEvent(
    final TreeTableView<OBTableNodeType> tree_table,
    final OBController controller,
    final OBGuiEventType event)
  {
    switch (event.kind()) {
      case INITIALIZED: {
        break;
      }

      case CLIENT: {
        final OBGuiEventClient event_c = (OBGuiEventClient) event;
        onClientEvent(tree_table, controller, event_c.event());
        break;
      }

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

  private static void configureTableBundleSelections(
    final TreeTableView<OBTableNodeType> tree_table,
    final SortedSet<OBBundleIdentifier> identifiers)
  {
    final TreeItem<OBTableNodeType> root_node = tree_table.getRoot();
    final OBTableNodeRoot root = (OBTableNodeRoot) root_node.getValue();
    root.selected = identifiers;
    tree_table.refresh();
  }

  private static HBox createStatusBar(
    final OBController controller)
  {
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
    return status_bar;
  }

  private static Menu menuEdit(
    final OBController controller)
  {
    final Menu menu = new Menu("Edit");
    return menu;
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

  private interface OBTableNodeType
  {
    enum OBTableNodeKind
    {
      ROOT,
      REPOSITORY,
      BUNDLE
    }

    OBTableNodeKind kind();

    String name();

    String version();

    String status();
  }

  private static final class OBTableNodeRoot implements OBTableNodeType
  {
    private final SortedMap<URI, OBRepositoryType> repositories;
    private SortedSet<OBBundleIdentifier> selected;

    OBTableNodeRoot(
      final SortedMap<URI, OBRepositoryType> in_repositories,
      final SortedSet<OBBundleIdentifier> in_selected)
    {
      this.repositories =
        Objects.requireNonNull(in_repositories, "repositories");
      this.selected =
        Objects.requireNonNull(in_selected, "selected");
    }

    @Override
    public OBTableNodeKind kind()
    {
      return OBTableNodeKind.ROOT;
    }

    @Override
    public String name()
    {
      return "";
    }

    @Override
    public String version()
    {
      return "";
    }

    @Override
    public String status()
    {
      return "";
    }
  }

  private static final class OBTableNodeRepository implements OBTableNodeType
  {
    private final OBRepositoryType repository;
    private final OBTableNodeRoot root;

    OBTableNodeRepository(
      final OBTableNodeRoot in_root,
      final OBRepositoryType in_repos)
    {
      this.root = Objects.requireNonNull(in_root, "root");
      this.repository = Objects.requireNonNull(in_repos, "repos");
    }

    @Override
    public OBTableNodeKind kind()
    {
      return OBTableNodeKind.REPOSITORY;
    }

    @Override
    public String name()
    {
      return this.repository.uri().toString();
    }

    @Override
    public String version()
    {
      return "";
    }

    @Override
    public String status()
    {
      return "";
    }
  }

  private static final class OBTableNodeBundle implements OBTableNodeType
  {
    private final OBBundleIdentifier identifier;
    private final OBTableNodeRoot root;

    OBTableNodeBundle(
      final OBTableNodeRoot in_root,
      final OBBundleIdentifier in_identifier)
    {
      this.root = Objects.requireNonNull(in_root, "root");
      this.identifier = Objects.requireNonNull(in_identifier, "identifier");
    }

    @Override
    public OBTableNodeKind kind()
    {
      return OBTableNodeKind.BUNDLE;
    }

    @Override
    public String name()
    {
      return this.identifier.name();
    }

    @Override
    public String version()
    {
      return this.identifier.version().toString();
    }

    @Override
    public String status()
    {
      return this.root.selected.contains(this.identifier) ? "Selected" : "";
    }
  }
}
