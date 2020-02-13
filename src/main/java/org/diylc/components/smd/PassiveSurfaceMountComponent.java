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

package org.diylc.components.smd;

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
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.SiUnit;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

public abstract class PassiveSurfaceMountComponent extends AbstractTransparentComponent {

  private static final long serialVersionUID = 1L;

  public static final Color PIN_COLOR = METAL_COLOR;
  public static final Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static final Color LABEL_COLOR = Color.white;
  public static final int EDGE_RADIUS = 4;
  public static final Size PIN_SIZE = Size.mm(0.8);

  protected Color bodyColor;
  protected Color borderColor;

  private Orientation orientation = Orientation.DEFAULT;
  private PassiveSurfaceMountPackage size = PassiveSurfaceMountPackage._1206;
  private Color labelColor = LABEL_COLOR;
  private transient Area[] body;

  public PassiveSurfaceMountComponent(SiUnit unit, Color bodyColor, Color borderColor) {
    super();
    controlPoints = getFreshControlPoints(2);
    valueUnit = unit;
    this.bodyColor = bodyColor;
    this.borderColor = borderColor;
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
    // Reset body shape.
    body = null;
  }

  @EditableProperty
  public PassiveSurfaceMountPackage getSize() {
    return size;
  }

  public void setSize(PassiveSurfaceMountPackage size) {
    this.size = size;
    updateControlPoints();
    // Reset body shape.
    body = null;
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
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  private void updateControlPoints() {
    Point firstPoint = controlPoints[0];
    controlPoints[0] = firstPoint;
    int pinSize = (int) PIN_SIZE.convertToPixels();
    int smdLength = (int) getSize().getLength().convertToPixels();
    int pinSpacing = smdLength - pinSize;

    // Update control points.
    int dx1;
    int dy1;
    switch (orientation) {
      case DEFAULT:
        dx1 = 0;
        dy1 = pinSpacing;
        break;
      case _90:
        dx1 = -pinSpacing;
        dy1 = 0;
        break;
      case _180:
        dx1 = 0;
        dy1 = -pinSpacing;
        break;
      case _270:
        dx1 = pinSpacing;
        dy1 = 0;
        break;
      default:
        throw new RuntimeException("Unexpected orientation: " + orientation);
    }
    controlPoints[1] = new Point(firstPoint.x + dx1, firstPoint.y + dy1);
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int smdWidth = (int) getSize().getWidth().convertToPixels();
      int smdLength = (int) getSize().getLength().convertToPixels();
      int pinSize = (int) PIN_SIZE.convertToPixels();
      int width;
      int height;

      // create main body
      switch (orientation) {
        case DEFAULT:
          width = smdWidth;
          height = smdLength;
          x = controlPoints[0].x - smdWidth / 2;
          y = controlPoints[0].y - pinSize / 2;
          break;
        case _90:
          width = smdLength;
          height = smdWidth;
          x = controlPoints[1].x - pinSize / 2;
          y = controlPoints[1].y - smdWidth / 2;
          break;
        case _180:
          width = smdWidth;
          height = smdLength;
          x = controlPoints[1].x - smdWidth / 2;
          y = controlPoints[1].y - pinSize / 2;
          break;
        case _270:
          width = smdLength;
          height = smdWidth;
          x = controlPoints[0].x - pinSize / 2;
          y = controlPoints[0].y - smdWidth / 2;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      Area mainArea =
          new Area(new RoundRectangle2D.Double(x, y, width, height, EDGE_RADIUS, EDGE_RADIUS));

      // create contact area
      Area contactArea = new Area();
      if (width > height) {
        contactArea.add(new Area(new Rectangle2D.Double(x, y, pinSize, height)));
        contactArea.add(new Area(new Rectangle2D.Double(x + width - pinSize, y, pinSize, height)));
      } else {
        contactArea.add(new Area(new Rectangle2D.Double(x, y, width, pinSize)));
        contactArea.add(new Area(new Rectangle2D.Double(x, y + height - pinSize, width, pinSize)));
      }
      contactArea.intersect(mainArea);

      mainArea.subtract(contactArea);
      body[0] = mainArea;
      body[1] = contactArea;
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

    // draw main area
    Area mainArea = getBody()[0];
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    Composite oldComposite = setTransparency(g2d);
    Color fillColor = outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor();
    mainArea.fill(g2d, fillColor);
    g2d.setComposite(oldComposite);
    Color drawColor = tryBorderColor(outlineMode, getBorderColor());
    mainArea.draw(g2d, drawColor);

    // draw contact area
    Area contactArea = getBody()[1];
    if (!outlineMode) {
      setTransparency(g2d);
      contactArea.fill(g2d, PIN_COLOR);
      g2d.setComposite(oldComposite);
    }
    contactArea.draw(g2d, tryBorderColor(outlineMode, PIN_BORDER_COLOR));

    // draw label
    g2d.setFont(project.getFont());
    g2d.setColor(tryLabelColor(outlineMode, getLabelColor()));
    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
    Rectangle textTarget = mainArea.getBounds();
    int length = textTarget.height > textTarget.width ? textTarget.height : textTarget.width;
    String label = getLabelForDisplay();
    Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);
    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());

    do {
      g2d.setFont(g2d.getFont().deriveFont(g2d.getFont().getSize2D() - 1));
      fontMetrics = g2d.getFontMetrics(g2d.getFont());
      rect = fontMetrics.getStringBounds(label, g2d);
      textHeight = (int) (rect.getHeight());
      textWidth = (int) (rect.getWidth());
    } while (textWidth > length && g2d.getFont().getSize2D() > 2);

    // Center text horizontally and vertically
    double centerX = textTarget.getX() + textTarget.getWidth() / 2;
    double centerY = textTarget.getY() + textTarget.getHeight() / 2;
    int x = -textWidth / 2;
    int y = -textHeight / 2 + fontMetrics.getAscent() - 1;
    g2d.translate(centerX, centerY);
    g2d.rotate(orientation.getTheta());
    g2d.drawString(label, x, y);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int radius = 4 * width / 32;
    int thickness = getClosestOdd(width / 2);
    g2d.rotate(Math.PI / 4, width / 2, height / 2);
    Area rect =
        Area.roundRect(
            (width - thickness) / 2, 4 * width / 32, thickness, height - 8 * width / 32, radius);
    rect.fillDraw(g2d, getBodyColor(), getBorderColor());
    Area contactArea = new Area();
    int contactSize = 4 * width / 32;
    contactArea.add(Area.rect((width - thickness) / 2, 4 * width / 32, thickness, contactSize));
    contactArea.add(
        Area.rect((width - thickness) / 2, height - 8 * width / 32, thickness, contactSize));
    contactArea.intersect(new Area(rect));
    contactArea.fill(g2d, PIN_COLOR);
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

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  public enum PassiveSurfaceMountPackage {
    _0201(Size.in(0.01), Size.in(0.02)),
    _0402(Size.in(0.02), Size.in(0.04)),
    _0603(Size.in(0.03), Size.in(0.06)),
    _0805(Size.in(0.05), Size.in(0.08)),
    _1206(Size.mm(1.6), Size.mm(3.2)),
    _1210(Size.mm(2.5), Size.mm(3.2)),
    _1806(Size.mm(1.6), Size.mm(4.5)),
    _1812(Size.mm(3.2), Size.mm(4.5));

    private Size width;
    private Size length;

    PassiveSurfaceMountPackage(Size width, Size length) {
      this.width = width;
      this.length = length;
    }

    public Size getWidth() {
      return width;
    }

    public Size getLength() {
      return length;
    }

    @Override
    public String toString() {
      return name().replace("_", "");
    }

    public int getValue() {
      return Integer.parseInt(toString());
    }
  }
}
