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

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

public abstract class PassiveSMDComponent<T> extends AbstractTransparentComponent<T> {

  private static final long serialVersionUID = 1L;

  public static final Color PIN_COLOR = METAL_COLOR;
  public static final Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static final Color LABEL_COLOR = Color.white;
  public static final int EDGE_RADIUS = 4;
  public static final Size PIN_SIZE = new Size(0.8d, SizeUnit.mm);

  protected T value;
  protected Color bodyColor;
  protected Color borderColor;

  private Orientation orientation = Orientation.DEFAULT;
  private SMDSize size = SMDSize._1206;
  private Point[] controlPoints = new Point[] {new Point(0, 0), new Point(0, 0)};
  private Color labelColor = LABEL_COLOR;
  private transient Area[] body;

  public PassiveSMDComponent() {
    super();
    updateControlPoints();
    display = Display.NAME;
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

  @EditableProperty
  public SMDSize getSize() {
    return size;
  }

  public void setSize(SMDSize size) {
    this.size = size;
    updateControlPoints();
    // Reset body shape.
    body = null;
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
    Composite oldComposite = setTransparency(g2d);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
    g2d.fill(mainArea);
    g2d.setComposite(oldComposite);

    Color finalBorderColor = tryBorderColor(outlineMode, getBorderColor());
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(mainArea);

    // draw contact area
    Area contactArea = getBody()[1];
    if (!outlineMode) {
      setTransparency(g2d);
      g2d.setColor(PIN_COLOR);
      g2d.fill(contactArea);
      g2d.setComposite(oldComposite);
    }

    finalBorderColor = tryBorderColor(outlineMode, PIN_BORDER_COLOR);
    g2d.setColor(finalBorderColor);
    g2d.draw(contactArea);

    // Draw label.
    g2d.setFont(project.getFont());
    Color finalLabelColor = tryLabelColor(outlineMode, getLabelColor());
    g2d.setColor(finalLabelColor);
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
    int contactSize = 4 * width / 32;
    int thickness = getClosestOdd(width / 2);
    g2d.rotate(Math.PI / 4, width / 2, height / 2);
    RoundRectangle2D rect =
        new RoundRectangle2D.Double(
            (width - thickness) / 2,
            4 * width / 32,
            thickness,
            height - 8 * width / 32,
            radius,
            radius);
    g2d.setColor(getBodyColor());
    g2d.fill(rect);
    g2d.setColor(getBorderColor());
    g2d.draw(rect);
    Area contactArea = new Area();
    contactArea.add(
        new Area(
            new Rectangle2D.Double(
                (width - thickness) / 2, 4 * width / 32, thickness, contactSize)));
    contactArea.add(
        new Area(
            new Rectangle2D.Double(
                (width - thickness) / 2, height - 8 * width / 32, thickness, contactSize)));
    contactArea.intersect(new Area(rect));
    g2d.setColor(PIN_COLOR);
    g2d.fill(contactArea);
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

  public enum SMDSize {
    _0805(new Size(0.05d, SizeUnit.in), new Size(0.08d, SizeUnit.in)),
    _1206(new Size(1.6d, SizeUnit.mm), new Size(3.2d, SizeUnit.mm)),
    _1210(new Size(2.5d, SizeUnit.mm), new Size(3.2d, SizeUnit.mm)),
    _1806(new Size(1.6d, SizeUnit.mm), new Size(4.5d, SizeUnit.mm)),
    _1812(new Size(3.2d, SizeUnit.mm), new Size(4.5d, SizeUnit.mm));

    private Size width;
    private Size length;

    SMDSize(Size width, Size length) {
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
