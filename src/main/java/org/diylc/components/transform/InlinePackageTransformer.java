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
import org.diylc.components.electromechanical.DipSwitch;
import org.diylc.components.passive.AudioTransformer;
import org.diylc.components.semiconductors.BridgeRectifier;
import org.diylc.components.semiconductors.DualInlineIc;
import org.diylc.components.semiconductors.InlinePackage;
import org.diylc.components.semiconductors.SingleInlineIc;
import org.diylc.core.IDIYComponent;

public class InlinePackageTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return component instanceof InlinePackage;
  }

  @Override
  public boolean mirroringChangesCircuit(IDIYComponent<?> component) {
    return component instanceof DualInlineIc
        || component instanceof DipSwitch
        || component instanceof AudioTransformer
        || component instanceof BridgeRectifier;
  }

  @Override
  public void rotate(IDIYComponent<?> component, Point center, int direction) {
    AffineTransform rotate =
        AffineTransform.getRotateInstance(Math.PI / 2 * direction, center.x, center.y);
    for (int index = 0; index < component.getControlPointCount(); index++) {
      Point p = new Point(component.getControlPoint(index));
      rotate.transform(p, p);
      component.setControlPoint(p, index);
    }

    InlinePackage ic = (InlinePackage) component;
    ic.setOrientation(ic.getOrientation().rotate(direction));
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point center, int direction) {
    // TODO: combine common code in mirrorDil() and mirrorSil()
    if (component instanceof DualInlineIc) {
      mirrorDil(component, center, direction);
    } else if (component instanceof SingleInlineIc) {
      mirrorSil(component, center, direction);
    } else {
      throw new RuntimeException(
          String.format(
              "InlinePackageTransformer: don't know how to mirror %s", component.getClass()));
    }
  }

  private void mirrorSil(IDIYComponent<?> component, Point center, int direction) {
    SingleInlineIc ic = (SingleInlineIc) component;
    int dx = center.x - ic.getControlPoint(0).x;
    int dy = center.y - ic.getControlPoint(0).y;
    if (direction == IComponentTransformer.HORIZONTAL) {
      Orientation o = ic.getOrientation();
      switch (o) {
        case _90:
          o = Orientation._270;
          break;
        case _270:
          o = Orientation._90;
          break;
        default:
      }

      for (int i = 0; i < ic.getControlPointCount(); i++) {
        Point p = ic.getControlPoint(i);
        ic.setControlPoint(new Point(p.x + 2 * dx, p.y), i);
      }

      ic.setOrientation(o);
    } else {
      Orientation o = ic.getOrientation();
      switch (o) {
        case DEFAULT:
          o = Orientation._180;
          break;
        case _180:
          o = Orientation.DEFAULT;
          break;
        default:
      }

      for (int i = 0; i < ic.getControlPointCount(); i++) {
        Point p = ic.getControlPoint(i);
        ic.setControlPoint(new Point(p.x, p.y + 2 * dy), i);
      }

      ic.setOrientation(o);
    }
  }

  private void mirrorDil(IDIYComponent<?> component, Point center, int direction) {
    DualInlineIc ic = (DualInlineIc) component;

    if (direction == IComponentTransformer.HORIZONTAL) {
      int dx = 2 * (center.x - ic.getControlPoint(1).x);
      int dy = 0;
      Orientation o = ic.getOrientation();
      switch (o) {
        case _90:
          o = Orientation._270;
          dx -= 2 * (ic.getControlPoint(0).x - ic.getControlPoint(1).x);
          dy -= ic.getControlPoint(0).y - ic.getControlPoint(ic.getControlPointCount() - 1).y;
          break;
        case _180:
          dx += ic.getControlPoint(0).x - ic.getControlPoint(ic.getControlPointCount() - 1).x;
          break;
        case _270:
          dx -= 2 * (ic.getControlPoint(0).x - ic.getControlPoint(1).x);
          dy -= ic.getControlPoint(0).y - ic.getControlPoint(ic.getControlPointCount() - 1).y;
          o = Orientation._90;
          break;
        case DEFAULT:
        default:
          dx += ic.getControlPoint(0).x - ic.getControlPoint(ic.getControlPointCount() - 1).x;
      }

      for (int i = 0; i < ic.getControlPointCount(); i++) {
        Point p = ic.getControlPoint(i);
        ic.setControlPoint(new Point(p.x + dx, p.y + dy), i);
      }

      ic.setOrientation(o);
    } else {
      int dx = 0;
      int dy = 2 * (center.y - ic.getControlPoint(1).y);
      Orientation o = ic.getOrientation();
      switch (o) {
        case _90:
          dy += ic.getControlPoint(0).y - ic.getControlPoint(ic.getControlPointCount() - 1).y;
          break;
        case _180:
          dx -= ic.getControlPoint(0).x - ic.getControlPoint(ic.getControlPointCount() - 1).x;
          dy -= 2 * (ic.getControlPoint(0).y - ic.getControlPoint(1).y);
          o = Orientation.DEFAULT;
          break;
        case _270:
          dy += ic.getControlPoint(0).y - ic.getControlPoint(ic.getControlPointCount() - 1).y;
          break;
        case DEFAULT:
        default:
          dx -= ic.getControlPoint(0).x - ic.getControlPoint(ic.getControlPointCount() - 1).x;
          dy -= 2 * (ic.getControlPoint(0).y - ic.getControlPoint(1).y);
          o = Orientation._180;
      }

      for (int i = 0; i < ic.getControlPointCount(); i++) {
        Point p = ic.getControlPoint(i);
        ic.setControlPoint(new Point(p.x + dx, p.y + dy), i);
      }

      ic.setOrientation(o);
    }
  }
}
