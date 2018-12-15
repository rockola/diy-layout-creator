/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Observable;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

/**
 * Base class for all leaded components such as resistors or capacitors. Has two control points and
 * draws leads between them. Also, it positions and draws the shape of the component as specified by
 * a child class.
 * 
 * @author Branislav Stojkovic
 */
public abstract class AbstractLeadedComponent<T> extends AbstractTransparentComponent<T> {

  private static final long serialVersionUID = 1L;

  public static Color LEAD_COLOR = Color.decode("#CCCCCC");
  public static Color LEAD_COLOR_ICON = LEAD_COLOR.darker().darker();
  public static Size LEAD_THICKNESS = new Size(0.6d, SizeUnit.mm);
  public static Size DEFAULT_SIZE = new Size(1d, SizeUnit.in);

  protected Size length;
  protected Size width;
  protected Point[] points = new Point[] {new Point((int) (-DEFAULT_SIZE.convertToPixels() / 2), 0),
      new Point((int) (DEFAULT_SIZE.convertToPixels() / 2), 0)};
  protected Color bodyColor = Color.white;
  protected Color borderColor = Color.black;
  protected Color labelColor = LABEL_COLOR;
  protected Color leadColor = LEAD_COLOR;
  protected Display display = Display.NAME;
  private boolean flipStanding = false;

  protected AbstractLeadedComponent() {
    super();
    try {
      this.length = getDefaultLength().clone();
      this.width = getDefaultWidth().clone();
    } catch (CloneNotSupportedException e) {
      // This should never happen because Size supports cloning.
    } catch (NullPointerException e) {
      // This will happen if components do not have any shape.
    }
  }
  
