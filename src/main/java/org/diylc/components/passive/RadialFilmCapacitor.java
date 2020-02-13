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
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractComponent;
import org.diylc.components.Area;
import org.diylc.components.LeadType;
import org.diylc.core.CreationMethod;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.measures.Size;

@ComponentDescriptor(
    name = "Film Capacitor (Radial)",
    author = "Branislav Stojkovic",
    category = "Passive",
    creationMethod = CreationMethod.POINT_BY_POINT,
    instanceNamePrefix = "C",
    description = "Radial film capacitor, similar to Sprague Orange Drop",
    zOrder = AbstractComponent.COMPONENT,
    leadType = LeadType.RADIAL,
    transformer = SimpleComponentTransformer.class)
public class RadialFilmCapacitor extends RadialCapacitor {

  private static final long serialVersionUID = 1L;

  public static final Size DEFAULT_WIDTH = Size.in(0.25);
  public static final Size DEFAULT_HEIGHT = Size.in(0.125);
  public static final Color BODY_COLOR = Color.decode("#FF8000");
  public static final Color BORDER_COLOR = BODY_COLOR.darker();

  public RadialFilmCapacitor() {
    super();
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR_ICON);
    g2d.drawLine(0, height / 2, width, height / 2);
    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect(4, height / 2 - 3, width - 8, 6, 5, 5);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect(4, height / 2 - 3, width - 8, 6, 5, 5);
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
    double radius = getWidth().convertToPixels() * 0.7;
    return Area.roundRect(
        0f, 0f, getLength().convertToPixels(), getClosestOdd(getWidth().convertToPixels()), radius);
  }
}
