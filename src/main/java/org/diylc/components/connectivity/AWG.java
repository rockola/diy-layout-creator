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

package org.diylc.components.connectivity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum AWG {
  _8(8),
  _10(10),
  _12(12),
  _14(14),
  _16(16),
  _18(18),
  _20(20),
  _22(22),
  _24(24),
  _26(26),
  _28(28),
  _30(30),
  _32(32),
  _34(34),
  _36(36),
  _38(38),
  _40(40),
  _42(42),
  _44(44);

  private final int gauge;

  private static final Map<Integer, AWG> gauges = new HashMap<Integer, AWG>();

  static {
    for (AWG awg : EnumSet.allOf(AWG.class)) {
      gauges.put(awg.getGauge(), awg);
    }
  }

  AWG(int i) {
    this.gauge = i;
  }

  @Override
  public String toString() {
    return "#"
        + name().replace("_", "")
        + " ("
        + String.format("%1$,.2f", diameterIn() * 25.4)
        + "mm / "
        + String.format("%1$,.5f", diameterIn())
        + "in)";
  }

  public double diameterIn() {
    // TODO magic numbers
    return Math.pow(Math.E, -1.12436 - 0.11594 * getValue());
  }

  public int getValue() {
    return Integer.parseInt(name().replace("_", ""));
  }

  public int getGauge() {
    return gauge;
  }

  public static AWG getGauge(int value) {
    return gauges.get(value);
  }
}
