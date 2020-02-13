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

package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
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
    name = "Tantalum Capacitor",
    author = "Branislav Stojkovic",
    category = "Passive",
    creationMethod = CreationMethod.POINT_BY_POINT,
    instanceNamePrefix = "C",
    leadType = LeadType.RADIAL,
    description = "Vertically mounted tantalum capacitor",
    zOrder = AbstractComponent.COMPONENT,
    transformer = SimpleComponentTransformer.class)
public class TantalumCapacitor extends AbstractCapacitor {

  private static final long serialVersionUID = 1L;

  public static final Size DEFAULT_SIZE = Size.in(1d / 4);
  public static final Color BODY_COLOR = Color.decode("#CFAD28");
  public static final Color BORDER_COLOR = BODY_COLOR.darker();
  public static final Color MARKER_COLOR = BODY_COLOR.darker();
  public static final Color TICK_COLOR = Color.white;
  public static final Size HEIGHT = Size.in(0.4);

  private Color markerColor = MARKER_COLOR;
  private Color tickColor = TICK_COLOR;
  private boolean folded = false;
  private Size height = HEIGHT;
  private boolean invert = false;

  public TantalumCapacitor() {
    super();
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
    this.labelColor = TICK_COLOR;
  }

  @Override
  public String getValueForDisplay() {
    return getValue().toString() + (getVoltage() == null ? "" : " " + getVoltage().toString());
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BODY_COLOR);
    int margin = 3;
    Ellipse2D body =
        new Ellipse2D.Double(
            margin, margin, getClosestOdd(width - 2 * margin), getClosestOdd(width - 2 * margin));
    g2d.fill(body);
    Area marker = new Area(body);
    marker.subtract(
        new Area(
            new Rectangle2D.Double(4 * margin, margin, width, getClosestOdd(width - 2 * margin))));
    g2d.setColor(MARKER_COLOR);
    g2d.fill(marker);
    g2d.setColor(TICK_COLOR);
    g2d.drawLine(2 * margin + 1, height / 2 - 2, 2 * margin + 1, height / 2 + 2);
    g2d.drawLine(2 * margin - 2 + 1, height / 2, 2 * margin + 2 + 1, height / 2);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(body);
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    int height = (int) getHeight().convertToPixels();

    int totalDiameter = getClosestOdd(getLength().convertToPixels());
    if (!outlineMode) {
      Area area = new Area(getBodyShape());
      if (folded) {
        if (!invert) {
          area.subtract(Area.rect(totalDiameter * 0.2, -height, totalDiameter, height * 2));
        } else {
          area.subtract(Area.rect(0, -height, totalDiameter * 0.8, height * 2));
        }
      } else {
        if (!invert) {
          area.subtract(Area.rect(totalDiameter * 0.2, 0, totalDiameter, totalDiameter));
        } else {
          area.subtract(Area.rect(0, 0, totalDiameter * 0.8, totalDiameter));
        }
      }
      area.fill(g2d, markerColor);
    }
    Color finalTickColor = tryColor(outlineMode, tickColor);
    g2d.setColor(finalTickColor);
    g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(1));
    int tickLength = (int) (totalDiameter * 0.12);
    int centerX = (int) (totalDiameter * (!invert ? 0.1 : 0.9));
    int centerY = folded ? (int) (-height / 2 + tickLength * 3.5) : totalDiameter / 2;
    g2d.drawLine(centerX - tickLength / 2, centerY, centerX + tickLength / 2, centerY);
    g2d.drawLine(centerX, centerY - tickLength / 2, centerX, centerY + tickLength / 2);
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

  @EditableProperty(name = "Marker")
  public Color getMarkerColor() {
    return markerColor;
  }

  public void setMarkerColor(Color coverColor) {
    this.markerColor = coverColor;
  }

  @EditableProperty(name = "Tick")
  public Color getTickColor() {
    return tickColor;
  }

  public void setTickColor(Color tickColor) {
    this.tickColor = tickColor;
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
              diameter / 2);
    } else {
      body =
          new Area(new Ellipse2D.Double(0f, 0f, getClosestOdd(diameter), getClosestOdd(diameter)));
    }
    return body;
  }
}
