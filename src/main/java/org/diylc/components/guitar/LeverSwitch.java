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

package org.diylc.components.guitar;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractComponent;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.StringValue;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@StringValue
@ComponentDescriptor(
    name = "Lever Switch",
    category = "Guitar",
    author = "Branislav Stojkovic",
    description = "Strat-style lever switch",
    zOrder = AbstractComponent.COMPONENT,
    instanceNamePrefix = "SW",
    keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Guitar Wiring Diagram")
public class LeverSwitch extends AbstractTransparentComponent implements ISwitch {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(LeverSwitch.class);

  private static Color BASE_COLOR = Color.lightGray;
  private static Color WAFER_COLOR = Color.decode("#CD8500");
  private static Color LUG_COLOR = METAL_COLOR;
  private static Color COMMON_LUG_COLOR = Color.decode("#FF9999");

  private static Size BASE_WIDTH = Size.mm(10);
  private static Size BASE_LENGTH = Size.mm(47.5);
  private static Size WAFER_LENGTH = Size.mm(40);
  private static Size WAFER_SPACING = Size.mm(7.62);
  private static Size WAFER_THICKNESS = Size.mm(1.27);
  private static Size HOLE_SIZE = Size.mm(2);
  private static Size HOLE_SPACING = Size.mm(41.2);
  private static Size TERMINAL_WIDTH = Size.mm(2);
  private static Size TERMINAL_LENGTH = Size.in(0.1);
  private static Size TERMINAL_SPACING = Size.in(0.1);

  private Point[] controlPoints = new Point[] {new Point(0, 0)};
  transient Area[] body;
  private Orientation orientation = Orientation.DEFAULT;
  private LeverSwitchType type = LeverSwitchType.DP3T;
  private Boolean highlightCommon;

  public LeverSwitch() {
    super();
    updateControlPoints();
  }

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    Area[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (!componentState.isDragging()) {
      Composite oldComposite = setTransparency(g2d);
      body[0].fill(g2d, outlineMode ? Constants.TRANSPARENT_COLOR : BASE_COLOR);
      body[1].fill(g2d, outlineMode ? Constants.TRANSPARENT_COLOR : WAFER_COLOR);
      g2d.setComposite(oldComposite);
    }

    body[0].draw(g2d, tryBorderColor(outlineMode, BASE_COLOR.darker()));
    body[1].draw(g2d, tryBorderColor(outlineMode, WAFER_COLOR.darker()));
    body[2].fillDraw(g2d, LUG_COLOR, LUG_COLOR.darker());
    body[3].fillDraw(g2d, COMMON_LUG_COLOR, COMMON_LUG_COLOR.darker());
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[4];

      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int baseWidth = (int) BASE_WIDTH.convertToPixels();
      int baseLength = (int) BASE_LENGTH.convertToPixels();
      final int holeSize = getClosestOdd(HOLE_SIZE.convertToPixels());
      final int holeSpacing = (int) HOLE_SPACING.convertToPixels();
      final int waferLength = (int) WAFER_LENGTH.convertToPixels();
      final int waferSpacing = (int) WAFER_SPACING.convertToPixels();
      final int waferThickness = (int) WAFER_THICKNESS.convertToPixels();
      final int terminalSpacing = (int) TERMINAL_SPACING.convertToPixels();
      final int terminalLength = getClosestOdd(TERMINAL_LENGTH.convertToPixels());
      final int terminalWidth = getClosestOdd(TERMINAL_WIDTH.convertToPixels());

      int offsetY = 12;
      if (type == LeverSwitchType.DP3T
          || type == LeverSwitchType.DP4T
          || type == LeverSwitchType.DP3T_5pos) {
        x += terminalLength;
        offsetY = 7;
      }

      int baseX = x - terminalLength / 2 - waferSpacing;
      int baseY = y - (baseLength - terminalSpacing * offsetY) / 2;
      Area baseArea =
          Area.rect(baseX, baseY, baseWidth, baseLength)
              .subtract(
                  Area.circle(
                      baseX + baseWidth / 2, baseY + (baseLength - holeSpacing) / 2, holeSize))
              .subtract(
                  Area.circle(
                      baseX + baseWidth / 2,
                      baseY + (baseLength - holeSpacing) / 2 + holeSpacing,
                      holeSize));
      body[0] = baseArea;

      Area waferArea =
          Area.rect(
              x - terminalLength / 2 - waferThickness / 2,
              y - (waferLength - terminalSpacing * offsetY) / 2,
              waferThickness,
              waferLength);

      if (type == LeverSwitchType._4P5T) {
        waferArea.add(
            Area.rect(
                x - terminalLength / 2 - waferThickness / 2 + waferSpacing,
                y - (waferLength - terminalSpacing * 12) / 2,
                waferThickness,
                waferLength));
      }
      body[1] = waferArea;

      Area terminalArea = new Area();
      Area commonTerminalArea = new Area();
      final double theta = orientation.getTheta();
      for (int i = 0; i < controlPoints.length; i++) {
        Point point = controlPoints[i];
        Area terminal =
            Area.centeredRoundRect(point, terminalLength, terminalWidth, terminalWidth / 2)
                .subtract(
                    Area.centeredRoundRect(
                        point, terminalLength / 2, terminalWidth / 2, terminalWidth / 2));
        // Rotate the terminal if needed
        if (theta != 0) {
          terminal.transform(orientation.getRotation(point));
        }
        terminalArea.add(terminal);
        if (getHighlightCommon()
            && (((type == LeverSwitchType.DP3T || type == LeverSwitchType.DP3T_5pos)
                    && (i == 1 || i == 6))
                || (type == LeverSwitchType.DP4T && (i == 1 || i == 8))
                || ((type == LeverSwitchType._4P5T || type == LeverSwitchType.DP5T)
                    && (i == 0 || i == 11 || i == 12 || i == 23)))) {
          commonTerminalArea.add(terminal);
        } else {
          terminalArea.add(terminal);
        }
      }
      body[2] = terminalArea;
      body[3] = commonTerminalArea;

      // Rotate if needed
      if (orientation.isRotated()) {
        AffineTransform rotation = orientation.getRotation(controlPoints[0]);
        // Skip the last two because terminals are already rotated
        for (int i = 0; i < body.length - 2; i++) {
          body[i].transform(rotation);
        }
      }
    }
    return body;
  }

  private void updateControlPoints() {
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;
    int waferSpacing = (int) WAFER_SPACING.convertToPixels();
    int terminalSpacing = (int) TERMINAL_SPACING.convertToPixels();
    int terminalLength = (int) TERMINAL_LENGTH.convertToPixels();

    switch (type) {
      case DP3T:
      case DP3T_5pos:
        controlPoints = new Point[8];
        for (int i = 0; i < 8; i++) {
          controlPoints[i] =
              new Point(x + (i % 2 == 1 ? terminalLength : 0), y + i * terminalSpacing);
        }
        break;
      case DP4T:
        controlPoints = new Point[10];
        for (int i = 0; i < 10; i++) {
          controlPoints[i] =
              new Point(x + (i % 2 == 1 ? terminalLength : 0), y + i * terminalSpacing);
        }
        break;
      case DP5T:
        controlPoints = new Point[12];
        for (int i = 0; i < 12; i++) {
          controlPoints[i] = new Point(x, y + i * terminalSpacing + (i >= 6 ? terminalSpacing : 0));
        }
        break;
      case _4P5T:
        controlPoints = new Point[24];
        for (int i = 0; i < 12; i++) {
          controlPoints[i] = new Point(x, y + i * terminalSpacing + (i >= 6 ? terminalSpacing : 0));
          controlPoints[i + 12] =
              new Point(x + waferSpacing, y + i * terminalSpacing + (i >= 6 ? terminalSpacing : 0));
        }
        break;
      default:
        LOG.error("Unknown type {}", type);
        throw new RuntimeException("Unknown switch type " + type.toString());
    }

    // Rotate if needed
    if (orientation != Orientation.DEFAULT) {
      double theta = orientation.getTheta();
      AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
      for (Point point : controlPoints) {
        rotation.transform(point, point);
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setClip(width / 32, width / 32, width, height);
    Area.rect(0, 0, width * 2 / 3, height).fillDraw(g2d, BASE_COLOR, BASE_COLOR.darker());
    Area.rect(width / 8 * 3, 0, width / 8, height).fillDraw(g2d, WAFER_COLOR, WAFER_COLOR.darker());
    Area terminals = new Area();
    int terminalLength = getClosestOdd(11 * width / 32);
    int terminalWidth = getClosestOdd(7 * width / 32);
    Area terminal =
        Area.roundRect(
                width / 16 * 7, 4 * width / 32, terminalLength, terminalWidth, terminalWidth / 2)
            .subtract(
                Area.roundRect(
                    width / 16 * 7 + terminalLength / 4 + 1,
                    4 * width / 32 + terminalWidth / 4 + 1,
                    terminalLength / 2,
                    terminalWidth / 2,
                    terminalWidth / 4));
    terminals.add(terminal);
    terminal = new Area(terminal);
    terminal.transform(
        AffineTransform.getTranslateInstance(-terminalLength, terminalWidth + 2 * width / 32));
    terminals.add(terminal);
    terminal = new Area(terminal);
    terminal.transform(
        AffineTransform.getTranslateInstance(terminalLength, terminalWidth + 2 * width / 32));
    terminals.add(terminal);
    terminals.fillDraw(g2d, METAL_COLOR, METAL_COLOR.darker());
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.controlPoints[index].setLocation(point);
    // Invalidate the body
    body = null;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  @EditableProperty
  public LeverSwitchType getType() {
    return type;
  }

  public void setType(LeverSwitchType type) {
    this.type = type;
    updateControlPoints();
    // Invalidate body
    this.body = null;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Invalidate the body
    body = null;
  }

  @EditableProperty(name = "Mark Common Lugs")
  public Boolean getHighlightCommon() {
    if (highlightCommon == null) {
      highlightCommon = true;
    }
    return highlightCommon;
  }

  public void setHighlightCommon(Boolean highlightCommon) {
    this.highlightCommon = highlightCommon;

    body = null;
  }

  public enum LeverSwitchType {
    DP3T("DP3T (Standard 3-Position Strat)"),
    DP3T_5pos("DP3T (Standard 5-Position Strat)"),
    _4P5T("4P5T (Super/Mega)"),
    DP4T("DP4T (4-Position Tele)"),
    DP5T("DP5T");

    private String title;

    LeverSwitchType(String title) {
      this.title = title;
    }

    @Override
    public String toString() {
      return title;
    }
  }

  //  @Override
  //  public String getControlPointNodeName(int index) {
  //    // we don't want the switch to produce any nodes, it just makes connections
  //    return null;
  //  }

  // switch stuff

  @Override
  public int getPositionCount() {
    switch (type) {
      case DP3T:
        return 3;
      case DP4T:
        return 4;
      case DP3T_5pos:
      case DP5T:
      case _4P5T:
        return 5;
      default:
    }
    return 0;
  }

  @Override
  public String getPositionName(int position) {
    return Integer.toString(position + 1);
  }

  @Override
  public boolean arePointsConnected(int index1, int index2, int position) {
    switch (type) {
      case DP3T:
        return (index1 == 1 && index2 == index1 + 2 * (position + 1))
            || (index2 == 6 && index2 == index1 + 2 * (3 - position));
      case DP4T:
        return (index1 == 1 && index2 == index1 + 2 * (position + 1))
            || (index2 == 8 && index2 == index1 + 2 * (4 - position));
      case DP3T_5pos:
        if (position % 2 == 0) {
          return (index1 == 1 && index2 == index1 + position + 2)
              || (index2 == 6 && index2 == index1 + 6 - position);
        } else {
          return (index2 == 6
                  && (index1 == 2
                      || (index1 == 0 && position == 1)
                      || (index1 == 4 && position == 3)))
              || (index1 == 1
                  && (index2 == 5
                      || (index2 == 3 && position == 1)
                      || (index2 == 7 && position == 3)));
        }
      case DP5T:
        return (index1 == 0 || index2 == 11) && index2 - index1 == position + 1;
      case _4P5T:
        return (index1 == 0 || index1 == 12 || index2 == 11 || index2 == 23)
            && index2 - index1 == position + 1;
      default:
    }
    return false;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
