/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2020 held jointly by the individual authors.

  This file is part of DIYLC.

  DIYLC is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  DIYLC is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
  License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC. If not, see <http://www.gnu.org/licenses/>.
*/
package org.diylc.appframework.update;

import org.apache.logging.log4j.LogManager;

public enum ChangeType {
  BUG_FIX("Bug Fix"),
  NEW_FEATURE("New Feature"),
  IMPROVEMENT("Improvement");

  private String name;

  private ChangeType(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  public static ChangeType parseName(String name) {
    switch (name) {
      case "Bug Fix":
        return BUG_FIX;
      case "New Feature":
        return NEW_FEATURE;
      case "Improvement":
        return IMPROVEMENT;
      default:
        LogManager.getLogger(ChangeType.class).error("parseName({}) not a valid ChangeType", name);
        return null;
    }
  }
}
