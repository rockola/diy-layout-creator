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

package org.diylc.presenter;

import java.awt.Point;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.App;
import org.diylc.common.ComponentType;
import org.diylc.common.Config;
import org.diylc.common.Orientation;
import org.diylc.common.OrientationHV;
import org.diylc.common.PropertyWrapper;
import org.diylc.components.AbstractComponent;
import org.diylc.core.Grid;
import org.diylc.core.Project;

/**
 * Manages component instantiation.
 *
 * @author Branislav Stojkovic
 */
public class InstantiationManager {

  private static final Logger LOG = LogManager.getLogger(InstantiationManager.class);
  private static final InstantiationManager instantiationManager = new InstantiationManager();

  public static final int MAX_RECENT_COMPONENTS = 16;
  public static final ComponentType clipboardType =
      new ComponentType("Clipboard contents", "Components from the clipboard", "Multi");
  public static final ComponentType blockType =
      new ComponentType("Building block", "Components from the building block", "Multi");

  private static ComponentType componentTypeSlot;
  private static AbstractComponent template;
  private static List<AbstractComponent> componentSlot;
  private static Point firstControlPoint;
  private static Point potentialControlPoint;

  private InstantiationManager() {
    //
  }

  public static ComponentType getComponentTypeSlot() {
    return componentTypeSlot;
  }

  public static AbstractComponent getTemplate() {
    return template;
  }

  public static List<AbstractComponent> getComponentSlot() {
    return componentSlot;
  }

  public static Point getFirstControlPoint() {
    return firstControlPoint;
  }

  public static Point getPotentialControlPoint() {
    return potentialControlPoint;
  }

  public static void setPotentialControlPoint(Point potentialPoint) {
    potentialControlPoint = potentialPoint;
  }

  public static void setComponentTypeSlot(
      ComponentType typeSlot,
      AbstractComponent theTemplate,
      Project project,
      boolean forceInstantiate)
      throws Exception {
    componentTypeSlot = typeSlot;
    template = theTemplate;
    if (componentTypeSlot != null
        && (forceInstantiate || componentTypeSlot.getCreationMethod().isSingleClick())) {
      componentSlot = instantiateComponent(componentTypeSlot, template, project);
    } else {
      componentSlot = null;
    }
    firstControlPoint = null;
    potentialControlPoint = null;
  }

  public static void instantiatePointByPoint(Point scaledPoint, Project project) throws Exception {
    firstControlPoint = scaledPoint;
    componentSlot = instantiateComponent(componentTypeSlot, template, firstControlPoint, project);
    // Set the other control point to the same location, we'll
    // move it later when mouse moves.
    componentSlot.get(0).setControlPoint(firstControlPoint, 0);
    componentSlot.get(0).setControlPoint(firstControlPoint, 1);
  }

  /**
   * Updates component in the slot with the new second control point.
   *
   * @param scaledPoint
   * @return true, if any change is made
   */
  public static boolean updatePointByPoint(Point scaledPoint) {
    boolean changeMade = !scaledPoint.equals(potentialControlPoint);
    potentialControlPoint = scaledPoint;
    if (componentSlot != null && !componentSlot.isEmpty()) {
      componentSlot.get(0).setControlPoint(scaledPoint, 1);
    }
    return changeMade;
  }

  public static void pasteComponents(
      Collection<AbstractComponent> components,
      Point scaledPoint,
      boolean snapToGrid,
      boolean autoGroup,
      Project project) {

    Set<String> existingNames = new HashSet<String>();
    for (AbstractComponent c : project.getComponents()) {
      existingNames.add(c.getName());
    }
    List<AbstractComponent> allComponents =
        new ArrayList<AbstractComponent>(project.getComponents());

    // Adjust location of components so they are centered under the mouse
    // cursor
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    /*
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    */
    for (AbstractComponent component : components) {
      // assign a new name if it already exists in the project
      if (existingNames.contains(component.getName())) {
        ComponentType componentType =
            ComponentType.extractFrom((Class<? extends AbstractComponent>) component.getClass());
        String newName = createUniqueName(componentType, allComponents);
        existingNames.add(newName);
        component.setName(newName);
        allComponents.add(component);
      }

      for (int i = 0; i < component.getControlPointCount(); i++) {
        Point p = component.getControlPoint(i);
        /*
        if (p.x > maxX) {
          maxX = p.x;
        }
        if (p.y > maxY) {
          maxY = p.y;
        }
        */
        if (p.x < minX) {
          minX = p.x;
        }
        if (p.y < minY) {
          minY = p.y;
        }
      }
    }
    int x = minX;
    int y = minY;
    if (snapToGrid) {
      x = project.getGrid().roundToGrid(x);
      y = project.getGrid().roundToGrid(y);
    }
    for (AbstractComponent component : components) {
      for (int i = 0; i < component.getControlPointCount(); i++) {
        Point p = component.getControlPoint(i);
        p.translate(-x, -y);
        component.setControlPoint(p, i);
      }
    }

    // Update component slot
    componentSlot = new ArrayList<AbstractComponent>(components);

    // Update the component type slot so the app knows that
    // something's being instantiated.
    componentTypeSlot = autoGroup ? blockType : clipboardType;

    if (snapToGrid) {
      scaledPoint = project.getGrid().snapToGrid(scaledPoint);
    }
    // Update the location according to mouse location
    updateSingleClick(scaledPoint, snapToGrid, project.getGrid());
  }

