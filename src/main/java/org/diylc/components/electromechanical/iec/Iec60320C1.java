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

package org.diylc.components.electromechanical.iec;

import org.diylc.components.Area;
import org.diylc.components.electromechanical.Pin;
import org.diylc.core.annotations.Variant;
import org.diylc.core.measures.Size;

/**
 * IEC60320 C1
 *
 * <p>0.2A 250V connector for class II equipment and cold conditions.
 */
public class Iec60320C1 extends Iec60320 {

  private static final long serialVersionUID = 1L;

  public Iec60320C1() {
    super(Iec60320.CouplerType.CONNECTOR);
    final Size pinHorizontalSpacing = Size.mm(6.6);
    final Size pinWidth = Size.mm(2.9);
    pins.add(Pin.circular(pinHorizontalSpacing.half().negative(), pinWidth));
    pins.add(Pin.circular(pinHorizontalSpacing.half(), pinWidth));
    final Size inletHeight = Size.mm(8.2);
    final Size inletWidth = Size.mm(14.5);
    final Size courtyardWidth = Size.mm(18.5);
    final Size courtyardHeight = Size.mm(13);
    final Size courtyardCornerRadius = Size.mm(0.5);
    coupler = Area.centeredRoundRect(reference, inletWidth, inletHeight, inletHeight.half());
    courtyard =
        Area.centeredRoundRect(reference, courtyardWidth, courtyardHeight, courtyardCornerRadius);
  }

  @Variant
  public static IecSocket variant() {
    return new Iec60320C1();
  }
}
