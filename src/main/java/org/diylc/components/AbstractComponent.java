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

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.App;
import org.diylc.common.Config;
import org.diylc.common.Display;
import org.diylc.core.ComponentState; // should probably be in this package
import org.diylc.core.IDIYComponent; // should probably be in this package
import org.diylc.core.Theme;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.parsing.XmlNode;
import org.diylc.presenter.ComponentArea; // should probably be in this package

/**
 * Abstract implementation of {@link IDIYComponent} that contains component name and toString.
 *
 * <p>IMPORTANT: to improve performance, all fields except for <code>Point</code> and <code>Point
 * </code> arrays should be immutable. Failing to comply with this can result in annoying and hard
 * to trace bugs.
 *
 * @author Branislav Stojkovic
 * @param <T> Value type.
 */
public abstract class AbstractComponent<T> implements IDIYComponent<T> {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(AbstractComponent.class);

  public static final Color CANVAS_COLOR = Color.white;
  public static final Color SELECTION_COLOR = Color.red;
  public static final Color LABEL_COLOR = Color.black;
  public static final Color LABEL_COLOR_SELECTED = Color.red;
  public static final Font LABEL_FONT = new Font(Config.getString("font.label"), Font.PLAIN, 14);
  public static final Color METAL_COLOR = Color.decode("#759DAF");
  public static final Color LIGHT_METAL_COLOR = Color.decode("#EEEEEE");
  public static final Color COPPER_COLOR = Color.decode("#DA8A67");
  public static final int ICON_SIZE = 32;

  protected static final double HALF_PI = Math.PI / 2;
  protected static final double SQRT_TWO = Math.sqrt(2);

  protected String name = "";
  protected Display display = Display.NAME;
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

  /** Clears component area. */
  public void resetArea() {
    setArea(null);
  }

  protected transient boolean outlineMode;

  public boolean getOutlineMode() {
    return outlineMode;
  }

  public void setOutlineMode(boolean outlineMode) {
    this.outlineMode = outlineMode;
  }

  public void resetOutlineMode() {
    this.outlineMode = false;
  }

  protected transient ComponentState componentState;

  public ComponentState getState() {
    return componentState;
  }

  public void setState(ComponentState state) {
    componentState = state;
  }

  public void resetState() {
    setState(null);
  }

  public boolean isSelected() {
    return componentState.isSelected();
  }

  public boolean isDragging() {
    return componentState.isDragging();
  }

  public boolean isSelectedOrDragging() {
    return componentState.isSelectedOrDragging();
  }

  protected Theme theme() {
    return App.getTheme();
  }

  private Color getColorUnlessSelectedOrDragging(
      boolean outlineMode, Color selectedOrDraggingColor, Color color) {
    if (isSelectedOrDragging()) {
      return selectedOrDraggingColor;
    }
    return outlineMode ? theme().getOutlineColor() : color;
  }

  /**
   * Use color unless in outline mode, selected, or dragging.
   *
   * <p>If in outline mode, returns outline color specified in theme. Otherwise if selected or
   * dragging, returns SELECTION_COLOR. Finally, returns the given color.
   *
   * @param outlineMode true if outline mode color should override.
   * @param color Color to use unless outline mode/selection/drag overrides.
   * @return color
   */
  public Color tryColor(boolean outlineMode, Color color) {
    return getColorUnlessSelectedOrDragging(outlineMode, SELECTION_COLOR, color);
  }

  /**
   * Use border color unless in outline mode, selected, or dragging.
   *
   * <p>Defaults to calling tryColor, which means using SELECTION_COLOR when not in outline mode and
   * selected or dragging. Override this in subclass if different selected/dragging color is needed.
   *
   * @param outlineMode true if outline mode color should override.
   * @param color Color to use unless outline mode/selection/drag overrides.
   * @return color
   */
  public Color tryBorderColor(boolean outlineMode, Color color) {
    return tryColor(outlineMode, color);
  }

  /**
   * Use color unless in outline mode, selected, or dragging.
   *
   * <p>If in outline mode, returns outline color specified in theme. Otherwise if selected or
   * dragging, returns LABEL_COLOR_SELECTED. Finally, returns the given color.
   *
   * @param outlineMode true if outline mode color should override.
   * @param color Color to use unless outline mode/selection/drag overrides.
   * @return color
   */
  public Color tryLabelColor(boolean outlineMode, Color color) {
    return getColorUnlessSelectedOrDragging(outlineMode, LABEL_COLOR_SELECTED, color);
  }

