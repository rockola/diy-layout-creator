/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2020 held jointly by the individual authors.

  This file is part of DIYLC.

  DIYLC is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  DIYLC is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
  License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC. If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc.swing.tree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.App;
import org.diylc.common.ComponentType;
import org.diylc.core.Template;
import org.diylc.images.Icon;
import org.diylc.swing.tree.FilteredTreeModel;
import org.diylc.swing.tree.ComponentNode;

public class ComponentTreePanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(ComponentTreePanel.class);

  public static final String COMPONENT_SHORTCUT_KEY = "componentShortcuts";

  private JTextField searchField = new JTextField();
  private JTree tree;

  public ComponentTreePanel() {
    super();
    setLayout(new GridBagLayout());

    tree = new JTree();
    tree.setRootVisible(false);
    final ComponentNode root = new ComponentNode(App.getString("components.title"));
    FilteredTreeModel model = new FilteredTreeModel(root);
    tree.setModel(model);
    model.addNode(root, new ComponentNode(App.getString("components.favorites")));
    model.addNode(root, new ComponentNode(App.getString("components.recently-used")));
    model.addNode(root, new ComponentNode(App.getString("components.building-blocks")));
    Map<String, List<ComponentType>> componentTypes = ComponentType.getComponentTypes();
    for (String category : ComponentType.getCategories(true)) {
      ComponentNode categoryNode = new ComponentNode(category);
      model.addNode(root, categoryNode);
      for (ComponentType type : componentTypes.get(category)) {
        ComponentNode typeNode = new ComponentNode(type);
        model.addNode(categoryNode, typeNode);
        for (Template template : type.getVariants()) {
          model.addNode(typeNode, new ComponentNode(template));
        }
      }
    }
    tree.setCellRenderer(new DefaultTreeCellRenderer() {
        public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
          super.getTreeCellRendererComponent(tree, value, sel, expanded,
                                             leaf, row, hasFocus);
          ComponentNode item = (ComponentNode) value;
          ComponentType type = item.getComponentType();
          boolean visible = item.isVisible();
          if (visible) {
            if (item.isCategory()) {
              setPreferredSize(new Dimension(250, 20));
              setOpenIcon(Icon.FolderOpen.icon());
              setClosedIcon(Icon.Folder.icon());
            } else if (type != null) {
              setPreferredSize(new Dimension(250, 32));
              // TODO: modify icon if this item is a variant (Template)
              // perhaps add a "V" in lower right corner?
              // currently shows parent ComponentType icon as is
              setIcon(type.getIcon());
            }
          } else {
            setPreferredSize(new Dimension(0, 0));
          }
          return this;
        }
      });
    tree.addTreeSelectionListener(e -> {
        if (e.isAddedPath()) {
          ComponentNode node = (ComponentNode) e.getPath().getLastPathComponent();
          final ComponentType type = node.getComponentType();
          if (type != null) {
            App.ui().getPresenter().setNewComponentTypeSlot(type, node.getVariant(), false);
          }
        }
      });

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weighty = 0;
    constraints.gridwidth = 1;
    add(getSearchField(), constraints);

    final ComponentTreePanel treePanel = this;
    getSearchField().getDocument().addDocumentListener(new DocumentListener() {

        public void changedUpdate(DocumentEvent e) {
          process(e);
        }

        public void removeUpdate(DocumentEvent e) {
          process(e);
        }

        public void insertUpdate(DocumentEvent e) {
          process(e);
        }

        private void show(ComponentNode node) {
          showNode(node, true);
          for (Enumeration e = node.children(); e.hasMoreElements();) {
            ComponentNode n = (ComponentNode) e.nextElement();
            show(n);
          }
        }

        private boolean showNode(ComponentNode node, boolean show) {
          boolean wasShown = node.setVisible(show);
          LOG.trace("showNode({}, {}) wasShown {}",
                    node, show, wasShown);
          if (show != wasShown) {
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            model.nodeChanged(node);
          }
          return show;
        }

        private boolean visitAllNodes(
            JTree tree,
            ComponentNode node,
            String searchString) {
          boolean childMatched = false;
          Object u = node.getUserObject();
          String s = u.toString();
          boolean match = u.toString().toLowerCase().contains(searchString);
          if (node.getChildCount() > 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
              ComponentNode n = (ComponentNode) e.nextElement();
              if (match) {
                show(n);
              } else {
                boolean b = visitAllNodes(tree, n, searchString);
                if (b) {
                  childMatched = true;
                }
              }
            }
          }
          LOG.trace("visitAllNodes(..., {}) {} {}",
                    searchString, s, match ? "matches" : "does not match");
          return showNode(node, match || childMatched);
        }

        private void expandTree(final JTree tree) {
          for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
          }
        }

        private void process(DocumentEvent e) {
          String text = treePanel.getSearchField().getText().trim().toLowerCase();
          FilteredTreeModel filteredModel = (FilteredTreeModel) tree.getModel();
          filteredModel.setFilter(text);
          filteredModel.reload();
          expandTree(tree);
        }
      });

    constraints.gridy++;
    constraints.weightx = 1;
    constraints.weighty = 1;
    JScrollPane sp = new JScrollPane(tree);
    add(sp, constraints);
  }

  private void collapse(TreeNode node) {
    if (node.getChildCount() > 0) {
      for (Enumeration e = node.children(); e.hasMoreElements();) {
        TreeNode n = (TreeNode) e.nextElement();
        collapse(n);
      }
    }
    DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) node;
    tree.collapsePath(new TreePath(theNode));
  }

  private void collapseAll() {
    TreeNode root = (TreeNode) tree.getModel().getRoot();
    collapse(root);
  }

  public JTextField getSearchField() {
    return searchField;
  }
}
