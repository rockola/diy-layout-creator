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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.diylc.awt.StringUtils;
import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.components.Area;
import org.diylc.components.transform.InlinePackageTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "Audio Transformer",
    author = "Branislav Stojkovic",
    category = "Passive",
    instanceNamePrefix = "TR",
    description = "Small signal audio transformer with EI core",
    zOrder = IDIYComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_VALUE,
    transformer = InlinePackageTransformer.class)
public class AudioTransformer extends AbstractMultiPartComponent<String> {

  private static final long serialVersionUID = 1L;

  public static final Color CORE_COLOR = METAL_COLOR;
  public static final Color CORE_BORDER_COLOR = CORE_COLOR.darker();
  public static final Color COIL_COLOR = Color.decode("#DDDDDD");
  public static final Color COIL_BORDER_COLOR = COIL_COLOR.darker();
  public static final Color PIN_COLOR = Color.decode("#00B2EE");
  public static final Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static final Color LABEL_COLOR = Color.white;
  public static final int EDGE_RADIUS = 6;
  public static final Size PIN_SIZE = Size.in(0.03);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;
  private Size leadSpacing = Size.in(0.1);
  private Size windingSpacing = Size.in(0.5);
  private Size coreThickness = Size.in(0.15);
  private Size coreWidth = Size.in(0.6);
  private Size coilWidth = Size.in(0.5);
  private Size coilLength = Size.in(0.6);
  private Point[] controlPoints = new Point[] {new Point(0, 0)};
  private Color coreColor = CORE_COLOR;
  private Color coreBorderColor = CORE_BORDER_COLOR;
  private Color coilColor = COIL_COLOR;
  private Color coilBorderColor = COIL_BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  private boolean primaryCT = true;
  private boolean secondaryCT = true;

  private transient Area[] body;

  public AudioTransformer() {
    super();
    updateControlPoints();
    alpha = 100;
    display = Display.BOTH;
  }

