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

package org.diylc.common;

import java.awt.Point;
import org.diylc.components.AbstractComponent;

public interface IComponentTransformer {

  int CLOCKWISE = 1;
  int COUNTER_CLOCKWISE = -1;

  int HORIZONTAL = 0;
  int VERTICAL = 1;

  boolean canRotate(AbstractComponent component);

  default boolean canMirror(AbstractComponent component) {
    return canRotate(component);
  }

  default boolean mirroringChangesCircuit() {
    return false;
  }

  default boolean mirroringChangesCircuit(AbstractComponent component) {
    return mirroringChangesCircuit();
  }

  void rotate(AbstractComponent component, Point center, int direction);

  void mirror(AbstractComponent component, Point center, int direction);
}
