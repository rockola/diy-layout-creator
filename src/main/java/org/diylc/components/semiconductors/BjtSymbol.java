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

package org.diylc.components.semiconductors;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import org.diylc.common.ObjectCache;
import org.diylc.components.Abstract3LegSymbol;
import org.diylc.components.AbstractComponent;
import org.diylc.components.Area;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;

@ComponentDescriptor(
    name = "BJT",
    author = "Branislav Stojkovic",
    category = "Schematic Symbols",
    instanceNamePrefix = "Q",
    description = "Bipolar junction transistor schematic symbol",
    zOrder = AbstractComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_TAG_AND_VALUE,
    keywordTag = "Schematic")
public class BjtSymbol extends Abstract3LegSymbol {

  private static final long serialVersionUID = 1L;

  protected BjtPolarity polarity = BjtPolarity.NPN;

  public Area[] getBody() {
    Area[] body = new Area[3];
    Point[] controlPoints = getControlPoints();
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    int halfPinSpacing = (int) PIN_SPACING.convertToPixels() / 2;

    Path2D polyline = new Path2D.Double();
    polyline.moveTo(x + halfPinSpacing, y - pinSpacing);
    polyline.lineTo(x + halfPinSpacing, y + pinSpacing);
    body[0] = new Area(polyline);

    polyline = new Path2D.Double();
    polyline.moveTo(x, y);
    int x2 = x + halfPinSpacing;
    int x3 = x + pinSpacing * 2;
    polyline.lineTo(x2, y);
    polyline.moveTo(x2, y - halfPinSpacing);
    polyline.lineTo(x3, y - pinSpacing);
    polyline.lineTo(x3, y - pinSpacing * 2);
    polyline.moveTo(x2, y + halfPinSpacing);
    polyline.lineTo(x3, y + pinSpacing);
    polyline.lineTo(x3, y + pinSpacing * 2);
    body[1] = new Area(polyline);

    Area arrow;
    double theta = Math.atan(1.0 / 3);
    if (polarity == BjtPolarity.NPN) {
      arrow =
          new Area(
              new Polygon(
                  new int[] {x + pinSpacing, x + pinSpacing, x + pinSpacing * 10 / 6},
                  new int[] {
                    y - pinSpacing / 5 + halfPinSpacing,
                    y + pinSpacing / 5 + halfPinSpacing,
                    y + halfPinSpacing
                  },
                  3));
      arrow.transform(
          AffineTransform.getRotateInstance(theta, x + halfPinSpacing, y + halfPinSpacing));
    } else {
      theta = -theta;
      arrow =
          new Area(
              new Polygon(
                  new int[] {x + pinSpacing, x + pinSpacing * 10 / 6, x + pinSpacing * 10 / 6},
                  new int[] {
                    y - halfPinSpacing,
                    y - pinSpacing / 5 - halfPinSpacing,
                    y + pinSpacing / 5 - halfPinSpacing
                  },
                  3));
      arrow.transform(
          AffineTransform.getRotateInstance(theta, x + halfPinSpacing, y - halfPinSpacing));
    }
    body[2] = arrow;

    return body;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    final int x13 = width / 3;
    final int x18 = width / 8;
    final int x34 = width * 3 / 4;
    final int y12 = height / 2;
    final int y13 = height / 3;
    final int y14 = height / 4;
    final int y15 = height / 5;
    final int y23 = height * 2 / 3;
    final int y34 = height * 3 / 4;
    final int y45 = height * 4 / 5;

    g2d.setColor(COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    g2d.drawLine(x13, y15, x13, y45);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.drawLine(x18, y12, x13, y12);
    g2d.drawLine(x34, 1, x34, y14);
    g2d.drawLine(x13, y13 + 1, x34, y14);
    g2d.drawLine(x34, height - 1, x34, y34);
    g2d.drawLine(x13, y23 - 1, x34, y34);
  }

  @EditableProperty(name = "Polarity")
  public BjtPolarity getPolarity() {
    return polarity;
  }

  public void setPolarity(BjtPolarity polarity) {
    this.polarity = polarity;

    body = null;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return getName() + "." + index;
  }
}
