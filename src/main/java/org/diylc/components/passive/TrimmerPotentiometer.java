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

package org.diylc.components.passive;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "Trimmer Potentiometer",
    author = "Branislav Stojkovic",
    category = "Passive",
    creationMethod = CreationMethod.SINGLE_CLICK,
    instanceNamePrefix = "VR",
    description = "Various types of board mounted trimmer potentiometers",
    zOrder = IDIYComponent.COMPONENT)
public class TrimmerPotentiometer extends AbstractPotentiometer {

  private static final long serialVersionUID = 1L;

  public static final Color PIN_COLOR = Color.decode("#00B2EE");
  public static final Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static final Size PIN_SIZE = Size.in(0.03);

  protected static final Size FLAT_SHAFT_SIZE = Size.mm(4.5);
  protected static final Size ROUNDED_EDGE = Size.mm(1);
  protected static final Size SPACING = Size.in(0.1);
  protected static final Display DISPLAY = Display.NAME;

  private static final Color BODY_COLOR = Color.decode("#FFFFE0");
  private static final Color BORDER_COLOR = Color.decode("#8E8E38");
  private static final Color SHAFT_COLOR = Color.decode("#FFFFE0");
  private static final Color SHAFT_BORDER_COLOR = Color.decode("#8E8E38");

  protected Color bodyColor = BODY_COLOR;
  protected Color borderColor = BORDER_COLOR;
  // Array of 7 elements: 3 lug connectors, 1 pot body and 3 lugs
  protected transient Shape[] body = null;

  protected TrimmerType type = TrimmerType.FLAT_SMALL;

  public TrimmerPotentiometer() {
    controlPoints = new Point[] {new Point(0, 0), new Point(0, 0), new Point(0, 0)};
    updateControlPoints();
    display = DISPLAY;
  }

  protected void updateControlPoints() {
    int spacing = (int) SPACING.convertToPixels();
    int[] multipliers = getType().getControlPointMultipliers(getOrientation());
    int dx1 = multipliers[0] * spacing;
    int dy1 = multipliers[1] * spacing;
    int dx2 = multipliers[2] * spacing;
    int dy2 = multipliers[3] * spacing;

    controlPoints[1].setLocation(controlPoints[0].x + dx1, controlPoints[0].y + dy1);
    controlPoints[2].setLocation(controlPoints[0].x + dx2, controlPoints[0].y + dy2);
  }

  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[2];

      // Calculate the center point as center of the minimum bounding
      // rectangle.
      final int centerX =
          (Math.max(Math.max(controlPoints[0].x, controlPoints[1].x), controlPoints[2].x)
                  + Math.min(Math.min(controlPoints[0].x, controlPoints[1].x), controlPoints[2].x))
              / 2;
      final int centerY =
          (Math.max(Math.max(controlPoints[0].y, controlPoints[1].y), controlPoints[2].y)
                  + Math.min(Math.min(controlPoints[0].y, controlPoints[1].y), controlPoints[2].y))
              / 2;

