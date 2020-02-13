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
import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractComponent;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.StringValue;
import org.diylc.core.measures.Size;

@StringValue
@ComponentDescriptor(
    name = "Turret Lug",
    category = "Connectivity",
    author = "Branislav Stojkovic",
    description = "Turret terminal lug",
    instanceNamePrefix = "Turret",
    zOrder = AbstractComponent.TRACE + 0.1,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME,
    autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    transformer = SimpleComponentTransformer.class)
public class Turret extends AbstractComponent {

  private static final long serialVersionUID = 1L;

  public static final Size SIZE = Size.in(0.16);
  public static final Size HOLE_SIZE = Size.in(0.0625);
  public static final Color COLOR = Color.decode("#E0C04C");

  private Size size = SIZE;
  private Size holeSize = HOLE_SIZE;
  private Color color = COLOR;
  private Point point = new Point(0, 0);

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    if (!checkPointsClipped(g2d.getClip())) {
      g2d.setColor(color);
      g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(1f));
      drawingObserver.startTrackingContinuityArea(true);

      int diameter = getClosestOdd((int) size.convertToPixels());
      g2d.fill(Area.circle(point, diameter));
      drawingObserver.stopTrackingContinuityArea();
      g2d.setColor(tryColor(false, color.darker()));
      g2d.draw(Area.circle(point, diameter));

      g2d.setColor(CANVAS_COLOR);
      int holeDiameter = getClosestOdd((int) holeSize.convertToPixels());
      g2d.fill(Area.circle(point, holeDiameter));
      g2d.setColor(tryColor(false, color.darker()));
      g2d.draw(Area.circle(point, holeDiameter));
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int diameter = getClosestOdd(width / 2);
    Point center = new Point(width / 2, height / 2);
    Area.circle(center, diameter).fillDraw(g2d, COLOR, COLOR.darker());
    int holeDiameter = 5;
    Area.circle(center, holeDiameter).fillDraw(g2d, CANVAS_COLOR, COLOR.darker());
  }

  @EditableProperty
  public Size getSize() {
    return size;
  }

  public void setSize(Size size) {
    this.size = size;
  }

  @EditableProperty(name = "Hole size")
  public Size getHoleSize() {
    return holeSize;
  }

  public void setHoleSize(Size holeSize) {
    this.holeSize = holeSize;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
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
