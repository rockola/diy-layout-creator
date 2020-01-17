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

package org.diylc.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.diylc.App;
import org.diylc.common.Config;
import org.diylc.core.IDIYComponent; // should probably be in this package
import org.diylc.core.ComponentState; // should probably be in this package
import org.diylc.core.Theme;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.presenter.ComponentArea; // should probably be in this package

/**
 * Abstract implementation of {@link IDIYComponent} that contains
 * component name and toString.
 *
 * <p>IMPORTANT: to improve performance, all fields except for
 * <code>Point</code> and <code>Point </code> arrays should be
 * immutable. Failing to comply with this can result in annoying and
 * hard to trace bugs.
 *
 * @author Branislav Stojkovic
 * @param <T>
 */
public abstract class AbstractComponent<T> implements IDIYComponent<T> {

  private static final long serialVersionUID = 1L;

  protected String name = "";

  public static Color SELECTION_COLOR = Color.red;
  public static Color LABEL_COLOR = Color.black;
  public static Color LABEL_COLOR_SELECTED = Color.red;
  public static Font LABEL_FONT = new Font(Config.getString("font.label"), Font.PLAIN, 14);
  public static Color METAL_COLOR = Color.decode("#759DAF");
  public static Color LIGHT_METAL_COLOR = Color.decode("#EEEEEE");
  public static Color COPPER_COLOR = Color.decode("#DA8A67");

  protected static Theme theme = (Theme) App.getObject(Theme.THEME_KEY, Theme.DEFAULT_THEME);

  protected transient int sequenceNumber = 0;

  private static int componentSequenceNumber = 0;

  private transient ComponentArea componentArea;

  private int nextSequenceNumber() {
    componentSequenceNumber = componentSequenceNumber + 1;
    return componentSequenceNumber;
  }

  public ComponentArea getArea() {
    return componentArea;
  }

  public void setArea(ComponentArea area) {
    componentArea = area;
  }

  /**
     Clears component area.
  */
  public void resetArea() {
    setArea(null);
  }

  private transient ComponentState componentState;

  public ComponentState getState() {
    return componentState;
  }

  public void setState(ComponentState state) {
    componentState = state;
  }

  public void resetState() {
    setState(null);
  }

  public boolean isSelectedOrDragging() {
    return (componentState == ComponentState.SELECTED
            || componentState == ComponentState.DRAGGING);
  }

  private Color getColorUnlessSelectedOrDragging(
      boolean outlineMode,
      Color selectedOrDraggingColor,
      Color color) {
    if (isSelectedOrDragging()) {
      return selectedOrDraggingColor;
    }
    return outlineMode ? theme.getOutlineColor() : color;
  }

  /**
     Use color unless in outline mode, selected, or dragging.

     If in outline mode, returns outline color specified in theme.
     Otherwise if selected or dragging, returns SELECTION_COLOR.
     Finally, returns the given color.

     @param outlineMode true if outline mode color should override.
     @param color Color to use unless outline mode/selection/drag overrides.
     @returns color
   */
  public Color tryColor(boolean outlineMode, Color color) {
    return getColorUnlessSelectedOrDragging(outlineMode, SELECTION_COLOR, color);
  }

  /**
     Use border color unless in outline mode, selected, or dragging.

     Defaults to calling tryColor, which means using SELECTION_COLOR
     when not in outline mode and selected or dragging. Override this
     in subclass if different selected/dragging color is needed.

     @param outlineMode true if outline mode color should override.
     @param color Color to use unless outline mode/selection/drag overrides.
     @returns color
   */
  public Color tryBorderColor(boolean outlineMode, Color color) {
    return tryColor(outlineMode, color);
  }

  /**
     Use color unless in outline mode, selected, or dragging.

     If in outline mode, returns outline color specified in theme.
     Otherwise if selected or dragging, returns LABEL_COLOR_SELECTED.
     Finally, returns the given color.

     @param outlineMode true if outline mode color should override.
     @param color Color to use unless outline mode/selection/drag overrides.
     @returns color
   */
  public Color tryLabelColor(boolean outlineMode, Color color) {
    return getColorUnlessSelectedOrDragging(outlineMode, LABEL_COLOR_SELECTED, color);
  }

  /**
     Use color unless selected or dragging.

     If selected or dragging, returns SELECTION_COLOR.

     @param color Color to use unless selected or dragging.
     @returns color unless selected or dragging, defaults to SELECTION_COLOR otherwise
   */
  public Color tryLeadColor(Color color) {
    return getColorUnlessSelectedOrDragging(false, SELECTION_COLOR, color);
  }

  /**
     Draw or fill rectangle according to outline mode.
  */
  public void drawFillRect(
      Graphics2D g2d,
      boolean outlineMode,
      int x,
      int y,
      int width,
      int height) {
    if (outlineMode) {
      g2d.drawRect(x, y, width, height);
    } else {
      g2d.fillRect(x, y, width, height);
    }
  }