  protected boolean IsCopperArea() {
    return false;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    double distance = points[0].distance(points[1]);
    Shape shape = getBodyShape();
    // If there's no body, just draw the line connecting the ending points.
    if (shape == null) {
      drawLead(g2d, componentState, drawingObserver, IsCopperArea());
    } else if (supportsStandingMode() && length.convertToPixels() > points[0].distance(points[1])) {
      // When ending points are too close draw the component in standing
      // mode.
      int width = getClosestOdd(this.width.convertToPixels());
      Shape body =
          new Ellipse2D.Double((getFlipStanding() ? points[1] : points[0]).x - width / 2,
              (getFlipStanding() ? points[1] : points[0]).y - width / 2, width, width);
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getStandingBodyColor());
      g2d.fill(body);
      g2d.setComposite(oldComposite);
      Color finalBorderColor;
      if (outlineMode) {
        Theme theme =
            (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
        finalBorderColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
                : theme.getOutlineColor();
      } else {
        finalBorderColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
                : borderColor;
      }

      g2d.setColor(finalBorderColor);
      g2d.draw(body);
      if (!outlineMode) {
        drawLead(g2d, componentState, drawingObserver, false);
      }
    } else {
      // Normal mode with component body in the center and two lead parts.
      Rectangle shapeRect = shape.getBounds();
      double theta = Math.atan2(points[1].y - points[0].y, points[1].x - points[0].x);
      // Go back to the original transformation to draw leads.
      if (!outlineMode) {
        AffineTransform textTransform = g2d.getTransform();
        // if (length.convertToPixels() > points[0].distance(points[1]))
        // {
        // g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
        // 0.5f));
        // }
        float leadThickness = getLeadThickness();
        double leadLength = (distance - calculatePinSpacing(shapeRect)) / 2;

        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
        Color leadColor = shouldShadeLeads() ?
                getLeadColorForPainting(componentState).darker() :
                getLeadColorForPainting(componentState);
        g2d.setColor(leadColor);
        drawLeads(g2d, theta, leadLength);

        if (shouldShadeLeads()) {
          g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness - 2));
          leadColor = getLeadColorForPainting(componentState);
          g2d.setColor(leadColor);
          drawLeads(g2d, theta, leadLength);
        }
        g2d.setTransform(textTransform);
      }
      // Transform graphics to draw the body in the right place and at the
      // right angle.
      AffineTransform oldTransform = null;
      double width;
      double length;
      if (useShapeRectAsPosition()) {
        width = shapeRect.getHeight();
        length = shapeRect.getWidth();
      } else {
        width = getWidth().convertToPixels();
        length = getLength().convertToPixels();
      }
      oldTransform = g2d.getTransform();
      g2d.translate((points[0].x + points[1].x - length) / 2, (points[0].y + points[1].y - width) / 2);
      g2d.rotate(theta, length / 2, width / 2);
      // Draw body.
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      if (bodyColor != null) {
          g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
          g2d.fill(shape);
      }
      decorateComponentBody(g2d, outlineMode);
      g2d.setComposite(oldComposite);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      Color finalBorderColor;
      if (outlineMode) {
        Theme theme =
            (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
        finalBorderColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
                : theme.getOutlineColor();
      } else {
        finalBorderColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
                : borderColor;
      }
      g2d.setColor(finalBorderColor);
      g2d.draw(shape);

      // // Go back to the original transformation to draw leads.
      // if (!outlineMode) {
      // AffineTransform textTransform = g2d.getTransform();
      // g2d.setTransform(oldTransform);
      // if (length.convertToPixels() > points[0].distance(points[1])) {
      // g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
      // 0.5f));
      // }
      // int leadThickness = getClosestOdd(getLeadThickness());
      // double leadLength = (distance - calculatePinSpacing(shapeRect)) /
      // 2 - leadThickness / 2;
      // g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
      // Color leadColor = shouldShadeLeads() ?
      // getLeadColor(componentState).darker()
      // : getLeadColor(componentState);
      // g2d.setColor(leadColor);
      // int endX = (int) (points[0].x + Math.cos(theta) * leadLength);
      // int endY = (int) Math.round(points[0].y + Math.sin(theta) *
      // leadLength);
      // g2d.drawLine(points[0].x, points[0].y, endX, endY);
      // endX = (int) (points[1].x + Math.cos(theta - Math.PI) *
      // leadLength);
      // endY = (int) Math.round(points[1].y + Math.sin(theta - Math.PI) *
      // leadLength);
      // g2d.drawLine(points[1].x, points[1].y, endX, endY);
      // if (shouldShadeLeads()) {
      // g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness
      // - 2));
      // leadColor = getLeadColor(componentState);
      // g2d.setColor(leadColor);
      // g2d.drawLine(points[0].x, points[0].y, (int) (points[0].x +
      // Math.cos(theta)
      // * leadLength), (int) (points[0].y + Math.sin(theta) *
      // leadLength));
      // g2d.drawLine(points[1].x, points[1].y, (int) (points[1].x +
      // Math.cos(theta
      // - Math.PI)
      // * leadLength), (int) (points[1].y + Math.sin(theta - Math.PI)
      // * leadLength));
      // }
      // g2d.setComposite(oldComposite);
      // g2d.setTransform(textTransform);
      // }

      // Draw label.
      g2d.setFont(project.getFont());
      if (useShapeRectAsPosition()) {
        g2d.translate(shapeRect.x, shapeRect.y);
      }
      Color finalLabelColor;
      if (outlineMode) {
        Theme theme =
            (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
        finalLabelColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
                : theme.getOutlineColor();
      } else {
        finalLabelColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
                : labelColor;
      }
      g2d.setColor(finalLabelColor);
      FontMetrics fontMetrics = g2d.getFontMetrics();
      String label = "";
      label = display == Display.NAME ? getName() : (getValue() == null ? "" : getValue().toString());
      if (display == Display.NONE) {
        label = "";
      }
      if (display == Display.BOTH) {
        label = getName() + " " + (getValue() == null ? "" : getValue().toString());
      }
      Rectangle2D textRect = fontMetrics.getStringBounds(label, g2d);
      // Don't offset in outline mode.
      int offset = outlineMode ? 0 : getLabelOffset((int) length, (int) textRect.getWidth());
      // Adjust label angle if needed to make sure that it's readable.
      if ((theta >= Math.PI / 2 && theta <= Math.PI) || (theta < -Math.PI / 2 && theta > -Math.PI)) {
        g2d.rotate(Math.PI, length / 2, width / 2);
        offset = -offset;
      }
      g2d.drawString(label, (int) (length - textRect.getWidth()) / 2 + offset,
          calculateLabelYCoordinate(shapeRect, textRect, fontMetrics));
      g2d.setTransform(oldTransform);
    }
  }

