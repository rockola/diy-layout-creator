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

package org.diylc.appframework.miscutils;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class that injects the provided properties into class static fields. To illustrate it,
 * consider the following example: <br>
 * <code>
 * com.bancika.ClassA.INT_COST=15
 * com.bancika.ClassB.STR_CONST=Test
 * </code> <br>
 * After {@link #injectProperties(Properties)} is executed, static field INT_COST in the
 * com.bancika.ClassA class will be populated with int value of 15. Similar for the other property
 * line.<br>
 * Currently it supports Boolean, Integer, Double and Color together with their primitives.
 *
 * @author Branislav Stojkovic
 */
public class PropertyInjector {

  private static final Logger LOG = LogManager.getLogger(PropertyInjector.class);

  private PropertyInjector() {}

  /**
   * Injects properties from the provided {@link Properties} object.
   *
   * @param properties Properties to be injected.
   */
  public static void injectProperties(Properties properties) {
    for (Entry<Object, Object> entry : properties.entrySet()) {
      String key = entry.getKey().toString();
      String value = entry.getValue().toString();
      try {
        String className = key.substring(0, key.lastIndexOf('.'));
        String fieldName = key.substring(key.lastIndexOf('.') + 1);
        Class<?> clazz = Class.forName(className);
        try {
          LOG.info("Injecting " + key + " = " + value);
          Field field = clazz.getField(fieldName);
          field.setAccessible(true);
          Class<?> fieldType = field.getType();
          if (String.class.isAssignableFrom(fieldType)) {
            field.set(null, value);
          } else if (Integer.class.isAssignableFrom(fieldType)
              || int.class.isAssignableFrom(fieldType)) {
            int intValue = Integer.parseInt(value);
            field.set(null, intValue);
          } else if (Double.class.isAssignableFrom(fieldType)
              || double.class.isAssignableFrom(fieldType)) {
            double doubleValue = Double.parseDouble(value);
            field.set(null, doubleValue);
          } else if (Boolean.class.isAssignableFrom(fieldType)
              || boolean.class.isAssignableFrom(fieldType)) {
            boolean booleanValue = Boolean.parseBoolean(value);
            field.set(null, booleanValue);
          } else if (Color.class.isAssignableFrom(fieldType)) {
            Color color = Color.decode(value);
            field.set(null, color);
          } else {
            LOG.warn("Property type not supported.");
          }
        } catch (SecurityException e) {
          LOG.warn("Could not inject {}. Field access denied: {} ", key, fieldName);
        } catch (NoSuchFieldException e) {
          LOG.warn("Could not inject {}. Field not found: {}", key, fieldName);
        } catch (IllegalArgumentException e) {
          LOG.warn("Could not inject {}. Illegal access: {}", key, fieldName);
        } catch (IllegalAccessException e) {
          LOG.warn("Could not inject {}. Field illegal access: ", key, fieldName);
        }
      } catch (StringIndexOutOfBoundsException e) {
        LOG.warn("Property name does not match format ClassName.FIELD_NAME: {}", key);
      } catch (ClassNotFoundException e) {
        LOG.warn("Could not inject {}. Class not found.", key);
      }
    }
  }
}
