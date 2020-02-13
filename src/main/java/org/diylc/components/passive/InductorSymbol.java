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

@ComponentValue(SiUnit.HENRY)
@ComponentDescriptor(
    name = "Inductor",
    author = "Branislav Stojkovic",
    category = "Schematic Symbols",
    creationMethod = CreationMethod.POINT_BY_POINT,
    instanceNamePrefix = "L",
    description = "Inductor schematic symbol",
    zOrder = AbstractComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Schematic",
    transformer = SimpleComponentTransformer.class)
public class InductorSymbol extends AbstractSchematicLeadedSymbol {

  private static final long serialVersionUID = 1L;

  public static final Size DEFAULT_LENGTH = Size.in(0.3);
  public static final Size DEFAULT_WIDTH = Size.in(0.08);

  private Value current = null;
  private Value resistance = null;
  private boolean core = false;

  public InductorSymbol() {
    super();
    valueUnit = SiUnit.HENRY;
  }

  @ComponentValue(SiUnit.AMPERE)
  @EditableProperty
  public Value getCurrent() {
    return current;
  }

  public void setCurrent(Value current) {
    if (current == null || current.hasUnit(SiUnit.AMPERE)) {
      this.current = current;
    }
  }

  @ComponentValue(SiUnit.OHM)
  @EditableProperty
  public Value getResistance() {
    return resistance;
  }

  public void setResistance(Value resistance) {
    if (resistance == null || resistance.hasUnit(SiUnit.OHM)) {
      this.resistance = resistance;
    }
  }

  @Override
  public String getValueForDisplay() {
    return getValue().toString() + (getCurrent() == null ? "" : " " + getCurrent().toString());
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR);
    g2d.drawLine(0, height / 2, width / 8, height / 2);
    g2d.drawLine(width * 7 / 8, height / 2, width, height / 2);
    g2d.setColor(COLOR);

    Path2D polyline = new Path2D.Double();
    polyline.moveTo(width / 8, height / 2);
    polyline.curveTo(width / 8, height / 4, width * 3 / 8, height / 4, width * 3 / 8, height / 2);
    polyline.curveTo(
        width * 3 / 8, height / 4, width * 5 / 8, height / 4, width * 5 / 8, height / 2);
    polyline.curveTo(
        width * 5 / 8, height / 4, width * 7 / 8, height / 4, width * 7 / 8, height / 2);

    polyline.moveTo(width / 8, height * 6 / 10);
    polyline.lineTo(width * 7 / 8, height * 6 / 10);
    polyline.moveTo(width / 8, height * 7 / 10);
    polyline.lineTo(width * 7 / 8, height * 7 / 10);
    g2d.draw(polyline);
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
    double d = length / 10;
    polyline.moveTo(0, width / 2);
    polyline.curveTo(0, 0, 2 * d, 0, 2 * d, width / 2);
    polyline.curveTo(2 * d, 0, 4 * d, 0, 4 * d, width / 2);
    polyline.curveTo(4 * d, 0, 6 * d, 0, 6 * d, width / 2);
    polyline.curveTo(6 * d, 0, 8 * d, 0, 8 * d, width / 2);
    polyline.curveTo(8 * d, 0, 10 * d, 0, 10 * d, width / 2);
    if (core) {
      polyline.moveTo(0, width * 3 / 4);
      polyline.lineTo(length, width * 3 / 4);
      polyline.moveTo(0, width * 7 / 8);
      polyline.lineTo(length, width * 7 / 8);
    }
    return new Area(polyline);
  }

  @Override
  protected boolean useShapeRectAsPosition() {
    return false;
  }

  @EditableProperty
  public boolean getCore() {
    return core;
  }

  public void setCore(boolean core) {
    this.core = core;
  }
}
