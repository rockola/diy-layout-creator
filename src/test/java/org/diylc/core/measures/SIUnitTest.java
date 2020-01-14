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

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.WordUtils;

import org.junit.jupiter.api.Test;

public class SIUnitTest {
  @Test
  public void unitSymbols() {
    // these constants should never be changed as they come from the SI system
    assertEquals(SIUnit.AMPERE.symbol, "A", "ampere symbol should be A");
    assertEquals(SIUnit.FARAD.symbol, "F", "farad symbol should be F");
    assertEquals(SIUnit.HENRY.symbol, "H", "henry symbol should be H");
    assertEquals(SIUnit.METER.symbol, "m", "meter symbol should be m");
    assertEquals(SIUnit.OHM.symbol, "Ω", "ohm symbol should be Ω");
    assertEquals(SIUnit.VOLT.symbol, "V", "volt symbol should be V");
    assertEquals(SIUnit.WATT.symbol, "W", "watt symbol should be W");
  }

  @Test
  public void unitNames() {
    Map<String, SIUnit> map = Stream.of(new Object[][] {
        { "ampere", SIUnit.AMPERE },
        { "farad", SIUnit.FARAD },
        { "henry", SIUnit.HENRY },
        { "meter", SIUnit.METER },
        { "ohm", SIUnit.OHM },
        { "volt", SIUnit.VOLT },
        { "watt", SIUnit.WATT }
      }).collect(Collectors.toMap(data -> (String) data[0], data -> (SIUnit) data[1]));

    for (Map.Entry<String, SIUnit> entry : map.entrySet()) {
      assertEquals(entry.getKey(), entry.getValue().toString());
      assertEquals(WordUtils.capitalize(entry.getKey()), entry.getValue().toUnitName());
    }
  }

  @Test
  public void measureToStringTest() {
    assertEquals("1 volts", SIUnit.measureToString(1.0, 0, false, SIUnit.VOLT));
    assertEquals("1.0 volts", SIUnit.measureToString(1.0, 1, false, SIUnit.VOLT));
    assertEquals("2 watts", SIUnit.measureToString(2.0, 0, false, SIUnit.WATT));
    //
    assertEquals("1 V", SIUnit.measureToString(1.0, 0, true, SIUnit.VOLT));
    assertEquals("1 kV", SIUnit.measureToString(1000.0, 0, true, SIUnit.VOLT));
    assertEquals("1.0 kV", SIUnit.measureToString(1000.0, 1, true, SIUnit.VOLT));
    assertEquals("101 mV", SIUnit.measureToString(.101, 0, true, SIUnit.VOLT));
    assertEquals("10 kW", SIUnit.measureToString(10101.0, 0, true, SIUnit.WATT));
    assertEquals("10.1 kW", SIUnit.measureToString(10101.0, 1, true, SIUnit.WATT));
    assertEquals("10.10 kW", SIUnit.measureToString(10101.0, 2, true, SIUnit.WATT));
    assertEquals("10.101 kW", SIUnit.measureToString(10101.0, 3, true, SIUnit.WATT));
    //
    assertEquals("47 µF", SIUnit.measureToString(.000047, 0, true, SIUnit.FARAD));
    assertEquals("47 nF", SIUnit.measureToString(4.7e-8, 0, true, SIUnit.FARAD));
    assertEquals("47 pF", SIUnit.measureToString(4.7e-11, 0, true, SIUnit.FARAD));
    assertEquals("47 µF", SIUnit.measureToString(.000047, 0, SIUnit.FARAD));
    assertEquals("47 nF", SIUnit.measureToString(4.7e-8, 0, SIUnit.FARAD));
    assertEquals("47 pF", SIUnit.measureToString(4.7e-11, 0, SIUnit.FARAD));
  }
}
