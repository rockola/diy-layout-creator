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

package org.diylc.components.connectivity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import org.apache.commons.text.WordUtils;
import org.diylc.common.PcbLayer;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractComponent;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;

@ComponentDescriptor(
    name = "Solder Pad",
    category = "Connectivity",
    author = "Branislav Stojkovic",
    description = "Copper solder pad, round or square",
    instanceNamePrefix = "Pad",
    zOrder = AbstractComponent.TRACE + 0.1,
    bomPolicy = BomPolicy.NEVER_SHOW,
    autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "PCB",
    transformer = SimpleComponentTransformer.class)
public class SolderPad extends AbstractComponent {

  private static final long serialVersionUID = 1L;

  public static final Size SIZE = Size.in(0.09);
  public static final Size HOLE_SIZE = Size.mm(0.8);
  public static final Color COLOR = Color.black;

  private Size size = SIZE;
  private Color color = COLOR;
  private Point point = new Point(0, 0);
  private SolderPadType type = SolderPadType.ROUND;
  private Size holeSize = HOLE_SIZE;
  private PcbLayer layer = PcbLayer._1;

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
    double diameter = getSize().convertToPixels();
    double holeDiameter = getHoleSize().convertToPixels();
    g2d.setColor(tryColor(false, color));
    drawingObserver.startTrackingContinuityArea(true);
    switch (type) {
      case ROUND:
        g2d.fill(Area.circle(point, diameter));
        break;
      case OVAL_HORIZONTAL:
        g2d.fill(Area.oval(point, diameter, 0.75 * diameter));
        break;
      case OVAL_VERTICAL:
        g2d.fill(Area.oval(point, 0.75 * diameter, diameter));
        break;
      default:
        g2d.fill(Area.centeredSquare(point, diameter));
    }
    drawingObserver.stopTrackingContinuityArea();
    if (getHoleSize().getValue() > 0) {
      g2d.setColor(CANVAS_COLOR);
      g2d.fill(Area.circle(point, holeDiameter));
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int diameter = getClosestOdd(width / 2);
    Area.circle(width / 2, height / 2, diameter).fill(g2d, COLOR);
    int holeDiameter = 5;
    Area.circle(width / 2, height / 2, holeDiameter).fill(g2d, CANVAS_COLOR);
  }

  @EditableProperty(validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getSize() {
    return size;
  }

  public void setSize(Size size) {
    this.size = size;
  }

  @EditableProperty(name = "Hole", validatorClass = PositiveMeasureValidator.class)
  public Size getHoleSize() {
    if (holeSize == null) {
      holeSize = HOLE_SIZE;
    }
    return holeSize;
  }

  public void setHoleSize(Size holeSize) {
    this.holeSize = holeSize;
  }

  @EditableProperty
  public PcbLayer getLayer() {
    if (layer == null) {
      layer = PcbLayer._1;
    }
    return layer;
  }

  public void setLayer(PcbLayer layer) {
    this.layer = layer;
  }

  @Override
  public int getControlPointCount() {
    return 1;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public Point getControlPoint(int index) {
    return point;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.point.setLocation(point);
  }

  @EditableProperty(name = "Color")
  public Color getLeadColor() {
    return color;
  }

  public void setLeadColor(Color color) {
    this.color = color;
  }

  @EditableProperty
  public SolderPadType getType() {
    return type;
  }

  public void setType(SolderPadType type) {
    this.type = type;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  public enum SolderPadType {
    ROUND,
    SQUARE,
    OVAL_HORIZONTAL,
    OVAL_VERTICAL;

    @Override
    public String toString() {
      return WordUtils.capitalize(name().replace('_', ' '));
    }
  }
}
