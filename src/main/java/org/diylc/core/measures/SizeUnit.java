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

import org.diylc.utils.Constants;

public enum SizeUnit implements Unit {
  px(Constants.MM_PER_INCH / Constants.PIXELS_PER_INCH),
  mm(1d),
  cm(10d),
  m(1e4d),
  in(Constants.MM_PER_INCH),
  ft(Constants.MM_PER_INCH * 12),
  yd(9144d);

  private double factor;
  private double pixels;

  SizeUnit(double factor) {
    this.factor = factor;
    this.pixels = factor / Constants.MM_PER_INCH * Constants.PIXELS_PER_INCH;
  }

  SizeUnit(SizeUnit u) {
    this(u.factor);
  }

  @Override
  public double getFactor() {
    return factor;
  }

  public double getPixels() {
    return pixels;
  }

  public double asPixels(double value) {
    return value * getPixels();
  }
}
