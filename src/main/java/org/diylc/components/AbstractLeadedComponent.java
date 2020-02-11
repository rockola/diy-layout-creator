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

package org.diylc.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import org.diylc.App;
import org.diylc.awt.ShadedPaint;
import org.diylc.awt.StringUtils;
import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.LineStyle;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

/**
 * Base class for all leaded components such as resistors or
 * capacitors. Has two control points and draws leads between
 * them. Also, it positions and draws the shape of the component as
 * specified by a child class.
 *
 * @author Branislav Stojkovic
 */
public abstract class AbstractLeadedComponent<T> extends AbstractTransparentComponent<T> {

  private static final long serialVersionUID = 1L;

  public static final Color LEAD_COLOR = Color.decode("#CCCCCC");
  public static final Color LEAD_COLOR_ICON = LEAD_COLOR.darker().darker();
  public static final Size LEAD_THICKNESS = Size.mm(0.6);
  public static final Size DEFAULT_SIZE = Size.in(1);

  protected Size length;
  protected Size width;
  protected Point[] points =
      new Point[] {
        new Point((int) (-DEFAULT_SIZE.convertToPixels() / 2), 0),
        new Point((int) (DEFAULT_SIZE.convertToPixels() / 2), 0),
        new Point(0, 0)
      };
  protected Color bodyColor = Color.white;
  protected Color borderColor = Color.black;
  protected Color labelColor = LABEL_COLOR;
  protected Color leadColor = LEAD_COLOR;
  protected boolean moveLabel = false;
  // parameters for adjusting the label control point
  protected Double gamma = null;
  protected Double r = null;

  private boolean flipStanding = false;
  private LabelOrientation labelOrientation = LabelOrientation.Directional;


  protected AbstractLeadedComponent() {
    super();
    this.length = getDefaultLength() == null ? null : new Size(getDefaultLength());
    this.width = getDefaultWidth() == null ? null : new Size(getDefaultWidth());
    this.display = Display.NAME;
    points[2] = calculateLabelPosition(points[0], points[1]);
  }

  protected boolean isCopperArea() {
    return false;
  }

  protected Point[] getPoints() {
    // convert old points to new
    if (points.length == 2) {
      points = new Point[] {points[0], points[1], calculateLabelPosition(points[0], points[1])};
      // to make standing components backward compatible and not show a label until the user
      // switches the display to something else
      if (isStanding()) {
        display = Display.NONE;
      }
    }
    return points;
  }

  protected Point getPoint(int nth) {
    return getPoints()[nth];
  }

  protected Point calculateLabelPosition(Point point1, Point point2) {
    return midpoint(point1, point2);
  }

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    double deltaX = getPoint(1).x - getPoint(0).x;
    double deltaY = getPoint(1).y - getPoint(0).y;
    double theta = Math.atan2(deltaY, deltaX);
    if (gamma != null) {
      // recalculate center position and theta, then adjust label point accordingly, while
      // preserving alpha and p
      double beta = gamma - (HALF_PI - theta);
      double x = (getPoint(1).x + getPoint(0).x) / 2.0;
      double y = (getPoint(1).y + getPoint(0).y) / 2.0;
      getPoints()[2].setLocation(x + Math.cos(beta) * r, y + Math.sin(beta) * r);
      gamma = null;
      r = null;
    }

    Area shape = getBodyShape();
    // If there's no body, just draw the line connecting the ending points.
    if (shape == null) {
      drawLead(g2d, drawingObserver, isCopperArea());
      return;
    }

    AffineTransform oldTransform = g2d.getTransform();
    double width;
    double length;
    Rectangle shapeRect;

