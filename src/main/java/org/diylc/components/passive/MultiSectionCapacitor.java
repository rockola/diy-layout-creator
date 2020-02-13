/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2018 held jointly by the individual authors.

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

package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import org.diylc.awt.StringUtils;
import org.diylc.common.Display;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractComponent;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.ComponentValue;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.MultipleValues;
import org.diylc.core.measures.SiUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.Value;
import org.diylc.utils.Constants;

@ComponentValue(SiUnit.FARAD)
@ComponentDescriptor(
    name = "Multi-Section Capacitor",
    author = "Branislav Stojkovic",
    category = "Passive",
    instanceNamePrefix = "C",
    description =
        "Multi-section vertically mounted electrolytic capacitor, similar to JJ, CE and others",
    zOrder = AbstractComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_VALUE)
public class MultiSectionCapacitor extends AbstractTransparentComponent {

  private static final long serialVersionUID = 1L;

  public static final Color BODY_COLOR = Color.decode("#6B6DCE");
  public static final Color BASE_COLOR = Color.decode("#333333");
  public static final Color BORDER_COLOR = BODY_COLOR.darker();
  public static final Color PIN_COLOR = METAL_COLOR; // Color.decode("#00B2EE");
  //  public static final Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static final Color LABEL_COLOR = Color.white;
  public static final Size PIN_SIZE = Size.in(0.08);
  //  public static final Size PIN_SPACING = Size.in(0.05);
  public static final Size BODY_DIAMETER = Size.in(1);

  /**
   * Maximum number of capacitor sections.
   *
   * <p>In the absence of a better guess there can be 1...9 sections, but the number of sections in
   * an actual manufactured multi-section capacitor is probably way less than 9. Any better guesses?
   */
  private static final int MAX_SECTIONS = 9;

  private static final double[] RELATIVE_DIAMETERS = new double[] {0.4d, 0.6d};
  private static final Format format = new DecimalFormat("0.#####");

  private Value voltage = null;

  private int sections = 3;
  private Orientation orientation = Orientation.DEFAULT;
  private transient Area[] body;
  private Color bodyColor = BODY_COLOR;
  private Color baseColor = BASE_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  private Color pinColor = PIN_COLOR;
  //  private Size pinSpacing = PIN_SPACING;
  private Size diameter = BODY_DIAMETER;

  private List<Value> values = new ArrayList<>();

  public MultiSectionCapacitor() {
    super();
    controlPoints = getFreshControlPoints(sections);
    updateControlPoints();
    setDisplay(Display.NAME);
  }

  @MultipleValues(9)
  @ComponentValue(SiUnit.FARAD)
  @EditableProperty
  public List<Value> getValues() {
    return values;
  }

  public void setValues(List<Value> values) {
    boolean needsUpdate = false;
    if ((this.values == null ? 0 : this.values.size()) != (values == null ? 0 : values.size())) {
      needsUpdate = true;
    }

    this.values = values;

    if (needsUpdate) {
      updateControlPoints();
      body = null;
    }
  }

  /*
  @Override
  private String getStringValue() {
    if (value == null || value.length == 0) {
      return "";
    }
    StringJoiner sb = new StringJoiner("/");
    for (Capacitance c : value) {
      sb.add(c == null || c.getValue() == null ? "" : format.format(c.getValue()));
    }
    return value[0] != null
        ? sb.toString()
        : sb.toString() + " " + (value[0].getUnit() == null ? "" : value[0].getUnit());
  }
  */

  @ComponentValue(SiUnit.VOLT)
  @EditableProperty
  public Value getVoltage() {
    return voltage;
  }

  public void setVoltage(Value voltage) {
    if (voltage == null || voltage.getUnit() == SiUnit.VOLT) {
      this.voltage = voltage;
    }
  }

  @EditableProperty
  public int getSections() {
    return sections;
  }

  public void setSections(int sections) {
    if (sections > 0 && sections <= MAX_SECTIONS) {
      this.sections = sections;
    }
  }

