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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.components.Area;
import org.diylc.components.transform.CliffJackTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "Cliff 1/4\" Jack",
    category = "Electro-Mechanical",
    author = "Branislav Stojkovic",
    description = "Cliff-style closed panel mount 1/4\" phono jack",
    zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = "J",
    autoEdit = false,
    transformer = CliffJackTransformer.class)
public class CliffJack1_4 extends AbstractMultiPartComponent<String> {

  private static final long serialVersionUID = 1L;

  private static Size SPACING = new Size(0.3d, SizeUnit.in);
  private static Size PIN_WIDTH = new Size(0.1d, SizeUnit.in);
  private static Size PIN_THICKNESS = new Size(0.02d, SizeUnit.in);
  private static Color BODY_COLOR = Color.decode("#666666");
  private static Color NUT_COLOR = Color.decode("#999999");
  private static Color BORDER_COLOR = Color.black;
  private static Color LABEL_COLOR = Color.white;
  private static Size BODY_WIDTH = new Size(3 / 4d, SizeUnit.in);
  private static Size BODY_LENGTH = new Size(0.9d, SizeUnit.in);
  private static Size TAIL_LENGTH = new Size(0.1d, SizeUnit.in);

  private Point[] controlPoints = new Point[] {new Point(0, 0)};
  private JackType type = JackType.MONO;
  private Orientation orientation = Orientation.DEFAULT;
  private transient Area[] body;
  private String value = "";

  public CliffJack1_4() {
    super();
    updateControlPoints();
  }

