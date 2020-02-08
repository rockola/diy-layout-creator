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

public enum Orientation {
  DEFAULT(0, 0),
  _90(90, Math.PI / 2),
  _180(180, Math.PI),
  _270(270, Math.PI * 3 / 2);

  private int value;
  private double theta;

  Orientation(int v, double theta) {
    value = v;
    this.theta = theta;
  }

  @Override
  public String toString() {
    if (this == DEFAULT) {
      return "Default";
    } else {
      return name().replace("_", "") + " degrees clockwise";
    }
  }

  public int toInt() {
    return this.value;
  }

  public boolean isDefault() {
    return this == DEFAULT;
  }

  public boolean is90() {
    return this == _90;
  }

  public boolean is180() {
    return this == _180;
  }

  public boolean is270() {
    return this == _270;
  }

  /**
     Get angle.
  */
  public double getTheta() {
    return this.theta;
  }

  public Orientation mirrorHorizontal() {
    switch (this) {
      case _90:
        return _270;
      case _270:
        return _90;
      default:
        return this;
    }
  }

  public Orientation mirrorVertical() {
    switch (this) {
      case DEFAULT:
        return _180;
      case _180:
        return DEFAULT;
      default:
        return this;
    }
  }

  public Orientation mirror(int hv) {
    switch (hv) {
      case IComponentTransformer.HORIZONTAL:
        return mirrorHorizontal();
      case IComponentTransformer.VERTICAL:
        return mirrorVertical();
      default:
        throw new RuntimeException("Unknown direction " + hv);
    }
  }
}
