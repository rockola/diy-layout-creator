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

package org.diylc.components.semiconductors;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import org.diylc.awt.StringUtils;
import org.diylc.common.Display;
import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "Op-amp",
    author = "Branislav Stojkovic",
    category = "Schematic Symbols",
    instanceNamePrefix = "IC",
    description = "Operational amplifier symbol with 3 or 5 contacts",
    zOrder = IDIYComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_TAG_AND_VALUE,
    keywordTag = "Schematic")
public class OperationalAmplifierSymbol extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  public static final Size PIN_SPACING = Size.in(0.1);
  public static final Color BODY_COLOR = Color.white;
  public static final Color BORDER_COLOR = Color.black;
  public static final int DEFAULT_POINT_COUNT = 5;

  protected int pointCount = DEFAULT_POINT_COUNT;
  protected String value = "";
  protected Point[] controlPoints = getFreshControlPoints(DEFAULT_POINT_COUNT);
  protected Color bodyColor = BODY_COLOR;
  protected Color borderColor = BORDER_COLOR;

  private Boolean flip;
  private transient Area[] body;

  public OperationalAmplifierSymbol() {
    super();
    updateControlPoints();
    display = Display.NAME;
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
    Area[] body = getBody();

    Composite oldComposite = setTransparency(g2d);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(body[0]);
    g2d.setComposite(oldComposite);

    Color finalBorderColor = tryBorderColor(outlineMode, borderColor);
    g2d.setColor(finalBorderColor);
    // Draw contacts
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(body[1]);
    // Draw triangle
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    g2d.draw(body[0]);
    // Draw label
    g2d.setFont(project.getFont());
    Color finalLabelColor = tryLabelColor(outlineMode, LABEL_COLOR);
    g2d.setColor(finalLabelColor);
    int x = (controlPoints[0].x + controlPoints[2].x) / 2;
    String label = getLabelForDisplay();
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    StringUtils.drawCenteredText(g2d, label, x, controlPoints[0].y + pinSpacing);
    // Draw +/- markers
    StringUtils.drawCenteredText(
        g2d, getFlip() ? "+" : "-", controlPoints[0].x + pinSpacing, controlPoints[0].y);
    StringUtils.drawCenteredText(
        g2d, getFlip() ? "-" : "+", controlPoints[1].x + pinSpacing, controlPoints[1].y);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = 3 * width / 32;
    Path2D polygon = new Path2D.Double();
    polygon.moveTo(margin, margin);
    polygon.lineTo(margin, height - margin);
    polygon.lineTo(width - margin, height / 2);
    polygon.closePath();
    Area area = new Area(polygon);
    // area.subtract(new Area(new Rectangle2D.Double(0, 0, 2 * margin,
    // height)));
    area.intersect(Area.rect(2 * margin, 0, width, height));
    area.fill(g2d, BODY_COLOR);
    g2d.setColor(BORDER_COLOR);
    g2d.setFont(LABEL_FONT.deriveFont(8f));
    StringUtils.drawCenteredText(g2d, "-", 3 * margin, height / 3);
    StringUtils.drawCenteredText(g2d, "+", 3 * margin + 1, height * 2 / 3);
    // g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND,
    // BasicStroke.JOIN_ROUND));
    g2d.draw(area);
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public int getControlPointCount() {
    return pointCount;
  }

  private void updateControlPoints() {
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    // Update control points.
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;

    controlPoints[1].x = x;
    controlPoints[1].y = y + pinSpacing * 2;

    controlPoints[2].x = x + pinSpacing * 6;
    controlPoints[2].y = y + pinSpacing;

    controlPoints[3].x = x + pinSpacing * 3;
    controlPoints[3].y = y - pinSpacing;

    controlPoints[4].x = x + pinSpacing * 3;
    controlPoints[4].y = y + pinSpacing * 3;
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      int pinSpacing = (int) PIN_SPACING.convertToPixels();
      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      Path2D triangle = new Path2D.Double();
      triangle.moveTo(x + pinSpacing / 2, y - pinSpacing * 3 / 2);
      triangle.lineTo(x + pinSpacing * 11 / 2, y + pinSpacing);
      triangle.lineTo(x + pinSpacing / 2, y + pinSpacing * 7 / 2);
      triangle.closePath();
      body[0] = new Area(triangle);

      Path2D polyline = new Path2D.Double();
      polyline.moveTo(controlPoints[0].x, controlPoints[0].y);
      polyline.lineTo(controlPoints[0].x + pinSpacing / 2, controlPoints[0].y);
      polyline.moveTo(controlPoints[1].x, controlPoints[1].y);
      polyline.lineTo(controlPoints[1].x + pinSpacing / 2, controlPoints[1].y);
      polyline.moveTo(controlPoints[2].x, controlPoints[2].y);
      polyline.lineTo(controlPoints[2].x - pinSpacing / 2, controlPoints[2].y);
      if (pointCount == 5) {
        polyline.moveTo(controlPoints[3].x, controlPoints[3].y);
        polyline.lineTo(controlPoints[3].x, controlPoints[3].y + pinSpacing * 3 / 4);
        polyline.moveTo(controlPoints[4].x, controlPoints[4].y);
        polyline.lineTo(controlPoints[4].x, controlPoints[4].y - pinSpacing * 3 / 4);
      }
      body[1] = new Area(polyline);
    }
    return body;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @EditableProperty
  @Override
  public String getValue() {
    return this.value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  @EditableProperty(name = "Contacts (3 or 5)")
  public int getPointCount() {
    return pointCount;
  }

  public void setPointCount(int pointCount) {
    if (pointCount == 3 || pointCount == 5) {
      this.pointCount = pointCount;
      updateControlPoints();
      body = null;
    }
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty
  public Boolean getFlip() {
    if (flip == null) {
      flip = false;
    }
    return flip;
  }

  public void setFlip(Boolean flip) {
    this.flip = flip;
  }

  @EditableProperty
  public Display getDisplay() {
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
