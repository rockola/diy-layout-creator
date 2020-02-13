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

package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractComponent;
import org.diylc.components.Area;
import org.diylc.components.LeadType;
import org.diylc.core.CreationMethod;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.ComponentValue;
import org.diylc.core.measures.SiUnit;
import org.diylc.core.measures.Size;

@ComponentValue(SiUnit.FARAD)
@ComponentDescriptor(
    name = "Electrolytic Capacitor (Axial)",
    author = "Branislav Stojkovic",
    category = "Passive",
    creationMethod = CreationMethod.POINT_BY_POINT,
    instanceNamePrefix = "C",
    description = "Axial electrolytic capacitor, similar to Sprague Atom, F&T, etc",
    leadType = LeadType.AXIAL,
    zOrder = AbstractComponent.COMPONENT,
    transformer = SimpleComponentTransformer.class)
public class AxialElectrolyticCapacitor extends ElectrolyticCapacitor {

  private static final long serialVersionUID = 1L;

  public static final Size DEFAULT_WIDTH = Size.in(.5);
  public static final Size DEFAULT_HEIGHT = Size.in(.125);

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR_ICON);
    g2d.drawLine(0, height / 2, width, height / 2);
    g2d.setColor(BODY_COLOR);
    g2d.fillRect(4, height / 2 - 3, width - 8, 6);
    g2d.setColor(MARKER_COLOR);
    g2d.fillRect(width - 9, height / 2 - 3, 5, 6);
    g2d.setColor(TICK_COLOR);
    g2d.drawLine(width - 6, height / 2 - 1, width - 6, height / 2 + 1);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(4, height / 2 - 3, width - 8, 6);
  }

  @Override
  protected Size getDefaultWidth() {
    return DEFAULT_HEIGHT;
  }

  @Override
  protected Size getDefaultLength() {
    return DEFAULT_WIDTH;
  }

  @Override
  protected Area getBodyShape() {
    double length = getLength().convertToPixels();
    double width = getWidth().convertToPixels();
    RoundRectangle2D rect =
        new RoundRectangle2D.Double(0f, 0f, length, width, width / 6, width / 6);
    Area a = new Area(rect);
    double notchDiameter = width / 4;
    a.subtract(
        new Area(
            new Ellipse2D.Double(
                notchDiameter, -notchDiameter * 3 / 4, notchDiameter, notchDiameter)));
    a.subtract(
        new Area(
            new Ellipse2D.Double(
                notchDiameter, width - notchDiameter / 4, notchDiameter, notchDiameter)));

    if (!getPolarized()) {
      a.subtract(
          new Area(
              new Ellipse2D.Double(
                  length - notchDiameter * 2,
                  -notchDiameter * 3 / 4,
                  notchDiameter,
                  notchDiameter)));
      a.subtract(
          new Area(
              new Ellipse2D.Double(
                  length - notchDiameter * 2,
                  width - notchDiameter / 4,
                  notchDiameter,
                  notchDiameter)));
    }
    return a;
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    int width = (int) getWidth().convertToPixels();
    int length = (int) getLength().convertToPixels();
    g2d.setColor(blend(getBorderColor(), getBodyColor()));
    int notchDiameter = width / 4;
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.drawLine(notchDiameter, 0, notchDiameter, width);
    g2d.drawLine(notchDiameter * 2, 0, notchDiameter * 2, width);
    if (polarized) {
      int markerLength = (int) (getLength().convertToPixels() * 0.2);
      if (!outlineMode) {
        g2d.setColor(markerColor);
        Rectangle2D markerRect =
            new Rectangle2D.Double(length - markerLength, 0, markerLength + 2, width);
        Area markerArea = new Area(markerRect);
        markerArea.intersect((Area) getBodyShape());
        g2d.fill(markerArea);
      }
      Color finalTickColor = tryColor(outlineMode, tickColor);
      g2d.setColor(finalTickColor);
      g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(2));
      g2d.drawLine(
          (int) getLength().convertToPixels() - markerLength / 2,
          (int) (width / 2 - width * 0.15),
          (int) getLength().convertToPixels() - markerLength / 2,
          (int) (width / 2 + width * 0.15));
    } else {
      g2d.drawLine(length - notchDiameter, 0, length - notchDiameter, width);
      g2d.drawLine(length - notchDiameter * 2, 0, length - notchDiameter * 2, width);
    }
  }

  public static Color blend(Color c0, Color c1) {
    double totalAlpha = c0.getAlpha() + c1.getAlpha();
    double weight0 = c0.getAlpha() / totalAlpha;
    double weight1 = c1.getAlpha() / totalAlpha;

    double r = weight0 * c0.getRed() + weight1 * c1.getRed();
    double g = weight0 * c0.getGreen() + weight1 * c1.getGreen();
    double b = weight0 * c0.getBlue() + weight1 * c1.getBlue();
    double a = Math.max(c0.getAlpha(), c1.getAlpha());

    return new Color((int) r, (int) g, (int) b, (int) a);
  }
}
