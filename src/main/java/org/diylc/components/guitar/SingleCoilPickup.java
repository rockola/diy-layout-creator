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

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.OrientationHV;
import org.diylc.components.Area;
import org.diylc.components.RoundedPath;
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
    name = "Single Coil Pickup",
    category = "Guitar",
    author = "Branislav Stojkovic",
    description = "Single coil guitar pickup, both Strat and Tele style",
    zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = AbstractGuitarPickup.INSTANCE_NAME_PREFIX,
    autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Guitar Wiring Diagram")
public class SingleCoilPickup extends AbstractSingleOrHumbuckerPickup {

  private static final long serialVersionUID = 1L;
  private static final Color BODY_COLOR = Color.white;
  private static final Color BASE_COLOR = Color.gray;
  public static final Color LUG_COLOR = Color.decode("#E0C04C");
  //  private static final Color POINT_COLOR = Color.lightGray;
  private static final Size WIDTH = Size.mm(15.5);
  private static final Size LENGTH = Size.mm(83);
  private static final Size BASE_RADIUS = Size.in(0.15);
  private static final Size LUG_DIAMETER = Size.in(0.06);
  // strat-specific
  private static final Size STRAT_LIP_WIDTH = Size.mm(5);
  private static final Size STRAT_LIP_LENGTH = Size.mm(20);
  private static final Size STRAT_INNER_LENGTH = Size.mm(70);
  // tele-specific
  private static final Size TELE_BASE_WIDTH = Size.in(1.5);
  private static final Size TELE_LIP_LENGTH = Size.in(1.735);
  private static final Size TELE_LENGTH = Size.in(2.87);
  private static final Size TELE_HOLE_SPACING = Size.in(1.135);
  //
  private static final Size HOLE_SIZE = Size.mm(2);
  private static final Size HOLE_MARGIN = Size.mm(4);
  private static final Size POLE_SIZE = Size.mm(4);
  private static final Size POLE_SPACING = Size.mm(11.68);
  //
  private static final Size RAIL_WIDTH = Size.mm(1.5);
  private static final Size RAIL_LENGTH = Size.mm(60);
  private static final Size COIL_SPACING = Size.mm(7.5);

  private Color color = BODY_COLOR;
  private Color poleColor = METAL_COLOR;
  private Color baseColor = BASE_COLOR;
  private SingleCoilType type = SingleCoilType.Stratocaster;
  private PolePieceType polePieceType = PolePieceType.RODS;
  private Color lugColor = LUG_COLOR;

