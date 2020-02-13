/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2020 held jointly by the individual authors.

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

import java.util.HashMap;
import java.util.Map;

public enum SiPrefix {
  ATTO("a", -18),
  FEMTO("f", -15),
  PICO("p", -12),
  NANO("n", -9),
  MICRO("µ", -6), // hooray for UTF-8!
  MILLI("m", -3),
  NONE("", 0),
  KILO("k", 3),
  MEGA("M", 6),
  GIGA("G", 9),
  TERA("T", 12),
  PETA("P", 15),
  EXA("E", 18);

  private String abbreviation;
  private int exponent;

  private static Map<String, SiPrefix> abbreviationMap = new HashMap<>();
  private static Map<Integer, SiPrefix> exponentMap = new HashMap<>();

  static {
    for (SiPrefix prefix : SiPrefix.values()) {
      abbreviationMap.put(prefix.getAbbreviation(), prefix);
      exponentMap.put(prefix.getExponent(), prefix);
    }
  }

  SiPrefix(String abbrev, int exp) {
    abbreviation = abbrev;
    exponent = exp;
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }

  public String getAbbreviation() {
    return abbreviation;
  }

  public int getExponent() {
    return exponent;
  }

  public static SiPrefix fromAbbreviation(String abbrev) {
    return abbreviationMap.get(abbrev);
  }

  public static SiPrefix fromAbbreviation(char abbrev) {
    return fromAbbreviation(String.valueOf(abbrev));
  }

  public static SiPrefix fromAbbreviation(Character abbrev) {
    return fromAbbreviation(String.valueOf(abbrev));
  }

  public static SiPrefix fromExponent(Integer exp) {
    return exponentMap.get(exp);
  }
}
