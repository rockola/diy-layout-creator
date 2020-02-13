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

import java.awt.Point;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.annotations.EditableProperty;

public abstract class AbstractPotentiometer extends AbstractTransparentComponent {

  private static final long serialVersionUID = 1L;

  protected Orientation orientation = Orientation.DEFAULT;
  protected Taper taper = Taper.LIN;

  protected AbstractPotentiometer() {
    super();
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public String getValueForDisplay() {
    return (getValue() == null ? "" : getValue().toString()) + " " + taper.toString();
  }

  @EditableProperty
  public Taper getTaper() {
    return taper;
  }

  public void setTaper(Taper taper) {
    this.taper = taper;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
  }
}
