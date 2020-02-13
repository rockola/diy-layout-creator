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

package org.diylc.components.tube;

import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import org.diylc.common.Display;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.Area;
import org.diylc.components.PinCount;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

// @ComponentDescriptor(name = "Sub-Mini Tube", author = "Branislav Stojkovic", category = "Tubes",
// instanceNamePrefix = "V", description = "Sub-miniature (pencil) vacuum tube", stretchable =
// false, zOrder = AbstractComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE,
// transformer =
// TO220Transformer.class)
public class SubminiTube extends AbstractTransparentComponent {

  private static final long serialVersionUID = 1L;

  public static final Color BODY_COLOR = Color.lightGray;
  public static final Color BORDER_COLOR = Color.gray;
  public static final Color PIN_COLOR = Color.decode("#00B2EE");
  public static final Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static final Color LABEL_COLOR = Color.white;
  public static final Size PIN_SIZE = Size.in(0.03);
  public static final Size PIN_SPACING = Size.in(0.1);
  public static final Size BODY_WIDTH = Size.in(0.4);
  public static final Size BODY_THICKNESS = Size.mm(4.5);
  public static final Size BODY_HEIGHT = Size.mm(9);
  public static final Size DIAMETER = Size.in(0.4);
  public static final Size LENGTH = Size.in(1.375);
  public static final Size LEAD_LENGTH = Size.in(0.2);
  public static final Size LEAD_THICKNESS = Size.mm(0.8);

  private final PinCount pinCount = new PinCount(3, 10).setPins(8);
  private Orientation orientation = Orientation.DEFAULT;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private boolean folded = false;
  private Size leadLength = LEAD_LENGTH;
  private PinArrangement leadArrangement = PinArrangement.Circular;
  private boolean topLead = false;
  private Size diameter = DIAMETER;
  private Size length = LENGTH;
  private Size leadSpacing = PIN_SPACING;
  private transient Area[] body;

