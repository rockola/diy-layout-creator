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

package org.diylc.swing.plugins.tree;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.App;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.common.ComponentType;
import org.diylc.common.Favorite;
import org.diylc.common.Favorite.FavoriteType;
import org.diylc.common.IBlockProcessor.InvalidBlockException;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.core.Template;
import org.diylc.images.Icon;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.Presenter;
import org.diylc.swing.plugins.toolbox.ComponentButtonFactory;

public class TreePanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(TreePanel.class);

  public static final String COMPONENT_SHORTCUT_KEY = "componentShortcuts";

  private DefaultTreeModel treeModel;
  private JPopupMenu popup;
  private JTree tree;
  private JScrollPane treeScroll;
  private JTextField searchField = makeSearchField();
  private DefaultMutableTreeNode recentNode;
  private DefaultMutableTreeNode blocksNode;
  private DefaultMutableTreeNode favoritesNode;
  private List<String> recentComponents;
  private List<Favorite> favorites;
  private List<String> blocks;

  private IPlugInPort plugInPort;
  private boolean initializing = false;

  protected static Dimension visibleComponentSize = new Dimension(250, 32);
  protected static Dimension visibleGroupSize = new Dimension(250, 20);
  protected static Dimension invisibleSize = new Dimension(0, 0);
  protected static int evocationCounter = 1;
  protected static HashMap<String, String> shortcutMap =
      (HashMap<String, String>) App.getObject(TreePanel.COMPONENT_SHORTCUT_KEY);

  public TreePanel(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weighty = 0;
    gbc.gridwidth = 1;

    add(getSearchField(), gbc);

    gbc.gridy++;

    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 1;
    gbc.weightx = 1;

    add(getTreeScroll(), gbc);

    setPreferredSize(new Dimension(240, 200));

    ConfigurationManager.addListener(
        IPlugInPort.Key.RECENT_COMPONENTS,
        (key, value) -> {
          List<String> newComponents = (List<String>) value;
          if (newComponents != null
              && !new HashSet<String>(newComponents)
              .equals(new HashSet<String>(TreePanel.this.recentComponents))) {
            LOG.info("Detected recent component change");
            refreshRecentComponents(newComponents);
            TreePanel.this.recentComponents = new ArrayList<String>(newComponents);
          } else {
            LOG.info("Detected no recent component change");
          }
        });
    ConfigurationManager.addListener(
        IPlugInPort.Key.BLOCKS,
        (key, value) -> {
          if (value != null) {
            Map<String, List<IDIYComponent<?>>> newBlocks =
                (Map<String, List<IDIYComponent<?>>>) value;
            List<String> blockNames = new ArrayList<String>(newBlocks.keySet());
            Collections.sort(blockNames);
            if (!blockNames.equals(TreePanel.this.blocks)) {
              LOG.info("Detected block change");
              refreshBuildingBlocks(blockNames);
            } else {
              LOG.info("Detected no block change");
            }
          } else {
            LOG.info("Detected no block change");
          }
        });
    ConfigurationManager.addListener(
        IPlugInPort.Key.TEMPLATES,
        (key, value) -> {
          LOG.info("Detected variants change, repainting the tree");
          repaint();
        });
    ConfigurationManager.addListener(
        IPlugInPort.Key.FAVORITES,
        (key, value) -> {
          List<Favorite> newFavorites = (List<Favorite>) value;
          if (newFavorites != null && !newFavorites.equals(TreePanel.this.favorites)) {
            LOG.info("Detected favorites change");
            refreshFavorites(newFavorites);
          } else {
            LOG.info("Detected no favorites change");
          }
        });

    getTree().expandRow(0);
    initializeDnD();
  }

  private void initializeDnD() {
    // Initialize drag source recognizer.
    DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
        getTree(),
        DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK,
        new TreeGestureListener(plugInPort));
    // Initialize drop target.
    new DropTarget(
        getTree(),
        DnDConstants.ACTION_COPY_OR_MOVE,
        new TreeTargetListener(plugInPort),
        true);
  }

  public JScrollPane getTreeScroll() {
    if (treeScroll == null) {
      treeScroll = new JScrollPane(getTree());
      treeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }
    return treeScroll;
  }

  public DefaultMutableTreeNode getRecentNode() {
    if (this.recentNode == null) {
      this.recentNode = new DefaultMutableTreeNode(new Payload("(Recently Used)", null), true);

      List<String> recent = (List<String>) App.getObject(IPlugInPort.Key.RECENT_COMPONENTS);
      if (recent != null) {
        this.recentComponents = new ArrayList<String>(recent);
        refreshRecentComponents(recent);
      } else {
        this.recentComponents = new ArrayList<String>();
      }
    }
    return this.recentNode;
  }

  public DefaultMutableTreeNode getBlocksNode() {
    if (this.blocksNode == null) {
      this.blocksNode = new DefaultMutableTreeNode(new Payload("(Building Blocks)", null), true);

      Map<String, List<IDIYComponent<?>>> newBlocks =
          (Map<String, List<IDIYComponent<?>>>) App.getObject(IPlugInPort.Key.BLOCKS);
      if (newBlocks != null) {
        List<String> blockNames = new ArrayList<String>(newBlocks.keySet());
        Collections.sort(blockNames);
        refreshBuildingBlocks(blockNames);
      } else {
        this.blocks = new ArrayList<String>();
      }
    }
    return this.blocksNode;
  }

  public DefaultMutableTreeNode getFavoritesNode() {
    if (this.favoritesNode == null) {
      this.favoritesNode = new DefaultMutableTreeNode(new Payload("(Favorites)", null), true);

      List<Favorite> favorites = (List<Favorite>) App.getObject(IPlugInPort.Key.FAVORITES);
      if (favorites != null) {
        refreshFavorites(favorites);
      } else {
        this.favorites = new ArrayList<Favorite>();
      }
    }
    return this.favoritesNode;
  }

  private void refreshRecentComponents(List<String> recentComponentClassList) {
    getRecentNode().removeAllChildren();
    for (String componentClassName : recentComponentClassList) {
      final ComponentType componentType;
      try {
        componentType = ComponentType.extractFrom(
            (Class<? extends IDIYComponent<?>>) Class.forName(componentClassName));
        Payload payload = new Payload(
            componentType,
            new MouseAdapter() {

              @Override
              public void mouseClicked(MouseEvent e) {
                if (plugInPort.getNewComponentTypeSlot() != componentType) {
                  plugInPort.setNewComponentTypeSlot(componentType, null, false);
                }
              }
            });
        final DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(payload, false);
        String text = getSearchField().getText().trim().toLowerCase();
        boolean visible = componentType.isOfInterest(text);
        payload.setVisible(visible);
        getRecentNode().add(componentNode);
      } catch (ClassNotFoundException e) {
        LOG.error("Could not create recent component button for " + componentClassName, e);
      }
    }
    if (!initializing) {
      getTreeModel().nodeStructureChanged(getRecentNode());
    }
  }

  private void refreshBuildingBlocks(List<String> blocks) {
    this.blocks = blocks;

    getBlocksNode().removeAllChildren();
    for (final String block : blocks) {
      Payload payload = new Payload(
          block,
          new MouseAdapter() {

            long previousActionTime = 0;

            @Override
            public void mouseClicked(MouseEvent e) {
              if (e == null
                  || SwingUtilities.isLeftMouseButton(e)
                  && System.currentTimeMillis() - previousActionTime > 100) {
                previousActionTime = System.currentTimeMillis();
                try {
                  plugInPort.loadBlock(block);
                } catch (InvalidBlockException e1) {
                  // TODO Auto-generated catch block
                  e1.printStackTrace();
                }
              }
            }
          });
      final DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(payload, false);
      String text = getSearchField().getText().trim().toLowerCase();
      boolean visible = block.toLowerCase().contains(text);
      payload.setVisible(visible);
      getBlocksNode().add(componentNode);
    }
    if (!initializing) {
      getTreeModel().nodeStructureChanged(getBlocksNode());
    }
  }

  private void refreshFavorites(List<Favorite> favorites) {
    this.favorites = new ArrayList<Favorite>(favorites);

    getFavoritesNode().removeAllChildren();

    Map<String, List<ComponentType>> types = ComponentType.getComponentTypes();
    Map<String, ComponentType> typesByClass = new HashMap<String, ComponentType>();
    for (Map.Entry<String, List<ComponentType>> e : types.entrySet()) {
      for (ComponentType c : e.getValue()) {
        typesByClass.put(c.getInstanceClass().getCanonicalName(), c);
      }
    }

    for (Favorite f : favorites) {
      if (f.getType() == FavoriteType.Component) {
        final ComponentType type = typesByClass.get(f.getName());
        if (type != null) {
          Payload payload = new Payload(
              type,
              new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                  plugInPort.setNewComponentTypeSlot(type, null, false);
                }
              });
          DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(payload, false);
          getFavoritesNode().add(componentNode);
        }
      } else if (f.getType() == FavoriteType.Block) {
        final String block = f.getName();
        Payload payload = new Payload(
            block,
            new MouseAdapter() {

              long previousActionTime = 0;

              @Override
              public void mouseClicked(MouseEvent e) {
                if (e == null
                    || SwingUtilities.isLeftMouseButton(e)
                    && System.currentTimeMillis() - previousActionTime > 100) {
                  previousActionTime = System.currentTimeMillis();
                  try {
                    plugInPort.loadBlock(block);
                  } catch (InvalidBlockException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                  }
                }
              }
            });
        final DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(payload, false);
        String text = getSearchField().getText().trim().toLowerCase();
        boolean visible = block.toLowerCase().contains(text);
        payload.setVisible(visible);
        getFavoritesNode().add(componentNode);
      }
    }
    if (!initializing) {
      getTreeModel().nodeStructureChanged(getFavoritesNode());
    }
  }

  private static class MyTreeModelListener implements TreeModelListener {
    public void treeNodesChanged(TreeModelEvent e) {
      // super.treeNodesChanged(e);
    }

    public void treeNodesInserted(TreeModelEvent e) {}

    public void treeNodesRemoved(TreeModelEvent e) {
      // super.treeNodesRemoved(e);
    }

    public void treeStructureChanged(TreeModelEvent e) {}
  }

  public DefaultTreeModel getTreeModel() {
    if (treeModel == null) {
      LOG.trace("getTreeModel() building tree model");
      final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Components", true);
      initializing = true;
      rootNode.add(getFavoritesNode());
      rootNode.add(getRecentNode());
      rootNode.add(getBlocksNode());
      initializing = false;
      Map<String, List<ComponentType>> componentTypes = ComponentType.getComponentTypes();
      for (String category : ComponentType.getCategories(true)) {
        final DefaultMutableTreeNode categoryNode =
            new DefaultMutableTreeNode(new Payload(category, null));
        rootNode.add(categoryNode);
        for (final ComponentType type : componentTypes.get(category)) {
          Payload payload = new Payload(
              type,
              new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                  plugInPort.setNewComponentTypeSlot(type, null, false);
                }
              });
          final DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(payload, false);
          categoryNode.add(componentNode);
        }
      }
      treeModel = new DefaultTreeModel(rootNode);
      treeModel.addTreeModelListener(new MyTreeModelListener());
    }
    return treeModel;
  }

  public JTree getTree() {
    if (tree == null) {
      tree = new JTree(getTreeModel());
      tree.setRootVisible(false);
      tree.setCellRenderer(new ComponentCellRenderer());
      tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      tree.setRowHeight(0);
      ToolTipManager.sharedInstance().registerComponent(tree);

      tree.addTreeSelectionListener(new TreeSelectionListener() {

          @Override
          public void valueChanged(TreeSelectionEvent e) {
            if (e.isAddedPath()) {
              DefaultMutableTreeNode node =
                  (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
              if (node != null && node.getUserObject() != null) {
                Payload payload = (Payload) node.getUserObject();
                if (payload.getClickListener() != null) {
                  payload.getClickListener().mouseClicked(null);
                }
              }
            }
          }
        });

      tree.addMouseListener(new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() != 1) {
              return;
            }
            if (SwingUtilities.isRightMouseButton(e)) {
              int row = tree.getClosestRowForLocation(e.getX(), e.getY());
              tree.setSelectionRow(row);
              getPopup().show(e.getComponent(), e.getX(), e.getY());
            } else {
              TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
              DefaultMutableTreeNode node =
                  (DefaultMutableTreeNode) path.getLastPathComponent();
              if (node != null && node.getUserObject() != null) {
                Payload payload = (Payload) node.getUserObject();
                if (payload.getClickListener() != null) {
                  payload.getClickListener().mouseClicked(e);
                }
              }
            }
          }
        });
    }
    return tree;
  }

  public JPopupMenu getPopup() {
    if (popup == null) {
      popup = new JPopupMenu();
      popup.add("Loading...");
      popup.addPopupMenuListener(new PopupMenuListener() {

          @Override
          public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            popup.removeAll();

            DefaultMutableTreeNode selectedNode =
                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

            if (selectedNode == null || selectedNode.getUserObject() == null) {
              return;
            }

            Payload payload = (Payload) selectedNode.getUserObject();

            final ComponentType componentType = payload.getComponentType();

            final String identifier =
                componentType == null
                ? "block:" + payload.toString()
                : componentType.getInstanceClass().getCanonicalName();

            JMenu shortcutSubmenu = new JMenu("Assign Shortcut");
            final JMenuItem noneItem = new JMenuItem("None");
            noneItem.addActionListener((ev) -> {
                HashMap<String, String> map = (HashMap<String, String>) App.getObject(
                    COMPONENT_SHORTCUT_KEY,
                    new HashMap<String, String>());
                Iterator<Entry<String, String>> it = map.entrySet().iterator();
                while (it.hasNext()) {
                  Entry<String, String> item = it.next();
                  if (item.getValue().equals(identifier)) {
                    it.remove();
                  }
                }

                App.putValue(COMPONENT_SHORTCUT_KEY, map);
                TreePanel.this.invalidate();
                TreePanel.this.repaint();
              });
            shortcutSubmenu.add(noneItem);

            for (int i = 1; i <= 12; i++) {
              final JMenuItem item = new JMenuItem("F" + i);
              item.addActionListener((ev) -> {
                  HashMap<String, String> map = (HashMap<String, String>) App.getObject(
                      COMPONENT_SHORTCUT_KEY,
                      new HashMap<String, String>());
                  if (map.containsKey(item.getText())) {
                    map.remove(item.getText());
                  }
                  Iterator<Entry<String, String>> it = map.entrySet().iterator();
                  while (it.hasNext()) {
                    Entry<String, String> mapItem = it.next();
                    if (mapItem.getValue().equals(identifier)) {
                      it.remove();
                    }
                  }
                  map.put(item.getText(), identifier);

                  App.putValue(COMPONENT_SHORTCUT_KEY, map);
                  TreePanel.this.invalidate();
                  TreePanel.this.repaint();
                });
              shortcutSubmenu.add(item);
            }

            if (selectedNode.isLeaf()) {
              final Favorite fav =
                  new Favorite(
                      componentType == null ? FavoriteType.Block : FavoriteType.Component,
                      componentType == null
                      ? payload.toString()
                      : componentType.getInstanceClass().getCanonicalName());
              final boolean isFavorite = favorites != null && favorites.indexOf(fav) >= 0;
              final JMenuItem favoritesItem =
                  new JMenuItem(
                      isFavorite ? "Remove From Favorites" : "Add To Favorites",
                      isFavorite ? Icon.StarBlue.icon() : Icon.StarGrey.icon());
              favoritesItem.addActionListener((ev) -> {
                  List<Favorite> favorites =
                      new ArrayList<Favorite>(TreePanel.this.favorites);
                  if (isFavorite) {
                    favorites.remove(fav);
                  } else {
                    favorites.add(fav);
                    Collections.sort(favorites);
                  }
                  App.putValue(IPlugInPort.Key.FAVORITES, favorites);
                });
              popup.add(favoritesItem);
            }

            if (componentType != null) {
              popup.add(new SelectAllAction(plugInPort, componentType));
              popup.add(shortcutSubmenu);
              popup.add(new JSeparator());

              LOG.trace("getPopup() Getting variants for {}", componentType.getName());
              List<Template> templates = plugInPort.getVariantsFor(componentType);
              if (templates == null || templates.isEmpty()) {
                JMenuItem item = new JMenuItem("<no variants>");
                item.setEnabled(false);
                popup.add(item);
              } else {
                for (Template template : templates) {
                  JMenuItem item = ComponentButtonFactory.createVariantItem(
                      plugInPort, template, componentType);
                  popup.add(item);
                }
              }
            } else if (selectedNode.isLeaf()) {
              popup.add(shortcutSubmenu);
              popup.add(new DeleteBlockAction(plugInPort, payload.toString()));
            }
          }

          @Override
          public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

          @Override
          public void popupMenuCanceled(PopupMenuEvent e) {}
        });
    }
    return popup;
  }

  public JTextField getSearchField() {
    return searchField;
  }

  private JTextField makeSearchField() {
    searchField = new JTextField() {

        private static final long serialVersionUID = 1L;

        @Override
        public void paint(Graphics g) {
          super.paint(g);

          Graphics2D g2d = (Graphics2D) g;
          g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
          javax.swing.Icon icon = Icon.SearchBox.icon();
          icon.paintIcon(searchField, g2d, searchField.getWidth() - 18, 3);

          if (searchField.getText().trim().length() == 0 && !searchField.hasFocus()) {
            g2d.setColor(Color.gray);
            g2d.setFont(searchField.getFont());
            g2d.drawString(
                "Search (press Q to jump here)", 4, 3 + searchField.getFont().getSize());
          }
        }
      };

    searchField.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
          searchField.repaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
          searchField.repaint();
        }
      });

    searchField.getDocument().addDocumentListener(new DocumentListener() {

        public void changedUpdate(DocumentEvent e) {
          process(e);
        }

        public void removeUpdate(DocumentEvent e) {
          process(e);
        }

        public void insertUpdate(DocumentEvent e) {
          process(e);
        }

        private void process(DocumentEvent e) {
          String text = searchField.getText().trim().toLowerCase();
          LOG.trace("process() text '{}'", text);
          DefaultTreeModel model = getTreeModel();
          DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
          LOG.trace("process() iterating over model");
          for (int i = 0; i < model.getChildCount(rootNode); i++) {
            int visibleCount = 0;
            DefaultMutableTreeNode categoryNode =
                (DefaultMutableTreeNode) model.getChild(rootNode, i);
            LOG.trace("process() iterating over category");
            boolean categoryNodeChanged = false;
            for (int j = 0; j < model.getChildCount(categoryNode); j++) {
              DefaultMutableTreeNode componentNode =
                  (DefaultMutableTreeNode) model.getChild(categoryNode, j);
              Object obj = componentNode.getUserObject();
              if (obj instanceof Payload) {
                Payload payload = (Payload) obj;
                boolean visible = text.length() == 0 || payload.isOfInterest(text);
                if (visible != payload.isVisible()) {
                  payload.setVisible(visible);
                  model.nodeChanged(componentNode);
                  categoryNodeChanged = true;
                }
                if (visible) {
                  visibleCount++;
                }
              }
            }
            if (categoryNodeChanged) {
              model.nodeChanged(categoryNode);
            }

            Object obj = categoryNode.getUserObject();
            if (obj instanceof Payload) {
              Payload payload = (Payload) obj;
              boolean categoryVisible = visibleCount > 0;
              if (categoryVisible != payload.isVisible()) {
                payload.setVisible(categoryVisible);
                // model.nodeStructureChanged(rootNode);
              }
              if (categoryVisible && text.length() > 0) {
                getTree().expandPath(new TreePath(categoryNode.getPath()));
              }
            }
          }
          LOG.trace("process() iterated over model");
          for (int i = 0; i < model.getChildCount(rootNode); i++) {
            DefaultMutableTreeNode categoryNode =
                (DefaultMutableTreeNode) model.getChild(rootNode, i);
            getTree().expandPath(new TreePath(categoryNode.getPath()));
          }
          LOG.trace("process() iterated over model again");
          model.nodeChanged(rootNode);
        }
      });

    return searchField;
  }

  public class ComponentCellRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTreeCellRendererComponent(
        final JTree tree,
        final Object value,
        final boolean selected,
        final boolean expanded,
        final boolean leaf,
        final int row,
        final boolean hasFocus) {

      super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

      Object obj = ((DefaultMutableTreeNode) value).getUserObject();
      if (obj instanceof Payload) {
        Payload payload = (Payload) obj;
        boolean noComponentType = payload.getComponentType() == null;
        javax.swing.Icon icon = noComponentType
                                ? (leaf ? Icon.Component.icon() : null)
                                : payload.getIcon();
        if (icon != null) {
          setIcon(icon);
        } else {
          setOpenIcon(Icon.FolderOpen.icon());
          setClosedIcon(Icon.Folder.icon());
        }
        setToolTipText(payload.getTooltip());
        setPreferredSize(payload.getPreferredSize());
        setText(payload.labelText());
      }

      return this;
    }
  }

  public class Payload {
    private ComponentType componentType;
    private String category;
    private boolean isVisible;
    private MouseListener clickListener;
    private String searchKey;

    private String tooltip;
    private String visibleLabel;
    private String invisibleLabel;
    private int howManyVariants = -1;

    private final Pattern contributedPattern = Pattern.compile("^(.*)\\[(.*)\\]");

    private Payload(ComponentType componentType, String category, MouseListener clickListener) {
      super();
      this.componentType = componentType;
      this.category = category;
      this.clickListener = clickListener;
      this.isVisible = true;
      this.searchKey =
          componentType == null
          ? category.toLowerCase()
          : componentType.getSearchKey();
    }

    public Payload(ComponentType componentType, MouseListener clickListener) {
      this(componentType, null, clickListener);
    }

    public Payload(String category, MouseListener clickListener) {
      this(null, category, clickListener);
    }

    public ComponentType getComponentType() {
      return componentType;
    }

    public boolean isVisible() {
      return isVisible;
    }

    public void setVisible(boolean isVisible) {
      this.isVisible = isVisible;
    }

    public boolean isOfInterest(String text) {
      return searchKey.contains(text);
    }

    public MouseListener getClickListener() {
      return clickListener;
    }

    public Dimension getPreferredSize() {
      return isVisible()
          ? (componentType == null ? visibleGroupSize : visibleComponentSize)
          : invisibleSize;
    }

    public String forDisplay() {
      if (componentType == null) {
        String display = category;
        Matcher match = contributedPattern.matcher(display);
        if (match.find()) {
          String name = match.group(1);
          String owner = match.group(2);
          display = name + "<font color='gray'>[" + owner + "]</font>";
        }
        return display;
      }
      return componentType.getName();
    }

    public String labelText() {
      if (visibleLabel == null) {
        String variantsHtml = "";
        String identifier;
        if (componentType == null) {
          identifier = "block:" + toString();
        } else {
          identifier = componentType.getInstanceClass().getCanonicalName();
          if (howManyVariants() > 0) {
            variantsHtml = String.format(
                " <a style=\"text-shadow: -1px 0 black, 0 1px black, 1px 0 black, 0 -1px black; "
                + "background-color: #D7FFC6; color: #666666;\">[+%d]</a>",
                howManyVariants());
          }
        }
        String shortcutHtml = "";
        if (shortcutMap != null && shortcutMap.containsValue(identifier)) {
          for (String key : shortcutMap.keySet()) {
            if (shortcutMap.get(key).equals(identifier)) {
              shortcutHtml = String.format(
                  " <a style=\"text-shadow: "
                  + "-1px 0 black, 0 1px black, 1px 0 black, 0 -1px black; "
                  + "background-color: #eeeeee; color: #666666;\">&nbsp;%s&nbsp;</a>",
                  key);
            }
          }
        }
        visibleLabel = "<html>" + forDisplay() + shortcutHtml + variantsHtml + "</html>";
        invisibleLabel = "<html>" + forDisplay() + variantsHtml + "</html>";
      }
      return isVisible() ? visibleLabel : invisibleLabel;
    }

    public String getTooltip() {
      if (componentType != null && tooltip == null) {
        tooltip = String.format(
            "<html><b>%s</b><br>%s<br>Author: %s<br><br>"
            + "Left click to instantiate this component, right click for more options",
            componentType.getName(),
            componentType.getDescription(),
            componentType.getAuthor());
      }
      return tooltip;
    }

    public javax.swing.Icon getIcon() {
      return componentType == null ? null : componentType.getIcon();
    }

    public int howManyVariants() {
      if (howManyVariants == -1) {
        LOG.trace(
            "Payload::howManyVariants() #{} Getting variants for {}",
            evocationCounter++,
            componentType == null ? "(null)" : componentType.getName());
        howManyVariants = componentType == null ? 0 : componentType.howManyVariants();
      }
      return howManyVariants;
    }

    @Override
    public String toString() {
      return componentType == null ? category : componentType.getName();
    }
  }

  public static class SelectAllAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private ComponentType componentType;

    public SelectAllAction(IPlugInPort plugInPort, ComponentType componentType) {
      super();
      this.plugInPort = plugInPort;
      this.componentType = componentType;
      putValue(AbstractAction.NAME, "Select All");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("{} triggered", getValue(AbstractAction.NAME));
      if (componentType != null) {
        plugInPort.setNewComponentTypeSlot(null, null, false);
        Project project = plugInPort.currentProject();
        List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
        for (IDIYComponent<?> component : project.getComponents()) {
          if (componentType.getInstanceClass().equals(component.getClass())) {
            newSelection.add(component);
          }
        }

        project.clearSelection();
        project.setSelection(newSelection);
        plugInPort.refresh();
      }
    }
  }

  public static class DeleteBlockAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private String blockName;

    public DeleteBlockAction(IPlugInPort plugInPort, String blockName) {
      super();
      this.plugInPort = plugInPort;
      this.blockName = blockName;
      putValue(AbstractAction.NAME, "Delete Building Block");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("{} triggered", getValue(AbstractAction.NAME));
      if (App.ui().getPresenter().userConfirmed(
              "Are you sure you want to delete building block \"" + blockName + "\"?",
              "Delete Building Block")) {
        plugInPort.deleteBlock(blockName);
      }
    }
  }
}
