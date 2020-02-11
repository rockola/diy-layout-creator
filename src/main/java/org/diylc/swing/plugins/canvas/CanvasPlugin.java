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

package org.diylc.swing.plugins.canvas;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.App;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.common.BadPositionException;
import org.diylc.common.Config;
import org.diylc.common.EventType;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.ComponentTransferable;
import org.diylc.core.ExpansionMode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.Template;
import org.diylc.core.measures.Size;
import org.diylc.images.Icon;
import org.diylc.swing.action.ActionFactory;
import org.diylc.swing.action.ActionFactoryAction;
import org.diylc.swing.plugins.file.ProjectDrawingProvider;
import org.diylc.swingframework.ruler.IRulerListener;
import org.diylc.swingframework.ruler.RulerScrollPane;

public class CanvasPlugin implements IPlugIn, ClipboardOwner {

  private static final Logger LOG = LogManager.getLogger(CanvasPlugin.class);

  private RulerScrollPane scrollPane;
  private CanvasPanel canvasPanel;
  private JPopupMenu popupMenu;
  private JMenu selectionMenu;
  private JMenu expandMenu;
  private JMenu transformMenu;
  private JMenu applyTemplateMenu;
  private Clipboard clipboard;
  private IPlugInPort pluginPort;
  private double zoomLevel = 1;
  private Map<String, ActionFactoryAction> actions = new HashMap<>();

  public CanvasPlugin() {
    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
  }

  @Override
  public void connect(IPlugInPort pluginPort) {
    this.pluginPort = pluginPort;
    try {
      App.ui().injectGuiComponent(getScrollPane(), SwingConstants.CENTER);
    } catch (BadPositionException e) {
      LOG.error("Could not install canvas plugin", e);
    }

    getScrollPane().setRulerVisible(App.showRulers());

    // revalidate canvas on scrolling when we render visible rect only
    if (CanvasPanel.RENDER_VISIBLE_RECT_ONLY) {
      AdjustmentListener visibleRectOnlyListener =
          new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
              getCanvasPanel().invalidateCache();
              getCanvasPanel().revalidate();
            }
          };

