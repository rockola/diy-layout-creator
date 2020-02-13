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
import java.awt.Graphics2D;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractComponent;
import org.diylc.core.CreationMethod;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.measures.Size;

@ComponentDescriptor(
    name = "Diode (plastic)",
    author = "Branislav Stojkovic",
    category = "Semiconductors",
    creationMethod = CreationMethod.POINT_BY_POINT,
    instanceNamePrefix = "D",
    description = "Plastic diode, like most rectifier, zener, Schottky, etc.",
    zOrder = AbstractComponent.COMPONENT,
    transformer = SimpleComponentTransformer.class)
public class DiodePlastic extends AbstractDiode {

  private static final long serialVersionUID = 1L;

  public static final Size DEFAULT_WIDTH = Size.in(0.25);
  public static final Size DEFAULT_HEIGHT = Size.in(0.125);
  public static final Size MARKER_WIDTH = Size.mm(1);
  public static final Color BODY_COLOR = Color.darkGray;
  public static final Color MARKER_COLOR = Color.decode("#DDDDDD");
  public static final Color LABEL_COLOR = Color.white;
  public static final Color BORDER_COLOR = BODY_COLOR.darker();

  public DiodePlastic() {
    super(MARKER_COLOR);
    this.labelColor = LABEL_COLOR;
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR_ICON);
    g2d.drawLine(0, height / 2, width, height / 2);
    g2d.setColor(BODY_COLOR);
    g2d.fillRect(6, height / 2 - 3, width - 12, 6);
    g2d.setColor(MARKER_COLOR);
    int markerWidth = 4 * width / 32;
    g2d.fillRect(width - 6 - markerWidth, height / 2 - 3, markerWidth, 6);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(6, height / 2 - 3, width - 12, 6);
  }

  @Override
  protected Size getDefaultWidth() {
    return DEFAULT_HEIGHT;
  }

  @Override
  protected Size getDefaultLength() {
    return DEFAULT_WIDTH;
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    Color finalMarkerColor = tryColor(outlineMode, markerColor);
    g2d.setColor(finalMarkerColor);
    int width = (int) getLength().convertToPixels();
    int markerWidth = (int) MARKER_WIDTH.convertToPixels();
    g2d.fillRect(width - markerWidth, 0, markerWidth, getClosestOdd(getWidth().convertToPixels()));
  }
}
