package com.io7m.osgibrowse.gui;

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;

public final class CatalogBrowserWindow extends JXFrame
{
  private static final Logger LOG = LoggerFactory.getLogger(CatalogBrowserWindow.class);

  private final JXTreeTable table;
  private final CatalogViewTreeModel tree_model;
  private final JScrollPane table_scroll;
  private final JSplitPane split_pane;
  private final JPanel bundle_info;

  private static JMenuBar createMenu(
    final ControllerType controller)
  {
    final JMenu menu_file = new JMenu("File");
    final JMenuItem menu_file_quit =
      new JMenuItem(new AbstractAction("Quit")
      {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          controller.quit();
        }
      });
    menu_file_quit.setMnemonic('Q');
    menu_file.add(menu_file_quit);

    final JMenu menu_edit = new JMenu("Edit");

    final JMenuBar menu_bar = new JMenuBar();
    menu_bar.add(menu_file);
    menu_bar.add(menu_edit);
    return menu_bar;
  }

  CatalogBrowserWindow(
    final ControllerType controller)
  {
    Objects.requireNonNull(controller, "controller");

    this.setPreferredSize(new Dimension(800, 600));

    this.tree_model = new CatalogViewTreeModel();
    this.table = new JXTreeTable(this.tree_model);
    this.table.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(final MouseEvent e)
      {
        final TreePath selected =
          CatalogBrowserWindow.this.table.getPathForLocation(e.getX(), e.getY());
        if (selected != null) {
          if (e.getClickCount() == 2) {
            final Object last = selected.getLastPathComponent();
            if (last instanceof CatalogBundleType) {
              CatalogBrowserWindow.this.tree_model.selectBundle((CatalogBundleType) last);
            }
          }
        }
      }
    });

    final HighlightPredicate highlight_predicate = (renderer, adapter) -> {
      final int model_row = adapter.convertRowIndexToModel(adapter.row);
      final Object v_name = adapter.getValueAt(model_row, 0);
      final Object v_vers = adapter.getValueAt(model_row, 1);

      if (v_name instanceof String && v_vers instanceof String) {
        return this.tree_model.isBundleSelected(
          CatalogBundleIdentifier.builder()
            .setName((String) v_name)
            .setVersion((String) v_vers)
            .build());
      }

      return false;
    };

    this.table.addHighlighter(new ColorHighlighter(highlight_predicate, Color.BLACK, Color.WHITE));

    this.table_scroll = new JScrollPane(this.table);
    this.bundle_info = new JPanel();

    controller.repositories()
      .thenAccept(repositories ->
                    SwingUtilities.invokeLater(
                      () -> this.tree_model.setRepositories(repositories)));

    this.setJMenuBar(createMenu(controller));
    this.split_pane = new JSplitPane(HORIZONTAL_SPLIT, this.table_scroll, this.bundle_info);
    this.split_pane.setResizeWeight(0.5);

    final Container content = this.getContentPane();
    content.setLayout(new BorderLayout());
    content.add(this.split_pane, BorderLayout.CENTER);
  }
}