  @Override
  public String getIdentifier() {
    if (sequenceNumber == 0) {
      // has to be inited here as won't/can't be inited when unmarshaling
      sequenceNumber = nextSequenceNumber();
    }
    return String.format(
        "<%s id=%d name=\"%s\"/>",
        this.getClass().getName(),
        sequenceNumber,
        getName());
  }

  @EditableProperty(defaultable = false)
  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean canControlPointOverlap(int index) {
    return false;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public String getValueForDisplay() {
    return getValue() == null ? "" : getValue().toString();
  }

  /**
   * Returns the closest odd number, i.e. x when x is odd, or x + 1 when x is even.
   *
   * @param x
   * @return
   */
  protected int getClosestOdd(double x) {
    return ((int) x / 2) * 2 + 1;
  }

  /**
   * Returns darker color if possible, or lighter if it's already dark.
   *
   * @param color
   * @return
   */
  protected Color darkerOrLighter(Color color) {
    float[] hsb = new float[3];
    Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
    return new Color(
        Color.HSBtoRGB(hsb[0], hsb[1], hsb[2] > 0.5 ? hsb[2] - 0.25f : hsb[2] + 0.25f));
  }

  /**
   * @param clip
   * @return true if none of the control points lie in the clip rectangle.
   */
  protected boolean checkPointsClipped(Shape clip) {
    for (int i = 0; i < getControlPointCount(); i++) {
      if (clip.contains(getControlPoint(i))) {
        return false;
      }
    }
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    for (int i = 0; i < getControlPointCount(); i++) {
      Point p = getControlPoint(i);
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
    Rectangle2D rect = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    return !clip.intersects(rect);
  }

  @SuppressWarnings("unchecked")
  public IDIYComponent<T> clone() throws CloneNotSupportedException {
    try {
      // Instantiate object of the same type
      AbstractComponent<T> newInstance =
          (AbstractComponent<T>) this.getClass().getConstructors()[0].newInstance();
      Class<?> clazz = this.getClass();
      while (AbstractComponent.class.isAssignableFrom(clazz)) {
        Field[] fields = clazz.getDeclaredFields();
        clazz = clazz.getSuperclass();
        // fields = this.getClass().getDeclaredFields();
        // Copy over all non-static, non-final fields that are declared
        // in AbstractComponent or one of it's child classes
        for (Field field : fields) {
          if (!Modifier.isStatic(field.getModifiers())
              && !Modifier.isFinal(field.getModifiers())) {
            field.setAccessible(true);
            Object value = field.get(this);

            // Deep copy point arrays.
            // TODO: something nicer
            if (value != null
                && value.getClass().isArray()
                && value.getClass().getComponentType().isAssignableFrom(Point.class)) {
              Object newArray =
                  Array.newInstance(value.getClass().getComponentType(), Array.getLength(value));
              for (int i = 0; i < Array.getLength(value); i++) {
                Point p = (Point) Array.get(value, i);
                Array.set(newArray, i, new Point(p));
              }
              value = newArray;
            }
            // Deep copy points.
            // TODO: something nicer
            if (value != null && value instanceof Point) {
              value = new Point((Point) value);
            }

            field.set(newInstance, value);
          }
        }
      }
      return newInstance;
    } catch (Exception e) {
      throw new CloneNotSupportedException(
          "Could not clone the component. " + e.getMessage());
    }
  }

  @Override
  public boolean isEqualTo(IDIYComponent<?> other) {
    if (other == null) {
      return false;
    }
    if (!other.getClass().equals(this.getClass())) {
      return false;
    }
    Class<?> clazz = this.getClass();
    while (AbstractComponent.class.isAssignableFrom(clazz)) {
      Field[] fields = clazz.getDeclaredFields();
      clazz = clazz.getSuperclass();
      // fields = this.getClass().getDeclaredFields();
      // Copy over all non-static, non-final fields that are declared
      // in
      // AbstractComponent or one of it's child classes
      for (Field field : fields) {
        if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
          field.setAccessible(true);
          try {
            Object value = field.get(this);
            Object otherValue = field.get(other);
            if (!compareObjects(value, otherValue)) {
              return false;
            }
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    return true;
  }

  private boolean compareObjects(Object o1, Object o2) {
    if (o1 == null && o2 == null) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }
    if (o1.getClass().isArray()) {
      if (o1.getClass().getComponentType() == byte.class) {
        return Arrays.equals((byte[]) o1, (byte[]) o2);
      }
      return Arrays.equals((Object[]) o1, (Object[]) o2);
    }
    return o1.equals(o2);
  }

  @Override
  public String getControlPointNodeName(int index) {
    return Integer.toString(index + 1);
  }

  @Override
  public String getInternalLinkName(int index1, int index2) {
    return null;
  }

  @Override
  public String[] getSectionNames(int pointIndex) {
    return null;
  }

  @Override
  public String getCommonPointName(int pointIndex) {
    return null;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return true;
  }

  @Override
  public void nudge(int offsetX, int offsetY) {
    for (int i = 0; i < getControlPointCount(); i++) {
      Point p = getControlPoint(i);
      p.x += offsetX;
      p.y += offsetY;
      setControlPoint(p, i);
    }
  }
}
