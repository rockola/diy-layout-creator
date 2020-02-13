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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.App;
import org.diylc.common.Config;
import org.diylc.common.Display;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.SiUnit;
import org.diylc.core.measures.Value;
import org.diylc.parsing.XmlNode;
import org.diylc.presenter.ComponentArea;

/**
 * Base class for all components.
 *
 * @author Branislav Stojkovic
 */
public abstract class AbstractComponent {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(AbstractComponent.class);

  public static final int CHASSIS = 1;
  public static final int BOARD = 2;
  public static final int TRACE = 3;
  public static final int COMPONENT = 4;
  public static final int TEXT = 5;

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

  protected transient int sequenceNumber = 0;

  protected Point[] controlPoints;
  protected SiUnit valueUnit;

  private static int componentSequenceNumber = 0;

  private String name = "";
  private Value value;
  private String stringValue = "";
  private Display display = Display.NAME;

  private transient ComponentArea componentArea;

  private int nextSequenceNumber() {
    componentSequenceNumber = componentSequenceNumber + 1;
    return componentSequenceNumber;
  }

  @EditableProperty(defaultable = false)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @EditableProperty
  public final Value getValue() {
    return value;
  }

  public final void setValue(Value value) {
    if (value == null || valueUnit != null || value.getUnit() == valueUnit) {
      this.value = value;
    }
  }

  @EditableProperty(name = "Value")
  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String value) {
    this.stringValue = value;
  }

  @EditableProperty
  public Display getDisplay() {
    return display;
  }

  public void setDisplay(Display value) {
    this.display = value;
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

  /**
   * @param index
   * @return true, if the specified control point may overlap with other control points <b>of the
   *     same component</b>. The other control point must be able to overlap too.
   */
  public boolean canControlPointOverlap(int index) {
    return false;
  }

  public String toString() {
    return name;
  }

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
   * Copy control points from another component.
   *
   * <p>The purpose of this method is to replace other with this, saving location from other.
   *
   * @param other Other component.
   */
  public void copyControlPoints(AbstractComponent other) {
    if (other != null) {
      if (other.getControlPointCount() != this.getControlPointCount()) {
        throw new RuntimeException(
            String.format(
                "copyControlPoints(%s) other has %d points while this has %d",
                other.getName(), other.getControlPointCount(), this.getControlPointCount()));
      }
      for (int i = 0; i < other.getControlPointCount(); i++) {
        this.controlPoints[i] = new Point(other.controlPoints[i]);
      }
    }
  }

  public final int getControlPointCount() {
    return getControlPoints() == null ? 0 : getControlPoints().length;
  }

  /**
   * Get control point with specified index.
   *
   * @param nth
   * @return nth control point.
   */
  public abstract Point getControlPoint(int nth);

  protected Point[] getControlPoints() {
    return controlPoints;
  }

  /**
   * Updates the control point at the specified index.
   *
   * @param point
   * @param index
   */
  public abstract void setControlPoint(Point point, int nth);

  public abstract void drawIcon(Graphics2D g2d, int width, int height);

  /**
   * @param index
   * @return true if the specified control point may stick to control points of other components.
   */
  public boolean isControlPointSticky(int index) {
    return false;
  }

  /**
   * @param index Index of control point
   * @return visibility policy associated with control point.
   */
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  /**
   * Draws the component onto the {@link Graphics2D}.
   *
   * @param g2d
   * @param componentState
   * @param outlineMode
   * @param project
   * @param drawingObserver
   */
  public abstract void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver);

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

  public AbstractComponent clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException(
        "clone() not implemented for " + this.getClass().getName());
  }

  // TODO implement this for all subclasses
  // remember to call super.isEqualTo(other) first
  public boolean isEqualTo(AbstractComponent other) {
    if (other == null || !other.getClass().equals(this.getClass())) {
      return false;
    }
    if ((name != other.name
        || (name == null && other.name != null)
        || (name != null && other.name == null)
        || !name.equals(other.name))) {
      return false;
    }
    if (display != other.display) {
      return false;
    }
    return true;
  }

  protected Point[] getFreshControlPoints(int howMany) {
    Point[] array = new Point[howMany];
    for (int i = 0; i < howMany; i++) {
      array[i] = new Point(0, 0);
    }
    return array;
  }

  public String getControlPointNodeName(int index) {
    return Integer.toString(index + 1);
  }

  public String getInternalLinkName(int index1, int index2) {
    return null;
  }

  public String[] getSectionNames(int pointIndex) {
    return null;
  }

  public String getCommonPointName(int pointIndex) {
    return null;
  }

  public boolean canPointMoveFreely(int pointIndex) {
    return true;
  }

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

  public List<AbstractComponent> getDefaultVariants() {
    return new ArrayList<AbstractComponent>();
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
