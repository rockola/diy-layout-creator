/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2019 held jointly by the individual authors.

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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractComponent;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;

@ComponentDescriptor(
    name = "Breadboard",
    category = "Boards",
    author = "Branislav Stojkovic",
    description = "Prototyping solderless breadboard",
    instanceNamePrefix = "BB",
    zOrder = AbstractComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME,
    autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME)
public class Breadboard extends AbstractComponent {

  private static final long serialVersionUID = 1L;

  public static final Color FILL_COLOR = Color.white;
  public static final Color BORDER_COLOR = Color.black;
  public static final Size BODY_ARC = Size.mm(3);
  public static final Size SPACING = Size.in(0.1);
  public static final Color HOLE_COLOR = Color.decode("#EEEEEE");
  public static final Color PLUS_COLOR = Color.red;
  public static final Color MINUS_COLOR = Color.blue;
  public static final float COORDINATE_FONT_SIZE = 9f;
  public static final Color COORDINATE_COLOR = Color.gray.brighter();
  public static final Size HOLE_SIZE = Size.mm(1.5);
  public static final Size HOLE_ARC = Size.mm(1);

  protected BreadboardSize breadboardSize;
  protected PowerStripPosition powerStripPosition;
  protected Orientation orientation;

  public Breadboard() {
    super();
    controlPoints = getFreshControlPoints(2);
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
    Point point = controlPoints[0];
    // adjust the angle
    getOrientation().applyRotation(g2d, point);

    // draw body
    g2d.setColor(FILL_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    int holeCount = getBreadboardSize() == BreadboardSize.Full ? 63 : 30;
    double width = 23 * project.getGridSpacing().convertToPixels();
    double height = (holeCount + 1) * project.getGridSpacing().convertToPixels();
    int bodyArc = (int) BODY_ARC.convertToPixels();
    // NOTE: ignoring outline mode, may not be correct
    Area.roundRect(point, width, height, bodyArc)
        .fillDraw(g2d, FILL_COLOR, tryBorderColor(false, BORDER_COLOR));

    drawingObserver.stopTracking();

    // draw lines
    double spacing = SPACING.convertToPixels();
    g2d.setColor(PLUS_COLOR);
    g2d.drawLine(
        (int) (point.x + spacing),
        (int) (point.y + spacing),
        (int) (point.x + spacing),
        (int) (point.y + holeCount * spacing));
    g2d.drawLine(
        (int) (point.x + 19 * spacing),
        (int) (point.y + spacing),
        (int) (point.x + 19 * spacing),
        (int) (point.y + holeCount * spacing));
    g2d.setColor(MINUS_COLOR);
    g2d.drawLine(
        (int) (point.x + 4 * spacing),
        (int) (point.y + spacing),
        (int) (point.x + 4 * spacing),
        (int) (point.y + holeCount * spacing));
    g2d.drawLine(
        (int) (point.x + 22 * spacing),
        (int) (point.y + spacing),
        (int) (point.x + 22 * spacing),
        (int) (point.y + holeCount * spacing));

    int holeSize = getClosestOdd(HOLE_SIZE.convertToPixels());
    int holeArc = (int) HOLE_ARC.convertToPixels();

    g2d.setFont(LABEL_FONT.deriveFont(COORDINATE_FONT_SIZE));
    byte a = "a".getBytes()[0];

    // draw main holes
    for (int section = 0; section <= 1; section++) {
      double offset = section * 7 * spacing;

      for (int y = 0; y < holeCount; y++) {
        g2d.setColor(COORDINATE_COLOR);
        int coordinateX = (int) (point.x + offset + (section == 0 ? 5.5 : 10.5) * spacing);
        StringUtils.drawCenteredText(
            g2d,
            Integer.toString(y + 1),
            coordinateX,
            (int) (point.y + (y + 1) * spacing),
            (section == 0 ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT),
            VerticalAlignment.CENTER);
        for (int x = 0; x < 5; x++) {
          int holeX = (int) (point.x + offset + (x + 6) * spacing);
          int holeY = (int) (point.y + (y + 1) * spacing);
          Area.roundRect(holeX, holeY, holeSize, holeSize, holeArc)
              .fillDraw(g2d, HOLE_COLOR, BORDER_COLOR);

          // Draw horizontal labels
          if (y == 0) {
            g2d.setColor(COORDINATE_COLOR);
            String label = new String(new byte[] {(byte) (a + x + 5 * section)});
            for (int i = 0; i < 2; i++) {
              StringUtils.drawCenteredText(
                  g2d,
                  label,
                  holeX,
                  (int) (i == 0 ? point.y : point.y + spacing * 30 + COORDINATE_FONT_SIZE / 2),
                  HorizontalAlignment.CENTER,
                  VerticalAlignment.TOP);
            }
          }
        }
      }
    }

    double powerOffset =
        getPowerStripPosition() == PowerStripPosition.Inline
            ? (getBreadboardSize() == BreadboardSize.Full ? 2d : 1d)
            : (getBreadboardSize() == BreadboardSize.Full ? 1.5d : 0.5d);

    int psHoleCount = Math.round(holeCount / 10f) * 10;

    // draw power strip holes
    for (int section = 0; section <= 1; section++) {
      double offset = section * 18 * spacing;
      for (int y = 0; y < psHoleCount - 1; y++) {
        for (int x = 0; x < 2; x++) {
          if ((y + 1) % 6 == 0) {
            continue;
          }
          int holeX = (int) (point.x + offset + (x + 2) * spacing);
          int holeY = (int) (point.y + (y + 1 + powerOffset) * spacing);
          g2d.setColor(HOLE_COLOR);
          g2d.fillRoundRect(
              holeX - holeSize / 2, holeY - holeSize / 2, holeSize, holeSize, holeArc, holeArc);
          g2d.setColor(BORDER_COLOR);
          g2d.drawRoundRect(
              holeX - holeSize / 2, holeY - holeSize / 2, holeSize, holeSize, holeArc, holeArc);
        }
      }
    }

    // draw transparent connections just to designate connections
    drawingObserver.startTrackingContinuityArea(true);
    final Composite oldComposite = g2d.getComposite();
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f));

