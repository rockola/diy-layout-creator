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

package org.diylc.components.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import org.diylc.common.Config;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;

public abstract class Misc<T> extends AbstractComponent<T> {

  private static final long serialVersionUID = 1L;

  public static final String DEFAULT_TEXT = Config.getString("components.double-click-to-edit");

  protected Point point = new Point(0, 0);
  protected String text = DEFAULT_TEXT;
  protected Color color = LABEL_COLOR;
  protected Font font = LABEL_FONT;
  protected HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
  protected VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;
  protected Orientation orientation = Orientation.DEFAULT;

  protected transient int textHeight;
  protected transient int textWidth;
  protected transient int x;
  protected transient int y;

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D rect = fontMetrics.getStringBounds(text, g2d);
    textHeight = (int) rect.getHeight();
    textWidth = (int) rect.getWidth();
    x = point.x;
    y = point.y;

    switch (getVerticalAlignment()) {
      case CENTER:
        y = point.y - textHeight / 2 + fontMetrics.getAscent();
        break;
      case TOP:
        y = point.y - textHeight + fontMetrics.getAscent();
        break;
      case BOTTOM:
        y = point.y + fontMetrics.getAscent();
        break;
      default:
        throw new RuntimeException("Unexpected alignment: " + getVerticalAlignment());
    }
    switch (getHorizontalAlignment()) {
      case CENTER:
        x = point.x - textWidth / 2;
        break;
      case LEFT:
        x = point.x;
        break;
      case RIGHT:
        x = point.x - textWidth;
        break;
      default:
        throw new RuntimeException("Unexpected alignment: " + getHorizontalAlignment());
    }
    g2d.setColor(isSelected() ? LABEL_COLOR_SELECTED : color);
    g2d.setFont(font);

    g2d.rotate(getOrientation().getTheta(), point.x, point.y);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D rect = fontMetrics.getStringBounds("Abc", g2d);

    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());

    // Center text horizontally and vertically.
    int x = (width - textWidth) / 2 + 1;
    int y = (height - textHeight) / 2 + fontMetrics.getAscent();

    g2d.setColor(LABEL_COLOR);
    flipText(g2d, width);
    g2d.drawString("Abc", x, y);
  }

  protected void flipText(Graphics2D g2d, int width) {}

  /*
  @Override
  public String getName() {
    return super.getName();
  }
  */

  @EditableProperty
  public Font getFont() {
    return font;
  }

  public void setFont(Font font) {
    this.font = font;
  }

  // Bold and italic fields are named to be alphabetically after
  // Font. This is important! (ours is not to question why, I guess)
  @EditableProperty(name = "Font Bold")
  public boolean getBold() {
    return font.isBold();
  }

  public void setBold(boolean bold) {
    if (bold) {
      if (font.isItalic()) {
        font = font.deriveFont(Font.BOLD + Font.ITALIC);
      } else {
        font = font.deriveFont(Font.BOLD);
      }
    } else {
      if (font.isItalic()) {
        font = font.deriveFont(Font.ITALIC);
      } else {
        font = font.deriveFont(Font.PLAIN);
      }
    }
  }

  @EditableProperty(name = "Font Italic")
  public boolean getItalic() {
    return font.isItalic();
  }

  public void setItalic(boolean italic) {
    if (italic) {
      if (font.isBold()) {
        font = font.deriveFont(Font.BOLD + Font.ITALIC);
      } else {
        font = font.deriveFont(Font.ITALIC);
      }
    } else {
      if (font.isBold()) {
        font = font.deriveFont(Font.BOLD);
      } else {
        font = font.deriveFont(Font.PLAIN);
      }
    }
  }

  @EditableProperty
  public Orientation getOrientation() {
    if (orientation == null) {
      orientation = Orientation.DEFAULT;
    }
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
  }

  @EditableProperty(name = "Font Size")
  public int getFontSize() {
    return font.getSize();
  }

  public void setFontSize(int size) {
    font = font.deriveFont((float) size);
  }

  @Override
  public int getControlPointCount() {
    return 1;
  }

  @Override
  public Point getControlPoint(int index) {
    return point;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.point.setLocation(point);
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @EditableProperty(name = "Vertical Alignment")
  public VerticalAlignment getVerticalAlignment() {
    if (verticalAlignment == null) {
      verticalAlignment = VerticalAlignment.CENTER;
    }
    return verticalAlignment;
  }

  public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
    this.verticalAlignment = verticalAlignment;
  }

  @EditableProperty(name = "Horizontal Alignment")
  public HorizontalAlignment getHorizontalAlignment() {
    if (horizontalAlignment == null) {
      horizontalAlignment = HorizontalAlignment.CENTER;
    }
    return horizontalAlignment;
  }

  public void setHorizontalAlignment(HorizontalAlignment alignment) {
    this.horizontalAlignment = alignment;
  }
}
