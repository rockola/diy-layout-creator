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
package org.diylc.components.guitar;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Lever Switch", category = "Guitar", author = "Branislav Stojkovic",
    description = "Strat-style lever switch", zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = "SW", keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Guitar Wiring Diagram")
public class LeverSwitch extends AbstractTransparentComponent<String> implements ISwitch {

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = Color.lightGray;
  private static Color WAFER_COLOR = Color.decode("#CD8500");
  private static Color LUG_COLOR = METAL_COLOR;
  private static Color COMMON_LUG_COLOR = Color.decode("#FF9999");

  private static Size BASE_WIDTH = new Size(10d, SizeUnit.mm);
  private static Size BASE_LENGTH = new Size(47.5d, SizeUnit.mm);
  private static Size WAFER_LENGTH = new Size(40d, SizeUnit.mm);
  private static Size WAFER_SPACING = new Size(7.62d, SizeUnit.mm);
  private static Size WAFER_THICKNESS = new Size(1.27d, SizeUnit.mm);
  private static Size HOLE_SIZE = new Size(2d, SizeUnit.mm);
  private static Size HOLE_SPACING = new Size(41.2d, SizeUnit.mm);
  private static Size TERMINAL_WIDTH = new Size(2d, SizeUnit.mm);
  private static Size TERMINAL_LENGTH = new Size(0.1d, SizeUnit.in);
  private static Size TERMINAL_SPACING = new Size(0.1d, SizeUnit.in);

  private String value = "";
  private Point[] controlPoints = new Point[] {new Point(0, 0)};
  transient Shape[] body;
  private Orientation orientation = Orientation.DEFAULT;
  private LeverSwitchType type = LeverSwitchType.DP3T;
  private Boolean highlightCommon;

