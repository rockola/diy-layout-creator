/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.components.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.diylc.common.HorizontalAlignment;
import org.diylc.common.Orientation;
import org.diylc.common.PCBLayer;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractComponent;
import org.diylc.components.transform.TextTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;

@ComponentDescriptor(name = "PCB Text", author = "Branislav Stojkovic", category = "Misc",
    description = "Mirrored text for PCB artwork", instanceNamePrefix = "L", zOrder = IDIYComponent.TRACE,
    flexibleZOrder = false, bomPolicy = BomPolicy.NEVER_SHOW, transformer = TextTransformer.class)
public class PCBText extends AbstractComponent<Void> {

  public static String DEFAULT_TEXT = "Double click to edit text";

  public static Font DEFAULT_FONT = new Font("Courier New", Font.BOLD, 15);

  private static final long serialVersionUID = 1L;

  private Point point = new Point(0, 0);
  private String text = DEFAULT_TEXT;
  private Font font = DEFAULT_FONT;
  private Color color = LABEL_COLOR;
  private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
  private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;
  private Orientation orientation = Orientation.DEFAULT;
  
  private PCBLayer layer = PCBLayer._1;

  @SuppressWarnings("incomplete-switch")
  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    g2d.setColor(componentState == ComponentState.SELECTED ? LABEL_COLOR_SELECTED : color);
    g2d.setFont(font);
    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D rect = fontMetrics.getStringBounds(text, g2d);

    int textHeight = (int) rect.getHeight();
    int textWidth = (int) rect.getWidth();

    int x = point.x;
    int y = point.y;
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

    switch (getOrientation()) {
      case _90:
        g2d.rotate(Math.PI / 2, point.x, point.y);
        break;
      case _180:
        g2d.rotate(Math.PI, point.x, point.y);
        break;
      case _270:
        g2d.rotate(Math.PI * 3 / 2, point.x, point.y);
        break;
    }

    // Flip horizontally
    AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
    tx.translate(-2 * x - textWidth, 0);
    g2d.transform(tx);

    g2d.drawString(text, x, y);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(LABEL_COLOR);
    g2d.setFont(DEFAULT_FONT.deriveFont(15f * width / 32).deriveFont(Font.BOLD));

    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D rect = fontMetrics.getStringBounds("Abc", g2d);

    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());

    // Center text horizontally and vertically.
    int x = (width - textWidth) / 2 + 1;
    int y = (height - textHeight) / 2 + fontMetrics.getAscent();
    g2d.scale(-1, 1);
    g2d.translate(-width, 0);

    g2d.drawString("Abc", x, y);
  }

  @EditableProperty
  public Font getFont() {
    return font;
  }

  public void setFont(Font font) {
    this.font = font;
  }

  // Bold and italic fields are named to be alphabetically after Font. This is
  // important!

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

  @EditableProperty(defaultable = false)
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
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
  
  @EditableProperty
  public PCBLayer getLayer() {
    if (layer == null) {
      layer = PCBLayer._1;
    }
    return layer;
  }

  public void setLayer(PCBLayer layer) {
    this.layer = layer;
  }

  @Override
  public String getName() {
    return super.getName();
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}
  
  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }
}
