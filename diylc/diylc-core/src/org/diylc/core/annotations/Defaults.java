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
package org.diylc.core.annotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Defaults {
  private Defaults() {}

  private static final Map<Class<?>, Object> DEFAULTS;

  static {
    // Only add to this map via put(Map, Class<T>, T)
    Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();
    put(map, boolean.class, false);
    put(map, Boolean.class, false);
    put(map, char.class, '\0');
    put(map, byte.class, (byte) 0);
    put(map, Byte.class, (byte) 0);
    put(map, short.class, (short) 0);
    put(map, Short.class, (short) 0);
    put(map, int.class, 0);
    put(map, Integer.class, 0);
    put(map, long.class, 0L);
    put(map, Long.class, 0L);
    put(map, float.class, 0f);
    put(map, Float.class, 0f);
    put(map, double.class, 0d);
    put(map, Double.class, 0d);
    DEFAULTS = Collections.unmodifiableMap(map);
  }

  private static <T> void put(Map<Class<?>, Object> map, Class<T> type, T value) {
    map.put(type, value);
  }

  /**
   * Returns the default value of {@code type} as defined by JLS --- {@code 0} for numbers,
   * {@code false} for {@code boolean} and {@code '\0'} for {@code char}. For non-primitive types
   * and {@code void}, {@code null} is returned.
   * 
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public static <T> T defaultValue(Class<T> type) throws Exception {
    // Primitives.wrap(type).cast(...) would avoid the warning, but we can't use that from here
    // the put method enforces this key-value relationship
    T t = (T) DEFAULTS.get(type);
    if (t == null) {
      return (T) type.getConstructors()[0].newInstance();
    }
    return t;
  }
}