  public LeverSwitch() {
    super();
    updateControlPoints();
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (componentState != ComponentState.DRAGGING) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BASE_COLOR);
      g2d.fill(body[0]);
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : WAFER_COLOR);
      g2d.fill(body[1]);
      g2d.setComposite(oldComposite);
    }

    Color finalBorderColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : BASE_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);

    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : WAFER_COLOR.darker();
    }

    g2d.draw(body[1]);

    g2d.setColor(LUG_COLOR);
    g2d.fill(body[2]);
    g2d.setColor(LUG_COLOR.darker());
    g2d.draw(body[2]);
    g2d.setColor(COMMON_LUG_COLOR);
    g2d.fill(body[3]);
    g2d.setColor(COMMON_LUG_COLOR.darker());
    g2d.draw(body[3]);
  }

  @SuppressWarnings("incomplete-switch")
  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[4];

      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int baseWidth = (int) BASE_WIDTH.convertToPixels();
      int baseLength = (int) BASE_LENGTH.convertToPixels();
      int holeSize = getClosestOdd(HOLE_SIZE.convertToPixels());
      int holeSpacing = (int) HOLE_SPACING.convertToPixels();
      int waferLength = (int) WAFER_LENGTH.convertToPixels();
      int waferSpacing = (int) WAFER_SPACING.convertToPixels();
      int waferThickness = (int) WAFER_THICKNESS.convertToPixels();
      int terminalSpacing = (int) TERMINAL_SPACING.convertToPixels();
      int terminalLength = getClosestOdd(TERMINAL_LENGTH.convertToPixels());
      int terminalWidth = getClosestOdd(TERMINAL_WIDTH.convertToPixels());
      
      int yOffset;
      if (type == LeverSwitchType.DP3T || type == LeverSwitchType.DP4T || type == LeverSwitchType.DP3T_5pos) {
        x += terminalLength;
        yOffset = 7;
      } else {
        yOffset = 12;
      }      

      int baseX = x - terminalLength / 2 - waferSpacing;
      int baseY = y - (baseLength - terminalSpacing * yOffset) / 2;
      Area baseArea = new Area(new Rectangle2D.Double(baseX, baseY, baseWidth, baseLength));
      baseArea.subtract(new Area(new Ellipse2D.Double(baseX + baseWidth / 2 - holeSize / 2, baseY
          + (baseLength - holeSpacing) / 2 - holeSize / 2, holeSize, holeSize)));
      baseArea.subtract(new Area(new Ellipse2D.Double(baseX + baseWidth / 2 - holeSize / 2, baseY
          + (baseLength - holeSpacing) / 2 - holeSize / 2 + holeSpacing, holeSize, holeSize)));
      body[0] = baseArea;

      Area waferArea =
          new Area(new Rectangle2D.Double(x - terminalLength / 2 - waferThickness / 2, y
              - (waferLength - terminalSpacing * yOffset) / 2, waferThickness,
              waferLength));

      if (type == LeverSwitchType._4P5T) {
        waferArea.add(new Area(new Rectangle2D.Double(x - terminalLength / 2 - waferThickness / 2 + waferSpacing, y
            - (waferLength - terminalSpacing * 12) / 2, waferThickness,
            waferLength)));
      }
      body[1] = waferArea;

      double theta = 0;
      // Rotate if needed
      if (orientation != Orientation.DEFAULT) {
        switch (orientation) {
          case _90:
            theta = Math.PI / 2;
            break;
          case _180:
            theta = Math.PI;
            break;
          case _270:
            theta = Math.PI * 3 / 2;
            break;
        }
      }

      Area terminalArea = new Area();
      Area commonTerminalArea = new Area();
      for (int i = 0; i < controlPoints.length; i++) {
        Point point = controlPoints[i];
        Area terminal =
            new Area(new RoundRectangle2D.Double(point.x - terminalLength / 2, point.y - terminalWidth / 2,
                terminalLength, terminalWidth, terminalWidth / 2, terminalWidth / 2));
        terminal.subtract(new Area(new RoundRectangle2D.Double(point.x - terminalLength / 4, point.y - terminalWidth
            / 4, terminalLength / 2, terminalWidth / 2, terminalWidth / 2, terminalWidth / 2)));
        // Rotate the terminal if needed
        if (theta != 0) {
          AffineTransform rotation = AffineTransform.getRotateInstance(theta, point.x, point.y);
          terminal.transform(rotation);
        }
        terminalArea.add(terminal);
        if (getHighlightCommon() && 
            (((type == LeverSwitchType.DP3T || type == LeverSwitchType.DP3T_5pos) && (i == 1 || i == 6)) ||
            (type == LeverSwitchType.DP4T && (i == 1 || i == 8)) ||
            ((type == LeverSwitchType._4P5T || type == LeverSwitchType.DP5T) && (i == 0 || i == 11 || i == 12 || i == 23))))
          commonTerminalArea.add(terminal);
        else
          terminalArea.add(terminal);
      }
      body[2] = terminalArea;
      body[3] = commonTerminalArea;

      // Rotate if needed
      if (theta != 0) {
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, controlPoints[0].x, controlPoints[0].y);
        // Skip the last two because terminals are already rotated
        for (int i = 0; i < body.length - 2; i++) {
          Shape shape = body[i];
          Area area = (Area) shape;
          area.transform(rotation);
        }
      }
    }
    return body;
  }

  @SuppressWarnings("incomplete-switch")
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
          controlPoints[i] = new Point(x + (i % 2 == 1 ? terminalLength : 0), y + i * terminalSpacing);
        }
        break;
      case DP4T:
        controlPoints = new Point[10];
        for (int i = 0; i < 10; i++) {
          controlPoints[i] = new Point(x + (i % 2 == 1 ? terminalLength : 0), y + i * terminalSpacing);
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
          controlPoints[i + 12] = new Point(x + waferSpacing, y + i * terminalSpacing + (i >= 6 ? terminalSpacing : 0));
        }
        break;
    }

    // Rotate if needed
    if (orientation != Orientation.DEFAULT) {
      double theta = 0;
      switch (orientation) {
        case _90:
          theta = Math.PI / 2;
          break;
        case _180:
          theta = Math.PI;
          break;
        case _270:
          theta = Math.PI * 3 / 2;
          break;
      }
      AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
      for (Point point : controlPoints) {
        rotation.transform(point, point);
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setClip(width / 32, width / 32, width, height);
    g2d.setColor(BASE_COLOR);
    g2d.fillRect(0, 0, width * 2 / 3, height);
    g2d.setColor(BASE_COLOR.darker());
    g2d.drawRect(0, 0, width * 2 / 3, height);
    g2d.setColor(WAFER_COLOR);
    g2d.fillRect(width / 8 * 3, 0, width / 8, height);
    g2d.setColor(WAFER_COLOR.darker());
    g2d.drawRect(width / 8 * 3, 0, width / 8, height);
    Area terminals = new Area();
    int terminalLength = getClosestOdd(11 * width / 32);
    int terminalWidth = getClosestOdd(7 * width / 32);
    Area terminal =
        new Area(new RoundRectangle2D.Double(width / 16 * 7, 4 * width / 32, terminalLength, terminalWidth,
            terminalWidth / 2, terminalWidth / 2));
    terminal.subtract(new Area(new RoundRectangle2D.Double(width / 16 * 7 + terminalLength / 4 + 1, 4 * width / 32
        + terminalWidth / 4 + 1, terminalLength / 2, terminalWidth / 2, terminalWidth / 4, terminalWidth / 4)));
    terminals.add(terminal);
    terminal = new Area(terminal);
    terminal.transform(AffineTransform.getTranslateInstance(-terminalLength, terminalWidth + 2 * width / 32));
    terminals.add(terminal);
    terminal = new Area(terminal);
    terminal.transform(AffineTransform.getTranslateInstance(terminalLength, terminalWidth + 2 * width / 32));
    terminals.add(terminal);
    g2d.setColor(METAL_COLOR);
    g2d.fill(terminals);
    g2d.setColor(METAL_COLOR.darker());
    g2d.draw(terminals);
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
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

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
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
    if (highlightCommon == null)
      highlightCommon = true;
    return highlightCommon;
  }
  
  public void setHighlightCommon(Boolean highlightCommon) {
    this.highlightCommon = highlightCommon;
    
    body = null;
  }
 
  public enum LeverSwitchType {
    DP3T("DP3T (Standard 3-Position Strat)"), DP3T_5pos("DP3T (Standard 5-Position Strat)"), _4P5T("4P5T (Super/Mega)"), DP4T("DP4T (4-Position Tele)"), DP5T("DP5T");

    private String title;

    private LeverSwitchType(String title) {
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
        return (index1 == 1 && index2 == index1 + 2 * (position + 1)) || (index2 == 6 && index2 == index1 + 2 * (3 - position));
      case DP4T:
        return (index1 == 1 && index2 == index1 + 2 * (position + 1)) || (index2 == 8 && index2 == index1 + 2 * (4 - position));
      case DP3T_5pos:
        if (position % 2 == 0)          
          return (index1 == 1 && index2 == index1 + position + 2) || (index2 == 6 && index2 == index1 + 6 - position);
        else
          return (index2 == 6 && (index1 == 2 || (index1 == 0 && position == 1) || (index1 == 4 && position == 3))) || 
              (index1 == 1 && (index2 == 5 || (index2 == 3 && position == 1) || (index2 == 7 && position == 3)));
      case DP5T:
        return (index1 == 0 || index2 == 11) && index2 - index1 == position + 1;
      case _4P5T:
        return (index1 == 0 || index1 == 12 || index2 == 11 || index2 == 23) && index2 - index1 == position + 1;        
    }
    return false;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
