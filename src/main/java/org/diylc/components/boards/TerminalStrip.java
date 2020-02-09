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

package org.diylc.components.boards;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.Area;
import org.diylc.components.transform.TerminalStripTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IContinuity;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "Terminal Strip",
    author = "Branislav Stojkovic",
    category = "Boards",
    instanceNamePrefix = "TS",
    description = "Row of terminals for point-to-point construction",
    zOrder = IDIYComponent.BOARD,
    keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    transformer = TerminalStripTransformer.class)
public class TerminalStrip extends AbstractTransparentComponent<String> implements IContinuity {

  private static final long serialVersionUID = 1L;

  public static final Color BOARD_COLOR = Color.decode("#CD8500");
  public static final Color BORDER_COLOR = BOARD_COLOR.darker();
  public static final Color TERMINAL_COLOR = Color.lightGray;
  public static final Color TERMINAL_BORDER_COLOR = TERMINAL_COLOR.darker();
  public static final int EDGE_RADIUS = 2;
  public static final Size HOLE_SIZE = Size.in(0.06);
  public static final Size MOUNTING_HOLE_SIZE = Size.in(0.07);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;
  private int terminalCount = 10;
  private Size boardWidth = Size.in(0.35);
  private Size terminalSpacing = Size.in(0.25);
  private Size holeSpacing = Size.in(0.5);
  private Point[] controlPoints = new Point[] {new Point(0, 0)};
  private Color boardColor = BOARD_COLOR;
  private Color borderColor = BORDER_COLOR;
  private boolean centerHole = false;

  private transient Area[] body;

  public TerminalStrip() {
    super();
    updateControlPoints();
  }

