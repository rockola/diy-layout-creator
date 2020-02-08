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

package org.diylc.components;

import java.util.ArrayList;
import java.util.List;

public class PinCount {

  private int smallestPinNumber;
  private int largestPinNumber;
  private boolean evenPinsOnly;
  private final transient List<Integer> pinCounts = new ArrayList<>();
  private Integer value;

  public PinCount(int min, int max) {
    this(min, max, false);
  }

  public PinCount(int min, int max, boolean evenOnly) {
    this.evenPinsOnly = evenOnly;
    // ensure smallestPinNumber is not negative
    this.smallestPinNumber = Integer.max(0, min);
    if (evenOnly && this.smallestPinNumber % 2 != 0) {
      this.smallestPinNumber += 1;
    }
    // ensure largestPinNumber is not negative
    this.largestPinNumber = Integer.max(0, max);
    if (evenOnly && this.largestPinNumber % 2 != 0) {
      this.largestPinNumber += 1;
    }
    if (this.smallestPinNumber > this.largestPinNumber) {
      throw new RuntimeException(String.format(
          "Invalid %srange (%d, %d)",
          evenOnly ? "even only " : "",
          min,
          max));
    }
    for (int i = this.smallestPinNumber; i <= this.largestPinNumber; i += evenOnly ? 2 : 1) {
      pinCounts.add(i);
    }
  }

  /**
   * Get pin count.
   *
   * @return pin count, or null if not set.
   */
  public Integer pins() {
    return value;
  }

  /**
   * Set pin count.
   *
   * @param newValue New pin count to be set subject to constraints.
   * @return this PinCount if newValue was valid and the value was set, null otherwise
   */
  public PinCount setPins(int newValue) {
    if (newValue >= smallestPinNumber
        && newValue <= largestPinNumber
        && !evenPinsOnly || newValue % 2 == 0) {
      value = Integer.valueOf(newValue);
      return this;
    }
    return null;
  }

  public PinCount setPins(PinCount newPinCount) {
    return setPins(newPinCount.pins());
  }

  @Override
  public String toString() {
    return pins().toString();
  }

  public List<Integer> validPinCounts() {
    return pinCounts;
  }
}