  @EditableProperty
  public Size getDiameter() {
    return diameter;
  }

  public void setDiameter(Size diameter) {
    this.diameter = diameter;
    // Reset body shape;
    updateControlPoints();
    body = null;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  private void updateControlPoints() {
    int pinSpacing =
        (int)
            (getDiameter().convertToPixels()
                * RELATIVE_DIAMETERS[values == null || values.size() == 1 ? 0 : 1]);

    int newCount = values.size() + 1;
    if (newCount != controlPoints.length) {
      // need new control points
      Point[] newPoints = new Point[newCount];
      newPoints[0] = controlPoints[0];
      for (int i = 1; i < newCount; i++) {
        newPoints[i] = new Point(0, 0);
      }
      controlPoints = newPoints;
    }

    // Update control points.
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;

    if (newCount == 2) {
      switch (orientation) {
        case DEFAULT:
          controlPoints[1].setLocation(x, y + pinSpacing);
          break;
        case _90:
          controlPoints[1].setLocation(x - pinSpacing, y);
          break;
        case _180:
          controlPoints[1].setLocation(x, y - pinSpacing);
          break;
        case _270:
          controlPoints[1].setLocation(x + pinSpacing, y);
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
    } else if (newCount == 3) {
      switch (orientation) {
        case DEFAULT:
          controlPoints[1].setLocation(x + pinSpacing / 2, y + pinSpacing / 2);
          controlPoints[2].setLocation(x - pinSpacing / 2, y + pinSpacing / 2);
          break;
        case _90:
          controlPoints[1].setLocation(x - pinSpacing / 2, y - pinSpacing / 2);
          controlPoints[2].setLocation(x - pinSpacing / 2, y + pinSpacing / 2);
          break;
        case _180:
          controlPoints[1].setLocation(x - pinSpacing / 2, y - pinSpacing / 2);
          controlPoints[2].setLocation(x + pinSpacing / 2, y - pinSpacing / 2);
          break;
        case _270:
          controlPoints[1].setLocation(x + pinSpacing / 2, y - pinSpacing / 2);
          controlPoints[2].setLocation(x + pinSpacing / 2, y + pinSpacing / 2);
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
    } else {
      double theta = Math.PI * 2 / newCount;
      double centerX;
      double centerY;
      double theta0;

      switch (orientation) {
        case DEFAULT:
          centerX = x;
          centerY = y + pinSpacing / 2;
          theta0 = -HALF_PI;
          break;
        case _90:
          centerX = x - pinSpacing / 2;
          centerY = y;
          theta0 = 0;
          break;
        case _180:
          centerX = x;
          centerY = y - pinSpacing / 2;
          theta0 = HALF_PI;
          break;
        case _270:
          centerX = x + pinSpacing / 2;
          centerY = y;
          theta0 = -Math.PI;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }

      for (int i = 1; i < newCount; i++) {
        controlPoints[i].setLocation(
            centerX + Math.cos(theta0 + theta * i) * pinSpacing / 2,
            centerY + Math.sin(theta0 + theta * i) * pinSpacing / 2);
      }
    }
  }

  public Area[] getBody() {
    if (body == null) {
      double centerX;
      double centerY;
      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int pinSpacing =
          (int)
              (getDiameter().convertToPixels()
                  * RELATIVE_DIAMETERS[values == null || values.size() == 1 ? 0 : 1]);
      if (controlPoints.length == 2 || controlPoints.length == 3) {
        switch (orientation) {
          case DEFAULT:
            centerX = x;
            centerY = y + pinSpacing / 2;
            break;
          case _90:
            centerX = x - pinSpacing / 2;
            centerY = y;
            break;
          case _180:
            centerX = x;
            centerY = y - pinSpacing / 2;
            break;
          case _270:
            centerX = x + pinSpacing / 2;
            centerY = y;
            break;
          default:
            throw new RuntimeException("Unexpected orientation: " + orientation);
        }
      } else {
        switch (orientation) {
          case DEFAULT:
            centerX = x;
            centerY = y + pinSpacing / 2;
            break;
          case _90:
            centerX = x - pinSpacing / 2;
            centerY = y;
            break;
          case _180:
            centerX = x;
            centerY = y - pinSpacing / 2;
            break;
          case _270:
            centerX = x + pinSpacing / 2;
            centerY = y;
            break;
          default:
            throw new RuntimeException("Unexpected orientation: " + orientation);
        }
      }
      int bodyDiameter = getClosestOdd(getDiameter().convertToPixels());
      int innerDiameter = getClosestOdd(getDiameter().convertToPixels() * 0.85);

      body =
          new Area[] {
            Area.circle(centerX, centerY, bodyDiameter),
            Area.circle(centerX, centerY, innerDiameter)
          };
    }
    return body;
  }

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
    Area[] area = getBody();

    final Composite oldComposite = setTransparency(g2d);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(area[0]);
    drawingObserver.startTracking();
    g2d.setComposite(oldComposite);

    Color finalBorderColor = tryBorderColor(outlineMode, borderColor);
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(area[0]);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : baseColor);
    g2d.fill(area[1]);
    if (outlineMode) {
      g2d.setColor(baseColor.darker());
      g2d.draw(area[1]);
    }

    for (Point point : controlPoints) {
      if (!outlineMode) {
        g2d.setColor(pinColor);
        g2d.fillOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
      }
      g2d.setColor(tryColor(outlineMode, pinColor.darker()));
      g2d.drawOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
    }

    // Draw label.
    Color finalLabelColor = tryLabelColor(outlineMode, getLabelColor());
    g2d.setColor(finalLabelColor);
    Rectangle bounds = area[0].getBounds();
    g2d.setFont(project.getFont());
    StringUtils.drawCenteredText(
        g2d, getLabelForDisplay(), bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);

    // draw polarity markers
    g2d.setColor(pinColor.darker());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f));
    double markerSize = pinSize * 0.7;
    for (int i = 0; i < controlPoints.length; i++) {
      int x = controlPoints[i].x;
      int y = controlPoints[i].y;
      g2d.drawLine((int) (x - markerSize / 2), y, (int) (x + markerSize / 2), y);
      if (i > 0) {
        g2d.drawLine(x, (int) (y - markerSize / 2), x, (int) (y + markerSize / 2));
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = 2 * width / 32;
    Area area =
        new Area(new Ellipse2D.Double(margin, margin, width - 2 * margin, width - 2 * margin));
    area.fillDraw(g2d, BODY_COLOR, BORDER_COLOR);
    margin = 6 * width / 32;
    area =
        new Area(
            new Ellipse2D.Double(margin, margin, width - 2 * margin + 1, width - 2 * margin + 1));
    area.fill(g2d, BASE_COLOR);
    int pinSize = 2 * width / 32;
    for (int i = 0; i < 3; i++) {
      Area.circle(
              (i == 1 ? width * 3 / 8 : width / 2) - pinSize / 2,
              height / 2 + (i - 1) * (height / 5),
              pinSize)
          .fill(g2d, PIN_COLOR);
    }
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Base")
  public Color getBaseColor() {
    return baseColor;
  }

  public void setBaseColor(Color baseColor) {
    this.baseColor = baseColor;
  }

  //  @EditableProperty(name = "Pin spacing")
  //  public Size getPinSpacing() {
  //    return pinSpacing;
  //  }
  //
  //  public void setPinSpacing(Size pinSpacing) {
  //    this.pinSpacing = pinSpacing;
  //    updateControlPoints();
  //    // Reset body shape;
  //    body = null;
  //  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @EditableProperty(name = "Pin Color")
  public Color getPinColor() {
    return pinColor;
  }

  public void setPinColor(Color pinColor) {
    this.pinColor = pinColor;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
