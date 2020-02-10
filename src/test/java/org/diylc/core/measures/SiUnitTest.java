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

public class SiUnitTest {
  @Test
  public void unitSymbols() {
    // these constants should never be changed as they come from the SI system
    assertEquals(SiUnit.AMPERE.symbol, "A", "ampere symbol should be A");
    assertEquals(SiUnit.FARAD.symbol, "F", "farad symbol should be F");
    assertEquals(SiUnit.HENRY.symbol, "H", "henry symbol should be H");
    assertEquals(SiUnit.METER.symbol, "m", "meter symbol should be m");
    assertEquals(SiUnit.OHM.symbol, "Ω", "ohm symbol should be Ω");
    assertEquals(SiUnit.VOLT.symbol, "V", "volt symbol should be V");
    assertEquals(SiUnit.WATT.symbol, "W", "watt symbol should be W");
  }

  @Test
  public void unitNames() {
    Map<String, SiUnit> map = Stream.of(new Object[][] {
        { "ampere", SiUnit.AMPERE },
        { "farad", SiUnit.FARAD },
        { "henry", SiUnit.HENRY },
        { "meter", SiUnit.METER },
        { "ohm", SiUnit.OHM },
        { "volt", SiUnit.VOLT },
        { "watt", SiUnit.WATT }
      }).collect(Collectors.toMap(data -> (String) data[0], data -> (SiUnit) data[1]));

    for (Map.Entry<String, SiUnit> entry : map.entrySet()) {
      assertEquals(entry.getKey(), entry.getValue().toString());
      assertEquals(WordUtils.capitalize(entry.getKey()), entry.getValue().toUnitName());
    }
  }

  @Test
  public void measureToStringTest() {
    assertEquals("1 volts", SiUnit.measureToString(1.0, 0, false, SiUnit.VOLT));
    assertEquals("1.0 volts", SiUnit.measureToString(1.0, 1, false, SiUnit.VOLT));
    assertEquals("2 watts", SiUnit.measureToString(2.0, 0, false, SiUnit.WATT));
    //
    assertEquals("1 V", SiUnit.measureToString(1.0, 0, true, SiUnit.VOLT));
    assertEquals("1 kV", SiUnit.measureToString(1000.0, 0, true, SiUnit.VOLT));
    assertEquals("1.0 kV", SiUnit.measureToString(1000.0, 1, true, SiUnit.VOLT));
    assertEquals("101 mV", SiUnit.measureToString(.101, 0, true, SiUnit.VOLT));
    assertEquals("10 kW", SiUnit.measureToString(10101.0, 0, true, SiUnit.WATT));
    assertEquals("10.1 kW", SiUnit.measureToString(10101.0, 1, true, SiUnit.WATT));
    assertEquals("10.10 kW", SiUnit.measureToString(10101.0, 2, true, SiUnit.WATT));
    assertEquals("10.101 kW", SiUnit.measureToString(10101.0, 3, true, SiUnit.WATT));
    //
    assertEquals("47 µF", SiUnit.measureToString(.000047, 0, true, SiUnit.FARAD));
    assertEquals("47 nF", SiUnit.measureToString(4.7e-8, 0, true, SiUnit.FARAD));
    assertEquals("47 pF", SiUnit.measureToString(4.7e-11, 0, true, SiUnit.FARAD));
    assertEquals("47 µF", SiUnit.measureToString(.000047, 0, SiUnit.FARAD));
    assertEquals("47 nF", SiUnit.measureToString(4.7e-8, 0, SiUnit.FARAD));
    assertEquals("47 pF", SiUnit.measureToString(4.7e-11, 0, SiUnit.FARAD));
  }
}