  /**
   * Updates location of component slot based on the new mouse location.
   *
   * @param scaledPoint
   * @param snapToGrid
   * @param grid
   * @return true if we need to refresh the canvas
   */
  public static boolean updateSingleClick(Point scaledPoint, boolean snapToGrid, Grid grid) {
    LOG.trace("updateSingleClick({}, {}, {})", scaledPoint, snapToGrid, grid);
    if (potentialControlPoint == null) {
      potentialControlPoint = new Point(0, 0);
    }
    if (scaledPoint == null) {
      scaledPoint = new Point(0, 0);
    }
    int dx = scaledPoint.x - potentialControlPoint.x;
    int dy = scaledPoint.y - potentialControlPoint.y;
    if (snapToGrid) {
      dx = grid.roundToGrid(dx);
      dy = grid.roundToGrid(dy);
    }
    // Only repaint if there's an actual change.
    if (dx == 0 && dy == 0) {
      return false;
    }
    potentialControlPoint.translate(dx, dy);
    if (componentSlot == null) {
      LOG.error("Component slot should not be null!");
    } else {
      Point p = new Point();
      for (AbstractComponent component : componentSlot) {
        for (int i = 0; i < component.getControlPointCount(); i++) {
          p.setLocation(component.getControlPoint(i));
          p.translate(dx, dy);
          component.setControlPoint(p, i);
        }
      }
    }
    return true;
  }

  public static List<AbstractComponent> instantiateComponent(
      ComponentType componentType, AbstractComponent template, Project project)
      throws InstantiationException, IllegalAccessException, InvocationTargetException,
          NoSuchMethodException {
    return instantiateComponent(componentType, template, new Point(0, 0), project);
  }

  public static List<AbstractComponent> instantiateComponent(
      ComponentType componentType, AbstractComponent template, Point point, Project project)
      throws InstantiationException, IllegalAccessException, InvocationTargetException,
          NoSuchMethodException {
    LOG.info("Instantiating component of type {}", componentType.getInstanceClass().getName());

    // Instantiate the component.
    AbstractComponent component =
        componentType.getInstanceClass().getDeclaredConstructor().newInstance();

    component.setName(createUniqueName(componentType, project.getComponents()));

    // Translate them to the desired location.
    if (point != null) {
      for (int j = 0; j < component.getControlPointCount(); j++) {
        Point controlPoint = new Point(component.getControlPoint(j));
        controlPoint.translate(point.x, point.y);
        // snapPointToGrid(controlPoint);
        component.setControlPoint(controlPoint, j);
      }
    }

    // loadComponentShapeFromTemplate(component, template);

    fillWithDefaultProperties(component, template);

    // Write to recent components
    List<String> recentComponentTypes =
        (List<String>)
            App.getObject(Config.Flag.RECENT_COMPONENTS, (Object) new ArrayList<ComponentType>());
    String className = componentType.getInstanceClass().getName();
    if (recentComponentTypes.size() == 0 || !recentComponentTypes.get(0).equals(className)) {

      // Remove if it's already somewhere in the list.
      recentComponentTypes.remove(className);
      // Add to the end of the list.
      recentComponentTypes.add(0, className);
      // Trim the list if necessary.
      if (recentComponentTypes.size() > MAX_RECENT_COMPONENTS) {
        recentComponentTypes.remove(recentComponentTypes.size() - 1);
      }
      App.putValue(Config.Flag.RECENT_COMPONENTS, recentComponentTypes);
    }

    List<AbstractComponent> list = new ArrayList<AbstractComponent>();
    list.add(component);
    return list;
  }

