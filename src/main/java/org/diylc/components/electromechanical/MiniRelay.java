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
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.Voltage;
import org.diylc.core.measures.VoltageUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "Mini Relay",
    author = "Branislav Stojkovic",
    category = "Electro-Mechanical",
    instanceNamePrefix = "RY",
    description = "Miniature PCB mount relay, like Omron G5V-1 or G5V-2",
    zOrder = IDIYComponent.COMPONENT)
public class MiniRelay extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  public static final Color BODY_COLOR = Color.gray;
  public static final Color BORDER_COLOR = Color.gray.darker();
  public static final Color PIN_COLOR = Color.decode("#00B2EE");
  public static final Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static final Color INDENT_COLOR = Color.gray.darker();
  public static final Color LABEL_COLOR = Color.white;
  public static final int EDGE_RADIUS = 6;
  public static final Size PIN_SIZE = Size.in(0.03);
  public static final Size INDENT_SIZE = Size.in(0.07);
  public static final Size BODY_MARGIN = Size.in(0.05);
  public static final Size MINI_PIN_SPACING = Size.in(0.2);
  public static final Size MINI_ROW_SPACING = Size.in(0.3);
  public static final Size MINI_WIDTH = Size.mm(20.1);
  public static final Size MINI_HEIGHT = Size.mm(9.9);
  public static final Size MINI_GAP = Size.in(0.1);
  public static final Size ULTRA_PIN_SPACING = Size.in(0.1);
  public static final Size ULTRA_ROW_SPACING = Size.in(0.2);
  public static final Size ULTRA_WIDTH = Size.mm(12.2);
  public static final Size ULTRA_HEIGHT = Size.mm(7.2);
  public static final Size ULTRA_GAP = Size.in(0.1);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;

  private Point[] controlPoints = new Point[] {new Point(0, 0)};
  private RelayType type = RelayType.DPDT;
  private RelaySize size = RelaySize.Miniature;
  private Voltage voltage = new Voltage(12d, VoltageUnit.V);
  private transient Area[] body;

  public MiniRelay() {
    super();
    updateControlPoints();
    display = Display.NAME;
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

  @EditableProperty
  public Display getDisplay() {
    if (display == null) {
      display = Display.VALUE;
    }
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
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
    int pinCount = 0;
    switch (type) {
      case DPDT:
        pinCount = 8;
        break;
      case SPDT:
        pinCount = 6;
        break;
      default:
        throw new RuntimeException("Unexpected type: " + type);
    }
    controlPoints = new Point[pinCount];
    controlPoints[0] = firstPoint;
    int pinSpacing =
        size == RelaySize.Miniature
            ? (int) MINI_PIN_SPACING.convertToPixels()
            : (int) ULTRA_PIN_SPACING.convertToPixels();
    int rowSpacing =
        size == RelaySize.Miniature
            ? (int) MINI_ROW_SPACING.convertToPixels()
            : (int) ULTRA_ROW_SPACING.convertToPixels();
    // Update control points.
    int dx1;
    int dy1;
    int dx2;
    int dy2;
    int delta = 0;
    for (int i = 0; i < pinCount / 2; i++) {
      if (i == 1) {
        delta =
            size == RelaySize.Miniature
                ? (int) MINI_GAP.convertToPixels()
                : (int) ULTRA_GAP.convertToPixels();
        if (type == RelayType.SPDT) {
          delta += pinSpacing;
        }
      }
      switch (orientation) {
        case DEFAULT:
          dx1 = 0;
          dy1 = i * pinSpacing + delta;
          dx2 = rowSpacing;
          dy2 = i * pinSpacing + delta;
          break;
        case _90:
          dx1 = -(i * pinSpacing + delta);
          dy1 = 0;
          dx2 = -(i * pinSpacing + delta);
          dy2 = rowSpacing;
          break;
        case _180:
          dx1 = 0;
          dy1 = -(i * pinSpacing + delta);
          dx2 = -rowSpacing;
          dy2 = -(i * pinSpacing + delta);
          break;
        case _270:
          dx1 = i * pinSpacing + delta;
          dy1 = 0;
          dx2 = i * pinSpacing + delta;
          dy2 = -rowSpacing;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      controlPoints[i] = new Point(firstPoint.x + dx1, firstPoint.y + dy1);
      controlPoints[i + pinCount / 2] = new Point(firstPoint.x + dx2, firstPoint.y + dy2);
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int centerX = (controlPoints[0].x + controlPoints[controlPoints.length - 1].x) / 2;
      int centerY = (controlPoints[0].y + controlPoints[controlPoints.length - 1].y) / 2;
      int bodyMargin = getClosestOdd(BODY_MARGIN.convertToPixels());
      int width = 0;
      int height = 0;
      int rowSpacing =
          size == RelaySize.Miniature
              ? (int) MINI_ROW_SPACING.convertToPixels()
              : (int) ULTRA_ROW_SPACING.convertToPixels();
      Area indentation = null;
      int indentationSize = getClosestOdd(INDENT_SIZE.convertToPixels());
      switch (orientation) {
        case DEFAULT:
          width =
              (int)
                  (size == RelaySize.Miniature
                      ? MINI_HEIGHT.convertToPixels()
                      : ULTRA_HEIGHT.convertToPixels());
          height =
              (int)
                  (size == RelaySize.Miniature
                      ? MINI_WIDTH.convertToPixels()
                      : ULTRA_WIDTH.convertToPixels());
          x -= bodyMargin;
          y -= bodyMargin;
          indentation =
              new Area(
                  new Rectangle2D.Double(
                      centerX - indentationSize / 2,
                      y - indentationSize / 2,
                      indentationSize,
                      indentationSize));
          break;
        case _90:
          width =
              (int)
                  (size == RelaySize.Miniature
                      ? MINI_WIDTH.convertToPixels()
                      : ULTRA_WIDTH.convertToPixels());
          height =
              (int)
                  (size == RelaySize.Miniature
                      ? MINI_HEIGHT.convertToPixels()
                      : ULTRA_HEIGHT.convertToPixels());
          x -= -bodyMargin + width;
          y -= bodyMargin;
          indentation =
              new Area(
                  new Rectangle2D.Double(
                      x + width - indentationSize / 2,
                      centerY - indentationSize / 2,
                      indentationSize,
                      indentationSize));
          break;
        case _180:
          width =
              (int)
                  (size == RelaySize.Miniature
                      ? MINI_HEIGHT.convertToPixels()
                      : ULTRA_HEIGHT.convertToPixels());
          height =
              (int)
                  (size == RelaySize.Miniature
                      ? MINI_WIDTH.convertToPixels()
                      : ULTRA_WIDTH.convertToPixels());
          x -= rowSpacing + bodyMargin;
          y -= -bodyMargin + height;
          indentation =
              new Area(
                  new Rectangle2D.Double(
                      centerX - indentationSize / 2,
                      y + height - indentationSize / 2,
                      indentationSize,
                      indentationSize));
          break;
        case _270:
          width =
              (int)
                  (size == RelaySize.Miniature
                      ? MINI_WIDTH.convertToPixels()
                      : ULTRA_WIDTH.convertToPixels());
          height =
              (int)
                  (size == RelaySize.Miniature
                      ? MINI_HEIGHT.convertToPixels()
                      : ULTRA_HEIGHT.convertToPixels());
          x -= bodyMargin;
          y -= bodyMargin + rowSpacing;
          indentation =
              new Area(
                  new Rectangle2D.Double(
                      x - indentationSize / 2,
                      centerY - indentationSize / 2,
                      indentationSize,
                      indentationSize));
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      body[0] =
          new Area(
              new RoundRectangle2D.Double(
                  centerX - width / 2,
                  centerY - height / 2,
                  width,
                  height,
                  EDGE_RADIUS,
                  EDGE_RADIUS));
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
    Area mainArea = getBody()[0];
    final Composite oldComposite = setTransparency(g2d);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
    g2d.fill(mainArea);
    g2d.setComposite(oldComposite);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
    for (Point point : controlPoints) {
      if (!outlineMode) {
        g2d.setColor(PIN_COLOR);
        g2d.fillOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
      }
      g2d.setColor(tryBorderColor(outlineMode, PIN_BORDER_COLOR));
      g2d.drawOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
    }

    final Color finalBorderColor = tryBorderColor(outlineMode, BORDER_COLOR);
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (outlineMode) {
      Area area = new Area(mainArea);
      area.subtract(getBody()[1]);
      g2d.draw(area);
    } else {
      g2d.draw(mainArea);
      if (getBody()[1] != null) {
        g2d.setColor(INDENT_COLOR);
        g2d.fill(getBody()[1]);
      }
    }
    // Draw label.
    g2d.setFont(project.getFont());
    Color finalLabelColor = tryLabelColor(outlineMode, LABEL_COLOR);
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
    int radius = 6 * width / 32;
    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect(width / 6, 1, 4 * width / 6, height - 4, radius, radius);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect(width / 6, 1, 4 * width / 6, height - 4, radius, radius);
    int pinSize = 2 * width / 32;
    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < 4; i++) {
      if (i == 1) {
        continue;
      }
      g2d.fillOval(width / 4, (height / 5) * (i + 1) - 1, pinSize, pinSize);
      g2d.fillOval(3 * width / 4 - pinSize, (height / 5) * (i + 1) - 1, pinSize, pinSize);
    }
  }

  @Override
  public String getValueForDisplay() {
    return getValue() + " " + getType() + " " + getVoltage();
  }

  @EditableProperty
  public RelayType getType() {
    return type;
  }

  public void setType(RelayType type) {
    this.type = type;
    updateControlPoints();
    // Invalidate body
    this.body = null;
  }

  @EditableProperty
  public Voltage getVoltage() {
    return voltage;
  }

  public void setVoltage(Voltage voltage) {
    this.voltage = voltage;
  }

  @EditableProperty
  public RelaySize getSize() {
    return size;
  }

  public void setSize(RelaySize size) {
    this.size = size;
    updateControlPoints();
    // Invalidate body
    this.body = null;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  public enum RelayType {
    SPDT,
    DPDT;
  }

  public enum RelaySize {
    Miniature,
    Ultra_miniature;

    public String toString() {
      return name().replace('_', '-');
    }
  }
}
