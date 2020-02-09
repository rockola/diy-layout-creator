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

package org.diylc.core.measures;

import org.diylc.core.annotations.EditableProperty;

public class Nudge {

  private Size offsetX;
  private Size offsetY;
  private boolean affectStuckComponents;

  @EditableProperty(name = "X-axis", sortOrder = 1)
  public Size getOffsetX() {
    return offsetX;
  }

  public void setOffsetX(Size offsetX) {
    this.offsetX = offsetX;
  }

  @EditableProperty(name = "Y-axis", sortOrder = 2)
  public Size getOffsetY() {
    return offsetY;
  }

  public void setOffsetY(Size offsetY) {
    this.offsetY = offsetY;
  }

  public void setOffsets(Size offsetX, Size offsetY) {
    setOffsetX(offsetX);
    setOffsetY(offsetY);
  }

  public void setOffsets(Size offset) {
    setOffsets(offset, offset);
  }

  @EditableProperty(name = "Include stuck", sortOrder = 10)
  public boolean getAffectStuckComponents() {
    return affectStuckComponents;
  }

  public void setAffectStuckComponents(boolean affectStuckComponents) {
    this.affectStuckComponents = affectStuckComponents;
  }
}
