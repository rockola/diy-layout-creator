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

package org.diylc.components.guitar;

import java.awt.Color;

import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;

public abstract class AbstractBassPickup extends AbstractSingleOrHumbuckerPickup {

  private static final long serialVersionUID = 1L;

  protected static final Color BODY_COLOR = Color.decode("#333333");
  protected static final Size POINT_MARGIN = Size.mm(1.5);
  protected static final Size POINT_SIZE = Size.mm(2);
  protected static final Size POLE_SIZE = Size.mm(4);
  protected static final Size EDGE_RADIUS = Size.in(0.08);
  protected static final Size LIP_RADIUS = Size.in(0.45);
  protected static final Size LIP_HOLE_SIZE = Size.in(0.1);
  protected static final Size LIP_HOLE_SPACING = Size.in(0.1);

  protected Color color = BODY_COLOR;
  protected Color poleColor = METAL_COLOR;

  protected AbstractBassPickup() {
    super();
    numberOfStrings = 4;
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @EditableProperty(name = "Pole Color")
  public Color getPoleColor() {
    if (poleColor == null) {
      poleColor = METAL_COLOR;
    }
    return poleColor;
  }

  public void setPoleColor(Color poleColor) {
    this.poleColor = poleColor;
  }
}
