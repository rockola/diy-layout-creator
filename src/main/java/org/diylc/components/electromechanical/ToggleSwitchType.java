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

package org.diylc.components.electromechanical;

public enum ToggleSwitchType {
  SPST(1, 1),
  SPDT(1, 2),
  DPDT(2, 2),
  _DP3T_mustang(2, 3),
  _3PDT(3, 2),
  _4PDT(4, 2),
  _5PDT(5, 2),
  SPDT_off(1, 2),
  DPDT_off(2, 2),
  _3PDT_off(3, 2),
  _4PDT_off(4, 2),
  _5PDT_off(5, 2);

  private int switchPoles;
  private int switchThrows;

  ToggleSwitchType(int switchPoles, int switchThrows) {
    this.switchPoles = switchPoles;
    this.switchThrows = switchThrows;
  }

  @Override
  public String toString() {
    String name = name();
    if (name.startsWith("_")) {
      name = name.substring(1);
    }
    name = name.replace("_", " ");
    name = name.replace("mustang", "");
    name = name.replace("off", " (Center OFF)");
    return name;
  }

  public int getPoles() {
    return this.switchPoles;
  }

  public int getThrows() {
    return this.switchThrows;
  }
}