  private void updateControlPoints() {
    // invalidate body shape
    body = null;
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;
    int spacing = (int) SPACING.convertToPixels();
    controlPoints = new Point[type == JackType.STEREO ? 6 : 4];

    controlPoints[0] = new Point(x, y);
    controlPoints[1] = new Point(x, y + 2 * spacing);
    controlPoints[2] = new Point(x + 2 * spacing, y);
    controlPoints[3] = new Point(x + 2 * spacing, y + 2 * spacing);
    if (type == JackType.STEREO) {
      controlPoints[4] = new Point(x + spacing, y);
      controlPoints[5] = new Point(x + spacing, y + 2 * spacing);
    }

    // Apply rotation if necessary
    double angle = orientation.getTheta();
    if (angle != 0) {
      AffineTransform rotation = AffineTransform.getRotateInstance(angle, x, y);
      for (int i = 1; i < controlPoints.length; i++) {
        rotation.transform(controlPoints[i], controlPoints[i]);
      }
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[5];

      // Create body.
      int bodyLength = (int) BODY_LENGTH.convertToPixels();
      int bodyWidth = (int) BODY_WIDTH.convertToPixels();
      int centerX = (controlPoints[0].x + controlPoints[3].x) / 2;
      int centerY = (controlPoints[0].y + controlPoints[3].y) / 2;
      body[0] = Area.centeredRect(centerX, centerY, bodyLength, bodyWidth);

      int tailLength = (int) TAIL_LENGTH.convertToPixels();
      body[1] = Area.roundRect(
          centerX - bodyLength / 2 - tailLength,
          centerY - bodyWidth / 4,
          tailLength * 2,
          bodyWidth / 2,
          tailLength);
      Area tailArea = new Area(body[1]);
      tailArea.subtract(new Area(body[0]));
      body[1] = tailArea;

      body[2] = Area.rect(
          centerX + bodyLength / 2,
          centerY - bodyWidth / 4,
          tailLength,
          bodyWidth / 2);

      body[3] = Area.rect(
          centerX + bodyLength / 2 + tailLength,
          centerY - bodyWidth / 4,
          tailLength,
          bodyWidth / 2);
      tailArea = new Area(body[3]);
      int radius = bodyLength / 2 + tailLength * 2;
      tailArea.intersect(Area.circle(centerX, centerY, radius * 2));
      body[3] = tailArea;

      // Apply rotation if necessary
      double angle = orientation.getTheta();
      if (angle != 0) {
        AffineTransform rotation = AffineTransform.getRotateInstance(angle, centerX, centerY);
        for (int i = 0; i < body.length; i++) {
          if (body[i] != null) {
            Area area = new Area(body[i]);
            area.transform(rotation);
            body[i] = area;
          }
        }
      }

      // Create pins.
      Area pins = new Area();
      int pinWidth = (int) PIN_WIDTH.convertToPixels();
      int pinThickness = (int) PIN_THICKNESS.convertToPixels();
      for (int i = 0; i < getControlPointCount(); i++) {
        boolean flip = !(orientation == Orientation.DEFAULT || orientation == Orientation._180);
        int width = flip ? pinWidth : pinThickness;
        int thickness = flip ? pinThickness : pinWidth;
        pins.add(Area.centeredRect(getControlPoint(i), width, thickness));
      }

      body[4] = pins;
    }
    return body;
  }

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    Composite oldComposite = setTransparency(g2d);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
    for (int i = 0; i < body.length - 1; i++) {
      if (i == body.length - 2) {
        // Nut is brighter colored.
        g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : NUT_COLOR);
      }
      g2d.fill(body[i]);
    }
    g2d.setComposite(oldComposite);

    final Color finalBorderColor = tryBorderColor(outlineMode, BORDER_COLOR);
    g2d.setColor(finalBorderColor);
    for (int i = 0; i < body.length - 1; i++) {
      g2d.draw(body[i]);
    }

    // Pins are the last piece.
    Shape pins = body[body.length - 1];
    if (!outlineMode) {
      g2d.setColor(METAL_COLOR);
      g2d.fill(pins);
    }
    g2d.setColor(tryColor(outlineMode, METAL_COLOR.darker()));
    g2d.draw(pins);

    final Color finalLabelColor = tryLabelColor(outlineMode, LABEL_COLOR);
    g2d.setColor(finalLabelColor);
    g2d.setFont(project.getFont());
    int centerX = (controlPoints[0].x + controlPoints[3].x) / 2;
    int centerY = (controlPoints[0].y + controlPoints[3].y) / 2;
    StringUtils.drawCenteredText(
        g2d, name, centerX, centerY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    final int bodyWidth = getClosestOdd(width * 3 / 5);
    final int tailWidth = getClosestOdd(width * 3 / 9);
    final int w128 = 4 * 32 / width;

    // body
    Area.roundRect(
        (width - tailWidth) / 2,
        height / 2,
        tailWidth,
        height / 2 - 2 * 32 / height,
        w128).drawBordered(g2d, BODY_COLOR, BORDER_COLOR);

    // nut
    Area.roundRect(
        (width - tailWidth) / 2,
        2 * 32 / height,
        tailWidth,
        height / 2,
        w128).drawBordered(g2d, NUT_COLOR, BORDER_COLOR);

    // "area": what is this part?
    Area.rect(
        (width - bodyWidth) / 2,
        height / 7 + 1,
        bodyWidth,
        height * 5 / 7).drawBordered(g2d, BODY_COLOR, BORDER_COLOR);

    final int pinX1 = getClosestOdd((width - bodyWidth * 3 / 4) / 2);
    final int pinX2 = getClosestOdd((width + bodyWidth * 3 / 4) / 2) - 1;
    final int y14 = width * 2 / 8;
    final int y38 = width * 3 / 8;
    final int y58 = width * 5 / 8;
    final int y34 = width * 6 / 8;
    g2d.setColor(METAL_COLOR);
    g2d.drawLine(pinX1, y14, pinX1, y38);
    g2d.drawLine(pinX1, y58, pinX1, y34);
    g2d.drawLine(pinX2, y14, pinX2, y38);
    g2d.drawLine(pinX2, y58, pinX2, y34);
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @EditableProperty
  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public JackType getType() {
    return type;
  }

  public void setType(JackType type) {
    this.type = type;
    updateControlPoints();
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
