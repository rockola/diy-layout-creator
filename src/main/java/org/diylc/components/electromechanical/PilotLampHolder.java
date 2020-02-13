/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2018 held jointly by the individual authors.

  This file is part of DIYLC.

  DIYLC is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  DIYLC is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
  License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc.components.electromechanical;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractComponent;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.StringValue;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@StringValue
@ComponentDescriptor(
    name = "Pilot Lamp Holder",
    category = "Electro-Mechanical",
    author = "Branislav Stojkovic",
    description = "Fender-style pilot bulb holder for T2 and T-3 ¼\" miniature bayonet lamps",
    zOrder = AbstractComponent.COMPONENT,
    instanceNamePrefix = "PL")
public class PilotLampHolder extends AbstractMultiPartComponent {

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = Color.lightGray;
  private static Color WAFER_COLOR = Color.decode("#CD8500");

  private static final double SLEEVE_THETA = Math.PI * 0.29444444444;

  private static Size THREAD_OUTER_DIAMETER = Size.in(0.6875);
  private static Size NUT_DIAMETER = Size.in(0.875);
  private static Size THREAD_THICKNESS = Size.in(0.05);
  private static Size WAFER_DIAMETER = Size.in(0.2);
  private static Size INNER_DIAMETER = Size.in(0.05);
  private static Size RING_DIAMETER = Size.in(0.15);
  private static Size SPRING_LENGTH = Size.in(0.463);
  private static Size SPRING_WIDTH = Size.in(0.12);
  private static Size HOLE_DIAMETER = Size.in(0.05);
  private static Size HOLE_TO_EDGE = Size.in(0.063);
  private static Size HOLE_SPACING = Size.in(0.1);

  transient Area[] body;
  @Deprecated private Orientation orientation = Orientation.DEFAULT;
  private Integer angle = 0;

  public PilotLampHolder() {
    super();
    controlPoints = getFreshControlPoints(4);
    updateControlPoints();
  }

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    Area[] body = getBody();