      getScrollPane().getHorizontalScrollBar().addAdjustmentListener(visibleRectOnlyListener);
      getScrollPane().getVerticalScrollBar().addAdjustmentListener(visibleRectOnlyListener);
    }

    ConfigurationManager.addListener(
        Config.Flag.SHOW_RULERS, (key, value) -> getScrollPane().setRulerVisible((Boolean) value));
    ConfigurationManager.addListener(
        Config.Flag.HARDWARE_ACCELERATION,
        (key, value) -> {
          canvasPanel.invalidateCache();
          scrollPane.invalidateBuffers();
        });
    ConfigurationManager.addListener(Config.Flag.METRIC, (key, value) -> updateZeroLocation());
    ConfigurationManager.addListener(
        Config.Flag.EXTRA_SPACE,
        (key, value) -> {
          refreshSize();
          // Scroll to the center.
          Rectangle visibleRect = canvasPanel.getVisibleRect();
          visibleRect.setLocation(
              (canvasPanel.getWidth() - visibleRect.width) / 2,
              (canvasPanel.getHeight() - visibleRect.height) / 2);
          canvasPanel.scrollRectToVisible(visibleRect);
          canvasPanel.revalidate();
          updateZeroLocation();
        });

    actions.put("cut", ActionFactory.createCutAction(pluginPort, clipboard, this));
    actions.put("copy", ActionFactory.createCopyAction(pluginPort, clipboard, this));
    actions.put("paste", ActionFactory.createPasteAction(pluginPort, clipboard));
    actions.put("duplicate", ActionFactory.createDuplicateAction(pluginPort));
    actions.put("edit-selection", ActionFactory.createEditSelectionAction(pluginPort));
    actions.put("delete-selection", ActionFactory.createDeleteSelectionAction(pluginPort));
    actions.put("rotate-clockwise", ActionFactory.createRotateSelectionAction(pluginPort, 1));
    actions.put(
        "rotate-counterclockwise", ActionFactory.createRotateSelectionAction(pluginPort, -1));
    actions.put(
        "mirror-horizontally",
        ActionFactory.createMirrorSelectionAction(pluginPort, IComponentTransformer.HORIZONTAL));
    actions.put(
        "mirror-vertically",
        ActionFactory.createMirrorSelectionAction(pluginPort, IComponentTransformer.VERTICAL));
    actions.put("save-as-template", ActionFactory.createSaveAsTemplateAction(pluginPort));
    actions.put("save-as-block", ActionFactory.createSaveAsBlockAction(pluginPort));
    actions.put("group", ActionFactory.createGroupAction(pluginPort));
    actions.put("ungroup", ActionFactory.createUngroupAction(pluginPort));
    actions.put("send-to-back", ActionFactory.createSendToBackAction(pluginPort));
    actions.put("bring-to-front", ActionFactory.createBringToFrontAction(pluginPort));
    actions.put("nudge", ActionFactory.createNudgeAction(pluginPort));
    actions.put(
        "expand-all", ActionFactory.createExpandSelectionAction(pluginPort, ExpansionMode.ALL));
    actions.put(
        "expand-immediate",
        ActionFactory.createExpandSelectionAction(pluginPort, ExpansionMode.IMMEDIATE));
    actions.put(
        "expand-same-type",
        ActionFactory.createExpandSelectionAction(pluginPort, ExpansionMode.SAME_TYPE));
  }

  public CanvasPanel getCanvasPanel() {
    if (canvasPanel == null) {
      canvasPanel = new CanvasPanel(pluginPort);
      canvasPanel.addMouseListener(
          new MouseAdapter() {

            private MouseEvent pressedEvent;

            @Override
            public void mouseClicked(MouseEvent e) {
              if (!scrollPane.isMouseScrollMode() && !(e.getButton() == MouseEvent.BUTTON2)) {
                pluginPort.mouseClicked(
                    e.getPoint(),
                    e.getButton(),
                    Utils.isMac() ? e.isMetaDown() : e.isControlDown(),
                    e.isShiftDown(),
                    e.isAltDown(),
                    e.getClickCount());
              }
            }

            @Override
            public void mousePressed(MouseEvent e) {
              LOG.info("Pressed: " + e.isPopupTrigger());
              canvasPanel.requestFocus();
              pressedEvent = e;
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
              // Invoke the rest of the code later so we get the chance to
              // process selection messages.
              SwingUtilities.invokeLater(
                  () -> {
                    if (pluginPort.getNewComponentTypeSlot() == null
                        && (e.isPopupTrigger()
                            || (pressedEvent != null && pressedEvent.isPopupTrigger()))) {
                      Project project = pluginPort.currentProject();
                      // Enable actions.
                      List<AbstractAction> selectionActions =
                          Arrays.asList(
                              actions.get("cut"),
                              actions.get("copy"),
                              actions.get("duplicate"),
                              actions.get("edit-selection"),
                              actions.get("delete-selection"),
                              actions.get("expand-all"),
                              actions.get("expand-immediate"),
                              actions.get("expand-same-type"),
                              actions.get("group"),
                              actions.get("ungroup"),
                              actions.get("nudge"),
                              actions.get("send-to-back"),
                              actions.get("bring-to-front"),
                              actions.get("rotate-clockwise"),
                              actions.get("rotate-counterclockwise"),
                              actions.get("mirror-horizontally"),
                              actions.get("mirror-vertically"));
                      boolean enabled = !project.emptySelection();
                      for (AbstractAction a : selectionActions) {
                        a.setEnabled(enabled);
                      }
                      enabled = false;
                      try {
                        enabled = clipboard.isDataFlavorAvailable(ComponentTransferable.listFlavor);
                      } catch (NullPointerException ex) {
                        LOG.error("mouseReleased() flavor is null?", ex);
                      } catch (IllegalStateException ie) {
                        // clipboard is currently unavailable
                        LOG.debug("mouseReleased() clipboard unavailable", ie);
                      }
                      actions.get("paste").setEnabled(enabled);
                      actions
                          .get("save-as-template")
                          .setEnabled(project.getSelection().size() == 1);
                      actions.get("save-as-block").setEnabled(project.getSelection().size() > 1);
                      showPopupAt(e.getX(), e.getY());
                    }
                  });
            }
          });

      canvasPanel.addKeyListener(
          new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
              if (pluginPort.keyPressed(
                  e.getKeyCode(),
                  Utils.isMac() ? e.isMetaDown() : e.isControlDown(),
                  e.isShiftDown(),
                  e.isAltDown())) {
                e.consume();
              }
            }
          });

      canvasPanel.addMouseMotionListener(
          new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
              if (!scrollPane.isMouseScrollMode()) {
                canvasPanel.setCursor(pluginPort.getCursorAt(e.getPoint()));
                pluginPort.mouseMoved(
                    e.getPoint(),
                    Utils.isMac() ? e.isMetaDown() : e.isControlDown(),
                    e.isShiftDown(),
                    e.isAltDown());
              }
            }
          });
    }
    return canvasPanel;
  }

  private RulerScrollPane getScrollPane() {
    if (scrollPane == null) {
      scrollPane =
          new RulerScrollPane(
              getCanvasPanel(),
              new ProjectDrawingProvider(pluginPort, true, false, true),
              Size.mm(10).convertToPixels(),
              Size.in(1).convertToPixels());
      scrollPane.invalidateBuffers();
      scrollPane.setMetric(App.metric());
      scrollPane.setWheelScrollingEnabled(true);
      scrollPane.addUnitListener(
          new IRulerListener() {

            @Override
            public void unitsChanged(boolean isMetric) {
              pluginPort.setMetric(isMetric);
            }
          });

      double extraSpace = pluginPort.getExtraSpace();
      scrollPane.setZeroLocation(new Point2D.Double(extraSpace, extraSpace));

      // disable built-in scrolling mechanism, we'll do it manually
      scrollPane.setWheelScrollingEnabled(false);

      scrollPane.addMouseWheelListener(
          new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
              final JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
              final JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();

              if (App.wheelZoom() || (Utils.isMac() ? e.isMetaDown() : e.isControlDown())) {

                Point mousePos = getCanvasPanel().getMousePosition(true);
                // change zoom level
                double oldZoom = pluginPort.getZoomLevel();
                double newZoom;
                Double[] availableZoomLevels = pluginPort.getAvailableZoomLevels();
                if (e.getWheelRotation() > 0) {
                  int i = availableZoomLevels.length - 1;
                  while (i > 0 && availableZoomLevels[i] >= oldZoom) {
                    i--;
                  }
                  pluginPort.setZoomLevel(newZoom = availableZoomLevels[i]);
                } else {
                  int i = 0;
                  while (i < availableZoomLevels.length - 1 && availableZoomLevels[i] <= oldZoom) {
                    i++;
                  }
                  pluginPort.setZoomLevel(newZoom = availableZoomLevels[i]);
                }

                Rectangle2D selectionBounds = pluginPort.getSelectionBounds(true);
                Rectangle visibleRect = scrollPane.getVisibleRect();

                if (selectionBounds == null) {
                  // center to cursor
                  Point desiredPos =
                      new Point(
                          (int) (1d * mousePos.x / oldZoom * newZoom),
                          (int) (1d * mousePos.y / oldZoom * newZoom));
                  int dx = desiredPos.x - mousePos.x;
                  int dy = desiredPos.y - mousePos.y;
                  horizontalScrollBar.setValue(horizontalScrollBar.getValue() + dx);
                  verticalScrollBar.setValue(verticalScrollBar.getValue() + dy);
                } else {
                  // center to selection
                  horizontalScrollBar.setValue(
                      (int)
                          (selectionBounds.getX()
                              + (selectionBounds.getWidth() - visibleRect.getWidth()) / 2));
                  verticalScrollBar.setValue(
                      (int)
                          (selectionBounds.getY()
                              + (selectionBounds.getHeight() - visibleRect.getHeight()) / 2));
                }
              }
              JScrollBar theScrollBar = e.isShiftDown() ? horizontalScrollBar : verticalScrollBar;
              int newValue =
                  theScrollBar.getValue()
                      + theScrollBar.getBlockIncrement()
                          * e.getScrollAmount()
                          * e.getWheelRotation();
              if (newValue <= theScrollBar.getMaximum()) {
                theScrollBar.setValue(newValue);
              }
            }
          });
    }
    return scrollPane;
  }

  private void showPopupAt(int x, int y) {
    updateSelectionMenu(x, y);
    updateApplyTemplateMenu();
    getPopupMenu().show(canvasPanel, x, y);
  }

  public JPopupMenu getPopupMenu() {
    if (popupMenu == null) {
      popupMenu = new JPopupMenu();
      popupMenu.add(getSelectionMenu());
      popupMenu.addSeparator();
      popupMenu.add(actions.get("cut"));
      popupMenu.add(actions.get("copy"));
      popupMenu.add(actions.get("paste"));
      popupMenu.add(actions.get("duplicate"));
      popupMenu.addSeparator();
      popupMenu.add(actions.get("edit-selection"));
      popupMenu.add(actions.get("delete-selection"));
      popupMenu.add(getTransformMenu());
      popupMenu.add(actions.get("save-as-template"));
      popupMenu.add(getApplyTemplateMenu());
      popupMenu.add(actions.get("save-as-block"));
      popupMenu.add(getExpandMenu());
      popupMenu.addSeparator();
      popupMenu.add(ActionFactory.createEditProjectAction(pluginPort));
    }
    return popupMenu;
  }

  public JMenu getSelectionMenu() {
    if (selectionMenu == null) {
      selectionMenu = new JMenu("Select");
      selectionMenu.setIcon(Icon.ElementsSelection.icon());
    }
    return selectionMenu;
  }

  public JMenu getExpandMenu() {
    if (expandMenu == null) {
      expandMenu = new JMenu(App.getString("menu.edit.expand-selection"));
      expandMenu.setIcon(Icon.BranchAdd.icon());
      expandMenu.add(actions.get("expand-all"));
      expandMenu.add(actions.get("expand-immediate"));
      expandMenu.add(actions.get("expand-same-type"));
    }
    return expandMenu;
  }

  public JMenu getTransformMenu() {
    if (transformMenu == null) {
      transformMenu = new JMenu(App.getString("menu.edit.transform-selection"));
      transformMenu.setIcon(Icon.MagicWand.icon());
      transformMenu.add(actions.get("rotate-clockwise"));
      transformMenu.add(actions.get("rotate-counterclockwise"));
      transformMenu.addSeparator();
      transformMenu.add(actions.get("mirror-horizontally"));
      transformMenu.add(actions.get("mirror-vertically"));
      transformMenu.addSeparator();
      transformMenu.add(actions.get("nudge"));
      transformMenu.addSeparator();
      transformMenu.add(actions.get("send-to-back"));
      transformMenu.add(actions.get("bring-to-front"));
      transformMenu.addSeparator();
      transformMenu.add(actions.get("group"));
      transformMenu.add(actions.get("ungroup"));
    }
    return transformMenu;
  }

  public JMenu getApplyTemplateMenu() {
    if (applyTemplateMenu == null) {
      applyTemplateMenu = new JMenu("Apply Variant");
      applyTemplateMenu.setIcon(Icon.BriefcaseInto.icon());
    }
    return applyTemplateMenu;
  }

  private void updateSelectionMenu(int x, int y) {
    getSelectionMenu().removeAll();
    for (IDIYComponent<?> component : pluginPort.findComponentsAt(new Point(x, y))) {
      JMenuItem item = new JMenuItem(component.getName());
      final IDIYComponent<?> finalComponent = component;
      item.addActionListener(
          (e) -> {
            Project project = pluginPort.currentProject();
            project.clearSelection();
            project.addToSelection(finalComponent);
            pluginPort.refresh();
          });
      getSelectionMenu().add(item);
    }
  }

  private void updateApplyTemplateMenu() {
    getApplyTemplateMenu().removeAll();
    List<Template> templates = null;

    try {
      LOG.trace("updateApplyTemplateMenu() Loading variants for selection");
      templates = pluginPort.getVariantsForSelection();
    } catch (Exception e) {
      LOG.info("Could not load variants for selection");
    }

    if (templates == null) {
      getApplyTemplateMenu().setEnabled(false);
      return;
    }

    getApplyTemplateMenu().setEnabled(templates.size() > 0);

    for (Template template : templates) {
      JMenuItem item = new JMenuItem(template.getName());
      final Template finalTemplate = template;
      item.addActionListener((e) -> pluginPort.applyVariantToSelection(finalTemplate));
      getApplyTemplateMenu().add(item);
    }
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(EventType.PROJECT_LOADED, EventType.ZOOM_CHANGED, EventType.REPAINT);
  }

  @Override
  public void processMessage(final EventType eventType, Object... params) {
    switch (eventType) {
      case PROJECT_LOADED:
        refreshSize();
        if ((Boolean) params[1]) {
          // Scroll to the center.
          final Rectangle visibleRect = canvasPanel.getVisibleRect();
          visibleRect.setLocation(
              (canvasPanel.getWidth() - visibleRect.width) / 2,
              (canvasPanel.getHeight() - visibleRect.height) / 2);
          SwingUtilities.invokeLater(
              () -> {
                canvasPanel.scrollRectToVisible(visibleRect);
                canvasPanel.revalidate();
              });
        }
        break;
      case ZOOM_CHANGED:
        final Rectangle visibleRect = canvasPanel.getVisibleRect();
        refreshSize();
        // Try to set the visible area to be centered with the previous
        // one.
        double zoomFactor = (Double) params[0] / zoomLevel;
        visibleRect.setBounds(
            (int) (visibleRect.x * zoomFactor),
            (int) (visibleRect.y * zoomFactor),
            visibleRect.width,
            visibleRect.height);

        canvasPanel.scrollRectToVisible(visibleRect);
        canvasPanel.revalidate();

        updateZeroLocation();

        zoomLevel = (Double) params[0];
        break;
      case REPAINT:
        canvasPanel.repaint();
        // Refresh selection bounds after we're done with painting to ensure we have traced the
        // component areas
        SwingUtilities.invokeLater(
            () -> {
              scrollPane.setSelectionRectangle(pluginPort.getSelectionBounds(true));
            });
        break;
      default:
        LOG.debug("{} event not handled", eventType);
    }
    // }
    // });
  }

  private void refreshSize() {
    Dimension d = pluginPort.getCanvasDimensions(true, App.extraSpace());
    canvasPanel.setSize(d);
    canvasPanel.setPreferredSize(d);
    getScrollPane().setZoomLevel(pluginPort.getZoomLevel());
  }

  /**
   * Causes ruler scroll pane to refresh by sending a fake mouse moved message to the canvasPanel.
   */
  public void refresh() {
    canvasPanel.dispatchEvent(
        new MouseEvent(
            canvasPanel,
            MouseEvent.MOUSE_MOVED,
            System.currentTimeMillis(),
            0,
            1,
            1, // canvasPanel.getWidth()
            // /
            // 2,
            // canvasPanel.getHeight() / 2,
            0,
            false));
  }

  @Override
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    // TODO Auto-generated method stub

  }

  private void updateZeroLocation() {
    double extraSpace = pluginPort.getExtraSpace();
    getScrollPane().setZeroLocation(new Point2D.Double(extraSpace, extraSpace));
  }
}
