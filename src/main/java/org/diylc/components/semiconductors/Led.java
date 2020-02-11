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

package org.diylc.components.semiconductors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.components.Area;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;

@ComponentDescriptor(
    name = "LED",
    author = "Branislav Stojkovic",
    category = "Semiconductors",
    creationMethod = CreationMethod.POINT_BY_POINT,
    instanceNamePrefix = "D",
    description = "Light Emitting Diode",
    zOrder = IDIYComponent.COMPONENT,
    transformer = SimpleComponentTransformer.class)
public class Led extends AbstractLeadedComponent<String> {

  private static final long serialVersionUID = 1L;

  public static final Size DEFAULT_SIZE = Size.mm(5);
  public static final Color BODY_COLOR = Color.decode("#5DFC0A");
  public static final Color BORDER_COLOR = BODY_COLOR.darker();

  private String value = "";

  public Led() {
    super();
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
  }

  @EditableProperty
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    Point center = Area.point(width / 2, height / 2);
    Area.rotate(g2d, -Math.PI / 4, center);
    g2d.setColor(LEAD_COLOR_ICON);
    g2d.drawLine(0, center.y, width, center.y);

    int margin = 4 * width / 32;
    Area area = new Area(new Ellipse2D.Double(
        margin,
        margin,
        width - 2 * margin,
        width - 2 * margin));
    area.intersect(new Area(new Rectangle2D.Double(
        margin,
        margin,
        width - 5 * margin / 2,
        width - 2 * margin)));
    area.fillDraw(g2d, BODY_COLOR, BORDER_COLOR);
    Area.circle(center, width - 4 * margin + 2).draw(g2d, BORDER_COLOR);
  }

  @Override
  protected Size getDefaultWidth() {
    return DEFAULT_SIZE;
  }

  @Override
  protected Size getDefaultLength() {
    return DEFAULT_SIZE;
  }

  @Override
  protected Area getBodyShape() {
    int size = getClosestOdd((int) (getLength().convertToPixels() * 1.2));
    Area area = Area.circle(0, 0, size);
    area.intersect(Area.rect(0, 0, getLength().convertToPixels() * 1.15, size));
    return area;
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    if (!outlineMode) {
      int size = getClosestOdd((int) (getLength().convertToPixels() * 1.2));
      int innerSize = getClosestOdd(getLength().convertToPixels());
      int x = (size - innerSize) / 2;
      Area.circle(x, x, innerSize).draw(g2d, getBorderColor());
    }
  }

  @EditableProperty(name = "Size")
  @Override
  public Size getLength() {
    return super.getLength();
  }

  @Override
  public Size getWidth() {
    return super.getWidth();
  }
}