  @Override
  protected OrientationHV getControlPointDirection() {
    return OrientationHV.HORIZONTAL;
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

    Composite oldComposite = setTransparency(g2d);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBaseColor());
    g2d.fill(body[4]);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getColor());
    g2d.fill(body[3] == null ? body[0] : body[3]);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getLugColor());
    g2d.fill(body[1]);
    g2d.setColor(tryColor(outlineMode, darkerOrLighter(LUG_COLOR)));
    g2d.draw(body[1]);
    g2d.setComposite(oldComposite);

    Color finalBorderColor = tryBorderColor(outlineMode, darkerOrLighter(getBaseColor()));
    g2d.setColor(finalBorderColor);
    g2d.draw(body[4]);

    finalBorderColor = tryBorderColor(outlineMode, darkerOrLighter(color));
    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);
    if (body[3] != null) {
      g2d.draw(body[3]);
    }

    if (!outlineMode) {
      g2d.setColor(getPoleColor());
      g2d.fill(body[2]);
      g2d.setColor(darkerOrLighter(getPoleColor()));
      g2d.draw(body[2]);
    }

    drawMainLabel(g2d, project, outlineMode, componentState);
    drawTerminalLabels(g2d, finalBorderColor, project);
  }

  @Override
  protected int getMainLabelYOffset() {
    return (getPolePieceType().isHumbucker() || getPolePieceType() == PolePieceType.NONE)
        ? 0
        : (int) (WIDTH.convertToPixels() / 2 - 20);
  }

  @Override
  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[5];

      Point[] points = getControlPoints();
      int x = (points[0].x + points[3].x) / 2;
      int y = (points[0].y + points[3].y) / 2;
      int width = (int) WIDTH.convertToPixels();
      int length = (int) LENGTH.convertToPixels();
      int stratInnerLength = (int) STRAT_INNER_LENGTH.convertToPixels();
      int teleLength = (int) TELE_LENGTH.convertToPixels();
      int teleBaseWidth = (int) TELE_BASE_WIDTH.convertToPixels();
      int teleLipLength = getClosestOdd(TELE_LIP_LENGTH.convertToPixels());
      int teleHoleSpacing = (int) TELE_HOLE_SPACING.convertToPixels();
      int coilLength = getType() == SingleCoilType.Stratocaster ? stratInnerLength : teleLength;
      int lipWidth = (int) STRAT_LIP_WIDTH.convertToPixels();
      int lipLength = (int) STRAT_LIP_LENGTH.convertToPixels();
      int holeSize = getClosestOdd(HOLE_SIZE.convertToPixels());
      int holeMargin = getClosestOdd(HOLE_MARGIN.convertToPixels());
      int baseRadius = (int) BASE_RADIUS.convertToPixels();
      int coilOffset = 0;

      if (getType() == SingleCoilType.Stratocaster) {
        coilOffset = lipWidth / 2;

        Area mainArea = new Area(new RoundRectangle2D.Double(
            x - length / 2, y - lipWidth / 2 - width, length, width, width, width));
        // Cutout holes
        mainArea.subtract(new Area(new Ellipse2D.Double(
            x - length / 2 + holeMargin - holeSize / 2,
            y - lipWidth / 2 - width / 2 - holeSize / 2,
            holeSize,
            holeSize)));
        mainArea.subtract(new Area(new Ellipse2D.Double(
            x + length / 2 - holeMargin - holeSize / 2,
            y - lipWidth / 2 - width / 2 - holeSize / 2,
            holeSize,
            holeSize)));

        body[3] = mainArea;
        RoundedPath basePath = new RoundedPath(baseRadius);
        basePath.moveTo(x, y + lipWidth / 2);
        basePath.lineTo(x + lipLength / 2, y + lipWidth / 2);
        basePath.lineTo(x + length / 2, y - lipWidth);
        basePath.lineTo(x - length / 2, y - lipWidth);
        basePath.lineTo(x - lipLength / 2, y + lipWidth / 2);
        basePath.lineTo(x, y + lipWidth / 2);

        Area base = new Area(basePath.getPath());
        base.subtract(mainArea);

        body[4] = base;
      } else if (getType() == SingleCoilType.Telecaster) {
        coilOffset = (teleBaseWidth - width) / 4;

        RoundedPath basePath = new RoundedPath(baseRadius);
        basePath.moveTo(x, y + coilOffset);
        basePath.lineTo(x + teleLipLength / 2, y + coilOffset);
        basePath.lineTo(x + coilLength * 0.53, y - coilOffset - width / 2);
        basePath.lineTo(x + coilLength / 2 - width * 0.45, y - coilOffset - width);
        basePath.lineTo(x, y - 3 * coilOffset - width);
        basePath.lineTo(x - coilLength / 2 + width * 0.45, y - coilOffset - width);
        basePath.lineTo(x - coilLength * 0.53, y - coilOffset - width / 2);
        basePath.lineTo(x - teleLipLength / 2, y + coilOffset);
        basePath.lineTo(x, y + coilOffset);

        Area base = new Area(basePath.getPath());
        base.intersect(new Area(new Rectangle2D.Double(
            x - coilLength * 0.48,
            y - teleBaseWidth,
            coilLength * 0.96,
            teleBaseWidth * 2)));

        // Cutout holes
        base.subtract(new Area(new Ellipse2D.Double(
            x - teleLipLength / 2 - holeSize / 2, y, holeSize, holeSize)));
        base.subtract(new Area(new Ellipse2D.Double(
            x + teleLipLength / 2 - holeSize / 2, y, holeSize, holeSize)));
        base.subtract(new Area(new Ellipse2D.Double(
            x - holeSize / 2, y - teleHoleSpacing, holeSize, holeSize)));

        body[4] = base;
      }

      Area poleArea = new Area();
      int poleSize;
      int poleSpacing;
      int poleMargin;
      int coilSpacing;
      int railWidth;
      int railSpacing;
      int railLength;
      switch (getPolePieceType()) {
        case RODS:
          poleSize = (int) POLE_SIZE.convertToPixels();
          poleSpacing = (int) POLE_SPACING.convertToPixels();
          poleMargin = (length - poleSpacing * 5) / 2;
          for (int i = 0; i < getNumberOfStrings(); i++) {
            poleArea.add(Area.circle(
                x - length / 2 + poleMargin + i * poleSpacing,
                y - coilOffset - width / 2 - poleSize / 2,
                poleSize));
          }
          break;
        case ROD_HUMBUCKER:
          poleSize = (int) POLE_SIZE.convertToPixels() / 2;
          poleSpacing = (int) POLE_SPACING.convertToPixels();
          poleMargin = (length - poleSpacing * 5) / 2;
          coilSpacing = (int) COIL_SPACING.convertToPixels();
          for (int i = 0; i < getNumberOfStrings(); i++) {
            int poleX = x - length / 2 + poleMargin + i * poleSpacing;
            int poleY1 = y - coilOffset - width / 2 - coilSpacing / 2;
            int poleY2 = y - coilOffset - width / 2 + coilSpacing / 2;
            poleArea.add(Area.circle(poleX, poleY1, poleSize));
            poleArea.add(Area.circle(poleX, poleY2, poleSize));
          }
          break;
        case RAIL_HUMBUCKER:
          railWidth = (int) RAIL_WIDTH.convertToPixels();
          railSpacing = (int) COIL_SPACING.convertToPixels();
          railLength = (int) RAIL_LENGTH.convertToPixels();
          poleArea.add(Area.centeredRect(
              x,
              y - coilOffset - width / 2 - railSpacing / 2,
              railLength,
              railWidth));
          poleArea.add(Area.centeredRect(
              x,
              y - coilOffset - width / 2 + railSpacing / 2,
              railLength,
              railWidth));
          break;
        case RAIL:
          railWidth = 2 * (int) RAIL_WIDTH.convertToPixels();
          railLength = (int) RAIL_LENGTH.convertToPixels();
          poleArea.add(Area.centeredRoundRect(
              x,
              y - coilOffset - width / 2,
              railLength,
              railWidth,
              railWidth / 2));
          break;
        default:
      }

      body[0] = Area.roundRect(
          x - coilLength / 2,
          y - coilOffset - width,
          coilLength,
          width,
          width);

      body[2] = poleArea;

      if (body[3] == null) {
        ((Area) body[4]).subtract((Area) body[0]);
      }

      // Rotate if needed
      if (orientation != Orientation.DEFAULT) {
        double theta = orientation.getTheta();
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
        for (Shape shape : body) {
          Area area = (Area) shape;
          if (area != null) {
            area.transform(rotation);
          }
        }
      }

      Area newArea = new Area();
      int lugDiameter = getClosestOdd(LUG_DIAMETER.convertToPixels());
      double lugHole = getClosestOdd(lugDiameter * 0.4);
      for (int i = getPolarity().isHumbucking() ? 0 : 1;
           i < (getPolarity().isHumbucking() ? 4 : 3);
           i++) {
        Point p = points[i];
        newArea.add(Area.ring(p, lugDiameter, lugHole));
      }
      body[1] = newArea;
    }
    return body;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    final int x = width / 2;
    final int y = height / 2;
    final int bodyWidth = 8 * width / 32;
    final int bodyLength = 30 * width / 32;

    g2d.rotate(Math.PI / 4, x, y);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    g2d.setColor(BASE_COLOR);
    g2d.fillPolygon(
        new int[] {
          width * 9 / 16,
          width * 9 / 16,
          width * 11 / 16,
          width * 11 / 16},
        new int[] {
          (height - bodyLength) / 2,
          (height + bodyLength) / 2,
          height * 5 / 8,
          height * 3 / 8
        },
        4);

    Area bodyShape = Area.centeredRoundRect(x, y, bodyWidth, bodyLength, bodyWidth);
    g2d.setColor(BODY_COLOR);
    g2d.fill(bodyShape);
    g2d.setColor(Color.gray);
    g2d.draw(bodyShape);

    g2d.setColor(METAL_COLOR);
    int poleSize = 2;
    int poleSpacing = bodyLength / (DEFAULT_NUMBER_OF_STRINGS + 1);
    int poleY = y - poleSpacing * (DEFAULT_NUMBER_OF_STRINGS - 1) / 2;
    for (int i = 0; i < DEFAULT_NUMBER_OF_STRINGS; i++) {
      g2d.fill(Area.circle(x, poleY + i * poleSpacing, poleSize));
    }
  }

  @EditableProperty
  public SingleCoilType getType() {
    if (type == null) {
      type = SingleCoilType.Stratocaster;
    }
    return type;
  }

  public void setType(SingleCoilType type) {
    this.type = type;
    // Invalidate the body
    body = null;
  }

  @EditableProperty(name = "Pole Pieces")
  public PolePieceType getPolePieceType() {
    if (polePieceType == null) {
      polePieceType = PolePieceType.RODS;
    }
    return polePieceType;
  }

  public void setPolePieceType(PolePieceType polePieceType) {
    this.polePieceType = polePieceType;
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

  @EditableProperty(name = "Base")
  public Color getBaseColor() {
    if (baseColor == null) {
      baseColor = BASE_COLOR;
    }
    return baseColor;
  }

  public void setBaseColor(Color baseColor) {
    this.baseColor = baseColor;
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

  @EditableProperty(name = "Lugs")
  public Color getLugColor() {
    if (lugColor == null) {
      lugColor = LUG_COLOR;
    }
    return lugColor;
  }

  public void setLugColor(Color lugColor) {
    this.lugColor = lugColor;
  }

  public enum SingleCoilType {
    Stratocaster,
    Telecaster;
  }

  public enum PolePieceType {
    RODS("Single Rods"),
    RAIL("Single Rail"),
    RAIL_HUMBUCKER("Dual Rail Humbucker"),
    ROD_HUMBUCKER("Dual Rods"),
    NONE("None");

    private String label;

    PolePieceType(String label) {
      this.label = label;
    }

    public boolean isHumbucker() {
      return this == RAIL_HUMBUCKER || this == ROD_HUMBUCKER;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
