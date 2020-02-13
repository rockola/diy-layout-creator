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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import org.diylc.common.ObjectCache;
import org.diylc.common.ResistorColorCode;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractComponent;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.components.Area;
import org.diylc.core.CreationMethod;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.ComponentValue;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.SiUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.Value;

@ComponentValue(SiUnit.OHM)
@ComponentDescriptor(
    name = "Resistor",
    author = "Branislav Stojkovic",
    category = "Passive",
    creationMethod = CreationMethod.POINT_BY_POINT,
    instanceNamePrefix = "R",
    description = "Resistor layout symbol",
    zOrder = AbstractComponent.COMPONENT,
    transformer = SimpleComponentTransformer.class)
public class Resistor extends AbstractLeadedComponent {

  private static final long serialVersionUID = 1L;

  public static final Size DEFAULT_WIDTH = Size.in(0.5);
  public static final Size DEFAULT_HEIGHT = Size.in(0.125);
  public static final Color BODY_COLOR = Color.decode("#82CFFD");
  public static final Color BORDER_COLOR = BODY_COLOR.darker();
  public static final int BAND_SPACING = 5;
  public static final int FIRST_BAND = -4;

  private Value power = null;
  private ResistorColorCode colorCode = ResistorColorCode._5_BAND;
  private ResistorShape shape = ResistorShape.Standard;

  public Resistor() {
    super();
    valueUnit = SiUnit.OHM;
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
  }

  @Override
  protected boolean supportsStandingMode() {
    return true;
  }

  @Override
  public String getValueForDisplay() {
    return getValue().toString() + (getPower() == null ? "" : " " + getPower().toString());
  }

  @ComponentValue(SiUnit.WATT)
  @EditableProperty(name = "Power Rating")
  public Value getPower() {
    return power;
  }

  public void setPower(Value powerNew) {
    if (powerNew == null || powerNew.getUnit() == SiUnit.WATT) {
      this.power = powerNew;
    }
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR_ICON);
    g2d.drawLine(0, height / 2, width, height / 2);
    g2d.setColor(BODY_COLOR);
    Area a = Area.rect(6, height / 2 - 3, width - 14, 6);
    a.add(new Area(new Ellipse2D.Double(4, height / 2 - 4, 8, 8)));
    a.add(new Area(new Ellipse2D.Double(width - 12, height / 2 - 4, 8, 8)));
    g2d.fill(a);
    g2d.setColor(Color.red);
    g2d.drawLine(11, height / 2 - 3, 11, height / 2 + 3);
    g2d.setColor(Color.orange);
    g2d.drawLine(14, height / 2 - 3, 14, height / 2 + 3);
    g2d.setColor(Color.black);
    g2d.drawLine(17, height / 2 - 3, 17, height / 2 + 3);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(a);
  }

  @Override
  protected Size getDefaultWidth() {
    return DEFAULT_HEIGHT;
  }

  @Override
  protected Size getDefaultLength() {
    return DEFAULT_WIDTH;
  }

  @EditableProperty(name = "Color code")
  public ResistorColorCode getColorCode() {
    return colorCode;
  }

  public void setColorCode(ResistorColorCode colorCode) {
    this.colorCode = colorCode;
  }

  @EditableProperty
  public ResistorShape getShape() {
    if (shape == null) {
      shape = ResistorShape.Standard;
    }
    return shape;
  }

  public void setShape(ResistorShape shape) {
    this.shape = shape;
  }

  @Override
  protected Area getBodyShape() {
    double length = getLength().convertToPixels();
    double width = getClosestOdd(getWidth().convertToPixels());
    Area bodyShape = null;
    if (getShape() == ResistorShape.Standard) {
      bodyShape = Area.rect(width / 2, width / 10, length - width, width * 8 / 10);
      bodyShape.add(new Area(new Ellipse2D.Double(0, 0, width, width)));
      bodyShape.add(new Area(new Ellipse2D.Double(length - width, 0, width, width)));
    } else {
      bodyShape = Area.rect(0, 0, length, width);
    }
    return bodyShape;
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    if (!outlineMode && getValue() != null && colorCode != ResistorColorCode.NONE) {
      Area body = (Area) getBodyShape();
      Stroke stroke = ObjectCache.getInstance().fetchZoomableStroke(2);
      int width = getClosestOdd(getWidth().convertToPixels());
      int x = getShape() == ResistorShape.Standard ? width + FIRST_BAND : -FIRST_BAND;
      Color[] bands = getColorCode().getBands(getValue());
      for (int i = 0; i < bands.length; i++) {
        g2d.setColor(bands[i]);
        Area line = new Area(stroke.createStrokedShape(new Line2D.Double(x, 0, x, width)));
        line.intersect(body);
        g2d.fill(line);
        x += BAND_SPACING;
      }
    }
  }

  @Override
  protected int getLabelOffset(int bodyLength, int bodyWidth, int labelLength) {
    if (getValue() == null
        || getColorCode() == ResistorColorCode.NONE
        || !getLabelOrientation().isDirectional()) {
      return 0;
    }

    Color[] bands = getColorCode().getBands(getValue());
    int bandLenght = FIRST_BAND + BAND_SPACING * (bands.length - 1);

    if (labelLength
        < (getShape() == ResistorShape.Standard
            ? bodyLength - 2 * bodyWidth - bandLenght
            : bodyLength - bandLenght)) {
      return bandLenght;
    }

    int bandArea = getShape() == ResistorShape.Standard ? bodyWidth + bandLenght : -bandLenght;
    return bandArea / 2;
  }

  @EditableProperty(name = "Reverse (standing)")
  public boolean getFlipStanding() {
    return super.getFlipStanding();
  }

  public enum ResistorShape {
    Tubular,
    Standard
  }
}