  /**
   * Creates a unique component name for the specified type. Existing components are taken into
   * account.
   *
   * @param componentType Type of component
   * @param components Existing components
   * @return
   */
  public static String createUniqueName(
      ComponentType componentType, List<AbstractComponent> components) {
    boolean exists = true;
    String[] takenNames = new String[components.size()];
    for (int j = 0; j < components.size(); j++) {
      takenNames[j] = components.get(j).getName();
    }
    Arrays.sort(takenNames);
    int i = 0;
    while (exists) {
      i++;
      String name = componentType.getNamePrefix() + i;
      exists = false;
      if (Arrays.binarySearch(takenNames, name) >= 0) {
        exists = true;
      }
    }
    return componentType.getNamePrefix() + i;
  }

  public static void fillWithDefaultProperties(AbstractComponent target, AbstractComponent source) {
    throw new RuntimeException("TODO");
  }

  /**
   * Finds any properties that have default values and injects default values. Typically it should
   * be used for {@link AbstractComponent} and {@link Project} objects.
   *
   * @param object
   * @param template
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   * @throws SecurityException
   */
  /*
  public static void fillWithDefaultProperties(Object object, AbstractComponent template) {
    // Extract properties.
    List<PropertyWrapper> properties = ComponentProcessor.extractProperties(object.getClass());
    Map<String, PropertyWrapper> propertyCache = new HashMap<String, PropertyWrapper>();
    // Override with default values if available.
    for (PropertyWrapper property : properties) {
      propertyCache.put(property.getName(), property);
      Object defaultValue =
          App.getObject(
              String.format(
                  "%s%s:%s",
                  Presenter.DEFAULTS_KEY_PREFIX, object.getClass().getName(), property.getName()));
      if (defaultValue != null) {
        property.setValue(defaultValue);
        try {
          property.writeTo(object);
        } catch (Exception e) {
          LOG.error("Could not write property " + property.getName(), e);
        }
      }
    }
    if (template != null) {
      for (Map.Entry<String, Object> pair : template.getValues().entrySet()) {
        PropertyWrapper property = propertyCache.get(pair.getKey());
        if (property == null) {
          LOG.warn("Cannot find property " + pair.getKey());
        } else {
          LOG.debug("Filling value from template for " + pair.getKey());
          property.setValue(pair.getValue());
          try {
            property.writeTo(object);
          } catch (Exception e) {
            LOG.error("Could not write property " + property.getName(), e);
          }
        }
      }
    }
  }
  */

  /**
   * Uses stored control points from the template to shape component.
   *
   * @param component
   * @param template
   */
  /*
  public static void loadComponentShapeFromTemplate(
      AbstractComponent component, AbstractComponent template) {
    if (template != null
        && template.getPoints() != null
        && template.getPoints().size() >= component.getControlPointCount()) {
      for (int i = 0; i < component.getControlPointCount(); i++) {
        Point p = new Point(component.getControlPoint(0));
        p.translate(template.getPoints().get(i).x, template.getPoints().get(i).y);
        component.setControlPoint(p, i);
      }
    }
  }
  */

  public static void tryToRotateComponentSlot() {
    if (componentSlot == null) {
      LOG.debug("Component slot is empty, cannot rotate");
      return;
    }
    List<PropertyWrapper> properties =
        ComponentProcessor.extractProperties(componentTypeSlot.getInstanceClass());
    PropertyWrapper angleProperty = null;
    for (PropertyWrapper propertyWrapper : properties) {
      if (propertyWrapper.getType().getName().equals(Orientation.class.getName())
          || propertyWrapper.getType().getName().equals(OrientationHV.class.getName())
          || propertyWrapper.getName().equalsIgnoreCase("angle")) {
        angleProperty = propertyWrapper;
        break;
      }
    }
    if (angleProperty == null) {
      LOG.debug(
          "Component in the slot does not have a property of type Orientation, cannot rotate");
      return;
    }
    try {
      for (AbstractComponent component : componentSlot) {
        angleProperty.readFrom(component);
        Object value = angleProperty.getValue();
        if (value instanceof Orientation) {
          angleProperty.setValue(
              Orientation.values()[
                  (((Orientation) value).ordinal() + 1) % Orientation.values().length]);
        } else if (value instanceof OrientationHV) {
          angleProperty.setValue(
              OrientationHV.values()[
                  (((OrientationHV) value).ordinal() + 1) % OrientationHV.values().length]);
        } else if (angleProperty.getName().equalsIgnoreCase("angle")) {
          int angle = (Integer) angleProperty.getValue();
          angle += 90;
          if (angle >= 360) {
            angle -= 360;
          }
          angleProperty.setValue(angle);
        }
        angleProperty.writeTo(component);
      }
    } catch (Exception e) {
      LOG.warn("Error trying to rotate the component", e);
    }
  }
}