  @EditableProperty
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }

  @EditableProperty(name = "Terminals")
  public int getTerminalCount() {
    return terminalCount;
  }

  public void setTerminalCount(int terminalCount) {
    this.terminalCount = terminalCount;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty(
      name = "Terminal Spacing",
      validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getTerminalSpacing() {
    return terminalSpacing;
  }

  public void setTerminalSpacing(Size pinSpacing) {
    this.terminalSpacing = pinSpacing;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty(name = "Hole Spacing", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getHoleSpacing() {
    return holeSpacing;
  }

  public void setHoleSpacing(Size rowSpacing) {
    this.holeSpacing = rowSpacing;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty(name = "Center Terminal")
  public boolean getCenterHole() {
    return centerHole;
  }

  public void setCenterHole(boolean centerHole) {
    this.centerHole = centerHole;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty(name = "Board Width", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getBoardWidth() {
    return boardWidth;
  }

  public void setBoardWidth(Size boardWidth) {
    this.boardWidth = boardWidth;
    // Reset body shape;
    body = null;
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  private void updateControlPoints() {
    Point firstPoint = controlPoints[0];
    controlPoints = new Point[getTerminalCount() * (getCenterHole() ? 3 : 2)];
    controlPoints[0] = firstPoint;
    int pinSpacing = (int) this.terminalSpacing.convertToPixels();
    int rowSpacing = (int) this.holeSpacing.convertToPixels();
    // Update control points.
    int dx1;
    int dy1;
    int dx2;
    int dy2;
    int cx;
    int cy;
    for (int i = 0; i < getTerminalCount(); i++) {
      switch (orientation) {
        case DEFAULT:
          dx1 = 0;
          dy1 = i * pinSpacing;
          dx2 = rowSpacing;
          dy2 = i * pinSpacing;
          cx = rowSpacing / 2;
          cy = i * pinSpacing;
          break;
        case _90:
          dx1 = -i * pinSpacing;
          dy1 = 0;
          dx2 = -i * pinSpacing;
          dy2 = rowSpacing;
          cx = -i * pinSpacing;
          cy = rowSpacing / 2;
          break;
        case _180:
          dx1 = 0;
          dy1 = -i * pinSpacing;
          dx2 = -rowSpacing;
          dy2 = -i * pinSpacing;
          cx = -rowSpacing / 2;
          cy = -i * pinSpacing;
          break;
        case _270:
          dx1 = i * pinSpacing;
          dy1 = 0;
          dx2 = i * pinSpacing;
          dy2 = -rowSpacing;
          cx = i * pinSpacing;
          cy = -rowSpacing / 2;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      controlPoints[i] = new Point(firstPoint.x + dx1, firstPoint.y + dy1);
      controlPoints[i + getTerminalCount()] = new Point(firstPoint.x + dx2, firstPoint.y + dy2);
      if (centerHole) {
        controlPoints[i + 2 * getTerminalCount()] = new Point(firstPoint.x + cx, firstPoint.y + cy);
      }
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      final int holeSize = (int) HOLE_SIZE.convertToPixels();
      final int terminalSpacing = (int) getTerminalSpacing().convertToPixels();
      final int holeSpacing = (int) getHoleSpacing().convertToPixels();
      final int boardWidth = (int) getBoardWidth().convertToPixels();
      final int boardLength = (getTerminalCount() - 1) * terminalSpacing + 2 * boardWidth;
      final int mountingHoleSize = getClosestOdd(MOUNTING_HOLE_SIZE.convertToPixels());
      final int halfHoleAdjuster = boardWidth / 2 - mountingHoleSize / 2;
      final int holeAdjuster = boardWidth / 2 - mountingHoleSize;
      final int heightAdjuster = boardLength - boardWidth / 2;
      final int spacingAdd = holeSpacing / 2 - boardWidth / 2;
      final int spacingSubtract = holeSpacing / 2 + boardWidth / 2;
      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int width = boardWidth;
      int height = boardLength;
      int ix1;
      final int iy1 = y + halfHoleAdjuster;
      int ix2;
      int iy2;
      switch (orientation) {
        case DEFAULT:
          x += spacingAdd;
          y -= boardWidth;
          ix1 = x + halfHoleAdjuster;
          ix2 = ix1;
          iy2 = y + heightAdjuster;
          break;
        case _90:
          width = boardLength;
          height = boardWidth;
          x += boardWidth - boardLength;
          y += spacingAdd;
          ix1 = x + holeAdjuster;
          ix2 = x + heightAdjuster;
          iy2 = iy1;
          break;
        case _180:
          x -= spacingSubtract;
          y += boardWidth - boardLength;
          ix1 = x + halfHoleAdjuster;
          ix2 = ix1;
          iy2 = y + heightAdjuster;
          break;
        case _270:
          width = boardLength;
          height = boardWidth;
          x -= boardWidth;
          y -= spacingSubtract;
          ix1 = x + holeAdjuster;
          ix2 = x + heightAdjuster;
          iy2 = iy1;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      Area indentation = new Area(new Ellipse2D.Double(
          ix1,
          iy1,
          mountingHoleSize,
          mountingHoleSize));
      indentation.add(new Area(new Ellipse2D.Double(
          ix2,
          iy2,
          mountingHoleSize,
          mountingHoleSize)));

      Area bodyArea = Area.roundRect(x, y, width, height, EDGE_RADIUS);
      bodyArea.subtract(indentation);
      body[0] = bodyArea;

      Area terminals = new Area();
      for (int i = 0; i < getTerminalCount(); i++) {
        Point p1 = getControlPoint(i);
        Point p2 = getControlPoint(i + getTerminalCount());
        if (p2.x < p1.x || p2.y < p1.y) {
          Point p = p1;
          p1 = p2;
          p2 = p;
        }

        Area terminal = Area.roundRect(
            p1.x - holeSize,
            p1.y - holeSize,
            p2.x - p1.x + holeSize * 2,
            p2.y - p1.y + holeSize * 2,
            holeSize);

        terminal.subtract(Area.circle(p1, holeSize));
        terminal.subtract(Area.circle(p2, holeSize));
        if (centerHole) {
          Point p3 = getControlPoint(i + 2 * getTerminalCount());
          Area centerHole = Area.circle(p3, holeSize);
          terminal.subtract(centerHole);
          bodyArea.subtract(centerHole);
        }

        terminals.add(terminal);
      }
      body[1] = terminals;
    }
    return body;
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
    Area mainArea = getBody()[0];

    final Composite oldComposite = setTransparency(g2d);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBoardColor());
    g2d.fill(mainArea);

    final Color finalBorderColor = tryBorderColor(outlineMode, getBorderColor());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(finalBorderColor);
    g2d.draw(mainArea);

    Area terminalArea = getBody()[1];

    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : TERMINAL_COLOR);
    drawingObserver.startTrackingContinuityArea(true);
    g2d.fill(terminalArea);
    drawingObserver.stopTrackingContinuityArea();

    final Color finalTerminalBorderColor = tryBorderColor(outlineMode, TERMINAL_BORDER_COLOR);
    g2d.setColor(finalTerminalBorderColor);
    g2d.draw(terminalArea);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    Area.rect(width / 4, 1, width / 2, height - 4).fillDraw(g2d, BOARD_COLOR, BORDER_COLOR);
    int radius = 6 * width / 32;
    int holeSize = 3 * width / 32;
    int terminalSize = getClosestOdd(height / 5);
    Area terminal = Area.roundRect(
        2 * width / 32,
        height / 5,
        width - 4 * width / 32,
        terminalSize,
        radius);
    terminal.subtract(Area.circle(
        2 * width / 32 + holeSize,
        height * 3 / 10 - holeSize / 2,
        holeSize));
    terminal.subtract(Area.circle(
        width - 2 * width / 32 - holeSize * 2,
        height * 3 / 10 - holeSize / 2,
        holeSize));
    terminal.fillDraw(g2d, TERMINAL_COLOR, TERMINAL_BORDER_COLOR);
    g2d.translate(0, height * 2 / 5);
    terminal.fillDraw(g2d, TERMINAL_COLOR, TERMINAL_BORDER_COLOR);
  }

  @EditableProperty(name = "Board")
  public Color getBoardColor() {
    if (boardColor == null) {
      boardColor = BOARD_COLOR;
    }
    return boardColor;
  }

  public void setBoardColor(Color bodyColor) {
    this.boardColor = bodyColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    if (borderColor == null) {
      borderColor = BORDER_COLOR;
    }
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return "TerminalStrip" + index;
  }

  @Override
  public boolean arePointsConnected(int index1, int index2) {
    return Math.abs(index1 - index2) == getTerminalCount();
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