      // Calculate body dimensions based on the selected type.
      int length = getClosestOdd(getType().getLength().convertToPixels());
      int width = getClosestOdd(getType().getWidth().convertToPixels());
      switch (getType()) {
        case FLAT_LARGE:
        case FLAT_SMALL:
        case FLAT_XSMALL:
        case FLAT_XLARGE:
        case FLAT_SMALL2:
          int shaftSize = getClosestOdd(FLAT_SHAFT_SIZE.convertToPixels());
          Area shaft = Area.circle(centerX, centerY, shaftSize);
          Area slot = new Area(new Rectangle2D.Double(
              centerX - shaftSize / 2, centerY - shaftSize / 8, shaftSize, shaftSize / 4));
          slot.transform(AffineTransform.getRotateInstance(Math.PI / 4, centerX, centerY));
          shaft.subtract(slot);
          body[1] = shaft;
          break;
        default:
          break;
      }
      if (orientation == Orientation.DEFAULT || orientation == Orientation._180) {
        int p = length;
        length = width;
        width = p;
      }
      switch (getType()) {
        case FLAT_SMALL2:
          body[0] = Area.oval(centerX, centerY, length, width);
          break;
        default:
          double edge = ROUNDED_EDGE.convertToPixels();
          body[0] = Area.centeredRoundRect(centerX, centerY, length, width, edge);
      }
    }
    return body;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    super.setControlPoint(point, index);
    body = null;
  }

  @Override
  public void setOrientation(Orientation orientation) {
    super.setOrientation(orientation);
    updateControlPoints();
    body = null;
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
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    Shape mainShape = getBody()[0];
    Shape shaftShape = getBody()[1];
    if (mainShape != null) {
      g2d.setColor(bodyColor);
      if (!outlineMode) {
        Composite oldComposite = setTransparency(g2d);
        g2d.fill(mainShape);
        if (shaftShape != null) {
          g2d.setColor(SHAFT_COLOR);
          g2d.fill(shaftShape);
          g2d.setColor(SHAFT_BORDER_COLOR);
          g2d.draw(shaftShape);
        }
        g2d.setComposite(oldComposite);
      }
      Color finalBorderColor = tryBorderColor(outlineMode, borderColor);
      g2d.setColor(finalBorderColor);
      g2d.draw(mainShape);
    }

    // Draw pins.
    int pinSize = getClosestOdd(PIN_SIZE.convertToPixels());
    for (Point point : controlPoints) {
      if (!outlineMode) {
        Area.circle(point, pinSize).fill(g2d, PIN_COLOR);
      }
      Area.circle(point, pinSize).draw(g2d, tryBorderColor(outlineMode, PIN_BORDER_COLOR));
    }

    // Draw label.
    g2d.setFont(project.getFont());

    Color finalLabelColor = tryLabelColor(outlineMode, LABEL_COLOR);
    g2d.setColor(finalLabelColor);
    String label = getLabelForDisplay();
    final FontMetrics fontMetrics = g2d.getFontMetrics();
    final Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);
    final int textHeight = (int) rect.getHeight();
    final int textWidth = (int) rect.getWidth();
    final Rectangle2D bodyRect = getBody()[0].getBounds2D();
    final int panelHeight = (int) bodyRect.getHeight();
    final int panelWidth = (int) bodyRect.getWidth();
    int x = (panelWidth - textWidth) / 2;
    int y = (panelHeight - textHeight) / 2 + fontMetrics.getAscent();
    g2d.drawString(label, (int) (bodyRect.getX() + x), (int) (bodyRect.getY() + y));
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = 4;
    g2d.setColor(BODY_COLOR);
    g2d.fillRect(margin, margin, width - 2 * margin, width - 2 * margin);
    g2d.setColor(BORDER_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.drawRect(margin, margin, width - 2 * margin, width - 2 * margin);
    int shaftSize = 11;
    int slotSize = 2;
    Area area = new Area(new Ellipse2D.Double(
        width / 2 - shaftSize / 2, width / 2 - shaftSize / 2, shaftSize, shaftSize));
    Area slot = new Area(new Rectangle2D.Double(0, width / 2 - slotSize / 2, width, slotSize));
    slot.transform(AffineTransform.getRotateInstance(Math.PI / 4, width / 2, width / 2));
    area.subtract(slot);
    g2d.setColor(SHAFT_COLOR);
    g2d.fill(area);
    g2d.setColor(SHAFT_BORDER_COLOR);
    g2d.draw(area);

    int pinSize = 3;
    g2d.setColor(PIN_COLOR);
    g2d.fillOval(margin - pinSize / 2, 10 - pinSize / 2, pinSize, pinSize);
    g2d.setColor(PIN_BORDER_COLOR);
    g2d.drawOval(margin - pinSize / 2, 10 - pinSize / 2, pinSize, pinSize);

    g2d.setColor(PIN_COLOR);
    g2d.fillOval(margin - pinSize / 2, 21 - pinSize / 2, pinSize, pinSize);
    g2d.setColor(PIN_BORDER_COLOR);
    g2d.drawOval(margin - pinSize / 2, 21 - pinSize / 2, pinSize, pinSize);

    g2d.setColor(PIN_COLOR);
    g2d.fillOval(width - margin - pinSize / 2, width / 2 - pinSize / 2, pinSize, pinSize);
    g2d.setColor(PIN_BORDER_COLOR);
    g2d.drawOval(width - margin - pinSize / 2, width / 2 - pinSize / 2, pinSize, pinSize);
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty
  public Display getDisplay() {
    if (display == null) {
      display = DISPLAY;
    }
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty
  public TrimmerType getType() {
    return type;
  }

  public void setType(TrimmerType type) {
    this.type = type;
    updateControlPoints();
    body = null;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  public enum TrimmerType {
    FLAT_SMALL("Horizontal Small 1",
               Size.mm(9.5),
               2, 1, 0, 2),
    FLAT_SMALL2("Horizontal Small 2",
                Size.mm(9.5),
                4, 1, 0, 2),
    FLAT_XSMALL("Horizontal X-Small",
                Size.mm(5),
                2, 1, 0, 2),
    FLAT_LARGE("Horizontal Medium",
               Size.mm(9.5),
               4, 1, 0, 2),
    FLAT_XLARGE("Horizontal Large",
                Size.mm(13),
                5, 2, 0, 4),
    VERTICAL_INLINE("Vertical Inline",
                    Size.mm(9.5),
                    Size.mm(4.5),
                    0, 1, 0, 2),
    VERTICAL_OFFSET("Vertical Offset 1",
                    Size.mm(9.5),
                    Size.mm(4.5),
                    1, 1, 0, 2),
    VERTICAL_OFFSET_BIG_GAP("Vertical Offset 2",
                            Size.mm(9.5),
                            Size.mm(4.5),
                            2, 1, 0, 2);

    private Size bodyLength;
    private Size bodyWidth;
    private Map<Orientation, int[]> multipliers = new HashMap<>();
    private String label;

    TrimmerType(
        String label, Size bodyLength, Size bodyWidth, int mx1, int my1, int mx2, int my2) {
      this.label = label;
      this.bodyLength = bodyLength;
      this.bodyWidth = bodyWidth;
      this.multipliers.put(Orientation.DEFAULT, new int[]{ mx1, my1, mx2, my2 });
      this.multipliers.put(Orientation._90, new int[]{ -my1, mx1, -my2, mx2 });
      this.multipliers.put(Orientation._180, new int[]{ -mx1, -my1, -mx2, -my2 });
      this.multipliers.put(Orientation._270, new int[]{ my1, -mx1, my2, -mx2 });
    }

    TrimmerType(String label, Size bodySize, int mx1, int my1, int mx2, int my2) {
      this(label, bodySize, bodySize, mx1, my1, mx2, my2);
    }

    @Override
    public String toString() {
      return label;
    }

    public Size getLength() {
      return bodyLength;
    }

    public Size getWidth() {
      return bodyWidth;
    }

    public int[] getControlPointMultipliers(Orientation o) {
      return this.multipliers.get(o);
    }
  }
}
