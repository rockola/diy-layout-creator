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
import java.awt.Composite;
import java.awt.Graphics2D;
import org.diylc.common.ObjectCache;
import org.diylc.common.OrientationHV;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractComponent;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;

@ComponentDescriptor(
    name = "Cut Line",
    category = "Connectivity",
    author = "Branislav Stojkovic",
    description = "Cut line",
    instanceNamePrefix = "CL",
    zOrder = AbstractComponent.COMPONENT,
    bomPolicy = BomPolicy.NEVER_SHOW,
    autoEdit = false,
    transformer = SimpleComponentTransformer.class)
public class CutLine extends AbstractTransparentComponent {

  private static final long serialVersionUID = 1L;

  public static final Size WIDTH = Size.in(0.125);
  public static final Size LENGTH = Size.in(3.125);
  public static final Color COLOR = Color.black;

  private Size width = WIDTH;
  private Size length = LENGTH;
  private Color color = COLOR;
  private OrientationHV orientation = OrientationHV.VERTICAL;

  CutLine() {
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
    int w = getClosestOdd((int) getWidth().convertToPixels());
    int l = getClosestOdd((int) getLength().convertToPixels());
    g2d.setColor(tryColor(false, color));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(w));

    boolean isHorizontal = getOrientation() == OrientationHV.HORIZONTAL;
    Composite oldComposite = setTransparency(g2d);
    g2d.drawLine(
        controlPoints[0].x,
        controlPoints[0].y,
        isHorizontal ? controlPoints[0].x + l : controlPoints[0].x,
        isHorizontal ? controlPoints[0].y : controlPoints[0].y + l);
    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    g2d.setColor(COLOR);
    g2d.drawLine(width / 2, height - 2, width / 2, 1);
  }

  @EditableProperty
  public Size getWidth() {
    return width;
  }

  public void setWidth(Size width) {
    this.width = width;
  }

  @EditableProperty
  public Size getLength() {
    return length;
  }

  public void setLength(Size length) {
    this.length = length;
  }

  @EditableProperty
  public OrientationHV getOrientation() {
    return orientation;
  }

  public void setOrientation(OrientationHV orientation) {
    this.orientation = orientation;
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
