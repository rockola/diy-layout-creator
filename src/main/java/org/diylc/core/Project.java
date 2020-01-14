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
import org.diylc.common.Config;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.MultiLineText;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

/**
 * Entity class that defines a project. Contains project properties
 * and a collection of components.This class is serialized to
 * file. Some filed getters are tagged with {@link EditableProperty}
 * to enable for user to edit them.
 *
 * @author Branislav Stojkovic
 */
public class Project implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(Project.class);

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
  // should really be called "instantiated"
  private transient Date created = new Date();
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

  /**
   * Removes all the groups that contain at least one of the specified components.
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
   * @return
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
