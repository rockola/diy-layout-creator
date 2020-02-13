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

package org.diylc.components.connectivity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractComponent;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;

@ComponentDescriptor(
    name = "Dot",
    category = "Connectivity",
    author = "Branislav Stojkovic",
    description = "Connector dot",
    instanceNamePrefix = "Dot",
    zOrder = AbstractComponent.COMPONENT,
    bomPolicy = BomPolicy.NEVER_SHOW,
    autoEdit = false,
    transformer = SimpleComponentTransformer.class)
public class Dot extends AbstractComponent {

  private static final long serialVersionUID = 1L;

  public static final Size SIZE = Size.mm(1);
  public static final Color COLOR = Color.black;

  private Size size = SIZE;
  private Color color = COLOR;

  public Dot() {
    super();
    controlPoints = getFreshControlPoints(1);
  }

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    int diameter = getClosestOdd((int) getSize().convertToPixels());
    Area.circle(controlPoints[0], diameter).fill(g2d, tryColor(false, color));
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int diameter = 7 * width / 32;
    Area.circle(width / 2, height / 2, diameter).fill(g2d, COLOR);
  }

  @EditableProperty(validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getSize() {
    return size;
  }

  public void setSize(Size size) {
    this.size = size;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @EditableProperty(name = "Color")
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }
}
