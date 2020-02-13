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

package org.diylc.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.core.IPropertyValidator;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.SiUnit;

/**
 * Entity class for editable properties extracted from component objects. Represents a single
 * editable property together with it's current value.
 *
 * @author Branislav Stojkovic
 */
public class PropertyWrapper implements Cloneable {

  private static final Logger LOG = LogManager.getLogger(ComponentType.class);

  private static Map<String, IPropertyValidator> propertyValidatorCache;

  private String name;
  private Class type;
  private Object value;
  private String setter;
  private String getter;
  private boolean defaultable;
  private EditableProperty annotation;
  private IPropertyValidator validator;
  private boolean unique = true;
  private boolean changed = false;
  private int sortOrder;
  private Object ownerObject;
  /**
   * If not null, the type of the associated component value (SiUnit.FARAD, etc.).
   *
   * <p>Set with class annotation @ComponentValue.
   */
  private SiUnit valueType;
  /**
   * If true, the value of this component is a String.
   *
   * <p>Set with class annotation @StringValue. stringValue is ignored if valueType is not null.
   */
  private boolean stringValue = false;
  /** If true and this property has a String value, the editor should be multi-line. */
  private boolean multiLine = false;

  static {
    propertyValidatorCache = new HashMap<String, IPropertyValidator>();
  }

  public PropertyWrapper(
      String name,
      Class type,
      String getter,
      String setter,
      EditableProperty annotation,
      SiUnit valueType,
      boolean stringValue,
      boolean multiLine) {
    super();
    this.name = name;
    this.type = type;
    this.getter = getter;
    this.setter = setter;
    this.annotation = annotation;
    if (annotation != null) {
      this.defaultable = annotation.defaultable();
      this.sortOrder = annotation.sortOrder();
      this.validator = getPropertyValidator(annotation.validatorClass());
    }
    this.valueType = valueType;
    this.stringValue = stringValue;
    this.multiLine = multiLine;
    this.ownerObject = null;
  }

  public PropertyWrapper(
      String name, Class type, String getter, String setter, EditableProperty annotation) {
    this(name, type, getter, setter, annotation, null, false, false);
  }

  private static IPropertyValidator getPropertyValidator(
      Class<? extends IPropertyValidator> clazz) {
    if (propertyValidatorCache.containsKey(clazz.getName())) {
      return propertyValidatorCache.get(clazz.getName());
    }
    IPropertyValidator validator;
    try {
      validator = clazz.newInstance();
    } catch (Exception e) {
      LOG.error("Could not instantiate validator for " + clazz.getName(), e);
      // TODO throw exception? if instantiation fails, null is returned,
      // but is this really the correct behaviour?
      return null;
    }
    propertyValidatorCache.put(clazz.getName(), validator);
    return validator;
  }

  public void readFrom(Object object)
      throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
          SecurityException, NoSuchMethodException {
    this.ownerObject = object;
    this.value = getGetter().invoke(object);
  }

  public void writeTo(Object object)
      throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
          SecurityException, NoSuchMethodException {
    object.getClass().getMethod(setter, type).invoke(object, this.value);
  }

  public Method getGetter() throws SecurityException, NoSuchMethodException {
    return getOwnerObject().getClass().getMethod(getter);
  }

  public String getName() {
    return name;
  }

  public Class<?> getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public SiUnit getValueType() {
    return valueType;
  }

  public boolean isStringValue() {
    return stringValue;
  }

  public boolean isMultiLine() {
    return multiLine;
  }

  public Object getOwnerObject() {
    return ownerObject;
  }

  public EditableProperty getAnnotation() {
    return annotation;
  }

  public boolean isDefaultable() {
    return defaultable;
  }

  public IPropertyValidator getValidator() {
    return validator;
  }

  public boolean isUnique() {
    return unique;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public boolean isChanged() {
    return changed;
  }

  public void setChanged(boolean changed) {
    this.changed = changed;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  // @Override
  // public Object clone() throws CloneNotSupportedException {
  // // Try to invoke clone method on value if possible.
  // try {
  // Method cloneMethod = value.getClass().getMethod("clone");
  // return new Property(name, type, cloneMethod.invoke(value));
  // } catch (Exception e) {
  // }
  // return new Property(name, type, value);
  // }
  /*
  @Override
  public PropertyWrapper clone() throws CloneNotSupportedException {
    PropertyWrapper c = (PropertyWrapper) super.clone();
    c.name = this.name;
    c.type = this.type;
    c.value = this.value.clone();
    c.setter = this.setter;
    c.getter = this.getter;
    c.defaultable = this.defaultable;
    c.validator = this.validator;
    c.unique = this.unique;
    c.changed = this.changed;
    c.sortOrder = this.sortOrder;
    c.ownerObject = this.ownerObject;
    return c;
  }
  */

  public Object clone() throws CloneNotSupportedException {
    super.clone();
    PropertyWrapper clone =
        new PropertyWrapper(
            this.name,
            this.type,
            this.getter,
            this.setter,
            this.annotation,
            this.valueType,
            this.stringValue,
            this.multiLine);
    clone.value = this.value; // NOTE: not copied! //ola 20100110
    clone.changed = this.changed;
    clone.unique = this.unique;
    return clone;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (defaultable ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((setter == null) ? 0 : setter.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PropertyWrapper other = (PropertyWrapper) obj;
    if (defaultable != other.defaultable) {
      return false;
    }
    if (getter == null) {
      if (other.getter != null) {
        return false;
      }
    } else if (!getter.equals(other.getter)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (setter == null) {
      if (other.setter != null) {
        return false;
      }
    } else if (!setter.equals(other.setter)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return name + " = " + value;
  }
}
