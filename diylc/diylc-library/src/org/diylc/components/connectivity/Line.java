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
package org.diylc.components.connectivity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import org.diylc.common.Display;
import org.diylc.common.LineStyle;
import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Line", author = "Branislav Stojkovic", category = "Shapes",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "LN", description = "Line with optional arrows",
    zOrder = IDIYComponent.COMPONENT, bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false,
    transformer = SimpleComponentTransformer.class)
public class Line extends AbstractLeadedComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = Color.black;  

  private Color color = COLOR;
  protected LineStyle style = LineStyle.SOLID; 
  private Size thickness = new Size(1d, SizeUnit.px);
  private Size arrowSize = new Size(5d, SizeUnit.px);
  private Polygon arrow = null;  
  private AffineTransform arrowTx = new AffineTransform();
  private boolean arrowStart = false;
  private boolean arrowEnd = false;

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(COLOR);
    g2d.drawLine(1, height - 2, width - 2, 1);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    float thickness = (float) getThickness().convertToPixels();
    Stroke stroke = null;
    switch (getStyle()) {
      case SOLID:
        stroke = ObjectCache.getInstance().fetchZoomableStroke(thickness);
        break;
      case DASHED:
        stroke = ObjectCache.getInstance().fetchStroke(thickness, new float[] {thickness * 2, thickness * 4}, thickness * 4);
        break;
      case DOTTED:
        stroke = ObjectCache.getInstance().fetchStroke(thickness, new float[] {thickness, thickness * 5}, 0);
        break;
    }
    g2d.setStroke(stroke);
    g2d.setColor(componentState == ComponentState.SELECTED ? SELECTION_COLOR : color);
    
    Point startPoint = new Point(getControlPoint(0));
    Point endPoint = new Point(getControlPoint(1));
    
    if (arrowStart) {
      arrowTx.setToIdentity();
      double angle = Math.atan2(getControlPoint(1).y - getControlPoint(0).y, getControlPoint(1).x - getControlPoint(0).x);
      arrowTx.translate(getControlPoint(0).x, getControlPoint(0).y);
      arrowTx.rotate((angle + Math.PI / 2d));
      AffineTransform oldTx = g2d.getTransform();
      g2d.transform(arrowTx);         
      g2d.fill(getArrow());
      g2d.setTransform(oldTx);
      
      double distance = distance(startPoint, endPoint);
      interpolate(startPoint, endPoint, getArrowSize().convertToPixels() * 0.9 / distance, startPoint);
    }
    if (arrowEnd) {
      arrowTx.setToIdentity();
      double angle = Math.atan2(getControlPoint(1).y - getControlPoint(0).y, getControlPoint(1).x - getControlPoint(0).x);
      arrowTx.translate(getControlPoint(1).x, getControlPoint(1).y);
      arrowTx.rotate((angle - Math.PI / 2d));
      AffineTransform oldTx = g2d.getTransform();
      g2d.transform(arrowTx);   
      g2d.fill(getArrow());
      g2d.setTransform(oldTx);
      
      double distance = distance(startPoint, endPoint);
      interpolate(endPoint, startPoint, getArrowSize().convertToPixels() * 0.9 / distance, endPoint);
    }
    
    g2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
  }
  
  private void interpolate(Point p1, Point p2, double t, Point p) {
    p.setLocation((int)Math.round(p1.x * (1-t) + p2.x * t), (int)Math.round(p1.y * (1-t) + p2.y * t));
  }
  
  private double distance(Point p1, Point p2) {
    return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
  }

  @Override
  public Color getLeadColorForPainting(ComponentState componentState) {
    return componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : color;
  }
  
  public Polygon getArrow() {
    if (arrow == null) {
      arrow = new Polygon();
      int t = (int) getArrowSize().convertToPixels();
      arrow.addPoint(0, 0);
      arrow.addPoint(-t, -t * 2);
      arrow.addPoint(t, -t * 2);
    }
    return arrow;
  }
  
  @EditableProperty
  public Size getThickness() {
    if (thickness == null)
      thickness = new Size(1d, SizeUnit.px);
    return thickness;
  }
  
  public void setThickness(Size thickness) {
    this.thickness = thickness;   
  }
  
  @EditableProperty
  public Size getArrowSize() {
    if (arrowSize == null)
      arrowSize = thickness = new Size(1d, SizeUnit.px); 
    return arrowSize;
  }
  
  public void setArrowSize(Size arrowSize) {
    this.arrowSize = arrowSize;
    arrow = null;
  }

  @Override
  public Color getLeadColor() {
    return super.getLeadColor();
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }
  
  @EditableProperty(name = "Style")
  public LineStyle getStyle() {
    if (style == null)
      style = LineStyle.SOLID;
    return style;
  }

  public void setStyle(LineStyle style) {
    this.style = style;
  }
  
  @EditableProperty(name = "Start Arrow")
  public boolean getArrowStart() {
    return arrowStart;
  }

  public void setArrowStart(boolean arrowStart) {
    this.arrowStart = arrowStart;
  }

  @EditableProperty(name = "End Arrow")
  public boolean getArrowEnd() {
    return arrowEnd;
  }

  public void setArrowEnd(boolean arrowEnd) {
    this.arrowEnd = arrowEnd;
  }

  public Color getBodyColor() {
    return super.getBodyColor();
  }

  @Override
  public Color getBorderColor() {
    return super.getBorderColor();
  }

  @Override
  public Byte getAlpha() {
    return super.getAlpha();
  }

  @Override
  public Size getLength() {
    return super.getLength();
  }

  @Override
  public Size getWidth() {
    return super.getWidth();
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}

  @Override
  protected Shape getBodyShape() {
    return null;
  }

  @Override
  protected Size getDefaultWidth() {
    return null;
  }

  @Override
  protected Size getDefaultLength() {
    return null;
  }

  @Deprecated
  @Override
  public Color getLabelColor() {
    return super.getLabelColor();
  }

  @Deprecated
  @Override
  public String getName() {
    return super.getName();
  }

  @Deprecated
  @Override
  public Display getDisplay() {
    return super.getDisplay();
  }
  
  @Deprecated
  @Override
  public org.diylc.components.AbstractLeadedComponent.LabelOriantation getLabelOriantation() {
    return super.getLabelOriantation();
  }
}
