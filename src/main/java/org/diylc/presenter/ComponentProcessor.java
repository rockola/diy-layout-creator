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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.common.ComponentType;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IPropertyValidator;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;

/**
 * Utility class with component processing methods.
 *
 * @author Branislav Stojkovic
 */
public class ComponentProcessor {

  private static final Logger LOG = LogManager.getLogger(ComponentProcessor.class);

  private static Map<String, List<PropertyWrapper>> propertyCache;
  private static Map<String, IPropertyValidator> propertyValidatorCache;
  private static Map<String, ComponentType> componentTypeMap;
  private static Map<String, IComponentTransformer> componentTransformerMap;

  static {
    propertyCache = new HashMap<String, List<PropertyWrapper>>();
    componentTypeMap = new HashMap<String, ComponentType>();
    propertyValidatorCache = new HashMap<String, IPropertyValidator>();
    componentTransformerMap = new HashMap<String, IComponentTransformer>();
  }

  /**
     Extract component type from class.
     Class should implement IDIYComponent or be a subclass of one that does.
     Also, class should have a ComponentDescriptor annotation.

     NOTE: Could - and indeed should - this be done at compile time?
     Runtime handling is of course required if components are to be
     loaded from external JARs.

     @param clazz The class.
     @returns component type
   */
  public static ComponentType extractComponentTypeFrom(
      Class<? extends IDIYComponent<?>> clazz) {

    /* have we already seen this class? if so, get it from cache */
    if (componentTypeMap.containsKey(clazz.getName())) {
      return componentTypeMap.get(clazz.getName());
    }
    /* does this class have the required annotation? can't do much if not */
    if (!clazz.isAnnotationPresent(ComponentDescriptor.class)) {
      return null;
    }
    /* good to go, let's look at the annotation */
    ComponentDescriptor annotation = clazz.getAnnotation(ComponentDescriptor.class);
    String name = annotation.name();
    String description = annotation.description();
    CreationMethod creationMethod = annotation.creationMethod();
    String category = annotation.category();
    String namePrefix = annotation.instanceNamePrefix();
    String author = annotation.author();
    Icon icon = null;
    double zOrder = annotation.zOrder();
    boolean flexibleZOrder = annotation.flexibleZOrder();
    BomPolicy bomPolicy = annotation.bomPolicy();
    boolean autoEdit = annotation.autoEdit();
    IComponentTransformer transformer = getComponentTransformer(annotation.transformer());
    KeywordPolicy keywordPolicy = annotation.keywordPolicy();
    String keywordTag = annotation.keywordTag();

    try {
      // Draw component icon for later use
      IDIYComponent<?> componentInstance = (IDIYComponent<?>) clazz.newInstance();
      Image image = new BufferedImage(Presenter.ICON_SIZE,
                                      Presenter.ICON_SIZE,
                                      java.awt.image.BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = (Graphics2D) image.getGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      componentInstance.drawIcon(g2d, Presenter.ICON_SIZE, Presenter.ICON_SIZE);
      icon = new ImageIcon(image);
    } catch (Exception e) {
      LOG.error("Error drawing component icon for " + clazz.getName(), e);
    }
    ComponentType componentType = new ComponentType(
        name,
        description,
        creationMethod,
        category,
        namePrefix,
        author,
        icon,
        clazz,
        zOrder,
        flexibleZOrder,
        bomPolicy,
        autoEdit,
        transformer,
        keywordPolicy,
        keywordTag);
    componentTypeMap.put(clazz.getName(), componentType);
    return componentType;
  }

  /**
   * Extracts all editable properties from the component class.
   *
   * @param clazz The class.
   * @return list of properties.
   */
  public static List<PropertyWrapper> extractProperties(Class<?> clazz) {
    if (propertyCache.containsKey(clazz.getName())) {
      return cloneProperties(propertyCache.get(clazz.getName()));
    }
    List<PropertyWrapper> properties = new ArrayList<PropertyWrapper>();
    for (Method getter : clazz.getMethods()) {
      if (getter.getName().startsWith("get")) {
        try {
          EditableProperty annotation = getter.getAnnotation(EditableProperty.class);
          if (annotation != null
              && !getter.isAnnotationPresent(Deprecated.class)) {
            /* We are interested in this method if it
               a) is named as a getter (starts with "get")
               b) has @EditableProperty annotation
               c) does not have @Deprecated annotation
            */
            String field = getter.getName().substring(3); // part after "get"
            String name = annotation.name().equals("") ? field : annotation.name();
            IPropertyValidator validator = getPropertyValidator(annotation.validatorClass());
            Method setter = clazz.getMethod("set" + field, getter.getReturnType());
            PropertyWrapper property =
                new PropertyWrapper(
                    name,
                    getter.getReturnType(),
                    getter.getName(),
                    setter.getName(),
                    annotation.defaultable(),
                    validator,
                    annotation.sortOrder());
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
      Collection<IDIYComponent<?>> selectedComponents) throws Exception {
    if (selectedComponents.isEmpty()) {
      return null;
    }
    List<PropertyWrapper> properties = new ArrayList<PropertyWrapper>();
    List<IDIYComponent<?>> selectedList = new ArrayList<IDIYComponent<?>>(selectedComponents);
    IDIYComponent<?> firstComponent = selectedList.get(0);

    properties.addAll(extractProperties(firstComponent.getClass()));
    // Initialize values
    for (PropertyWrapper property : properties) {
      property.readFrom(firstComponent);
    }
    for (int i = 1; i < selectedComponents.size(); i++) {
      IDIYComponent<?> component = selectedList.get(i);
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

  private static IComponentTransformer getComponentTransformer(
      Class<? extends IComponentTransformer> clazz) {
    IComponentTransformer transformer = null;
    if (clazz != null) {
      transformer = componentTransformerMap.get(clazz.getName());
      if (transformer == null) {
        try {
          transformer = clazz.newInstance();
          componentTransformerMap.put(clazz.getName(), transformer);
        } catch (Exception e) {
          LOG.error("Could not instantiate validator for " + clazz.getName(), e);
          // TODO throw exception? if instantiation fails, null is returned,
          // but is this really the correct behaviour?
        }
      }
    }
    return transformer;
  }
}
