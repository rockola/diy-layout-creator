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

package org.diylc.components.electromechanical;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "Plastic DC Jack",
    category = "Electro-Mechanical",
    author = "Branislav Stojkovic",
    description = "Panel mount plastic DC jack",
    zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = "J",
    autoEdit = false)
public class PlasticDCJack extends AbstractMultiPartComponent<String> {

  private static final long serialVersionUID = 1L;

  private static Size LUG_WIDTH = Size.in(0.08);
  private static Size LUG_THICKNESS = Size.in(0.02);
  private static Size SPACING = Size.in(0.1);
  private static Size DIAMETER = Size.in(0.5);
  private static Color BODY_COLOR = Color.decode("#666666");
  private static Color PHENOLIC_COLOR = Color.decode("#CD8500");
  private static Color BORDER_COLOR = Color.black;
  private static Color MARKING_COLOR = Color.lightGray;

  private Point[] controlPoints = new Point[] {
    new Point(0, 0),
    new Point(0, 0),
    new Point(0, 0)
  };
  private String value = "";
  private DCPolarity polarity = DCPolarity.CENTER_NEGATIVE;
  private transient Area[] body;

  public PlasticDCJack() {
    updateControlPoints();
  }

  private void updateControlPoints() {
    // invalidate body shape
    body = null;

    int x = controlPoints[0].x;
    int y = controlPoints[0].y;

    int spacing = (int) SPACING.convertToPixels();
    controlPoints[1] = new Point(x + spacing, y + spacing);
    controlPoints[2] = new Point(x - spacing, y + spacing * 2);
  }

  @Override
  public Area[] getBody() {
    if (body == null) {
      body = new Area[4];

      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int spacing = (int) SPACING.convertToPixels();
      int diameter = getClosestOdd(DIAMETER.convertToPixels());
      body[0] = Area.circle(x, y + spacing, diameter);
      int rectWidth = (int) (diameter / SQRT_TWO) - 2;
      body[1] = Area.centeredSquare(x, y + spacing, rectWidth);
      int lugWidth = getClosestOdd(LUG_WIDTH.convertToPixels());
      int lugThickness = getClosestOdd(LUG_THICKNESS.convertToPixels());
      Point groundPoint = controlPoints[controlPoints.length - 1];
      Area groundLug = Area.circle(groundPoint.x + spacing, groundPoint.y, lugWidth);
      groundLug.add(Area.rect(
          groundPoint.x,
          groundPoint.y - lugWidth / 2,
          spacing,
          lugWidth));
      groundLug.subtract(Area.circle(groundPoint.x + spacing, groundPoint.y, lugWidth / 3));
      body[2] = groundLug;

      Area lugArea = new Area();
      for (int i = 0; i < controlPoints.length; i++) {
        Point point = controlPoints[i];
        if (i == getControlPointCount() - 1) {
          lugArea.add(Area.rect(point, lugThickness, lugWidth));
        } else {
          lugArea.add(Area.rect(point, lugWidth, lugThickness));
        }
      }
      body[3] = lugArea;
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
    Shape[] body = getBody();

    Composite oldComposite = setTransparency(g2d);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
    g2d.fill(body[0]);
    if (!outlineMode) {
      g2d.setColor(PHENOLIC_COLOR);
      g2d.fill(body[1]);
    }
    g2d.setComposite(oldComposite);

    final Color finalBorderColor = tryBorderColor(outlineMode, BORDER_COLOR);
    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);
    if (!outlineMode) {
      g2d.setColor(PHENOLIC_COLOR.darker());
      g2d.draw(body[1]);

      g2d.setColor(METAL_COLOR);
      g2d.fill(body[2]);
      g2d.setColor(METAL_COLOR.darker());
      g2d.draw(body[2]);

      g2d.setColor(METAL_COLOR);
      g2d.fill(body[3]);
    }

    g2d.setColor(tryColor(outlineMode, METAL_COLOR.darker()));
    g2d.draw(body[3]);

    if (!outlineMode && !getPolarity().isNone()) {
      int spacing = (int) SPACING.convertToPixels();
      g2d.setColor(MARKING_COLOR);
      g2d.setFont(project.getFont().deriveFont(12f));
      StringUtils.drawCenteredText(
          g2d,
          getPolarity().isCenterNegative() ? "+" : "-",
          controlPoints[0].x,
          controlPoints[0].y - spacing * 7 / 16);
      StringUtils.drawCenteredText(
          g2d,
          getPolarity().isCenterNegative() ? "-" : "+",
          controlPoints[2].x,
          controlPoints[2].y - spacing * 3 / 4);
    }

    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = 2 * 32 / width;
    int diameter = getClosestOdd(width - margin);
    Point center = new Point(width / 2, height / 2);
    Area.circle(center, diameter).fillDraw(g2d, BODY_COLOR, BORDER_COLOR);
    int rectWidth = getClosestOdd(((width - 2 * margin) / SQRT_TWO) - margin / 2);
    Area.centeredSquare(center, rectWidth).fillDraw(g2d, PHENOLIC_COLOR, PHENOLIC_COLOR.darker());
    int lugWidth = 4 * 32 / width;
    g2d.setColor(METAL_COLOR);
    g2d.drawLine((width - lugWidth) / 2, height / 3, (width + lugWidth) / 2, height / 3);
    g2d.drawLine(width * 2 / 3, (height - lugWidth) / 2, width * 2 / 3, (height + lugWidth) / 2);
    g2d.fillOval(
        (width - lugWidth) / 2,
        height * 2 / 3 - lugWidth / 2,
        lugWidth,
        lugWidth);
    g2d.fillRect(
        width / 2 - lugWidth * 3 / 2,
        height * 2 / 3 - lugWidth / 2,
        lugWidth * 3 / 2,
        lugWidth);
    g2d.setColor(PHENOLIC_COLOR);
    g2d.fillOval(
        (width - margin) / 2,
        height * 2 / 3 - margin / 2,
        margin,
        margin);
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
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    this.body = null;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @EditableProperty
  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public DCPolarity getPolarity() {
    if (polarity == null) {
      polarity = DCPolarity.CENTER_NEGATIVE;
    }
    return polarity;
  }

  public void setPolarity(DCPolarity polarity) {
    this.polarity = polarity;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
