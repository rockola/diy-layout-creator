/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2020 held jointly by the individual authors.

  This file is part of DIYLC.

  DIYLC is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  DIYLC is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc.swing.plugins.canvas;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.image.VolatileImage;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.App;
import org.diylc.common.ComponentType;
import org.diylc.common.DrawOption;
import org.diylc.common.IBlockProcessor.InvalidBlockException;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.plugins.tree.TreePanel;

/**
 * GUI class used to draw onto.
 *
 * @author Branislav Stojkovic
 */
public class CanvasPanel extends JComponent implements Autoscroll {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LogManager.getLogger(CanvasPlugin.class);

  public static boolean RENDER_VISIBLE_RECT_ONLY = true;

  private IPlugInPort plugInPort;

  private Image bufferImage;
  private GraphicsConfiguration screenGraphicsConfiguration;

  public boolean useHardwareAcceleration = App.hardwareAcceleration();

  // static final EnumSet<DrawOption> DRAW_OPTIONS =
  // EnumSet.of(DrawOption.GRID,
  // DrawOption.SELECTION, DrawOption.ZOOM, DrawOption.CONTROL_POINTS);
  // static final EnumSet<DrawOption> DRAW_OPTIONS_ANTI_ALIASING =
  // EnumSet.of(DrawOption.GRID,
  // DrawOption.SELECTION, DrawOption.ZOOM, DrawOption.ANTIALIASING,
  // DrawOption.CONTROL_POINTS);

  private HashMap<String, ComponentType> componentTypeCache;

  public CanvasPanel(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    setFocusable(true);
    initializeListeners();
    initializeDnD();
    GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] devices = graphicsEnvironment.getScreenDevices();
    screenGraphicsConfiguration = devices[0].getDefaultConfiguration();

