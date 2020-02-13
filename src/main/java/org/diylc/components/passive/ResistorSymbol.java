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

import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractComponent;
import org.diylc.components.AbstractSchematicLeadedSymbol;
import org.diylc.components.Area;
import org.diylc.core.CreationMethod;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.ComponentValue;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.SiUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.Value;

@ComponentValue(SiUnit.OHM)
@ComponentDescriptor(
    name = "Resistor",
    author = "Branislav Stojkovic",
    category = "Schematic Symbols",
    creationMethod = CreationMethod.POINT_BY_POINT,
    instanceNamePrefix = "R",
    description = "Resistor schematic symbol",
    zOrder = AbstractComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Schematic",
    transformer = SimpleComponentTransformer.class)
public class ResistorSymbol extends AbstractSchematicLeadedSymbol {

  private static final long serialVersionUID = 1L;

  public static final Size DEFAULT_LENGTH = Size.in(0.3);
  public static final Size DEFAULT_WIDTH = Size.in(0.08);

  private Value power = new Value(0.5, SiUnit.WATT);

  public ResistorSymbol() {
    super();
  }

  @Override
  public String getValueForDisplay() {
    return getValue().toString() + " " + getPower().toString();
  }

  @ComponentValue(SiUnit.WATT)
  @EditableProperty(name = "Power Rating")
  public Value getPower() {
    return power;
  }

  public void setPower(Value powerNew) {
    if (powerNew == null || powerNew.getUnit() == SiUnit.WATT) {
      this.power = powerNew;
    }
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR);
    g2d.drawLine(0, height / 2, 4, height / 2);
    g2d.drawLine(width - 4, height / 2, width, height / 2);
    g2d.setColor(COLOR);
    g2d.drawPolyline(
        new int[] {4, 6, 10, 14, 18, 22, 26, 28},
        new int[] {
          height / 2,
          height / 2 + 2,
          height / 2 - 2,
          height / 2 + 2,
          height / 2 - 2,
          height / 2 + 2,
          height / 2 - 2,
          height / 2
        },
        8);
  }

  @Override
  protected Size getDefaultWidth() {
    return DEFAULT_WIDTH;
  }

  @Override
  protected Size getDefaultLength() {
    return DEFAULT_LENGTH;
  }

  @Override
  protected Area getBodyShape() {
    Path2D polyline = new Path2D.Double();
    double length = getLength().convertToPixels();
    double width = getWidth().convertToPixels();
    polyline.moveTo(0, width / 2);
    polyline.lineTo(length / 16, width);
    polyline.lineTo(3 * length / 16, 0);
    polyline.lineTo(5 * length / 16, width);
    polyline.lineTo(7 * length / 16, 0);
    polyline.lineTo(9 * length / 16, width);
    polyline.lineTo(11 * length / 16, 0);
    polyline.lineTo(13 * length / 16, width);
    polyline.lineTo(15 * length / 16, 0);
    polyline.lineTo(length, width / 2);
    return new Area(polyline);
  }
}
