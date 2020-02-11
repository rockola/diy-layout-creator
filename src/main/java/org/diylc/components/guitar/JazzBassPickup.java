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

package org.diylc.components.guitar;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.OrientationHV;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "Jazz Bass Pickup",
    category = "Guitar",
    author = "Branislav Stojkovic",
    description = "Single coil pickup for Jazz Bass and similar guitars",
    zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = AbstractGuitarPickup.INSTANCE_NAME_PREFIX,
    autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Guitar Wiring Diagram")
public class JazzBassPickup extends AbstractBassPickup {

  private static final long serialVersionUID = 1L;

  private static Size WIDTH = Size.in(0.73);
  private static Size LENGTH = Size.in(4.1);
  private static Size LIP_SPACING = Size.in(1.92);
  private static Size POLE_SPACING = Size.in(0.85);
  private static Size POLE_SPACING_MINOR = Size.in(0.28);

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
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : color);
    g2d.fill(body[0]);
    g2d.fill(body[1]);
    g2d.setComposite(oldComposite);

    final Color finalBorderColor = tryBorderColor(outlineMode, color.darker());
    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);
    g2d.draw(body[1]);

    if (!outlineMode) {
      g2d.setColor(getPoleColor());
      g2d.fill(body[3]);
      g2d.setColor(darkerOrLighter(getPoleColor()));
      g2d.draw(body[3]);
    }
    drawMainLabel(g2d, project, outlineMode, componentState);
    drawTerminalLabels(g2d, finalBorderColor, project);
  }

  @Override
  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[4];

      Point[] points = getControlPoints();
      int x = points[0].x;
      int y = points[0].y;
      int width = (int) WIDTH.convertToPixels();
      int length = (int) LENGTH.convertToPixels();
      final int edgeRadius = (int) EDGE_RADIUS.convertToPixels();
      final int pointMargin = (int) POINT_MARGIN.convertToPixels();
      final int lipRadius = (int) LIP_RADIUS.convertToPixels();
      final int lipSpacing = (int) LIP_SPACING.convertToPixels();
      final int pointSize = getClosestOdd(POINT_SIZE.convertToPixels());
      final int lipHoleSize = getClosestOdd(LIP_HOLE_SIZE.convertToPixels());
      final int lipHoleSpacing = getClosestOdd(LIP_HOLE_SPACING.convertToPixels());

      body[0] =
          new Area(
              new RoundRectangle2D.Double(
                  x - length, y - pointMargin, length, width, edgeRadius, edgeRadius));

      Area lip =
          new Area(new Ellipse2D.Double(-lipRadius / 2, -lipRadius / 2, lipRadius, lipRadius));
      lip.subtract(
          new Area(
              new Ellipse2D.Double(
                  -lipHoleSize / 2, -lipHoleSpacing - lipHoleSize / 2, lipHoleSize, lipHoleSize)));
      lip.transform(
          AffineTransform.getTranslateInstance(x - length / 2 - lipSpacing / 2, y - pointMargin));

      body[1] = new Area(lip);
      lip.transform(AffineTransform.getTranslateInstance(lipSpacing, 0));
      ((Area) body[1]).add(lip);
      lip = new Area(body[1]);
      double y1 = lip.getBounds2D().getY();
      lip.transform(AffineTransform.getScaleInstance(1, -1));
      lip.transform(AffineTransform.getTranslateInstance(0, 2 * y1 + width + lipRadius));
      ((Area) body[1]).add(lip);

      ((Area) body[1]).subtract((Area) body[0]);

      body[2] =
          new Area(
              new Ellipse2D.Double(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize));

      int poleSize = (int) POLE_SIZE.convertToPixels();
      int poleSpacing = (int) POLE_SPACING.convertToPixels();
      int poleSpacingMinor = (int) POLE_SPACING_MINOR.convertToPixels();
      int poleMargin = (length - poleSpacing * 3) / 2;
      Area poleArea = new Area();
      for (int i = 0; i < getNumberOfStrings(); i++) {
        Ellipse2D pole =
            new Ellipse2D.Double(
                x - length + poleMargin + i * poleSpacing - poleSize / 2 - poleSpacingMinor / 2,
                y - pointMargin - poleSize / 2 + width / 2,
                poleSize,
                poleSize);
        poleArea.add(new Area(pole));
        pole =
            new Ellipse2D.Double(
                x - length + poleMargin + i * poleSpacing - poleSize / 2 + poleSpacingMinor / 2,
                y - pointMargin - poleSize / 2 + width / 2,
                poleSize,
                poleSize);
        poleArea.add(new Area(pole));
      }
      body[3] = poleArea;

      // Rotate if needed
      if (orientation != Orientation.DEFAULT) {
        AffineTransform rotation = orientation.getRotation(x, y);
        for (Shape shape : body) {
          Area area = (Area) shape;
          if (shape != null) {
            area.transform(rotation);
          }
        }
      }
    }
    return body;
  }

  @Override
  protected int getMainLabelYOffset() {
    return (int) (WIDTH.convertToPixels() / 2 - 20);
  }

  @Override
  protected OrientationHV getControlPointDirection() {
    return OrientationHV.VERTICAL;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    final int x = width / 2;
    final int y = height / 2;
    g2d.rotate(Math.PI / 4, width / 2, height / 2);

    int bodyWidth = (int) (5f * width / 32);
    int bodyLength = (int) (30f * width / 32);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    g2d.setColor(BODY_COLOR);
    g2d.fill(Area.centeredRoundRect(x, y, bodyWidth, bodyLength, 3));

    // lipSpacing = 1/2 * distance between lips
    int lipSpacing = (int) (7f * width / 32);
    int lipSize = (int) (3f * width / 32);
    g2d.fill(
        Area.centeredRoundRect(
            x - lipSize / 2, y - lipSpacing, bodyWidth + 2 * lipSize, lipSize, lipSize));
    g2d.fill(
        Area.centeredRoundRect(
            x - lipSize / 2, y + lipSpacing, bodyWidth + 2 * lipSize, lipSize, lipSize));

    g2d.setColor(BODY_COLOR.darker());
    g2d.drawRoundRect(
        (width - bodyWidth) / 2, (height - bodyLength) / 2, bodyWidth, bodyLength, 3, 3);

    g2d.setColor(METAL_COLOR);
    final int numberOfStrings = 4;
    int poleSize = 2;
    int poleSpacing = bodyLength / (numberOfStrings + 1);
    int poleY = y - poleSpacing * (numberOfStrings - 1) / 2;
    for (int i = 0; i < numberOfStrings; i++) {
      g2d.fill(Area.circle(x, poleY + i * poleSpacing + (poleSize / 2), poleSize));
      g2d.fill(Area.circle(x, poleY + i * poleSpacing - (poleSize / 2), poleSize));
    }
  }
}
