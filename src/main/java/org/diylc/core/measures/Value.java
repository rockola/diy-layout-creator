package org.diylc.core.measures;

import java.io.Serializable;

public class Value implements Serializable, Cloneable {
  private static final long serialVersionUID = 1L;

  private Double value;
  private final SiUnit unit;

  public Value(double value, SiUnit unit) {
    super();
    this.value = Double.valueOf(value);
    this.unit = unit;
  }

  public double getValue() {
    return value.doubleValue();
  }

  public SiUnit getUnit() {
    return unit;
  }

  public boolean hasUnit(SiUnit unit) {
    return getUnit().equals(unit);
  }

  public String getUnitString() {
    return unit.unitString(getValue());
  }

  public static Value parse(String valueWithUnit) {
    char unitSymbol = valueWithUnit.charAt(valueWithUnit.length() - 1);
    if (unitSymbol == 'R' || unitSymbol == 'r') {
      unitSymbol = SiUnit.OHM.getSymbol().charAt(0);
    }
    SiUnit unit = SiUnit.fromSymbol(unitSymbol);
    if (unit == null) {
      throw new RuntimeException("Could not parse '" + valueWithUnit + "'");
    }
    char prefix = Character.valueOf(valueWithUnit.charAt(valueWithUnit.length() - 2));
    if (prefix == ' ' || Character.isDigit(prefix)) {
      return new Value(
          Double.valueOf(valueWithUnit.substring(0, valueWithUnit.length() - 2)), unit);
    } else {
      SiPrefix siPrefix = SiPrefix.fromAbbreviation(prefix);
      return new Value(
          Double.valueOf(valueWithUnit.substring(0, valueWithUnit.length() - 3))
              * siPrefix.getExponent(),
          unit);
    }
  }

  public static Value parse(Double value, String unitWithPrefix) {
    if (unitWithPrefix != null && unitWithPrefix.length() == 2) {
      SiPrefix prefix = SiPrefix.fromAbbreviation(unitWithPrefix.charAt(0));
      if (prefix != null) {
        SiUnit unit = SiUnit.fromSymbol(unitWithPrefix.charAt(1));
        if (unit != null) {
          return new Value(value * prefix.getExponent(), unit);
        }
      }
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((unit == null) ? 0 : unit.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this != obj) {
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      Value other = (Value) obj;
      if (!unit.equals(other.unit) || !value.equals(other.value)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return unit.measureToString(value, 4);
  }
}
