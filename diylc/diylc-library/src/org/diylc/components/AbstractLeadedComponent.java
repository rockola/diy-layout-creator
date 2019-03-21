/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.components;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.ShadedPaint;
import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.LineStyle;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
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
  @Deprecated
  protected Point[] points;
  protected Point[] newPoints = new Point[] {new Point((int) (-DEFAULT_SIZE.convertToPixels() / 2), 0),
      new Point((int) (DEFAULT_SIZE.convertToPixels() / 2), 0), new Point(0, 0)};
  protected Color bodyColor = Color.white;
  protected Color borderColor = Color.black;
  protected Color labelColor = LABEL_COLOR;
  protected Color leadColor = LEAD_COLOR;
  protected Display display = Display.NAME;
  private boolean flipStanding = false;
  private LabelOriantation labelOriantation = LabelOriantation.Directional;
  protected boolean moveLabel = false;
  
  // parameters for adjusting the label control point
  protected Double gamma = null;
  protected Double p = null;

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
    if (newPoints != null)
      newPoints[2] = calculateLabelPosition(newPoints[0], newPoints[1]);
  }

  protected boolean IsCopperArea() {
    return false;
  }
  
  protected Point[] getNewPoints() {
    // convert old points to new
    if (points != null) {      
      newPoints = new Point[] { points[0], points[1], calculateLabelPosition(points[0], points[1]) };
      points = null;
      // to make standing components backward compatible and not show a label until the user switches the display to something else
      if (isStanding())
        display = Display.NONE;
    }
    return newPoints;
  }
  
  protected Point calculateLabelPosition(Point point1, Point point2) {
    double x = (point1.x + point2.x) / 2.0;
    double y = (point1.y + point2.y) / 2.0;
    return new Point((int) x, (int) y);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (gamma != null) {
      // recalculate center position and theta, then adjust label point accordingly, while preserving alpha and p
      double x = (getNewPoints()[1].x + getNewPoints()[0].x) / 2.0;
      double y = (getNewPoints()[1].y + getNewPoints()[0].y) / 2.0;
      double theta = Math.atan2(getNewPoints()[1].y - getNewPoints()[0].y, getNewPoints()[1].x - getNewPoints()[0].x);
      double beta = gamma - (Math.PI / 2 - theta);
      getNewPoints()[2].setLocation(x + Math.cos(beta) * p, y + Math.sin(beta) * p);
      gamma = null;
      p = null;
    }
    
    double distance = getNewPoints()[0].distance(getNewPoints()[1]);
    Shape shape = getBodyShape();
    // If there's no body, just draw the line connecting the ending points.
    if (shape == null) {
      drawLead(g2d, componentState, drawingObserver, IsCopperArea());
      return;
    }
    
    AffineTransform oldTransform = g2d.getTransform();
    double width;
    double length;
    Rectangle shapeRect;
    double theta = Math.atan2(getNewPoints()[1].y - getNewPoints()[0].y, getNewPoints()[1].x - getNewPoints()[0].x);
    
    if (isStanding()) {
      // When ending points are too close draw the component in standing
      // mode.
      width = length = getClosestOdd(this.width.convertToPixels());      
      Shape body =
          new Ellipse2D.Double((getFlipStanding() ? getNewPoints()[1] : getNewPoints()[0]).x - width / 2,
              (getFlipStanding() ? getNewPoints()[1] : getNewPoints()[0]).y - width / 2, width, width);
      shapeRect = body.getBounds();
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

      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.setColor(finalBorderColor);
      g2d.draw(body);
      if (!outlineMode) {
        drawLead(g2d, componentState, drawingObserver, false);
      }
    } else {
      // Normal mode with component body in the center and two lead parts.
      shapeRect = shape.getBounds();      
      // Go back to the original transformation to draw leads.
      if (!outlineMode) {
//        AffineTransform textTransform = g2d.getTransform();
        // if (length.convertToPixels() > points[0].distance(points[1]))
        // {
        // g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
        // 0.5f));
        // }
        float leadThickness = getLeadThickness();
        double leadLength = (distance - calculatePinSpacing(shapeRect)) / 2;

        if (shouldShadeLeads()) {
          Stroke leadStroke = ObjectCache.getInstance().fetchBasicStroke(leadThickness - 1);     
          Color leadColor = getLeadColorForPainting(componentState);          
          
          Area leadArea = new Area();
          
          int endX = (int) (getNewPoints()[0].x + Math.cos(theta) * leadLength);
          int endY = (int) (getNewPoints()[0].y + Math.sin(theta) * leadLength);
          Line2D line = new Line2D.Double(getNewPoints()[0].x, getNewPoints()[0].y, endX, endY);
          leadArea.add(new Area(leadStroke.createStrokedShape(line)));
          
          endX = (int) (getNewPoints()[1].x + Math.cos(theta - Math.PI) * leadLength);
          endY = (int) (getNewPoints()[1].y + Math.sin(theta - Math.PI) * leadLength);
          line = new Line2D.Double(getNewPoints()[1].x, getNewPoints()[1].y, endX, endY);
          leadArea.add(new Area(leadStroke.createStrokedShape(line)));
          
          g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
          g2d.setColor(leadColor);
          g2d.fill(leadArea);
          g2d.setColor(leadColor.darker());
          g2d.draw(leadArea);          
        } else {
          g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
          Color leadColor = getLeadColorForPainting(componentState);
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
      g2d.translate((getNewPoints()[0].x + getNewPoints()[1].x - length) / 2, (getNewPoints()[0].y + getNewPoints()[1].y - width) / 2);
      g2d.rotate(theta, length / 2, width / 2);
      // Draw body.
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      if (bodyColor != null) {
        g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);    
        
        if (!outlineMode && ConfigurationManager.getInstance().readBoolean(IPlugInPort.HI_QUALITY_RENDER_KEY, false)) {
          Point p1 = new Point((int) (length / 2), 0);
          Point p2 = new Point((int) (length / 2), (int) width);
          ShadedPaint paint = theta > 0 && theta < Math.PI ? new ShadedPaint(p2, p1, bodyColor) : new ShadedPaint(p1, p2, bodyColor);
          Paint oldPaint = g2d.getPaint();
          g2d.setPaint(paint);
          g2d.fill(shape);
          g2d.setPaint(oldPaint);
        } else {        
          g2d.fill(shape);
        }
      }
      
      Composite newComposite = null;
      if (!decorateAboveBorder())
        decorateComponentBody(g2d, outlineMode);
      else
        newComposite = g2d.getComposite();
      
      g2d.setComposite(oldComposite);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(getOutlineStrokeSize()));
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
    int offset = outlineMode ? 0 : getLabelOffset((int) length, (int) width, (int) textRect.getWidth());
    
    // Adjust label angle if needed to make sure that it's readable.
    if ((theta >= Math.PI / 2 && theta <= Math.PI) || (theta < -Math.PI / 2 && theta > -Math.PI)) {
      g2d.rotate(Math.PI, length / 2, width / 2);
      theta += Math.PI;
      offset = -offset;
    }
    
    if (getMoveLabel()) {
      g2d.setTransform(oldTransform);
      g2d.translate(getNewPoints()[2].x, getNewPoints()[2].y);
      if (getLabelOriantation() != LabelOriantation.Horizontal) {
        g2d.rotate(theta);
      }
      drawCenteredText(g2d, label, offset, 0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      g2d.setTransform(oldTransform);
    } else {
      if (getLabelOriantation() == LabelOriantation.Horizontal) {
        g2d.setTransform(oldTransform);
        double x = (getNewPoints()[0].x + getNewPoints()[1].x - length) / 2.0;
        double y = (getNewPoints()[0].y + getNewPoints()[1].y - width) / 2.0;
        g2d.drawString(label, (int) (x + (length - textRect.getWidth()) / 2 + offset),
            (int)(y + calculateLabelYOffset(shapeRect, textRect, fontMetrics)));
      } else {
        g2d.drawString(label, (int) (length - textRect.getWidth()) / 2 + offset,
            calculateLabelYOffset(shapeRect, textRect, fontMetrics));      
        g2d.setTransform(oldTransform); 
      }  
    }
//      if (getLabelOriantation() == LabelOriantation.Horizontal) {        
//
//        drawCenteredText(g2d, label, getNewPoints()[2].x, getNewPoints()[2].x, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
//        g2d.drawString(label, (int) (x + (length - textRect.getWidth()) / 2 + offset),
//            (int)(y + calculateLabelYCoordinate(shapeRect, textRect, fontMetrics)));
//      } else {
//        g2d.drawString(label, (int) (length - textRect.getWidth()) / 2 + offset,
//            calculateLabelYCoordinate(shapeRect, textRect, fontMetrics));      
//        g2d.setTransform(oldTransform); 
//      }          
  }

  protected boolean isStanding() {
    return supportsStandingMode() && this.length.convertToPixels() > getNewPoints()[0].distance(getNewPoints()[1]);
  }

  private void drawLeads(Graphics2D g2d, double theta, double leadLength) {
    double endX = getNewPoints()[0].x + Math.cos(theta) * leadLength;
    double endY = getNewPoints()[0].y + Math.sin(theta) * leadLength;
    g2d.draw(new Line2D.Double(getNewPoints()[0].x, getNewPoints()[0].y, endX, endY));

    endX = getNewPoints()[1].x + Math.cos(theta - Math.PI) * leadLength;
    endY = getNewPoints()[1].y + Math.sin(theta - Math.PI) * leadLength;
    g2d.draw(new Line2D.Double(getNewPoints()[1].x, getNewPoints()[1].y, endX, endY));
  }

  private void drawLead(Graphics2D g2d, ComponentState componentState, IDrawingObserver observer, boolean isCopperArea) {
    if (isCopperArea)
      observer.startTrackingContinuityArea(true);
    
    float thickness = getLeadThickness();
    
    Line2D line = new Line2D.Double(getNewPoints()[0].x, getNewPoints()[0].y, getNewPoints()[1].x, getNewPoints()[1].y);
    
    if (shouldShadeLeads()) {
      // for some reason the stroked line gets approx 1px thicker when converted to shape
      thickness -= 1;
    }
    
    Stroke stroke = null;
    switch (getStyle()) {
      case SOLID:
        stroke = ObjectCache.getInstance().fetchBasicStroke(thickness);
        break;
      case DASHED:
        stroke =
            ObjectCache.getInstance().fetchStroke(thickness, new float[] {thickness * 2, thickness * 4}, thickness * 4, BasicStroke.CAP_SQUARE);
        break;
      case DOTTED:
        stroke = ObjectCache.getInstance().fetchStroke(thickness, new float[] {thickness / 4, thickness * 4}, 0, BasicStroke.CAP_ROUND);
        break;
    }
    
    if (shouldShadeLeads()) {     
      Shape lineShape = stroke.createStrokedShape(line);
      
      g2d.setColor(getLeadColorForPainting(componentState));
      g2d.fill(lineShape);
  
      if (isCopperArea)
        observer.stopTrackingContinuityArea();
  
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      Color leadColor = getLeadColorForPainting(componentState);
      g2d.setColor(leadColor.darker());
      g2d.draw(lineShape);      
    } else {
      g2d.setColor(getLeadColorForPainting(componentState));
      g2d.setStroke(stroke);
      g2d.draw(line);
      
      if (isCopperArea)
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

  protected int calculateLabelYOffset(Rectangle2D shapeRect, Rectangle2D textRect, FontMetrics fontMetrics) {
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
    return getNewPoints().length;
  }

  @Override
  public Point getControlPoint(int index) {
    return (Point) getNewPoints()[index];
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
  public void setControlPoint(Point point, int index) {        
    // when moving one of the ending points, try to retain the angle and distance from the center point to label point
    if (index < 2 && gamma == null) {
      double x = (getNewPoints()[1].x + getNewPoints()[0].x) / 2.0;
      double y = (getNewPoints()[1].y + getNewPoints()[0].y) / 2.0;
      double theta = Math.atan2(getNewPoints()[1].y - getNewPoints()[0].y, getNewPoints()[1].x - getNewPoints()[0].x);
      double beta = Math.atan2(getNewPoints()[2].y - y, getNewPoints()[2].x - x);
      gamma = beta + (Math.PI / 2 - theta); 
      p = getNewPoints()[2].distance(x, y);
    }
    
    getNewPoints()[index].setLocation(point);   
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

  @EditableProperty(name = "Length", defaultable = true, validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getLength() {
    return length;
  }

  public void setLength(Size length) {
    this.length = length;
  }

  @EditableProperty(name = "Width", defaultable = true, validatorClass = PositiveNonZeroMeasureValidator.class)
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
  public LabelOriantation getLabelOriantation() {
    if (labelOriantation == null)
      labelOriantation = LabelOriantation.Directional;
    return labelOriantation;
  }
  
  public void setLabelOriantation(LabelOriantation labelOriantation) {
    this.labelOriantation = labelOriantation;
  }
  
  @Override
  public String getInternalLinkName(int index1, int index2) {
    if ((index1 == 0 && index2 == 1) || (index2 == 0 && index1 == 1))
      return getName();
    return null;
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
    
  @EditableProperty(name = "Moveable Label")
  public boolean getMoveLabel() {
    return moveLabel;
  }

  public void setMoveLabel(boolean moveLabel) {
    this.moveLabel = moveLabel;
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 2)
      return null;
    return Integer.toString(index + 1);
  }

  public enum LabelOriantation {
    Directional, Horizontal
  }
}
