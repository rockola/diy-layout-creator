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

package org.diylc.components.semiconductors;

import java.util.StringJoiner;

public enum TransistorPinout {
  BJT_EBC,
  BJT_CBE,
  JFET_DSG,
  JFET_GSD,
  JFET_DGS,
  JFET_SGD,
  MOSFET_DSG,
  MOSFET_GSD,
  MOSFET_DGS,
  MOSFET_SGD;

  @Override
  public String toString() {
    String[] parts = name().split("_");
    StringJoiner sj = new StringJoiner("-");
    for (int i = 0; i < parts[1].length(); i++) {
      sj.add(parts[1].substring(i, i + 1));
    }
    return sj.toString() + "     " + parts[0];
  }
}