    initializeActions();
  }

  public void invalidateCache() {
    bufferImage = null;
  }

  public HashMap<String, ComponentType> getComponentTypeCache() {
    if (componentTypeCache == null) {
      componentTypeCache = new HashMap<String, ComponentType>();
      for (Entry<String, List<ComponentType>> entry :
          this.plugInPort.getComponentTypes().entrySet()) {
        for (ComponentType type : entry.getValue())
          componentTypeCache.put(type.getInstanceClass().getCanonicalName(), type);
      }
    }
    return componentTypeCache;
  }

  private void initializeDnD() {
    // Initialize drag source recognizer.
    DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
        this,
        DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK,
        new CanvasGestureListener(plugInPort));
    // Initialize drop target.
    new DropTarget(
        this, DnDConstants.ACTION_COPY_OR_MOVE, new CanvasTargetListener(plugInPort), true);
  }

  private void initializeActions() {
    InputMap focusedMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    focusedMap.put(App.getKeyStroke("Repeat Last"), "repeatLast");
    focusedMap.put(App.getKeyStroke("Cancel"), "clearSlot");

    for (int i = 1; i <= 12; i++) {
      final int x = i;
      String fkey = "functionKey" + i;
      focusedMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + i - 1, 0), fkey);
      getActionMap().put(
          fkey,
          new AbstractAction() {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
              functionKeyPressed(x);
            }
          });
    }

    getActionMap().put(
        "clearSlot",
        new AbstractAction() {

          private static final long serialVersionUID = 1L;

          @Override
          public void actionPerformed(ActionEvent e) {
            CanvasPanel.this.plugInPort.setNewComponentTypeSlot(null, null, false);
          }
        });

    getActionMap().put(
        "repeatLast",
        new AbstractAction() {

          private static final long serialVersionUID = 1L;

          @SuppressWarnings("unchecked")
          @Override
          public void actionPerformed(ActionEvent e) {
            List<String> recent =
                (List<String>) App.getObject(IPlugInPort.Key.RECENT_COMPONENTS);
            if (recent != null && !recent.isEmpty()) {
              String clazz = recent.get(0);
              Map<String, List<ComponentType>> componentTypes =
                  CanvasPanel.this.plugInPort.getComponentTypes();
              for (Map.Entry<String, List<ComponentType>> entry : componentTypes.entrySet()) {
                for (ComponentType type : entry.getValue()) {
                  if (type.getInstanceClass().getCanonicalName().equals(clazz)) {
                    CanvasPanel.this.plugInPort.setNewComponentTypeSlot(type, null, false);
                    // hack: fake mouse movement to repaint
                    CanvasPanel.this.plugInPort.mouseMoved(
                        getMousePosition(), false, false, false);
                    return;
                  }
                }
              }
            }
          }
        });
  }

  @SuppressWarnings("unchecked")
  protected void functionKeyPressed(int i) {
    HashMap<String, String> shortcutMap =
        (HashMap<String, String>) App.getObject(TreePanel.COMPONENT_SHORTCUT_KEY);
    if (shortcutMap == null) {
      return;
    }

    String typeName = shortcutMap.get("F" + i);
    if (typeName == null) {
      return;
    }
    if (typeName.startsWith("block:")) {
      String blockName = typeName.substring(6);
      try {
        plugInPort.loadBlock(blockName);
      } catch (InvalidBlockException e) {
        LOG.error(
            "functionKeyPressed({}): Could not find block assigned to shortcut {}",
            i,
            blockName);
      }
    } else {
      ComponentType type = getComponentTypeCache().get(typeName);
      if (type == null) {
        LOG.error("functionKeyPressed({}): Could not find type {}", i, typeName);
        return;
      }
      this.plugInPort.setNewComponentTypeSlot(type, null, false);
    }

    // hack: fake mouse movement to repaint
    this.plugInPort.mouseMoved(getMousePosition(), false, false, false);
  }

  protected void createBufferImage() {
    Rectangle visibleRect = getVisibleRect();
    int imageWidth = RENDER_VISIBLE_RECT_ONLY ? visibleRect.width : getWidth();
    int imageHeight = RENDER_VISIBLE_RECT_ONLY ? visibleRect.height : getHeight();

    if (useHardwareAcceleration) {
      bufferImage =
          screenGraphicsConfiguration.createCompatibleVolatileImage(imageWidth, imageHeight);
      ((VolatileImage) bufferImage).validate(screenGraphicsConfiguration);
    } else {
      bufferImage = createImage(imageWidth, imageHeight);
    }
  }

  @Override
  public void paint(Graphics g) {
    if (plugInPort == null) {
      return;
    }
    if (bufferImage == null) {
      createBufferImage();
    }
    Graphics2D g2d = (Graphics2D) bufferImage.getGraphics();

    Rectangle visibleRect = getVisibleRect();

    int x = 0;
    int y = 0;

    if (RENDER_VISIBLE_RECT_ONLY) {
      x = visibleRect.x;
      y = visibleRect.y;
      g2d.translate(-x, -y);
    } else {
      g2d.setClip(visibleRect);
    }

    Set<DrawOption> drawOptions =
        EnumSet.of(DrawOption.SELECTION, DrawOption.ZOOM, DrawOption.CONTROL_POINTS);
    if (App.antiAliasing()) {
      drawOptions.add(DrawOption.ANTIALIASING);
    }
    if (App.outlineMode()) {
      drawOptions.add(DrawOption.OUTLINE_MODE);
    }
    if (App.showGrid()) {
      drawOptions.add(DrawOption.GRID);
    }
    if (App.extraSpace()) {
      drawOptions.add(DrawOption.EXTRA_SPACE);
    }

    plugInPort.draw(g2d, drawOptions, null, null);

    if (useHardwareAcceleration) {
      VolatileImage volatileImage = (VolatileImage) bufferImage;
      do {
        try {
          if (volatileImage.contentsLost()) {
            createBufferImage();
          }
          g.drawImage(bufferImage, x, y, this);
        } catch (NullPointerException e) {
          createBufferImage();
        }
      } while (volatileImage == null || volatileImage.contentsLost());
    } else {
      g.drawImage(bufferImage, x, y, this);
    }
    g2d.dispose();
  }

  @Override
  public void update(Graphics g) {
    paint(g);
  }

  private void initializeListeners() {
    addComponentListener(
        new ComponentAdapter() {

          @Override
          public void componentResized(ComponentEvent e) {
            invalidateCache();
            invalidate();
          }
        });
  }

  @Override
  public void autoscroll(Point cursorAt) {
    scrollRectToVisible(new Rectangle(cursorAt.x - 15, cursorAt.y - 15, 30, 30));
  }

  @Override
  public Insets getAutoscrollInsets() {
    Rectangle rect = getVisibleRect();
    return new Insets(
        rect.y - 15, rect.x - 15, rect.y + rect.height + 15, rect.x + rect.width + 15);
  }

  public void setUseHardwareAcceleration(boolean useHardwareAcceleration) {
    this.useHardwareAcceleration = useHardwareAcceleration;
    bufferImage = null;
  }
}
