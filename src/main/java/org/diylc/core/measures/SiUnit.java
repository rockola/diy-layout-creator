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

// import java.io.Serializable;
// import java.text.DecimalFormat;
// import java.text.Format;

import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum SiUnit {
  AMPERE("A"), // current
  FARAD("F"), // capacitance
  HENRY("H"), // inductance
  METER("m"), // length
  OHM("Ω"), // resistance
  VOLT("V"), // voltage
  WATT("W"); // power

  public final String symbol;

  SiUnit(String symbol) {
    this.symbol = symbol;
  }

  private static final Logger LOG = LogManager.getLogger(SiUnit.class);

  public String toString() {
    return this.name().toLowerCase();
  }

  public String toUnitName() {
    return WordUtils.capitalize(this.toString());
  }

  /**
   * Integer exponent to prefix name/symbol. See e.g.
   * https://www.nist.gov/pml/weights-and-measures/metric-si-prefixes
   */
  private static String exponentToPrefixNameOrSymbol(int exponent, boolean useSymbol) {
    switch (exponent) {
      case -12:
        return useSymbol ? "p" : "pico";
      case -9:
        return useSymbol ? "n" : "nano";
      case -6:
        return useSymbol ? "µ" : "micro"; // hooray for UTF-8!
      case -3:
        return useSymbol ? "m" : "milli";
      case 0:
        return "";
      case 3:
        return useSymbol ? "k" : "kilo";
      case 6:
        return useSymbol ? "M" : "mega";
      case 9:
        return useSymbol ? "G" : "giga";
      case 12:
        return useSymbol ? "T" : "tera";
      case 15:
        return useSymbol ? "P" : "peta";
      case 18:
        return useSymbol ? "E" : "exa";
      default:
        LOG.error("Don't know what the prefix would be for {}", exponent);
        return "";
    }
  }

  public static String exponentToPrefix(int exponent) {
    return exponentToPrefixNameOrSymbol(exponent, false);
  }

  public static String exponentToPrefixSymbol(int exponent) {
    return exponentToPrefixNameOrSymbol(exponent, true);
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
