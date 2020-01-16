/*
  Diy Layout Creator (DIYLC).
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

package org.diylc.presenter;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.App;
import org.diylc.appframework.Serializer;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.common.BuildingBlockPackage;
import org.diylc.common.ComponentType;
import org.diylc.common.DrawOption;
import org.diylc.common.EventType;
import org.diylc.common.IComponentFilter;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IKeyProcessor;
import org.diylc.common.INetlistAnalyzer;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.common.VariantPackage;
import org.diylc.core.ExpansionMode;
import org.diylc.core.IContinuity;
import org.diylc.core.IDIYComponent;
import org.diylc.core.ISwitch;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.core.Template;
import org.diylc.core.Theme;
import org.diylc.core.annotations.IAutoCreator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.NetlistAnalyzer;
import org.diylc.netlist.Node;
import org.diylc.netlist.Position;
import org.diylc.netlist.SwitchSetup;
import org.diylc.utils.Constants;

/**
 * The main presenter class, contains core app logic and drawing routines.
 *
 * @author Branislav Stojkovic
 */
public class Presenter implements IPlugInPort {

  private static final Logger LOG = LogManager.getLogger(Presenter.class);
  private static final InstantiationManager instantiationManager =
      InstantiationManager.getInstance();

  private String getMsg(String key) {
    return App.getString("message.presenter." + key);
  }

  public static final String DEFAULTS_KEY_PREFIX = "default.";

  private static volatile Map<String, List<Template>> defaultVariantMap = null;

  static {
    try {
      @SuppressWarnings("unchecked")
      Map<String, List<Template>> map =
          (Map<String, List<Template>>) Serializer.fromResource("/org/diylc/variants.xml");
      LOG.info("Loading default variant map");
      defaultVariantMap = new TreeMap<String, List<Template>>(String.CASE_INSENSITIVE_ORDER);
      defaultVariantMap.putAll(map);
      LOG.info("Loaded default variants for {} components", defaultVariantMap.size());
    } catch (IOException e) {
      LOG.error("Could not load default variants", e);
    }
  }

  public static final List<IDIYComponent<?>> EMPTY_SELECTION = Collections.emptyList();

  public static final int ICON_SIZE = 32;

  private static final int MAX_RECENT_FILES = 20;

  private Project currentProject;
  private Map<String, List<ComponentType>> componentTypes;
  /**
   * {@link List} of {@link IAutoCreator} objects that are capable of
   * creating more components automatically when a component is
   * created, e.g. Solder Pads.
   */
  private List<IAutoCreator> autoCreators;
  // Maps component class names to ComponentType objects.
  private List<IPlugIn> plugIns;

  // Maps components that have at least one dragged point to set of
  // indices that designate which of their control points are being
  // dragged.
  private Map<IDIYComponent<?>, Set<Integer>> controlPointMap;
  private Set<IDIYComponent<?>> lockedComponents;

  // Utilities
  private DrawingManager drawingManager;
  private ProjectFileManager projectFileManager;

  private Rectangle selectionRect;

  private static MessageDispatcher<EventType> messageDispatcher =
      new MessageDispatcher<EventType>();

  // D&D
  private boolean dragInProgress = false;

  // Previous mouse location, not scaled for zoom factor.
  private Point previousDragPoint = null;
  private Project preDragProject = null;
  private int dragAction;
  private Point previousScaledPoint;

  public Presenter() {
    super();
    plugIns = new ArrayList<IPlugIn>();
    lockedComponents = new HashSet<IDIYComponent<?>>();
    currentProject = new Project();
    drawingManager = new DrawingManager(messageDispatcher);
    projectFileManager = new ProjectFileManager(messageDispatcher);
    upgradeVariants();
  }

  public static void dispatchMessage(EventType eventType, Object... params) {
    messageDispatcher.dispatchMessage(eventType, params);
  }

  public void installPlugin(IPlugIn plugIn) {
    LOG.trace("installPlugin({})", plugIn.getClass().getSimpleName());
    plugIns.add(plugIn);
    plugIn.connect(this);
    messageDispatcher.subscribe(plugIn);
  }

  // IPlugInPort

  @Override
  public Double[] getAvailableZoomLevels() {
    return new Double[] {0.25d, 0.3333d, 0.5d, 0.6667d, 0.75d, 1d, 1.25d, 1.5d, 2d, 2.5d, 3d};
  }

  @Override
  public double getZoomLevel() {
    return drawingManager.getZoomLevel();
  }

  @Override
  public void setZoomLevel(double zoomLevel) {
    drawingManager.setZoomLevel(zoomLevel);
  }

  @Override
  public Cursor getCursorAt(Point point) {
    // Only change the cursor if we're not making a new component.
    if (App.getBoolean(Key.HIGHLIGHT_CONTINUITY_AREA, false))
      return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

    if (instantiationManager.getComponentTypeSlot() == null) {
      // Scale point to remove zoom factor.
      Point2D scaledPoint = scalePoint(point);
      if (controlPointMap != null && !controlPointMap.isEmpty()) {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
      }
      for (IDIYComponent<?> component : currentProject.getComponents()) {
        if (!isComponentLocked(component)
            && isComponentVisible(component)
            && !App.getBoolean(Key.HIGHLIGHT_CONTINUITY_AREA, false)) {

          ComponentArea area = drawingManager.getComponentArea(component);
          if (area != null
              && area.getOutlineArea() != null
              && area.getOutlineArea().contains(scaledPoint)) {

            return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
          }
        }
      }
    }
    return Cursor.getDefaultCursor();
  }

  @Override
  public Dimension getCanvasDimensions(boolean useZoom, boolean includeExtraSpace) {
    return drawingManager.getCanvasDimensions(
        currentProject,
        (useZoom ? drawingManager.getZoomLevel() : 1 / Constants.PIXEL_SIZE),
        includeExtraSpace);
  }

  public Project getCurrentProject() {
    return currentProject;
  }

  public void loadProject(Project project, boolean freshStart, String filename) {
    LOG.trace("loadProject({}, {})", project.getTitle(), freshStart);
    this.currentProject = project;
    currentProject.clearSelection();
    dispatchMessage(EventType.PROJECT_LOADED, project, freshStart, filename);
    dispatchMessage(EventType.REPAINT);
    dispatchMessage(EventType.LAYER_STATE_CHANGED, currentProject.getLockedLayers());
    dispatchMessage(EventType.LAYER_VISIBILITY_CHANGED, currentProject.getHiddenLayers());
  }

  public void createNewProject() {
    LOG.trace("createNewFile()");
    try {
      Project project = new Project();
      instantiationManager.fillWithDefaultProperties(project, null);
      loadProject(project, true, null);
      projectFileManager.startNewFile();
    } catch (Exception e) {
      LOG.error("Could not create new file", e);
      App.ui().error(getMsg("could-not-create"));
    }
  }

  /**
   * Loads a project from a file but does not set it up for use.
   *
   * @param fileName name of the project file
   * @return the project
   */
  public Project getProjectFromFile(String fileName) {
    return ProjectFileManager.getProjectFromFile(fileName);
  }

  public void loadProjectFromFile(String fileName) {
    LOG.trace("loadProjectFromFile({})", fileName);
    List<String> warnings = null;
    try {
      warnings = new ArrayList<String>();
      Project project =
          (Project) projectFileManager.deserializeProjectFromFile(fileName, warnings);
      loadProject(project, true, fileName);
      projectFileManager.fireFileStatusChanged();
      if (!warnings.isEmpty()) {
        StringBuilder builder = new StringBuilder(getMsg("issues-with-file"));
        for (String warning : warnings) {
          builder.append(warning);
        }
        App.ui().warn(builder.toString());
      }
      addToRecentFiles(fileName);
    } catch (Exception ex) {
      LOG.error("Could not load file " + fileName, ex);
      StringBuffer errorMessage = new StringBuffer();
      errorMessage.append(String.format(getMsg("could-not-open"), fileName));
      if (warnings != null && !warnings.isEmpty()) {
        errorMessage.append(" ").append(getMsg("possible-reasons"));
        for (String warn : warnings) {
          errorMessage.append(warn).append("\n");
        }
      }
      App.ui().error(errorMessage.toString());
    }
  }

  @SuppressWarnings("unchecked")
  private void addToRecentFiles(String fileName) {
    List<String> recentFiles = (List<String>) App.getObject(Key.RECENT_FILES);
    if (recentFiles == null) recentFiles = new ArrayList<String>();
    recentFiles.remove(fileName);
    recentFiles.add(0, fileName);
    while (recentFiles.size() > MAX_RECENT_FILES) recentFiles.remove(recentFiles.size() - 1);
    App.putValue(Key.RECENT_FILES, recentFiles);
  }

  private int showConfirmDialog(String message, String title, int optionType, int messageType) {
    return App.ui().showConfirmDialog(message, title, optionType, messageType);
  }

  private int showConfirmDialog(String message, String title) {
    return showConfirmDialog(message, title, IView.YES_NO_OPTION, IView.QUESTION_MESSAGE);
  }

  public boolean userConfirmed(String message, String title) {
    return showConfirmDialog(message, title) == IView.YES_OPTION;
  }

  private boolean warnedUserConfirmed(String message, String title) {
    return (showConfirmDialog(message, title, IView.YES_NO_OPTION, IView.WARNING_MESSAGE)
            == IView.YES_OPTION);
  }

  @Override
  public boolean allowFileAction() {
    if (projectFileManager.isModified()) {
      int response = showConfirmDialog(
          getMsg("unsaved-changes"),
          App.getString("message.warn"),
          IView.YES_NO_CANCEL_OPTION,
          IView.WARNING_MESSAGE);
      if (response == IView.YES_OPTION) {
        if (this.getCurrentFileName() == null) {
          File file = App.ui().promptFileSave();
          if (file == null) {
            return false;
          }
          saveProjectToFile(file.getAbsolutePath(), false);
        } else {
          saveProjectToFile(this.getCurrentFileName(), false);
        }
      }
      return response != IView.CANCEL_OPTION;
    }
    return true;
  }

  @Override
  public void saveProjectToFile(String fileName, boolean isBackup) {
    LOG.trace("saveProjectToFile({})", fileName);
    try {
      currentProject.setFileVersion(App.getVersionNumber());
      projectFileManager.serializeProjectToFile(currentProject, fileName, isBackup);
      if (!isBackup) addToRecentFiles(fileName);
    } catch (Exception ex) {
      LOG.error("Could not save file", ex);
      if (!isBackup) {
        App.ui().error(String.format(getMsg("could-not-save"), fileName));
      }
    }
  }

  @Override
  public String getCurrentFileName() {
    return projectFileManager.getCurrentFileName();
  }

