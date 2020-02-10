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
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import org.diylc.common.Display;
import org.diylc.common.ObjectCache;
import org.diylc.common.OrientationHV;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "Fuse Holder (Panel)",
    category = "Electro-Mechanical",
    author = "Branislav Stojkovic",
    description = "Panel mounted fuse holder",
    zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = "FH",
    autoEdit = false)
public class FuseHolderPanel extends AbstractMultiPartComponent<String> {

  private static final long serialVersionUID = 1L;

  // common
  private static final Size SPACING = Size.in(0.2);
  private static final Size LUG_WIDTH = Size.mm(4);
  private static final Size LUG_THICKNESS = Size.mm(0.8);
  private static final Size INNER_DIAMETER = Size.in(0.5);
  private static final Size OUTER_DIAMETER = Size.in(0.6);
  private static final Color BODY_COLOR = Color.decode("#555555");
  private static final Color BORDER_COLOR = BODY_COLOR.darker();
  private static final Color LABEL_COLOR = Color.white;

  protected Point[] controlPoints = getFreshControlPoints(2);
  protected transient Area[] body;
  protected String value = "";
  private OrientationHV orientation = OrientationHV.VERTICAL;
  private boolean hasFuse = false;

  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;

  public FuseHolderPanel() {
    super();
    updateControlPoints();
    display = Display.VALUE;
  }

  private void updateControlPoints() {
    Point firstPoint = controlPoints[0];
    int spacing = (int) SPACING.convertToPixels();

    controlPoints[1].setLocation(
        firstPoint.x + (orientation == OrientationHV.HORIZONTAL ? spacing : 0),
        firstPoint.y + (orientation == OrientationHV.HORIZONTAL ? 0 : spacing));
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
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    // Reset body shape.
    body = null;
  }

  @EditableProperty
  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @EditableProperty
  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public OrientationHV getOrientation() {
    return orientation;
  }

  public void setOrientation(OrientationHV orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }

  @EditableProperty(name = "Fuse")
  public boolean getHasFuse() {
    return hasFuse;
  }

  public void setHasFuse(boolean hasFuse) {
    this.hasFuse = hasFuse;
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
    Area[] body = getBody();
    // Draw body if available.
    if (body != null) {
      Composite oldComposite = setTransparency(g2d);
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
      for (Area a : body) {
        if (a != null) {
          g2d.fill(a);
          break;
        }
      }
      g2d.setComposite(oldComposite);

      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      final Color finalBorderColor = tryBorderColor(outlineMode, getBorderColor());
      g2d.setColor(finalBorderColor);
      for (Area a : body) {
        if (a != null) {
          g2d.draw(a);
        }
      }
    }
    // Do not track these changes because the whole switch has been tracked
    // so far.
    drawingObserver.stopTracking();

    // Draw lugs.
    int lugWidth = getClosestOdd((int) LUG_WIDTH.convertToPixels());
    int lugHeight = getClosestOdd((int) LUG_THICKNESS.convertToPixels());

    if (orientation == OrientationHV.HORIZONTAL) {
      int p = lugHeight;
      lugHeight = lugWidth;
      lugWidth = p;
    }

    for (Point p : controlPoints) {
      if (outlineMode) {
        g2d.setColor(theme().getOutlineColor());
        g2d.drawRect(p.x - lugWidth / 2, p.y - lugHeight / 2, lugWidth, lugHeight);
      } else {
        g2d.setColor(METAL_COLOR);
        g2d.fillRect(p.x - lugWidth / 2, p.y - lugHeight / 2, lugWidth, lugHeight);
      }
    }

    // Draw label.
    g2d.setFont(project.getFont());
    final Color finalLabelColor = tryLabelColor(outlineMode, labelColor);
    g2d.setColor(finalLabelColor);
    String label = getLabelForDisplay();
    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
    Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);
    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());
    // Center text horizontally and vertically
    Rectangle bounds = body[0].getBounds();
    int x = bounds.x + (bounds.width - textWidth) / 2;
    int y = bounds.y + (bounds.height - textHeight) / 2 + fontMetrics.getAscent();
    g2d.drawString(label, x, y);

    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  @Override
  public Area[] getBody() {
    if (body == null) {
      final Point firstPoint = controlPoints[0];
      final int spacing = (int) SPACING.convertToPixels();
      final int outerDiameter = (int) OUTER_DIAMETER.convertToPixels();
      final int innerDiameter = (int) INNER_DIAMETER.convertToPixels();
      final int dx = orientation.isHorizontal() ? spacing / 2 : 0;
      final int dy = orientation.isVertical() ? spacing / 2 : 0;
      body = new Area[2];
      body[0] = new Area(new Ellipse2D.Double(
          firstPoint.x + dx - outerDiameter / 2,
          firstPoint.y + dy - outerDiameter / 2,
          outerDiameter,
          outerDiameter));
      body[1] = new Area(new Ellipse2D.Double(
          firstPoint.x + dx - innerDiameter / 2,
          firstPoint.y + dy - innerDiameter / 2,
          innerDiameter,
          innerDiameter));
    }
    return body;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BODY_COLOR);
    int margin = (int) (2f * width / 32);
    int terminal = (int) (5f * width / 32);
    int terminalSpacingV = width / 4;
    Shape body = new Ellipse2D.Double(margin, margin, width - 2 * margin, height - 2 * margin);
    g2d.fill(body);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(body);
    g2d.setColor(METAL_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f));
    g2d.drawLine(
        width / 2 - terminal / 2,
        height / 2 - terminalSpacingV / 2,
        width / 2 + terminal / 2,
        height / 2 - terminalSpacingV / 2);
    g2d.drawLine(
        width / 2 - terminal / 2,
        height / 2 + terminalSpacingV / 2,
        width / 2 + terminal / 2,
        height / 2 + terminalSpacingV / 2);
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

  @EditableProperty
  public Display getDisplay() {
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
