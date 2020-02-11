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

package org.diylc.components.chassis;

import java.awt.Composite;
import java.awt.Graphics2D;
import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.shapes.AbstractShape;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;

@ComponentDescriptor(
    name = "Rectangular Cutout",
    author = "Branislav Stojkovic",
    category = "Electro-Mechanical",
    instanceNamePrefix = "RECTC",
    description = "Rectangular chassis cutout, with or without rounded edges",
    zOrder = IDIYComponent.CHASSIS + 0.1,
    bomPolicy = BomPolicy.NEVER_SHOW,
    autoEdit = false,
    transformer = SimpleComponentTransformer.class)
public class RectangularCutout extends AbstractShape {

  private static final long serialVersionUID = 1L;

  protected Size edgeRadius = Size.mm(0);

  public RectangularCutout() {
    this.borderColor = LIGHT_METAL_COLOR.darker();
  }

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    g2d.setStroke(
        ObjectCache.getInstance().fetchBasicStroke((int) borderThickness.convertToPixels()));
    int radius = (int) edgeRadius.convertToPixels();
    if (!componentState.isDragging()) {
      Composite oldComposite = setTransparency(g2d);
      g2d.setColor(color);
      g2d.fillRoundRect(
          firstPoint.x,
          firstPoint.y,
          secondPoint.x - firstPoint.x,
          secondPoint.y - firstPoint.y,
          radius,
          radius);
      g2d.setComposite(oldComposite);
    }
    // Do not track any changes that follow because the whole rect has been
    // tracked so far.
    drawingObserver.stopTracking();
    g2d.setColor(tryBorderColor(false, borderColor));
    g2d.drawRoundRect(
        firstPoint.x,
        firstPoint.y,
        secondPoint.x - firstPoint.x,
        secondPoint.y - firstPoint.y,
        radius,
        radius);
  }

  @EditableProperty(name = "Radius")
  public Size getEdgeRadius() {
    return edgeRadius;
  }

  public void setEdgeRadius(Size edgeRadius) {
    this.edgeRadius = edgeRadius;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    g2d.setColor(COLOR);
    g2d.fillRect(2 / factor, 4 / factor, width - 4 / factor, height - 8 / factor);
    g2d.setColor(LIGHT_METAL_COLOR.darker());
    g2d.drawRect(2 / factor, 4 / factor, width - 4 / factor, height - 8 / factor);
  }
}
