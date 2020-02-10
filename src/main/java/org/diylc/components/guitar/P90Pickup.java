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

package org.diylc.components.guitar;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.OrientationHV;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "P-90 Single Coil Pickup",
    category = "Guitar",
    author = "Branislav Stojkovic",
    description = "Single coil P-90 guitar pickup, both \"dog ear\" and \"soap bar\"",
    zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = AbstractGuitarPickup.INSTANCE_NAME_PREFIX,
    autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Guitar Wiring Diagram")
public class P90Pickup extends AbstractSingleOrHumbuckerPickup {

  private static final long serialVersionUID = 1L;
  private static Color BODY_COLOR = Color.decode("#D8C989");
  //  private static Color POINT_COLOR = Color.darkGray;

  // dog ear
  private static Size DOG_EAR_WIDTH = Size.mm(41);
  private static Size DOG_EAR_LENGTH = Size.mm(86.9);
  private static Size TOTAL_LENGTH = Size.mm(118.7);
  private static Size DOG_EAR_EDGE_RADIUS = Size.mm(4);
  // soap bar
  private static Size SOAP_BAR_WIDTH = Size.mm(35.3);
  private static Size SOAP_BAR_LENGTH = Size.mm(85.6);
  private static Size SOAP_BAR_EDGE_RADIUS = Size.mm(8);
  //
  private static Size LIP_RADIUS = Size.mm(10);
  private static Size POINT_MARGIN = Size.mm(3.5);
  private static Size POINT_SIZE = Size.mm(2);
  private static Size LIP_HOLE_SIZE = Size.mm(2.5);
  private static Size LIP_HOLE_SPACING = Size.mm(97);
  private static Size POLE_SIZE = Size.mm(4);
  private static Size POLE_SPACING = Size.mm(11.68);

  private Color color = BODY_COLOR;
  private P90Type type = P90Type.DOG_EAR;
  private Color poleColor = METAL_COLOR;

