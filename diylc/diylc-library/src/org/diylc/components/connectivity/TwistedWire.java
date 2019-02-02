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
package org.diylc.components.connectivity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.diylc.common.LineStyle;
import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractCurvedComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Twisted Leads", author = "Branislav Stojkovic", category = "Connectivity",
    instanceNamePrefix = "W", description = "A pair of flexible leads twisted tighly together", zOrder = IDIYComponent.COMPONENT,
    flexibleZOrder = true, bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false,
    transformer = SimpleComponentTransformer.class, continuity = true)
public class TwistedWire extends AbstractCurvedComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = Color.green;
  public static Color COLOR2 = Color.blue;
  public static double INSULATION_THICKNESS_PCT = 0.3;

  protected AWG gauge = AWG._22;
  
  private Color color2 = COLOR2;
  
  // cached areas
  transient private Area firstLeadArea = null;
  transient private Area secondLeadArea = null;

  @Override
  protected Color getDefaultColor() {
    return COLOR;
  }
  
  @EditableProperty(name = "Color 1")
  public Color getLeadColor() {
    return color;
  }
  
  @EditableProperty(name = "Color 2")
  public Color getColor2() {
    return color2;
  }
  
  public void setColor2(Color color2) {
    this.color2 = color2;
  }
  
  @Override
  public LineStyle getStyle() {
    // prevent from editing
    return super.getStyle();
  }

  @Override
  protected void drawCurve(Path2D curve, Graphics2D g2d, ComponentState componentState, IDrawingObserver drawingObserver) {
    int thickness =
        (int) (Math.pow(Math.E, -1.12436 - 0.11594 * gauge.getValue()) * Constants.PIXELS_PER_INCH * (1 + 2 * INSULATION_THICKNESS_PCT)) - 1;
    Color curveColor1 =
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
            : color;
    Color curveColor2 =
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
            : color2;
    
    if (firstLeadArea == null || secondLeadArea == null)
      recalculate(curve, thickness);
    
    g2d.setColor(curveColor1);
    g2d.fill(firstLeadArea);
    g2d.setColor(curveColor2);
    g2d.fill(secondLeadArea);
    if (componentState == ComponentState.NORMAL) {      
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.setColor(color.darker());
      g2d.draw(firstLeadArea);
      g2d.setColor(color2.darker());
      g2d.draw(secondLeadArea);
    }
  }
  
  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {    
    Stroke stroke = ObjectCache.getInstance().fetchBasicStroke(3);
    CubicCurve2D curve1 = new CubicCurve2D.Double(2, height - 2, width - 4, height - 4, 4, 4, width - 2, 2);
    CubicCurve2D curve2 = new CubicCurve2D.Double(2, height - 2, 4, 4, width - 4, height - 4, width - 2, 2);
    
    Area area1 = new Area(stroke.createStrokedShape(curve1));
    Area area2 = new Area(stroke.createStrokedShape(curve2));
    area2.subtract(area1);
    
    g2d.setColor(COLOR2);    
    g2d.fill(area2);
    g2d.setColor(COLOR);
    g2d.fill(area1);
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(COLOR2.darker());    
    g2d.draw(area2);
    g2d.setColor(COLOR.darker());
    g2d.draw(area1);
  }

  @EditableProperty(name = "AWG")
  public AWG getGauge() {
    return gauge;
  }

  public void setGauge(AWG gauge) {
    this.gauge = gauge;
    
    // invalidate cached areas
    this.firstLeadArea = null;
    this.secondLeadArea = null;
  }
  
  @Override
  public void setControlPoint(Point point, int index) {   
    super.setControlPoint(point, index);
    
    // invalidate cached areas
    this.firstLeadArea = null;
    this.secondLeadArea = null;
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}  
  
  // hard-core math below :)

  public void recalculate(Path2D path, float thickness) {
    PathIterator iterator = path.getPathIterator(null);
    float[] coords = new float[6];
    Point current = new Point();

    // convert all segments to cubic curves
    List<CubicCurve2D> curves = new ArrayList<CubicCurve2D>();
    while (!iterator.isDone()) {
      int type = iterator.currentSegment(coords);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          current.setLocation(coords[0], coords[1]);
          break;
        case PathIterator.SEG_LINETO:
          curves.add(new CubicCurve2D.Double(current.x, current.y, (current.x + coords[0]) / 2,
              (current.y + coords[1]) / 2, (current.x + coords[0]) / 2, (current.y + coords[1]) / 2, coords[0],
              coords[1]));
          break;
        case PathIterator.SEG_QUADTO:
          curves.add(new CubicCurve2D.Double(current.x, current.y, coords[0], coords[1], coords[0], coords[1],
              coords[2], coords[3]));
          break;
        case PathIterator.SEG_CUBICTO:
          curves.add(new CubicCurve2D.Double(current.x, current.y, coords[0], coords[1], coords[2], coords[3],
              coords[4], coords[5]));
          break;
      }
      iterator.next();
    }

    Stroke stroke = ObjectCache.getInstance().fetchBasicStroke(thickness);

    double segmentLength = thickness * 7;

    // Convert to polygon
    List<Line2D> polygon = new ArrayList<Line2D>();
    for (CubicCurve2D curve : curves)
      polygon.addAll(split(curve, segmentLength));

    firstLeadArea = new Area();
    secondLeadArea = new Area();

    List<Path2D> firstCurves = new ArrayList<Path2D>();
    List<Path2D> secondCurves = new ArrayList<Path2D>();

    Path2D current1 = new Path2D.Double();
    Path2D current2 = new Path2D.Double();
    double offset = thickness * 1.5;
    double rectSize = thickness * 4;

    current1.moveTo(polygon.get(0).getX1(), polygon.get(0).getY1());
    current2.moveTo(polygon.get(0).getX1(), polygon.get(0).getY1());

    for (int i = 0; i < polygon.size(); i++) {
      Line2D line = polygon.get(i);
      double centerX = (line.getX1() + line.getX2()) / 2;
      double centerY = (line.getY1() + line.getY2()) / 2;
      double theta = Math.atan2(line.getY2() - line.getY1(), line.getX2() - line.getX1());

      double sign = i % 2 == 0 ? 1 : -1;

      double theta1 = theta - sign * Math.PI / 2;
      double theta2 = theta + sign * Math.PI / 2;

      current1.quadTo(centerX + offset * Math.cos(theta1),
          centerY + offset * Math.sin(theta1), line.getX2(), line.getY2());
      current2.quadTo(centerX + offset * Math.cos(theta2),
          centerY + offset * Math.sin(theta2), line.getX2(), line.getY2());

      if (i % 2 == 0 || i == polygon.size() - 1) {
        firstCurves.add(current1);
        current1 = new Path2D.Double();
        current1.moveTo(line.getX2(), line.getY2());
      }
      if (i % 2 == 1 || i == polygon.size() - 1) {
        secondCurves.add(current2);
        current2 = new Path2D.Double();
        current2.moveTo(line.getX2(), line.getY2());
      }
    }

    for (Path2D p : firstCurves) {
      firstLeadArea.add(new Area(stroke.createStrokedShape(p)));
    }
    for (Path2D p : secondCurves) {
      secondLeadArea.add(new Area(stroke.createStrokedShape(p)));
    }

    for (int i = 0; i < polygon.size(); i++) {
      Line2D line = polygon.get(i);
      Area pointRect1 = new Area(new Rectangle2D.Double(line.getX1() - rectSize / 2, line.getY1() - rectSize / 2, rectSize, rectSize));

      if (i % 2 == 1) {
        pointRect1.intersect(firstLeadArea);
        secondLeadArea.subtract(pointRect1);        
      } else {
        pointRect1.intersect(secondLeadArea);
        firstLeadArea.subtract(pointRect1);        
      }
      
      if (i == polygon.size() - 1) {
        Area pointRect2 = new Area(new Rectangle2D.Double(line.getX2() - rectSize / 2, line.getY2() - rectSize / 2, rectSize, rectSize));
        if (i % 2 == 0) {
          pointRect2.intersect(firstLeadArea);
          secondLeadArea.subtract(pointRect2);        
        } else {
          pointRect2.intersect(secondLeadArea);
          firstLeadArea.subtract(pointRect2);        
        }
      }
    }
  }

  private List<Line2D> split(CubicCurve2D curve, double segmentLength) {
    Point2D p1 = curve.getP1();
    Point2D p2 = curve.getP2();
    List<Line2D> res = new ArrayList<Line2D>();
    
    double length = Double.MAX_VALUE;
    
    if (p1.distance(p2) <= segmentLength)
      length = calculateLength(curve, segmentLength / 10);
    
    if (length <= segmentLength) {
      res.add(new Line2D.Double(p1, p2));
    } else {
      CubicCurve2D left = new CubicCurve2D.Double();
      CubicCurve2D right = new CubicCurve2D.Double();
      curve.subdivide(left, right);
      res.addAll(split(left, segmentLength));
      res.addAll(split(right, segmentLength));
    }
    return res;
  }
  
  private double calculateLength(CubicCurve2D curve, double precision) {
    Point2D p1 = curve.getP1();
    Point2D p2 = curve.getP2();
    
    double d = p1.distance(p2); 
    if (d <= precision) {
      return d;
    }  
    
    CubicCurve2D left = new CubicCurve2D.Double();
    CubicCurve2D right = new CubicCurve2D.Double();
    curve.subdivide(left, right);
    
    return calculateLength(left, precision) + calculateLength(right, precision);
  }
}