  public SubminiTube() {
    super();
    controlPoints = getFreshControlPoints(3);
    updateControlPoints();
    setDisplay(Display.NAME);
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  private void updateControlPoints() {
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    // Update control points.
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;
    int newPointCount = getPinCount().pins();
    // Need a new array
    if (newPointCount != controlPoints.length) {
      controlPoints = new Point[newPointCount];
      for (int i = 0; i < controlPoints.length; i++) {
        controlPoints[i] = new Point(x, y);
      }
    }
    int dx;
    int dy;
    if (folded) {
      switch (orientation) {
        case DEFAULT:
          dx = 0;
          dy = pinSpacing;
          break;
        case _90:
          dx = -pinSpacing;
          dy = 0;
          break;
        case _180:
          dx = 0;
          dy = -pinSpacing;
          break;
        case _270:
          dx = pinSpacing;
          dy = 0;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      for (int i = 1; i < controlPoints.length; i++) {
        controlPoints[i].setLocation(controlPoints[0].x + i * dx, controlPoints[0].y + i * dy);
      }
    } else {
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
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int pinSpacing = (int) PIN_SPACING.convertToPixels();
      int bodyWidth = getClosestOdd(BODY_WIDTH.convertToPixels());
      int bodyThickness = getClosestOdd(BODY_THICKNESS.convertToPixels());
      int bodyHeight = getClosestOdd(BODY_HEIGHT.convertToPixels());
      int tabThickness = 0; // (int) TAB_THICKNESS.convertToPixels();
      int tabHeight = 0; // (int) TAB_HEIGHT.convertToPixels();
      int tabHoleDiameter = 0; // (int)
      // TAB_HOLE_DIAMETER.convertToPixels();
      double leadLength = getLeadLength().convertToPixels();

      switch (orientation) {
        case DEFAULT:
          if (folded) {
            body[0] =
                Area.rect(x + leadLength, y + pinSpacing - bodyWidth / 2, bodyHeight, bodyWidth);
            body[1] =
                Area.rect(
                        x + leadLength + bodyHeight,
                        y + pinSpacing - bodyWidth / 2,
                        tabHeight,
                        bodyWidth)
                    .subtract(
                        Area.circle(
                            x + leadLength + bodyHeight + tabHeight / 2,
                            y + pinSpacing,
                            tabHoleDiameter));
          } else {
            body[0] = Area.centeredRect(x, y, bodyThickness, bodyWidth);
            body[1] =
                Area.rect(
                    x + bodyThickness / 2 - tabThickness,
                    y + pinSpacing - bodyWidth / 2,
                    tabThickness,
                    bodyWidth);
          }
          break;
        case _90:
          if (folded) {
            body[0] =
                Area.rect(x - pinSpacing - bodyWidth / 2, y + leadLength, bodyWidth, bodyHeight);
            body[1] =
                Area.rect(
                        x - pinSpacing - bodyWidth / 2,
                        y + leadLength + bodyHeight,
                        bodyWidth,
                        tabHeight)
                    .subtract(
                        Area.circle(
                            x - pinSpacing,
                            y + leadLength + bodyHeight + tabHeight / 2,
                            tabHoleDiameter));
          } else {
            body[0] =
                Area.rect(
                    x - pinSpacing - bodyWidth / 2,
                    y - bodyThickness / 2,
                    bodyWidth,
                    bodyThickness);
            body[1] =
                Area.rect(
                    x - pinSpacing - bodyWidth / 2,
                    y + bodyThickness / 2 - tabThickness,
                    bodyWidth,
                    tabThickness);
          }
          break;
        case _180:
          if (folded) {
            body[0] =
                Area.rect(
                    x - leadLength - bodyHeight,
                    y - pinSpacing - bodyWidth / 2,
                    bodyHeight,
                    bodyWidth);
            body[1] =
                Area.rect(
                        x - leadLength - bodyHeight - tabHeight,
                        y - pinSpacing - bodyWidth / 2,
                        tabHeight,
                        bodyWidth)
                    .subtract(
                        Area.circle(
                            x - leadLength - bodyHeight - tabHeight / 2,
                            y - pinSpacing,
                            tabHoleDiameter));
          } else {
            body[0] = Area.centeredRect(x, y - pinSpacing, bodyThickness, bodyWidth);
            body[1] =
                Area.rect(
                    x - bodyThickness / 2, y - pinSpacing - bodyWidth / 2, tabThickness, bodyWidth);
          }
          break;
        case _270:
          if (folded) {
            body[0] =
                Area.rect(
                    x + pinSpacing - bodyWidth / 2,
                    y - leadLength - bodyHeight,
                    bodyWidth,
                    bodyHeight);
            body[1] =
                Area.rect(
                        x + pinSpacing - bodyWidth / 2,
                        y - leadLength - bodyHeight - tabHeight,
                        bodyWidth,
                        tabHeight)
                    .subtract(
                        Area.circle(
                            x + pinSpacing,
                            y - leadLength - bodyHeight - tabHeight / 2,
                            tabHoleDiameter));
          } else {
            body[0] = Area.centeredRect(x + pinSpacing, y, bodyWidth, bodyThickness);
            body[1] = Area.centeredRect(x + pinSpacing, y, bodyWidth, tabThickness);
          }
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
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
    int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
    Area mainArea = getBody()[0];

    final Composite oldComposite = setTransparency(g2d);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(mainArea);

    // Draw pins.
    if (folded) {
      int leadThickness = getClosestOdd(LEAD_THICKNESS.convertToPixels());
      int leadLength = (int) getLeadLength().convertToPixels();
      Color finalPinColor = outlineMode ? new Color(0, 0, 0, 0) : METAL_COLOR;
      Color finalPinBorderColor = tryBorderColor(outlineMode, METAL_COLOR.darker());
      for (Point point : controlPoints) {
        switch (orientation) {
          case _90:
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
            g2d.setColor(finalPinBorderColor);
            g2d.drawLine(point.x, point.y, point.x, point.y + leadLength - leadThickness / 2);
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness - 2));
            g2d.setColor(finalPinColor);
            g2d.drawLine(point.x, point.y, point.x, point.y + leadLength - leadThickness / 2);
            break;
          case _180:
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
            g2d.setColor(finalPinBorderColor);
            g2d.drawLine(point.x, point.y, point.x - leadLength - leadThickness / 2, point.y);
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness - 2));
            g2d.setColor(finalPinColor);
            g2d.drawLine(point.x, point.y, point.x - leadLength - leadThickness / 2, point.y);
            break;
          case _270:
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
            g2d.setColor(finalPinBorderColor);
            g2d.drawLine(point.x, point.y, point.x, point.y - leadLength);
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness - 2));
            g2d.setColor(finalPinColor);
            g2d.drawLine(point.x, point.y, point.x, point.y - leadLength);
            break;
          case DEFAULT:
          default:
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
            g2d.setColor(finalPinBorderColor);
            g2d.drawLine(point.x, point.y, point.x + leadLength - leadThickness / 2, point.y);
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness - 2));
            g2d.setColor(finalPinColor);
            g2d.drawLine(point.x, point.y, point.x + leadLength - leadThickness / 2, point.y);
            break;
        }
      }
    } else {
      if (!outlineMode) {
        for (Point point : controlPoints) {

          g2d.setColor(PIN_COLOR);
          g2d.fillOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
          g2d.setColor(PIN_BORDER_COLOR);
          g2d.drawOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
        }
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

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = 2 * width / 32;
    int bodySize = width * 5 / 10;
    int tabSize = bodySize * 6 / 10;
    int holeSize = 5 * width / 32;
    Area a = Area.rect((width - bodySize) / 2, margin, bodySize, tabSize);
    a.subtract(Area.circle(width / 2, margin + tabSize / 2, holeSize)).draw(g2d, BORDER_COLOR);
    Area.rect((width - bodySize) / 2, margin + tabSize, bodySize, bodySize)
        .fillDraw(g2d, BODY_COLOR, BORDER_COLOR);
    g2d.setColor(METAL_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    g2d.drawLine(width / 2, margin + tabSize + bodySize, width / 2, height - margin);
    g2d.drawLine(
        width / 2 - bodySize / 3,
        margin + tabSize + bodySize,
        width / 2 - bodySize / 3,
        height - margin);
    g2d.drawLine(
        width / 2 + bodySize / 3,
        margin + tabSize + bodySize,
        width / 2 + bodySize / 3,
        height - margin);
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
  public boolean getFolded() {
    return folded;
  }

  public void setFolded(boolean folded) {
    this.folded = folded;
    // Invalidate the body
    this.body = null;
  }

  @EditableProperty(name = "Lead Length")
  public Size getLeadLength() {
    if (leadLength == null) {
      leadLength = LEAD_LENGTH;
    }
    return leadLength;
  }

  public void setLeadLength(Size leadLength) {
    this.leadLength = leadLength;
    // Invalidate the body
    this.body = null;
  }

  @EditableProperty(name = "Pin Arrangement")
  public PinArrangement getPinArrangement() {
    return leadArrangement;
  }

  public void setPinArrangement(PinArrangement pinArrangement) {
    this.leadArrangement = pinArrangement;
  }

  @EditableProperty(name = "Top Lead")
  public boolean getTopLead() {
    return topLead;
  }

  public void setTopLead(boolean topLead) {
    this.topLead = topLead;
    updateControlPoints();
  }

  @EditableProperty
  public Size getDiameter() {
    return diameter;
  }

  public void setDiameter(Size diameter) {
    this.diameter = diameter;
    updateControlPoints();
  }

  @EditableProperty
  public Size getLength() {
    return length;
  }

  public void setLength(Size length) {
    this.length = length;
  }

  @EditableProperty(name = "Lead Count")
  public PinCount getPinCount() {
    return pinCount;
  }

  public void setPinCount(PinCount pinCount) {
    this.pinCount.setPins(pinCount);
    updateControlPoints();
  }

  @EditableProperty(name = "Lead Spacing")
  public Size getLeadSpacing() {
    return leadSpacing;
  }

  public void setLeadSpacing(Size leadSpacing) {
    this.leadSpacing = leadSpacing;
    updateControlPoints();
  }

  public enum PinArrangement {
    Inline("In-line"),
    Circular("Circular");

    private String label;

    PinArrangement(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
