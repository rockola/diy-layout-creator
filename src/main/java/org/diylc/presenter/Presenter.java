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

package org.diylc.presenter;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
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
import java.util.StringJoiner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.App;
import org.diylc.appframework.Serializer;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.common.BuildingBlockPackage;
import org.diylc.common.ComponentType;
import org.diylc.common.Config;
import org.diylc.common.DrawOption;
import org.diylc.common.EventType;
import org.diylc.common.IComponentFilter;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IKeyProcessor;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.common.VariantPackage;
import org.diylc.components.AbstractComponent;
import org.diylc.core.ExpansionMode;
import org.diylc.core.Grid;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.annotations.IAutoCreator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.NetlistAnalyzer;
import org.diylc.utils.Constants;

/**
 * The main presenter class, contains core app logic and drawing routines.
 *
 * @author Branislav Stojkovic
 */
public class Presenter implements IPlugInPort {

  private static final Logger LOG = LogManager.getLogger(Presenter.class);
  private static final int MAX_RECENT_FILES = 20;

  public static final String DEFAULTS_KEY_PREFIX = "default.";
  public static final List<AbstractComponent> EMPTY_SELECTION = Collections.emptyList();
  public static final int ICON_SIZE = 32;

  private List<Project> projects = new ArrayList<>();

  /**
   * {@link List} of {@link IAutoCreator} objects that are capable of creating more components
   * automatically when a component is created, e.g. Solder Pads.
   */
  private List<IAutoCreator> autoCreators;
  // Maps component class names to ComponentType objects.
  private List<IPlugIn> plugIns;

  // Maps components that have at least one dragged point to set of
  // indices that designate which of their control points are being
  // dragged.
  private Map<AbstractComponent, Set<Integer>> controlPointMap; // TODO move to Project
  private Set<AbstractComponent> lockedComponents; // TODO move to Project

  private DrawingManager drawingManager;
  private ProjectFileManager projectFileManager;

  private static MessageDispatcher<EventType> messageDispatcher =
      new MessageDispatcher<EventType>();

  // D&D
  private boolean dragInProgress = false;

  // Previous mouse location, not scaled for zoom factor.
  private Point previousDragPoint = null;
  private Project preDragProject = null;
  private int dragAction;
  private Point previousScaledPoint;
  private Rectangle selectionRect;

  public Presenter() {
    super();
    plugIns = new ArrayList<IPlugIn>();
    lockedComponents = new HashSet<AbstractComponent>();
    projects.add(new Project());
    drawingManager = new DrawingManager(messageDispatcher);
    projectFileManager = new ProjectFileManager(messageDispatcher);
    upgradeVariants();
  }

