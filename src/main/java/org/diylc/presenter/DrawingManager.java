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

package org.diylc.presenter;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.App;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.common.Config;
import org.diylc.common.DrawOption;
import org.diylc.common.EventType;
import org.diylc.common.GridType;
import org.diylc.common.IComponentFilter;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.Grid;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.utils.Constants;

/**
 * Utility that deals with painting {@link Project} on the {@link Graphics2D} and stores areas taken
 * by each drawn component in the component.
 *
 * @author Branislav Stojkovic
 */
public class DrawingManager {

  private static final Logger LOG = LogManager.getLogger(DrawingManager.class);
  private static final boolean SHADE_EXTRA_SPACE = true;

  public static final int CONTROL_POINT_SIZE = 7;
  public static final double EXTRA_SPACE = 0.25;
  public static final String ZOOM_KEY = "zoom";
  public static final Color CONTROL_POINT_COLOR = Color.blue;
  public static final Color SELECTED_CONTROL_POINT_COLOR = Color.green;

  private Composite slotComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
  private Composite lockedComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
  private List<IDIYComponent<?>> failedComponents = new ArrayList<IDIYComponent<?>>();
  private double zoomLevel = 1d; // ConfigurationManager.readDouble(ZOOM_KEY, 1d);
  private MessageDispatcher<EventType> messageDispatcher;

  public DrawingManager(MessageDispatcher<EventType> messageDispatcher) {
    super();
    this.messageDispatcher = messageDispatcher;
  }

  private void logTraceComponentSet(Collection<IDIYComponent<?>> components, String setIdentifier) {
    if (components != null && !components.isEmpty()) {
      LOG.trace("{} components=", setIdentifier);
      for (IDIYComponent<?> c : components) {
        LOG.trace("{} {}", setIdentifier.toUpperCase(), c.getIdentifier());
      }
    }
  }

