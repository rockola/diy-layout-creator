/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2019 held jointly by the individual authors.

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

package org.diylc.components.boards;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractComponent;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;

@ComponentDescriptor(
    name = "Perf Board w/ Pads",
    category = "Boards",
    author = "Branislav Stojkovic",
    zOrder = AbstractComponent.BOARD,
    instanceNamePrefix = "Board",
    description = "Perforated board with solder pads",
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME,
    autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Perf Board",
    transformer = SimpleComponentTransformer.class)
public class PerfBoard extends AbstractBoard {

  private static final long serialVersionUID = 1L;

  public static final Color COPPER_COLOR = Color.decode("#DA8A67");
  public static final Size SPACING = Size.in(0.1);
  public static final Size PAD_SIZE = Size.in(0.08);
  public static final Size HOLE_SIZE = Size.mm(0.7);

  // private Area copperArea;
  protected Size spacing = SPACING;
  protected Color padColor = COPPER_COLOR;
  protected boolean showPads = true;

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    Shape clip = g2d.getClip();
    Point firstPoint = firstPoint();
    Point secondPoint = secondPoint();
    if (checkPointsClipped(clip)
        && !clip.contains(firstPoint.x, secondPoint.y)
        && !clip.contains(secondPoint.x, firstPoint.y)) {
      return;
    }
    super.draw(g2d, componentState, outlineMode, project, drawingObserver);
    if (!isDragging()) {
      Composite oldComposite = setTransparency(g2d);
      Point p = new Point(firstPoint);
      int diameter = getClosestOdd((int) PAD_SIZE.convertToPixels());
      int holeDiameter = getClosestOdd((int) HOLE_SIZE.convertToPixels());
      int spacing = (int) this.spacing.convertToPixels();

      while (p.y < secondPoint.y - spacing) {
        p.x = firstPoint.x;
        p.y += spacing;
        while (p.x < secondPoint.x - spacing - diameter) {
          p.x += spacing;
          if (showPads) {
            Area.circle(p, diameter).fillDraw(g2d, padColor, padColor.darker());
          }
          Area.circle(p, holeDiameter).fillDraw(g2d, CANVAS_COLOR, padColor.darker());
        }
      }
      super.drawCoordinates(g2d, spacing, project);
      g2d.setComposite(oldComposite);
    }
  }

  @EditableProperty(name = "Pad color")
  public Color getPadColor() {
    return padColor;
  }

  public void setPadColor(Color padColor) {
    this.padColor = padColor;
  }

  @EditableProperty(validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getSpacing() {
    return spacing;
  }

  public void setSpacing(Size spacing) {
    this.spacing = spacing;
  }

  @EditableProperty(name = "Show pads")
  public boolean getShowPads() {
    return showPads;
  }

  public void setShowPads(boolean show) {
    this.showPads = show;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    double x = 2;
    double w = width - 2 * x;
    double h = height - 2 * x;
    Area.rect(x, x, w, h).fillDraw(g2d, BOARD_COLOR, BORDER_COLOR);
    x = width / 2;
    double d = x / 3;
    Area.circle(x, x, d).fill(g2d, CANVAS_COLOR);
    Area.ring(x, x, x, d).fillDraw(g2d, COPPER_COLOR, COPPER_COLOR.darker());
  }
}
