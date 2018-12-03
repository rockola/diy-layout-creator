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
package org.diylc.common;

import java.awt.Point;

import org.diylc.core.IDIYComponent;

public interface IComponentTransformer {

  public static final int CLOCKWISE = 1;
  public static final int COUNTER_CLOCKWISE = 1;

  public static final int HORIZONTAL = 0;
  public static final int VERTICAL = 1;
  
  boolean canRotate(IDIYComponent<?> component);
  
  boolean canMirror(IDIYComponent<?> component);
  
  boolean mirroringChangesCircuit();

  void rotate(IDIYComponent<?> component, Point center, int direction);

  void mirror(IDIYComponent<?> component, Point center, int direction);
}