  /**
   * Paints the project onto the canvas and returns a list of components that couldn't be drawn.
   *
   * @param g2d The canvas.
   * @param project The project.
   * @param drawOptions Options for drawing.
   * @param filter
   * @param selectionRect
   * @param lockedComponents
   * @param groupedComponents
   * @param controlPointSlot
   * @param componentSlot
   * @param dragInProgress
   * @param externalZoom
   * @return
   */
  public List<IDIYComponent<?>> drawProject(
      Graphics2D graphicsContext,
      Project project,
      Set<DrawOption> drawOptions,
      IComponentFilter filter,
      Rectangle selectionRect,
      // Collection<IDIYComponent<?>> selectedComponents,
      Set<IDIYComponent<?>> lockedComponents,
      Set<IDIYComponent<?>> groupedComponents,
      List<Point> controlPointSlot,
      List<IDIYComponent<?>> componentSlot,
      boolean dragInProgress,
      Double externalZoom) {

    failedComponents.clear();
    if (project == null) {
      return failedComponents;
    }
    LOG.trace(
        "drawProject(g2d, [Project {}], {}, ...) {}, externalZoom {}",
        project.getSequenceNumber(),
        drawOptions,
        dragInProgress ? "*DRAGGING*" : "not dragging",
        externalZoom);
    logTraceComponentSet(project.getSelection(), "selected");
    logTraceComponentSet(lockedComponents, "locked");
    logTraceComponentSet(groupedComponents, "grouped");

    double zoom = drawOptions.contains(DrawOption.ZOOM) ? zoomLevel : 1 / Constants.PIXEL_SIZE;
    if (externalZoom != null) {
      zoom *= externalZoom;
    }

    final Graphics2D g2d = (Graphics2D) graphicsContext.create();
    try {
      G2DWrapper g2dWrapper = new G2DWrapper(g2d, zoom);
      boolean antiAliasing = drawOptions.contains(DrawOption.ANTIALIASING);
      boolean hiQuality = App.highQualityRendering();

      g2d.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING,
          antiAliasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
      g2d.setRenderingHint(
          RenderingHints.KEY_TEXT_ANTIALIASING,
          antiAliasing
              ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
              : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
      g2d.setRenderingHint(
          RenderingHints.KEY_ALPHA_INTERPOLATION,
          hiQuality
              ? RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY
              : RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
      g2d.setRenderingHint(
          RenderingHints.KEY_COLOR_RENDERING,
          hiQuality
              ? RenderingHints.VALUE_COLOR_RENDER_QUALITY
              : RenderingHints.VALUE_COLOR_RENDER_SPEED);
      g2d.setRenderingHint(
          RenderingHints.KEY_RENDERING,
          hiQuality ? RenderingHints.VALUE_RENDER_QUALITY : RenderingHints.VALUE_RENDER_SPEED);
      g2d.setRenderingHint(
          RenderingHints.KEY_DITHERING,
          hiQuality ? RenderingHints.VALUE_DITHER_ENABLE : RenderingHints.VALUE_DITHER_DISABLE);
      g2d.setRenderingHint(
          RenderingHints.KEY_INTERPOLATION,
          hiQuality
              ? RenderingHints.VALUE_INTERPOLATION_BILINEAR
              : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

      // AffineTransform initialTx = g2d.getTransform();
      Dimension d =
          getCanvasDimensions(project, zoom, drawOptions.contains(DrawOption.EXTRA_SPACE));

      g2dWrapper.setColor(theme().getBgColor());
      g2dWrapper.fillRect(0, 0, d.width, d.height);
      g2d.clip(new Rectangle(new Point(0, 0), d));

      GridType gridType = GridType.LINES;
      if (drawOptions.contains(DrawOption.GRID) && gridType != GridType.NONE) {
        float zoomStep = (float) (project.getGridSpacing().convertToPixels() * zoom);
        float gridThickness = (float) (1f * (zoom > 1 ? 1 : zoom));
        switch (gridType) {
          case CROSSHAIR:
            g2d.setStroke(
                new BasicStroke(
                    gridThickness,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10f,
                    new float[] {zoomStep / 2f, zoomStep / 2f},
                    zoomStep / 4f));
            break;
          case DOT:
            g2d.setStroke(
                new BasicStroke(
                    gridThickness,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10f,
                    new float[] {1f, zoomStep - 1f},
                    0f));
            break;
          default:
            g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(gridThickness));
        }

        g2dWrapper.setColor(theme().getGridColor());
        for (double i = zoomStep; i < d.width; i += zoomStep) {
          g2dWrapper.draw(new Line2D.Double(i, 0, i, d.height - 1));
        }
        for (double j = zoomStep; j < d.height; j += zoomStep) {
          g2dWrapper.draw(new Line2D.Double(0, j, d.width - 1, j));
        }
      }

      Area extraSpaceRect = null;
      AffineTransform extraSpaceTx = null;
      // manage extra space
      double extraSpace = 0;
      if (drawOptions.contains(DrawOption.EXTRA_SPACE)) {
        extraSpace = getExtraSpace(project) * zoom;
        float borderThickness = (float) (3f * (zoom > 1 ? 1 : zoom));
        g2d.setStroke(
            ObjectCache.getInstance()
                .fetchStroke(
                    borderThickness,
                    new float[] {
                      borderThickness * 4, borderThickness * 4,
                    },
                    0,
                    BasicStroke.CAP_BUTT));
        g2dWrapper.setColor(theme().getOutlineColor());
        Dimension inner = getCanvasDimensions(project, zoom, false);
        extraSpaceRect = Area.rect(extraSpace, extraSpace, inner.getWidth(), inner.getHeight());
        g2d.draw(extraSpaceRect);
        extraSpaceTx = g2d.getTransform();

        // translate to the new (0, 0)
        g2d.transform(AffineTransform.getTranslateInstance(extraSpace, extraSpace));
      }

      // apply zoom
      if (Math.abs(1.0 - zoom) > 1e-4) {
        g2dWrapper.scale(zoom, zoom);
      }

      for (IDIYComponent<?> component : project.getComponents()) {
        // Do not draw the component if it's filtered out.
        if (filter != null && !filter.testComponent(component)) {
          continue;
        }
        ComponentState state = ComponentState.NORMAL;
        if (drawOptions.contains(DrawOption.SELECTION) && project.inSelection(component)) {
          state = dragInProgress ? ComponentState.DRAGGING : ComponentState.SELECTED;
        }
        // Do not track the area if component is not invalidated and was
        // drawn in the same state.
        ComponentState lastState = component.getState();
        boolean trackArea = lastState != state;
        LOG.trace(
            "Component {} {} lastState {} and state {} so trackArea is {}",
            component.getIdentifier(),
            component.getArea(),
            lastState,
            state,
            trackArea);

        synchronized (g2d) {
          g2dWrapper.startedDrawingComponent();
          if (!trackArea) {
            g2dWrapper.stopTracking();
          }
          // Draw locked components in a new composite.
          if (lockedComponents.contains(component)) {
            g2d.setComposite(lockedComposite);
          }
          // Draw the component through the g2dWrapper.
          try {
            component.setState(state);
            boolean outlineMode = drawOptions.contains(DrawOption.OUTLINE_MODE);
            LOG.trace(
                "drawOptions {} DrawOption.OUTLINE_MODE",
                outlineMode ? "contains" : "does not contain");
            component.setOutlineMode(outlineMode);
            component.draw(g2dWrapper, state, outlineMode, project, g2dWrapper);
            if (g2dWrapper.isTrackingContinuityArea()) {
              LOG.info(
                  "Component {} did not stop tracking continuity area.", component.getIdentifier());
              g2dWrapper.stopTrackingContinuityArea();
            }
          } catch (Exception e) {
            LOG.error("Error drawing " + component.getIdentifier(), e);
            failedComponents.add(component);
          }
          ComponentArea area = g2dWrapper.finishedDrawingComponent();
          if (trackArea && area != null && !area.getOutlineArea().isEmpty()) {
            LOG.trace("Setting area for component {} to {}", component.getIdentifier(), area);
            component.setArea(area);
            component.setState(state);
          } else {
            LOG.trace(
                "Did not set area for component {}, trackArea is {}",
                component.getIdentifier(),
                trackArea);
          }
          ComponentArea theArea = component.getArea();
          LOG.trace(
              "Component {} {} area{}",
              component.getIdentifier(),
              theArea == null ? "does not have" : "has",
              theArea != null && theArea.getOutlineArea().isEmpty() ? " but outline is empty" : "");
        }
      }

      // Draw control points.
      if (drawOptions.contains(DrawOption.CONTROL_POINTS)) {
        // Draw unselected points first to make sure they are below.
        if (dragInProgress || drawOptions.contains(DrawOption.OUTLINE_MODE)) {
          for (IDIYComponent<?> component : project.getComponents()) {
            for (int i = 0; i < component.getControlPointCount(); i++) {
              VisibilityPolicy vp = component.getControlPointVisibilityPolicy(i);
              boolean inSelection = project.inSelection(component);
              if ((groupedComponents.contains(component)
                      && (vp.isAlways() || (inSelection && vp.isWhenSelected()))
                  || (!groupedComponents.contains(component) && !inSelection && vp.isAlways()))) {

                g2dWrapper.setColor(CONTROL_POINT_COLOR);
                Point controlPoint = component.getControlPoint(i);
                int pointSize = CONTROL_POINT_SIZE - 2;
                g2dWrapper.fillOval(
                    controlPoint.x - pointSize / 2,
                    controlPoint.y - pointSize / 2,
                    pointSize,
                    pointSize);
              }
            }
          }
        }
        // Then draw the selected ones.
        for (IDIYComponent<?> component : project.getSelection()) {
          for (int i = 0; i < component.getControlPointCount(); i++) {
            VisibilityPolicy vp = component.getControlPointVisibilityPolicy(i);

            if (!groupedComponents.contains(component) && (vp.isWhenSelected() || vp.isAlways())) {

              Point controlPoint = component.getControlPoint(i);
              int pointSize = CONTROL_POINT_SIZE;

              g2dWrapper.setColor(SELECTED_CONTROL_POINT_COLOR.darker());
              g2dWrapper.fillOval(
                  controlPoint.x - pointSize / 2,
                  controlPoint.y - pointSize / 2,
                  pointSize,
                  pointSize);
              g2dWrapper.setColor(SELECTED_CONTROL_POINT_COLOR);
              g2dWrapper.fillOval(
                  controlPoint.x - CONTROL_POINT_SIZE / 2 + 1,
                  controlPoint.y - CONTROL_POINT_SIZE / 2 + 1,
                  CONTROL_POINT_SIZE - 2,
                  CONTROL_POINT_SIZE - 2);
            }
          }
        }
      }

      // Draw component slot in a separate composite.
      if (componentSlot != null) {
        g2dWrapper.startedDrawingComponent();
        g2dWrapper.setComposite(slotComposite);
        ComponentState state = ComponentState.NORMAL;
        boolean outlineMode = drawOptions.contains(DrawOption.OUTLINE_MODE);
        for (IDIYComponent<?> component : componentSlot) {
          try {
            component.setState(state);
            component.setOutlineMode(outlineMode);
            component.draw(g2dWrapper, state, outlineMode, project, g2dWrapper);

          } catch (Exception e) {
            LOG.error("Error drawing " + component.getIdentifier(), e);
            failedComponents.add(component);
          }
        }
        g2dWrapper.finishedDrawingComponent();
      }

      // Draw control points of the component in the slot.
      if (controlPointSlot != null) {
        for (Point point : controlPointSlot) {
          if (point != null) {
            g2dWrapper.setColor(SELECTED_CONTROL_POINT_COLOR.darker());
            g2dWrapper.fillOval(
                point.x - CONTROL_POINT_SIZE / 2,
                point.y - CONTROL_POINT_SIZE / 2,
                CONTROL_POINT_SIZE,
                CONTROL_POINT_SIZE);
            g2dWrapper.setColor(SELECTED_CONTROL_POINT_COLOR);
            g2dWrapper.fillOval(
                point.x - CONTROL_POINT_SIZE / 2 + 1,
                point.y - CONTROL_POINT_SIZE / 2 + 1,
                CONTROL_POINT_SIZE - 2,
                CONTROL_POINT_SIZE - 2);
          }
        }
      }

      // Go back to the original transformation and zoom in to draw the
      // selection rectangle and other similar elements.
      // g2d.setTransform(initialTx);
      // if ((drawOptions.contains(DrawOption.ZOOM)) && (Math.abs(1.0 -
      // zoomLevel) > 1e-4)) {
      // g2d.scale(zoomLevel, zoomLevel);
      // }

      // At the end draw selection rectangle if needed.
      if (drawOptions.contains(DrawOption.SELECTION) && (selectionRect != null)) {
        g2d.setColor(Color.white);
        g2d.draw(selectionRect);
        g2d.setColor(Color.black);
        g2d.setStroke(Constants.DASHED_STROKE);
        g2d.draw(selectionRect);
      }

      // Draw component/continuity areas when debugging
      final boolean debugComponentAreas = App.isDebug(Config.Flag.DEBUG_COMPONENT_AREA);
      final boolean debugContinuityAreas = App.isDebug(Config.Flag.DEBUG_CONTINUITY_AREA);
      if (debugComponentAreas || debugContinuityAreas) {
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        for (IDIYComponent<?> component : project.getComponents()) {
          LOG.trace("Component {}", component.getIdentifier());
          ComponentArea area = component.getArea();
          if (area == null) {
            LOG.error("Component {} area is NULL!", component.getIdentifier());
          } else {
            if (debugComponentAreas) {
              g2d.setColor(Color.red);
              g2d.draw(area.getOutlineArea());
            }
            if (debugContinuityAreas) {
              if (area.hasContinuityPositiveAreas()) {
                g2d.setColor(Color.green);
                for (Area continuityPositive : area.getContinuityPositiveAreas()) {
                  g2d.draw(continuityPositive);
                }
              }
              if (area.hasContinuityNegativeAreas()) {
                g2d.setColor(Color.blue);
                for (Area continuityNegative : area.getContinuityNegativeAreas()) {
                  g2d.draw(continuityNegative);
                }
              }
            }
          }
        }
      }

      if (project.hasContinuityArea() && App.highlightContinuityArea()) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.setColor(Color.green);
        g2d.fill(project.getContinuityArea());
      }

      // shade extra space
      if (SHADE_EXTRA_SPACE && extraSpaceRect != null) {
        Area extraSpaceArea = Area.rect(0, 0, d.getWidth(), d.getHeight()).subtract(extraSpaceRect);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
        g2d.setTransform(extraSpaceTx);
        extraSpaceArea.fill(g2d, theme().getOutlineColor());
      }
    } finally {
      g2d.dispose();
    }

    return failedComponents;
  }

  public double getZoomLevel() {
    return zoomLevel;
  }

  public void setZoomLevel(double zoomLevel) {
    LOG.trace("setZoomLevel({})", zoomLevel);
    if (this.zoomLevel != zoomLevel) {
      this.zoomLevel = zoomLevel;
      fireZoomChanged();
      // TODO: save zoom level in config?
    }
  }

  public void invalidateComponent(IDIYComponent<?> component) {
    LOG.trace("invalidateComponent({})", component.getIdentifier());
    component.resetArea();
    component.resetState();
  }

  public ComponentArea getComponentArea(IDIYComponent<?> component) {
    // return componentAreaMap.get(component);
    return component.getArea();
  }

  public static double getExtraSpace(Project project) {
    if (project == null) {
      return 0d;
    }
    double width = project.getWidth().convertToPixels();
    double height = project.getHeight().convertToPixels();
    double targetExtraSpace = EXTRA_SPACE * Math.max(width, height);
    return project.getGrid().roundToGrid(targetExtraSpace);
  }

  public static Dimension getCanvasDimensions(
      Project project, Double zoomLevel, boolean includeExtraSpace) {
    if (project == null) {
      return new Dimension(0, 0);
    }

    double width = project.getWidth().convertToPixels();
    double height = project.getHeight().convertToPixels();

    if (includeExtraSpace) {
      double extraSpace = getExtraSpace(project);
      width += 2 * extraSpace;
      height += 2 * extraSpace;
    }

    width *= zoomLevel;
    height *= zoomLevel;

    return new Dimension((int) width, (int) height);
  }

  public void fireZoomChanged() {
    if (messageDispatcher != null) {
      messageDispatcher.dispatchMessage(EventType.ZOOM_CHANGED, zoomLevel);
      messageDispatcher.dispatchMessage(EventType.REPAINT);
    }
  }

  private Theme theme() {
    return App.getTheme();
  }

  public Theme getTheme() {
    return theme();
  }

  public void setTheme(Theme theme) {
    App.setTheme(theme);
    if (messageDispatcher != null) {
      messageDispatcher.dispatchMessage(EventType.REPAINT);
    }
  }
}