    for (int section = 0; section <= 1; section++) {
      double offset = section * 18 * spacing;
      for (int x = 0; x < 2; x++) {
        int holeX = (int) (point.x + offset + (x + 2) * spacing);
        int holeY1 = (int) (point.y + (1 + powerOffset) * spacing);
        int holeY2 = (int) (point.y + (psHoleCount - 1 + powerOffset) * spacing);
        g2d.setColor(HOLE_COLOR);
        g2d.fillRoundRect(
            holeX - holeSize / 2,
            holeY1 - holeSize / 2,
            holeSize,
            holeSize + holeY2 - holeY1,
            holeArc,
            holeArc);
      }
    }

    for (int section = 0; section <= 1; section++) {
      double offset = section * 7 * spacing;

      for (int y = 0; y < holeCount; y++) {
        g2d.setColor(COORDINATE_COLOR);
        int coordinateX;
        if (section == 0) {
          coordinateX = (int) (point.x + offset + 5.5 * spacing);
        } else {
          coordinateX = (int) (point.x + offset + 10.5 * spacing);
        }
        StringUtils.drawCenteredText(
            g2d,
            Integer.toString(y + 1),
            coordinateX,
            (int) (point.y + (y + 1) * spacing),
            (section == 0 ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT),
            VerticalAlignment.CENTER);
        //        for (int x = 0; x < 5; x++) {
        int holeX1 = (int) (point.x + offset + 6 * spacing);
        int holeX2 = (int) (point.x + offset + 10 * spacing);
        int holeY = (int) (point.y + (y + 1) * spacing);
        g2d.setColor(HOLE_COLOR);
        g2d.fillRoundRect(
            holeX1 - holeSize / 2,
            holeY - holeSize / 2,
            holeSize + holeX2 - holeX1,
            holeSize,
            holeArc,
            holeArc);
        //          }
      }
      //      }
    }

    g2d.setComposite(oldComposite);
    drawingObserver.stopTrackingContinuityArea();
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    Area.rect(0, 0, width - 1, height - 1).fillDraw(g2d, FILL_COLOR, BORDER_COLOR);
    final double holeHorizontalOffset = width / 8;
    final double holeVerticalOffset = height / 4;
    final double holeHorizontalSpacing = width / 4;
    final double holeVerticalSpacing = height / 4;
    final double holeDiameter = width / 12;
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 4; col++) {
        Area.circle(
                holeHorizontalOffset + col * holeHorizontalSpacing,
                holeVerticalOffset + row * holeVerticalSpacing,
                holeDiameter)
            .fillDraw(g2d, HOLE_COLOR, BORDER_COLOR);
      }
    }

    final int lineOffset = 3;
    g2d.setColor(MINUS_COLOR);
    g2d.drawLine(width / 2, lineOffset, width / 2, height - lineOffset);
  }

  @EditableProperty
  public Orientation getOrientation() {
    if (orientation == null) {
      orientation = Orientation.DEFAULT;
    }
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
  }

  @EditableProperty(name = "Size")
  public BreadboardSize getBreadboardSize() {
    if (breadboardSize == null) {
      breadboardSize = BreadboardSize.Half;
    }
    return breadboardSize;
  }

  public void setBreadboardSize(BreadboardSize breadboardSize) {
    this.breadboardSize = breadboardSize;
  }

  @EditableProperty(name = "Power Strip")
  public PowerStripPosition getPowerStripPosition() {
    if (powerStripPosition == null) {
      powerStripPosition = PowerStripPosition.Inline;
    }
    return powerStripPosition;
  }

  public void setPowerStripPosition(PowerStripPosition powerStripPosition) {
    this.powerStripPosition = powerStripPosition;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  /*
  @Override
  public Point getControlPoint(int index) {
    if (index == 0) {
      return point;
    }
    double spacing = SPACING.convertToPixels();
    int holeCount = (getBreadboardSize() == BreadboardSize.Full) ? 63 : 30;
    // adjust the angle
    Point secondPoint =
        new Point((int) (point.x + 23 * spacing), (int) (point.y + (holeCount + 1) * spacing));
    double theta = getOrientation().getTheta();
    if (theta != 0) {
      AffineTransform tx = getOrientation().getRotation(point);
      tx.transform(secondPoint, secondPoint);
    }
    return controlPoints[index];
  }
  */

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  public enum BreadboardSize {
    Half,
    Full
  }

  public enum PowerStripPosition {
    Inline,
    Offset
  }
}
