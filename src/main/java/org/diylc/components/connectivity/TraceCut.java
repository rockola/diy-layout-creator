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
import org.diylc.components.boards.AbstractBoard;
import org.diylc.components.boards.VeroBoard;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;

@ComponentDescriptor(
    name = "Trace Cut",
    category = "Connectivity",
    author = "Branislav Stojkovic",
    description = "Designates the place where a trace on the vero board needs to be cut",
    instanceNamePrefix = "Cut",
    zOrder = IDIYComponent.BOARD + 1,
    bomPolicy = BomPolicy.NEVER_SHOW,
    autoEdit = false,
    transformer = SimpleComponentTransformer.class)
public class TraceCut extends AbstractComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static final Color BORDER_COLOR = Color.red;
  public static final Color FILL_COLOR = Color.white;
  public static final Size CUT_WIDTH = Size.mm(0.5);
  public static final Size HOLE_SIZE = Size.mm(0.7);
  public static final Size SIZE = Size.in(0.08);

  protected Point point = new Point(0, 0);

  private Size size = SIZE;
  private Color fillColor = FILL_COLOR;
  private Color boardColor = AbstractBoard.BOARD_COLOR;
  private Boolean cutBetweenHoles = false;
  private Size holeSpacing = VeroBoard.SPACING;
  @Deprecated private Color borderColor = BORDER_COLOR;

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
    g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(1f));
    g2d.setColor(tryColor(false, boardColor));
    int size = getClosestOdd((int) this.size.convertToPixels());
    int cutWidth = getClosestOdd((int) CUT_WIDTH.convertToPixels());
    if (getCutBetweenHoles()) {
      int holeSpacing = getClosestOdd(getHoleSpacing().convertToPixels());
      drawingObserver.startTrackingContinuityArea(false);
      g2d.fillRect(
          point.x - holeSpacing / 2 - cutWidth / 2, point.y - size / 2 - 1, cutWidth, size + 2);
      drawingObserver.stopTrackingContinuityArea();
    } else {
      drawingObserver.startTrackingContinuityArea(false);
      g2d.fillRoundRect(point.x - size / 2, point.y - size / 2, size, size, size, size);
      drawingObserver.stopTrackingContinuityArea();

      int holeSize = getClosestOdd((int) HOLE_SIZE.convertToPixels());
      Area.circle(point, holeSize).fillDraw(g2d, CANVAS_COLOR, boardColor.darker());
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    Area.rect(
        0,
        2 / factor,
        width - 1,
        height - 4 / factor).fillDraw(g2d, AbstractBoard.BOARD_COLOR, AbstractBoard.BORDER_COLOR);
    Area.rect(
        1 / factor,
        width / 3,
        width - 2 / factor,
        getClosestOdd(width / 3) + 1).fillDraw(g2d, COPPER_COLOR, COPPER_COLOR.darker());

    g2d.setColor(AbstractBoard.BOARD_COLOR);
    g2d.fillRoundRect(
        width / 3,
        width / 3,
        getClosestOdd(width / 3) + 2,
        getClosestOdd(width / 3) + 2,
        width / 3,
        width / 3);

    Area.rect(
        1 / factor,
        2 / factor,
        width - 2 / factor,
        4 / factor).fillDraw(g2d, COPPER_COLOR, COPPER_COLOR.darker());
    Area.rect(
        1 / factor,
        height - 6 / factor,
        width - 2 / factor,
        4 / factor).fillDraw(g2d, COPPER_COLOR, COPPER_COLOR.darker());

    g2d.setColor(CANVAS_COLOR);
    g2d.fillOval(
        width / 6 - 1, width / 2 - 1, getClosestOdd(3.0 / factor), getClosestOdd(3.0 / factor));
    g2d.fillOval(
        width / 2 - 1, width / 2 - 1, getClosestOdd(3.0 / factor), getClosestOdd(3.0 / factor));
    g2d.fillOval(
        5 * width / 6 - 1, width / 2 - 1, getClosestOdd(3.0 / factor), getClosestOdd(3.0 / factor));
    g2d.setColor(COPPER_COLOR.darker());
    g2d.drawOval(
        width / 6 - 1, width / 2 - 1, getClosestOdd(3.0 / factor), getClosestOdd(3.0 / factor));
    g2d.drawOval(
        width / 2 - 1, width / 2 - 1, getClosestOdd(3.0 / factor), getClosestOdd(3.0 / factor));
    g2d.drawOval(
        5 * width / 6 - 1, width / 2 - 1, getClosestOdd(3.0 / factor), getClosestOdd(3.0 / factor));
  }

  @Override
  public int getControlPointCount() {
    return 1;
  }

  @Override
  public Point getControlPoint(int index) {
    return point;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.point.setLocation(point);
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}

  @EditableProperty
  public Size getSize() {
    return size;
  }

  public void setSize(Size size) {
    this.size = size;
  }

  @EditableProperty(name = "Fill")
  public Color getFillColor() {
    return fillColor;
  }

  public void setFillColor(Color fillColor) {
    this.fillColor = fillColor;
  }

  @Deprecated
  public Color getBorderColor() {
    return borderColor;
  }

  @Deprecated
  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty(name = "Cut between holes")
  public boolean getCutBetweenHoles() {
    if (cutBetweenHoles == null) {
      cutBetweenHoles = false;
    }
    return cutBetweenHoles;
  }

  public void setCutBetweenHoles(boolean cutBetweenHoles) {
    this.cutBetweenHoles = cutBetweenHoles;
  }

  @EditableProperty(name = "Board")
  public Color getBoardColor() {
    if (boardColor == null) {
      boardColor = AbstractBoard.BOARD_COLOR;
    }
    return boardColor;
  }

  public void setBoardColor(Color boardColor) {
    this.boardColor = boardColor;
  }

  @EditableProperty(name = "Hole spacing")
  public Size getHoleSpacing() {
    if (holeSpacing == null) {
      holeSpacing = VeroBoard.SPACING;
    }
    return holeSpacing;
  }

  public void setHoleSpacing(Size holeSpacing) {
    this.holeSpacing = holeSpacing;
  }

  @Deprecated
  @Override
  public String getName() {
    return super.getName();
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }
}
