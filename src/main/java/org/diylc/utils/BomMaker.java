/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.diylc.common.ComponentType;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.presenter.ComponentProcessor;

public class BomMaker {

  private static BomMaker instance;

  public static BomMaker getInstance() {
    if (instance == null) {
      instance = new BomMaker();
    }
    return instance;
  }

  private BomMaker() {}

  public List<BomEntry> createBom(List<IDIYComponent<?>> components) {
    Map<String, BomEntry> entryMap = new LinkedHashMap<String, BomEntry>();
    List<IDIYComponent<?>> sortedComponents = new ArrayList<IDIYComponent<?>>(components);
    Collections.sort(sortedComponents, new Comparator<IDIYComponent<?>>() {

      @Override
      public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
        String name1 = o1.getName();
        String name2 = o2.getName();
        Pattern p = Pattern.compile("(\\D+)(\\d+)");
        Matcher m1 = p.matcher(name1);
        Matcher m2 = p.matcher(name2);
        if (m1.matches() && m2.matches()) {
          String prefix1 = m1.group(1);
          int value1 = Integer.parseInt(m1.group(2));
          String prefix2 = m2.group(1);
          int value2 = Integer.parseInt(m2.group(2));
          int compare = prefix1.compareToIgnoreCase(prefix2);
          if (compare != 0) {
            return compare;
          }
          return new Integer(value1).compareTo(value2);
        }
        return name1.compareToIgnoreCase(name2);
      }
    });
    for (IDIYComponent<?> component : sortedComponents) {
      @SuppressWarnings("unchecked")
      ComponentType type =
          ComponentProcessor.getInstance().extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());
      if (type.getBomPolicy() == BomPolicy.NEVER_SHOW)
        continue;
      String name = component.getName();
      String value;
      try {
        value = component.getValueForDisplay();
      } catch (Exception e) {
        value = "<undefined>";
      }
      if ((name != null) && (value != null)) {
        String key = type.getName() + "|" + value;
        if (entryMap.containsKey(key)) {
          BomEntry entry = entryMap.get(key);
          entry.setQuantity(entry.getQuantity() + 1);
          if (type.getBomPolicy() == BomPolicy.SHOW_ALL_NAMES) {
            entry.setName(entry.getName() + ", " + name);
          }
        } else {
          entryMap.put(key,
              new BomEntry(type.getName(), type.getBomPolicy() == BomPolicy.SHOW_ALL_NAMES ? name : type.getName(),
                  value, 1));
        }
      }
    }
    List<BomEntry> bom = new ArrayList<BomEntry>(entryMap.values());
    return bom;
  }
}
