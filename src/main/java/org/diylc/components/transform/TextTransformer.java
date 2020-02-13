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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.common.IComponentTransformer;
import org.diylc.components.AbstractComponent;
import org.diylc.components.misc.Label;
import org.diylc.components.misc.Misc;
import org.diylc.components.misc.PcbText;

public class TextTransformer implements IComponentTransformer {

  private static final Logger LOG = LogManager.getLogger(TextTransformer.class);

  @Override
  public boolean canRotate(AbstractComponent component) {
    return component instanceof Label || component instanceof PcbText;
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

    if (component instanceof Misc) {
      Misc snap = (Misc) component;
      snap.setOrientation(snap.getOrientation().rotate(direction));
    } else {
      LOG.error("rotate() trying to rotate {} but don't know how", component.getClass().getName());
    }
  }

  @Override
  public boolean canMirror(AbstractComponent component) {
    return false;
  }

  @Override
  public boolean mirroringChangesCircuit() {
    return false;
  }

  @Override
  public void mirror(AbstractComponent component, Point center, int direction) {
    throw new RuntimeException("Unexpected call to mirror() in TextTransformer");
  }
}
