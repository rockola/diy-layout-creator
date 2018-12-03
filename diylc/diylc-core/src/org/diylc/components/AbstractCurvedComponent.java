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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.CubicCurve2D;

import org.diylc.common.ObjectCache;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

public abstract class AbstractCurvedComponent<T> extends AbstractTransparentComponent<T> {

  private static final long serialVersionUID = 1L;

  public static Color GUIDELINE_COLOR = Color.blue;
  public static Size DEFAULT_SIZE = new Size(1d, SizeUnit.in);

  protected Point[] controlPoints = new Point[] {new Point(0, 0),
      new Point((int) (DEFAULT_SIZE.convertToPixels() / 2), 0),
      new Point((int) (DEFAULT_SIZE.convertToPixels() / 2), (int) (DEFAULT_SIZE.convertToPixels())),
      new Point((int) DEFAULT_SIZE.convertToPixels(), (int) DEFAULT_SIZE.convertToPixels())};

  protected Color color = getDefaultColor();
  protected PointCount pointCount = PointCount.FOUR;

  /**
   * Draws the specified curve onto graphics.
   * 
   * @param curve
   * @param g2d
   * @param componentState
   * @param theme
   */
  protected abstract void drawCurve(CubicCurve2D curve, Graphics2D g2d, ComponentState componentState);

  /**
   * @return default color.
   */
  protected abstract Color getDefaultColor();

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(getDefaultColor().darker());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3));
    CubicCurve2D curve =
        new CubicCurve2D.Double(1, height - 1, width / 4, height / 3, 3 * width / 4, 2 * height / 3, width - 1, 1);
    g2d.draw(curve);
    g2d.setColor(getDefaultColor());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(curve);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    if (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING) {
      // Do not track guidelines.
      drawingObserver.stopTracking();
      g2d.setStroke(Constants.DASHED_STROKE);
      g2d.setColor(GUIDELINE_COLOR);
      g2d.drawLine(controlPoints[0].x, controlPoints[0].y, controlPoints[1].x, controlPoints[1].y);
      g2d.drawLine(controlPoints[1].x, controlPoints[1].y, controlPoints[2].x, controlPoints[2].y);
      g2d.drawLine(controlPoints[2].x, controlPoints[2].y, controlPoints[3].x, controlPoints[3].y);
      drawingObserver.startTracking();
    }
    CubicCurve2D curve =
        new CubicCurve2D.Double(controlPoints[0].x, controlPoints[0].y, controlPoints[1].x, controlPoints[1].y,
            controlPoints[2].x, controlPoints[2].y, controlPoints[3].x, controlPoints[3].y);

    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    // g2d.setColor(color.darker());
    // g2d.setStroke(new BasicStroke(thickness));
    // g2d.draw(path);
    // g2d.setColor(color);
    // g2d.setStroke(new BasicStroke(thickness - 2));
    // g2d.draw(path);
    drawCurve(curve, g2d, componentState);
    g2d.setComposite(oldComposite);
  }

  @EditableProperty(name = "Point Count")
  public PointCount getPointCount() {
    if (pointCount == null) {
      pointCount = PointCount.FOUR;
    }
    return pointCount;
  }

  public void setPointCount(PointCount pointCount) {
    this.pointCount = pointCount;
    // Reset control points.
    for (int i = 0; i < getControlPointCount(); i++) {
      setControlPoint(getControlPoint(i), i);
    }
  }

  @Override
  public int getControlPointCount() {
    switch (getPointCount()) {
      case TWO:
        return 2;
      case THREE:
        return 3;
      case FOUR:
        return 4;
    }
    return 0;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return index == 0 || index == getControlPointCount() - 1;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public boolean canControlPointOverlap(int index) {
    // Only shape control points may overlap.
    switch (getPointCount()) {
      case TWO:
        return false;
      case THREE:
        return index == 1;
      case FOUR:
        return index == 1 || index == 2;
    }
    return false;
  }

  @Override
  public Point getControlPoint(int index) {
    if (index == 0) {
      return controlPoints[0];
    }
    switch (getPointCount()) {
      case TWO:
        switch (index) {
          case 1:
            return controlPoints[3];
        }
      case THREE:
        switch (index) {
          case 1:
            return controlPoints[1];
          case 2:
            return controlPoints[3];
        }
      case FOUR:
        switch (index) {
          case 1:
            return controlPoints[1];
          case 2:
            return controlPoints[2];
          case 3:
            return controlPoints[3];
        }
    }
    return controlPoints[index];
  }

  @Override
  public void setControlPoint(Point point, int index) {
    if (index == 0) {
      controlPoints[0].setLocation(point);
    } else {
      switch (getPointCount()) {
        case TWO:
          switch (index) {
            case 1:
              Point center = new Point((point.x + controlPoints[0].x) / 2, (point.y + controlPoints[0].y) / 2);
              controlPoints[1].setLocation(center);
              controlPoints[2].setLocation(center);
              controlPoints[3].setLocation(point);
              break;
          }
          break;
        case THREE:
          switch (index) {
            case 1:
              controlPoints[1].setLocation(point);
              controlPoints[2].setLocation(point);
              break;
            case 2:
              controlPoints[3].setLocation(point);
              break;
          }
          break;
        case FOUR:
          switch (index) {
            case 1:
              controlPoints[1].setLocation(point);
              break;
            case 2:
              controlPoints[2].setLocation(point);
              break;
            case 3:
              controlPoints[3].setLocation(point);
              break;
          }
          break;
      }
    }
  }

  @EditableProperty(name="Color")
  public Color getLeadColor() {
    return color;
  }

  public void setLeadColor(Color color) {
    this.color = color;
  }

  enum PointCount {
    TWO, THREE, FOUR;

    @Override
    public String toString() {
      return name().substring(0, 1) + name().substring(1).toLowerCase();
    }
  }
}