    if (isStanding()) {
      // When ending points are too close draw the component in standing
      // mode.
      width = length = getClosestOdd(this.width.convertToPixels());
      Area body = Area.circle(
          (getFlipStanding() ? getPoints()[1] : getPoints()[0]).x - width / 2,
          (getFlipStanding() ? getPoints()[1] : getPoints()[0]).y - width / 2,
          width);
      shapeRect = body.getBounds();
      final Composite oldComposite = setTransparency(g2d);
      body.fill(g2d, outlineMode ? Constants.TRANSPARENT_COLOR : getStandingBodyColor());
      g2d.setComposite(oldComposite);

      Color finalBorderColor = tryBorderColor(outlineMode, borderColor);
      body.draw(g2d, finalBorderColor);
      if (!outlineMode) {
        g2d.setColor(finalBorderColor);
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
        drawLead(g2d, drawingObserver, false);
      }
    } else {
      // Normal mode with component body in the center and two lead parts.
      shapeRect = shape.getBounds();
      // Go back to the original transformation to draw leads.
      if (!outlineMode) {

        float leadThickness = getLeadThickness();
        double distance = getPoint(0).distance(getPoint(1));
        double leadLength = (distance - calculatePinSpacing(shapeRect)) / 2;

        if (shouldShadeLeads()) {
          Stroke leadStroke = ObjectCache.getInstance().fetchBasicStroke(leadThickness - 1);
          int endX = (int) (getPoint(0).x + Math.cos(theta) * leadLength);
          int endY = (int) (getPoint(0).y + Math.sin(theta) * leadLength);
          Line2D line = new Line2D.Double(getPoint(0).x, getPoint(0).y, endX, endY);
          Area leadArea = new Area(leadStroke.createStrokedShape(line));
          endX = (int) (getPoint(1).x + Math.cos(theta - Math.PI) * leadLength);
          endY = (int) (getPoint(1).y + Math.sin(theta - Math.PI) * leadLength);
          line = new Line2D.Double(getPoint(1).x, getPoint(1).y, endX, endY);
          leadArea.add(new Area(leadStroke.createStrokedShape(line)));

          g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
          Color leadColor = getLeadColorForPainting();
          leadArea.fillDraw(g2d, leadColor, leadColor.darker());
        } else {
          g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
          Color leadColor = getLeadColorForPainting();
          g2d.setColor(leadColor);
          drawLeads(g2d, theta, leadLength);
        }
        //        g2d.setTransform(textTransform);
      }
      // Transform graphics to draw the body in the right place and at the
      // right angle.
      if (useShapeRectAsPosition()) {
        width = shapeRect.getHeight();
        length = shapeRect.getWidth();
      } else {
        width = getWidth().convertToPixels();
        length = getLength().convertToPixels();
      }
      g2d.translate(
          (getPoint(0).x + getPoint(1).x - length) / 2,
          (getPoint(0).y + getPoint(1).y - width) / 2);
      g2d.rotate(theta, length / 2, width / 2);
      // Draw body.
      final Composite oldComposite = setTransparency(g2d);
      if (bodyColor != null) {
        g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);

        if (!outlineMode && App.highQualityRendering()) {
          Point p1 = new Point((int) (length / 2), 0);
          Point p2 = new Point((int) (length / 2), (int) width);
          ShadedPaint paint =
              theta > 0 && theta < Math.PI
                  ? new ShadedPaint(p2, p1, bodyColor)
                  : new ShadedPaint(p1, p2, bodyColor);
          Paint oldPaint = g2d.getPaint();
          g2d.setPaint(paint);
          g2d.fill(shape);
          g2d.setPaint(oldPaint);
        } else {
          g2d.fill(shape);
        }
      }

      Composite newComposite = null;
      if (decorateAboveBorder()) {
        newComposite = g2d.getComposite();
      } else {
        decorateComponentBody(g2d, outlineMode);
      }

