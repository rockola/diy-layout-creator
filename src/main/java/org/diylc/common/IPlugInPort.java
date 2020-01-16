/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2019 held jointly by the individual authors.

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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.diylc.appframework.simplemq.IMessageListener;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.core.ExpansionMode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.Template;
import org.diylc.core.Theme;
import org.diylc.core.measures.Size;

/**
 * Interface for communication between plug-ins and the
 * application. Plug-ins may acquire information or make changes
 * through this interface. Application events are dispatched to
 * plug-ins through {@link MessageDispatcher}
 *
 * @author Branislav Stojkovic
 * @see IPlugIn
 * @see MessageDispatcher
 * @see IMessageListener
 * @see EventType
 */
public interface IPlugInPort
    extends
        IMouseProcessor,
        IKeyProcessor,
        IVariantProcessor,
        IBlockProcessor,
        INetlistProcessor {

  String THEME_KEY = "theme";
  int DND_TOGGLE_STICKY = 0x1;
  int DND_TOGGLE_SNAP = 0x40000000;

  enum Key {
    ABNORMAL_EXIT,
    ANTI_ALIASING,
    AUTO_EDIT,
    BLOCKS,
    CONTINUOUS_CREATION,
    EXPORT_GRID,
    EXTRA_SPACE,
    FAVORITES,
    HARDWARE_ACCELERATION,
    HEARTBEAT,
    HIGHLIGHT_CONTINUITY_AREA,
    HI_QUALITY_RENDER,
    METRIC,
    OUTLINE,
    RECENT_COMPONENTS,
    RECENT_FILES,
    SHOW_GRID,
    SHOW_RULERS,
    SNAP_TO_GRID,
    STICKY_POINTS,
    TEMPLATES,
    WHEEL_ZOOM
  }

  enum Debug {
    COMPONENT_AREA,
    CONTINUITY_AREA
  }

  /**
   * Returns size of the canvas that takes project dimensions into
   * account as well as zoom level.  Each dimension is calculated as
   * the product of the actual size and the number of pixels per
   * unit. If <code>useZoom</code> is set to true, the result is
   * scaled by zoom factor.
   *
   * @param useZoom
   * @param includeExtraSpace
   * @return canvas dimensions
   */
  Dimension getCanvasDimensions(boolean useZoom, boolean includeExtraSpace);

  /**
   * Returns an instance of {@link Cursor} that should be used at the
   * specified location.
   *
   * <p>Note: point coordinates are scaled for zoom factor.
   *
   * @param point
   * @return cursor
   */
  Cursor getCursorAt(Point point);

  /**
   * Returns an instance of currently loaded project.
   *
   * @return
   */
  Project getCurrentProject();

  /**
   * Loads specified {@link Project}.
   *
   * @param project
   * @param freshStart
   * @param filename Optional file name when loading from a file.
   */
  void loadProject(Project project, boolean freshStart, String filename);

  /** Creates a new project. */
  void createNewProject();

  /**
   * Loads a project from the specified file.
   *
   * @param fileName
   */
  void loadProjectFromFile(String fileName);

  /**
   * Saves the current project into the specified file.
   *
   * @param fileName
   * @param isBackup
   */
  void saveProjectToFile(String fileName, boolean isBackup);

  /**
     @return the current file name.
  */
  String getCurrentFileName();

  /**
     @return true if the current project is modified.
  */
  boolean isProjectModified();

  /**
   * Shows a user dialog if there are changes to confirm that it's safe to proceed.
   *
   * @return true, if file actions (new, open, close) can be taken
   */
  boolean allowFileAction();

  /**
   * Returns all available {@link ComponentType}s classified by
   * category. Result is a {@link Map} between category name to a
   * {@link List} of all {@link ComponentType}s that share that
   * category name.
   *
   * @return
   */
  Map<String, List<ComponentType>> getComponentTypes();

  /**
   * Draws project on the provided {@link Graphics2D}. If the provided
   * filter is not null, it will be used to filter the components that
   * are shown.
   *
   * @param g2d
   * @param drawOptions specific drawing options
   * @param filter
   * @param externalZoom
   */
  void draw(
      Graphics2D g2d,
      Set<DrawOption> drawOptions,
      IComponentFilter filter,
      Double externalZoom);

  Double[] getAvailableZoomLevels();

  /**
   * Returns current zoom level where <code>zoomLevel = 1.0d</code> means 100%.
   *
   * @return current zoom level
   */
  double getZoomLevel();

  /**
   * Changes current zoom level where <code>zoomLevel = 1.0d</code> means 100%.
   *
   * @param zoomLevel new zoom leve
   */
  void setZoomLevel(double zoomLevel);

  /**
   * Adds a list of components to the project.
   *
   * @param components
   * @param autoGroup
   */
  void pasteComponents(Collection<IDIYComponent<?>> components, boolean autoGroup);

  /**
   * Selects all components in the project.
   *
   * @param layer If > 0, designates layer to select. If <= 0, select all regardless of layer.
   */
  void selectAll(int layer);

  /** Duplicates selected components and places them nearby. */
  void duplicateSelection();

  /** Deletes all the selected components from the project. */
  void deleteSelectedComponents();

  /** Groups all selected components. */
  void groupSelectedComponents();

  /** Ungroups all selected components. */
  void ungroupSelectedComponents();

  /**
   * Expands the current selection to include surrounding
   * components. Options are controlled with
   * <code>expansionMode</code> flag.
   *
   * @param expansionMode
   */
  void expandSelection(ExpansionMode expansionMode);

  /**
   * Finds all components at the specified location, sorted by z-index
   * from top to bottom. Location depends on the current zoom level.
   *
   * @param point
   * @return
   */
  List<IDIYComponent<?>> findComponentsAt(Point point);

  /**
   * Rotates selection for 90 degrees.
   *
   * @param direction 1 for clockwise, -1 for counter-clockwise
   */
  void rotateSelection(int direction);

  /**
   * Mirrors selected components in the given axis.
   *
   * @param direction
   */
  void mirrorSelection(int direction);

  /**
   * Returns the minimum rectangle containing all selected components,
   * or null if none exists.  Rectangle is scaled by the current zoom
   * level.
   *
   * @param applyZoom
   * @return
   */
  Rectangle2D getSelectionBounds(boolean applyZoom);

  /**
   * Moves selection for the specified offset.
   *
   * @param xOffset
   * @param yOffset
   * @param includeStuckComponents
   */
  void nudgeSelection(Size xOffset, Size yOffset, boolean includeStuckComponents);

  /**
   * Sets default value for the specified property name for currently
   * selected component types.
   *
   * @param propertyName display name for property
   * @param value new default value, must not be null
   */
  void setSelectionDefaultPropertyValue(String propertyName, Object value);

  /**
   * Sets default value for the specified property name for projects.
   *
   * @param clazz class to set defaults to
   * @param propertyName display name for property
   * @param value new default value, must not be null
   */
  void setDefaultPropertyValue(Class<?> clazz, String propertyName, Object value);

  /**
   * @return a list of properties that are mutual for all the selected components. Resulting list
   *     may be empty if selected components do not have mutual properties or can be null if the
   *     selection is empty.
   */
  List<PropertyWrapper> getMutualSelectionProperties();

  void editSelection();

  /**
   * @param obj
   * @return a list of editable properties of the current project.
   */
  List<PropertyWrapper> getProperties(Object obj);

  /**
   * Applies specified properties to the current project.
   *
   * @param obj
   * @param properties
   */
  void applyProperties(Object obj, List<PropertyWrapper> properties);

  /**
     Gets the current new component slot.
  */
  ComponentType getNewComponentTypeSlot();

  /**
   * Sets the new component slot. Specified component type will be used to instantiate new
   * component.
   *
   * @param componentType
   * @param template
   * @param forceInstantiate
   */
  void setNewComponentTypeSlot(
      ComponentType componentType, Template template, boolean forceInstantiate);

  /**
   * Changes default size notation, true for metric, false for imperial.
   *
   * @param isMetric
   */
  void setMetric(boolean isMetric);

  /**
   * Locks or unlocks the specified layer. All components within +- 0.5 range will be affected by
   * the change as well.
   *
   * @param layerZOrder
   * @param locked
   */
  void setLayerLocked(int layerZOrder, boolean locked);

  /**
   * Shows or hides the specified layer. All components within +- 0.5 range will be affected by the
   * change as well.
   *
   * @param layerZOrder
   * @param visible
   */
  void setLayerVisibility(int layerZOrder, boolean visible);

  /**
     @return selection size expressed in both inches or centimeters, respectively
  */
  Point2D[] calculateSelectionDimension();

  /**
     Sends each of the selected components one step back.
  */
  void sendSelectionToBack();

  /**
     Brings each of the selected components one step to front.
  */
  void bringSelectionToFront();

  /**
     Causes the display to refresh.
  */
  void refresh();

  /**
     @return currently selected theme.
  */
  Theme getSelectedTheme();

  /**
   * Changes the current theme.
   *
   * @param theme
   */
  void setSelectedTheme(Theme theme);

  /**
   * Renumbers all the selected components.
   *
   * @param xAxisFirst
   */
  void renumberSelectedComponents(boolean xAxisFirst);

  /**
     @return size of extra space around the canvas
  */
  double getExtraSpace();
}
