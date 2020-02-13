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

package org.diylc.components.shapes;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;

@ComponentDescriptor(
    name = "Rectangle",
    author = "Branislav Stojkovic",
    category = "Shapes",
    instanceNamePrefix = "RECT",
    description = "Ractangular area, with or withouth rounded edges",
    zOrder = AbstractComponent.COMPONENT,
    flexibleZOrder = true,
    bomPolicy = BomPolicy.SHOW_ALL_NAMES,
    autoEdit = false,
    transformer = SimpleComponentTransformer.class)
public class Rectangle extends AbstractShape {

  private static final long serialVersionUID = 1L;

  protected Size edgeRadius = Size.mm(0);

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

    Point firstPoint =
        new Point(
            Math.min(controlPoints[0].x, controlPoints[1].x),
            Math.min(controlPoints[0].y, controlPoints[1].y));
    Point secondPoint =
        new Point(
            Math.max(controlPoints[0].x, controlPoints[1].x),
            Math.max(controlPoints[0].y, controlPoints[1].y));

    Area rect =
        Area.roundRect(
            firstPoint.x,
            firstPoint.y,
            secondPoint.x - firstPoint.x,
            secondPoint.y - firstPoint.y,
            radius);
    Composite oldComposite = setTransparency(g2d, 0);
    rect.fill(g2d, color);
    g2d.setComposite(oldComposite);

    // Do not track any changes that follow because the whole rect has been
    // tracked so far.
    drawingObserver.stopTracking();
    rect.draw(g2d, tryBorderColor(false, borderColor));
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
    Area.rect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor)
        .fillDraw(g2d, COLOR, BORDER_COLOR);
  }
}
