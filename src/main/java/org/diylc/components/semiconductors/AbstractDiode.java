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
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.components.Area;
import org.diylc.core.annotations.EditableProperty;

public abstract class AbstractDiode extends AbstractLeadedComponent {

  protected Color markerColor;

  protected AbstractDiode(Color markerColor) {
    super();
    this.markerColor = markerColor;
  }

  @Override
  protected boolean supportsStandingMode() {
    return true;
  }

  @Override
  public Color getStandingBodyColor() {
    return getFlipStanding() ? getBodyColor() : getMarkerColor();
  }

  @EditableProperty(name = "Reverse (standing)")
  public boolean getFlipStanding() {
    return super.getFlipStanding();
  }

  @EditableProperty(name = "Marker")
  public Color getMarkerColor() {
    return markerColor;
  }

  public void setMarkerColor(Color markerColor) {
    this.markerColor = markerColor;
  }

  @Override
  protected Area getBodyShape() {
    return Area.rect(
        0f, 0f, getLength().convertToPixels(), getClosestOdd(getWidth().convertToPixels()));
  }
}
