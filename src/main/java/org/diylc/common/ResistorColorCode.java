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

package org.diylc.common;

import java.awt.Color;
import org.apache.commons.text.WordUtils;
import org.diylc.core.measures.SiUnit;
import org.diylc.core.measures.Value;

public enum ResistorColorCode {
  NONE,
  _4_BAND,
  _5_BAND;

  private static final Color[] COLOR_DIGITS =
      new Color[] {
        Color.black,
        Color.decode("#8B4513"),
        Color.red,
        Color.orange,
        Color.yellow,
        Color.decode("#76EE00"),
        Color.blue,
        Color.decode("#91219E"),
        Color.lightGray,
        Color.white
      };
  private static final Color[] COLOR_MULTIPLIER =
      new Color[] {
        Color.lightGray.brighter(),
        Color.decode("#FFB90F"),
        Color.black,
        Color.decode("#8B4513"),
        Color.red,
        Color.orange,
        Color.yellow,
        Color.decode("#76EE00"),
        Color.blue
      };

  @Override
  public String toString() {
    if (name().startsWith("_")) {
      return name().substring(1).replace("_", " ").toLowerCase();
    }
    return WordUtils.capitalize(name());
  }

  public Color[] getBands(Value value) {
    if (value.getUnit() != SiUnit.OHM || value.getValue() < 0) {
      return null;
    }

    // epsilon is used for zero value checking; anything smaller than
    // a nano-ohm is considered 0 for our purposes
    final double epsilon = 1.0e-9;
    Color[] bands;
    if (value.getValue() < epsilon) {
      switch (this) {
        case _4_BAND:
          bands = new Color[3];
          bands[0] = COLOR_DIGITS[0];
          bands[1] = COLOR_DIGITS[0];
          bands[2] = COLOR_MULTIPLIER[2];
          break;
        case _5_BAND:
          bands = new Color[4];
          bands[0] = COLOR_DIGITS[0];
          bands[1] = COLOR_DIGITS[0];
          bands[2] = COLOR_DIGITS[0];
          bands[3] = COLOR_MULTIPLIER[2];
          break;
        default:
          bands = new Color[] {};
      }
    } else {
      double base = value.getValue();
      int multiplier = 0;
      while (base > (this == _4_BAND ? 99 : 999)) {
        multiplier += 1;
        base /= 10;
      }
      while (base < (this == _4_BAND ? 10 : 100)) {
        multiplier -= 1;
        base *= 10;
      }
      if (multiplier > 6 || multiplier < -2) {
        // Out of range
        return new Color[] {};
      }
      switch (this) {
        case _4_BAND:
          bands = new Color[3];
          bands[0] = COLOR_DIGITS[(int) (base / 10)];
          bands[1] = COLOR_DIGITS[(int) (base % 10)];
          bands[2] = COLOR_MULTIPLIER[multiplier + 2];
          break;
        case _5_BAND:
          bands = new Color[4];
          bands[0] = COLOR_DIGITS[(int) (base / 100)];
          bands[1] = COLOR_DIGITS[(int) (base / 10 % 10)];
          bands[2] = COLOR_DIGITS[(int) (base % 10)];
          bands[3] = COLOR_MULTIPLIER[multiplier + 2];
          break;
        default:
          bands = new Color[] {};
      }
    }
    return bands;
  }
}
