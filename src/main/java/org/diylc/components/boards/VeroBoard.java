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

package org.diylc.components.boards;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import org.diylc.common.OrientationHV;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;

@ComponentDescriptor(
    name = "Vero Board",
    category = "Boards",
    author = "Branislav Stojkovic",
    zOrder = IDIYComponent.BOARD,
    instanceNamePrefix = "Board",
    description = "Perforated FR4 board with copper strips connecting all holes in a row",
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME,
    autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    transformer = SimpleComponentTransformer.class)
public class VeroBoard extends AbstractBoard {

  private static final long serialVersionUID = 1L;

  public static final Color BORDER_COLOR = BOARD_COLOR.darker();
  public static final Size SPACING = Size.in(0.1);
  public static final Size STRIP_SIZE = Size.in(0.07);
  public static final Size HOLE_SIZE = Size.mm(0.7);

  protected Size spacing = SPACING;
  protected Color stripColor = COPPER_COLOR;
  protected OrientationHV orientation = OrientationHV.HORIZONTAL;

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    Shape clip = g2d.getClip();
    if (checkPointsClipped(clip)
        && !clip.contains(firstPoint.x, secondPoint.y)
        && !clip.contains(secondPoint.x, firstPoint.y)) {
      return;
    }
    super.draw(g2d, componentState, outlineMode, project, drawingObserver);
    if (!componentState.isDragging()) {
      Composite oldComposite = setTransparency(g2d);
      Point p = new Point(firstPoint);
      int stripSize = getClosestOdd((int) STRIP_SIZE.convertToPixels());
      int holeSize = getClosestOdd((int) HOLE_SIZE.convertToPixels());
      int spacing = (int) this.spacing.convertToPixels();

      if (orientation == OrientationHV.HORIZONTAL) {
        while (p.y < secondPoint.y - spacing) {
          p.x = firstPoint.x;
          p.y += spacing;
          g2d.setColor(stripColor);
          drawingObserver.startTrackingContinuityArea(true);
          g2d.fillRect(
              p.x + spacing / 2,
              p.y - stripSize / 2,
              secondPoint.x - spacing - p.x,
              stripSize);
          drawingObserver.stopTrackingContinuityArea();
          g2d.setColor(stripColor.darker());
          g2d.drawRect(
              p.x + spacing / 2,
              p.y - stripSize / 2,
              secondPoint.x - spacing - p.x,
              stripSize);
          while (p.x < secondPoint.x - spacing - holeSize) {
            p.x += spacing;
            Area.circle(p, holeSize).fillDraw(g2d, CANVAS_COLOR, stripColor.darker());
          }
        }
      } else {
        while (p.x < secondPoint.x - spacing) {
          p.x += spacing;
          p.y = firstPoint.y;
          g2d.setColor(stripColor);
          drawingObserver.startTrackingContinuityArea(true);
          g2d.fillRect(
              p.x - stripSize / 2,
              p.y + spacing / 2,
              stripSize,
              secondPoint.y - spacing - p.y);
          drawingObserver.stopTrackingContinuityArea();
          g2d.setColor(stripColor.darker());
          g2d.drawRect(
              p.x - stripSize / 2,
              p.y + spacing / 2,
              stripSize,
              secondPoint.y - spacing - p.y);
          while (p.y < secondPoint.y - spacing - holeSize) {
            p.y += spacing;
            Area.circle(p, holeSize).fillDraw(g2d, CANVAS_COLOR, stripColor.darker());
          }
        }
      }
      g2d.setComposite(oldComposite);
      super.drawCoordinates(g2d, spacing, project);
    }
  }

  @EditableProperty(name = "Strip Color")
  public Color getStripColor() {
    return stripColor;
  }

  public void setStripColor(Color padColor) {
    this.stripColor = padColor;
  }

  @EditableProperty(validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getSpacing() {
    return spacing;
  }

  public void setSpacing(Size spacing) {
    this.spacing = spacing;
  }

  @EditableProperty
  public OrientationHV getOrientation() {
    return orientation;
  }

  public void setOrientation(OrientationHV orientation) {
    this.orientation = orientation;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    Area.rect(0, 2 / factor, width - 1, height - 4 / factor)
        .fillDraw(g2d, BOARD_COLOR, BORDER_COLOR);
    Area.rect(1 / factor, width / 4, width - 2 / factor, width / 2)
        .fillDraw(g2d, COPPER_COLOR, COPPER_COLOR.darker());
    Area.rect(1 / factor, 2 / factor, width - 2 / factor, 3 / factor)
        .fillDraw(g2d, COPPER_COLOR, COPPER_COLOR.darker());
    Area.rect(1 / factor, height - 5 / factor, width - 2 / factor, 3 / factor)
        .fillDraw(g2d, COPPER_COLOR, COPPER_COLOR.darker());
    double holeWidth = height / 6;
    for (int i = 0; i < 4; i++) {
      Area.circle(i * width / 3, height / 2, holeWidth)
          .fillDraw(g2d, CANVAS_COLOR, COPPER_COLOR.darker());
    }
    /*
    g2d.setColor(CANVAS_COLOR);
    g2d.fillOval(
        width / 3 - 2 / factor,
        width / 2 - 2 / factor,
        getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor));
    g2d.fillOval(
        2 * width / 3 - 2 / factor,
        width / 2 - 2 / factor,
        getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor));
    g2d.setColor(COPPER_COLOR.darker());
    g2d.drawOval(
        width / 3 - 2 / factor,
        width / 2 - 2 / factor,
        getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor));
    g2d.drawOval(
        2 * width / 3 - 2 / factor,
        width / 2 - 2 / factor,
        getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor));
    */
  }
}
