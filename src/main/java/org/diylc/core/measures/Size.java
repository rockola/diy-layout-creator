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

import org.diylc.parsing.XmlNode;
import org.diylc.utils.Constants;

public class Size extends AbstractMeasure<SizeUnit> implements Comparable<Size> {

  private static final long serialVersionUID = 1L;

  // portrait
  public static final Size A4_HEIGHT = Size.mm(297);
  public static final Size A4_WIDTH = Size.mm(210);

  public Size(Double value, SizeUnit unit) {
    super(value, unit);
  }

  public Size(Size s) {
    super(s.value, s.unit);
  }

  public Size(XmlNode node) {
    super(
        Double.parseDouble(node.attributes.get("value")),
        SizeUnit.valueOf(node.attributes.get("unit")));
  }

  /**
   * Convert to pixels. Return value can be fractional. Use this when the return value is used in
   * calculations. Integer (int) values are returned directly by intPixels().
   */
  public double asPixels() {
    return getUnit().asPixels(getValue());
  }

  public double convertToPixels() {
    return asPixels();
  }

  /** Convert to an integer number of pixels. */
  public int intPixels() {
    return (int) convertToPixels();
  }

  public Size multiply(double multiplier) {
    return new Size(multiplier * getValue(), getUnit());
  }

  public Size eighth() {
    return multiply(0.125);
  }

  public Size quarter() {
    return multiply(0.25);
  }

  public Size half() {
    return multiply(0.5);
  }

  public Size twice() {
    return multiply(2d);
  }

  public Size negative() {
    return multiply(-1d);
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
    return Double.compare(value * unit.getFactor(), o.getValue() * o.getUnit().getFactor());
  }

  public static Size mm(double value) {
    return new Size(value, SizeUnit.mm);
  }

  public static Size mm(int value) {
    return mm((double) value);
  }

  public static Size in(double value) {
    return new Size(value, SizeUnit.in);
  }

  public static Size in(int value) {
    return in((double) value);
  }

  public static Size px(double value) {
    return new Size(value, SizeUnit.px);
  }

  public static Size px(int value) {
    return px((double) value);
  }
}