  private String getMsg(String key) {
    return App.getString("message.presenter." + key);
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
    if (App.highlightContinuityArea()) {
      return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }

    if (InstantiationManager.getComponentTypeSlot() == null) {
      // Scale point to remove zoom factor.
      Point2D scaledPoint = scalePoint(point);
      if (controlPointMap != null && !controlPointMap.isEmpty()) {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
      }
      for (AbstractComponent component : currentProject().getComponents()) {
        if (currentProject().isActive(component)) {
          ComponentArea area = drawingManager.getComponentArea(component);
          if (area != null && area.inOutlineArea(scaledPoint)) {
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
        currentProject(),
        useZoom ? drawingManager.getZoomLevel() : 1 / Constants.PIXEL_SIZE,
        includeExtraSpace);
  }

  public Project currentProject() {
    return projects.isEmpty() ? null : projects.get(0);
  }

  public void loadProject(Project project, boolean freshStart, String filename) {
    LOG.trace("loadProject({}, {})", project.getTitle(), freshStart);
    // TODO: handle multiple projects
    projects.clear();
    projects.add(project);
    currentProject().clearSelection();
    dispatchMessage(EventType.PROJECT_LOADED, currentProject(), freshStart, filename);
    dispatchMessage(EventType.REPAINT);
    dispatchMessage(EventType.LAYER_STATE_CHANGED, currentProject().getLockedLayers());
    dispatchMessage(EventType.LAYER_VISIBILITY_CHANGED, currentProject().getHiddenLayers());
  }

  public void loadProject(String fileName) {
    LOG.trace("loadProject({})", fileName);
    List<String> warnings = null;
    try {
      warnings = new ArrayList<String>();
      Project project = (Project) projectFileManager.deserializeProjectFromFile(fileName, warnings);
      loadProject(project, true, fileName);
      projectFileManager.fireFileStatusChanged();
      if (!warnings.isEmpty()) {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(getMsg("issues-with-file"));
        for (String warning : warnings) {
          sj.add(warning);
        }
        App.ui().warn(sj.toString());
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

  public void createNewProject() {
    LOG.trace("createNewFile()");
    try {
      Project project = new Project();
      // TODO InstantiationManager.fillWithDefaultProperties(project, null);
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

  private void addToRecentFiles(String fileName) {
    List<String> recentFiles = (List<String>) App.getObject(Config.Flag.RECENT_FILES);
    if (recentFiles == null) {
      recentFiles = new ArrayList<String>();
    }
    recentFiles.remove(fileName);
    recentFiles.add(0, fileName);
    while (recentFiles.size() > MAX_RECENT_FILES) {
      recentFiles.remove(recentFiles.size() - 1);
    }
    App.putValue(Config.Flag.RECENT_FILES, recentFiles);
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
    return showConfirmDialog(message, title, IView.YES_NO_OPTION, IView.WARNING_MESSAGE)
        == IView.YES_OPTION;
  }

  @Override
  public boolean allowFileAction() {
    if (projectFileManager.isModified()) {
      int response =
          showConfirmDialog(
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
      currentProject().setFileVersion(App.getVersionNumber());
      projectFileManager.serializeProjectToFile(currentProject(), fileName, isBackup);
      if (!isBackup) {
        addToRecentFiles(fileName);
      }
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

  public static void drawProject(Project p, Graphics2D g2d, Set<DrawOption> drawOptions) {
    LOG.trace("drawProject([Project {}], [Graphics2D], {})", p.getSequenceNumber(), drawOptions);
    List<String> failedComponentNames =
        drawProjectInternal(p, g2d, drawOptions, null, null, null, false, new DrawingManager(null));

    if (!failedComponentNames.isEmpty()) {
      for (String fc : failedComponentNames) {
        LOG.error("drawProject([Project {}], ...) {} failed", p.getSequenceNumber(), fc);
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
    LOG.trace("draw([g2d], {}, [filter], {})", drawOptions, externalZoom);
    if (currentProject() != null) {
      List<String> failedComponentNames =
          drawProjectInternal(
              currentProject(),
              g2d,
              drawOptions,
              filter,
              externalZoom,
              selectionRect,
              dragInProgress,
              drawingManager);

      dispatchMessage(
          EventType.STATUS_MESSAGE_CHANGED,
          failedComponentNames.isEmpty()
              ? ""
              : "Failed to draw components: " + Utils.toCommaString(failedComponentNames));
    }
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

    LOG.trace(
        "drawProjectInternal([Project {}], ..., {}, ..., dragInProgress={}, ...)",
        p.getSequenceNumber(),
        drawOptions,
        dragInProgress);

    Set<AbstractComponent> groupedComponents = new HashSet<AbstractComponent>();
    for (AbstractComponent component : p.getComponents()) {
      // Only try to draw control points of ungrouped components.
      if (findAllGroupedComponents(p, component).size() > 1) {
        groupedComponents.add(component);
      }
    }

    // Concatenate the specified filter with our own filter that removes hidden layers
    IComponentFilter newFilter =
        new IComponentFilter() {

          @Override
          public boolean testComponent(AbstractComponent component) {
            return ((filter == null || filter.testComponent(component)) && p.isVisible(component));
          }
        };

    // Don't draw the component in the slot if both control points
    // match.
    List<AbstractComponent> componentSlotToDraw = null;
    if (InstantiationManager.getFirstControlPoint() != null
        && InstantiationManager.getPotentialControlPoint() != null
        && InstantiationManager.getFirstControlPoint()
            .equals(InstantiationManager.getPotentialControlPoint())) {
      //
    } else {
      componentSlotToDraw = InstantiationManager.getComponentSlot();
    }
    List<AbstractComponent> failedComponents =
        drawingManager.drawProject(
            g2d,
            p,
            drawOptions,
            newFilter,
            selectionRect,
            p.getLockedComponents(),
            groupedComponents,
            Arrays.asList(
                InstantiationManager.getFirstControlPoint(),
                InstantiationManager.getPotentialControlPoint()),
            componentSlotToDraw,
            dragInProgress,
            externalZoom);
    List<String> failedComponentNames = new ArrayList<String>();
    for (AbstractComponent component : failedComponents) {
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
  public List<AbstractComponent> findComponentsAtScaled(Point point) {
    LOG.trace("findComponentsAtScaled({})", point);
    List<AbstractComponent> components = currentProject().findComponentsAt(point);
    Iterator<AbstractComponent> iterator = components.iterator();
    while (iterator.hasNext()) {
      AbstractComponent component = iterator.next();
      if (!currentProject().isActive(component)) {
        iterator.remove();
      }
    }
    return components;
  }

  @Override
  public List<AbstractComponent> findComponentsAt(Point point) {
    Point scaledPoint = scalePoint(point);
    LOG.trace("findComponentsAtScaled({}) scaledPoint {}", point, scaledPoint);
    List<AbstractComponent> components = findComponentsAtScaled(scaledPoint);
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
    if (isSnapToGrid()) {
      scaledPoint = currentProject().getGrid().snapToGrid(scaledPoint);
    }
    if (clickCount >= 2) {
      editSelection();
    } else {
      if (InstantiationManager.getComponentTypeSlot() != null) {
        // Try to rotate the component on right click while creating.
        if (button != MouseEvent.BUTTON1) {
          InstantiationManager.tryToRotateComponentSlot();
          dispatchMessage(EventType.REPAINT);
          return;
        }
        // Keep the reference to component type for later.
        ComponentType componentTypeSlot = InstantiationManager.getComponentTypeSlot();
        AbstractComponent template = null; // TODO InstantiationManager.getTemplate();
        Project oldProject = currentProject().clone();
        switch (componentTypeSlot.getCreationMethod()) {
          case SINGLE_CLICK:
            LOG.trace("mouseClicked() creation method is Single Click");
            try {
              List<AbstractComponent> componentSlot = InstantiationManager.getComponentSlot();
              List<AbstractComponent> newSelection = new ArrayList<AbstractComponent>();
              for (AbstractComponent component : componentSlot) {
                currentProject().getComponents().add(component);
                newSelection.add(component);
              }
              // group components if there's more than one,
              // e.g. building blocks, but not clipboard contents
              if (componentSlot.size() > 1
                  && !componentTypeSlot.getName().toLowerCase().contains("clipboard")) {

                currentProject().getGroups().add(new HashSet<AbstractComponent>(componentSlot));
              }
              dispatchMessage(EventType.REPAINT);
              currentProject().setSelection(newSelection);
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
            setNewComponentTypeSlot(
                App.continuousCreation() ? componentTypeSlot : null,
                App.continuousCreation() ? template : null,
                false);
            break;
          case POINT_BY_POINT:
            LOG.trace("mouseClicked() creation method is Point-by-point");
            // First click is just to set the controlPointSlot and
            // componentSlot.
            if (InstantiationManager.getComponentSlot() == null) {
              try {
                InstantiationManager.instantiatePointByPoint(scaledPoint, currentProject());
              } catch (Exception e) {
                App.ui().error("Could not create component. Check log for details.");
                LOG.error("Could not create component", e);
              }
              dispatchMessage(
                  EventType.SLOT_CHANGED,
                  componentTypeSlot,
                  InstantiationManager.getFirstControlPoint());
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
        if (!oldProject.equals(currentProject())) {
          dispatchMessage(
              EventType.PROJECT_MODIFIED,
              oldProject,
              currentProject().clone(),
              "Add " + componentTypeSlot.getName());
          // drawingManager.clearContinuityArea();
          projectFileManager.notifyFileChange();
        }
      } else if (App.highlightContinuityArea()) {
        // NOTE: findContinuityAreaAtPoint(scaledPoint) has SIDE EFFECTS only!
        currentProject().findContinuityAreaAtPoint(scaledPoint);
        dispatchMessage(EventType.REPAINT);
      } else {
        List<AbstractComponent> newSelection =
            new ArrayList<AbstractComponent>(currentProject().getSelection());
        List<AbstractComponent> components = findComponentsAtScaled(scaledPoint);
        // If there's nothing under mouse cursor deselect all.
        if (components.isEmpty()) {
          if (!ctrlDown) {
            newSelection.clear();
          }
        } else {
          AbstractComponent topComponent = components.get(0);
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
        currentProject().setSelection(newSelection);
        updateSelection();
        dispatchMessage(EventType.REPAINT);
      }
    }
  }

  private void addPendingComponentsToProject(
      Point scaledPoint, ComponentType componentTypeSlot, AbstractComponent template) {
    List<AbstractComponent> componentSlot = InstantiationManager.getComponentSlot();
    Point firstPoint = componentSlot.get(0).getControlPoint(0);
    // don't allow to create component with the same points
    if (scaledPoint == null || scaledPoint.equals(firstPoint)) {
      return;
    }
    // componentSlot.get(0).setControlPoint(scaledPoint, 1);
    List<AbstractComponent> newSelection = new ArrayList<AbstractComponent>();
    for (AbstractComponent component : componentSlot) {
      addComponent(component, true);
      // Select the new component if it's not locked or invisible.
      if (currentProject().isActive(component)) {
        newSelection.add(component);
      }
    }

    currentProject().setSelection(newSelection);
    updateSelection();
    dispatchMessage(EventType.REPAINT);

    if (componentTypeSlot.isAutoEdit() && App.autoEdit()) {
      editSelection();
    }
    setNewComponentTypeSlot(
        App.continuousCreation() ? componentTypeSlot : null,
        App.continuousCreation() ? template : null,
        false);
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
    Map<AbstractComponent, Set<Integer>> controlPointMap =
        new HashMap<AbstractComponent, Set<Integer>>();
    // If there aren't any control points, try to add all the selected
    // components with all their control points. That will allow the
    // user to drag the whole components.
    for (AbstractComponent c : currentProject().getSelection()) {
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
          oldProject = currentProject().clone();
          rotateComponents(
              currentProject().getSelection(), key == IKeyProcessor.VK_RIGHT ? 1 : -1, snapToGrid);
          rotate = true;
          break;
        case IKeyProcessor.VK_H:
        case IKeyProcessor.VK_V:
          oldProject = currentProject().clone();
          mirrorComponents(
              currentProject().getSelection(),
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
          currentProject().clone(),
          rotate ? "Rotate Selection" : "Mirror Selection");
      dispatchMessage(EventType.REPAINT);
      // drawingManager.clearContinuityArea();
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

    int d = snapToGrid ? (int) currentProject().getGrid().getSpacing().convertToPixels() : 1;
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

    oldProject = currentProject().clone();
    moveComponents(controlPointMap, dx, dy, snapToGrid);
    dispatchMessage(
        EventType.PROJECT_MODIFIED, oldProject, currentProject().clone(), "Move Selection");
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
    LOG.trace("mouseMoved({}, {}, {}, {})", point, ctrlDown, shiftDown, altDown);
    if (point == null) {
      return;
    }

    dragAction = shiftDown ? IPlugInPort.DND_TOGGLE_SNAP : 0;

    Map<AbstractComponent, Set<Integer>> components =
        new HashMap<AbstractComponent, Set<Integer>>();
    previousScaledPoint = scalePoint(point);
    if (InstantiationManager.getComponentTypeSlot() != null) {
      if (isSnapToGrid()) {
        previousScaledPoint = currentProject().getGrid().snapToGrid(previousScaledPoint);
      }
      boolean refresh = false;
      switch (InstantiationManager.getComponentTypeSlot().getCreationMethod()) {
        case POINT_BY_POINT:
          refresh = InstantiationManager.updatePointByPoint(previousScaledPoint);
          break;
        case SINGLE_CLICK:
          refresh =
              InstantiationManager.updateSingleClick(
                  previousScaledPoint, isSnapToGrid(), currentProject().getGrid());
          break;
        default:
      }
      if (refresh) {
        dispatchMessage(EventType.REPAINT);
      }
    } else {
      // Go backwards so we take the highest z-order components first.
      for (int i = currentProject().getComponents().size() - 1; i >= 0; i--) {
        AbstractComponent component = currentProject().getComponents().get(i);
        for (int pointIndex = 0; pointIndex < component.getControlPointCount(); pointIndex++) {

          Point controlPoint = component.getControlPoint(pointIndex);
          // Only consider selected components that are not grouped.
          if (currentProject().inSelection(component)
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
          new HashMap<AbstractComponent, Set<Integer>>(components));
    }
  }

  @Override
  public void selectAll(int layer) {
    LOG.trace("selectAll({})", layer);
    List<AbstractComponent> newSelection =
        new ArrayList<AbstractComponent>(currentProject().getComponents());
    newSelection.removeAll(currentProject().getLockedComponents());
    if (layer > 0) {
      Iterator<AbstractComponent> i = newSelection.iterator();
      while (i.hasNext()) {
        ComponentType type = ComponentType.extractFrom(i.next());
        if ((int) type.getZOrder() != layer) {
          i.remove();
        }
      }
    }
    currentProject().setSelection(newSelection);
    updateSelection();
    dispatchMessage(EventType.REPAINT);
  }

  @Override
  public Rectangle2D getSelectionBounds(boolean applyZoom) {
    Rectangle2D bounds = null;
    if (!currentProject().emptySelection()) {
      int minX = Integer.MAX_VALUE;
      int maxX = Integer.MIN_VALUE;
      int minY = Integer.MAX_VALUE;
      int maxY = Integer.MIN_VALUE;
      for (AbstractComponent c : currentProject().getSelection()) {
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
        } else if (currentProject().contains(c)) {
          LOG.debug("Area is null for {} of type {}", c.getName(), c.getClass().getName());
        }
      }

      if (App.extraSpace()) {
        double extraSpace = drawingManager.getExtraSpace(currentProject());
        // TODO should extraSpace really be added to (not subtracted
        // from) {minX,minY}?
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
      bounds = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }
    return bounds;
  }

  @Override
  public void nudgeSelection(Size offsetX, Size offsetY, boolean includeStuckComponents) {
    LOG.trace(
        "nudgeSelection({}, {}, {}){}",
        offsetX,
        offsetY,
        includeStuckComponents,
        currentProject().emptySelection() ? " selection is empty" : "");
    if (!currentProject().emptySelection()) {
      Map<AbstractComponent, Set<Integer>> controlPointMap =
          new HashMap<AbstractComponent, Set<Integer>>();
      // If there aren't any control points, try to add all the
      // selected components with all their control points. That will
      // allow the user to drag the whole components.
      for (AbstractComponent c : currentProject().getSelection()) {
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

      int dx = (int) offsetX.convertToPixels();
      int dy = (int) offsetY.convertToPixels();
      moveComponents(controlPointMap, dx, dy, false);
      Project oldProject = currentProject().clone();
      dispatchMessage(
          EventType.PROJECT_MODIFIED, oldProject, currentProject().clone(), "Move Selection");
      dispatchMessage(EventType.REPAINT);
      // drawingManager.clearContinuityArea();
    }
  }

  @Override
  public void dragStarted(Point point, int dragAction, boolean forceSelectionRect) {
    LOG.trace("dragStarted({}, {}, {})", point, dragActionToString(dragAction), forceSelectionRect);
    if (InstantiationManager.getComponentTypeSlot() != null) {
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
    if (App.highlightContinuityArea()) {
      LOG.debug("Cannot start drag in highlight continuity mode.");
      return;
    }
    this.dragInProgress = true;
    this.dragAction = dragAction;
    this.preDragProject = currentProject().clone();
    Point scaledPoint = scalePoint(point);
    this.previousDragPoint = scaledPoint;
    List<AbstractComponent> components =
        (forceSelectionRect ? null : findComponentsAtScaled(scaledPoint));
    if (!this.controlPointMap.isEmpty()) {
      // If we're dragging control points, reset selection.
      LOG.trace("We're dragging control points, reset selection.");
      currentProject()
          .setSelection(new ArrayList<AbstractComponent>(this.controlPointMap.keySet()));
      updateSelection();
      dispatchMessage(EventType.REPAINT);
    } else if (components == null || components.isEmpty()) {
      // If there are no components are under the cursor, reset selection.
      LOG.trace("There are no components under the cursor, reset selection.");
      currentProject().clearSelection();
      dispatchMessage(EventType.REPAINT);
    } else {
      // Take the last component, i.e. the top order component.
      AbstractComponent component = components.get(0);
      LOG.trace("Selected {}", component.getIdentifier());
      // If the component under the cursor is not already selected, make
      // it into the only selected component.
      if (!currentProject().inSelection(component)) {
        currentProject().logTraceSelection();
        LOG.trace(
            "{} was not selected, making it the only selected component",
            component.getIdentifier());
        currentProject()
            .setSelection(new ArrayList<AbstractComponent>(findAllGroupedComponents(component)));
        updateSelection();
        dispatchMessage(EventType.REPAINT);
      }
      // If there aren't any control points, try to add all the selected
      // components with all their control points. That will allow the
      // user to drag the whole components.
      for (AbstractComponent c : currentProject().getSelection()) {
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
  private void includeStuckComponents(Map<AbstractComponent, Set<Integer>> controlPointMap) {
    int oldSize = controlPointMap.size();
    LOG.trace("includeStuckComponents: Expanding selected component map");
    for (AbstractComponent component : currentProject().getComponents()) {
      // Check if there's a control point in the current selection
      // that matches with one of its control points.
      for (int i = 0; i < component.getControlPointCount(); i++) {
        // Do not process a control point if it's already in the map and
        // if it's locked.
        if (component.isControlPointSticky(i)
            && !(controlPointMap.containsKey(component)
                && controlPointMap.get(component).contains(i))
            && currentProject().isActive(component)) {
          boolean componentMatches = false;
          for (Map.Entry<AbstractComponent, Set<Integer>> entry : controlPointMap.entrySet()) {
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
    if (point == null || App.highlightContinuityArea()) {
      return false;
    }
    Point scaledPoint = scalePoint(point);
    LOG.trace("dragOver({}) scaledPoint {}", point, scaledPoint);
    if (!controlPointMap.isEmpty()) {
      // We're dragging control point(s).
      int dx = scaledPoint.x - previousDragPoint.x;
      int dy = scaledPoint.y - previousDragPoint.y;
      Point actualD = moveComponents(this.controlPointMap, dx, dy, isSnapToGrid());
      if (actualD == null) {
        return true;
      }
      previousDragPoint.translate(actualD.x, actualD.y);
    } else if (currentProject().emptySelection()
        && InstantiationManager.getComponentTypeSlot() == null
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
    } else if (InstantiationManager.getComponentSlot() != null) {
      this.previousScaledPoint = scalePoint(point);
      InstantiationManager.updateSingleClick(
          previousScaledPoint, isSnapToGrid(), currentProject().getGrid());
    }
    dispatchMessage(EventType.REPAINT);
    return true;
  }

  private Point moveComponents(
      Map<AbstractComponent, Set<Integer>> controlPointMap, int dx, int dy, boolean snapToGrid) {
    // After we make the transfer and snap to grid, calculate actual dx
    // and dy. We'll use them to translate the previous drag point.
    int actualDx = 0;
    int actualDy = 0;
    // For each component, do a simulation of the move to see if any of
    // them will overlap or go out of bounds.

    boolean useExtraSpace = App.extraSpace();
    Dimension d = drawingManager.getCanvasDimensions(currentProject(), 1d, useExtraSpace);
    double extraSpace = useExtraSpace ? drawingManager.getExtraSpace(currentProject()) : 0;
    Grid grid = currentProject().getGrid();

    if (controlPointMap.size() == 1) {
      Map.Entry<AbstractComponent, Set<Integer>> entry =
          controlPointMap.entrySet().iterator().next();

      Point firstPoint =
          entry.getKey().getControlPoint(entry.getValue().toArray(new Integer[] {})[0]);
      Point testPoint = new Point(firstPoint);
      testPoint.translate(dx, dy);
      if (snapToGrid) {
        testPoint = grid.snapToGrid(testPoint);
      }
      actualDx = testPoint.x - firstPoint.x;
      actualDy = testPoint.y - firstPoint.y;
    } else if (snapToGrid) {
      actualDx = grid.roundToGrid(dx);
      actualDy = grid.roundToGrid(dy);
    } else {
      actualDx = dx;
      actualDy = dy;
    }

    if (actualDx == 0 && actualDy == 0) {
      // Nothing to move.
      return null;
    }

    // Validate if moving can be done.
    for (Map.Entry<AbstractComponent, Set<Integer>> entry : controlPointMap.entrySet()) {
      AbstractComponent component = entry.getKey();
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
    for (Map.Entry<AbstractComponent, Set<Integer>> entry : controlPointMap.entrySet()) {
      AbstractComponent c = entry.getKey();
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
    if (!currentProject().emptySelection()) {
      LOG.trace("Rotating selected components");
      Project oldProject = currentProject().clone();
      rotateComponents(currentProject().getSelection(), direction, isSnapToGrid());
      dispatchMessage(
          EventType.PROJECT_MODIFIED, oldProject, currentProject().clone(), "Rotate Selection");
      // drawingManager.clearContinuityArea();
      dispatchMessage(EventType.REPAINT);
    }
  }

  /** @param direction 1 for clockwise, -1 for counter-clockwise */
  private void rotateComponents(
      Collection<AbstractComponent> components, int direction, boolean snapToGrid) {
    Point center = getCenterOf(components, snapToGrid);

    boolean canRotate = true;
    for (AbstractComponent component : currentProject().getSelection()) {
      ComponentType type = ComponentType.extractFrom(component);
      if (type.getTransformer() == null || !type.getTransformer().canRotate(component)) {
        canRotate = false;
        break;
      }
    }

    if (!canRotate
        && !userConfirmed(getMsg("unrotatable-components"), getMsg("mirror-selection"))) {
      return;
    }

    for (AbstractComponent component : currentProject().getSelection()) {
      ComponentType type = ComponentType.extractFrom(component);
      if (type.getTransformer() != null && type.getTransformer().canRotate(component)) {
        drawingManager.invalidateComponent(component);
        type.getTransformer().rotate(component, center, direction);
      }
    }
  }

  @Override
  public void mirrorSelection(int direction) {
    if (!currentProject().emptySelection()) {
      LOG.trace("Mirroring selected components");
      Project oldProject = currentProject().clone();

      mirrorComponents(currentProject().getSelection(), direction, isSnapToGrid());

      dispatchMessage(
          EventType.PROJECT_MODIFIED, oldProject, currentProject().clone(), "Mirror Selection");
      dispatchMessage(EventType.REPAINT);
      // drawingManager.clearContinuityArea();
    }
  }

  private void mirrorComponents(
      Collection<AbstractComponent> components, int direction, boolean snapToGrid) {
    Point center = getCenterOf(components, snapToGrid);
    boolean canMirror = true;
    boolean changesCircuit = false;
    for (AbstractComponent component : components) {
      ComponentType type = ComponentType.extractFrom(component);
      if (type.getTransformer() == null || !type.getTransformer().canMirror(component)) {
        canMirror = false;
        break;
      }
      if (type.getTransformer() != null
          && type.getTransformer().mirroringChangesCircuit(component)) {
        changesCircuit = true;
      }
    }

    if (!canMirror && !userConfirmed(getMsg("unmirrorable-components"), getMsg("mirror-selection"))
        || (changesCircuit
            && !userConfirmed(getMsg("confirm-mirroring"), getMsg("mirror-selection")))) {
      return;
    }

    for (AbstractComponent component : components) {
      ComponentType type = ComponentType.extractFrom(component);
      drawingManager.invalidateComponent(component);
      if (type.getTransformer() != null && type.getTransformer().canMirror(component)) {
        type.getTransformer().mirror(component, center, direction);
      }
    }
  }

  private Point getCenterOf(Collection<AbstractComponent> components, boolean snapToGrid) {
    // Determine center of rotation
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    for (AbstractComponent component : components) {
      for (int i = 0; i < component.getControlPointCount(); i++) {
        Point p = component.getControlPoint(i);
        minX = Integer.min(minX, p.x);
        maxX = Integer.max(minX, p.x);
        minY = Integer.min(minY, p.y);
        maxY = Integer.max(maxY, p.y);
      }
    }

    int centerX = (maxX + minX) / 2;
    int centerY = (maxY + minY) / 2;
    if (snapToGrid) {
      centerX = currentProject().getGrid().roundToGrid(centerX);
      centerY = currentProject().getGrid().roundToGrid(centerY);
    }

    return new Point(centerX, centerY);
  }

  @Override
  public void dragEnded(Point point) {
    if (dragInProgress && InstantiationManager.getComponentSlot() != null) {
      Point scaledPoint = scalePoint(point);
      LOG.trace("dragEnded({}) scaled point {}", point, scaledPoint);

      if (currentProject().emptySelection()) {
        // If there's no selection, finalize selectionRect and see
        // which components intersect with it.
        if (scaledPoint != null) {
          this.selectionRect = Utils.createRectangle(scaledPoint, previousDragPoint);
        }
        List<AbstractComponent> newSelection = new ArrayList<AbstractComponent>();
        if (!App.highlightContinuityArea()) {
          for (AbstractComponent component : currentProject().getComponents()) {
            if (currentProject().isActive(component)) {
              ComponentArea area = drawingManager.getComponentArea(component);
              if (area != null && area.intersectsOutlineArea(selectionRect)) {
                newSelection.addAll(findAllGroupedComponents(component));
              }
            }
          }
        }
        selectionRect = null;
        currentProject().setSelection(newSelection);
        updateSelection();
      } else if (InstantiationManager.getComponentSlot() != null) {
        preDragProject = currentProject().clone();
        addPendingComponentsToProject(
            scaledPoint, InstantiationManager.getComponentTypeSlot(), null);
      } else {
        updateSelection();
      }

      // There is selection, so we need to finalize the drag&drop
      // operation.
      if (!preDragProject.equals(currentProject())) {
        dispatchMessage(
            EventType.PROJECT_MODIFIED, preDragProject, currentProject().clone(), "Drag");
        // drawingManager.clearContinuityArea();
        projectFileManager.notifyFileChange();
      }
      dispatchMessage(EventType.REPAINT);
      dragInProgress = false;
    }
  }

  @Override
  public void pasteComponents(Collection<AbstractComponent> components, boolean autoGroup) {
    LOG.trace("pasteComponents({}, {})", components, autoGroup);
    InstantiationManager.pasteComponents(
        components, previousScaledPoint, isSnapToGrid(), autoGroup, currentProject());
    dispatchMessage(EventType.REPAINT);
    dispatchMessage(
        EventType.SLOT_CHANGED,
        InstantiationManager.getComponentTypeSlot(),
        InstantiationManager.getFirstControlPoint());
  }

  /*
    @Override
    public void duplicateSelection() {
    LOG.trace("duplicateSelection()");
    if (currentProject.emptySelection()) {
    LOG.debug("Nothing to duplicate");
    return;
    }
    Project oldProject = currentProject.clone();
    Set<AbstractComponent> newSelection = new HashSet<AbstractComponent>();

    int grid = (int) currentProject.getGridSpacing().convertToPixels();
    for (AbstractComponent component : currentProject.getSelection()) {
    try {
    AbstractComponent cloned = component.clone();
    ComponentType componentType =
    ComponentType.extractFrom(
    (Class<? extends AbstractComponent>) cloned.getClass());
    cloned.setName(InstantiationManager.createUniqueName(componentType,
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
  */

  @Override
  public void deleteSelectedComponents() {
    LOG.trace("deleteSelectedComponents()");
    if (currentProject().emptySelection()) {
      LOG.debug("deleteSelectedComponents(): Nothing to delete");
      return;
    }

    Project oldProject = currentProject().clone();
    // Remove selected components from any groups.
    currentProject().ungroupSelection();
    // Remove from area map.
    for (AbstractComponent component : currentProject().getSelection()) {
      drawingManager.invalidateComponent(component);
    }
    // Finally, remove the components themselves.
    currentProject().removeSelection();
    dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject().clone(), "Delete");
    // drawingManager.clearContinuityArea();
    projectFileManager.notifyFileChange();
    dispatchMessage(EventType.REPAINT);
  }

  @Override
  public void setSelectionDefaultPropertyValue(String propertyName, Object value) {
    LOG.trace("setSelectionDefaultPropertyValue({}, {})", propertyName, value);
    for (AbstractComponent component : currentProject().getSelection()) {
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
    boolean previous = App.setMetric(isMetric);
    if (previous != isMetric) {
      // refresh selection size in status bar
      dispatchMessage(
          EventType.SELECTION_CHANGED, currentProject().getSelection(), controlPointMap.keySet());
    }
  }

  @Override
  public void groupSelectedComponents() {
    LOG.trace("groupSelectedComponents()");
    Project oldProject = currentProject().clone();
    currentProject().groupSelection();
    // Notify the listeners.
    dispatchMessage(EventType.REPAINT);
    if (!oldProject.equals(currentProject())) {
      dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject().clone(), "Group");
      projectFileManager.notifyFileChange();
    }
  }

  @Override
  public void ungroupSelectedComponents() {
    LOG.trace("ungroupSelectedComponents()");
    Project oldProject = currentProject().clone();
    currentProject().ungroupSelection();
    // Notify the listeners.
    dispatchMessage(EventType.REPAINT);
    if (!oldProject.equals(currentProject())) {
      dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject().clone(), "Ungroup");
      projectFileManager.notifyFileChange();
    }
  }

  @Override
  public void setLayerLocked(int layerZOrder, boolean locked) {
    LOG.trace("setLayerLocked({}, {})", layerZOrder, locked);
    Project oldProject = currentProject().clone();
    if (locked) {
      currentProject().getLockedLayers().add(layerZOrder);
    } else {
      currentProject().getLockedLayers().remove(layerZOrder);
    }
    currentProject().clearSelection();
    dispatchMessage(EventType.REPAINT);
    dispatchMessage(EventType.LAYER_STATE_CHANGED, currentProject().getLockedLayers());
    if (!oldProject.equals(currentProject())) {
      dispatchMessage(
          EventType.PROJECT_MODIFIED,
          oldProject,
          currentProject().clone(),
          locked ? "Lock Layer" : "Unlock Layer");
      projectFileManager.notifyFileChange();
    }
  }

  @Override
  public void setLayerVisibility(int layerZOrder, boolean visible) {
    LOG.trace("setLayerVisibility({}, {})", layerZOrder, visible);
    Project oldProject = currentProject().clone();
    if (visible) {
      currentProject().getHiddenLayers().remove(layerZOrder);
    } else {
      currentProject().getHiddenLayers().add(layerZOrder);
    }
    currentProject().clearSelection();
    dispatchMessage(EventType.REPAINT);
    dispatchMessage(EventType.LAYER_VISIBILITY_CHANGED, currentProject().getHiddenLayers());
    if (!oldProject.equals(currentProject())) {
      dispatchMessage(
          EventType.PROJECT_MODIFIED,
          oldProject,
          currentProject().clone(),
          visible ? "Show Layer" : "Hide Layer");
      projectFileManager.notifyFileChange();
    }
  }

  @Override
  public void sendSelectionToBack() {
    LOG.trace("sendSelectionToBack()");
    Project oldProject = currentProject().clone();
    /* sort the selection in the reversed Z-order to preserve the
    order after moving to the back */
    List<AbstractComponent> selection =
        new ArrayList<AbstractComponent>(currentProject().getSelection());
    Collections.sort(
        selection,
        new Comparator<AbstractComponent>() {

          @Override
          public int compare(AbstractComponent o1, AbstractComponent o2) {
            return Integer.compare(
                currentProject().getComponents().indexOf(o2),
                currentProject().getComponents().indexOf(o1));
          }
        });

    for (AbstractComponent component : selection) {
      ComponentType componentType = ComponentType.extractFrom(component);
      int index = currentProject().getComponents().indexOf(component);
      if (index < 0) {
        LOG.error("Component {} not found in the project!", component.getIdentifier());
        // TODO throw exception? this should definitely not happen //ola 20200113
      } else {
        while (index > 0) {
          AbstractComponent componentBefore = currentProject().getComponents().get(index - 1);
          if (!currentProject().inSelection(componentBefore)) {
            ComponentType componentBeforeType = ComponentType.extractFrom(componentBefore);
            if (!componentType.isFlexibleZOrder()
                && (Math.round(componentBeforeType.getZOrder())
                    < Math.round(componentType.getZOrder()))
                // && forceConfirmation != IView.YES_OPTION
                && !userConfirmed(getMsg("bottom-reached"), getMsg("send-selection-to-back"))) {
              break;
            }
          }
          Collections.swap(currentProject().getComponents(), index, index - 1);
          index--;
        }
      }
    }
    if (!oldProject.equals(currentProject())) {
      dispatchMessage(
          EventType.PROJECT_MODIFIED, oldProject, currentProject().clone(), "Send to Back");
      projectFileManager.notifyFileChange();
      dispatchMessage(EventType.REPAINT);
    }
  }

  @Override
  public void bringSelectionToFront() {
    LOG.trace("bringSelectionToFront()");
    Project oldProject = currentProject().clone();
    List<AbstractComponent> selection =
        new ArrayList<AbstractComponent>(currentProject().getSelection());
    // sort the selection in Z-order
    Collections.sort(
        selection,
        new Comparator<AbstractComponent>() {

          @Override
          public int compare(AbstractComponent o1, AbstractComponent o2) {
            return Integer.compare(
                currentProject().getComponents().indexOf(o1),
                currentProject().getComponents().indexOf(o2));
          }
        });

    for (AbstractComponent component : selection) {
      ComponentType componentType = ComponentType.extractFrom(component);
      int index = currentProject().getComponents().indexOf(component);
      if (index < 0) {
        LOG.warn("Component {} not found in the project", component.getIdentifier());
      } else {
        while (index < currentProject().getComponents().size() - 1) {
          AbstractComponent componentAfter = currentProject().getComponents().get(index + 1);
          if (!currentProject().inSelection(componentAfter)) {
            ComponentType componentAfterType = ComponentType.extractFrom(componentAfter);
            if (!componentType.isFlexibleZOrder()
                && (Math.round(componentAfterType.getZOrder())
                    > Math.round(componentType.getZOrder()))
                && !userConfirmed(getMsg("top-reached"), getMsg("bring-selection-to-front"))) {
              break;
            }
          }
          Collections.swap(currentProject().getComponents(), index, index + 1);
          index++;
        }
      }
    }
    if (!oldProject.equals(currentProject())) {
      dispatchMessage(
          EventType.PROJECT_MODIFIED, oldProject, currentProject().clone(), "Bring to Front");
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

  @Override
  public void renumberSelectedComponents(final boolean xAxisFirst) {
    if (currentProject().emptySelection()) {
      LOG.debug("renumberSelectedComponents({}) no selection", xAxisFirst);
      return;
    }
    LOG.trace("renumberSelectedComponents({})", xAxisFirst);
    Project oldProject = currentProject().clone();
    List<AbstractComponent> components =
        new ArrayList<AbstractComponent>(currentProject().getSelection());
    // Sort components by their location.
    Collections.sort(
        components,
        new Comparator<AbstractComponent>() {

          @Override
          public int compare(AbstractComponent o1, AbstractComponent o2) {
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
    for (AbstractComponent component : components) {
      component.setName("");
    }
    // Assign new ones.
    for (AbstractComponent component : components) {
      component.setName(
          InstantiationManager.createUniqueName(
              ComponentType.extractFrom(component), currentProject().getComponents()));
    }

    dispatchMessage(
        EventType.PROJECT_MODIFIED, oldProject, currentProject().clone(), "Renumber selection");
    projectFileManager.notifyFileChange();
    dispatchMessage(EventType.REPAINT);
  }

  /** Update selection. */
  public void updateSelection() {
    // this.selectedComponents = new HashSet<AbstractComponent>(newSelection);
    Map<AbstractComponent, Set<Integer>> controlPointMap =
        new HashMap<AbstractComponent, Set<Integer>>();
    for (AbstractComponent component : currentProject().getSelection()) {
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
        EventType.SELECTION_CHANGED, currentProject().getSelection(), controlPointMap.keySet());
  }

  /** Expand selection. */
  @Override
  public void expandSelection(ExpansionMode expansionMode) {
    LOG.trace("expandSelection({})", expansionMode);
    List<AbstractComponent> newSelection =
        new ArrayList<AbstractComponent>(currentProject().getSelection());
    List<Netlist> netlists = currentProject().extractNetlists(false);
    List<Set<AbstractComponent>> allGroups = NetlistAnalyzer.extractComponentGroups(netlists);
    // Find control points of all selected components and all types
    Set<String> selectedNamePrefixes = new HashSet<String>();
    if (expansionMode == ExpansionMode.SAME_TYPE) {
      for (AbstractComponent component : currentProject().getSelection()) {
        selectedNamePrefixes.add(ComponentType.extractFrom(component).getNamePrefix());
      }
    }
    // Now try to find components that intersect with at least one component
    // in the pool.
    for (AbstractComponent component : currentProject().getComponents()) {
      if (newSelection.contains(component)) {
        // no need to consider it, it's already in the selection
        continue;
      }
      // construct a list of component groups that contain the current component
      List<Set<AbstractComponent>> componentGroups = new ArrayList<Set<AbstractComponent>>();
      for (Set<AbstractComponent> e : allGroups) {
        if (e.contains(component)) {
          componentGroups.add(e);
        }
      }
      if (componentGroups.isEmpty()) {
        continue;
      }
      // Skip already selected components or ones that cannot be stuck to
      // other components.
      boolean matches = false;

      outer:
      for (AbstractComponent selectedComponent : currentProject().getSelection()) {
        // try to find the selectedComponent in one of the groups
        for (Set<AbstractComponent> s : componentGroups) {
          if (s.contains(selectedComponent)) {
            matches = true;
            break outer;
          }
        }
      }

      if (matches) {
        switch (expansionMode) {
          case ALL:
          case IMMEDIATE:
            newSelection.add(component);
            break;
          case SAME_TYPE:
            if (selectedNamePrefixes.contains(
                ComponentType.extractFrom(component).getNamePrefix())) {
              newSelection.add(component);
            }
            break;
          default:
        }
      }
    }

    int oldSize = currentProject().getSelection().size();
    currentProject().setSelection(newSelection);
    // Go deeper if possible.
    if (newSelection.size() > oldSize && expansionMode != ExpansionMode.IMMEDIATE) {
      expandSelection(expansionMode);
    }
    dispatchMessage(EventType.REPAINT);
  }

  /**
   * Finds all components that are grouped with the specified component. This should be called any
   * time components are added or removed from the selection.
   *
   * @param component
   * @return set of all components that belong to the same group with the specified component. At
   *     the minimum, set contains that single component.
   */
  private static Set<AbstractComponent> findAllGroupedComponents(
      Project p, AbstractComponent component) {
    Set<AbstractComponent> components = new HashSet<AbstractComponent>();
    components.add(component);
    for (Set<AbstractComponent> group : p.getGroups()) {
      if (group.contains(component)) {
        components.addAll(group);
        break;
      }
    }
    return components;
  }

  private Set<AbstractComponent> findAllGroupedComponents(AbstractComponent component) {
    return findAllGroupedComponents(currentProject(), component);
  }

  @Override
  public Point2D[] calculateSelectionDimension() {
    if (currentProject().emptySelection()) {
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
  private void addComponent(AbstractComponent component, boolean allowAutoCreate) {
    int index = currentProject().getComponents().size();
    while (index > 0
        && ComponentType.extractFrom(component).getZOrder()
            < ComponentType.extractFrom(currentProject().getComponents().get(index - 1))
                .getZOrder()) {
      index--;
    }
    if (index < currentProject().getComponents().size()) {
      currentProject().getComponents().add(index, component);
    } else {
      currentProject().getComponents().add(component);
    }

    // Check if we should auto-create something.
    if (allowAutoCreate) {
      for (IAutoCreator creator : this.getAutoCreators()) {
        List<AbstractComponent> newComponents = creator.createIfNeeded(component);
        if (newComponents != null) {
          for (AbstractComponent c : newComponents) {
            addComponent(c, false);
          }
        }
      }
    }
  }

  @Override
  public List<PropertyWrapper> getMutualSelectionProperties() {
    try {
      return ComponentProcessor.getMutualSelectionProperties(currentProject().getSelection());
    } catch (Exception e) {
      LOG.error("Could not get mutual selection properties", e);
      return null;
    }
  }

  private void applyPropertiesToSelection(List<PropertyWrapper> properties) {
    LOG.trace("applyPropertiesToSelection({})", properties);
    Project oldProject = currentProject().clone();
    try {
      for (AbstractComponent component : currentProject().getSelection()) {
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
      if (!oldProject.equals(currentProject())) {
        dispatchMessage(
            EventType.PROJECT_MODIFIED, oldProject, currentProject().clone(), "Edit Selection");
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
    Project oldProject = currentProject().clone();
    try {
      for (PropertyWrapper property : properties) {
        property.writeTo(obj);
      }
    } catch (Exception e) {
      LOG.error("Could not apply properties", e);
      App.ui().error("Could not apply changes. Check the log for details.");
    } finally {
      // Notify the listeners.
      if (!oldProject.equals(currentProject())) {
        dispatchMessage(
            EventType.PROJECT_MODIFIED, oldProject, currentProject().clone(), "Edit Project");
        // drawingManager.clearContinuityArea();
        projectFileManager.notifyFileChange();
      }
      drawingManager.fireZoomChanged();
    }
  }

  @Override
  public ComponentType getNewComponentTypeSlot() {
    return InstantiationManager.getComponentTypeSlot();
  }

  @Override
  public void setNewComponentTypeSlot(
      ComponentType componentType, AbstractComponent variant, boolean forceInstantiate) {
    LOG.trace("setNewComponentSlot({})", componentType == null ? "null" : componentType.getName());
    if (componentType != null && componentType.getInstanceClass() == null) {

      LOG.warn("Cannot set new component type slot for type {}", componentType.getName());
      setNewComponentTypeSlot(null, null, false);
      return;
    }

    // try to find a default template if none is provided
    if (componentType != null && variant == null) {
      /*
        TODO refactor all of this
      String defaultTemplate = getDefaultVariant(componentType);
      LOG.trace(
          "setNewComponentTypeSlot(...) getting default template {} among variants for {}",
          defaultTemplate,
          componentType.getName());
      template = Template.find(getVariantsFor(componentType), defaultTemplate);
      */
      LOG.error("TODO: remember to refactor default component variant!");
    }

    try {
      InstantiationManager.setComponentTypeSlot(
          componentType, variant, currentProject(), forceInstantiate);

      if (forceInstantiate) {
        currentProject().setSelection(InstantiationManager.getComponentSlot());
        updateSelection();
      } else if (componentType != null) {
        currentProject().clearSelection();
      }

      dispatchMessage(EventType.REPAINT);
      dispatchMessage(
          EventType.SLOT_CHANGED,
          InstantiationManager.getComponentTypeSlot(),
          InstantiationManager.getFirstControlPoint(),
          forceInstantiate);
    } catch (Exception e) {
      LOG.error("Could not set component type slot", e);
      App.ui().error("Could not set component type slot. Check log for details.");
    }
  }

  @Override
  public void saveSelectedComponentAsVariant(String variantName) {
    LOG.trace("saveSelectedComponentAsVariant({})", variantName);
    if (currentProject().getSelection().size() != 1) {
      throw new RuntimeException("Can only save a single component as a variant at once.");
    }
    // Get first component from selection
    // NOTE: is selection guaranteed to be in order? //ola 20200113
    AbstractComponent component = currentProject().getSelection().iterator().next();
    ComponentType type = ComponentType.extractFrom(component);
    Map<String, List<AbstractComponent>> variantMap = null;
    /* TODO!!!!!!!!!!!!!!!! was
       = (Map<String, List<AbstractComponent>>) App.getObject(Config.Flag.TEMPLATES);
    */
    if (variantMap == null) {
      variantMap = new HashMap<String, List<AbstractComponent>>();
    }
    String key = type.getInstanceClass().getCanonicalName();
    List<AbstractComponent> variants = variantMap.get(key);
    if (variants == null) {
      variants = new ArrayList<AbstractComponent>();
      variantMap.put(key, variants);
    }
    List<PropertyWrapper> properties = ComponentProcessor.extractProperties(component.getClass());
    Map<String, Object> values = new HashMap<String, Object>();
    for (PropertyWrapper property : properties) {
      String propertyName = property.getName();
      if (!propertyName.equalsIgnoreCase("name")) {
        try {
          property.readFrom(component);
          values.put(propertyName, property.getValue());
        } catch (Exception e) {
          LOG.error("Could not set property {} for {}", propertyName, component.getIdentifier());
          // TODO: ok to ignore?
        }
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

    AbstractComponent template = null;
    try {
      template = component.clone(); // new Template(variantName, values, points);
    } catch (CloneNotSupportedException ce) {
      LOG.error("Couldn't clone " + component.getName(), ce);
      throw new RuntimeException(ce);
    }
    template.setName(variantName);
    boolean exists = false;
    for (AbstractComponent v : variants) {
      if (v.getName().equalsIgnoreCase(template.getName())) {
        exists = true;
        break;
      }
    }

    if (exists) {
      if (!warnedUserConfirmed(getMsg("confirm-variant-overwrite"), getMsg("save-as-variant"))) {
        return;
      }
      // Delete the existing variant
      Iterator<AbstractComponent> i = variants.iterator();
      while (i.hasNext()) {
        AbstractComponent t = i.next();
        if (t.getName().equalsIgnoreCase(template.getName())) {
          i.remove();
        }
      }
    }

    variants.add(template);

    if (System.getProperty("org.diylc.WriteStaticVariants", "false").equalsIgnoreCase("true")) {
      // unify default and user-variants
      ComponentType.addVariants(variantMap);
      try {
        ComponentType.saveVariants("variants.xml");
        // no more user variants
        App.putValue(Config.Flag.TEMPLATES, null);
        LOG.trace("Saved default variants");
      } catch (IOException e) {
        LOG.error("Could not save default variants", e);
        // TODO: UI error dialog
        // TODO: propagate exception?
      }
    } else {
      App.putValue(Config.Flag.TEMPLATES, variantMap);
    }
  }

  @Override
  public List<AbstractComponent> getVariantsFor(ComponentType type) {
    if (type == null) {
      LOG.trace("getVariantsFor({}) No type, no variants");
      return new ArrayList<AbstractComponent>();
    }
    return type.getVariants();
  }

  @Override
  public List<AbstractComponent> getVariantsForSelection() {
    LOG.trace("getVariantsForSelection()");
    if (currentProject().emptySelection()) {
      LOG.error("getVariantsForSelection() No components selected");
      // throw new RuntimeException("No components selected");
      return null;
    }
    ComponentType selectedType = null;
    Iterator<AbstractComponent> iterator = currentProject().getSelection().iterator();
    while (iterator.hasNext()) {
      ComponentType type = ComponentType.extractFrom(iterator.next());
      if (selectedType == null) {
        selectedType = type;
      } else if (selectedType.getInstanceClass() != type.getInstanceClass()) {
        return null;
      }
    }
    return getVariantsFor(selectedType);
  }

  @Override
  public void applyVariantToSelection(AbstractComponent template) {
    LOG.trace("applyVariantToSelection({})", template.getName());

    Project oldProject = currentProject().clone();

    List<AbstractComponent> newSelection = new ArrayList<>();
    for (AbstractComponent component : currentProject().getSelection()) {
      try {
        drawingManager.invalidateComponent(component);
        // InstantiationManager.loadComponentShapeFromTemplate(component, template);
        AbstractComponent newComponent = template.clone();
        newComponent.copyControlPoints(component);
        newSelection.add(newComponent);
        // InstantiationManager.fillWithDefaultProperties(component, template);
      } catch (Exception e) {
        LOG.warn("Could not apply templates to " + component.getIdentifier(), e);
      }
    }

    currentProject().removeSelection();
    currentProject().addComponents(newSelection);
    currentProject().setSelection(newSelection);

    // Notify the listeners.
    if (!oldProject.equals(currentProject())) {
      dispatchMessage(
          EventType.PROJECT_MODIFIED, oldProject, currentProject().clone(), "Edit Selection");
      // drawingManager.clearContinuityArea();
      projectFileManager.notifyFileChange();
    }
    dispatchMessage(EventType.REPAINT);
  }

  @Override
  public void deleteVariant(ComponentType type, String templateName) {
    LOG.trace("deleteTemplate({}, {})", type, templateName);
    if (type != null) {
      Map<String, List<AbstractComponent>> templateMap =
          (Map<String, List<AbstractComponent>>) App.getObject(Config.Flag.TEMPLATES);
      if (templateMap != null) {
        // try by class name and then by old category.type format
        List<String> keys = new ArrayList<String>();
        keys.add(type.getInstanceClass().getCanonicalName());
        keys.add(type.getCategory() + "." + type.getName());

        for (String key : keys) {
          List<AbstractComponent> templates = templateMap.get(key);
          if (templates != null) {
            Iterator<AbstractComponent> i = templates.iterator();
            while (i.hasNext()) {
              AbstractComponent t = i.next();
              if (t.getName().equalsIgnoreCase(templateName)) {
                i.remove();
              }
            }
          }
        }
      }
      App.putValue(Config.Flag.TEMPLATES, templateMap);
    }
  }

  @Override
  public void setDefaultVariant(ComponentType type, String templateName) {
    LOG.trace("setTemplateDefault({}, {})", type, templateName);
    Map<String, String> defaultTemplateMap =
        (Map<String, String>) App.getObject(Config.Flag.DEFAULT_TEMPLATES);
    if (defaultTemplateMap == null) {
      defaultTemplateMap = new HashMap<String, String>();
    }
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
    App.putValue(Config.Flag.DEFAULT_TEMPLATES, defaultTemplateMap);
  }

  @Override
  public String getDefaultVariant(ComponentType type) {
    Map<String, String> defaultTemplateMap =
        (Map<String, String>) App.getObject(Config.Flag.DEFAULT_TEMPLATES);
    if (defaultTemplateMap == null) {
      return null;
    }
    String key1 = type.getInstanceClass().getCanonicalName();
    String key2 = type.getCategory() + "." + type.getName();
    String ret = defaultTemplateMap.get(key1);
    if (ret == null) {
      ret = defaultTemplateMap.get(key2);
    }
    return ret;
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

    if (p != null && App.extraSpace()) {
      double extraSpace = drawingManager.getExtraSpace(currentProject());
      p.translate((int) (-extraSpace), (int) (-extraSpace));
    }
    return p;
  }

  @Override
  public void saveSelectionAsBlock(String blockName) {
    LOG.trace("saveSelectionAsBlock({})", blockName);
    List<AbstractComponent> blockComponents =
        new ArrayList<AbstractComponent>(currentProject().getSelection());
    Collections.sort(
        blockComponents,
        new Comparator<AbstractComponent>() {

          @Override
          public int compare(AbstractComponent o1, AbstractComponent o2) {
            return Integer.compare(
                currentProject().getComponents().indexOf(o1),
                currentProject().getComponents().indexOf(o2));
          }
        });
    App.addBlock(blockName, blockComponents);
  }

  @Override
  public void loadBlock(String blockName) throws InvalidBlockException {
    LOG.trace("loadBlock({})", blockName);
    Collection<AbstractComponent> components = App.getBlocks().get(blockName);
    if (components == null) {
      throw new InvalidBlockException();
    }
    // clear potential control point every time!
    InstantiationManager.setPotentialControlPoint(null);
    // clone components
    List<AbstractComponent> clones = new ArrayList<AbstractComponent>();
    List<AbstractComponent> testComponents =
        new ArrayList<AbstractComponent>(currentProject().getComponents());
    for (AbstractComponent c : components) {
      try {
        AbstractComponent clone = c.clone();
        clone.setName(
            InstantiationManager.createUniqueName(
                ComponentType.extractFrom(clone), testComponents));
        testComponents.add(clone);
        clones.add(clone);
      } catch (CloneNotSupportedException e) {
        LOG.error("Could not clone component {}", c);
      }
    }
    // paste them to the project
    pasteComponents(clones, true);
  }

  @Override
  public void deleteBlock(String blockName) {
    LOG.trace("deleteBlock({})", blockName);
    App.getBlocks().remove(blockName);
  }

  @Override
  public double getExtraSpace() {
    double extraSpace = 0d;

    if (App.extraSpace()) {
      extraSpace =
          (drawingManager.getExtraSpace(currentProject()) / Constants.PIXELS_PER_INCH)
              * (App.metric() ? SizeUnit.in.getFactor() / SizeUnit.cm.getFactor() : 1);
    }

    return extraSpace;
  }

  @Override
  public int importVariants(String fileName) throws IOException {
    LOG.trace("importVariants({})", fileName);

    VariantPackage pkg = (VariantPackage) Serializer.fromFile(fileName);
    if (pkg == null || pkg.getVariants().isEmpty()) {
      return 0;
    }

    Map<String, List<AbstractComponent>> variantMap =
        (Map<String, List<AbstractComponent>>) App.getObject(Config.Flag.TEMPLATES);
    if (variantMap == null) {
      variantMap = new HashMap<String, List<AbstractComponent>>();
    }

    for (Map.Entry<String, List<AbstractComponent>> entry : pkg.getVariants().entrySet()) {
      List<AbstractComponent> templates = variantMap.get(entry.getKey());
      if (templates == null) {
        templates = new ArrayList<AbstractComponent>();
        variantMap.put(entry.getKey(), templates);
      }
      try {
        for (AbstractComponent v : entry.getValue()) {
          templates.add(v.clone());
        }
      } catch (CloneNotSupportedException ce) {
        LOG.error("Could not clone components", ce);
        throw new RuntimeException(ce);
      }
    }
    App.putValue(Config.Flag.TEMPLATES, variantMap);
    LOG.info("Loaded variants for %d components", pkg.getVariants().size());
    return pkg.getVariants().size();
  }

  @Override
  public int importBlocks(String fileName) throws IOException {
    LOG.trace("importBlocks({})", fileName);
    int importedBlocks = 0;
    BuildingBlockPackage pkg = (BuildingBlockPackage) Serializer.fromFile(fileName);
    if (pkg != null && !pkg.getBlocks().isEmpty()) {
      for (Map.Entry<String, List<AbstractComponent>> entry : pkg.getBlocks().entrySet()) {
        // TODO should we really replace an existing block with the same name?
        App.getBlocks().put(entry.getKey() + " [" + pkg.getOwner() + "]", entry.getValue());
      }
      importedBlocks = pkg.getBlocks().size();
      LOG.info("Loaded building blocks for {} components", importedBlocks);
    }
    return importedBlocks;
  }

  private static boolean upgradedVariants = false;

  private synchronized void upgradeVariants() {
    return;
    /* TODO figure out whether this is needed at all
    if (upgradedVariants) {
      return;
    }

    upgradedVariants = true;

    LOG.info("upgradeVariants() Checking if variants need to be updated");
    Map<String, List<Template>> variantMap =
        (Map<String, List<Template>>) App.getObject(Config.Flag.TEMPLATES);

    if (variantMap != null) {
      Map<String, ComponentType> typeMap =
          new TreeMap<String, ComponentType>(String.CASE_INSENSITIVE_ORDER);

      LOG.info("upgradeVariants() Getting component types");
      for (Map.Entry<String, List<ComponentType>> entry :
          ComponentType.getComponentTypes().entrySet()) {
        LOG.debug("component type key {}", entry.getKey());
        for (ComponentType type : entry.getValue()) {
          String canonicalName = type.getInstanceClass().getCanonicalName();
          typeMap.put(canonicalName, type);
          String categoryAndName = type.getCategory() + "." + type.getName();
          typeMap.put(categoryAndName, type);
          // Hack... TODO figure out why
          String hackName = null;
          if (type.getCategory().contains("Electro-Mechanical")) {
            hackName =
                type.getCategory().replace("Electro-Mechanical", "Electromechanical")
                    + "."
                    + type.getName();
            typeMap.put(hackName, type);
          }
          if (hackName == null) {
            LOG.trace("Added type as {} and {}", canonicalName, categoryAndName);
          } else {
            LOG.trace("Added type as {}, {}, AND {}", canonicalName, categoryAndName, hackName);
          }
        }
      }

      Map<String, List<Template>> newVariantMap = new HashMap<String, List<Template>>();

      for (Map.Entry<String, List<Template>> entry : variantMap.entrySet()) {
        if (typeMap.containsKey(entry.getKey())) {
          newVariantMap.put(
              typeMap.get(entry.getKey()).getInstanceClass().getCanonicalName(), entry.getValue());
        } else {
          LOG.warn("Could not upgrade variants for {}", entry.getKey());
        }
      }

      App.putValue(Config.Flag.TEMPLATES, newVariantMap);
    }
    */
  }
}
