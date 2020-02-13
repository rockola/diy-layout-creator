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

package org.diylc.components.transform;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractComponent;
import org.diylc.components.semiconductors.TransistorTO220;

public class TO220Transformer implements IComponentTransformer {

  @Override
  public boolean canRotate(AbstractComponent component) {
    return component instanceof TransistorTO220;
  }

  @Override
  public boolean canMirror(AbstractComponent component) {
    return component instanceof TransistorTO220;
  }

  @Override
  public boolean mirroringChangesCircuit() {
    return true;
  }

  @Override
  public void rotate(AbstractComponent component, Point center, int direction) {
    AffineTransform rotate =
        AffineTransform.getRotateInstance(Math.PI / 2 * direction, center.x, center.y);
    for (int index = 0; index < component.getControlPointCount(); index++) {
      Point p = new Point(component.getControlPoint(index));
      rotate.transform(p, p);
      component.setControlPoint(p, index);
    }

    TransistorTO220 transistor = (TransistorTO220) component;
    transistor.setOrientation(transistor.getOrientation().rotate(direction));
  }

  @Override
  public void mirror(AbstractComponent component, Point center, int direction) {
    TransistorTO220 transistor = (TransistorTO220) component;
    if (direction == IComponentTransformer.HORIZONTAL) {
      int dx = 2 * (center.x - transistor.getControlPoint(0).x);
      int dy = 0;
      Orientation o = transistor.getOrientation();
      switch (o) {
        case _90:
          dx += (transistor.getControlPoint(0).x - transistor.getControlPoint(2).x);
          break;
        case _180:
          o = Orientation.DEFAULT;
          dy -= (transistor.getControlPoint(0).y - transistor.getControlPoint(2).y);
          break;
        case _270:
          dx += (transistor.getControlPoint(0).x - transistor.getControlPoint(2).x);
          break;
        case DEFAULT:
        default:
          o = Orientation._180;
          dy -= (transistor.getControlPoint(0).y - transistor.getControlPoint(2).y);
      }

      for (int i = 0; i < transistor.getControlPointCount(); i++) {
        Point p = transistor.getControlPoint(i);
        transistor.setControlPoint(new Point(p.x + dx, p.y + dy), i);
      }

      transistor.setOrientation(o);
    } else {
      int dx = 0;
      int dy = 2 * (center.y - transistor.getControlPoint(0).y);
      Orientation o = transistor.getOrientation();
      switch (o) {
        case _90:
          dx -= (transistor.getControlPoint(0).x - transistor.getControlPoint(2).x);
          o = Orientation._270;
          break;
        case _180:
          dy += (transistor.getControlPoint(0).y - transistor.getControlPoint(2).y);
          break;
        case _270:
          dx -= (transistor.getControlPoint(0).x - transistor.getControlPoint(2).x);
          o = Orientation._90;
          break;
        case DEFAULT:
        default:
          dy += (transistor.getControlPoint(0).y - transistor.getControlPoint(2).y);
      }

      for (int i = 0; i < transistor.getControlPointCount(); i++) {
        Point p = transistor.getControlPoint(i);
        transistor.setControlPoint(new Point(p.x + dx, p.y + dy), i);
      }

      transistor.setOrientation(o);
    }
  }
}
