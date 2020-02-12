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

import java.awt.Graphics2D;
import java.awt.Point;
import java.io.Serializable;
import java.util.List;
import javax.swing.Icon;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.presenter.ComponentArea;

/**
 * Interface for component instance. Implementation classes of this interface will be instantiated
 * by the application when component is added to the canvas.
 *
 * <p><b>Implementing classes should meet the following: </b>
 *
 * <ul>
 *   <li>Must have an empty constructor.
 *   <li>Class should be annotated with {@link ComponentDescriptor}.
 *   <li>Getters for properties editable by users should be annotated with {@link EditableProperty}.
 *   <li>Component configuration should be stored in <code>public static</code> fields so they can
 *       be set through config file.
 * </ul>
 *
 * @author Branislav Stojkovic
 * @param <T> type of component values, e.g. Resistance for resistors or String for transistors.
 */
public interface IDIYComponent<T> extends Serializable {

  int CHASSIS = 1;
  int BOARD = 2;
  int TRACE = 3;
  int COMPONENT = 4;
  int TEXT = 5;

  /** @return identifier for component for e.g. debugging. */
  String getIdentifier();

  ComponentArea getArea();

  void setArea(ComponentArea area);

  void resetArea();

  ComponentState getState();

  void setState(ComponentState state);

  void resetState();

  boolean getOutlineMode();

  void setOutlineMode(boolean outlineMode);

  void resetOutlineMode();

  /** @return component instance name. */
  String getName();

  /**
   * Updates component instance name.
   *
   * @param name
   */
  void setName(String name);

  /** @return component value. */
  T getValue();

  /**
   * Updates component value.
   *
   * @param value
   */
  void setValue(T value);

  /**
   * @return number of control points for this component instance. May vary between two instances of
   *     the same type, e.g. DIL IC with 8 and 16 pins will have 8 or 16 pins although they are of
   *     the same type.
   */
  int getControlPointCount();

  /**
   * @param index
   * @return control point at the specified index.
   */
  Point getControlPoint(int index);

  /**
   * Updates the control point at the specified index.
   *
   * @param point
   * @param index
   */
  void setControlPoint(Point point, int index);

  /**
   * @param index
   * @return true, if the specified control point may stick to control points of other components.
   */
  boolean isControlPointSticky(int index);

  /**
   * @param index
   * @return true, if the specified control point may overlap with other control points <b>of the
   *     same component</b>. The other control point must be able to overlap too.
   */
  boolean canControlPointOverlap(int index);

  /**
   * @param index
   * @return {@link VisibilityPolicy} of the control point.
   */
  VisibilityPolicy getControlPointVisibilityPolicy(int index);

  /**
   * @param index
   * @return name of the control point node, if the control point represents a graph node, null
   *     otherwise.
   */
  String getControlPointNodeName(int index);

  /**
   * @param index1
   * @param index2
   * @return name of the internal linked formed by the two control points, null if they do not form
   *     an internal link
   */
  String getInternalLinkName(int index1, int index2);

  /**
   * @param pointIndex
   * @return name of the internal section containing the specified point, or null if the component
   *     does not have internal sections. One point can be shared between multiple sections, in
   *     which case, the result may contain more than one element.
   */
  String[] getSectionNames(int pointIndex);

  /**
   * @param pointIndex
   * @return name of the common point that joins all the other points with the same name together,
   *     e.g. GND
   */
  String getCommonPointName(int pointIndex);

  /**
   * @param pointIndex
   * @return true if the control point can move without affecting other points' position, false
   *     otherwise
   */
  boolean canPointMoveFreely(int pointIndex);

  /**
   * Move all control points of the component by an offset.
   *
   * <p>Offset is specified separately for both horizontal and vertical directions.
   *
   * @param offsetX X offset
   * @param offsetY Y offset
   */
  void nudge(int offsetX, int offsetY);

  /**
   * Draws the component onto the {@link Graphics2D}.
   *
   * @param g2d
   * @param componentState
   * @param outlineMode
   * @param project
   * @param drawingObserver
   */
  void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver);

  /**
   * Draws icon representation of the component. This should not depend on component state, i.e. it
   * should be treated as a static method.
   *
   * @param g2d
   * @param width
   * @param height
   */
  void drawIcon(Graphics2D g2d, int width, int height);

  Icon getImageIcon();

  /**
   * Clones the component.
   *
   * @return
   */
  IDIYComponent<T> clone() throws CloneNotSupportedException;

  /** @return full value for BOM. */
  String getValueForDisplay();

  /**
   * Get a list of default variants.
   *
   * @return List (possibly empty) of component instances.
   */
  List<IDIYComponent<T>> getDefaultVariants();

  /**
   * Checks if two components are equal.
   *
   * @param other
   * @return
   */
  boolean isEqualTo(IDIYComponent<?> other);
}
