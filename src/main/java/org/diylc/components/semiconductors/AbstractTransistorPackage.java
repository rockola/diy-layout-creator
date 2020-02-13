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
import org.diylc.common.Display;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.Area;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.netlist.ISpiceMapper;

public abstract class AbstractTransistorPackage extends AbstractTransparentComponent
    implements ISpiceMapper {

  private static final long serialVersionUID = 1L;

  protected TransistorPinout pinout;
  protected Orientation orientation = Orientation.DEFAULT;
  protected transient Area[] body;

  protected Color bodyColor;
  protected Color borderColor;
  protected Color labelColor = LABEL_COLOR;

  public AbstractTransistorPackage() {
    super();
    controlPoints = getFreshControlPoints(3);
    setDisplay(Display.NAME);
    alpha = 100;
  }

  protected abstract void updateControlPoints();

  @Override
  public int mapToSpiceNode(int index) {
    if (getPinout() == null) {
      return index;
    }

    switch (getPinout()) {
      case BJT_CBE:
      case JFET_DGS:
      case MOSFET_DGS:
        return index;
      case BJT_EBC:
      case JFET_SGD:
      case MOSFET_SGD:
        return 2 - index;
      case JFET_DSG:
      case MOSFET_DSG:
        switch (index) {
          case 0:
            return 0;
          case 1:
            return 2;
          case 2:
            return 1;
          default:
            throw new RuntimeException("unknown index " + index + " for pinout " + getPinout());
        }
      case JFET_GSD:
      case MOSFET_GSD:
        switch (index) {
          case 0:
            return 1;
          case 1:
            return 2;
          case 2:
            return 0;
          default:
            throw new RuntimeException("unknown index " + index + " for pinout " + getPinout());
        }
      default:
        throw new RuntimeException("unknown pinout " + getPinout());
    }
  }

  @Override
  public String getComment() {
    return getPinout() == null
        ? "Pinout not configured, validate this line"
        : getPinout().toString() + " pinout";
  }

  @Override
  public String getPrefix() {
    return getPinout() == null ? null : getPinout().name().substring(0, 1);
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
  public boolean isControlPointSticky(int index) {
    return true;
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

  @EditableProperty
  public TransistorPinout getPinout() {
    return pinout;
  }

  public void setPinout(TransistorPinout pinout) {
    this.pinout = pinout;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return (index >= 3) ? null : Integer.toString(index + 1);
  }
}
