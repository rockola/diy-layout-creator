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

package org.diylc.components.smd;

import java.awt.Color;
import org.diylc.components.AbstractComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.ComponentValue;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.SiUnit;

@ComponentValue(SiUnit.FARAD)
@ComponentDescriptor(
    name = "SMD Capacitor",
    author = "Branislav Stojkovic",
    category = "SMD",
    instanceNamePrefix = "C",
    description = "Surface mount capacitor",
    zOrder = AbstractComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_VALUE)
public class SurfaceMountCapacitor extends PassiveSurfaceMountComponent {

  private static final long serialVersionUID = 1L;

  public static final Color BODY_COLOR = Color.decode("#BD9347");
  public static final Color BORDER_COLOR = BODY_COLOR.darker();

  public SurfaceMountCapacitor() {
    super(SiUnit.FARAD, BODY_COLOR, BORDER_COLOR);
  }
}