  /**
   * Use color unless selected or dragging.
   *
   * <p>If selected or dragging, returns SELECTION_COLOR.
   *
   * @param color Color to use unless selected or dragging.
   * @return color unless selected or dragging, defaults to SELECTION_COLOR otherwise
   */
  public Color tryLeadColor(Color color) {
    return getColorUnlessSelectedOrDragging(false, SELECTION_COLOR, color);
  }

  /** Draw or fill rectangle according to outline mode. */
  public void drawFillRect(
      Graphics2D g2d, boolean outlineMode, int x, int y, int width, int height) {
    if (outlineMode) {
      g2d.drawRect(x, y, width, height);
    } else {
      g2d.fillRect(x, y, width, height);
    }
  }

  public Point midpoint(Point a, Point b) {
    return new Point((a.x + b.x) / 2, (a.y + b.y) / 2);
  }

  @Override
  public String getIdentifier() {
    if (sequenceNumber == 0) {
      // has to be inited here as won't/can't be inited when unmarshaling
      sequenceNumber = nextSequenceNumber();
    }
    return String.format(
        "<%s id=%d name=\"%s\"/>", this.getClass().getName(), sequenceNumber, getName());
  }

  protected String getLabelForDisplay() {
    String s = "";
    switch (display) {
      case NAME:
        s = getName();
        break;
      case NONE:
        break;
      case BOTH:
        s = getName() + " ";
        // fallthrough intentional
      case VALUE:
        // fallthrough intentional
      default:
        if (getValue() != null) {
          s = s + getValue().toString();
        }
    }
    return s;
  }

  protected List<String> getLabelListForDisplay() {
    List<String> strings = new ArrayList<>();
    switch (display) {
      case NAME:
        strings.add(getName());
        break;
      case NONE:
        strings.add("");
      case BOTH:
        strings.add(getName());
        // fallthrough intentional
      case VALUE:
        // fallthrough intentional
      default:
        String value = getValue().toString();
        if (!value.isEmpty()) {
          strings.add(value);
        }
    }
    return strings;
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
  protected boolean checkPointsClipped(Area clip) {
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

  protected boolean checkPointsClipped(Shape clip) {
    return checkPointsClipped(new Area(clip));
  }

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
          if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
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
            if (value instanceof Point) {
              value = new Point((Point) value);
            }

            field.set(newInstance, value);
          }
        }
      }
      return newInstance;
    } catch (Exception e) {
      throw new CloneNotSupportedException("Could not clone the component. " + e.getMessage());
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

  protected Point[] getFreshControlPoints(int howMany) {
    Point[] array = new Point[howMany];
    for (int i = 0; i < howMany; i++) {
      array[i] = new Point(0, 0);
    }
    return array;
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

  public static double deltaX(Point p1, Point p2) {
    return p2.x - p1.x;
  }

  public static double deltaY(Point p1, Point p2) {
    return p2.y - p1.y;
  }

  public static double distance(Point p1, Point p2) {
    return Math.hypot(deltaX(p1, p2), deltaY(p1, p2));
  }

  public Icon getImageIcon() {
    Image image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = (Graphics2D) image.getGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    drawIcon(g2d, ICON_SIZE, ICON_SIZE);
    return new ImageIcon(image);
  }

  /** ************************************************************** Unmarshal XML */

  /** TODO: turn this into an exception class */
  private RuntimeException unsupportedUnmarshalFunction(String f) {
    return new RuntimeException(f + "() not supported for " + getClass().getName());
  }

  public void setAngle(String s) {
    throw unsupportedUnmarshalFunction("setAngle");
  }

  public void setShowLabels(String s) {
    throw unsupportedUnmarshalFunction("setShowLabels");
  }

  protected ListMultimap<String, XmlNode> fillField(
      ListMultimap<String, XmlNode> contents,
      String key,
      final BiConsumer<AbstractComponent, String> setter) {

    List<XmlNode> payload = contents.get(key);
    for (XmlNode node : payload) {
      setter.accept(this, node.getValue());
    }
    contents.removeAll(key);
    return contents;
  }

  protected ListMultimap<String, XmlNode> fillFields(ListMultimap<String, XmlNode> contents) {
    LOG.trace("fillFields(...)");
    contents = fillField(contents, "name", AbstractComponent::setName);
    return contents;
  }

  protected ListMultimap<String, XmlNode> fillFields(XmlNode node) {
    // handle node attributes here if needed
    return fillFields(node.children);
  }
}
