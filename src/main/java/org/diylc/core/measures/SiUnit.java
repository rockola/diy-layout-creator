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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum SiUnit {
  AMPERE("A", -6, 0), // current
  FARAD("F", -12, 0), // capacitance
  HENRY("H", -12, 0), // inductance
  METER("m", -9, 0), // length
  OHM("Ω", 0, 6), // resistance
  VOLT("V", -3, 3), // voltage
  WATT("W", -3, 3); // power

  public final String symbol;
  public final int rangeStart;
  public final int rangeEnd;
  public final Map<Integer, String> range;
  public final Map<String, SiPrefix> prefixRange;

  public static final Map<String, SiUnit> symbolMap;

  static {
    symbolMap = new HashMap<String, SiUnit>();
    for (SiUnit unit : SiUnit.values()) {
      symbolMap.put(unit.getSymbol(), unit);
    }
  }

  SiUnit(String symbol, int rangeStart, int rangeEnd) {
    this.symbol = symbol;
    this.rangeStart = rangeStart;
    this.rangeEnd = rangeEnd;
    this.range = new HashMap<>();
    this.prefixRange = new HashMap<>();
    for (int i = rangeStart; i <= rangeEnd; i += 3) {
      SiPrefix prefix = SiPrefix.fromExponent(i);
      String unitString = prefix.getAbbreviation() + symbol;
      this.range.put(i, unitString);
      this.prefixRange.put(unitString, prefix);
    }
  }

  private static final Logger LOG = LogManager.getLogger(SiUnit.class);

  public String toString() {
    return this.name().toLowerCase();
  }

  public String toUnitName() {
    return WordUtils.capitalize(this.toString());
  }

  public String getSymbol() {
    return symbol;
  }

  public Collection<String> getRange() {
    return range.values();
  }

  public String[] getRangeArray() {
    return (String[]) getRange().toArray();
  }

  public Map<String, SiPrefix> getRangeMap() {
    return new HashMap<>(prefixRange);
  }

  public static SiUnit fromSymbol(String symbol) {
    return symbolMap.get(symbol);
  }

  public static SiUnit fromSymbol(Character symbol) {
    return fromSymbol(String.valueOf(symbol));
  }

  /**
   * Integer exponent to prefix name/symbol. See e.g.
   * https://www.nist.gov/pml/weights-and-measures/metric-si-prefixes
   *
   * @param exponent The exponent
   * @param useSymbol If true, use prefix abbreviation, otherwise full name of prefix
   * @returns prefix name
   */
  private static String exponentToPrefixNameOrSymbol(int exponent, boolean useSymbol) {
    String ret = "";
    SiPrefix prefix = SiPrefix.fromExponent(exponent);
    if (prefix == null) {
      LOG.error("Don't know what the prefix would be for {}", exponent);
    } else {
      ret = useSymbol ? prefix.getAbbreviation() : prefix == SiPrefix.NONE ? "" : prefix.toString();
    }
    return ret;
  }

  public static String exponentToPrefix(int exponent) {
    return exponentToPrefixNameOrSymbol(exponent, false);
  }

  public static String exponentToPrefixSymbol(int exponent) {
    return exponentToPrefixNameOrSymbol(exponent, true);
  }

  /**
   * Get string containing unit symbol and prefix according to given value.
   *
   * <p>Example: If value is 1000.0 and unit is WATT, "kW" is returned.
   *
   * @param value Value to use in determining prefix.
   * @return String containing exponent prefix and unit symbol.
   */
  public String unitString(double value) {
    // TODO: get rid of cut'n'paste, make measureToString() use this
    int exponent = (int) Math.floor(Math.log10(value) / 3) * 3;
    double v = value / Math.pow(10, exponent);
    String prefixSymbol = exponentToPrefixSymbol(exponent);
    return prefixSymbol + symbol;
  }

  public static String measureToString(
      double value, int decimals, boolean withSymbol, SiUnit unit) {

    int exponent = (int) Math.floor(Math.log10(value) / 3) * 3;
    double v = value / Math.pow(10, exponent);
    String prefixNameOrSymbol = exponentToPrefixNameOrSymbol(exponent, withSymbol);

    LOG.debug(
        "measureToString({}, {}, {}, {}) v {} exponent {} name-or-symbol {}",
        value,
        decimals,
        withSymbol,
        unit,
        v,
        exponent,
        prefixNameOrSymbol);

    return String.format(
        "%." + decimals + "f %s%s",
        v,
        prefixNameOrSymbol,
        withSymbol ? unit.symbol : unit.toString() + "s"); // to do: handle case of exactly 1 & i18n
  }

  /* default is to use unit symbol */
  public static String measureToString(double value, int decimals, SiUnit unit) {
    return measureToString(value, decimals, true, unit);
  }

  public String measureToString(double value, int decimals, boolean withSymbol) {
    return measureToString(value, decimals, withSymbol, this);
  }

  public String measureToString(double value, int decimals) {
    return measureToString(value, decimals, this);
  }
}
