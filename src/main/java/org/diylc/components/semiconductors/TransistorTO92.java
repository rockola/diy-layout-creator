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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import org.diylc.common.ObjectCache;
import org.diylc.components.Area;
import org.diylc.components.transform.TO92Transformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "Transistor (TO-92)",
    author = "Branislav Stojkovic",
    category = "Semiconductors",
    instanceNamePrefix = "Q",
    description = "Transistor with small plastic or epoxy body",
    zOrder = IDIYComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_VALUE,
    transformer = TO92Transformer.class)
public class TransistorTO92 extends AbstractTransistorPackage {

  private static final long serialVersionUID = 1L;

  public static final Color BODY_COLOR = Color.gray;
  public static final Color BORDER_COLOR = Color.gray.darker();
  public static final Color PIN_COLOR = Color.decode("#00B2EE");
  public static final Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static final Color LABEL_COLOR = Color.white;
  public static final Size PIN_SIZE = Size.in(0.03);
  public static final Size PIN_SPACING = Size.in(0.05);
  public static final Size BODY_DIAMETER = Size.in(0.2);

  private boolean folded = false;
  private Size pinSpacing = PIN_SPACING;

  public TransistorTO92() {
    super();
    updateControlPoints();
    bodyColor = BODY_COLOR;
    borderColor = BORDER_COLOR;
  }

  @EditableProperty
  public boolean getFolded() {
    return folded;
  }

  public void setFolded(boolean folded) {
    this.folded = folded;
    // Reset body shape;
    body = null;
  }

  @EditableProperty(name = "Pin spacing")
  public Size getPinSpacing() {
    if (pinSpacing == null) {
      // TODO why not PIN_SPACING? //ola 20200209
      pinSpacing = Size.in(0.1);
    }
    return pinSpacing;
  }

  public void setPinSpacing(Size pinSpacing) {
    this.pinSpacing = pinSpacing;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @Override
  protected void updateControlPoints() {
    int pinSpacing = (int) getPinSpacing().convertToPixels();
    // Update control points.
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;
    switch (orientation) {
      case DEFAULT:
        controlPoints[1].setLocation(x, y + pinSpacing);
        controlPoints[2].setLocation(x, y + 2 * pinSpacing);
        break;
      case _90:
        controlPoints[1].setLocation(x - pinSpacing, y);
        controlPoints[2].setLocation(x - 2 * pinSpacing, y);
        break;
      case _180:
        controlPoints[1].setLocation(x, y - pinSpacing);
        controlPoints[2].setLocation(x, y - 2 * pinSpacing);
        break;
      case _270:
        controlPoints[1].setLocation(x + pinSpacing, y);
        controlPoints[2].setLocation(x + 2 * pinSpacing, y);
        break;
      default:
        throw new RuntimeException("Unexpected orientation: " + orientation);
    }
  }

  public Area[] getBody() {
    if (body == null) {
      int x = (controlPoints[0].x + controlPoints[1].x + controlPoints[2].x) / 3;
      int y = (controlPoints[0].y + controlPoints[1].y + controlPoints[2].y) / 3;
      int bodyDiameter = getClosestOdd(BODY_DIAMETER.convertToPixels());

      Area newBody = null;

      if (folded) {
        switch (orientation) {
          case DEFAULT:
            newBody = Area.rect(x - bodyDiameter, y - bodyDiameter / 2, bodyDiameter, bodyDiameter);
            break;
          case _90:
            newBody = Area.rect(x - bodyDiameter / 2, y - bodyDiameter, bodyDiameter, bodyDiameter);
            break;
          case _180:
            newBody = Area.rect(x, y - bodyDiameter / 2, bodyDiameter, bodyDiameter);
            break;
          case _270:
            newBody = Area.rect(x - bodyDiameter / 2, y, bodyDiameter, bodyDiameter);
            break;
          default:
            throw new RuntimeException("Unexpected orientation: " + orientation);
        }
      } else {
        switch (orientation) {
          case DEFAULT:
            newBody =
                Area.circle(x, y, bodyDiameter)
                    .subtract(
                        Area.rect(
                            x - bodyDiameter,
                            y - bodyDiameter / 2,
                            3 * bodyDiameter / 4,
                            bodyDiameter));
            break;
          case _90:
            newBody =
                Area.circle(x, y, bodyDiameter)
                    .subtract(
                        Area.rect(
                            x - bodyDiameter / 2,
                            y - bodyDiameter,
                            bodyDiameter,
                            3 * bodyDiameter / 4));
            break;
          case _180:
            newBody =
                Area.circle(x, y, bodyDiameter)
                    .subtract(
                        Area.rect(
                            x + bodyDiameter / 4,
                            y - bodyDiameter / 2,
                            3 * bodyDiameter / 4,
                            bodyDiameter));
            break;
          case _270:
            newBody =
                Area.circle(x, y, bodyDiameter)
                    .subtract(
                        Area.rect(
                            x - bodyDiameter / 2,
                            y + bodyDiameter / 4,
                            bodyDiameter,
                            3 * bodyDiameter / 4));
            break;
          default:
            throw new RuntimeException("Unexpected orientation: " + orientation);
        }
      }

      body = new Area[] {newBody};
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
    int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));

    if (!outlineMode) {
      for (Point point : controlPoints) {
        Area.circle(point, pinSize).fillDraw(g2d, PIN_COLOR, PIN_BORDER_COLOR);
      }
    }

    Area mainArea = getBody()[0];

    Composite oldComposite = setTransparency(g2d);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(mainArea);
    g2d.setComposite(oldComposite);

    Color finalBorderColor = tryBorderColor(outlineMode, borderColor);
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(mainArea);

    // Draw label.
    g2d.setFont(project.getFont());
    Color finalLabelColor = tryLabelColor(outlineMode, getLabelColor());
    g2d.setColor(finalLabelColor);
    String label = getLabelForDisplay();
    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
    Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);
    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());
    // Center text horizontally and vertically
    Rectangle bounds = mainArea.getBounds();
    int x = bounds.x + (bounds.width - textWidth) / 2;
    int y = bounds.y + (bounds.height - textHeight) / 2 + fontMetrics.getAscent();
    g2d.drawString(label, x, y);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    int margin = 3 * width / 32;
    Area area =
        new Area(new Ellipse2D.Double(margin / 2, margin, width - 2 * margin, width - 2 * margin));
    // area.subtract(new Area(new Rectangle2D.Double(0, 0, 2 * margin,
    // height)));
    area.intersect(Area.rect(2 * margin, 0, width, height));
    area.fillDraw(g2d, BODY_COLOR, BORDER_COLOR);
    g2d.setColor(PIN_COLOR);
    int pinSize = 2 * width / 32;
    for (int i = 0; i < 3; i++) {
      g2d.fillOval(width / 2 - pinSize / 2, (height / 4) * (i + 1), pinSize, pinSize);
    }
  }
}
