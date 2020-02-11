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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

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

  /** Get angle. */
  public double getTheta() {
    return this.theta;
  }

  public boolean isRotated() {
    return this != DEFAULT;
  }

  /**
   * Get rotation corresponding to angle.
   *
   * @param point Reference point of rotation.
   * @return AffineTransform implementing the rotation.
   */
  public AffineTransform getRotation(Point point) {
    return getRotation(point.x, point.y);
  }

  public AffineTransform getRotation(int x, int y) {
    return AffineTransform.getRotateInstance(getTheta(), x, y);
  }

  public void applyRotation(Graphics2D g2d, Point point) {
    if (getTheta() != 0) {
      g2d.transform(getRotation(point));
    }
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

  /**
   * Rotate this orientation.
   *
   * @param direction 1 clockwise, -1 counterclockwise
   * @return new orientation
   */
  public Orientation rotate(int direction) {
    // TODO maybe limit direction to be one of {-1, 1} - this is
    // probably how it's always called, but we can't be sure here
    int ordinal = ordinal() + direction;
    // TODO maybe use modulo arithmetic for this
    if (ordinal < 0) {
      ordinal = values().length - 1;
    } else if (ordinal >= values().length) {
      ordinal = 0;
    }
    return values()[ordinal];
  }
}
