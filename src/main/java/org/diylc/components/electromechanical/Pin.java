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

package org.diylc.components.electromechanical;

import java.awt.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.components.Area;
import org.diylc.core.measures.Size;

public class Pin {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(Pin.class);
  private static final Point reference = new Point(0, 0);

  private Area shape;

  private Pin(Area shape) {
    this.shape = new Area(shape);
  }

  public Point getOffset() {
    return new Point(0, 0);
  }

  public Area getShape() {
    return new Area(shape);
  }

  static Pin circular(Point center, Size diameter) {
    return new Pin(Area.circle(center, diameter));
  }

  public static Pin circular(double x, double y, double diameter) {
    return new Pin(Area.circle(x, y, diameter));
  }

  public static Pin circular(double x, double diameter) {
    return circular(x, 0d, diameter);
  }

  static Pin circular(double diameter) {
    return circular(0d, 0d, diameter);
  }

  static Pin circular(Size x, Size y, Size diameter) {
    return circular(x.asPixels(), y.asPixels(), diameter.asPixels());
  }

  static Pin circular(Size x, Size diameter) {
    return circular(x.asPixels(), diameter.asPixels());
  }

  static Pin rectangular(Point center, double width, double height) {
    return new Pin(Area.centeredRect(center, width, height));
  }

  static Pin rectangular(Point center, Size width, Size height) {
    return rectangular(center, width.asPixels(), height.asPixels());
  }

  static Pin rectangular(double width, double height) {
    return rectangular(reference, width, height);
  }

  static Pin rectangular(Size width, Size height) {
    return rectangular(width.asPixels(), height.asPixels());
  }

  static Pin square(double width) {
    return rectangular(width, width);
  }

  static Pin square(Size width) {
    return square(width.asPixels());
  }
}