  @Override
  public boolean isProjectModified() {
    return projectFileManager.isModified();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, List<ComponentType>> getComponentTypes() {
    LOG.trace("getComponentTypes()");
    if (componentTypes == null) {
      LOG.info("Loading component types.");
      componentTypes = new HashMap<String, List<ComponentType>>();
      Set<Class<?>> componentTypeClasses = null;
      try {
        componentTypeClasses = Utils.getClasses("org.diylc.components");

        for (Class<?> clazz : componentTypeClasses) {
          if (!Modifier.isAbstract(clazz.getModifiers())
              && IDIYComponent.class.isAssignableFrom(clazz)) {

            ComponentType componentType =
                ComponentProcessor.extractComponentTypeFrom(
                    (Class<? extends IDIYComponent<?>>) clazz);
            if (componentType == null) continue;
            List<ComponentType> nestedList;
            if (componentTypes.containsKey(componentType.getCategory())) {
              nestedList = componentTypes.get(componentType.getCategory());
            } else {
              nestedList = new ArrayList<ComponentType>();
              componentTypes.put(componentType.getCategory(), nestedList);
            }
            nestedList.add(componentType);
          }
        }

        for (Map.Entry<String, List<ComponentType>> e : componentTypes.entrySet()) {
          LOG.debug("{}: {}", e.getKey(), e.getValue());
        }
      } catch (Exception e) {
        LOG.error("Error loading component types", e);
      }
    }
    return componentTypes;
  }

  public List<IAutoCreator> getAutoCreators() {
    if (autoCreators == null) {
      autoCreators = new ArrayList<IAutoCreator>();
      Set<Class<?>> classes = null;
      try {
        classes = Utils.getClasses("org.diylc.components.autocreate");
        for (Class<?> clazz : classes) {
          if (IAutoCreator.class.isAssignableFrom(clazz)) {
            autoCreators.add((IAutoCreator) clazz.getDeclaredConstructor().newInstance());
            LOG.debug("Loaded auto-creator: " + clazz.getName());
          }
        }
      } catch (Exception e) {
        LOG.error("Error loading auto-creator types", e);
      }
    }
    return autoCreators;
  }

  @SuppressWarnings({"unchecked"})
  private boolean isComponentVisible(IDIYComponent<?> component) {
    return isComponentVisible(currentProject, component);
  }

  @SuppressWarnings({"unchecked"})
  private static boolean isComponentVisible(Project p, IDIYComponent<?> component) {

    ComponentType componentType =
        ComponentProcessor.extractComponentTypeFrom(
            (Class<? extends IDIYComponent<?>>) component.getClass());
    return !p.getHiddenLayers().contains((int) Math.round(componentType.getZOrder()));
  }

  public static void drawProject(Project p, Graphics2D g2d, Set<DrawOption> drawOptions) {
    List<String> failedComponentNames =
        drawProjectInternal(
            p,
            g2d,
            drawOptions,
            null,
            null,
            null,
            false,
            new DrawingManager(null));

    if (!failedComponentNames.isEmpty()) {
      for (String fc : failedComponentNames) {
        LOG.error("drawProject: {} failed", fc);
      }
    }
  }

  private String dragActionToString(int action) {
    if (action == DnDConstants.ACTION_COPY) {
      return "COPY";
    }
    if (action == DnDConstants.ACTION_MOVE) {
      return "MOVE";
    }
    if (action == DnDConstants.ACTION_LINK) {
      return "LINK";
    }
    String unknown = "[Unknown drag action " + action + "]";
    LOG.error("dragActionToString({}) {}", action, unknown);
    return unknown;
  }

  @Override
  public void draw(
      Graphics2D g2d,
      Set<DrawOption> drawOptions,
      final IComponentFilter filter,
      Double externalZoom) {
    if (currentProject == null) {
      return;
    }
    List<String> failedComponentNames =
        drawProjectInternal(
            currentProject,
            g2d,
            drawOptions,
            filter,
            externalZoom,
            selectionRect,
            dragInProgress,
            drawingManager);

    dispatchMessage(EventType.STATUS_MESSAGE_CHANGED,
                    failedComponentNames.isEmpty()
                    ? ""
                    : ("<html><font color='red'>Failed to draw components: "
                       + Utils.toCommaString(failedComponentNames)
                       + "</font></html>"));
  }

  private static List<String> drawProjectInternal(
      Project p,
      Graphics2D g2d,
      Set<DrawOption> drawOptions,
      final IComponentFilter filter,
      Double externalZoom,
      Rectangle selectionRect,
      boolean dragInProgress,
      DrawingManager drawingManager) {

    LOG.trace("drawProjectInternal({}, ..., dragInProgress={}, ...)", p, dragInProgress);

    Set<IDIYComponent<?>> groupedComponents = new HashSet<IDIYComponent<?>>();
    for (IDIYComponent<?> component : p.getComponents()) {
      // Only try to draw control points of ungrouped components.
      if (findAllGroupedComponents(p, component).size() > 1) {
        groupedComponents.add(component);
      }
    }

    // Concatenate the specified filter with our own filter that removes hidden layers
    IComponentFilter newFilter =
        new IComponentFilter() {

          @Override
          public boolean testComponent(IDIYComponent<?> component) {
            return ((filter == null || filter.testComponent(component))
                && isComponentVisible(p, component));
          }
        };

    // Don't draw the component in the slot if both control points
    // match.
    List<IDIYComponent<?>> componentSlotToDraw;
    if (instantiationManager.getFirstControlPoint() != null
        && instantiationManager.getPotentialControlPoint() != null
        && instantiationManager.getFirstControlPoint().equals(
            instantiationManager.getPotentialControlPoint())) {
      componentSlotToDraw = null;
    } else {
      componentSlotToDraw = instantiationManager.getComponentSlot();
    }
    List<IDIYComponent<?>> failedComponents =
        drawingManager.drawProject(
            g2d,
            p,
            drawOptions,
            newFilter,
            selectionRect,
            getLockedComponents(p),
            groupedComponents,
            Arrays.asList(
                instantiationManager.getFirstControlPoint(),
                instantiationManager.getPotentialControlPoint()),
            componentSlotToDraw,
            dragInProgress,
            externalZoom);
    List<String> failedComponentNames = new ArrayList<String>();
    for (IDIYComponent<?> component : failedComponents) {
      failedComponentNames.add(component.getName());
    }
    Collections.sort(failedComponentNames);
    return failedComponentNames;
  }

  /**
   * Finds all components whose areas include the specified {@link Point}. Point is <b>not</b>
   * scaled by the zoom factor. Components that belong to locked layers are ignored.
   *
   * @return
   */
  public List<IDIYComponent<?>> findComponentsAtScaled(Point point) {
    LOG.trace("findComponentsAtScaled({})", point);
    List<IDIYComponent<?>> components = currentProject.findComponentsAt(point);
    Iterator<IDIYComponent<?>> iterator = components.iterator();
    while (iterator.hasNext()) {
      IDIYComponent<?> component = iterator.next();
      if (isComponentLocked(component) || !isComponentVisible(component)) {
        iterator.remove();
      }
    }
    return components;
  }

  @Override
  public List<IDIYComponent<?>> findComponentsAt(Point point) {
    Point scaledPoint = scalePoint(point);
    LOG.trace("findComponentsAtScaled({}) scaledPoint {}", point, scaledPoint);
    List<IDIYComponent<?>> components = findComponentsAtScaled(scaledPoint);
    return components;
  }

  @Override
  public void mouseClicked(
      Point point,
      int button,
      boolean ctrlDown,
      boolean shiftDown,
      boolean altDown,
      int clickCount) {
    LOG.trace("mouseClicked({}, {}, {}, {}, {})", point, button, ctrlDown, shiftDown, altDown);
    Point scaledPoint = scalePoint(point);
    if (clickCount >= 2) {
      editSelection();
    } else {
      if (instantiationManager.getComponentTypeSlot() != null) {
        // Try to rotate the component on right click while creating.
        if (button != MouseEvent.BUTTON1) {
          instantiationManager.tryToRotateComponentSlot();
          dispatchMessage(EventType.REPAINT);
          return;
        }
        // Keep the reference to component type for later.
        ComponentType componentTypeSlot = instantiationManager.getComponentTypeSlot();
        Template template = instantiationManager.getTemplate();
        Project oldProject = currentProject.clone();
        switch (componentTypeSlot.getCreationMethod()) {
          case SINGLE_CLICK:
            LOG.trace("mouseClicked() creation method is Single Click");
            try {
              if (isSnapToGrid()) {
                CalcUtils.snapPointToGrid(scaledPoint, currentProject.getGridSpacing());
              }
              List<IDIYComponent<?>> componentSlot = instantiationManager.getComponentSlot();
              List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
              for (IDIYComponent<?> component : componentSlot) {
                currentProject.getComponents().add(component);
                newSelection.add(component);
              }
              // group components if there's more than one,
             // e.g. building blocks, but not clipboard
              // contents
              if (componentSlot.size() > 1
                  && !componentTypeSlot.getName().toLowerCase().contains("clipboard")) {

                this.currentProject.getGroups().add(new HashSet<IDIYComponent<?>>(componentSlot));
              }
              dispatchMessage(EventType.REPAINT);
              currentProject.setSelection(newSelection);
            } catch (Exception e) {
              LOG.error(
                  "Error instantiating component of type "
                  + componentTypeSlot.getInstanceClass().getName(),
                  e);
              // TODO: throw new RuntimeException(e)? //ola 20200113
            }

            if (componentTypeSlot.isAutoEdit() && App.autoEdit()) {
              editSelection();
            }
            if (App.continuousCreation()) {
              setNewComponentTypeSlot(componentTypeSlot, template, false);
            } else {
              setNewComponentTypeSlot(null, null, false);
            }
            break;
          case POINT_BY_POINT:
            LOG.trace("mouseClicked() creation method is Point-by-point");
            // First click is just to set the controlPointSlot and
            // componentSlot.
            if (isSnapToGrid()) {
              CalcUtils.snapPointToGrid(scaledPoint, currentProject.getGridSpacing());
            }
            if (instantiationManager.getComponentSlot() == null) {
              try {
                instantiationManager.instantiatePointByPoint(scaledPoint, currentProject);
              } catch (Exception e) {
                App.ui().error("Could not create component. Check log for details.");
                LOG.error("Could not create component", e);
              }
              dispatchMessage(
                  EventType.SLOT_CHANGED,
                  componentTypeSlot,
                  instantiationManager.getFirstControlPoint());
              dispatchMessage(EventType.REPAINT);
            } else {
              // On the second click, add the component to the
              // project.
              addPendingComponentsToProject(scaledPoint, componentTypeSlot, template);
            }
            break;
          default:
            LOG.error("Unknown creation method {}", componentTypeSlot.getCreationMethod());
        }
        // Notify the listeners.
        if (!oldProject.equals(currentProject)) {
          dispatchMessage(EventType.PROJECT_MODIFIED,
                          oldProject,
                          currentProject.clone(),
                          "Add " + componentTypeSlot.getName());
          //drawingManager.clearContinuityArea();
          projectFileManager.notifyFileChange();
        }
      } else if (App.getBoolean(Key.HIGHLIGHT_CONTINUITY_AREA, false)) {
        // NOTE: findContinuityAreaAtPoint(scaledPoint) has SIDE EFFECTS only!
        currentProject.findContinuityAreaAtPoint(scaledPoint);
        dispatchMessage(EventType.REPAINT);
      } else {
        List<IDIYComponent<?>> newSelection =
            new ArrayList<IDIYComponent<?>>(currentProject.getSelection());
        List<IDIYComponent<?>> components = findComponentsAtScaled(scaledPoint);
        // If there's nothing under mouse cursor deselect all.
        if (components.isEmpty()) {
          if (!ctrlDown) {
            newSelection.clear();
          }
        } else {
          IDIYComponent<?> topComponent = components.get(0);
          // If ctrl is pressed just toggle the component under mouse
          // cursor.
          if (ctrlDown) {
            if (newSelection.contains(topComponent)) {
              newSelection.removeAll(findAllGroupedComponents(topComponent));
            } else {
              newSelection.addAll(findAllGroupedComponents(topComponent));
            }
          } else {
            // Otherwise just select that one component.
            if (button == MouseEvent.BUTTON1
                || (button == MouseEvent.BUTTON3 && newSelection.size() == 1)
                || !newSelection.contains(topComponent)) {
              newSelection.clear();
            }

            newSelection.addAll(findAllGroupedComponents(topComponent));
          }
        }
        currentProject.setSelection(newSelection);
        updateSelection();
        dispatchMessage(EventType.REPAINT);
      }
    }
  }

  private void addPendingComponentsToProject(
      Point scaledPoint, ComponentType componentTypeSlot, Template template) {
    List<IDIYComponent<?>> componentSlot = instantiationManager.getComponentSlot();
    Point firstPoint = componentSlot.get(0).getControlPoint(0);
    // don't allow to create component with the same points
    if (scaledPoint == null || scaledPoint.equals(firstPoint)) return;
    // componentSlot.get(0).setControlPoint(scaledPoint, 1);
    List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
    for (IDIYComponent<?> component : componentSlot) {
      addComponent(component, true);
      // Select the new component if it's not locked and invisible.
      if (!isComponentLocked(component) && isComponentVisible(component)) {
        newSelection.add(component);
      }
    }

    currentProject.setSelection(newSelection);
    updateSelection();
    dispatchMessage(EventType.REPAINT);

    if (componentTypeSlot.isAutoEdit() && App.autoEdit()) {
      editSelection();
    }
    if (App.continuousCreation()) {
      setNewComponentTypeSlot(componentTypeSlot, template, false);
    } else {
      setNewComponentTypeSlot(null, null, false);
    }
  }

  @Override
  public boolean keyPressed(int key, boolean ctrlDown, boolean shiftDown, boolean altDown) {
    if (key != VK_DOWN
        && key != VK_LEFT
        && key != VK_UP
        && key != VK_RIGHT
        && key != IKeyProcessor.VK_H
        && key != IKeyProcessor.VK_V) {
      return false;
    }
    LOG.trace("keyPressed({}, {}, {}, {})", key, ctrlDown, shiftDown, altDown);
    Map<IDIYComponent<?>, Set<Integer>> controlPointMap =
        new HashMap<IDIYComponent<?>, Set<Integer>>();
    // If there aren't any control points, try to add all the selected
    // components with all their control points. That will allow the
    // user to drag the whole components.
    for (IDIYComponent<?> c : currentProject.getSelection()) {
      Set<Integer> pointIndices = new HashSet<Integer>();
      if (c.getControlPointCount() > 0) {
        for (int i = 0; i < c.getControlPointCount(); i++) {
          pointIndices.add(i);
        }
        controlPointMap.put(c, pointIndices);
      }
    }
    if (controlPointMap.isEmpty()) {
      return false;
    }

    boolean snapToGrid = App.snapToGrid();
    if (shiftDown) {
      snapToGrid = !snapToGrid;
    }

    Project oldProject = null;
    if (altDown) {
      boolean rotate = false;
      switch (key) {
        case IKeyProcessor.VK_RIGHT:
        case IKeyProcessor.VK_LEFT:
          oldProject = currentProject.clone();
          rotateComponents(
              currentProject.getSelection(),
              key == IKeyProcessor.VK_RIGHT ? 1 : -1,
              snapToGrid);
          rotate = true;
          break;
        case IKeyProcessor.VK_H:
        case IKeyProcessor.VK_V:
          oldProject = currentProject.clone();
          mirrorComponents(
              currentProject.getSelection(),
              key == IKeyProcessor.VK_H
              ? IComponentTransformer.HORIZONTAL
              : IComponentTransformer.VERTICAL,
              snapToGrid);
          break;
        default:
          return false;
      }
      dispatchMessage(
          EventType.PROJECT_MODIFIED,
          oldProject,
          currentProject.clone(),
          rotate ? "Rotate Selection" : "Mirror Selection");
      dispatchMessage(EventType.REPAINT);
      //drawingManager.clearContinuityArea();
      return true;
    }

    // Expand control points to include all stuck components.
    boolean sticky = App.stickyPoints();
    if (ctrlDown) {
      sticky = !sticky;
    }

    if (sticky) {
      includeStuckComponents(controlPointMap);
    }

    int d = snapToGrid ? (int) currentProject.getGridSpacing().convertToPixels() : 1;
    int dx = 0;
    int dy = 0;
    switch (key) {
      case IKeyProcessor.VK_DOWN:
        dy = d;
        break;
      case IKeyProcessor.VK_LEFT:
        dx = -d;
        break;
      case IKeyProcessor.VK_UP:
        dy = -d;
        break;
      case IKeyProcessor.VK_RIGHT:
        dx = d;
        break;
      default:
        return false;
    }

    oldProject = currentProject.clone();
    moveComponents(controlPointMap, dx, dy, snapToGrid);
    dispatchMessage(
        EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Move Selection");
    dispatchMessage(EventType.REPAINT);
    return true;
  }

  @Override
  public void editSelection() {
    List<PropertyWrapper> properties = getMutualSelectionProperties();
    if (properties != null && !properties.isEmpty()) {
      Set<PropertyWrapper> defaultedProperties = new HashSet<PropertyWrapper>();
      boolean edited = App.ui().editProperties(properties, defaultedProperties);
      if (edited) {
        try {
          applyPropertiesToSelection(properties);
        } catch (Exception e1) {
          App.ui().error(getMsg("selection-edit-error"));
          LOG.error("Error applying properties", e1);
        }
        // Save default values.
        for (PropertyWrapper property : defaultedProperties) {
          if (property.getValue() != null) {
            setSelectionDefaultPropertyValue(property.getName(), property.getValue());
          }
        }
      }
    }
  }

  @Override
  public void mouseMoved(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown) {
    if (point == null) {
      return;
    }

    dragAction = shiftDown ? IPlugInPort.DND_TOGGLE_SNAP : 0;

    Map<IDIYComponent<?>, Set<Integer>> components = new HashMap<IDIYComponent<?>, Set<Integer>>();
    this.previousScaledPoint = scalePoint(point);
    if (instantiationManager.getComponentTypeSlot() != null) {
      if (isSnapToGrid()) {
        CalcUtils.snapPointToGrid(previousScaledPoint, currentProject.getGridSpacing());
      }
      boolean refresh = false;
      switch (instantiationManager.getComponentTypeSlot().getCreationMethod()) {
        case POINT_BY_POINT:
          refresh = instantiationManager.updatePointByPoint(previousScaledPoint);
          break;
        case SINGLE_CLICK:
          refresh = instantiationManager.updateSingleClick(
              previousScaledPoint, isSnapToGrid(), currentProject.getGridSpacing());
          break;
      }
      if (refresh) {
        dispatchMessage(EventType.REPAINT);
      }
    } else {
      // Go backwards so we take the highest z-order components first.
      for (int i = currentProject.getComponents().size() - 1; i >= 0; i--) {
        IDIYComponent<?> component = currentProject.getComponents().get(i);
        for (int pointIndex = 0; pointIndex < component.getControlPointCount(); pointIndex++) {

          Point controlPoint = component.getControlPoint(pointIndex);
          // Only consider selected components that are not grouped.
          if (currentProject.inSelection(component)
              && component.canPointMoveFreely(pointIndex)
              && findAllGroupedComponents(component).size() == 1) {

            try {
              if (previousScaledPoint.distance(controlPoint) < DrawingManager.CONTROL_POINT_SIZE) {
                Set<Integer> indices = new HashSet<Integer>();
                indices.add(pointIndex);
                components.put(component, indices);
                break;
              }
            } catch (Exception e) {
              LOG.warn(
                  "Error reading control point for component of type {}",
                  component.getClass().getName());
            }
          }
        }
      }
    }

    Point2D inPoint =
        new Point2D.Double(
            1.0d * previousScaledPoint.x / Constants.PIXELS_PER_INCH,
            1.0d * previousScaledPoint.y / Constants.PIXELS_PER_INCH);
    Point2D mmPoint =
        new Point2D.Double(
            inPoint.getX() * SizeUnit.in.getFactor() / SizeUnit.cm.getFactor() * 10d,
            inPoint.getY() * SizeUnit.in.getFactor() / SizeUnit.cm.getFactor() * 10d);

    dispatchMessage(EventType.MOUSE_MOVED, previousScaledPoint, inPoint, mmPoint);

    if (!components.equals(controlPointMap)) {
      controlPointMap = components;
      dispatchMessage(
          EventType.AVAILABLE_CTRL_POINTS_CHANGED,
          new HashMap<IDIYComponent<?>, Set<Integer>>(components));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void selectAll(int layer) {
    LOG.trace("selectAll()");
    List<IDIYComponent<?>> newSelection =
        new ArrayList<IDIYComponent<?>>(currentProject.getComponents());
    newSelection.removeAll(getLockedComponents());
    if (layer > 0) {
      Iterator<IDIYComponent<?>> i = newSelection.iterator();
      while (i.hasNext()) {
        IDIYComponent<?> c = i.next();
        ComponentType type =
            ComponentProcessor.extractComponentTypeFrom(
                (Class<? extends IDIYComponent<?>>) c.getClass());
        if ((int) type.getZOrder() != layer) i.remove();
      }
    }
    currentProject.setSelection(newSelection);
    updateSelection();
    dispatchMessage(EventType.REPAINT);
  }

  @Override
  public Rectangle2D getSelectionBounds(boolean applyZoom) {
    if (currentProject.emptySelection()) {
      return null;
    }

    int minX = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxY = Integer.MIN_VALUE;
    for (IDIYComponent<?> c : currentProject.getSelection()) {
      ComponentArea compArea = drawingManager.getComponentArea(c);
      if (compArea != null && compArea.getOutlineArea() != null) {
        Rectangle rect = compArea.getOutlineArea().getBounds();
        if (rect.x < minX) {
          minX = rect.x;
        }
        if (rect.x + rect.width > maxX) {
          maxX = rect.x + rect.width;
        }
        if (rect.y < minY) {
          minY = rect.y;
        }
        if (rect.y + rect.height > maxY) {
          maxY = rect.y + rect.height;
        }
      } else if (currentProject.contains(c)) {
        LOG.debug("Area is null for {} of type {}", c.getName(), c.getClass().getName());
      }
    }

    if (App.getBoolean(Key.EXTRA_SPACE, true)) {
      double extraSpace = drawingManager.getExtraSpace(currentProject);
      minX += extraSpace;
      maxX += extraSpace;
      minY += extraSpace;
      maxY += extraSpace;
    }

    if (drawingManager.getZoomLevel() != 1 && applyZoom) {
      minX *= drawingManager.getZoomLevel();
      maxX *= drawingManager.getZoomLevel();
      minY *= drawingManager.getZoomLevel();
      maxY *= drawingManager.getZoomLevel();
    }
    return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
  }

  @Override
  public void nudgeSelection(Size xOffset, Size yOffset, boolean includeStuckComponents) {
    if (currentProject.emptySelection()) {
      return;
    }

    LOG.trace("nudgeSelection({}, {}, {})", xOffset, yOffset, includeStuckComponents);
    Map<IDIYComponent<?>, Set<Integer>> controlPointMap =
        new HashMap<IDIYComponent<?>, Set<Integer>>();
    // If there aren't any control points, try to add all the selected
    // components with all their control points. That will allow the
    // user to drag the whole components.
    for (IDIYComponent<?> c : currentProject.getSelection()) {
      Set<Integer> pointIndices = new HashSet<Integer>();
      if (c.getControlPointCount() > 0) {
        for (int i = 0; i < c.getControlPointCount(); i++) {
          pointIndices.add(i);
        }
        controlPointMap.put(c, pointIndices);
      }
    }
    if (controlPointMap.isEmpty()) {
      return;
    }

    if (includeStuckComponents) {
      includeStuckComponents(controlPointMap);
    }

    int dx = (int) xOffset.convertToPixels();
    int dy = (int) yOffset.convertToPixels();

    Project oldProject = currentProject.clone();
    moveComponents(controlPointMap, dx, dy, false);
    dispatchMessage(
        EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Move Selection");
    dispatchMessage(EventType.REPAINT);
    //drawingManager.clearContinuityArea();
  }

  @Override
  public void dragStarted(Point point, int dragAction, boolean forceSelectionRect) {
    LOG.trace(
        "dragStarted({}, {}, {})",
        point,
        dragActionToString(dragAction),
        forceSelectionRect);
    if (instantiationManager.getComponentTypeSlot() != null) {
      LOG.debug("Cannot start drag because a new component is being created.");
      mouseClicked(
          point,
          MouseEvent.BUTTON1,
          dragAction == DnDConstants.ACTION_COPY,
          dragAction == DnDConstants.ACTION_LINK,
          dragAction == DnDConstants.ACTION_MOVE,
          1);
      return;
    }
    if (App.getBoolean(Key.HIGHLIGHT_CONTINUITY_AREA, false)) {
      LOG.debug("Cannot start drag in highlight continuity mode.");
      return;
    }
    this.dragInProgress = true;
    this.dragAction = dragAction;
    this.preDragProject = currentProject.clone();
    Point scaledPoint = scalePoint(point);
    this.previousDragPoint = scaledPoint;
    List<IDIYComponent<?>> components =
        (forceSelectionRect ? null : findComponentsAtScaled(scaledPoint));
    if (!this.controlPointMap.isEmpty()) {
      // If we're dragging control points, reset selection.
      LOG.trace("We're dragging control points, reset selection.");
      currentProject.setSelection(new ArrayList<IDIYComponent<?>>(this.controlPointMap.keySet()));
      updateSelection();
      dispatchMessage(EventType.REPAINT);
    } else if (components == null || components.isEmpty()) {
      // If there are no components are under the cursor, reset selection.
      LOG.trace("There are no components under the cursor, reset selection.");
      currentProject.clearSelection();
      dispatchMessage(EventType.REPAINT);
    } else {
      // Take the last component, i.e. the top order component.
      IDIYComponent<?> component = components.get(0);
      LOG.trace("Selected {}", component.getIdentifier());
      // If the component under the cursor is not already selected, make
      // it into the only selected component.
      if (!currentProject.inSelection(component)) {
        currentProject.logTraceSelection();
        LOG.trace(
            "{} was not selected, making it the only selected component",
            component.getIdentifier());
        currentProject.setSelection(
            new ArrayList<IDIYComponent<?>>(findAllGroupedComponents(component)));
        updateSelection();
        dispatchMessage(EventType.REPAINT);
      }
      // If there aren't any control points, try to add all the selected
      // components with all their control points. That will allow the
      // user to drag the whole components.
      for (IDIYComponent<?> c : currentProject.getSelection()) {
        Set<Integer> pointIndices = new HashSet<Integer>();
        if (c.getControlPointCount() > 0) {
          for (int i = 0; i < c.getControlPointCount(); i++) {
            pointIndices.add(i);
          }
          this.controlPointMap.put(c, pointIndices);
        }
      }
      // Expand control points to include all stuck components.
      boolean sticky = App.stickyPoints();
      if (this.dragAction == IPlugInPort.DND_TOGGLE_STICKY) {
        LOG.trace("Setting sticky to {}", !sticky);
        sticky = !sticky;
      }
      if (sticky) {
        LOG.trace("Including stuck components");
        includeStuckComponents(controlPointMap);
      }
    }
  }

  @Override
  public void dragActionChanged(int dragAction) {
    LOG.trace("dragActionChanged({})", dragActionToString(dragAction));
    this.dragAction = dragAction;
  }

  /**
   * Finds any components that are stuck to one of the components already in the map.
   *
   * @param controlPointMap
   */
  private void includeStuckComponents(Map<IDIYComponent<?>, Set<Integer>> controlPointMap) {
    int oldSize = controlPointMap.size();
    LOG.trace("includeStuckComponents: Expanding selected component map");
    for (IDIYComponent<?> component : currentProject.getComponents()) {
      // Check if there's a control point in the current selection
      // that matches with one of its control points.
      for (int i = 0; i < component.getControlPointCount(); i++) {
        // Do not process a control point if it's already in the map and
        // if it's locked.
        if (!(controlPointMap.containsKey(component)
              && controlPointMap.get(component).contains(i))
            && !isComponentLocked(component)
            && isComponentVisible(component)) {
          if (component.isControlPointSticky(i)) {
            boolean componentMatches = false;
            for (Map.Entry<IDIYComponent<?>, Set<Integer>> entry : controlPointMap.entrySet()) {
              if (componentMatches) {
                break;
              }
              for (Integer j : entry.getValue()) {
                Point firstPoint = component.getControlPoint(i);
                if (entry.getKey().isControlPointSticky(j)) {
                  Point secondPoint = entry.getKey().getControlPoint(j);
                  // If they are close enough we can consider
                  // them matched.
                  if (firstPoint.distance(secondPoint) < DrawingManager.CONTROL_POINT_SIZE) {
                    componentMatches = true;
                    break;
                  }
                }
              }
            }
            if (componentMatches) {
              LOG.trace("Including component {}", component.getIdentifier());
              Set<Integer> indices = new HashSet<Integer>();
              // For stretchable components just add the matching
              // component. Otherwise, add all control points.
              if (component.canPointMoveFreely(i)) {
                indices.add(i);
              } else {
                for (int k = 0; k < component.getControlPointCount(); k++) {
                  indices.add(k);
                }
              }
              if (controlPointMap.containsKey(component)) {
                controlPointMap.get(component).addAll(indices);
              } else {
                controlPointMap.put(component, indices);
              }
            }
          }
        }
      }
    }
    int newSize = controlPointMap.size();
    // As long as we're adding new components, do another iteration.
    if (newSize > oldSize) {
      LOG.trace("Component count changed, trying one more time.");
      includeStuckComponents(controlPointMap);
    } else {
      LOG.trace("Component count didn't change, done with expanding.");
    }
  }

  private boolean isSnapToGrid() {
    boolean snapToGrid = App.snapToGrid();
    if (this.dragAction == IPlugInPort.DND_TOGGLE_SNAP) {
      snapToGrid = !snapToGrid;
    }
    return snapToGrid;
  }

  @Override
  public boolean dragOver(Point point) {
    if (point == null || App.getBoolean(Key.HIGHLIGHT_CONTINUITY_AREA, false)) {
      return false;
    }
    Point scaledPoint = scalePoint(point);
    LOG.trace("dragOver({}) scaledPoint {}", point, scaledPoint);
    if (!controlPointMap.isEmpty()) {
      // We're dragging control point(s).
      int dx = (scaledPoint.x - previousDragPoint.x);
      int dy = (scaledPoint.y - previousDragPoint.y);

      Point actualD = moveComponents(this.controlPointMap, dx, dy, isSnapToGrid());
      if (actualD == null) return true;

      previousDragPoint.translate(actualD.x, actualD.y);
    } else if (currentProject.emptySelection()
               && instantiationManager.getComponentTypeSlot() == null
               && previousDragPoint != null) {
      // If there's no selection, the only thing to do is update the
      // selection rectangle and refresh.
      Rectangle oldSelectionRect = (selectionRect == null ? null : new Rectangle(selectionRect));
      this.selectionRect = Utils.createRectangle(scaledPoint, previousDragPoint);
      if (selectionRect.equals(oldSelectionRect)) {
        return true;
      }
      // dispatchMessage(EventType.SELECTION_RECT_CHANGED,
      // selectionRect);
    } else if (instantiationManager.getComponentSlot() != null) {
      this.previousScaledPoint = scalePoint(point);
      instantiationManager.updateSingleClick(
          previousScaledPoint, isSnapToGrid(), currentProject.getGridSpacing());
    }
    dispatchMessage(EventType.REPAINT);
    return true;
  }

  private Point moveComponents(
      Map<IDIYComponent<?>, Set<Integer>> controlPointMap, int dx, int dy, boolean snapToGrid) {
    // After we make the transfer and snap to grid, calculate actual dx
    // and dy. We'll use them to translate the previous drag point.
    int actualDx = 0;
    int actualDy = 0;
    // For each component, do a simulation of the move to see if any of
    // them will overlap or go out of bounds.

    boolean useExtraSpace = App.getBoolean(Key.EXTRA_SPACE, true);
    Dimension d = drawingManager.getCanvasDimensions(currentProject, 1d, useExtraSpace);
    double extraSpace = useExtraSpace ? drawingManager.getExtraSpace(currentProject) : 0;

    if (controlPointMap.size() == 1) {
      Map.Entry<IDIYComponent<?>, Set<Integer>> entry =
          controlPointMap.entrySet().iterator().next();

      Point firstPoint =
          entry.getKey().getControlPoint(entry.getValue().toArray(new Integer[] {})[0]);
      Point testPoint = new Point(firstPoint);
      testPoint.translate(dx, dy);
      if (snapToGrid) {
        CalcUtils.snapPointToGrid(testPoint, currentProject.getGridSpacing());
      }

      actualDx = testPoint.x - firstPoint.x;
      actualDy = testPoint.y - firstPoint.y;
    } else if (snapToGrid) {
      actualDx = CalcUtils.roundToGrid(dx, currentProject.getGridSpacing());
      actualDy = CalcUtils.roundToGrid(dy, currentProject.getGridSpacing());
    } else {
      actualDx = dx;
      actualDy = dy;
    }

    if (actualDx == 0 && actualDy == 0) {
      // Nothing to move.
      return null;
    }

    // Validate if moving can be done.
    for (Map.Entry<IDIYComponent<?>, Set<Integer>> entry : controlPointMap.entrySet()) {
      IDIYComponent<?> component = entry.getKey();
      Point[] controlPoints = new Point[component.getControlPointCount()];
      for (int index = 0; index < component.getControlPointCount(); index++) {
        controlPoints[index] = new Point(component.getControlPoint(index));
        // When the first point is moved, calculate how much it
        // actually moved after snapping.
        if (entry.getValue().contains(index)) {
          controlPoints[index].translate(actualDx, actualDy);
          controlPoints[index].translate((int) extraSpace, (int) extraSpace);
          if (controlPoints[index].x < 0
              || controlPoints[index].y < 0
              || controlPoints[index].x > d.width
              || controlPoints[index].y > d.height) {
            // At least one control point went out of bounds.
            return null;
          }
        }
        // For control points that may overlap, just write null,
        // we'll ignore them later.
        if (component.canControlPointOverlap(index)) {
          controlPoints[index] = null;
        }
      }

      for (int i = 0; i < controlPoints.length - 1; i++) {
        for (int j = i + 1; j < controlPoints.length; j++) {
          if (controlPoints[i] != null
              && controlPoints[j] != null
              && controlPoints[i].equals(controlPoints[j])) {
            // Control points collision detected, cannot make
            // this move.
            return null;
          }
        }
      }
    }

    // Update all points to new location.
    for (Map.Entry<IDIYComponent<?>, Set<Integer>> entry : controlPointMap.entrySet()) {
      IDIYComponent<?> c = entry.getKey();
      drawingManager.invalidateComponent(c);
      for (Integer index : entry.getValue()) {
        Point p = new Point(c.getControlPoint(index));
        p.translate(actualDx, actualDy);
        c.setControlPoint(p, index);
      }
    }
    return new Point(actualDx, actualDy);
  }

  @Override
  public void rotateSelection(int direction) {
    if (!currentProject.emptySelection()) {
      LOG.trace("Rotating selected components");
      Project oldProject = currentProject.clone();
      rotateComponents(currentProject.getSelection(), direction, isSnapToGrid());
      dispatchMessage(
          EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Rotate Selection");
      // drawingManager.clearContinuityArea();
      dispatchMessage(EventType.REPAINT);
    }
  }

  /** @param direction 1 for clockwise, -1 for counter-clockwise */
  @SuppressWarnings("unchecked")
  private void rotateComponents(
      Collection<IDIYComponent<?>> components, int direction, boolean snapToGrid) {
    Point center = getCenterOf(components, snapToGrid);

    boolean canRotate = true;
    for (IDIYComponent<?> component : currentProject.getSelection()) {
      ComponentType type =
          ComponentProcessor.extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());
      if (type.getTransformer() == null || !type.getTransformer().canRotate(component)) {
        canRotate = false;
        break;
      }
    }

    if (!canRotate
        && !userConfirmed(getMsg("unrotatable-components"), getMsg("mirror-selection"))) {
      return;
    }

    for (IDIYComponent<?> component : currentProject.getSelection()) {
      ComponentType type =
          ComponentProcessor.extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());
      if (type.getTransformer() != null && type.getTransformer().canRotate(component)) {
        drawingManager.invalidateComponent(component);
        type.getTransformer().rotate(component, center, direction);
      }
    }
  }

  @Override
  public void mirrorSelection(int direction) {
    if (!currentProject.emptySelection()) {
      LOG.trace("Mirroring selected components");
      Project oldProject = currentProject.clone();

      mirrorComponents(currentProject.getSelection(), direction, isSnapToGrid());

      dispatchMessage(
          EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Mirror Selection");
      dispatchMessage(EventType.REPAINT);
      // drawingManager.clearContinuityArea();
    }
  }

  @SuppressWarnings("unchecked")
  private void mirrorComponents(
      Collection<IDIYComponent<?>> components, int direction, boolean snapToGrid) {
    Point center = getCenterOf(components, snapToGrid);

    boolean canMirror = true;
    boolean changesCircuit = false;
    for (IDIYComponent<?> component : components) {
      ComponentType type =
          ComponentProcessor.extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());
      if (type.getTransformer() == null || !type.getTransformer().canMirror(component)) {
        canMirror = false;
        break;
      }
      if (type.getTransformer() != null && type.getTransformer().mirroringChangesCircuit())
        changesCircuit = true;
    }

    if (!canMirror
        && !userConfirmed(getMsg("unmirrorable-components"), getMsg("mirror-selection"))) {
      return;
    }

    if (changesCircuit
        && !userConfirmed(getMsg("confirm-mirroring"), getMsg("mirror-selection"))) {
      return;
    }

    for (IDIYComponent<?> component : components) {
      ComponentType type =
          ComponentProcessor.extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());
      drawingManager.invalidateComponent(component);
      if (type.getTransformer() != null && type.getTransformer().canMirror(component)) {
        type.getTransformer().mirror(component, center, direction);
      }
    }
  }

  private Point getCenterOf(Collection<IDIYComponent<?>> components, boolean snapToGrid) {
    // Determine center of rotation
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    for (IDIYComponent<?> component : components) {
      for (int i = 0; i < component.getControlPointCount(); i++) {
        Point p = component.getControlPoint(i);
        if (minX > p.x) {
          minX = p.x;
        }
        if (maxX < p.x) {
          maxX = p.x;
        }
        if (minY > p.y) {
          minY = p.y;
        }
        if (maxY < p.y) {
          maxY = p.y;
        }
      }
    }
    int centerX = (maxX + minX) / 2;
    int centerY = (maxY + minY) / 2;

    if (snapToGrid) {
      centerX = CalcUtils.roundToGrid(centerX, this.currentProject.getGridSpacing());
      centerY = CalcUtils.roundToGrid(centerY, this.currentProject.getGridSpacing());
    }

    return new Point(centerX, centerY);
  }

  @Override
  public void dragEnded(Point point) {
    if (!dragInProgress && instantiationManager.getComponentSlot() == null) {
      return;
    }

    Point scaledPoint = scalePoint(point);
    LOG.trace("dragEnded({}) scaled point {}", point, scaledPoint);

    if (currentProject.emptySelection()) {
      // If there's no selection finalize selectionRect and see which
      // components intersect with it.
      if (scaledPoint != null) {
        this.selectionRect = Utils.createRectangle(scaledPoint, previousDragPoint);
      }
      List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
      if (!App.getBoolean(Key.HIGHLIGHT_CONTINUITY_AREA, false))
        for (IDIYComponent<?> component : currentProject.getComponents()) {
          if (!isComponentLocked(component) && isComponentVisible(component)) {
            ComponentArea area = drawingManager.getComponentArea(component);
            if ((area != null && area.getOutlineArea() != null)
                && (selectionRect != null)
                && area.getOutlineArea().intersects(selectionRect)) {
              newSelection.addAll(findAllGroupedComponents(component));
            }
          }
        }
      selectionRect = null;
      currentProject.setSelection(newSelection);
      updateSelection();
    } else if (instantiationManager.getComponentSlot() != null) {
      preDragProject = currentProject.clone();
      addPendingComponentsToProject(scaledPoint,
                                    instantiationManager.getComponentTypeSlot(),
                                    null);
    } else {
      updateSelection();
    }

    // There is selection, so we need to finalize the drag&drop
    // operation.
    if (!preDragProject.equals(currentProject)) {
      dispatchMessage(
          EventType.PROJECT_MODIFIED, preDragProject, currentProject.clone(), "Drag");
      // drawingManager.clearContinuityArea();
      projectFileManager.notifyFileChange();
    }
    dispatchMessage(EventType.REPAINT);
    dragInProgress = false;
  }

  @Override
  public void pasteComponents(Collection<IDIYComponent<?>> components, boolean autoGroup) {
    LOG.trace("pasteComponents({}, {})", components, autoGroup);
    instantiationManager.pasteComponents(
        components,
        this.previousScaledPoint,
        isSnapToGrid(),
        currentProject.getGridSpacing(),
        autoGroup,
        this.currentProject);
    dispatchMessage(EventType.REPAINT);
    dispatchMessage(
        EventType.SLOT_CHANGED,
        instantiationManager.getComponentTypeSlot(),
        instantiationManager.getFirstControlPoint());
  }

  @SuppressWarnings("unchecked")
  @Override
  public void duplicateSelection() {
    LOG.trace("duplicateSelection()");
    if (currentProject.emptySelection()) {
      LOG.debug("Nothing to duplicate");
      return;
    }
    Project oldProject = currentProject.clone();
    Set<IDIYComponent<?>> newSelection = new HashSet<IDIYComponent<?>>();

    int grid = (int) currentProject.getGridSpacing().convertToPixels();
    for (IDIYComponent<?> component : currentProject.getSelection()) {
      try {
        IDIYComponent<?> cloned = component.clone();
        ComponentType componentType =
            ComponentProcessor.extractComponentTypeFrom(
                (Class<? extends IDIYComponent<?>>) cloned.getClass());
        cloned.setName(instantiationManager.createUniqueName(componentType,
                                                             currentProject.getComponents()));
        newSelection.add(cloned);
        for (int i = 0; i < component.getControlPointCount(); i++) {
          Point p = component.getControlPoint(i);
          Point newPoint = new Point(p.x + grid, p.y + grid);
          cloned.setControlPoint(newPoint, i);
        }
        currentProject.getComponents().add(cloned);
      } catch (Exception e) {
        LOG.error("duplicateSelection() something went wrong", e);
        throw new RuntimeException(e);
      }
    }

    currentProject.setSelection(newSelection);
    updateSelection();

    dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Duplicate");
    // drawingManager.clearContinuityArea();
    projectFileManager.notifyFileChange();
    dispatchMessage(EventType.REPAINT);
  }

  @Override
  public void deleteSelectedComponents() {
    LOG.trace("deleteSelectedComponents()");
    if (currentProject.emptySelection()) {
      LOG.debug("deleteSelectedComponents(): Nothing to delete");
      return;
    }

    Project oldProject = currentProject.clone();
    // Remove selected components from any groups.
    currentProject.ungroupSelection();
    // Remove from area map.
    for (IDIYComponent<?> component : currentProject.getSelection()) {
      drawingManager.invalidateComponent(component);
    }
    // Finally, remove the components themselves.
    currentProject.removeSelection();
    dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Delete");
    // drawingManager.clearContinuityArea();
    projectFileManager.notifyFileChange();
    dispatchMessage(EventType.REPAINT);
  }

  @Override
  public void setSelectionDefaultPropertyValue(String propertyName, Object value) {
    LOG.trace("setSelectionDefaultPropertyValue({}, {})", propertyName, value);
    for (IDIYComponent<?> component : currentProject.getSelection()) {
      String className = component.getClass().getName();
      LOG.debug("Default property value set for {}:{}", className, propertyName);
      App.putValue(DEFAULTS_KEY_PREFIX + className + ":" + propertyName, value);
    }
  }

  @Override
  public void setDefaultPropertyValue(Class<?> clazz, String propertyName, Object value) {
    LOG.trace(
        "setDefaultPropertyValue({}, {}, {}) default set for {}:{}",
        clazz.getName(),
        propertyName,
        value,
        Project.class.getName(),
        propertyName);
    App.putValue(DEFAULTS_KEY_PREFIX + clazz.getName() + ":" + propertyName, value);
  }

  @Override
  public void setMetric(boolean isMetric) {
    ConfigurationManager.putValue(Key.METRIC, isMetric);
  }

  @Override
  public void groupSelectedComponents() {
    LOG.trace("groupSelectedComponents()");
    Project oldProject = currentProject.clone();
    currentProject.groupSelection();
    // Notify the listeners.
    dispatchMessage(EventType.REPAINT);
    if (!oldProject.equals(currentProject)) {
      dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Group");
      projectFileManager.notifyFileChange();
    }
  }

  @Override
  public void ungroupSelectedComponents() {
    LOG.trace("ungroupSelectedComponents()");
    Project oldProject = currentProject.clone();
    currentProject.ungroupSelection();
    // Notify the listeners.
    dispatchMessage(EventType.REPAINT);
    if (!oldProject.equals(currentProject)) {
      dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Ungroup");
      projectFileManager.notifyFileChange();
    }
  }

  @Override
  public void setLayerLocked(int layerZOrder, boolean locked) {
    LOG.trace("setLayerLocked({}, {})", layerZOrder, locked);
    Project oldProject = currentProject.clone();
    if (locked) {
      currentProject.getLockedLayers().add(layerZOrder);
    } else {
      currentProject.getLockedLayers().remove(layerZOrder);
    }
    currentProject.clearSelection();
    dispatchMessage(EventType.REPAINT);
    dispatchMessage(EventType.LAYER_STATE_CHANGED, currentProject.getLockedLayers());
    if (!oldProject.equals(currentProject)) {
      dispatchMessage(
          EventType.PROJECT_MODIFIED,
          oldProject,
          currentProject.clone(),
          locked ? "Lock Layer" : "Unlock Layer");
      projectFileManager.notifyFileChange();
    }
  }

  @Override
  public void setLayerVisibility(int layerZOrder, boolean visible) {
    LOG.trace("setLayerVisibility({}, {})", layerZOrder, visible);
    Project oldProject = currentProject.clone();
    if (visible) {
      currentProject.getHiddenLayers().remove(layerZOrder);
    } else {
      currentProject.getHiddenLayers().add(layerZOrder);
    }
    currentProject.clearSelection();
    dispatchMessage(EventType.REPAINT);
    dispatchMessage(EventType.LAYER_VISIBILITY_CHANGED, currentProject.getHiddenLayers());
    if (!oldProject.equals(currentProject)) {
      dispatchMessage(
          EventType.PROJECT_MODIFIED,
          oldProject,
          currentProject.clone(),
          visible ? "Show Layer" : "Hide Layer");
      projectFileManager.notifyFileChange();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void sendSelectionToBack() {
    LOG.trace("sendSelectionToBack()");
    int forceConfirmation = -1;
    Project oldProject = currentProject.clone();

    /* sort the selection in the reversed Z-order to preserve the
       order after moving to the back */
    List<IDIYComponent<?>> selection =
        new ArrayList<IDIYComponent<?>>(currentProject.getSelection());
    Collections.sort(
        selection,
        new Comparator<IDIYComponent<?>>() {

          @Override
          public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
            return Integer.compare(
                currentProject.getComponents().indexOf(o2),
                currentProject.getComponents().indexOf(o1));
          }
        });

    for (IDIYComponent<?> component : selection) {
      ComponentType componentType =
          ComponentProcessor.extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());
      int index = currentProject.getComponents().indexOf(component);
      if (index < 0) {
        LOG.error("Component {} not found in the project!", component.getIdentifier());
        // TODO throw exception? this should definitely not happen //ola 20200113
      } else
        while (index > 0) {
          IDIYComponent<?> componentBefore = currentProject.getComponents().get(index - 1);
          if (!currentProject.inSelection(componentBefore)) {
            ComponentType componentBeforeType =
                ComponentProcessor.extractComponentTypeFrom(
                    (Class<? extends IDIYComponent<?>>) componentBefore.getClass());
            if (!componentType.isFlexibleZOrder()
                && (Math.round(componentBeforeType.getZOrder())
                    < Math.round(componentType.getZOrder()))
                //&& forceConfirmation != IView.YES_OPTION
                && !userConfirmed(getMsg("bottom-reached"),
                                  getMsg("send-selection-to-back"))) {
              break;
            }
          }
          Collections.swap(currentProject.getComponents(), index, index - 1);
          index--;
        }
    }
    if (!oldProject.equals(currentProject)) {
      dispatchMessage(
          EventType.PROJECT_MODIFIED,
          oldProject,
          currentProject.clone(),
          "Send to Back");
      projectFileManager.notifyFileChange();
      dispatchMessage(EventType.REPAINT);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void bringSelectionToFront() {
    LOG.trace("bringSelectionToFront()");
    int forceConfirmation = -1;
    Project oldProject = currentProject.clone();

    // sort the selection in Z-order
    List<IDIYComponent<?>> selection =
        new ArrayList<IDIYComponent<?>>(currentProject.getSelection());
    Collections.sort(
        selection,
        new Comparator<IDIYComponent<?>>() {

          @Override
          public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
            return Integer.compare(
                currentProject.getComponents().indexOf(o1),
                currentProject.getComponents().indexOf(o2));
          }
        });

    for (IDIYComponent<?> component : selection) {
      ComponentType componentType =
          ComponentProcessor.extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());
      int index = currentProject.getComponents().indexOf(component);
      if (index < 0) {
        LOG.warn("Component {} not found in the project", component.getIdentifier());
      } else
        while (index < currentProject.getComponents().size() - 1) {
          IDIYComponent<?> componentAfter = currentProject.getComponents().get(index + 1);
          if (!currentProject.inSelection(componentAfter)) {
            ComponentType componentAfterType =
                ComponentProcessor.extractComponentTypeFrom(
                    (Class<? extends IDIYComponent<?>>) componentAfter.getClass());
            if (!componentType.isFlexibleZOrder()
                && (Math.round(componentAfterType.getZOrder())
                    > Math.round(componentType.getZOrder()))
                // && forceConfirmation != IView.YES_OPTION
                && !userConfirmed(getMsg("top-reached"), getMsg("bring-selection-to-front"))) {
              break;
            }
          }
          Collections.swap(currentProject.getComponents(), index, index + 1);
          index++;
        }
    }
    if (!oldProject.equals(currentProject)) {
      dispatchMessage(
          EventType.PROJECT_MODIFIED,
          oldProject,
          currentProject.clone(),
          "Bring to Front");
      projectFileManager.notifyFileChange();
      dispatchMessage(EventType.REPAINT);
    }
  }

  @Override
  public void refresh() {
    LOG.trace("refresh()");
    dispatchMessage(EventType.REPAINT);
  }

  @Override
  public Theme getSelectedTheme() {
    return drawingManager.getTheme();
  }

  @Override
  public void setSelectedTheme(Theme theme) {
    drawingManager.setTheme(theme);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void renumberSelectedComponents(final boolean xAxisFirst) {
    if (currentProject.emptySelection()) {
      LOG.debug("renumberSelectedComponents({}) no selection", xAxisFirst);
      return;
    }
    LOG.trace("renumberSelectedComponents({})", xAxisFirst);
    Project oldProject = currentProject.clone();
    List<IDIYComponent<?>> components =
        new ArrayList<IDIYComponent<?>>(currentProject.getSelection());
    // Sort components by their location.
    Collections.sort(
        components,
        new Comparator<IDIYComponent<?>>() {

          @Override
          public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
            int sumX1 = 0;
            int sumY1 = 0;
            int sumX2 = 0;
            int sumY2 = 0;
            for (int i = 0; i < o1.getControlPointCount(); i++) {
              sumX1 += o1.getControlPoint(i).getX();
              sumY1 += o1.getControlPoint(i).getY();
            }
            for (int i = 0; i < o2.getControlPointCount(); i++) {
              sumX2 += o2.getControlPoint(i).getX();
              sumY2 += o2.getControlPoint(i).getY();
            }
            sumX1 /= o1.getControlPointCount();
            sumY1 /= o1.getControlPointCount();
            sumX2 /= o2.getControlPointCount();
            sumY2 /= o2.getControlPointCount();

            if (xAxisFirst) {
              if (sumY1 < sumY2) {
                return -1;
              } else if (sumY1 > sumY2) {
                return 1;
              } else {
                if (sumX1 < sumX2) {
                  return -1;
                } else if (sumX1 > sumX2) {
                  return 1;
                }
              }
            } else {
              if (sumX1 < sumX2) {
                return -1;
              } else if (sumX1 > sumX2) {
                return 1;
              } else {
                if (sumY1 < sumY2) {
                  return -1;
                } else if (sumY1 > sumY2) {
                  return 1;
                }
              }
            }
            return 0;
          }
        });
    // Clear names.
    for (IDIYComponent<?> component : components) {
      component.setName("");
    }
    // Assign new ones.
    for (IDIYComponent<?> component : components) {
      component.setName(
          instantiationManager.createUniqueName(
              ComponentProcessor.extractComponentTypeFrom(
                  (Class<? extends IDIYComponent<?>>) component.getClass()),
              currentProject.getComponents()));
    }

    dispatchMessage(
        EventType.PROJECT_MODIFIED,
        oldProject,
        currentProject.clone(),
        "Renumber selection");
    projectFileManager.notifyFileChange();
    dispatchMessage(EventType.REPAINT);
  }

  /**
     Update selection.
   */
  public void updateSelection() {
    // this.selectedComponents = new HashSet<IDIYComponent<?>>(newSelection);
    Map<IDIYComponent<?>, Set<Integer>> controlPointMap =
        new HashMap<IDIYComponent<?>, Set<Integer>>();
    for (IDIYComponent<?> component : currentProject.getSelection()) {
      Set<Integer> indices = new HashSet<Integer>();
      for (int i = 0; i < component.getControlPointCount(); i++) {
        indices.add(i);
      }
      controlPointMap.put(component, indices);
    }
    if (App.stickyPoints()) {
      includeStuckComponents(controlPointMap);
    }
    dispatchMessage(
        EventType.SELECTION_CHANGED,
        currentProject.getSelection(),
        controlPointMap.keySet());
  }

  /**
     Expand selection.
  */
  @SuppressWarnings("unchecked")
  @Override
  public void expandSelection(ExpansionMode expansionMode) {
    LOG.trace("expandSelection({})", expansionMode);
    List<IDIYComponent<?>> newSelection =
        new ArrayList<IDIYComponent<?>>(currentProject.getSelection());
    List<Netlist> netlists = extractNetlists(false);
    List<Set<IDIYComponent<?>>> allGroups = NetlistAnalyzer.extractComponentGroups(netlists);
    // Find control points of all selected components and all types
    Set<String> selectedNamePrefixes = new HashSet<String>();
    if (expansionMode == ExpansionMode.SAME_TYPE) {
      for (IDIYComponent<?> component : currentProject.getSelection()) {
        selectedNamePrefixes.add(
            ComponentProcessor.extractComponentTypeFrom(
                (Class<? extends IDIYComponent<?>>) component.getClass()).getNamePrefix());
      }
    }
    // Now try to find components that intersect with at least one component
    // in the pool.
    for (IDIYComponent<?> component : getCurrentProject().getComponents()) {
      // no need to consider it, it's already in the selection
      if (newSelection.contains(component)) continue;
      // construct a list of component groups that contain the current component
      List<Set<IDIYComponent<?>>> componentGroups = new ArrayList<Set<IDIYComponent<?>>>();
      for (Set<IDIYComponent<?>> e : allGroups) {
        if (e.contains(component)) {
          componentGroups.add(e);
        }
      }
      if (componentGroups.isEmpty()) continue;
      // Skip already selected components or ones that cannot be stuck to
      // other components.
      boolean matches = false;
      outer:
      for (IDIYComponent<?> selectedComponent : currentProject.getSelection()) {
        // try to find the selectedComponent in one of the groups
        for (Set<IDIYComponent<?>> s : componentGroups)
          if (s.contains(selectedComponent)) {
            matches = true;
            break outer;
          }
      }

      if (matches) {
        switch (expansionMode) {
          case ALL:
          case IMMEDIATE:
            newSelection.add(component);
            break;
          case SAME_TYPE:
            if (selectedNamePrefixes.contains(ComponentProcessor.extractComponentTypeFrom(
                    (Class<? extends IDIYComponent<?>>) component.getClass()).getNamePrefix())) {
              newSelection.add(component);
            }
            break;
        }
      }
    }

    int oldSize = currentProject.getSelection().size();
    currentProject.setSelection(newSelection);
    // Go deeper if possible.
    if (newSelection.size() > oldSize && expansionMode != ExpansionMode.IMMEDIATE) {
      expandSelection(expansionMode);
    }
    dispatchMessage(EventType.REPAINT);
  }

  /**
   * Finds all components that are grouped with the specified
   * component. This should be called any time components are added or
   * removed from the selection.
   *
   * @param component
   * @return set of all components that belong to the same group with
   *     the specified component. At the minimum, set contains that
   *     single component.
   */
  private static Set<IDIYComponent<?>> findAllGroupedComponents(
      Project p, IDIYComponent<?> component) {
    Set<IDIYComponent<?>> components = new HashSet<IDIYComponent<?>>();
    components.add(component);
    for (Set<IDIYComponent<?>> group : p.getGroups()) {
      if (group.contains(component)) {
        components.addAll(group);
        break;
      }
    }
    return components;
  }

  private Set<IDIYComponent<?>> findAllGroupedComponents(IDIYComponent<?> component) {
    return findAllGroupedComponents(currentProject, component);
  }

  @Override
  public Point2D[] calculateSelectionDimension() {
    if (currentProject.emptySelection()) {
      return null;
    }
    Rectangle2D rect = getSelectionBounds(false);

    double width = rect.getWidth();
    double height = rect.getHeight();

    width /= Constants.PIXELS_PER_INCH;
    height /= Constants.PIXELS_PER_INCH;

    Point2D inSize = new Point2D.Double(width, height);

    width *= SizeUnit.in.getFactor() / SizeUnit.cm.getFactor();
    height *= SizeUnit.in.getFactor() / SizeUnit.cm.getFactor();

    Point2D cmSize = new Point2D.Double(width, height);

    return new Point2D[] {inSize, cmSize};
  }


  /**
   * Adds a component to the project taking z-order into account.
   *
   * @param component
   */
  @SuppressWarnings("unchecked")
  private void addComponent(IDIYComponent<?> component, boolean alowAutoCreate) {
    int index = currentProject.getComponents().size();
    while (index > 0
        && ComponentProcessor.extractComponentTypeFrom(
            (Class<? extends IDIYComponent<?>>) component.getClass())
           .getZOrder()
           < ComponentProcessor.extractComponentTypeFrom(
               (Class<? extends IDIYComponent<?>>) currentProject.getComponents()
               .get(index - 1).getClass()).getZOrder()) {
      index--;
    }
    if (index < currentProject.getComponents().size()) {
      currentProject.getComponents().add(index, component);
    } else {
      currentProject.getComponents().add(component);
    }

    // Check if we should auto-create something.
    for (IAutoCreator creator : this.getAutoCreators()) {
      List<IDIYComponent<?>> newComponents = creator.createIfNeeded(component);
      if (newComponents != null) {
        for (IDIYComponent<?> c : newComponents) addComponent(c, false);
      }
    }
  }

  @Override
  public List<PropertyWrapper> getMutualSelectionProperties() {
    try {
      return ComponentProcessor.getMutualSelectionProperties(currentProject.getSelection());
    } catch (Exception e) {
      LOG.error("Could not get mutual selection properties", e);
      return null;
    }
  }

  private void applyPropertiesToSelection(List<PropertyWrapper> properties) {
    LOG.trace("applyPropertiesToSelection({})", properties);
    Project oldProject = currentProject.clone();
    try {
      for (IDIYComponent<?> component : currentProject.getSelection()) {
        drawingManager.invalidateComponent(component);
        for (PropertyWrapper property : properties) {
          if (property.isChanged()) {
            property.writeTo(component);
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Could not apply selection properties", e);
      App.ui().error("Could not apply changes to the selection. Check the log for details.");
    } finally {
      // Notify the listeners.
      if (!oldProject.equals(currentProject)) {
        dispatchMessage(
            EventType.PROJECT_MODIFIED,
            oldProject,
            currentProject.clone(),
            "Edit Selection");
        // drawingManager.clearContinuityArea();
        projectFileManager.notifyFileChange();
      }
      dispatchMessage(EventType.REPAINT);
    }
  }

  @Override
  public List<PropertyWrapper> getProperties(Object object) {
    List<PropertyWrapper> properties = ComponentProcessor.extractProperties(object.getClass());
    try {
      for (PropertyWrapper property : properties) {
        property.readFrom(object);
      }
    } catch (Exception e) {
      LOG.error("Could not get object properties", e);
      return null;
    }
    Collections.sort(properties, ComparatorFactory.getInstance().getDefaultPropertyComparator());
    return properties;
  }

  @Override
  public void applyProperties(Object obj, List<PropertyWrapper> properties) {
    LOG.trace("applyProperties({}, {})", obj, properties);
    Project oldProject = currentProject.clone();
    try {
      for (PropertyWrapper property : properties) {
        property.writeTo(obj);
      }
    } catch (Exception e) {
      LOG.error("Could not apply properties", e);
      App.ui().error("Could not apply changes. Check the log for details.");
    } finally {
      // Notify the listeners.
      if (!oldProject.equals(currentProject)) {
        dispatchMessage(
            EventType.PROJECT_MODIFIED,
            oldProject, currentProject.clone(),
            "Edit Project");
        // drawingManager.clearContinuityArea();
        projectFileManager.notifyFileChange();
      }
      drawingManager.fireZoomChanged();
    }
  }

  @Override
  public ComponentType getNewComponentTypeSlot() {
    return instantiationManager.getComponentTypeSlot();
  }

  @Override
  public void setNewComponentTypeSlot(
      ComponentType componentType, Template template, boolean forceInstantiate) {
    LOG.trace("setNewComponentSlot({})", componentType == null ? "null" : componentType.getName());
    if (componentType != null && componentType.getInstanceClass() == null) {

      LOG.warn("Cannot set new component type slot for type {}", componentType.getName());
      setNewComponentTypeSlot(null, null, false);
      return;
    }

    // try to find a default template if none is provided
    if (componentType != null && template == null) {
      String defaultTemplate = getDefaultVariant(componentType);
      List<Template> templates = getVariantsFor(componentType);
      if (templates != null && defaultTemplate != null)
        for (Template t : templates) {
          if (t.getName().equals(defaultTemplate)) {
            template = t;
            break;
          }
        }
    }

    try {
      instantiationManager.setComponentTypeSlot(
          componentType, template, currentProject, forceInstantiate);

      if (forceInstantiate) {
        currentProject.setSelection(instantiationManager.getComponentSlot());
        updateSelection();
      } else if (componentType != null) {
        currentProject.clearSelection();
      }

      dispatchMessage(EventType.REPAINT);
      dispatchMessage(
          EventType.SLOT_CHANGED,
          instantiationManager.getComponentTypeSlot(),
          instantiationManager.getFirstControlPoint(),
          forceInstantiate);
    } catch (Exception e) {
      LOG.error("Could not set component type slot", e);
      App.ui().error("Could not set component type slot. Check log for details.");
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void saveSelectedComponentAsVariant(String variantName) {
    LOG.trace("saveSelectedComponentAsVariant({})", variantName);
    if (currentProject.getSelection().size() != 1) {
      throw new RuntimeException("Can only save a single component as a variant at once.");
    }
    // Get first component from selection
    // NOTE: is selection guaranteed to be in order? //ola 20200113
    IDIYComponent<?> component = currentProject.getSelection().iterator().next();
    ComponentType type =
        ComponentProcessor.extractComponentTypeFrom(
            (Class<? extends IDIYComponent<?>>) component.getClass());
    Map<String, List<Template>> variantMap =
        (Map<String, List<Template>>) ConfigurationManager.getObject(Key.TEMPLATES);
    if (variantMap == null) {
      variantMap = new HashMap<String, List<Template>>();
    }
    String key = type.getInstanceClass().getCanonicalName();
    List<Template> variants = variantMap.get(key);
    if (variants == null) {
      variants = new ArrayList<Template>();
      variantMap.put(key, variants);
    }
    List<PropertyWrapper> properties = ComponentProcessor.extractProperties(component.getClass());
    Map<String, Object> values = new HashMap<String, Object>();
    for (PropertyWrapper property : properties) {
      if (property.getName().equalsIgnoreCase("name")) {
        continue;
      }
      try {
        property.readFrom(component);
        values.put(property.getName(), property.getValue());
      } catch (Exception e) {
      }
    }
    List<Point> points = new ArrayList<Point>();

    for (int i = 0; i < component.getControlPointCount(); i++) {
      Point p = new Point(component.getControlPoint(i));
      points.add(p);
    }
    int x = points.iterator().next().x;
    int y = points.iterator().next().y;
    for (Point point : points) {
      point.translate(-x, -y);
    }

    Template template = new Template(variantName, values, points);
    boolean exists = false;
    for (Template t : variants) {
      if (t.getName().equalsIgnoreCase(variantName)) {
        exists = true;
        break;
      }
    }

    if (exists) {
      if (!warnedUserConfirmed(getMsg("confirm-variant-overwrite"), getMsg("save-as-variant"))) {
        return;
      }
      // Delete the existing variant
      Iterator<Template> i = variants.iterator();
      while (i.hasNext()) {
        Template t = i.next();
        if (t.getName().equalsIgnoreCase(variantName)) {
          i.remove();
        }
      }
    }

    variants.add(template);

    if (System.getProperty("org.diylc.WriteStaticVariants", "false").equalsIgnoreCase("true")) {
      if (defaultVariantMap == null) defaultVariantMap = new HashMap<String, List<Template>>();
      // unify default and user-variants
      for (Map.Entry<String, List<Template>> entry : variantMap.entrySet()) {
        if (defaultVariantMap.containsKey(entry.getKey())) {
          defaultVariantMap.get(entry.getKey()).addAll(entry.getValue());
        } else {
          defaultVariantMap.put(entry.getKey(), entry.getValue());
        }
      }
      try {
        Serializer.toFile("variants.xml", defaultVariantMap);
        // no more user variants
        ConfigurationManager.putValue(Key.TEMPLATES, null);
        LOG.trace("Saved default variants");
      } catch (IOException e) {
        LOG.error("Could not save default variants", e);
        // TODO: UI error dialog
        // TODO: propagate exception?
      }
    } else {
      ConfigurationManager.putValue(Key.TEMPLATES, variantMap);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Template> getVariantsFor(ComponentType type) {
    LOG.trace("getVariantsFor({}) Getting variant map from {}",
              type == null ? "null" : type.getName(),
              Key.TEMPLATES);

    List<Template> variants = new ArrayList<Template>();

    if (type != null) {
      // try by class name and then by old category.type format
      List<String> keys = new ArrayList<String>();
      keys.add(type.getInstanceClass().getCanonicalName());
      keys.add(type.getCategory() + "." + type.getName());

      Map<String, List<Template>> variantMap =
          (Map<String, List<Template>>) ConfigurationManager.getObject(Key.TEMPLATES);
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
    }
    return variants;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Template> getVariantsForSelection() {
    if (currentProject.emptySelection()) {
      LOG.error("getVariantsForSelection() No components selected");
      //throw new RuntimeException("No components selected");
      return null;
    }
    ComponentType selectedType = null;
    Iterator<IDIYComponent<?>> iterator = currentProject.getSelection().iterator();
    while (iterator.hasNext()) {
      ComponentType type =
          ComponentProcessor.extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) iterator.next().getClass());
      if (selectedType == null) {
        selectedType = type;
      } else if (selectedType.getInstanceClass() != type.getInstanceClass()) {
        return null;
      }
    }
    return getVariantsFor(selectedType);
  }

  @Override
  public void applyVariantToSelection(Template template) {
    LOG.trace("applyTemplateToSelection({})", template.getName());

    Project oldProject = currentProject.clone();

    for (IDIYComponent<?> component : currentProject.getSelection()) {
      try {
        drawingManager.invalidateComponent(component);
        // this.instantiationManager.loadComponentShapeFromTemplate(component, template);
        this.instantiationManager.fillWithDefaultProperties(component, template);
      } catch (Exception e) {
        LOG.warn("Could not apply templates to " + component.getIdentifier(), e);
      }
    }

    // Notify the listeners.
    if (!oldProject.equals(currentProject)) {
      dispatchMessage(
          EventType.PROJECT_MODIFIED,
          oldProject,
          currentProject.clone(),
          "Edit Selection");
      // drawingManager.clearContinuityArea();
      projectFileManager.notifyFileChange();
    }
    dispatchMessage(EventType.REPAINT);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void deleteVariant(ComponentType type, String templateName) {
    LOG.trace("deleteTemplate({}, {})", type, templateName);
    if (type != null) {
      Map<String, List<Template>> templateMap =
          (Map<String, List<Template>>) ConfigurationManager.getObject(Key.TEMPLATES);
      if (templateMap != null) {
        // try by class name and then by old category.type format
        List<String> keys = new ArrayList<String>();
        keys.add(type.getInstanceClass().getCanonicalName());
        keys.add(type.getCategory() + "." + type.getName());

        for (String key : keys) {
          List<Template> templates = templateMap.get(key);
          if (templates != null) {
            Iterator<Template> i = templates.iterator();
            while (i.hasNext()) {
              Template t = i.next();
              if (t.getName().equalsIgnoreCase(templateName)) {
                i.remove();
              }
            }
          }
        }
      }
      ConfigurationManager.putValue(Key.TEMPLATES, templateMap);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setDefaultVariant(ComponentType type, String templateName) {
    LOG.trace("setTemplateDefault({}, {})", type, templateName);
    Map<String, String> defaultTemplateMap =
        (Map<String, String>) ConfigurationManager.getObject(DEFAULT_TEMPLATES_KEY);
    if (defaultTemplateMap == null) defaultTemplateMap = new HashMap<String, String>();

    // try by class name and then by old category.type format
    String key1 = type.getInstanceClass().getCanonicalName();
    String key2 = type.getCategory() + "." + type.getName();

    if (templateName.equals(defaultTemplateMap.get(key1))
        || templateName.equals(defaultTemplateMap.get(key2))) {
      defaultTemplateMap.remove(key1);
      defaultTemplateMap.remove(key2);
    } else {
      // get rid of legacy key
      defaultTemplateMap.remove(key2);
      defaultTemplateMap.put(key1, templateName);
    }
    ConfigurationManager.putValue(DEFAULT_TEMPLATES_KEY, defaultTemplateMap);
  }

  @SuppressWarnings("unchecked")
  @Override
  public String getDefaultVariant(ComponentType type) {
    Map<String, String> defaultTemplateMap =
        (Map<String, String>) ConfigurationManager.getObject(DEFAULT_TEMPLATES_KEY);
    if (defaultTemplateMap == null) return null;

    String key1 = type.getInstanceClass().getCanonicalName();
    String key2 = type.getCategory() + "." + type.getName();

    if (defaultTemplateMap.containsKey(key1)) return defaultTemplateMap.get(key1);

    return defaultTemplateMap.get(key2);
  }

  private Set<IDIYComponent<?>> getLockedComponents() {
    lockedComponents = getLockedComponents(currentProject);
    return lockedComponents;
  }

  private static Set<IDIYComponent<?>> getLockedComponents(Project p) {
    Set<IDIYComponent<?>> locked = new HashSet<IDIYComponent<?>>();
    for (IDIYComponent<?> component : p.getComponents()) {
      if (isComponentLocked(p, component)) {
        locked.add(component);
      }
    }
    return locked;
  }

  @SuppressWarnings("unchecked")
  private boolean isComponentLocked(IDIYComponent<?> component) {
    return isComponentLocked(currentProject, component);
  }

  @SuppressWarnings("unchecked")
  private static boolean isComponentLocked(Project p, IDIYComponent<?> component) {
    ComponentType componentType =
        ComponentProcessor.extractComponentTypeFrom(
            (Class<? extends IDIYComponent<?>>) component.getClass());
    return p.getLockedLayers().contains((int) Math.round(componentType.getZOrder()));
  }

  /**
   * Scales point from display base to actual base.
   *
   * @param point
   * @return
   */
  private Point scalePoint(Point point) {
    Point p =
        point == null
            ? null
            : new Point(
                (int) (point.x / drawingManager.getZoomLevel()),
                (int) (point.y / drawingManager.getZoomLevel()));

    if (p != null && ConfigurationManager.getBoolean(Key.EXTRA_SPACE, true)) {
      double extraSpace = drawingManager.getExtraSpace(currentProject);
      p.translate((int) (-extraSpace), (int) (-extraSpace));
    }
    return p;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void saveSelectionAsBlock(String blockName) {
    LOG.trace("saveSelectionAsBlock({})", blockName);
    Map<String, List<IDIYComponent<?>>> blocks =
        (Map<String, List<IDIYComponent<?>>>) ConfigurationManager.getObject(Key.BLOCKS);
    if (blocks == null) blocks = new HashMap<String, List<IDIYComponent<?>>>();
    List<IDIYComponent<?>> blockComponents =
        new ArrayList<IDIYComponent<?>>(currentProject.getSelection());
    Collections.sort(
        blockComponents,
        new Comparator<IDIYComponent<?>>() {

          @Override
          public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
            return Integer.compare(
                currentProject.getComponents().indexOf(o1),
                currentProject.getComponents().indexOf(o2));
          }
        });
    blocks.put(blockName, blockComponents);
    ConfigurationManager.putValue(Key.BLOCKS, blocks);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void loadBlock(String blockName) throws InvalidBlockException {
    LOG.trace("loadBlock({})", blockName);
    Map<String, List<IDIYComponent<?>>> blocks =
        (Map<String, List<IDIYComponent<?>>>) ConfigurationManager.getObject(Key.BLOCKS);
    if (blocks != null) {
      Collection<IDIYComponent<?>> components = blocks.get(blockName);
      if (components == null) throw new InvalidBlockException();
      // clear potential control point every time!
      instantiationManager.setPotentialControlPoint(null);
      // clone components
      List<IDIYComponent<?>> clones = new ArrayList<IDIYComponent<?>>();
      List<IDIYComponent<?>> testComponents =
          new ArrayList<IDIYComponent<?>>(currentProject.getComponents());
      for (IDIYComponent<?> c : components)
        try {
          IDIYComponent<?> clone = c.clone();
          clone.setName(
              instantiationManager.createUniqueName(
                  ComponentProcessor.extractComponentTypeFrom(
                      (Class<? extends IDIYComponent<?>>) clone.getClass()),
                  testComponents));
          testComponents.add(clone);
          clones.add(clone);
        } catch (CloneNotSupportedException e) {
          LOG.error("Could not clone component {}", c);
        }
      // paste them to the project
      pasteComponents(clones, true);
    } else throw new InvalidBlockException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void deleteBlock(String blockName) {
    LOG.trace("deleteBlock({})", blockName);
    Map<String, List<IDIYComponent<?>>> blocks =
        (Map<String, List<IDIYComponent<?>>>) ConfigurationManager.getObject(Key.BLOCKS);
    if (blocks != null) {
      blocks.remove(blockName);
      ConfigurationManager.putValue(Key.BLOCKS, blocks);
    }
  }

  @Override
  public double getExtraSpace() {
    if (!ConfigurationManager.getBoolean(Key.EXTRA_SPACE, true)) return 0;

    double extraSpace = drawingManager.getExtraSpace(currentProject);
    boolean metric = ConfigurationManager.getBoolean(Key.METRIC, true);

    extraSpace /= Constants.PIXELS_PER_INCH;

    if (metric) extraSpace *= SizeUnit.in.getFactor() / SizeUnit.cm.getFactor();

    return extraSpace;
  }

  @SuppressWarnings("unchecked")
  @Override
  public int importVariants(String fileName) throws IOException {
    LOG.trace("importVariants({})", fileName);

    VariantPackage pkg = (VariantPackage) Serializer.fromFile(fileName);

    if (pkg == null || pkg.getVariants().isEmpty()) return 0;

    Map<String, List<Template>> variantMap =
        (Map<String, List<Template>>) ConfigurationManager.getObject(Key.TEMPLATES);
    if (variantMap == null) {
      variantMap = new HashMap<String, List<Template>>();
    }

    for (Map.Entry<String, List<Template>> entry : pkg.getVariants().entrySet()) {
      List<Template> templates;
      templates = variantMap.get(entry.getKey());
      if (templates == null) {
        templates = new ArrayList<Template>();
        variantMap.put(entry.getKey(), templates);
      }
      for (Template t : entry.getValue()) {
        templates.add(
            new Template(t.getName() + " [" + pkg.getOwner() + "]",
                         t.getValues(),
                         t.getPoints()));
      }
    }

    ConfigurationManager.putValue(Key.TEMPLATES, variantMap);

    LOG.info("Loaded variants for %d components", pkg.getVariants().size());

    return pkg.getVariants().size();
  }

  @SuppressWarnings("unchecked")
  @Override
  public int importBlocks(String fileName) throws IOException {
    LOG.trace("importBlocks({})", fileName);

    BuildingBlockPackage pkg = (BuildingBlockPackage) Serializer.fromFile(fileName);

    if (pkg == null || pkg.getBlocks().isEmpty()) return 0;

    Map<String, List<IDIYComponent<?>>> blocks =
        (Map<String, List<IDIYComponent<?>>>) ConfigurationManager.getObject(Key.BLOCKS);
    if (blocks == null) {
      blocks = new HashMap<String, List<IDIYComponent<?>>>();
    }

    for (Map.Entry<String, List<IDIYComponent<?>>> entry : pkg.getBlocks().entrySet()) {
      blocks.put(entry.getKey() + " [" + pkg.getOwner() + "]", entry.getValue());
    }

    ConfigurationManager.putValue(Key.BLOCKS, blocks);

    LOG.info("Loaded building blocks for %d components", pkg.getBlocks().size());

    return pkg.getBlocks().size();
  }

  private static boolean upgradedVariants = false;

  @SuppressWarnings("unchecked")
  private synchronized void upgradeVariants() {
    if (upgradedVariants) {
      return;
    }

    upgradedVariants = true;

    LOG.info("upgradeVariants() Checking if variants need to be updated using {}", Key.TEMPLATES);
    Map<String, List<Template>> variantMap =
        (Map<String, List<Template>>) ConfigurationManager.getObject(Key.TEMPLATES);

    if (variantMap == null) {
      return;
    }

    Map<String, ComponentType> typeMap =
        new TreeMap<String, ComponentType>(String.CASE_INSENSITIVE_ORDER);

    LOG.info("upgradeVariants() Getting component types");
    Map<String, List<ComponentType>> componentTypes = getComponentTypes();
    for (Map.Entry<String, List<ComponentType>> entry : componentTypes.entrySet())
      for (ComponentType type : entry.getValue()) {
        typeMap.put(type.getInstanceClass().getCanonicalName(), type);
        typeMap.put(type.getCategory() + "." + type.getName(), type);
        // hack...
        if (type.getCategory().contains("Electro-Mechanical"))
          typeMap.put(
              type.getCategory().replace("Electro-Mechanical", "Electromechanical")
                  + "."
                  + type.getName(),
              type);
      }

    Map<String, List<Template>> newVariantMap = new HashMap<String, List<Template>>();

    for (Map.Entry<String, List<Template>> entry : variantMap.entrySet()) {
      if (typeMap.containsKey(entry.getKey())) {
        newVariantMap.put(
            typeMap.get(entry.getKey()).getInstanceClass().getCanonicalName(),
            entry.getValue());
      } else {
        LOG.warn("Could not upgrade variants for {}", entry.getKey());
      }
    }

    ConfigurationManager.putValue(Key.TEMPLATES, newVariantMap);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Netlist> extractNetlists(boolean includeSwitches) {
    Map<Netlist, Netlist> result = new HashMap<Netlist, Netlist>();
    List<Node> nodes = new ArrayList<Node>();

    List<ISwitch> switches = new ArrayList<ISwitch>();

    for (IDIYComponent<?> c : currentProject.getComponents()) {
      ComponentType type =
          ComponentProcessor.extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) c.getClass());

      // extract nodes
      if (!(c instanceof IContinuity)) {
        for (int i = 0; i < c.getControlPointCount(); i++) {
          String nodeName = c.getControlPointNodeName(i);
          if (nodeName != null
              && (!includeSwitches || !ISwitch.class.isAssignableFrom(type.getInstanceClass()))) {
            nodes.add(new Node(c, i));
          }
        }
      }

      // extract switches
      if (includeSwitches && ISwitch.class.isAssignableFrom(type.getInstanceClass()))
        switches.add((ISwitch) c);
    }

    // save us the trouble
    if (nodes.isEmpty()) {
      return null;
    }

    // if there are no switches, make one with 1 position so we get 1 result back
    if (switches.isEmpty())
      switches.add(
          new ISwitch() {

            @Override
            public String getPositionName(int position) {
              return "Default";
            }

            @Override
            public int getPositionCount() {
              return 1;
            }

            @Override
            public boolean arePointsConnected(int index1, int index2, int position) {
              return false;
            }
          });

    // construct all possible combinations
    int[] positions = new int[switches.size()];
    for (int i = 0; i < switches.size(); i++) positions[i] = 0;

    // grab continuity areas
    List<Area> continuity = currentProject.getContinuityAreas();

    int i = switches.size() - 1;
    while (i >= 0) {
      // process the current combination
      Map<ISwitch, Integer> switchPositions = new HashMap<ISwitch, Integer>();
      List<Position> posList = new ArrayList<Position>();
      for (int j = 0; j < positions.length; j++) {
        switchPositions.put(switches.get(j), positions[j]);
        posList.add(new Position(switches.get(j), positions[j]));
      }
      List<Connection> connections = getConnections(switchPositions);
      Netlist graph = constructNetlist(nodes, connections, continuity);

      // merge graphs that are effectively the same
      if (result.containsKey(graph)) {
        result.get(graph).getSwitchSetup().add(new SwitchSetup(posList));
      } else {
        graph.getSwitchSetup().add(new SwitchSetup(posList));
        result.put(graph, graph);
      }

      // find the next combination if possible
      if (positions[i] < switches.get(i).getPositionCount() - 1) {
        positions[i]++;
      } else {
        while (i >= 0 && positions[i] == switches.get(i).getPositionCount() - 1) i--;
        if (i >= 0) {
          positions[i]++;
          for (int j = i + 1; j < positions.length; j++) positions[j] = 0;
          i = switches.size() - 1;
        }
      }
    }

    // sort everything alphabetically
    List<Netlist> netlists = new ArrayList<Netlist>(result.keySet());
    Collections.sort(netlists);

    return netlists;
  }

  private Netlist constructNetlist(
      List<Node> nodes, List<Connection> connections, List<Area> continuityAreas) {
    Netlist netlist = new Netlist();

    // debugging code
    // StringBuilder sb = new StringBuilder();
    // sb.append("Nodes:").append("\n");
    // for (Node n : nodes) {
    // sb.append(n.toString()).append("\n");
    // }
    // sb.append("Connections:").append("\n");
    // for (Line2D n : connections) {
    // sb.append(n.getP1()).append(":").append(n.getP2()).append("\n");
    // }
    // LOG.debug(sb.toString());

    for (int i = 0; i < nodes.size() - 1; i++)
      for (int j = i + 1; j < nodes.size(); j++) {
        Node node1 = nodes.get(i);
        Node node2 = nodes.get(j);
        Point2D point1 = node1.getComponent().getControlPoint(node1.getPointIndex());
        Point2D point2 = node2.getComponent().getControlPoint(node2.getPointIndex());

        String commonPoint1 = node1.getComponent().getCommonPointName(node1.getPointIndex());
        String commonPoint2 = node2.getComponent().getCommonPointName(node2.getPointIndex());

        // try both directions
        if (point1.distance(point2) < DrawingManager.CONTROL_POINT_SIZE
            || checkGraphConnectionBothWays(
                point1,
                point2,
                connections,
                continuityAreas,
                new boolean[connections.size()])
            || (commonPoint1 != null && commonPoint1.equalsIgnoreCase(commonPoint2))) {
          boolean added = false;
          // add to an existing vertex if possible
          for (Group g : netlist.getGroups())
            if (g.getNodes().contains(node1)) {
              g.getNodes().add(node2);
              added = true;
            } else if (g.getNodes().contains(node2)) {
              g.getNodes().add(node1);
              added = true;
            }
          if (!added) netlist.getGroups().add(new Group(node1, node2));
        }
      }

    // merge overlapping groups if needed
    boolean reduce = true;
    while (reduce) {
      reduce = false;
      List<Group> groups = netlist.getSortedGroups();
      Iterator<Group> i = groups.iterator();
      while (i.hasNext()) {
        Group g1 = i.next();
        for (Group g2 : groups) {
          if (g1 != g2 && !Collections.disjoint(g1.getNodes(), g2.getNodes())) {
            i.remove();
            g2.getNodes().addAll(g1.getNodes());
            reduce = true;
            break;
          }
        }
      }
      if (reduce) {
        netlist.getGroups().clear();
        netlist.getGroups().addAll(groups);
      }
    }

    Collections.sort(netlist.getSwitchSetup());

    return netlist;
  }

  private boolean checkGraphConnection(
      Point2D point1,
      Point2D point2,
      List<Connection> connections,
      List<Area> continuityAreas,
      boolean[] visited) {

    final double epsilon = DrawingManager.CONTROL_POINT_SIZE;

    if (point1.distance(point2) < epsilon) {
      return true;
    }

    for (Area a : continuityAreas) {
      if (a.contains(point1) && a.contains(point2)) {
        return true;
      }
    }

    for (int i = 0; i < connections.size(); i++) {
      if (visited[i]) {
        continue;
      }

      Connection c = connections.get(i);
      if (point1.distance(c.getP1()) < epsilon) {
        visited[i] = true;
        if (checkGraphConnection(c.getP2(), point2, connections, continuityAreas, visited)) {
          return true;
        }
      }
      if (point1.distance(c.getP2()) < epsilon) {
        visited[i] = true;
        if (checkGraphConnection(c.getP1(), point2, connections, continuityAreas, visited)) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean checkGraphConnectionBothWays(
      Point2D point1,
      Point2D point2,
      List<Connection> connections,
      List<Area> continuityAreas,
      boolean[] visited) {
    return checkGraphConnection(point1, point2, connections, continuityAreas, visited)
        || checkGraphConnection(point2, point1, connections, continuityAreas, visited);
  }

  @SuppressWarnings("unchecked")
  private List<Connection> getConnections(Map<ISwitch, Integer> switchPositions) {
    Set<Connection> connections = new HashSet<Connection>();
    for (IDIYComponent<?> c : currentProject.getComponents()) {
      ComponentType type = ComponentProcessor.extractComponentTypeFrom(
          (Class<? extends IDIYComponent<?>>) c.getClass());
      // handle direct connections
      if (c instanceof IContinuity) {
        for (int i = 0; i < c.getControlPointCount() - 1; i++)
          for (int j = i + 1; j < c.getControlPointCount(); j++)
            if (((IContinuity) c).arePointsConnected(i, j))
              connections.add(new Connection(c.getControlPoint(i), c.getControlPoint(j)));
      }
      // handle switches
      if (ISwitch.class.isAssignableFrom(type.getInstanceClass())
          && switchPositions.containsKey(c)) {
        int position = switchPositions.get(c);
        ISwitch s = (ISwitch) c;
        for (int i = 0; i < c.getControlPointCount() - 1; i++)
          for (int j = i + 1; j < c.getControlPointCount(); j++)
            if (s.arePointsConnected(i, j, position))
              connections.add(new Connection(c.getControlPoint(i), c.getControlPoint(j)));
      }
    }

    currentProject.expandConnections(connections);

    return new ArrayList<Connection>(connections);
  }

  @Override
  public List<INetlistAnalyzer> getNetlistAnalyzers() {
    Set<Class<?>> classes;
    try {
      classes = Utils.getClasses("org.diylc.netlist");
      List<INetlistAnalyzer> result = new ArrayList<INetlistAnalyzer>();

      for (Class<?> clazz : classes) {
        if (!Modifier.isAbstract(clazz.getModifiers())
            && INetlistAnalyzer.class.isAssignableFrom(clazz)) {
          result.add((INetlistAnalyzer) clazz.getDeclaredConstructor().newInstance());
        }
      }

      Collections.sort(
          result,
          new Comparator<INetlistAnalyzer>() {

            @Override
            public int compare(INetlistAnalyzer o1, INetlistAnalyzer o2) {
              return o1.getName().compareToIgnoreCase(o2.getName());
            }
          });

      return result;
    } catch (Exception e) {
      LOG.error("Could not load INetlistSummarizer implementations", e);
      return null;
    }
  }
}
