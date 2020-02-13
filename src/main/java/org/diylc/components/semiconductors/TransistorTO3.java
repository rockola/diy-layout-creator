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
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.common.Display;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractComponent;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.Area;
import org.diylc.components.TwoCircleTangent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.StringValue;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@StringValue
@ComponentDescriptor(
    name = "Transistor (TO-3)",
    author = "Branislav Stojkovic",
    category = "Semiconductors",
    instanceNamePrefix = "Q",
    description = "Transistor with large metal body",
    zOrder = AbstractComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_VALUE)
public class TransistorTO3 extends AbstractTransparentComponent {

  private static final long serialVersionUID = 1L;

  public static final Color BODY_COLOR = Color.lightGray;
  public static final Color BORDER_COLOR = BODY_COLOR.darker();
  public static final Color PIN_COLOR = Color.decode("#00B2EE");
  public static final Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static final Color LABEL_COLOR = Color.black;
  public static final Size LARGE_DIAMETER = Size.mm(26.2);
  public static final Size INNER_DIAMETER = Size.mm(21.3);
  public static final Size SMALL_DIAMETER = Size.mm(8);
  public static final Size HOLE_DISTANCE = Size.mm(30.1);
  public static final Size HOLE_SIZE = Size.mm(4.1);
  public static final Size PIN_SPACING = Size.mm(10.9);
  public static final Size PIN_OFFSET = Size.mm(1.85);
  public static final Size PIN_DIAMETER = Size.mm(1);

  private Orientation orientation = Orientation.DEFAULT;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  private transient Area[] body;

  public TransistorTO3() {
    super();
    controlPoints = getFreshControlPoints(2);
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
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    // Update control points.
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;
    switch (orientation) {
      case DEFAULT:
        controlPoints[1].setLocation(x, y + pinSpacing);
        break;
      case _90:
        controlPoints[1].setLocation(x - pinSpacing, y);
        break;
      case _180:
        controlPoints[1].setLocation(x, y - pinSpacing);
        break;
      case _270:
        controlPoints[1].setLocation(x + pinSpacing, y);
        break;
      default:
        throw new RuntimeException("Unexpected orientation: " + orientation);
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      int pinOffset = (int) PIN_OFFSET.convertToPixels();
      int x = (controlPoints[0].x + controlPoints[1].x) / 2;
      int y = (controlPoints[0].y + controlPoints[1].y) / 2;

      switch (orientation) {
        case DEFAULT:
          x += pinOffset;
          break;
        case _90:
          y += pinOffset;
          break;
        case _180:
          x -= pinOffset;
          break;
        case _270:
          y -= pinOffset;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }

      int largeDiameter = getClosestOdd(LARGE_DIAMETER.convertToPixels());
      int smallDiameter = getClosestOdd(SMALL_DIAMETER.convertToPixels());
      int holeDistance = getClosestOdd(HOLE_DISTANCE.convertToPixels());
      int holeSize = getClosestOdd(HOLE_SIZE.convertToPixels());
      TwoCircleTangent left =
          new TwoCircleTangent(
              new Point2D.Double(x, y),
              new Point2D.Double(x - holeDistance / 2, y),
              largeDiameter / 2,
              smallDiameter / 2);
      TwoCircleTangent right =
          new TwoCircleTangent(
              new Point2D.Double(x, y),
              new Point2D.Double(x + holeDistance / 2, y),
              largeDiameter / 2,
              smallDiameter / 2);

      body[0] = left;
      body[0].add(right);
      body[0].subtract(Area.circle(x - holeDistance / 2, y, holeSize));
      body[0].subtract(Area.circle(x + holeDistance / 2, y, holeSize));
      if (!orientation.isDefault()) {
        body[0].transform(orientation.getRotation(x, y));
      }
      int innerDiameter = getClosestOdd(INNER_DIAMETER.convertToPixels());
      body[1] = Area.circle(x, y, innerDiameter);
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
    int pinSize = (int) PIN_DIAMETER.convertToPixels() / 2 * 2;
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));

    for (Point point : controlPoints) {
      if (!outlineMode) {
        Area.circle(point, pinSize).fill(g2d, PIN_COLOR);
      }
      Area.circle(point, pinSize).draw(g2d, tryBorderColor(outlineMode, PIN_BORDER_COLOR));
    }

    Area mainArea = getBody()[0];
    Area innerArea = getBody()[1];

    Composite oldComposite = setTransparency(g2d);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(mainArea);
    g2d.setComposite(oldComposite);

    Color finalBorderColor = tryBorderColor(outlineMode, borderColor);
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(mainArea);
    g2d.draw(innerArea);

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
    int largeR = getClosestOdd(width * 3d / 8);
    int smallR = getClosestOdd(width / 6d);
    int hole = 4 * width / 32;
    Area area =
        new TwoCircleTangent(
            new Point2D.Double(width * 0.5, height * 0.5),
            new Point2D.Double(width / 2, height / 8d),
            largeR,
            smallR);
    area.add(
        (Area)
            new TwoCircleTangent(
                new Point2D.Double(width * 0.5, height * 0.5),
                new Point2D.Double(width / 2, height * 7 / 8d),
                largeR,
                smallR));
    area.subtract(Area.circle(width / 2, height / 8, hole));
    area.subtract(Area.circle(width / 2, height * 7 / 8, hole));
    area.transform(AffineTransform.getRotateInstance(Math.PI / 4, width / 2, height / 2));
    area.fillDraw(g2d, BODY_COLOR, BORDER_COLOR);
    int innerD = getClosestOdd(width / 2d);
    Area.circle(width / 2, height / 2, innerD).draw(g2d, BORDER_COLOR);
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
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }
}
