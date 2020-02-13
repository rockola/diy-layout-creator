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

package org.diylc.components.passive;

import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.annotations.ComponentValue;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.SiUnit;
import org.diylc.core.measures.Value;

public abstract class AbstractCapacitor extends AbstractLeadedComponent {

  protected boolean polarized = false;

  private Value voltage;

  public AbstractCapacitor() {
    super();
  }

  @ComponentValue(SiUnit.VOLT)
  @EditableProperty
  public Value getVoltage() {
    return voltage;
  }

  public void setVoltage(Value voltage) {
    if (voltage == null || voltage.getUnit() == SiUnit.VOLT) {
      this.voltage = voltage;
    }
  }

  @Override
  public String getValueForDisplay() {
    return getValue().toString() + (getVoltage() == null ? "" : " " + getVoltage().toString());
  }
}
