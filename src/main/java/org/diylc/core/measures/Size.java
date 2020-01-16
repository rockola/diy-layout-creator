/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2019 held jointly by the individual authors.

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

public class Size extends AbstractMeasure<SizeUnit> implements Comparable<Size> {

  private static final long serialVersionUID = 1L;

  public Size(Double value, SizeUnit unit) {
    super(value, unit);
  }

  public Size(Size s) {
    super(s.value, s.unit);
  }

  /**
     Convert to pixels.
     Return value can be fractional. Use this when the return value is used in calculations.
     Integer (int) values are returned directly by asPixels().
  */
  public double convertToPixels() {
    // double factor = getUnit().getFactor() / SizeUnit.in.getFactor();
    // int grids = (int) (factor * getValue() * Constants.GRIDS_PER_INCH);
    // double remainder = (factor * getValue() * Constants.GRIDS_PER_INCH) - grids;
    // return (int) Math.round(Constants.PIXELS_PER_INCH / Constants.GRIDS_PER_INCH
    // * (grids + remainder));
    return getValue() * getUnit().getFactor()
        / SizeUnit.in.getFactor() * Constants.PIXELS_PER_INCH;
  }

  /**
    Convert to an integer number of pixels.
  */
  public int asPixels() {
    return (int) convertToPixels();
  }

  public static Size parseSize(String value) {
    value = value.replace("*", "");
    for (SizeUnit unit : SizeUnit.values()) {
      if (value.toLowerCase().endsWith(unit.toString().toLowerCase())) {
        value = value.substring(0, value.length() - unit.toString().length()).trim();
        return new Size(parse(value), unit);
      }
    }
    throw new IllegalArgumentException("Could not parse size: " + value);
  }

  @Override
  public int compareTo(Size o) {
    return Double.compare(
        value * unit.getFactor(),
        o.getValue() * o.getUnit().getFactor());
  }
}
