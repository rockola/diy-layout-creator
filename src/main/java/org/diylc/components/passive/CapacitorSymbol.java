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

@ComponentValue(SiUnit.FARAD)
@ComponentDescriptor(
    name = "Capacitor",
    author = "Branislav Stojkovic",
    category = "Schematic Symbols",
    creationMethod = CreationMethod.POINT_BY_POINT,
    instanceNamePrefix = "C",
    description = "Capacitor schematic symbol with an optional polarity sign",
    zOrder = AbstractComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Schematic",
    transformer = SimpleComponentTransformer.class)
public class CapacitorSymbol extends AbstractSchematicLeadedSymbol {

  private static final long serialVersionUID = 1L;

  public static final Size DEFAULT_LENGTH = Size.in(0.05);
  public static final Size DEFAULT_WIDTH = Size.in(0.15);

  private Value voltage;
  private boolean polarized = false;

  public CapacitorSymbol() {
    super();
    valueUnit = SiUnit.FARAD;
  }

  @ComponentValue(SiUnit.VOLT)
  @EditableProperty(name = "Voltage")
  public Value getVoltage() {
    return voltage;
  }

  public void setVoltage(Value voltage) {
    if (voltage == null || voltage.getUnit() == SiUnit.VOLT) {
      this.voltage = voltage;
    }
  }

  @EditableProperty
  public boolean getPolarized() {
    return polarized;
  }

  public void setPolarized(boolean polarized) {
    this.polarized = polarized;
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR);
    g2d.drawLine(0, height / 2, 13, height / 2);
    g2d.drawLine(width - 13, height / 2, width, height / 2);
    g2d.setColor(COLOR);
    g2d.drawLine(14, height / 2 - 6, 14, height / 2 + 6);
    g2d.drawLine(width - 14, height / 2 - 6, width - 14, height / 2 + 6);
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
    polyline.moveTo(0, 0);
    polyline.lineTo(0, width);
    polyline.moveTo(length, 0);
    polyline.lineTo(length, width);
    return new Area(polyline);
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    if (polarized) {
      // Draw + sign.
      g2d.setColor(getBorderColor());
      int plusSize = getClosestOdd(getWidth().convertToPixels() / 4);
      int x = -plusSize;
      int y = plusSize;
      g2d.drawLine(x - plusSize / 2, y, x + plusSize / 2, y);
      g2d.drawLine(x, y - plusSize / 2, x, y + plusSize / 2);
    }
  }
}