    Composite oldComposite = setTransparency(g2d);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : WAFER_COLOR);
    g2d.fill(body[0]);
    g2d.setComposite(oldComposite);

    Color finalBorderColor = tryBorderColor(outlineMode, WAFER_COLOR.darker());
    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);

    drawingObserver.startTrackingContinuityArea(true);
    oldComposite = setTransparency(g2d);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BASE_COLOR);
    g2d.fill(body[3]);
    g2d.fill(body[4]);
    g2d.fill(body[5]);
    g2d.fill(body[1]);
    g2d.fill(body[2]);
    g2d.setComposite(oldComposite);
    drawingObserver.stopTrackingContinuityArea();

    finalBorderColor = tryBorderColor(outlineMode, BASE_COLOR.darker());
    g2d.setColor(finalBorderColor);
    g2d.draw(body[3]);
    g2d.draw(body[4]);
    g2d.draw(body[5]);
    g2d.draw(body[1]);
    g2d.draw(body[2]);

    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[6];

      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      final int threadOuterDiameter = getClosestOdd(THREAD_OUTER_DIAMETER.convertToPixels());
      final int threadThickness = getClosestOdd(THREAD_THICKNESS.convertToPixels());
      final int nutDiameter = getClosestOdd(NUT_DIAMETER.convertToPixels());
      final int waferDiameter = getClosestOdd(WAFER_DIAMETER.convertToPixels());
      final int innerDiameter = getClosestOdd(INNER_DIAMETER.convertToPixels());
      final int ringDiameter = getClosestOdd(RING_DIAMETER.convertToPixels());
      final int springLength = (int) SPRING_LENGTH.convertToPixels();
      final int springWidth = (int) SPRING_WIDTH.convertToPixels();
      final int holeDiameter = getClosestOdd(HOLE_DIAMETER.convertToPixels());
      final int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();
      int centerY = y + springLength - holeToEdge;

      Area wafer = Area.ring(x, centerY, waferDiameter, ringDiameter);
      body[0] = wafer;

      Area tip =
          Area.roundRect(
              x - springWidth / 2, y - holeToEdge, springWidth, springLength, springWidth);
      Area sleeve = new Area(tip);

      tip.subtract(Area.circle(x, centerY, waferDiameter));
      body[1] = tip;

      sleeve.transform(AffineTransform.getRotateInstance(SLEEVE_THETA, x, centerY));
      sleeve.add(Area.ring(x, centerY, ringDiameter, innerDiameter));
      body[2] = sleeve;

      tip.subtract(sleeve);

      Area thread = Area.circle(x, centerY, threadOuterDiameter);
      Path2D polygon = new Path2D.Double();
      for (int i = 0; i < 6; i++) {
        double theta = Math.PI / 3 * i;
        if (i == 0) {
          polygon.moveTo(
              x + nutDiameter / 2 * Math.cos(theta), centerY + nutDiameter / 2 * Math.sin(theta));
        } else {
          polygon.lineTo(
              x + nutDiameter / 2 * Math.cos(theta), centerY + nutDiameter / 2 * Math.sin(theta));
        }
      }
      polygon.closePath();
      Area nut = new Area(polygon);
      nut.subtract(thread);
      nut.subtract(Area.rect(x - springWidth / 2, y - holeToEdge, springWidth, springLength));
      nut.subtract(sleeve);

      thread.subtract(
          new Area(
              new Ellipse2D.Double(
                  x - threadOuterDiameter / 2 + threadThickness,
                  centerY - threadOuterDiameter / 2 + threadThickness,
                  threadOuterDiameter - 2 * threadThickness,
                  threadOuterDiameter - 2 * threadThickness)));
      thread.subtract(tip);
      thread.subtract(sleeve);

      body[3] = thread;
      body[4] = nut;

      double linkLength = (int) (Math.sin(Math.PI / 3) * nutDiameter / 2);
      Area link = Area.rect(x - waferDiameter / 2, centerY, waferDiameter, linkLength);
      link.subtract(Area.circle(x, centerY, waferDiameter));
      body[5] = link;

      nut.subtract(link);
      thread.subtract(link);

      if (getTheta() != 0) {
        AffineTransform rotation = AffineTransform.getRotateInstance(getTheta(), x, y);
        for (Area area : body) {
          if (area != null) {
            area.transform(rotation);
          }
        }
      }

      for (int i = 1; i <= 2; i++) {
        for (Point p : controlPoints) {
          body[i].subtract(Area.circle(p.x, p.y, holeDiameter));
        }
      }
    }

    return body;
  }

  private void updateControlPoints() {
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;

    int springLength = (int) SPRING_LENGTH.convertToPixels();
    int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();
    int holeSpacing = (int) HOLE_SPACING.convertToPixels();

    int centerY = y + springLength - holeToEdge;

    AffineTransform rotation = AffineTransform.getRotateInstance(Math.PI * 0.295, x, centerY);

    rotation.transform(controlPoints[0], controlPoints[1]);

    controlPoints[2].setLocation(controlPoints[0].x, controlPoints[0].y + holeSpacing);
    rotation.transform(controlPoints[2], controlPoints[3]);

    // Rotate if needed
    if (getTheta() != 0) {
      rotation = AffineTransform.getRotateInstance(getTheta(), x, y);
      for (Point point : controlPoints) {
        rotation.transform(point, point);
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int waferDiameter = (int) (7f * width / 32);
    int sleeveDiameter = (int) (7f * width / 32);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(6f * width / 32));
    g2d.setColor(BASE_COLOR);
    g2d.drawLine(width / 2, 4 * width / 32, width / 2, width / 4);

    int centerX = width / 2;
    int centerY = height / 2;
    g2d.setColor(WAFER_COLOR);
    g2d.draw(Area.circle(centerX, centerY, waferDiameter));

    g2d.setColor(BASE_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3f * width / 32));
    g2d.draw(Area.circle(centerX, centerY, sleeveDiameter));

    g2d.rotate(Math.PI * 0.295, width / 2, height / 2);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(6f * width / 32));
    g2d.drawLine(width / 2, 4 * width / 32, width / 2, width / 3);
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.controlPoints[index].setLocation(point);
    // Invalidate the body
    body = null;
  }

  @EditableProperty
  public Integer getAngle() {
    if (angle == null) {
      angle = (orientation == null) ? 0 : orientation.toInt();
    }
    return angle;
  }

  public void setAngle(Integer angle) {
    this.angle = angle;
    updateControlPoints();
    // Invalidate the body
    body = null;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  protected double getTheta() {
    return Math.toRadians(getAngle());
  }
}
