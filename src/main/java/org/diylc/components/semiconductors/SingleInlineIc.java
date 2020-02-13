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
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.diylc.common.Display;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractComponent;
import org.diylc.components.Area;
import org.diylc.components.PinCount;
import org.diylc.components.transform.InlinePackageTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.annotations.StringValue;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@StringValue
@ComponentDescriptor(
    name = "SIP IC",
    author = "Branislav Stojkovic",
    category = "Semiconductors",
    instanceNamePrefix = "IC",
    description = "Single-in-line package IC",
    zOrder = AbstractComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_VALUE,
    transformer = InlinePackageTransformer.class)
public class SingleInlineIc extends InlinePackage {

  private static final long serialVersionUID = 1L;

  public static final Size PIN_SIZE = Size.mm(0.8);
  public static final Size THICKNESS = Size.in(0.13);

  public SingleInlineIc() {
    super(defaultPinCount().setPins(8), Display.NAME);
    alpha = 100;
  }

  public static PinCount defaultPinCount() {
    return new PinCount(2, 20);
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

  @EditableProperty(name = "Pins")
  public PinCount getPinCount() {
    return pinCount;
  }

  public void setPinCount(PinCount pinCount) {
    this.pinCount.setPins(pinCount);
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty(name = "Pin Spacing", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getPinSpacing() {
    return pinSpacing;
  }

  public void setPinSpacing(Size pinSpacing) {
    this.pinSpacing = pinSpacing;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  protected void updateControlPoints() {
    Point firstPoint = controlPoints[0];
    controlPoints = new Point[pinCount.pins()];
    controlPoints[0] = firstPoint;
    double pinSpacing = this.pinSpacing.convertToPixels();
    // Update control points.
    double dx1;
    double dy1;
    for (int i = 0; i < pinCount.pins(); i++) {
      switch (orientation) {
        case DEFAULT:
          dx1 = 0;
          dy1 = i * pinSpacing;
          break;
        case _90:
          dx1 = -i * pinSpacing;
          dy1 = 0;
          break;
        case _180:
          dx1 = 0;
          dy1 = -i * pinSpacing;
          break;
        case _270:
          dx1 = i * pinSpacing;
          dy1 = 0;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      controlPoints[i] = new Point((int) (firstPoint.x + dx1), (int) (firstPoint.y + dy1));
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int thickness = getClosestOdd(THICKNESS.convertToPixels());
      double width;
      double height;
      double pinSpacing = (int) this.pinSpacing.convertToPixels();
      Area indentation = null;
      int indentationSize = getClosestOdd(INDENT_SIZE.convertToPixels());
      switch (orientation) {
        case DEFAULT:
          width = thickness;
          height = pinCount.pins() * pinSpacing;
          x -= thickness / 2;
          y -= pinSpacing / 2;
          indentation = Area.circle(x + width / 2, y, indentationSize);
          break;
        case _90:
          width = pinCount.pins() * pinSpacing;
          height = thickness;
          x -= (pinSpacing / 2) + width - pinSpacing;
          y -= thickness / 2;
          indentation = Area.circle(x + width, y + height / 2, indentationSize);
          break;
        case _180:
          width = thickness;
          height = pinCount.pins() * pinSpacing;
          x -= thickness / 2;
          y -= (pinSpacing / 2) + height - pinSpacing;
          indentation = Area.circle(x + width / 2, y + height, indentationSize);
          break;
        case _270:
          width = pinCount.pins() * pinSpacing;
          height = thickness;
          x -= pinSpacing / 2;
          y -= thickness / 2;
          indentation = Area.circle(x, y + height / 2, indentationSize);
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      body[0] =
          new Area(new RoundRectangle2D.Double(x, y, width, height, EDGE_RADIUS, EDGE_RADIUS));
      body[1] = indentation;
      if (indentation != null) {
        indentation.intersect(body[0]);
      }
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

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    if (!outlineMode) {
      int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
      for (Point point : controlPoints) {
        Area.circle(point, pinSize).fillDraw(g2d, PIN_COLOR, PIN_BORDER_COLOR);
      }
    }

    Composite oldComposite = setTransparency(g2d);
    Area mainArea = getBody()[0];
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
    g2d.fill(mainArea);
    g2d.setComposite(oldComposite);

    Color finalBorderColor = tryBorderColor(outlineMode, getBorderColor());
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (outlineMode) {
      Area area = new Area(mainArea);
      area.subtract(getBody()[1]);
      g2d.draw(area);
    } else {
      g2d.draw(mainArea);
      if (getBody()[1] != null) {
        g2d.setColor(getIndentColor());
        g2d.fill(getBody()[1]);
      }
    }
    // Draw label.
    g2d.setFont(project.getFont());
    Color finalLabelColor = tryLabelColor(outlineMode, getLabelColor());
    g2d.setColor(finalLabelColor);
    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
    String label = getLabelForDisplay();
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
    int radius = 6 * width / 32;
    int thickness = getClosestOdd(width / 3);
    g2d.rotate(Math.PI / 4, width / 2, height / 2);
    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect((width - thickness) / 2, 0, thickness, height, radius, radius);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect((width - thickness) / 2, 0, thickness, height, radius, radius);
    int pinSize = 2 * width / 32;
    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < 4; i++) {
      g2d.fillOval(width / 2 - pinSize / 2, (height / 5) * (i + 1), pinSize, pinSize);
    }
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    if (bodyColor == null) {
      bodyColor = BODY_COLOR;
    }
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
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

  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    if (labelColor == null) {
      labelColor = LABEL_COLOR;
    }
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @EditableProperty(name = "Indent")
  public Color getIndentColor() {
    if (indentColor == null) {
      indentColor = INDENT_COLOR;
    }
    return indentColor;
  }

  public void setIndentColor(Color indentColor) {
    this.indentColor = indentColor;
  }
}
