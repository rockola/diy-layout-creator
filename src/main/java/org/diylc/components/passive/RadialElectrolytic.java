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
import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractComponent;
import org.diylc.components.Area;
import org.diylc.components.LeadType;
import org.diylc.core.CreationMethod;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.ComponentValue;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.SiUnit;
import org.diylc.core.measures.Size;

@ComponentValue(SiUnit.FARAD)
@ComponentDescriptor(
    name = "Electrolytic Capacitor (Radial)",
    author = "Branislav Stojkovic",
    category = "Passive",
    creationMethod = CreationMethod.POINT_BY_POINT,
    instanceNamePrefix = "C",
    description = "Vertically mounted electrolytic capacitor, polarized or bipolar",
    leadType = LeadType.RADIAL,
    zOrder = AbstractComponent.COMPONENT,
    transformer = SimpleComponentTransformer.class)
public class RadialElectrolytic extends ElectrolyticCapacitor {

  private static final long serialVersionUID = 1L;

  public static final Size DEFAULT_SIZE = Size.in(0.25);
  public static final Size HEIGHT = Size.in(0.4);
  public static final Size EDGE_RADIUS = Size.mm(1);

  private boolean folded = false;
  private Size height = HEIGHT;
  private boolean invert = false;

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BODY_COLOR);
    int margin = 3;
    int diameter = getClosestOdd(width - 2 * margin);
    int x = margin + (width - margin) / 2;
    int y = margin + (height - margin) / 2;
    Area body = Area.circle(x, y, diameter);
    g2d.fill(body);
    Area marker = new Area(body);
    marker.subtract(
        Area.rect(margin, margin, width - 4 * margin, getClosestOdd(width - 2 * margin)));
    g2d.setColor(MARKER_COLOR);
    g2d.fill(marker);
    g2d.setColor(TICK_COLOR);
    g2d.drawLine(width - 2 * margin, height / 2 - 2, width - 2 * margin, height / 2 + 2);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(body);
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    int height = (int) getHeight().convertToPixels();
    if (polarized) {
      int totalDiameter = getClosestOdd(getLength().convertToPixels());
      if (!outlineMode) {
        Area area = new Area(getBodyShape());
        area.subtract(
            Area.rect(
                invert ? totalDiameter * 0.2 : 0,
                folded ? -height : 0,
                invert ? totalDiameter * 0.8 : totalDiameter,
                folded ? height * 2 : totalDiameter));
        g2d.setColor(markerColor);
        g2d.fill(area);
      }
      Color finalTickColor = tryColor(outlineMode, tickColor);
      g2d.setColor(finalTickColor);
      g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(1));
      if (folded) {
        int tickLength = height / 7;
        for (int i = 0; i < 3; i++) {
          int x = (int) (totalDiameter * (invert ? 0.08 : 0.92));
          int y1 = -height / 2 + tickLength + i * tickLength * 2;
          int y2 = y1 + tickLength;
          g2d.drawLine(x, y1, x, y2);
        }
      } else {
        int x = (int) (totalDiameter * (invert ? 0.1 : 0.9));
        int y1 = totalDiameter / 2 - (int) (totalDiameter * 0.06);
        int y2 = totalDiameter / 2 + (int) (totalDiameter * 0.06);
        g2d.drawLine(x, y1, x, y2);
      }
    }
  }

  @Override
  protected Size getDefaultWidth() {
    return null;
  }

  @Override
  public Size getWidth() {
    return super.getWidth();
  }

  @Override
  protected Size getDefaultLength() {
    // We'll reuse width property to set the diameter.
    return DEFAULT_SIZE;
  }

  @EditableProperty(name = "Diameter")
  @Override
  public Size getLength() {
    return super.getLength();
  }

  @EditableProperty
  public boolean getFolded() {
    return folded;
  }

  public void setFolded(boolean folded) {
    this.folded = folded;
  }

  @EditableProperty
  public Size getHeight() {
    if (height == null) {
      height = HEIGHT;
    }
    return height;
  }

  public void setHeight(Size height) {
    this.height = height;
  }

  @EditableProperty(name = "Invert polarity")
  public boolean getInvert() {
    return invert;
  }

  public void setInvert(boolean invert) {
    this.invert = invert;
  }

  @Override
  protected Area getBodyShape() {
    double height = (int) getHeight().convertToPixels();
    double diameter = (int) getLength().convertToPixels();
    Area body = null;
    if (folded) {
      body =
          Area.roundRect(
              0f,
              -height / 2 - LEAD_THICKNESS.convertToPixels() / 2,
              getClosestOdd(diameter),
              getClosestOdd(height),
              EDGE_RADIUS.convertToPixels());
    } else {
      body =
          new Area(new Ellipse2D.Double(0f, 0f, getClosestOdd(diameter), getClosestOdd(diameter)));
    }
    return body;
  }
}
