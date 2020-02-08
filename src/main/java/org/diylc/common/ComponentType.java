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

package org.diylc.common;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Icon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.appframework.Serializer;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IPlugInPort;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Template;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.Unmarshaller;
import org.diylc.core.annotations.Unmarshallers;
import org.diylc.parsing.XmlNode;
import org.diylc.parsing.XmlReader;

/**
 * Entity class used to describe a component type.
 *
 * @author Branislav Stojkovic
 * @see IDIYComponent
 */
public class ComponentType {

  private static final Logger LOG = LogManager.getLogger(ComponentType.class);
  private static final Map<String, List<ComponentType>> componentTypes;
  private static final Map<String, Class<? extends IDIYComponent<?>>> xmlReaders;
  private static final Map<String, Map<String, ComponentType>> unmarshalledTypes;

  private static volatile Map<String, List<Template>> defaultVariantMap;
  private static volatile Map<String, ComponentType> componentTypeMap;
  private static volatile Map<String, IComponentTransformer> componentTransformerMap;
  private static volatile int typeOrdinal;

  static {
    defaultVariantMap = new TreeMap<String, List<Template>>(String.CASE_INSENSITIVE_ORDER);
    componentTypeMap = new HashMap<String, ComponentType>();
    componentTransformerMap = new HashMap<String, IComponentTransformer>();
    unmarshalledTypes = new HashMap<String, Map<String, ComponentType>>();
    xmlReaders = new HashMap<String, Class<? extends IDIYComponent<?>>>();
    componentTypes = loadComponentTypes();
    LOG.trace("unmarshalledTypes is {}", unmarshalledTypes == null ? "NULL" : "not null");
    typeOrdinal = 1;
  }

  private String name;
  private String description;
  private CreationMethod creationMethod;
  private String category;
  private String namePrefix;
  private String author;
  private Icon icon;
  private Class<? extends IDIYComponent<?>> instanceClass;
  private double zOrder;
  private boolean flexibleZOrder;
  private BomPolicy bomPolicy;
  private boolean autoEdit;
  private String xmlTag;
  private IComponentTransformer transformer;
  private KeywordPolicy keywordPolicy;
  private String keywordTag;

  private List<Template> variants = new ArrayList<>();
  private String searchKey;

  public ComponentType(
      String name,
      String description,
      CreationMethod creationMethod,
      String category,
      String namePrefix,
      String author,
      Icon icon,
      Class<? extends IDIYComponent<?>> instanceClass,
      double zOrder,
      boolean flexibleZOrder,
      BomPolicy bomPolicy,
      boolean autoEdit,
      String xmlTag,
      IComponentTransformer transformer,
      KeywordPolicy keywordPolicy,
      String keywordTag) {
    super();
    this.name = name;
    this.description = description;
    this.creationMethod = creationMethod;
    this.category = category;
    this.namePrefix = namePrefix;
    this.author = author;
    this.icon = icon;
    this.instanceClass = instanceClass;
    this.zOrder = zOrder;
    this.flexibleZOrder = flexibleZOrder;
    this.bomPolicy = bomPolicy;
    this.autoEdit = autoEdit;
    this.xmlTag = xmlTag;
    this.transformer = transformer;
    this.keywordPolicy = keywordPolicy;
    this.keywordTag = keywordTag;

    if (xmlTag != null) {
      String[] tagParts = xmlTag.split(":");
      if (tagParts.length > 0) {
        String tag = tagParts[0];
        String type = tagParts.length > 1 ? tagParts[1] : "";
        Map<String, ComponentType> typeMap = unmarshalledTypes.get(tag);
        if (typeMap == null) {
          typeMap = new HashMap<String, ComponentType>();
          unmarshalledTypes.put(tag, typeMap);
        }
        typeMap.put(type, this);
      }
    }

    searchKey = String.format("%s/%s/%s",
                              name.toLowerCase(),
                              description.toLowerCase(),
                              category.toLowerCase());
    LOG.trace("ComponentType({}, ...) is {}th type", name, getTypeOrdinal());
  }

  public ComponentType(
      String name,
      String description,
      String category) {
    this(
        name,
        description,
        CreationMethod.SINGLE_CLICK,
        category,
        "",
        "",
        null,
        null,
        0,
        false,
        null,
        false,
        XmlNode.NO_TAG,
        null,
        KeywordPolicy.NEVER_SHOW,
        null);
  }

