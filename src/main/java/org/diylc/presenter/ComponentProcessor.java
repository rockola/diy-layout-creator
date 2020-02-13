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

package org.diylc.presenter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.common.PropertyWrapper;
import org.diylc.components.AbstractComponent;
import org.diylc.core.annotations.ComponentValue;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.MultiLineText;
import org.diylc.core.annotations.StringValue;
import org.diylc.core.measures.SiUnit;

/**
 * Utility class with component processing methods.
 *
 * @author Branislav Stojkovic
 */
public class ComponentProcessor {

  private static final Logger LOG = LogManager.getLogger(ComponentProcessor.class);

  private static Map<String, List<PropertyWrapper>> propertyCache;

  static {
    propertyCache = new HashMap<String, List<PropertyWrapper>>();
  }

  /**
   * Extracts all editable properties from the component class.
   *
   * @param clazz The class.
   * @return list of properties.
   */
  public static List<PropertyWrapper> extractProperties(Class clazz) {
    if (propertyCache.containsKey(clazz.getName())) {
      return cloneProperties(propertyCache.get(clazz.getName()));
    }
    List<PropertyWrapper> properties = new ArrayList<PropertyWrapper>();
    ComponentValue valueAnnotation = (ComponentValue) clazz.getAnnotation(ComponentValue.class);
    SiUnit valueType = valueAnnotation == null ? null : valueAnnotation.value();
    boolean stringValue = clazz.isAnnotationPresent(StringValue.class);
    for (Method getter : clazz.getMethods()) {
      if (getter.getName().startsWith("get")) {
        try {
          EditableProperty annotation = getter.getAnnotation(EditableProperty.class);
          if (annotation != null && !getter.isAnnotationPresent(Deprecated.class)) {
            /* We are interested in this method if it
               a) is named as a getter (starts with "get")
               b) has @EditableProperty annotation
               c) does not have @Deprecated annotation
            */
            String field = getter.getName().substring(3); // part after "get"
            String name = annotation.name().equals("") ? field : annotation.name();
            Method setter = clazz.getMethod("set" + field, getter.getReturnType());
            ComponentValue getterValueAnnotation = getter.getAnnotation(ComponentValue.class);
            SiUnit getterValueType =
                getterValueAnnotation == null ? null : getterValueAnnotation.value();
            /*
              @ComponentValue for class takes precedence over @ComponentValue
              for method if both are present and getter is getValue().
            */
            boolean isGetValue = name.equals("Value");
            /*
              If getter is getValue() and @StringValue for class is
              set to a non-empty string, use that as the field name.
            */
            if (isGetValue && stringValue) {
              String fieldName = ((StringValue) clazz.getAnnotation(StringValue.class)).value();
              if (!fieldName.equals("")) {
                name = fieldName;
              }
            }
            boolean multiLine =
                (isGetValue && stringValue && clazz.isAnnotationPresent(MultiLineText.class))
                    || getter.isAnnotationPresent(MultiLineText.class);
            PropertyWrapper property =
                new PropertyWrapper(
                    name,
                    getter.getReturnType(),
                    getter.getName(),
                    setter.getName(),
                    annotation,
                    isGetValue ? valueType : getterValueType,
                    isGetValue ? stringValue : false,
                    multiLine);
            properties.add(property);
          }
        } catch (NoSuchMethodException e) {
          LOG.debug("No matching setter found for {}, skipping", getter.getName());
        }
      }
    }

    propertyCache.put(clazz.getName(), properties);
    return cloneProperties(properties);
  }

  private static List<PropertyWrapper> cloneProperties(List<PropertyWrapper> properties) {
    List<PropertyWrapper> result = new ArrayList<PropertyWrapper>(properties.size());
    for (PropertyWrapper propertyWrapper : properties) {
      try {
        result.add((PropertyWrapper) propertyWrapper.clone());
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
    return result;
  }

  /**
   * Returns properties that have the same value for all the selected components.
   *
   * @param selectedComponents
   * @return
   */
  public static List<PropertyWrapper> getMutualSelectionProperties(
      Collection<AbstractComponent> selectedComponents) throws Exception {
    if (selectedComponents.isEmpty()) {
      return null;
    }
    List<PropertyWrapper> properties = new ArrayList<PropertyWrapper>();
    List<AbstractComponent> selectedList = new ArrayList<AbstractComponent>(selectedComponents);
    AbstractComponent firstComponent = selectedList.get(0);

    properties.addAll(extractProperties(firstComponent.getClass()));
    // Initialize values
    for (PropertyWrapper property : properties) {
      property.readFrom(firstComponent);
    }
    for (int i = 1; i < selectedComponents.size(); i++) {
      AbstractComponent component = selectedList.get(i);
      List<PropertyWrapper> newProperties = extractProperties(component.getClass());
      for (PropertyWrapper property : newProperties) {
        property.readFrom(component);
      }
      properties.retainAll(newProperties);
      // Try to find matching properties in old and new lists and see if
      // their values match.
      for (PropertyWrapper oldProperty : properties) {
        if (newProperties.contains(oldProperty)) {
          PropertyWrapper newProperty = newProperties.get(newProperties.indexOf(oldProperty));
          if (((newProperty.getValue() != null && oldProperty.getValue() != null)
                  && !newProperty.getValue().equals(oldProperty.getValue()))
              || newProperty.getValue() != oldProperty.getValue()) {
            // Values don't match, so the property is not unique valued.
            oldProperty.setUnique(false);
          }
        }
      }
    }
    Collections.sort(properties, ComparatorFactory.getInstance().getDefaultPropertyComparator());
    return properties;
  }
}