  @EditableProperty
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }

  @EditableProperty(name = "Lead Spacing", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getLeadSpacing() {
    return leadSpacing;
  }

  public void setLeadSpacing(Size leadSpacing) {
    this.leadSpacing = leadSpacing;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty(
      name = "Winding Spacing",
      validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getWindingSpacing() {
    return windingSpacing;
  }

  public void setWindingSpacing(Size rowSpacing) {
    this.windingSpacing = rowSpacing;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty
  public Display getDisplay() {
    if (display == null) {
      display = Display.VALUE;
    }
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  private void updateControlPoints() {
    Point firstPoint = controlPoints[0];
    int pinCount = 4 + (primaryCT ? 1 : 0) + (secondaryCT ? 1 : 0);
    controlPoints = new Point[pinCount];
    controlPoints[0] = firstPoint;
    double leadSpacing = this.leadSpacing.convertToPixels();
    double windingSpacing = this.windingSpacing.convertToPixels();

    // Update control points.
    for (int i = 1; i < 2 + (primaryCT ? 1 : 0); i++) {
      controlPoints[i] =
          new Point(firstPoint.x, (int) (firstPoint.y + i * leadSpacing * (primaryCT ? 1 : 2)));
    }
    for (int i = 0; i < 2 + (secondaryCT ? 1 : 0); i++) {
      controlPoints[2 + (primaryCT ? 1 : 0) + i] =
          new Point(
              (int) (firstPoint.x + windingSpacing),
              (int) (firstPoint.y + i * leadSpacing * (secondaryCT ? 1 : 2)));
    }

    AffineTransform tx = getTx();

    if (tx != null) {
      for (int i = 1; i < controlPoints.length; i++) {
        tx.transform(controlPoints[i], controlPoints[i]);
      }
    }
  }

  @Override
  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      double leadSpacing = this.leadSpacing.convertToPixels();
      double windingSpacing = this.windingSpacing.convertToPixels();
      int centerX = (int) (controlPoints[0].x + windingSpacing / 2);
      int centerY = (int) (controlPoints[0].y + leadSpacing);
      int coreWidth = getClosestOdd(this.coreWidth.convertToPixels());
      int coreThickness = getClosestOdd(this.coreThickness.convertToPixels());
      int coilWidth = getClosestOdd(this.coilWidth.convertToPixels());
      int coilLength = getClosestOdd(this.coilLength.convertToPixels());

      body[0] = Area.centeredRect(centerX, centerY, coreThickness, coreWidth);
      body[1] = Area.centeredRoundRect(centerX, centerY, coilLength, coilWidth, coilWidth / 3);
      body[1].subtract(body[0]);

      AffineTransform tx = getTx();
      if (tx != null) {
        for (Area b : body) {
          if (b != null) {
            b.transform(tx);
          }
        }
      }
    }
    return body;
  }

  private AffineTransform getTx() {
    AffineTransform rotation = null;
    if (orientation != Orientation.DEFAULT) {
      rotation =
          AffineTransform.getRotateInstance(
              orientation.getTheta(), controlPoints[0].x, controlPoints[0].y);
    }
    return rotation;
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
    Area[] body = getBody();

    if (!outlineMode) {
      int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
      for (Point point : controlPoints) {
        Area pin = Area.circle(point, pinSize);
        g2d.setColor(PIN_COLOR);
        g2d.fill(pin);
        g2d.setColor(PIN_BORDER_COLOR);
        g2d.draw(pin);
      }
    }

    Composite oldComposite = setTransparency(g2d);
    // render coil
    Area coilArea = body[1];
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getCoilColor());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.fill(coilArea);
    Color finalBorderColor = tryBorderColor(outlineMode, getCoilBorderColor());
    g2d.setColor(finalBorderColor);
    g2d.draw(coilArea);
    // render core
    Area coreArea = body[0];
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getCoreColor());
    g2d.fill(coreArea);
    g2d.setComposite(oldComposite);

    g2d.setColor(tryBorderColor(outlineMode, getCoreBorderColor()));
    g2d.draw(coreArea);

    drawingObserver.stopTracking();

    g2d.setFont(project.getFont());

    // Draw winding designations
    Point windingPoint =
        new Point(
            (int) (controlPoints[0].x + project.getFontSize()),
            (int) (controlPoints[0].y + leadSpacing.convertToPixels()));
    AffineTransform tx = getTx();
    if (tx != null) {
      tx.transform(windingPoint, windingPoint);
    }
    StringUtils.drawCenteredText(
        g2d,
        "P",
        windingPoint.x,
        windingPoint.y,
        HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
    windingPoint =
        new Point(
            (int) (controlPoints[0].x + windingSpacing.convertToPixels() - project.getFontSize()),
            (int) (controlPoints[0].y + leadSpacing.convertToPixels()));
    if (tx != null) {
      tx.transform(windingPoint, windingPoint);
    }
    StringUtils.drawCenteredText(
        g2d,
        "S",
        windingPoint.x,
        windingPoint.y,
        HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);

    // Draw label.
    Color finalLabelColor = tryLabelColor(outlineMode, getLabelColor());
    g2d.setColor(finalLabelColor);
    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
    List<String> labels = getLabelListForDisplay();

    if (!labels.isEmpty()) {
      boolean multiple = labels.size() > 1;
      Rectangle bounds = coreArea.getBounds();
      int i = 0;
      final AffineTransform oldTransform = g2d.getTransform();
      for (String label : labels) {
        Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);
        int textHeight = (int) (rect.getHeight());
        int textWidth = (int) (rect.getWidth());
        // Center text horizontally and vertically
        int x = bounds.x + (bounds.width - textWidth) / 2;
        int y = bounds.y + (bounds.height - textHeight) / 2 + fontMetrics.getAscent();

        if (getOrientation() == Orientation.DEFAULT || getOrientation() == Orientation._180) {
          int centerX = bounds.x + bounds.width / 2;
          int centerY = bounds.y + bounds.height / 2;
          g2d.rotate(-HALF_PI, centerX, centerY);
        }
        if (multiple) {
          // TODO: this does not handle more than 2 label parts
          if (i == 0) {
            g2d.translate(0, -textHeight / 2);
          } else if (i == 1) {
            g2d.translate(0, textHeight / 2);
          }
          i++;
        }
        g2d.drawString(label, x, y);
        g2d.setTransform(oldTransform);
      }
    }

    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int radius = (int) (12f * width / 32);
    int x1 = 1;
    int y1 = (int) (height / 8f);
    int x2 = width - 2;
    int y2 = (int) (height * 6 / 8f);

    g2d.setColor(COIL_COLOR);
    g2d.fillRoundRect(x1, y1, x2, y2, radius, radius);
    g2d.setColor(COIL_BORDER_COLOR);
    g2d.drawRoundRect(x1, y1, x2, y2, radius, radius);

    x1 = width * 3 / 8;
    y1 = 1;
    x2 = width / 4;
    y2 = height - 2;
    g2d.setColor(CORE_COLOR);
    g2d.fillRect(x1, y1, x2, y2);
    g2d.setColor(CORE_BORDER_COLOR);
    g2d.drawRect(x1, y1, x2, y2);

    int pinSize = (int) (2f * width / 32);
    x1 = width / 5 - pinSize + 1;
    x2 = 4 * width / 5 + 1;
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < 3; i++) {
      y1 = (height / 6) * (i + 2);
      g2d.fillOval(x1, y1, pinSize, pinSize);
      g2d.fillOval(x2, y1, pinSize, pinSize);
    }
  }

  @EditableProperty(name = "Core")
  public Color getCoreColor() {
    if (coreColor == null) {
      coreColor = CORE_COLOR;
    }
    return coreColor;
  }

  public void setCoreColor(Color coreColor) {
    this.coreColor = coreColor;
  }

  @EditableProperty(name = "Core Border")
  public Color getCoreBorderColor() {
    if (coreBorderColor == null) {
      coreBorderColor = CORE_BORDER_COLOR;
    }
    return coreBorderColor;
  }

  public void setCoreBorderColor(Color coreBorderColor) {
    this.coreBorderColor = coreBorderColor;
  }

  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    if (labelColor == null) {
      labelColor = LABEL_COLOR;
    }
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @EditableProperty(name = "Core Thickness")
  public Size getCoreThickness() {
    return coreThickness;
  }

  public void setCoreThickness(Size coreThickness) {
    this.coreThickness = coreThickness;
    body = null;
  }

  @EditableProperty(name = "Core Width")
  public Size getCoreWidth() {
    return coreWidth;
  }

  public void setCoreWidth(Size coreWidth) {
    this.coreWidth = coreWidth;
    body = null;
  }

  @EditableProperty(name = "Coil Width")
  public Size getCoilWidth() {
    return coilWidth;
  }

  public void setCoilWidth(Size coilWidth) {
    this.coilWidth = coilWidth;
    body = null;
  }

  @EditableProperty(name = "Coil Length")
  public Size getCoilLength() {
    return coilLength;
  }

  public void setCoilLength(Size coilLength) {
    this.coilLength = coilLength;
    body = null;
  }

  @EditableProperty(name = "Coil")
  public Color getCoilColor() {
    return coilColor;
  }

  public void setCoilColor(Color coilColor) {
    this.coilColor = coilColor;
  }

  @EditableProperty(name = "Coil Border")
  public Color getCoilBorderColor() {
    return coilBorderColor;
  }

  public void setCoilBorderColor(Color coilBorderColor) {
    this.coilBorderColor = coilBorderColor;
  }

  @EditableProperty(name = "Primary CT")
  public boolean getPrimaryCT() {
    return primaryCT;
  }

  public void setPrimaryCT(boolean primaryCT) {
    this.primaryCT = primaryCT;
    updateControlPoints();
  }

  @EditableProperty(name = "Secondary CT")
  public boolean getSecondaryCT() {
    return secondaryCT;
  }

  public void setSecondaryCT(boolean secondaryCT) {
    this.secondaryCT = secondaryCT;
    updateControlPoints();
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