  private static int getTypeOrdinal() {
    return typeOrdinal++;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public CreationMethod getCreationMethod() {
    return creationMethod;
  }

  public String getCategory() {
    return category;
  }

  public String getNamePrefix() {
    return namePrefix;
  }

  public String getAuthor() {
    return author;
  }

  public Icon getIcon() {
    return icon;
  }

  public Class<? extends IDIYComponent<?>> getInstanceClass() {
    return instanceClass;
  }

  public double getZOrder() {
    return zOrder;
  }

  public boolean isFlexibleZOrder() {
    return flexibleZOrder;
  }

  public BomPolicy getBomPolicy() {
    return bomPolicy;
  }

  public boolean isAutoEdit() {
    return autoEdit;
  }

  public IComponentTransformer getTransformer() {
    return transformer;
  }

  public KeywordPolicy getKeywordPolicy() {
    return keywordPolicy;
  }

  public String getKeywordTag() {
    return keywordTag;
  }

  public List<Template> getVariants() {
    if (variants.isEmpty()) {
      LOG.trace("getVariants() {} Getting variant map from {}",
                getName(), Config.Flag.TEMPLATES);
      // Variants have not been cached yet for this type, let's do it
      // First try by class name and then by old category.type format
      List<String> keys = new ArrayList<String>();
      keys.add(getInstanceClass().getCanonicalName());
      keys.add(getCategory() + "." + getName());

      Map<String, List<Template>> variantMap =
          (Map<String, List<Template>>) ConfigurationManager.getObject(Config.Flag.TEMPLATES);
      if (variantMap != null) {
        List<Template> userVariants = null;
        for (String key : keys) {
          userVariants = variantMap.get(key);
          if (userVariants != null && !userVariants.isEmpty()) {
            variants.addAll(userVariants);
          }
        }
      }
      if (defaultVariantMap != null) {
        List<Template> defaultVariants = null;
        for (String key : keys) {
          defaultVariants = defaultVariantMap.get(key);
          if (defaultVariants != null && !defaultVariants.isEmpty()) {
            variants.addAll(defaultVariants);
          }
        }
      }
      Collections.sort(
          variants,
          new Comparator<Template>() {

            @Override
            public int compare(Template o1, Template o2) {
              return o1.getName().compareTo(o2.getName());
            }
          });
    } else {
      LOG.trace("getVariants() {} Getting variant map from cache", getName());
    }

    return variants;
  }

  public int howManyVariants() {
    return getVariants().size();
  }

  /**
     True if this component type matches given key.

     <p>Type matches key if name, description, or category contains
     key. Key must already be in lowercase.

     @param key Search string
   */
  public boolean isOfInterest(String key) {
    return searchKey.contains(key);
  }

  public String getSearchKey() {
    return searchKey;
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * Returns all available {@link ComponentType}s classified by
   * category. Result is a {@link Map} between category name to a
   * {@link List} of all {@link ComponentType}s that share that
   * category name.
   *
   * @return
   */
  public static Map<String, List<ComponentType>> getComponentTypes() {
    LOG.trace("getComponentTypes()");
    return componentTypes;
  }

  public static List<String> getCategories(boolean sorted) {
    List<String> categories = new ArrayList<String>(getComponentTypes().keySet());
    if (sorted) {
      Collections.sort(categories);
    }
    return categories;
  }

  public static List<String> getCategories() {
    return getCategories(false);
  }

  private static Map<String, List<ComponentType>> loadComponentTypes() {
    LOG.trace("loadComponentTypes()");
    LOG.info("Loading component types.");
    Map<String, List<ComponentType>> foundTypes = new HashMap<>();
    try {
      for (Class<?> clazz : Utils.getClasses("org.diylc.components")) {
        if (IDIYComponent.class.isAssignableFrom(clazz)) {
          Class<? extends IDIYComponent<?>> theClass = (Class<? extends IDIYComponent<?>>) clazz;
          LOG.trace("Looking at {}", theClass.getName());
          if (!Modifier.isAbstract(theClass.getModifiers())) {
            ComponentType componentType = extractFrom(theClass);
            if (componentType != null) {
              List<ComponentType> nestedList = foundTypes.get(componentType.getCategory());
              if (nestedList == null) {
                nestedList = new ArrayList<ComponentType>();
                foundTypes.put(componentType.getCategory(), nestedList);
              }
              nestedList.add(componentType);
            }
          }
          Unmarshaller[] unmarshallers = theClass.getAnnotationsByType(Unmarshaller.class);
          for (Unmarshaller unmarshaller : unmarshallers) {
            String tagName = unmarshaller.value();
            LOG.trace("{} can unmarshal {}", theClass.getName(), tagName);
            if (xmlReaders.get(tagName) != null) {
              throw new RuntimeException("multiple unmarshallers for " + tagName);
            }
            xmlReaders.put(tagName, theClass);
          }
        }
      }
      // Log all found types for posterity
      for (Map.Entry<String, List<ComponentType>> e : foundTypes.entrySet()) {
        LOG.trace("{}: {}", e.getKey(), e.getValue());
      }
      // Log all found unmarshallers for posterity
      for (Map.Entry<String, Class<? extends IDIYComponent<?>>> e : xmlReaders.entrySet()) {
        if (e.getValue() != null) {
          LOG.trace("{} unmarshals {}", e.getValue().getName(), e.getKey());
        } else {
          String xmlReaderFail = e.getKey() + " can't be unmarshalled by null!";
          LOG.error(xmlReaderFail);
          throw new RuntimeException(xmlReaderFail);
        }
      }
    } catch (Exception e) {
      LOG.error("Error loading component types", e);
      throw new RuntimeException(e);
    }
    try {
      LOG.trace("Loading default variant map");
      loadVariants();
      LOG.info("Loaded default variants for {} components", defaultVariantMap.size());
    } catch (IOException e) {
      LOG.error("Could not load default variants", e);
      // TODO should we throw new RuntimeException(e)?
    }
    return foundTypes;
  }

  private static void loadVariants() throws IOException {
      XmlNode variantsXml = XmlReader.read(ComponentType.class.getResource(
          "/org/diylc/variants4.xml"));
      // TODO handle variants
      /*
      Map<String, List<Template>> map =
          (Map<String, List<Template>>) Serializer.fromResource("/org/diylc/variants.xml");
      defaultVariantMap.putAll(map);
      */
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

  /**
   * Get component type from cache.
   * If component type hasn't been added to cache, null is returned.
   *
   * @param className Name of component type class.
   * @return matching ComponentType, or null if not found
   */
  public static ComponentType byName(String className) {
    return componentTypeMap.get(className);
  }

  /**
     Extract component type from class.
  */
  public static ComponentType extractFrom(IDIYComponent<?> c) {
    return extractFrom((Class<? extends IDIYComponent<?>>) c.getClass());
  }

  /**
     Extract component type from class.
     Class should implement IDIYComponent or be a subclass of one that does.
     Also, class should have a ComponentDescriptor annotation.

     NOTE: Could - and indeed should - this be done at compile time?
     Runtime handling is of course required if components are to be
     loaded from external JARs.

     @param clazz The class.
     @return component type
   */
  public static ComponentType extractFrom(Class<? extends IDIYComponent<?>> clazz) {
    LOG.trace("extractFrom({})", clazz == null ? "null" : clazz.getName());
    if (clazz == null) {
      return null;
    }
    /* have we already seen this class? if so, get it from cache */
    ComponentType extractedType = byName(clazz.getName());
    if (extractedType != null) {
      return extractedType;
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
    String xmlTag = annotation.xmlTag();
    IComponentTransformer transformer = getComponentTransformer(annotation.transformer());
    KeywordPolicy keywordPolicy = annotation.keywordPolicy();
    String keywordTag = annotation.keywordTag();

    try {
      // Draw component icon for later use
      IDIYComponent<?> componentInstance = (IDIYComponent<?>) clazz.newInstance();
      icon = componentInstance.getImageIcon();
    } catch (Exception e) {
      LOG.error("Error drawing component icon for " + clazz.getName(), e);
    }
    extractedType = new ComponentType(
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
        xmlTag,
        transformer,
        keywordPolicy,
        keywordTag);
    componentTypeMap.put(clazz.getName(), extractedType);
    return extractedType;
  }

  public static void addVariants(Map<String, List<Template>> variants) {
    for (Map.Entry<String, List<Template>> e : variants.entrySet()) {
      String key = e.getKey();
      List<Template> value = e.getValue();
      if (defaultVariantMap.containsKey(key)) {
        defaultVariantMap.get(key).addAll(value);
      } else {
        defaultVariantMap.put(key, value);
      }
    }
  }

  public static void saveVariants(String fileName) throws IOException {
    Serializer.toFile("variants.xml", defaultVariantMap);
  }

  public static Map<String, ComponentType> getUnmarshalledTypes(String tagName) {
    return unmarshalledTypes.get(tagName);
  }
}
