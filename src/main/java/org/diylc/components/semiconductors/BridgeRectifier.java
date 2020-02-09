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

package org.diylc.components.semiconductors;

import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractLeadedComponent.LabelOrientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.Area;
import org.diylc.components.RoundedPolygon;
import org.diylc.components.transform.InlinePackageTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Current;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.Voltage;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "Bridge Rectifier",
    author = "Branislav Stojkovic",
    category = "Semiconductors",
    instanceNamePrefix = "BR",
    description = "Few variations of bridge rectifier chips",
    zOrder = IDIYComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_VALUE,
    transformer = InlinePackageTransformer.class)
public class BridgeRectifier extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;
  private static final Size MINI_LENGTH = Size.mm(6.7);
  private static final Size MINI_WIDTH = Size.mm(8.3);
  private static final Size MINI_HORIZONTAL_SPACING = Size.in(0.3);
  private static final Size MINI_VERTICAL_SPACING = Size.in(0.2);
  private static final int[] MINI1_LABEL_SPACING_X = new int[] {1, -1, -1, 1};
  private static final int[] MINI2_LABEL_SPACING_X = new int[] {1, -1, 1, -1};
  private static final Size MINI_ROUND_DIAMETER = Size.mm(9.1);
  private static final Size MINI_ROUND_SPACING = Size.in(0.2);
  private static final int[] MINI_ROUND_LABEL_SPACING_Y = new int[] {1, 1, -1, -1};
  private static final Size INLINE_LENGTH = Size.mm(23.2);
  private static final Size INLINE_WIDTH = Size.mm(2.7);
  private static final Size INLINE_SPACING = Size.in(0.2);
  private static final int[] INLINE_LABEL_SPACING_Y = new int[] {-1, -1, 1, 1};
  private static final Size BR3_LENGTH = Size.in(0.6);
  private static final Size BR3_SPACING = Size.in(0.425);
  private static final Size BR3_HOLE_SIZE = Size.mm(3);
  private static final int[] BR3_LABEL_SPACING_Y = new int[] {1, 1, -1, -1};

  public static final Color BODY_COLOR = Color.gray;
  public static final Color BORDER_COLOR = Color.gray.darker();
  public static final Color PIN_COLOR = Color.decode("#00B2EE");
  public static final Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static final Color LABEL_COLOR = Color.white;
  public static final int EDGE_RADIUS = 6;
  public static final Size SQUARE_PIN_SIZE = Size.in(0.04);
  public static final Size ROUND_PIN_SIZE = Size.in(0.032);
  public static final Size INDENT_SIZE = Size.in(0.07);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;
  private Point[] controlPoints =
      new Point[] {new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0)};
  private String[] pointLabels = new String[] {"+", "~", "~", "-"};
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  private RectifierType rectifierType = RectifierType.MiniDIP1;
  private Voltage voltage;
  private Current current;
  private transient Area[] body;
  private LabelOrientation labelOrientation = LabelOrientation.Directional;

  public BridgeRectifier() {
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

    int spacingH;
    int spacingV;

    switch (rectifierType) {
      case MiniDIP1:
        spacingH = (int) MINI_HORIZONTAL_SPACING.convertToPixels();
        spacingV = (int) MINI_VERTICAL_SPACING.convertToPixels();
        controlPoints[1].setLocation(firstPoint.x + spacingH, firstPoint.y);
        controlPoints[2].setLocation(firstPoint.x + spacingH, firstPoint.y + spacingV);
        controlPoints[3].setLocation(firstPoint.x, firstPoint.y + spacingV);
        break;
      case MiniDIP2:
        spacingH = (int) MINI_HORIZONTAL_SPACING.convertToPixels();
        spacingV = (int) MINI_VERTICAL_SPACING.convertToPixels();
        controlPoints[1].setLocation(firstPoint.x + spacingH, firstPoint.y);
        controlPoints[2].setLocation(firstPoint.x, firstPoint.y + spacingV);
        controlPoints[3].setLocation(firstPoint.x + spacingH, firstPoint.y + spacingV);
        break;
      case MiniRound1:
        spacingH = spacingV = (int) MINI_ROUND_SPACING.convertToPixels();
        controlPoints[1].setLocation(firstPoint.x + spacingH, firstPoint.y);
        controlPoints[2].setLocation(firstPoint.x + spacingH, firstPoint.y + spacingV);
        controlPoints[3].setLocation(firstPoint.x, firstPoint.y + spacingV);
        break;
      case MiniRound2:
        spacingH = spacingV = (int) MINI_ROUND_SPACING.convertToPixels();
        controlPoints[1].setLocation(firstPoint.x + spacingH, firstPoint.y);
        controlPoints[2].setLocation(firstPoint.x, firstPoint.y + spacingV);
        controlPoints[3].setLocation(firstPoint.x + spacingH, firstPoint.y + spacingV);
        break;
      case InLine:
        spacingV = (int) INLINE_SPACING.convertToPixels();
        controlPoints[1].setLocation(firstPoint.x, firstPoint.y + spacingV);
        controlPoints[2].setLocation(firstPoint.x, firstPoint.y + 2 * spacingV);
        controlPoints[3].setLocation(firstPoint.x, firstPoint.y + 3 * spacingV);
        break;
      case SquareBR3:
        spacingH = spacingV = (int) BR3_SPACING.convertToPixels();
        controlPoints[1].setLocation(firstPoint.x + spacingH, firstPoint.y);
        controlPoints[2].setLocation(firstPoint.x, firstPoint.y + spacingV);
        controlPoints[3].setLocation(firstPoint.x + spacingH, firstPoint.y + spacingV);
        break;
      default:
        throw new RuntimeException("unknown rectifier type " + rectifierType);
    }

    if (orientation != Orientation.DEFAULT) {
      double theta = orientation.getTheta();
      AffineTransform rotation =
          AffineTransform.getRotateInstance(theta, firstPoint.x, firstPoint.y);
      for (int i = 1; i < controlPoints.length; i++) {
        rotation.transform(controlPoints[i], controlPoints[i]);
      }
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];

      int centerX =
          (controlPoints[0].x + controlPoints[1].x + controlPoints[2].x + controlPoints[3].x) / 4;
      int centerY =
          (controlPoints[0].y + controlPoints[1].y + controlPoints[2].y + controlPoints[3].y) / 4;

      int width = 0;
      int length = 0;

      switch (rectifierType) {
        case MiniDIP1:
        case MiniDIP2:
          length = (int) MINI_LENGTH.convertToPixels();
          width = (int) MINI_WIDTH.convertToPixels();
          break;
        case MiniRound1:
        case MiniRound2:
          length = width = (int) MINI_ROUND_DIAMETER.convertToPixels();
          break;
        case InLine:
          length = (int) INLINE_WIDTH.convertToPixels();
          width = (int) INLINE_LENGTH.convertToPixels();
          break;
        case SquareBR3:
          length = width = (int) BR3_LENGTH.convertToPixels();
          break;
        default:
          throw new RuntimeException("unknown rectifier type " + rectifierType);
      }

      if (orientation == Orientation._90 || orientation == Orientation._270) {
        int p = length;
        length = width;
        width = p;
      }

      switch (rectifierType) {
        case MiniDIP1:
        case MiniDIP2:
        case InLine:
          body[0] = new Area(new RoundRectangle2D.Double(
              centerX - length / 2,
              centerY - width / 2,
              length,
              width,
              EDGE_RADIUS,
              EDGE_RADIUS));
          break;
        case MiniRound1:
        case MiniRound2:
          body[0] = new Area(new Ellipse2D.Double(
              centerX - length / 2, centerY - width / 2, length, width));
          break;
        case SquareBR3:
          double margin = (BR3_LENGTH.convertToPixels() - BR3_SPACING.convertToPixels()) / 2;
          double holeSize = BR3_HOLE_SIZE.convertToPixels();
          //
          //          Path2D path = new Path2D.Double();
          //          path.moveTo(centerX - width / 2 + margin, centerY - width / 2);
          //          path.lineTo(centerX + width / 2, centerY - width / 2);
          //          path.lineTo(centerX + width / 2, centerY + width / 2);
          //          path.lineTo(centerX - width / 2, centerY + width / 2);
          //          path.lineTo(centerX - width / 2, centerY - width / 2 + margin);
          //          path.closePath();

          RoundedPolygon poly = new RoundedPolygon(
              new Point[] {
                new Point(centerX, centerY - width / 2),
                new Point(centerX + width / 2, centerY - width / 2),
                new Point(centerX + width / 2, centerY + width / 2),
                new Point(centerX - width / 2, centerY + width / 2),
                new Point(centerX - width / 2, (int) (centerY - width / 2 + margin)),
                new Point((int) (centerX - width / 2 + margin), centerY - width / 2),
              },
              new double[] {
                EDGE_RADIUS, EDGE_RADIUS, EDGE_RADIUS, EDGE_RADIUS / 2, EDGE_RADIUS / 2
              });

          body[0] = new Area(poly);
          body[0].subtract(new Area(new Ellipse2D.Double(
              centerX - holeSize / 2, centerY - holeSize / 2, holeSize, holeSize)));

          if (orientation != Orientation.DEFAULT) {
            double theta = orientation.getTheta();
            AffineTransform rotation = AffineTransform.getRotateInstance(theta, centerX, centerY);
            body[0].transform(rotation);
          }

          break;
      }
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

    PinShape pinShape = rectifierType.getPinShape();
    int pinSize =
        (int) (pinShape == PinShape.Round
               ? ROUND_PIN_SIZE.convertToPixels()
               : SQUARE_PIN_SIZE.convertToPixels())
        / 2
        * 2;

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    if (!outlineMode) {
      for (Point point : controlPoints) {
        Area pin = (pinShape == PinShape.Round
                    ? Area.circle(point, pinSize)
                    : Area.centeredSquare(point, pinSize));
        pin.fillDraw(g2d, PIN_COLOR, PIN_BORDER_COLOR);
      }
    }

    Area mainArea = getBody()[0];
    Composite oldComposite = setTransparency(g2d);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
    g2d.fill(mainArea);
    g2d.setComposite(oldComposite);

    Color finalBorderColor = tryBorderColor(outlineMode, getBorderColor());
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (outlineMode) {
      Area area = new Area(mainArea);
      area.subtract(getBody()[1]);
      g2d.draw(area);
    } else {
      g2d.draw(mainArea);
    }

    drawingObserver.stopTracking();

    // Draw label.
    g2d.setFont(project.getFont());
    Color finalLabelColor = tryLabelColor(outlineMode, getLabelColor());
    g2d.setColor(finalLabelColor);
    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
    List<String> label = getLabelListForDisplay();
    if (!label.isEmpty()) {
      int i = 0;
      for (String l : label) {
        Rectangle2D rect = fontMetrics.getStringBounds(l, g2d);
        int textHeight = (int) (rect.getHeight());
        int textWidth = (int) (rect.getWidth());
        // Center text horizontally and vertically
        Rectangle bounds = mainArea.getBounds();
        int x = bounds.x + (bounds.width - textWidth) / 2;
        int y = bounds.y + (bounds.height - textHeight) / 2 + fontMetrics.getAscent();

        final AffineTransform oldTransform = g2d.getTransform();

        if (getLabelOrientation() == LabelOrientation.Directional
            && (getOrientation() == Orientation.DEFAULT || getOrientation() == Orientation._180)) {
          int centerX = bounds.x + bounds.width / 2;
          int centerY = bounds.y + bounds.height / 2;
          g2d.rotate(-HALF_PI, centerX, centerY);
        }

        if (label.length == 2) {
          if (i == 0) {
            g2d.translate(0, -textHeight / 2);
          } else if (i == 1) {
            g2d.translate(0, textHeight / 2);
          }
        }

        if (rectifierType == RectifierType.SquareBR3) {
          y -= BR3_HOLE_SIZE.convertToPixels();
        }
        g2d.drawString(l, x, y);
        g2d.setTransform(oldTransform);
        i++;
      }
    }

    // draw pin numbers
    g2d.setFont(project.getFont().deriveFont((float) (project.getFont().getSize2D() * 0.8)));
    g2d.setColor(finalLabelColor);
    for (int i = 0; i < controlPoints.length; i++) {
      Point point = controlPoints[i];

      int dx = 0;
      int dy = 0;

      // determine label offset
      switch (rectifierType) {
        case MiniDIP1:
          dx = pinSize * MINI1_LABEL_SPACING_X[i];
          break;
        case MiniDIP2:
          dx = pinSize * MINI2_LABEL_SPACING_X[i];
          break;
        case MiniRound1:
        case MiniRound2:
          dy = pinSize * MINI_ROUND_LABEL_SPACING_Y[i];
          break;
        case InLine:
          dy = pinSize * INLINE_LABEL_SPACING_Y[i];
          break;
        case SquareBR3:
          dy = pinSize * BR3_LABEL_SPACING_Y[i];
          break;
        default:
          throw new RuntimeException("unknown rectifier type " + rectifierType);
      }

      dx = (int) (1.5 * dx);
      dy = (int) (1.5 * dy);

      // now apply the correct rotation
      switch (rectifierType) {
        case MiniDIP1:
        case MiniDIP2:
          switch (orientation) {
            case _90:
              int p1 = dx;
              dx = dy;
              dy = p1;
              break;
            case _180:
              dx = -dx;
              dy = -dy;
              break;
            case _270:
              int p2 = dx;
              dx = -dy;
              dy = -p2;
              break;
          }
          break;
        case MiniRound1:
        case MiniRound2:
        case SquareBR3:
          switch (orientation) {
            case _90:
              int p1 = dx;
              dx = -dy;
              dy = -p1;
              break;
            case _180:
              dx = -dx;
              dy = -dy;
              break;
            case _270:
              int p2 = dx;
              dx = dy;
              dy = p2;
              break;
          }
          break;
        case InLine:
          switch (orientation) {
            case _90:
              int p1 = dx;
              dx = -dy;
              dy = -p1;
              break;
            case _180:
              dx = -dx;
              dy = -dy;
              break;
            case _270:
              int p2 = dx;
              dx = dy;
              dy = p2;
              break;
          }
          break;
      }

      StringUtils.drawCenteredText(
          g2d,
          pointLabels[i],
          point.x + dx,
          point.y + dy,
          HorizontalAlignment.CENTER,
          VerticalAlignment.CENTER);
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = (int) (2f * width / 32);
    Area area = new Area(new Ellipse2D.Double(
        margin / 2, margin, width - 2 * margin, width - 2 * margin));
    area.fillDraw(g2d, BODY_COLOR, BORDER_COLOR);
    g2d.setColor(PIN_COLOR);
    int pinSize = 2 * width / 32;
    g2d.fillOval(width * 2 / 8, height * 2 / 8, pinSize, pinSize);
    g2d.fillOval(width * 6 / 8 - pinSize, height * 2 / 8, pinSize, pinSize);
    g2d.fillOval(width * 6 / 8 - pinSize, height * 6 / 8 - pinSize, pinSize, pinSize);
    g2d.fillOval(width * 2 / 8, height * 6 / 8 - pinSize, pinSize, pinSize);
    g2d.setColor(LABEL_COLOR);
    g2d.setFont(LABEL_FONT.deriveFont(8f * width / 32));
    final int x1 = width * 2 / 8 + 1;
    final int y2 = height * 6 / 8 - 5 * width / 32;
    final int x2 = width * 6 / 8 - 2 * width / 32;
    StringUtils.drawCenteredText(
        g2d,
        "+",
        x1,
        height * 2 / 8 + 4 * width / 32,
        HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(
        g2d,
        "-",
        x1,
        y2,
        HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(
        g2d,
        "~",
        x2,
        height * 2 / 8 + 5 * width / 32,
        HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(
        g2d,
        "~",
        x2,
        y2,
        HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    if (bodyColor == null) {
      bodyColor = BODY_COLOR;
    }
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    if (borderColor == null) {
      borderColor = BORDER_COLOR;
    }
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
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

  @EditableProperty(name = "Type")
  public RectifierType getRectifierType() {
    return rectifierType;
  }

  public void setRectifierType(RectifierType rectifierType) {
    this.rectifierType = rectifierType;
    updateControlPoints();
    body = null;
  }

  @EditableProperty
  public Current getCurrent() {
    return current;
  }

  public void setCurrent(Current current) {
    this.current = current;
  }

  @EditableProperty
  public Voltage getVoltage() {
    return voltage;
  }

  public void setVoltage(Voltage voltage) {
    this.voltage = voltage;
  }

  @EditableProperty(name = "Label Orientation")
  public LabelOrientation getLabelOrientation() {
    if (labelOrientation == null) {
      labelOrientation = LabelOrientation.Directional;
    }
    return labelOrientation;
  }

  public void setLabelOrientation(LabelOrientation labelOrientation) {
    this.labelOrientation = labelOrientation;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  public enum RectifierType {
    MiniDIP1("DFM A", PinShape.Square),
    MiniDIP2("DFM B", PinShape.Square),
    MiniRound1("Round WOG A", PinShape.Round),
    MiniRound2("Round WOG B", PinShape.Round),
    InLine("In-Line D-44", PinShape.Round),
    SquareBR3("Square BR-3", PinShape.Round);

    private String label;
    private PinShape pinShape;

    RectifierType(String label, PinShape pinShape) {
      this.label = label;
      this.pinShape = pinShape;
    }

    @Override
    public String toString() {
      return label;
    }

    public PinShape getPinShape() {
      return pinShape;
    }
  }

  private enum PinShape {
    Square,
    Round
  }
}
