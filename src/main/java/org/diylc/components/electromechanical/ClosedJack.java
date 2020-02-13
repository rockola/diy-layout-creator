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
import java.awt.geom.GeneralPath;
import org.diylc.awt.StringUtils;
import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractComponent;
import org.diylc.components.Area;
import org.diylc.components.transform.JackTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.StringValue;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@StringValue
@ComponentDescriptor(
    name = "Closed Jack",
    category = "Electro-Mechanical",
    author = "Branislav Stojkovic",
    description = "Enclosed panel mount phono jack",
    zOrder = AbstractComponent.COMPONENT,
    instanceNamePrefix = "J",
    xmlTag = "jack:closed",
    autoEdit = false,
    transformer = JackTransformer.class)
public class ClosedJack extends AbstractJack {

  private static final long serialVersionUID = 1L;

  private static Size SPACING = Size.in(0.1);
  private static Size LUG_WIDTH = Size.in(0.1);
  private static Size LUG_LENGTH = Size.in(0.12);
  private static Size LUG_HOLE_SIZE = Size.mm(1);
  private static Color BODY_COLOR = Color.decode("#666666");
  private static Color SHAFT_COLOR = Color.decode("#AAAAAA");
  private static Size SHAFT_LENGTH = Size.in(0.25);
  private static Size SHAFT_WIDTH = Size.in(0.375);
  private static Color BORDER_COLOR = Color.black;
  private static Color LABEL_COLOR = Color.white;
  private static Size BODY_WIDTH = Size.in(0.65);
  private static Size BODY_LENGTH = Size.in(0.8);
  private static JackSize JACK_SIZE = JackSize.QUARTER_INCH;

  {
    controlPoints = new Point[] {new Point(0, 0)};
  }

  public ClosedJack() {
    super();
    updateControlPoints();
  }

  @Override
  protected void updateControlPoints() {
    // invalidate body shape
    body = null;
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;
    int spacing = (int) SPACING.convertToPixels();
    int bodyLength = (int) BODY_LENGTH.convertToPixels();
    controlPoints = new Point[type.isStereo() ? 3 : 2];

    controlPoints[0] = new Point(x, y);
    controlPoints[1] = new Point(x + bodyLength, y);
    if (type.isStereo()) {
      controlPoints[2] = new Point(x, y + 2 * spacing);
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
      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int lugWidth = (int) LUG_WIDTH.convertToPixels();
      int lugLength = (int) LUG_LENGTH.convertToPixels();
      int lugHoleSize = (int) LUG_HOLE_SIZE.convertToPixels();
      int bodyLength = (int) BODY_LENGTH.convertToPixels();
      int bodyWidth = (int) BODY_WIDTH.convertToPixels();
      body[0] = Area.rect(x + lugLength, y - bodyWidth / 2, bodyLength, bodyWidth);

      int shaftLength = (int) SHAFT_LENGTH.convertToPixels();
      int shaftWidth = (int) SHAFT_WIDTH.convertToPixels();
      Area shaft =
          Area.rect(x + lugLength + bodyLength, y - shaftWidth / 2, shaftLength, shaftWidth);
      body[1] = shaft;

      AffineTransform rotation = null;
      double angle = orientation.getTheta();
      if (angle != 0) {
        rotation = AffineTransform.getRotateInstance(angle, x, y);
      }

      GeneralPath path = new GeneralPath();
      int step = 4;
      for (int i = x + lugLength + bodyLength + step;
          i <= x + lugLength + bodyLength + shaftLength;
          i += step) {
        Point p = new Point(i, y - shaftWidth / 2 + 1);
        if (rotation != null) {
          rotation.transform(p, p);
        }
        path.moveTo(p.x, p.y);
        p = new Point(i - step, y + shaftWidth / 2 - 1);
        if (rotation != null) {
          rotation.transform(p, p);
        }
        path.lineTo(p.x, p.y);
      }
      Area pathArea = new Area(path);
      pathArea.intersect(shaft);
      body[2] = new Area(path);

      // Create lugs.
      Area lugs = new Area();

      int spacing = (int) SPACING.convertToPixels();
      Point[] untransformedControlPoints = new Point[type.isStereo() ? 3 : 2];

      untransformedControlPoints[0] = new Point(x, y);
      untransformedControlPoints[1] = new Point(x + bodyLength, y);
      if (type.isStereo()) {
        untransformedControlPoints[2] = new Point(x, y + 2 * spacing);
      }

      for (int i = 0; i < untransformedControlPoints.length; i++) {
        Point point = untransformedControlPoints[i];
        Area lug = Area.circle(point, lugWidth);
        lug.add(Area.rect(point.x, point.y - lugWidth / 2, lugLength, lugWidth));
        lug.subtract(Area.circle(point, lugHoleSize));
        lugs.add(lug);
      }

      body[3] = lugs;

      if (rotation != null) {
        for (Area area : body) {
          if (area != null) {
            area.transform(rotation);
          }
        }
      }
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
    Area[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    Composite oldComposite = setTransparency(g2d);
    final Color border = tryBorderColor(outlineMode, BORDER_COLOR);
    body[0].fillDraw(g2d, outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR, border);
    body[1].fillDraw(g2d, outlineMode ? Constants.TRANSPARENT_COLOR : SHAFT_COLOR, border);
    if (!outlineMode) {
      body[2].fillDraw(g2d, SHAFT_COLOR, SHAFT_COLOR.darker());
    }
    // Pins
    body[3].fillDraw(
        g2d,
        outlineMode ? Constants.TRANSPARENT_COLOR : METAL_COLOR,
        outlineMode ? BORDER_COLOR : METAL_COLOR.darker());
    g2d.setComposite(oldComposite);

    g2d.setColor(tryLabelColor(outlineMode, LABEL_COLOR));
    g2d.setFont(project.getFont());
    StringUtils.drawCenteredText(g2d, getName(), body[0].getBounds());
    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int bodyWidth = getClosestOdd(width * 3 / 5);
    int tailWidth = getClosestOdd(width * 3 / 10);

    g2d.setColor(SHAFT_COLOR);
    g2d.fillRect((width - tailWidth) / 2, 1, tailWidth, height / 2);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect((width - tailWidth) / 2, 1, tailWidth, height / 2);

    g2d.setColor(BODY_COLOR);
    g2d.fillRect((width - bodyWidth) / 2, height / 7 + 1, bodyWidth, height * 5 / 7);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect((width - bodyWidth) / 2, height / 7 + 1, bodyWidth, height * 5 / 7);

    g2d.setColor(METAL_COLOR);

    g2d.fillRect(width * 7 / 16, height * 6 / 7 + 1, width / 8, height / 7 - 1);
    g2d.fillRect(width * 7 / 16, height / 7 + 2, width / 8, height / 7 - 1);
  }
}
