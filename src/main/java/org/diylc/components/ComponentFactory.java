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
  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
  License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.diylc.core.IDIYComponent;
import org.diylc.parsing.XmlNode;

public class ComponentFactory {

  private ComponentFactory() {}

  public static List<IDIYComponent<?>> makeComponents(Map<String, Collection<XmlNode>> specs) {
    List<IDIYComponent<?>> components = new ArrayList<>();

    specs.entrySet().stream()
        .forEach(
            e -> {
              String key = e.getKey();
              // TODO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            });

    return components;
  }
}
