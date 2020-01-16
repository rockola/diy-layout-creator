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

package org.diylc.core;

import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.undo.UndoManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.appframework.update.VersionNumber;
import org.diylc.common.ComponentType;
import org.diylc.common.Config;
import org.diylc.common.EventType;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.MultiLineText;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

import org.diylc.presenter.ComponentArea; // needed for findComponentsAt
import org.diylc.presenter.ComponentProcessor; // needed by createUniqueName
import org.diylc.presenter.Connection; // needed by continuity area methods
import org.diylc.presenter.DrawingManager; // needed for CONTROL_POINT_SIZE only
import org.diylc.presenter.InstantiationManager; // needed by createUniqueName
import org.diylc.presenter.Presenter; // needed for dispatchMessage

/**
 * Project entity class. Contains project properties and a collection
 * of components.This class is serialized to file. Some field getters
 * are tagged with {@link EditableProperty} to enable user editing.
 *
 * @author Branislav Stojkovic
 */
public class Project implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(Project.class);
  private static final int CONTROL_POINT_SIZE = DrawingManager.CONTROL_POINT_SIZE;

  public static final String FILE_SUFFIX = ".diy";

  public static String DEFAULT_TITLE = Config.getString("project.new-title");
  public static Size DEFAULT_WIDTH = new Size(29d, SizeUnit.cm);
  public static Size DEFAULT_HEIGHT = new Size(21d, SizeUnit.cm);
  public static Size DEFAULT_GRID_SPACING = new Size(0.1d, SizeUnit.in);
  public static Font DEFAULT_FONT = new Font(Config.getString("font.label"), Font.PLAIN, 14);

  private VersionNumber fileVersion;
  private String title = DEFAULT_TITLE;
  private String author = System.getProperty("user.name");
  private String description;
  private Size width = DEFAULT_WIDTH;
  private Size height = DEFAULT_HEIGHT;
  private Size gridSpacing = DEFAULT_GRID_SPACING;
  private List<IDIYComponent<?>> components = new ArrayList<IDIYComponent<?>>();
  private Set<Set<IDIYComponent<?>>> groups = new HashSet<Set<IDIYComponent<?>>>();
  private Set<Integer> lockedLayers = new HashSet<Integer>();
  private Set<Integer> hiddenLayers = new HashSet<Integer>();
  private Font font = DEFAULT_FONT;

  private transient Set<IDIYComponent<?>> selection = new HashSet<IDIYComponent<?>>();
  private transient Area continuityArea;
  private transient Date created = new Date(); // should really be called "instantiated"
  private transient int sequenceNumber = nextSequenceNumber();

  private static int projectSequenceNumber = 0;

  public Project() {}

  /**
     xStream does not call default constructor, so any transients must
     be explicitly initialized.
   */
  private Object readResolve() {
    created = new Date();
    sequenceNumber = nextSequenceNumber();
    selection = new HashSet<IDIYComponent<?>>();
    return this;
  }

  private int nextSequenceNumber() {
    projectSequenceNumber = projectSequenceNumber + 1;
    return projectSequenceNumber;
  }

  public Date getCreated() {
    return new Date(created.getTime());
  }

  public Set<IDIYComponent<?>> getSelection() {
    return selection;
  }

  /* TODO: getSelection in Z order
       Collections.sort(
         getSelection(),
         ComparatorFactory.getInstance().getComponentProjectZOrderComparator(this));
  */


  public boolean emptySelection() {
    return selection.isEmpty();
  }

  public void clearSelection() {
    LOG.trace("clearSelection()");
    selection.clear();
  }

  public boolean inSelection(IDIYComponent<?> c) {
    return selection.contains(c);
  }

  /* Continuity area methods */

  private void clearContinuityArea() {
    this.continuityArea = null;
  }

  public boolean hasContinuityArea() {
    return this.continuityArea != null;
  }

  public Area getContinuityArea() {
    return this.continuityArea;
  }

  public Area findContinuityAreaAtPoint(Point p) {
    List<Area> areas = getContinuityAreas();

    for (Area a : areas) {
      if (a.contains(p)) {
        this.continuityArea = a;
        return a;
      }
    }

    this.continuityArea = null;
    return null;
  }

  public List<Area> getContinuityAreas() {
    // Find all individual continuity areas for all components
    List<Area> preliminaryAreas = new ArrayList<Area>();
    List<Boolean> checkBreakout = new ArrayList<Boolean>();
    Set<Connection> connections = new HashSet<Connection>();
    for (IDIYComponent<?> c : getComponents()) {
      ComponentArea a = c.getArea();

      if (c instanceof IContinuity) {
        for (int i = 0; i < c.getControlPointCount() - 1; i++)
          for (int j = i + 1; j < c.getControlPointCount(); j++)
            if (((IContinuity) c).arePointsConnected(i, j))
              connections.add(new Connection(c.getControlPoint(i), c.getControlPoint(j)));
      }

      if (a == null || a.getOutlineArea() == null) continue;
      if (a.getContinuityPositiveAreas() != null)
        for (Area a1 : a.getContinuityPositiveAreas()) {
          preliminaryAreas.add(a1);
          checkBreakout.add(false);
        }
      if (a.getContinuityNegativeAreas() != null) {
        for (Area na : a.getContinuityNegativeAreas())
          for (int i = 0; i < preliminaryAreas.size(); i++) {
            Area a1 = preliminaryAreas.get(i);
            if (a1.intersects(na.getBounds2D())) {
              a1.subtract(na);
              checkBreakout.set(i, true);
            }
          }
      }
    }

    // Check if we need to break some areas out in case they are interrupted
    List<Area> areas = new ArrayList<Area>();
    for (int i = 0; i < preliminaryAreas.size(); i++) {
      Area a = preliminaryAreas.get(i);
      // SpotBugs notes that checkBreakout is never used! //ola 20200110
      // if (checkBreakout.get(i))
      areas.addAll(tryBreakout(a));
      // else
      // areas.add(a);
    }

    expandConnections(connections);
    crunchAreas(areas, connections);

    return areas;
  }

  public void expandConnections(Set<Connection> connections) {
    Set<Connection> toAdd = new HashSet<Connection>();
    for (Connection c1 : connections) {
      for (Connection c2 : connections) {
        if (c1 != c2) {
          if (c1.getP1().distance(c2.getP1()) < CONTROL_POINT_SIZE) {
            toAdd.add(new Connection(c1.getP2(), c2.getP2()));
          }
          if (c1.getP1().distance(c2.getP2()) < CONTROL_POINT_SIZE) {
            toAdd.add(new Connection(c1.getP2(), c2.getP1()));
          }
          if (c1.getP2().distance(c2.getP1()) < CONTROL_POINT_SIZE) {
            toAdd.add(new Connection(c1.getP1(), c2.getP2()));
          }
          if (c1.getP2().distance(c2.getP2()) < CONTROL_POINT_SIZE) {
            toAdd.add(new Connection(c1.getP1(), c2.getP1()));
          }
        }
      }
    }
    if (connections.addAll(toAdd)) {
      expandConnections(connections);
    }
  }

  /**
   * Merges all areas that either overlap or are joined by connections.
   *
   * @param areas
   * @param connections
   * @return
   */
  private boolean crunchAreas(List<Area> areas, Set<Connection> connections) {
    boolean isChanged = false;

    List<Area> newAreas = new ArrayList<Area>();
    List<Boolean> consumed = new ArrayList<Boolean>();
    for (int i = 0; i < areas.size(); i++) {
      consumed.add(false);
    }
    for (int i = 0; i < areas.size(); i++) {
      for (int j = i + 1; j < areas.size(); j++) {
        if (consumed.get(j)) continue;
        Area a1 = areas.get(i);
        Area a2 = areas.get(j);
        Area intersection = null;
        if (a1.getBounds2D().intersects(a2.getBounds())) {
          intersection = new Area(a1);
          intersection.intersect(a2);
        }
        if (intersection != null && !intersection.isEmpty()) {
          // if the two areas intersect, make a union and
          // consume the second area
          a1.add(a2);
          consumed.set(j, true);
        } else {
          // maybe there's a connection between them
          for (Connection p : connections) {
            // use getBounds to optimize the computation,
            // don't get into complex math if not needed
            if ((a1.getBounds().contains(p.getP1())
                    && a2.getBounds().contains(p.getP2())
                    && a1.contains(p.getP1())
                    && a2.contains(p.getP2()))
                || (a1.getBounds().contains(p.getP2()) && a2.getBounds().contains(p.getP1()))
                    && a1.contains(p.getP2())
                    && a2.contains(p.getP1())) {

              a1.add(a2);
              consumed.set(j, true);
              break;
            }
          }
        }
      }
    }
    for (int i = 0; i < areas.size(); i++)
      if (!consumed.get(i)) newAreas.add(areas.get(i));
      else isChanged = true;

    if (isChanged) {
      areas.clear();
      areas.addAll(newAreas);
      crunchAreas(areas, connections);
    }

    return isChanged;
  }

  private List<Area> tryBreakout(Area a) {
    List<Area> toReturn = new ArrayList<Area>();
    Path2D p = null;
    PathIterator pathIterator = a.getPathIterator(null);
    while (!pathIterator.isDone()) {
      double[] coord = new double[6];
      int type = pathIterator.currentSegment(coord);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          if (p != null) {
            Area partArea = new Area(p);
            toReturn.add(partArea);
          }
          p = new Path2D.Double();
          p.moveTo(coord[0], coord[1]);
          break;
        case PathIterator.SEG_LINETO:
          p.lineTo(coord[0], coord[1]);
          break;
        case PathIterator.SEG_CUBICTO:
          p.curveTo(coord[0], coord[1], coord[2], coord[3], coord[4], coord[5]);
          break;
        case PathIterator.SEG_QUADTO:
          p.quadTo(coord[0], coord[1], coord[2], coord[3]);
          break;
      }
      pathIterator.next();
    }
    if (p != null) {
      Area partArea = new Area(p);
      toReturn.add(partArea);
    }

    return toReturn;
  }

  /**
     Remove certain components from this project.
   */
  public void removeComponents(Collection<IDIYComponent<?>> componentsToRemove) {
    Iterator<IDIYComponent<?>> i = componentsToRemove.iterator();
    while (i.hasNext()) {
      IDIYComponent<?> c = i.next();
      selection.remove(c);
      components.remove(c);
    }
  }

  /**
     Remove components in selection from this project.
  */
  public void removeSelection() {
    // need a copy of selection to avoid race condition
    //
    // TODO: figure out a way to remove components from project/selection
    // without duplicating selection
    removeComponents(new HashSet<IDIYComponent<?>>(selection));
    clearSelection();
  }

  public void setSelection(Collection<IDIYComponent<?>> newSelection) {
    clearSelection();
    Iterator<IDIYComponent<?>> i = newSelection.iterator();
    while (i.hasNext()) {
      selection.add(i.next());
    }
  }

  public void addToSelection(IDIYComponent<?> c) {
    selection.add(c);
  }

  private IDIYComponent<?> copyWithUniqueName(
      IDIYComponent<?> original) throws CloneNotSupportedException {
    IDIYComponent<?> theCopy = original.clone();
    /* TODO simplify unique name creation
       TODO make it possible to do this for a number of components
       so that existing components need to be traversed only once */
    ComponentType componentType =
        ComponentProcessor.extractComponentTypeFrom(
            (Class<? extends IDIYComponent<?>>) theCopy.getClass());
    theCopy.setName(InstantiationManager.getInstance().createUniqueName(
        componentType,
        getComponents()));
    return theCopy;
  }

  /**
     Duplicate selection.
  */
  public void duplicateSelection() {
    int offset = getGridSpacing().asPixels();
    Set<IDIYComponent<?>> newSelection = new HashSet<>();
    /*
      copy all selected components, move the copies by offset in both
      X and Y directions, place copies in project, set selection to
      copies, notify listeners of project change
    */
    try {
      for (IDIYComponent<?> component : getSelection()) {
        IDIYComponent<?> duplicateComponent = copyWithUniqueName(component);
        // TODO make sure duplicateComponent name is unique
        duplicateComponent.nudge(offset, offset);
        newSelection.add(duplicateComponent);
      }
    } catch (CloneNotSupportedException e) {
      LOG.fatal("duplicateSelection() could not clone component", e);
      throw new RuntimeException(e);
    }
    getComponents().addAll(newSelection);
    clearContinuityArea();
    setSelection(newSelection);
    // TODO clear continuity area
    // TODO notify project change (this is now unsaved & also in need of autosave)
    redraw();
  }

  /**
     Send Repaint message to change listeners.
  */
  private void redraw() {
    Presenter.dispatchMessage(EventType.REPAINT);
  }

  /**
   * Remove all groups that contain at least one of the specified components.
   *
   * @param components
   */
  public void ungroupSelection() {
    Iterator<Set<IDIYComponent<?>>> groupIterator = getGroups().iterator();
    while (groupIterator.hasNext()) {
      Set<IDIYComponent<?>> group = groupIterator.next();
      group.removeAll(getSelection());
      if (group.isEmpty()) {
        groupIterator.remove();
      }
    }
  }

  public void groupSelection() {
    // First remove the selected components from other groups
    ungroupSelection();
    // Then group them together
    getGroups().add(new HashSet<IDIYComponent<?>>(getSelection()));
  }

  public void logTraceSelection() {
    LOG.trace(
        "Project {} selection {}",
        this,
        emptySelection() ? "is empty" : "size " + getSelection().size());
    for (IDIYComponent<?> c : getSelection()) {
      LOG.trace("{} is selected", c.getIdentifier());
    }
  }

  // ****************************************************************
  // properties
  @EditableProperty(defaultable = false, sortOrder = 1)
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @EditableProperty(sortOrder = 3)
  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  @MultiLineText
  @EditableProperty(defaultable = false, sortOrder = 2)
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @EditableProperty(sortOrder = 4)
  public Size getWidth() {
    return width;
  }

  public void setWidth(Size width) {
    this.width = width;
  }

  @EditableProperty(sortOrder = 5)
  public Size getHeight() {
    return height;
  }

  public void setHeight(Size height) {
    this.height = height;
  }

  @EditableProperty(name = "Grid Spacing", validatorClass = SpacingValidator.class, sortOrder = 6)
  public Size getGridSpacing() {
    return gridSpacing;
  }

  public void setGridSpacing(Size gridSpacing) {
    this.gridSpacing = gridSpacing;
  }

  /**
   * List of components sorted by z-order ascending.
   *
   * @return a list of all components in the project
   */
  public List<IDIYComponent<?>> getComponents() {
    return components;
  }

  public boolean contains(IDIYComponent<?> c) {
    return getComponents().contains(c);
  }

  /**
   * Set of grouped components.
   *
   * @return
   */
  public Set<Set<IDIYComponent<?>>> getGroups() {
    return groups;
  }

  public Set<Integer> getLockedLayers() {
    return lockedLayers;
  }

  public Set<Integer> getHiddenLayers() {
    if (hiddenLayers == null) {
      hiddenLayers = new HashSet<Integer>();
    }
    return hiddenLayers;
  }

  public VersionNumber getFileVersion() {
    return fileVersion;
  }

  public void setFileVersion(VersionNumber fileVersion) {
    this.fileVersion = fileVersion;
  }

  @EditableProperty(name = "Default Font")
  public Font getFont() {
    if (font == null) {
      font = DEFAULT_FONT;
    }
    return font;
  }

  public void setFont(Font font) {
    this.font = font;
  }

  @EditableProperty(name = "Default Font Size")
  public int getFontSize() {
    return getFont().getSize();
  }

  public void setFontSize(int size) {
    font = getFont().deriveFont((float) size);
  }

  public List<IDIYComponent<?>> findComponentsAt(Point point) {
    LOG.trace("findComponentsAt({}) {}", point, this);
    List<IDIYComponent<?>> found = new ArrayList<IDIYComponent<?>>();
    LOG.trace("Project has {} components", getComponents().size());
    for (IDIYComponent<?> component : getComponents()) {
      LOG.trace(
          "findComponentsAt({}) {} looking at component {}",
          point,
          this,
          component.getIdentifier());
      // NOTE: BIG CHANGE - look for area directly from component! //ola 20100113
      //ComponentArea area = componentAreaMap.get(component);
      ComponentArea area = component.getArea();
      if (area == null) {
        LOG.trace(
            "findComponentsAt({}) {} component {} has no area",
            point,
            this,
            component.getIdentifier());
      } else {
        boolean isPointInArea = area.getOutlineArea().contains(point);
        LOG.trace(
            "component {} outline area in area {} {} point {}",
            component.getIdentifier(),
            area,
            isPointInArea ? "contains" : "does not contain",
            point);
        if (isPointInArea) {
          found.add(0, component);
        }
      }
    }
    if (!found.isEmpty()) {
      LOG.trace("Found {} components", found.size());
      for (IDIYComponent<?> c : found) {
        LOG.trace("{} was found", c.getIdentifier());
      }
    } else {
      LOG.trace("No components found");
    }
    return found;
  }



  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((author == null) ? 0 : author.hashCode());
    result = prime * result + ((components == null) ? 0 : components.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((height == null) ? 0 : height.hashCode());
    result = prime * result + ((title == null) ? 0 : title.hashCode());
    result = prime * result + ((width == null) ? 0 : width.hashCode());
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
    Project other = (Project) obj;
    if (author == null) {
      if (other.author != null) {
        return false;
      }
    } else if (!author.equals(other.author)) {
      return false;
    }
    if (components == null) {
      if (other.components != null) {
        return false;
      }
    } else if (components.size() != other.components.size()) {
      return false;
    } else {
      Iterator<IDIYComponent<?>> i1 = components.iterator();
      Iterator<IDIYComponent<?>> i2 = other.components.iterator();
      while (i1.hasNext()) {
        IDIYComponent<?> c1 = i1.next();
        IDIYComponent<?> c2 = i2.next();
        if (!c1.equalsTo(c2)) {
          return false;
        }
      }
    }
    if (description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!description.equals(other.description)) {
      return false;
    }
    if (height == null) {
      if (other.height != null) {
        return false;
      }
    } else if (!height.equals(other.height)) {
      return false;
    }
    if (title == null) {
      if (other.title != null) {
        return false;
      }
    } else if (!title.equals(other.title)) {
      return false;
    }
    if (width == null) {
      if (other.width != null) {
        return false;
      }
    } else if (!width.equals(other.width)) {
      return false;
    }
    if (gridSpacing == null) {
      if (other.gridSpacing != null) {
        return false;
      }
    } else if (!gridSpacing.equals(other.gridSpacing)) {
      return false;
    }
    if (font == null) {
      if (other.font != null) {
        return false;
      }
    } else if (!font.equals(other.font)) {
      return false;
    }
    if (groups == null) {
      if (other.groups != null) {
        return false;
      }
    } else if (!groups.equals(other.groups)) {
      return false;
    }
    if (lockedLayers == null) {
      if (other.lockedLayers != null) {
        return false;
      }
    } else if (!lockedLayers.equals(other.lockedLayers)) {
      return false;
    }
    if (hiddenLayers == null) {
      if (other.hiddenLayers != null) {
        return false;
      }
    } else if (!hiddenLayers.equals(other.hiddenLayers)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    if (title == null) {
      return String.format("<Project id=%d created=\"%s\"/>", sequenceNumber, created.toString());
    } else {
      return String.format(
          "<Project id=%d name=\"%s\" created=\"%s\"/>",
          sequenceNumber,
          title,
          created.toString());
    }
  }

  public static class SpacingValidator extends PositiveMeasureValidator {

    @Override
    public void validate(Object value) throws ValidationException {
      super.validate(value);
      Size size = (Size) value;
      if (size.compareTo(new Size(0.1d, SizeUnit.mm)) < 0) {
        throw new ValidationException("must be at least 0.1mm");
      }
      if (size.compareTo(new Size(1d, SizeUnit.in)) > 0) {
        throw new ValidationException("must be less than 1in");
      }
    }
  }

  @Override
  public Project clone() {
    Project project = null;
    try {
      project = (Project) super.clone();
    } catch (CloneNotSupportedException e) {
      // this song'n'dance is to get around declaring 'throws
      // CloneNotSupportedException' as this would require handling in
      // 50+ places in Presenter.java, some of which
      // e.g. mouseClicked() might be tricky to do properly
      //
      // TODO: end goal is to rewrite all uses of Project.clone() to
      // do something completely different //ola 20100110
      throw new RuntimeException(e);
    }
    project.sequenceNumber = nextSequenceNumber();
    project.setTitle(this.getTitle());
    project.setAuthor(this.getAuthor());
    project.setDescription(this.getDescription());
    project.setFileVersion(this.getFileVersion());
    project.setGridSpacing(this.getGridSpacing());
    project.setHeight(this.getHeight());
    project.setWidth(this.getWidth());
    project.getLockedLayers().addAll(this.getLockedLayers());
    project.getHiddenLayers().addAll(this.getHiddenLayers());
    project.setFont(this.getFont());

    Map<IDIYComponent<?>, IDIYComponent<?>> cloneMap =
        new HashMap<IDIYComponent<?>, IDIYComponent<?>>();

    List<IDIYComponent<?>> addedComponents = new ArrayList<>();
    for (IDIYComponent<?> component : this.components) {
      /*
      IDIYComponent<?> clone = null;
      try {
        clone = component.clone();
        LOG.trace(
            "Cloning {}, got {}",
            component.getIdentifier(),
            clone.getIdentifier());
      } catch (CloneNotSupportedException e) {
        // see above
        LOG.error("Could not clone {}", component.getIdentifier());
        throw new RuntimeException(e);
      }
      addedComponents.add(clone);
      cloneMap.put(component, clone);
      */
      // on 2nd thoughts let's not make a deep clone --ola 20200114
      addedComponents.add(component);
      cloneMap.put(component, component);
    }
    project.getComponents().clear();
    project.getComponents().addAll(addedComponents);

    for (Set<IDIYComponent<?>> group : this.groups) {
      Set<IDIYComponent<?>> cloneGroup = new HashSet<IDIYComponent<?>>();
      for (IDIYComponent<?> component : group) {
        cloneGroup.add(cloneMap.get(component));
      }
      project.groups.add(cloneGroup);
    }
    return project;
  }
}