  private void drawLeads(Graphics2D g2d, double theta, double leadLength) {
    int endX = (int) (points[0].x + Math.cos(theta) * leadLength);
    int endY = (int) (points[0].y + Math.sin(theta) * leadLength);
    g2d.drawLine(points[0].x, points[0].y, endX, endY);

    endX = (int) (points[1].x + Math.cos(theta - Math.PI) * leadLength);
    endY = (int) (points[1].y + Math.sin(theta - Math.PI) * leadLength);
    g2d.drawLine(points[1].x, points[1].y, endX, endY);
  }

  private void drawLead(Graphics2D g2d, ComponentState componentState, IDrawingObserver observer, boolean isCopperArea) {
    if (isCopperArea)
      observer.startTrackingContinuityArea(true);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(getLeadThickness()));
    Color leadColor =
        shouldShadeLeads() ? getLeadColorForPainting(componentState).darker() : getLeadColorForPainting(componentState);
    g2d.setColor(leadColor);
    g2d.drawLine(points[0].x, points[0].y, points[1].x, points[1].y);
    
    if (isCopperArea)
      observer.stopTrackingContinuityArea();

    if (shouldShadeLeads()) {
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(getLeadThickness() - 2));
      leadColor = getLeadColorForPainting(componentState);
      g2d.setColor(leadColor);
      g2d.drawLine(points[0].x, points[0].y, points[1].x, points[1].y);
    }
  }

  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    // Do nothing.
  }

  protected int calculateLabelYCoordinate(Rectangle2D shapeRect, Rectangle2D textRect, FontMetrics fontMetrics) {
    return (int) (shapeRect.getHeight() - textRect.getHeight()) / 2 + fontMetrics.getAscent();
  }

  protected boolean shouldShadeLeads() {
    return true;
  }

  protected boolean supportsStandingMode() {
    return false;
  }

  protected int getLabelOffset(int bodyWidth, int labelWidth) {
    return 0;
  }

  /**
   * @return default component length.
   */
  protected abstract Size getDefaultLength();

  /**
   * Returns default component width.
   * 
   * @return
   */
  protected abstract Size getDefaultWidth();

  /**
   * @return shape that represents component body. Shape should not be transformed and should be
   *         referenced to (0, 0).
   */
  protected abstract Shape getBodyShape();

  /**
   * Controls how component shape should be placed relative to start and end point.
   * 
   * @return <code>true<code> if shape rect should be used to center the component or <code>false</code>
   *         to place the component relative to <code>length</code> and <code>width</code> values.
   */
  protected boolean useShapeRectAsPosition() {
    return true;
  }

  /**
   * @return default lead thickness. Override this method to change it.
   */
  protected float getLeadThickness() {
    return getClosestOdd(LEAD_THICKNESS.convertToPixels());
  }

  /**
   * @return default lead color. Override this method to change it.
   */
  protected Color getLeadColorForPainting(ComponentState componentState) {
    return componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : getLeadColor();
  }

  @EditableProperty(name = "Lead color")
  public Color getLeadColor() {
    if (leadColor == null) {
      leadColor = LEAD_COLOR_ICON;
    }
    return leadColor;
  }

  public void setLeadColor(Color leadColor) {
    this.leadColor = leadColor;
  }

  protected int calculatePinSpacing(Rectangle shapeRect) {
    return shapeRect.width;
  }

  @Override
  public int getControlPointCount() {
    return points.length;
  }

  @Override
  public Point getControlPoint(int index) {
    return (Point) points[index];
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.ALWAYS;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    points[index].setLocation(point);
  }

  @EditableProperty(name = "Color")
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  public Color getStandingBodyColor() {
    return bodyColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty(name = "Length", defaultable = true)
  public Size getLength() {
    return length;
  }

  public void setLength(Size length) {
    this.length = length;
  }

  @EditableProperty(name = "Width", defaultable = true)
  public Size getWidth() {
    return width;
  }

  public void setWidth(Size width) {
    this.width = width;
  }

  @EditableProperty
  public Display getDisplay() {
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  @EditableProperty(name = "Label color")
  public Color getLabelColor() {
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  /**
   * Override this method with @EditableProperty annotation in child classes where standing mode is
   * supported
   * 
   * @return
   */
  public boolean getFlipStanding() {
    return flipStanding;
  }

  public void setFlipStanding(boolean flipStanding) {
    this.flipStanding = flipStanding;
  }
}
