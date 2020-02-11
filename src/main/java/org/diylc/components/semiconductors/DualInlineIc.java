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
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.diylc.common.Display;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.Area;
import org.diylc.components.PinCount;
import org.diylc.components.transform.InlinePackageTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "DIP IC",
    author = "Branislav Stojkovic",
    category = "Semiconductors",
    instanceNamePrefix = "IC",
    description = "Dual-in-line package IC",
    zOrder = IDIYComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_VALUE,
    transformer = InlinePackageTransformer.class)
public class DualInlineIc extends InlinePackage {

  private static final long serialVersionUID = 1L;
  private static final Size DEFAULT_ROW_SPACING = Size.in(0.3);

  public static final Size PIN_SIZE = Size.in(0.04);
  public static final DisplayNumbers DISPLAY_NUMBERS = DisplayNumbers.NO;

  private Size rowSpacing;
  private DisplayNumbers displayNumbers = DISPLAY_NUMBERS;

  public DualInlineIc() {
    super(defaultPinCount().setPins(8), Display.BOTH);
  }

  public static PinCount defaultPinCount() {
    return new PinCount(4, 50, true);
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

  @EditableProperty(name = "Row Spacing", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getRowSpacing() {
    if (rowSpacing == null) {
      rowSpacing = DEFAULT_ROW_SPACING;
    }
    return rowSpacing;
  }

  public void setRowSpacing(Size rowSpacing) {
    this.rowSpacing = rowSpacing;
    updateControlPoints();
    // Reset body shape;
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

  protected void updateControlPoints() {
    Point firstPoint = controlPoints[0];
    controlPoints = new Point[pinCount.pins()];
    controlPoints[0] = firstPoint;
    double pinSpacing = this.pinSpacing.convertToPixels();
    double rowSpacing = getRowSpacing().convertToPixels();
    // Update control points.
    double dx1;
    double dy1;
    double dx2;
    double dy2;
    for (int i = 0; i < pinCount.pins() / 2; i++) {
      switch (orientation) {
        case DEFAULT:
          dx1 = 0;
          dy1 = i * pinSpacing;
          dx2 = rowSpacing;
          dy2 = i * pinSpacing;
          break;
        case _90:
          dx1 = -i * pinSpacing;
          dy1 = 0;
          dx2 = -i * pinSpacing;
          dy2 = rowSpacing;
          break;
        case _180:
          dx1 = 0;
          dy1 = -i * pinSpacing;
          dx2 = -rowSpacing;
          dy2 = -i * pinSpacing;
          break;
        case _270:
          dx1 = i * pinSpacing;
          dy1 = 0;
          dx2 = i * pinSpacing;
          dy2 = -rowSpacing;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      controlPoints[i] = new Point((int) (firstPoint.x + dx1), (int) (firstPoint.y + dy1));
      controlPoints[i + pinCount.pins() / 2] =
          new Point((int) (firstPoint.x + dx2), (int) (firstPoint.y + dy2));
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      double x = controlPoints[0].x;
      double y = controlPoints[0].y;
      double pinSize = PIN_SIZE.convertToPixels();
      double pinSpacing = this.pinSpacing.convertToPixels();
      double rowSpacing = this.rowSpacing.convertToPixels();
      Area indentation = null;
      int indentationSize = getClosestOdd(INDENT_SIZE.convertToPixels());
      int pins = pinCount.pins();
      boolean vertical = orientation.isDefault() || orientation.is180();
      double width = vertical ? rowSpacing - pinSize : (pins / 2) * pinSpacing;
      double height = vertical ? (pins / 2) * pinSpacing : rowSpacing - pinSize;
      switch (orientation) {
        case DEFAULT:
          x += pinSize / 2;
          y -= pinSpacing / 2;
          indentation = Area.circle(x + width / 2, y, indentationSize);
          break;
        case _90:
          x -= (pinSpacing / 2) + width - pinSpacing;
          y += pinSize / 2;
          indentation = Area.circle(x + width, y + height / 2, indentationSize);
          break;
        case _180:
          x -= rowSpacing - pinSize / 2;
          y -= (pinSpacing / 2) + height - pinSpacing;
          indentation = Area.circle(x + width / 2, y + height, indentationSize);
          break;
        case _270:
          x -= pinSpacing / 2;
          y += pinSize / 2 - rowSpacing;
          indentation = Area.circle(x, y + height, indentationSize);
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      body[0] = Area.roundRect(x, y, width, height, EDGE_RADIUS);
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
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));

    if (!outlineMode) {
      int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
      for (Point point : controlPoints) {
        Area.centeredSquare(point, pinSize).fillDraw(g2d, PIN_COLOR, PIN_BORDER_COLOR);
      }
    }

    Composite oldComposite = setTransparency(g2d);
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

    drawingObserver.stopTracking();

    // Draw label.
    g2d.setFont(project.getFont());
    Color finalLabelColor = tryLabelColor(outlineMode, getLabelColor());
    g2d.setColor(finalLabelColor);
    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
    List<String> label = getLabelListForDisplay();
    if (!label.isEmpty()) {
      int i = 0;
      for (String l : label) {
        Rectangle2D rect = fontMetrics.getStringBounds(l, g2d);
        int textHeight = (int) (rect.getHeight());
        int textWidth = (int) (rect.getWidth());
        // Center text horizontally and vertically
        Rectangle bounds = mainArea.getBounds();
        int x = bounds.x + (bounds.width - textWidth) / 2;
        int y = bounds.y + (bounds.height - textHeight) / 2 + fontMetrics.getAscent();

        AffineTransform oldTransform = g2d.getTransform();

        if (getOrientation() == Orientation.DEFAULT || getOrientation() == Orientation._180) {
          int centerX = bounds.x + bounds.width / 2;
          int centerY = bounds.y + bounds.height / 2;
          g2d.rotate(-HALF_PI, centerX, centerY);
        }

        if (label.size() == 2) {
          if (i == 0) {
            g2d.translate(0, -textHeight / 2);
          } else if (i == 1) {
            g2d.translate(0, textHeight / 2);
          }
        }
        g2d.drawString(l, x, y);
        g2d.setTransform(oldTransform);
        i++;
      }
    }

    // draw pin numbers
    if (displayNumbers != DisplayNumbers.NO) {
      int pinNo = 0;
      int j = 0;
      int k = 1;
      int pinSize = (int) PIN_SIZE.convertToPixels();
      g2d.setFont(project.getFont().deriveFont((float) (project.getFont().getSize2D() * 0.66)));

      for (Point point : controlPoints) {
        pinNo++;

        // determine points relative to rotation
        int textX1 = point.x - 2 * pinSize;
        int textY1 = point.y + pinSize / 2;
        int textX2 = point.x + pinSize;
        int textY2 = point.y + pinSize / 2;
        switch (orientation) {
          case _90:
            textX2 = textX2 - pinSize - pinSize / 2;
            textY2 = textY2 + pinSize;
            textX1 = textX1 + 2 * pinSize - pinSize / 2;
            textY1 = textY1 - pinSize;
            break;
          case _180:
            textX1 = textX1 + 3 * pinSize;
            textX2 = textX2 - 3 * pinSize;
            break;
          case _270:
            textX1 = textX1 + pinSize + pinSize / 2;
            textY1 = textY1 + pinSize;
            textX2 = textX2 - pinSize - pinSize / 2;
            textY2 = textY2 - pinSize;
            break;
          default:
        }

        boolean secondPinSet = pinNo > pinCount.pins() / 2;
        int pinDisplayNumber = 0;
        int pinX = secondPinSet ? textX1 : textX2;
        int pinY = secondPinSet ? textY1 : textY2;
        switch (displayNumbers) {
          case DIP:
            pinDisplayNumber = secondPinSet ? pinCount.pins() - j : pinNo;
            if (secondPinSet) {
              j++;
            }
            break;
          case CONNECTOR:
            pinDisplayNumber = secondPinSet ? pinNo - (pinCount.pins() / 2) + k : pinNo + j;
            if (secondPinSet) {
              k++;
            } else {
              j++;
            }
            break;
          default:
            throw new RuntimeException("unhandled case for displayNumbers " + displayNumbers);
        }
        g2d.drawString(Integer.toString(pinDisplayNumber), pinX, pinY);
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    final int radius = 3 * width / 16;
    final int x = width / 6;
    Area.roundRect(x, 1, 2 * width / 3, height - 4, radius).fillDraw(g2d, BODY_COLOR, BORDER_COLOR);
    g2d.setColor(PIN_COLOR);
    int pinSize = width / 16;
    int x1 = x - pinSize;
    int x2 = 5 * width / 6 + 1;
    for (int i = 0; i < 4; i++) {
      int y = (height / 5) * (i + 1) - 1;
      g2d.fillRect(x1, y, pinSize, pinSize);
      g2d.fillRect(x2, y, pinSize, pinSize);
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

  @EditableProperty(name = "Display Pin #s")
  public DisplayNumbers getDisplayNumbers() {
    return displayNumbers;
  }

  public void setDisplayNumbers(DisplayNumbers numbers) {
    this.displayNumbers = numbers;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  public enum DisplayNumbers {
    NO("No"),
    DIP("DIP"),
    CONNECTOR("Connector");

    private String label;

    DisplayNumbers(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