      g2d.setComposite(oldComposite);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(getOutlineStrokeSize()));
      Color finalBorderColor = tryBorderColor(outlineMode, borderColor);
      g2d.setColor(finalBorderColor);
      g2d.draw(shape);

      if (decorateAboveBorder()) {
        g2d.setComposite(newComposite);
        decorateComponentBody(g2d, outlineMode);
        g2d.setComposite(oldComposite);
      }
    }

    // Draw label.
    g2d.setFont(project.getFont());
    if (useShapeRectAsPosition()) {
      g2d.translate(shapeRect.x, shapeRect.y);
    }
    Color finalLabelColor = tryLabelColor(outlineMode, labelColor);
    g2d.setColor(finalLabelColor);
    String label = getLabelForDisplay();
    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D textRect = fontMetrics.getStringBounds(label, g2d);
    // Don't offset in outline mode.
    int offset =
        outlineMode ? 0 : getLabelOffset((int) length, (int) width, (int) textRect.getWidth());

    // Adjust label angle if needed to make sure that it's readable.
    if ((theta >= HALF_PI && theta <= Math.PI) || (theta < -HALF_PI && theta > -Math.PI)) {
      g2d.rotate(Math.PI, length / 2, width / 2);
      theta += Math.PI;
      offset = -offset;
    }

    if (getMoveLabel()) {
      g2d.setTransform(oldTransform);
      g2d.translate(getPoint(2).x, getPoint(2).y);
      if (!getLabelOrientation().isHorizontal()) {
        g2d.rotate(theta);
      }
      StringUtils.drawCenteredText(
          g2d, label, offset, 0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      g2d.setTransform(oldTransform);
    } else {
      double x = 0.0;
      double y = 0.0;
      boolean standingOrHorizontal = isStanding() || getLabelOrientation().isHorizontal();
      if (standingOrHorizontal) {
        g2d.setTransform(oldTransform);
        x = (getPoint(0).x + getPoint(1).x - length) / 2.0;
        y = (getPoint(0).y + getPoint(1).y - width) / 2.0;
      }
      g2d.drawString(
          label,
          (int) (x + (length - textRect.getWidth()) / 2 + offset),
          (int) (y + calculateLabelYOffset(shapeRect, textRect, fontMetrics)));
      if (!standingOrHorizontal) {
        g2d.setTransform(oldTransform);
      }
    }
  }

  protected boolean isStanding() {
    return supportsStandingMode()
        && this.length.convertToPixels() > getPoints()[0].distance(getPoints()[1]);
  }

  private void drawLeads(Graphics2D g2d, double theta, double leadLength) {
    double endX = getPoints()[0].x + Math.cos(theta) * leadLength;
    double endY = getPoints()[0].y + Math.sin(theta) * leadLength;
    g2d.draw(new Line2D.Double(getPoints()[0].x, getPoints()[0].y, endX, endY));

    endX = getPoints()[1].x + Math.cos(theta - Math.PI) * leadLength;
    endY = getPoints()[1].y + Math.sin(theta - Math.PI) * leadLength;
    g2d.draw(new Line2D.Double(getPoints()[1].x, getPoints()[1].y, endX, endY));
  }

  private void drawLead(Graphics2D g2d,
                        IDrawingObserver observer,
                        boolean isCopperArea) {
    if (isCopperArea) {
      observer.startTrackingContinuityArea(true);
    }

    float thickness = getLeadThickness();
    Line2D line = new Line2D.Double(getPoint(0).x, getPoint(0).y, getPoint(1).x, getPoint(1).y);
    if (shouldShadeLeads()) {
      // for some reason the stroked line gets approx 1px thicker when converted to shape
      thickness -= 1;
    }

    Stroke stroke = null;
    switch (getStyle()) {
      case DASHED:
        stroke = ObjectCache.getInstance().fetchStroke(
            thickness,
            new float[] {thickness * 2, thickness * 4},
            thickness * 4,
            BasicStroke.CAP_SQUARE);
        break;
      case DOTTED:
        stroke = ObjectCache.getInstance().fetchStroke(
            thickness,
            new float[] {thickness / 4, thickness * 4},
            0,
            BasicStroke.CAP_ROUND);
        break;
      case SOLID:
      default:
        stroke = ObjectCache.getInstance().fetchBasicStroke(thickness);
        break;
    }

    if (shouldShadeLeads()) {
      Area lineShape = new Area(stroke.createStrokedShape(line));
      Color leadColor = getLeadColorForPainting();
      lineShape.fill(g2d, leadColor);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      lineShape.draw(g2d, leadColor.darker());
    } else {
      g2d.setStroke(stroke);
      g2d.setColor(getLeadColorForPainting());
      g2d.draw(line);
    }
    if (isCopperArea) {
      observer.stopTrackingContinuityArea();
    }
  }

  protected LineStyle getStyle() {
    return LineStyle.SOLID;
  }

  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    // Do nothing.
  }

  protected boolean decorateAboveBorder() {
    return false;
  }

  protected int calculateLabelYOffset(
      Rectangle2D shapeRect, Rectangle2D textRect, FontMetrics fontMetrics) {
    return (int) (shapeRect.getHeight() - textRect.getHeight()) / 2 + fontMetrics.getAscent();
  }

  protected boolean shouldShadeLeads() {
    return true;
  }

  protected boolean supportsStandingMode() {
    return false;
  }

  protected int getLabelOffset(int bodyLength, int bodyWidth, int labelLength) {
    return 0;
  }

  protected float getOutlineStrokeSize() {
    return 1f;
  }

  /**
     @return default component length.
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
   *     referenced to (0, 0).
   */
  protected abstract Area getBodyShape();

  /**
   * Controls how component shape should be placed relative to start and end point.
   *
   * <p>Returns <code>true</code> if shape rect should be used to center
   * the component or <code>false</code> to place the component
   * relative to <code>length</code> and <code>width</code> values.
   *
   * @return true (default unless child class overrides)
   */
  protected boolean useShapeRectAsPosition() {
    return true;
  }

  /**
     @return default lead thickness. Override this method to change it.
  */
  protected float getLeadThickness() {
    return getClosestOdd(LEAD_THICKNESS.convertToPixels());
  }

  /**
     @return default lead color. Override this method to change it.
  */
  protected Color getLeadColorForPainting() {
    return tryLeadColor(getLeadColor());
  }

  @EditableProperty(name = "Lead Color")
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
    return getPoints().length;
  }

  @Override
  public Point getControlPoint(int index) {
    return (Point) getPoints()[index];
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return index < 2;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return getMoveLabel() || index < 2 ? VisibilityPolicy.ALWAYS : VisibilityPolicy.NEVER;
  }

  @Override
  public boolean canControlPointOverlap(int index) {
    return index >= 2;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    // when moving one of the ending points, try to retain the angle and distance from the center
    // point to label point
    if (index < 2) {
      if (gamma == null) {
        double x = (getPoints()[1].x + getPoints()[0].x) / 2.0;
        double y = (getPoints()[1].y + getPoints()[0].y) / 2.0;
        double theta =
            Math.atan2(getPoints()[1].y - getPoints()[0].y, getPoints()[1].x - getPoints()[0].x);
        double beta = Math.atan2(getPoints()[2].y - y, getPoints()[2].x - x);
        gamma = beta + (HALF_PI - theta);
        r = getPoints()[2].distance(x, y);
      } else { // in case when we are copy pasting we don't want to recalculate 3rd point position
        // as they will all move in unison
        // when we moved the first point, gamma and r were initialized, so now we are canceling
        gamma = null;
        r = null;
      }
    }

    getPoints()[index].setLocation(point);
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

  @EditableProperty(
      name = "Length",
      defaultable = true,
      validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getLength() {
    return length;
  }

  public void setLength(Size length) {
    this.length = length;
  }

  @EditableProperty(
      name = "Width",
      defaultable = true,
      validatorClass = PositiveNonZeroMeasureValidator.class)
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

  @EditableProperty(name = "Label Color")
  public Color getLabelColor() {
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @EditableProperty(name = "Label Orientation")
  public LabelOrientation getLabelOrientation() {
    if (labelOrientation == null) {
      labelOrientation = LabelOrientation.Directional;
    }
    return labelOrientation;
  }

  public void setLabelOrientation(LabelOrientation labelOrientation) {
    this.labelOrientation = labelOrientation;
  }

  @Override
  public String getInternalLinkName(int index1, int index2) {
    return ((index1 == 0 && index2 == 1) || (index2 == 0 && index1 == 1)) ? getName() : null;
  }

  /**
   * Override this method with @EditableProperty annotation in child
   * classes where standing mode is supported.
   *
   * @return
   */
  public boolean getFlipStanding() {
    return flipStanding;
  }

  public void setFlipStanding(boolean flipStanding) {
    this.flipStanding = flipStanding;
  }

  @EditableProperty(name = "Moveable Label")
  public boolean getMoveLabel() {
    return moveLabel;
  }

  public void setMoveLabel(boolean moveLabel) {
    this.moveLabel = moveLabel;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return (index >= 2 ? null : Integer.toString(index + 1));
  }

  public enum LabelOrientation {
    Directional,
    Horizontal;

    public boolean isDirectional() {
      return this == Directional;
    }

    public boolean isHorizontal() {
      return this == Horizontal;
    }
  }
}
