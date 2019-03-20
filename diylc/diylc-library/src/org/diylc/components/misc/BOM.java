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
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.BomEntry;
import org.diylc.utils.BomMaker;

@ComponentDescriptor(name = "Bill of Materials", author = "Branislav Stojkovic", category = "Misc", description = "",
    instanceNamePrefix = "BOM", zOrder = IDIYComponent.TEXT, bomPolicy = BomPolicy.NEVER_SHOW,
    autoEdit = false)
public class BOM extends AbstractComponent<Void> {

  public static Size DEFAULT_SIZE = new Size(10d, SizeUnit.cm);
  public static Size SPACING = new Size(0.1d, SizeUnit.in);
  public static Color COLOR = Color.black;
  public static String DEFAULT_TEXT = "No components to show in the Bill of Materials";

  private static final long serialVersionUID = 1L;
  private Size size = DEFAULT_SIZE;

  private Point point = new Point(0, 0);
  private Color color = COLOR;
  
  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    List<BomEntry> bom = BomMaker.getInstance().createBom(project.getComponents());
    // Cleanup entries that do not have a value set.
    Iterator<BomEntry> iterator = bom.iterator();
    while (iterator.hasNext()) {
      BomEntry entry = iterator.next();
      if (entry.getValue() == null || entry.getName().toLowerCase().contains("bom")) {
        iterator.remove();
      }
    }
    g2d.setFont(project.getFont());
    g2d.setColor(componentState == ComponentState.DRAGGING || componentState == ComponentState.SELECTED ? SELECTION_COLOR
        : getColor());
    // Determine maximum name length and maximum value length to calculate
    // number of columns.
    FontMetrics fontMetrics = g2d.getFontMetrics();
    int maxNameWidth = 0;
    int maxValueWidth = 0;
    int maxHeight = 0;
    for (BomEntry entry : bom) {
      String valueStr;
      if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
        valueStr = "qty " + entry.getQuantity().toString();
      } else {
        valueStr = entry.getValue().toString();
      }
      Rectangle2D stringBounds = fontMetrics.getStringBounds(entry.getName(), g2d);
      if (stringBounds.getWidth() > maxNameWidth) {
        maxNameWidth = (int) stringBounds.getWidth();
      }
      if (stringBounds.getHeight() > maxHeight) {
        maxHeight = (int) stringBounds.getHeight();
      }
      stringBounds = fontMetrics.getStringBounds(valueStr, g2d);
      if (stringBounds.getWidth() > maxValueWidth) {
        maxValueWidth = (int) stringBounds.getWidth();
      }
      if (stringBounds.getHeight() > maxHeight) {
        maxHeight = (int) stringBounds.getHeight();
      }
    }
    // Calculate maximum entry size.
    int maxEntrySize = maxNameWidth + maxValueWidth + 2 * (int) SPACING.convertToPixels();
    int columnCount = (int) size.convertToPixels() / maxEntrySize;
    if (columnCount == 0)
      columnCount = 1;
    int columnWidth = (int) size.convertToPixels() / columnCount;
    int entriesPerColumn = (int) Math.ceil(1.d * bom.size() / columnCount);
    if (entriesPerColumn == 0) {
      g2d.drawString(DEFAULT_TEXT, point.x, point.y);
      return;
    }
    for (int i = 0; i < bom.size(); i++) {
      String valueStr;
      BomEntry entry = bom.get(i);
      if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
        valueStr = "qty " + entry.getQuantity().toString();
      } else {
        valueStr = entry.getValue().toString();
      }
      int columnIndex = i / entriesPerColumn;
      int rowIndex = i % entriesPerColumn;
      int x = point.x + columnIndex * columnWidth;
      int y = point.y + rowIndex * maxHeight;
      g2d.drawString(entry.getName(), x, y);
      x += maxNameWidth + SPACING.convertToPixels();
      g2d.drawString(valueStr, x, y);
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(Color.white);
    g2d.fillRect(width / 8, 0, 6 * width / 8, height - 1);
    g2d.setColor(Color.black);
    g2d.drawRect(width / 8, 0, 6 * width / 8, height - 1);
    g2d.setFont(LABEL_FONT.deriveFont(1f * 9 * width / 32).deriveFont(Font.PLAIN));

    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D rect = fontMetrics.getStringBounds("BOM", g2d);

    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());

    // Center text horizontally and vertically.
    int x = (width - textWidth) / 2 + 1;
    int y = textHeight + 2;

    g2d.drawString("BOM", x, y);

    g2d.setFont(g2d.getFont().deriveFont(1f * 5 * width / 32));

    fontMetrics = g2d.getFontMetrics();
    rect = fontMetrics.getStringBounds("resistors", g2d);
    x = (width - textWidth) / 2 + 1;
    y = height / 2 + 2;
    g2d.drawString("resistors", x, y);
    y += rect.getHeight() - 1;
    g2d.drawString("tubes", x, y);
    y += rect.getHeight() - 1;
    g2d.drawString("diodes", x, y);
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
    return VisibilityPolicy.NEVER;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.point.setLocation(point);
  }

  @EditableProperty(name = "Width")
  public Size getSize() {
    return size;
  }

  public void setSize(Size size) {
    this.size = size;
  }

  @EditableProperty
  public Color getColor() {
    if (color == null) {
      color = COLOR;
    }
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}
}
