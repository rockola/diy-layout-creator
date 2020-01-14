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

import org.diylc.DIYLC;

public enum ChangeType {
  BUG_FIX,
  NEW_FEATURE,
  IMPROVEMENT;

  /**
     Human readable string. Can be translated in resources.
  */
  public String theString() {
    return DIYLC.getString("message.update." + this.toString().replace("_", "-"));
  }

  public static ChangeType parse(String s) {
    for (ChangeType changeType : ChangeType.values()) {
      if (changeType.theString().equals(s)) {
        return changeType;
      }
    }
    LogManager.getLogger(ChangeType.class).error("parse({}) not a valid ChangeType", s);
    return null;
  }
}
