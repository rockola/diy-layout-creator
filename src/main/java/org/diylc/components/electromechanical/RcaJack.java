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

package org.diylc.components.electromechanical;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractComponent;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.components.Area;
import org.diylc.components.TwoCircleTangent;
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
    name = "RCA Jack",
    category = "Electro-Mechanical",
    author = "Branislav Stojkovic",
    description = "Panel mount RCA phono jack socket",
    zOrder = AbstractComponent.COMPONENT,
    instanceNamePrefix = "J")
public class RcaJack extends AbstractMultiPartComponent {

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = Color.lightGray;
  private static Color WAFER_COLOR = Color.black;
  private static Size BODY_DIAMETER = Size.in(0.52);
  private static Size WAFER_DIAMETER = Size.in(0.2);
  private static Size HEX_DIAMETER = Size.in(0.44);
  private static Size SPRING_LENGTH = Size.in(0.563);
  private static Size SPRING_WIDTH = Size.in(0.12);
  private static Size HOLE_DIAMETER = Size.in(0.05);
  private static Size HOLE_TO_EDGE = Size.in(0.063);

  transient Area[] body;
  private Orientation orientation = Orientation.DEFAULT;

  public RcaJack() {
    super();
    controlPoints = getFreshControlPoints(2);
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

    oldComposite = setTransparency(g2d);
    drawingObserver.startTrackingContinuityArea(true);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BASE_COLOR);
    g2d.fill(body[1]);
    g2d.fill(body[2]);
    drawingObserver.stopTrackingContinuityArea();

    if (body[3] != null) {
      g2d.fill(body[3]);
    }
    g2d.setComposite(oldComposite);

    finalBorderColor = tryBorderColor(outlineMode, BASE_COLOR.darker());
    g2d.setColor(finalBorderColor);
    g2d.draw(body[1]);
    g2d.draw(body[2]);
    if (body[3] != null) {
      g2d.draw(body[3]);
    }

    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[4];

      int x0 = controlPoints[0].x;
      int y0 = controlPoints[0].y;
      int x1 = controlPoints[1].x;
      int y1 = controlPoints[1].y;
      final int bodyDiameter = getClosestOdd(BODY_DIAMETER.convertToPixels());
      final int waferDiameter = getClosestOdd(WAFER_DIAMETER.convertToPixels());
      final int springWidth = (int) SPRING_WIDTH.convertToPixels();
      final int holeDiameter = getClosestOdd(HOLE_DIAMETER.convertToPixels());
      final double hexDiameter = HEX_DIAMETER.convertToPixels();

      body[0] = Area.ring(x0, y0, waferDiameter, holeDiameter);

      Area tip =
          new TwoCircleTangent(
              controlPoints[0], controlPoints[1], bodyDiameter / 2, springWidth / 2);
      tip.subtract(Area.circle(x1, y1, holeDiameter));
      tip.subtract(Area.circle(x0, y0, waferDiameter));
      body[1] = tip;

      Area sleeve = Area.ring(x0, y0, springWidth, holeDiameter);
      body[2] = sleeve;

      Path2D hex = new Path2D.Double();
      for (int i = 0; i < 6; i++) {
        double x = x0 + Math.cos(Math.PI / 3 * i) * hexDiameter / 2;
        double y = y0 + Math.sin(Math.PI / 3 * i) * hexDiameter / 2;
        if (i == 0) {
          hex.moveTo(x, y);
        } else {
          hex.lineTo(x, y);
        }
      }
      hex.closePath();
      Area hexArea = new Area(hex);
      hexArea.subtract(Area.circle(x0, y0, waferDiameter));
      body[3] = hexArea;
    }

    return body;
  }

  private void updateControlPoints() {
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;
    int springLength = (int) SPRING_LENGTH.convertToPixels();
    int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();
    int centerY = y + springLength - holeToEdge;

    controlPoints[1].setLocation(x, centerY);

    // Rotate if needed
    if (orientation != Orientation.DEFAULT) {
      AffineTransform rotation = orientation.getRotation(x, y);
      rotation.transform(controlPoints[1], controlPoints[1]);
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    double x0 = width * 0.35;
    double y0 = height * 0.65;
    double x1 = width * 0.75;
    double y1 = height * 0.25;
    TwoCircleTangent main =
        new TwoCircleTangent(
            new Point2D.Double(width * 0.35, height * 0.65),
            new Point2D.Double(x1, y1),
            width * 0.3,
            width * 0.1);
    main.subtract(new Area(new Ellipse2D.Double(x0 - 1, y0 - 1, 3, 3)));
    main.subtract(new Area(new Ellipse2D.Double(x1 - 1, y1 - 1, 2, 2)));

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    g2d.setColor(BASE_COLOR);
    g2d.fill(main);
    g2d.setColor(BASE_COLOR.darker());
    g2d.draw(main);

    double hexDiameter = width * 0.48;

    Path2D hex = new Path2D.Double();
    for (int i = 0; i < 6; i++) {
      double x = x0 + Math.cos(Math.PI / 3 * i) * hexDiameter / 2;
      double y = y0 + Math.sin(Math.PI / 3 * i) * hexDiameter / 2;
      if (i == 0) {
        hex.moveTo(x, y);
      } else {
        hex.lineTo(x, y);
      }
    }
    hex.closePath();
    g2d.draw(hex);

    double waferDiameter = width * 0.22;
    g2d.setColor(WAFER_COLOR);
    g2d.draw(
        new Ellipse2D.Double(
            x0 - waferDiameter / 2, y0 - waferDiameter / 2, waferDiameter, waferDiameter));
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Invalidate the body
    body = null;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return getName() + (index == 0 ? "Tip" : "Sleeve");
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