  @Override
  protected OrientationHV getControlPointDirection() {
    return OrientationHV.VERTICAL;
  }

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    final Composite oldComposite = setTransparency(g2d);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : color);
    g2d.fill(body[0]);
    if (body[1] != null) {
      g2d.fill(body[1]);
    }
    g2d.setComposite(oldComposite);

    Color finalBorderColor = tryBorderColor(outlineMode, color.darker());
    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);
    if (body[1] != null) {
      g2d.draw(body[1]);
    }

    if (!outlineMode) {
      g2d.setColor(getPoleColor());
      g2d.fill(body[3]);
      g2d.setColor(darkerOrLighter(getPoleColor()));
      g2d.draw(body[3]);
    }

    drawMainLabel(g2d, project, outlineMode, componentState);
    drawTerminalLabels(g2d, finalBorderColor, project);
  }

  @Override
  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[4];

      Point[] points = getControlPoints();
      int x = points[0].x;
      int y = points[0].y;
      int width = (int) getType().getWidth().convertToPixels();
      int length = (int) getType().getLength().convertToPixels();
      int edgeRadius = (int) getType().getEdgeRadius().convertToPixels();
      int pointMargin = (int) POINT_MARGIN.convertToPixels();
      int totalLength = (int) TOTAL_LENGTH.convertToPixels();
      int lipRadius = (int) LIP_RADIUS.convertToPixels();
      int pointSize = getClosestOdd(POINT_SIZE.convertToPixels());
      int lipHoleSize = getClosestOdd(LIP_HOLE_SIZE.convertToPixels());
      int lipHoleSpacing = getClosestOdd(LIP_HOLE_SPACING.convertToPixels());

      body[0] = new Area(new RoundRectangle2D.Double(
          x - length,
          y - pointMargin,
          length,
          width,
          edgeRadius,
          edgeRadius));

      if (getType() == P90Type.DOG_EAR) {
        double rectWidth = (totalLength - length) / SQRT_TWO;
        RoundRectangle2D roundRect = new RoundRectangle2D.Double(
            -rectWidth / 2, -rectWidth / 2, rectWidth, rectWidth, lipRadius, lipRadius);
        Area leftEar = new Area(roundRect);
        leftEar.transform(AffineTransform.getRotateInstance(Math.PI / 4));
        leftEar.transform(AffineTransform.getScaleInstance(1.1, 1.45));
        leftEar.transform(
            AffineTransform.getTranslateInstance(
                x /*+ pointMargin*/ - length, y - pointMargin + width / 2));
        leftEar.subtract((Area) body[0]);
        Area rightEar = new Area(roundRect);
        rightEar.transform(AffineTransform.getRotateInstance(Math.PI / 4));
        rightEar.transform(AffineTransform.getScaleInstance(1.1, 1.45));
        rightEar.transform(
            AffineTransform.getTranslateInstance(x, y - pointMargin + width / 2));
        rightEar.subtract((Area) body[0]);
        Area lipArea = leftEar;
        lipArea.add(rightEar);
        lipArea.subtract(new Area(new Ellipse2D.Double(
            x - length / 2 - lipHoleSpacing / 2 - lipHoleSize / 2,
            y - pointMargin + width / 2 - lipHoleSize / 2,
            lipHoleSize,
            lipHoleSize)));
        lipArea.subtract(new Area(new Ellipse2D.Double(
            x - length / 2 + lipHoleSpacing / 2 - lipHoleSize / 2,
            y - pointMargin + width / 2 - lipHoleSize / 2,
            lipHoleSize,
            lipHoleSize)));

        body[1] = lipArea;
      }

      body[2] = new Area(new Ellipse2D.Double(
          x - pointSize / 2, y - pointSize / 2, pointSize, pointSize));

      int poleSize = (int) POLE_SIZE.convertToPixels();
      int poleSpacing = (int) POLE_SPACING.convertToPixels();
      int poleMargin = (length - poleSpacing * 5) / 2;
      Area poleArea = new Area();
      for (int i = 0; i < 6; i++) {
        Ellipse2D pole = new Ellipse2D.Double(
            x - length + poleMargin + i * poleSpacing - poleSize / 2,
            y - pointMargin - poleSize / 2 + width / 2,
            poleSize,
            poleSize);
        poleArea.add(new Area(pole));
      }
      body[3] = poleArea;

      // Rotate if needed
      if (orientation != Orientation.DEFAULT) {
        double theta = orientation.getTheta();
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
        for (Shape shape : body) {
          Area area = (Area) shape;
          if (shape != null) {
            area.transform(rotation);
          }
        }
      }
    }
    return body;
  }

  @Override
  protected int getMainLabelYOffset() {
    return (int) (getType().getWidth().convertToPixels() / 2 - 20);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    final int x = width / 2;
    final int y = height / 2;
    g2d.rotate(Math.PI / 4, x, y);

    int baseWidth = 13 * width / 32;
    int baseLength = 27 * width / 32;
    final int radius = 6 * width / 32;

    g2d.setColor(BODY_COLOR);
    Polygon base = new Polygon(
        new int[] {
          x,
          x + baseWidth / 2,
          x + baseWidth / 2,
          x,
          x - baseWidth / 2,
          x - baseWidth / 2
        },
        new int[] {
          -2,
          y - baseLength / 2,
          y + baseLength / 2,
          height + 1,
          y + baseLength / 2,
          y - baseLength / 2
        },
        6);
    Area baseArea = new Area(base);
    baseArea.intersect(new Area(new Rectangle(0, -1, width, height + 1)));
    g2d.fill(baseArea);
    g2d.setColor(BODY_COLOR.darker());
    g2d.draw(baseArea);

    Area p90body = Area.centeredRoundRect(x, y, baseWidth, baseLength, radius);
    g2d.setColor(BODY_COLOR);
    g2d.fill(p90body);
    g2d.setColor(BODY_COLOR.darker());
    g2d.draw(p90body);

    g2d.setColor(METAL_COLOR);
    int poleSize = 2;
    int poleSpacing = baseLength / (DEFAULT_NUMBER_OF_STRINGS + 1);
    int poleY = y - poleSpacing * (DEFAULT_NUMBER_OF_STRINGS - 1) / 2;
    for (int i = 0; i < DEFAULT_NUMBER_OF_STRINGS; i++) {
      g2d.fill(Area.circle(x, poleY + i * poleSpacing, poleSize));
    }
  }

  @EditableProperty(name = "Type")
  public P90Type getType() {
    return type;
  }

  public void setType(P90Type type) {
    this.type = type;
    // Invalidate the body
    body = null;
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @EditableProperty(name = "Pole Color")
  public Color getPoleColor() {
    if (poleColor == null) {
      poleColor = METAL_COLOR;
    }
    return poleColor;
  }

  public void setPoleColor(Color poleColor) {
    this.poleColor = poleColor;
  }

  public enum P90Type {
    DOG_EAR("Dog Ear", DOG_EAR_LENGTH, DOG_EAR_WIDTH, DOG_EAR_EDGE_RADIUS),
    SOAP_BAR("Soap Bar", SOAP_BAR_LENGTH, SOAP_BAR_WIDTH, SOAP_BAR_EDGE_RADIUS);

    private String label;
    private Size length;
    private Size width;
    private Size edgeRadius;

    P90Type(String label, Size length, Size width, Size edgeRadius) {
      this.label = label;
      this.length = length;
      this.width = width;
      this.edgeRadius = edgeRadius;
    }

    public Size getLength() {
      return length;
    }

    public Size getWidth() {
      return width;
    }

    public Size getEdgeRadius() {
      return edgeRadius;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
