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

package org.diylc.components.guitar;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import org.diylc.common.ObjectCache;
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
    name = "P-Bass Pickup",
    category = "Guitar",
    author = "Branislav Stojkovic",
    description = "Split-coil pickup for P-Bass and similar guitars",
    zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = AbstractGuitarPickup.INSTANCE_NAME_PREFIX,
    autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Guitar Wiring Diagram")
public class PrecisionBassPickup extends AbstractBassPickup {

  private static final long serialVersionUID = 1L;

  private static Size WIDTH = Size.in(1.1);
  private static Size LENGTH = Size.in(2.2);
  private static Size POLE_SPACING = Size.in(0.38);

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
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : color);
    g2d.fill(body[0]);
    g2d.fill(body[1]);
    g2d.setComposite(oldComposite);

    Color finalBorderColor = tryBorderColor(outlineMode, color.darker());
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
  public Area[] getBody() {
    if (body == null) {
      body = new Area[4];

      Point[] points = getControlPoints();
      int x = points[0].x;
      int y = points[0].y;
      int width = (int) WIDTH.convertToPixels();
      int length = (int) LENGTH.convertToPixels();
      int edgeRadius = (int) EDGE_RADIUS.convertToPixels();
      int pointMargin = (int) POINT_MARGIN.convertToPixels();
      body[0] = Area.roundRect(x - length, y - pointMargin, length, width, edgeRadius);

      int lipRadius = (int) LIP_RADIUS.convertToPixels();
      int lipHoleSize = getClosestOdd(LIP_HOLE_SIZE.convertToPixels());
      int lipHoleSpacing = getClosestOdd(LIP_HOLE_SPACING.convertToPixels());
      Area lip = Area.circle(0, 0, lipRadius).subtract(Area.circle(lipHoleSpacing, 0, lipHoleSize));
      Area lip2 = new Area(lip);
      int lipY = y - pointMargin + width / 2;
      lip.transform(AffineTransform.getTranslateInstance(x, lipY));
      lip2.transform(AffineTransform.getTranslateInstance(x - length, lipY));
      lip.add(lip2);
      body[1] = new Area(lip);
      body[1].subtract(body[0]);

      int pointSize = getClosestOdd(POINT_SIZE.convertToPixels());
      body[2] = Area.circle(x, y, pointSize);

      int poleSize = (int) POLE_SIZE.convertToPixels();
      int poleSpacing = (int) POLE_SPACING.convertToPixels();
      int poleMargin = (length - poleSpacing * 3) / 2;
      Area poleArea = new Area();
      for (int i = 0; i < getNumberOfStrings(); i++) {
        int poleX = x - length + poleMargin + i * poleSpacing;
        int poleY = y - pointMargin + width / 2;
        poleArea.add(Area.circle(poleX, poleY, poleSize));
      }
      body[3] = poleArea;

      // Rotate if needed
      if (orientation.isRotated()) {
        AffineTransform rotation = orientation.getRotation(x, y);
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
  protected int getMainLabelYOffset() {
    return (int) (WIDTH.convertToPixels() / 2 - 20);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(Math.PI / 4, width / 2, height / 2);

    int bodyWidth = (int) (13f * width / 32);
    int bodyLength = (int) (30f * width / 32);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect(
        (width - bodyWidth) / 2, (height - bodyLength) / 2, bodyWidth, bodyLength, 3, 3);

    int lipSize = (int) (5f * width / 32);

    g2d.fillRoundRect(
        (width - lipSize) / 2,
        (height - bodyLength) / 2 - lipSize + 1,
        lipSize,
        bodyLength + 2 * lipSize - 2,
        lipSize,
        lipSize);

    g2d.setColor(BODY_COLOR.darker());
    g2d.drawRoundRect(
        (width - bodyWidth) / 2, (height - bodyLength) / 2, bodyWidth, bodyLength, 3, 3);

    g2d.setColor(METAL_COLOR);
    int poleSize = 2;
    int poleSpacing = (int) (15d * width / 32);
    for (int i = 0; i < 4; i++) {
      g2d.fillOval(
          (width - poleSize) / 2,
          (height - poleSpacing) / 2 + (i * poleSpacing / 3),
          poleSize,
          poleSize);
    }
  }

  @Override
  protected OrientationHV getControlPointDirection() {
    return OrientationHV.VERTICAL;
  }
}
