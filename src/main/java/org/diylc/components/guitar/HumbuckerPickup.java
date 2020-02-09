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

package org.diylc.components.guitar;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
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
    name = "Humbucker Pickup",
    category = "Guitar",
    author = "Branislav Stojkovic",
    description = "Double-coil humbucker guitar pickup (PAF, Mini Humbuckers, Filtertrons)",
    zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = AbstractGuitarPickup.INSTANCE_NAME_PREFIX,
    autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Guitar Wiring Diagram")
public class HumbuckerPickup extends AbstractSingleOrHumbuckerPickup {

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = Color.lightGray;
  private static Color BOBBIN_COLOR1 = Color.decode("#EAE3C6");
  private static Color BOBBIN_COLOR2 = Color.black;
  //  private static Color POINT_COLOR = Color.darkGray;
  private static Size WIDTH = Size.mm(36.5);
  private static Size LENGTH = Size.mm(68.58);
  private static Size WIDTH_MINI = Size.mm(29.3);
  private static Size LENGTH_MINI = Size.mm(67.4);
  private static Size WIDTH_FILTERTRON = Size.mm(34.9);
  private static Size LENGTH_FILTERTRON = Size.mm(71.4);
  private static Size FILTETRON_CUTOUT_MARGIN = Size.mm(1);
  private static Size LIP_WIDTH = Size.mm(12.7);
  private static Size LIP_LENGTH = Size.mm(7.9);
  private static Size EDGE_RADIUS = Size.mm(4);
  private static Size POINT_MARGIN = Size.mm(1.5);
  private static Size SCREW_LINE = Size.mm(1);
  private static Size POINT_SIZE = Size.mm(2);
  private static Size LIP_HOLE_SIZE = Size.mm(2);
  private static Size POLE_SIZE = Size.mm(4);
  private static Size POLE_SIZE_FILTERTRON = Size.mm(5);
  private static Size POLE_SPACING = Size.mm(10.1);

  private Color color = BASE_COLOR;
  private Color poleColor = METAL_COLOR;
  private HumbuckerType type;
  private boolean cover;
  private Boolean legs = true;
  private Color bobbinColor1 = BOBBIN_COLOR1;
  private Color bobbinColor2 = BOBBIN_COLOR2;
  private PolePieceType coilType1;
  private PolePieceType coilType2;

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    final Composite oldComposite = setTransparency(g2d);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : color);
    if (isFiltertron()) {
      g2d.fill(body[0]);
    }
    if (body[1] != null) {
      g2d.fill(body[1]);
    }

    if (!outlineMode) {
      if (body[4] != null) {
        g2d.setColor(getBobbinColor1());
        g2d.fill(body[4]);
      }
      if (body[5] != null) {
        g2d.setColor(getBobbinColor2());
        g2d.fill(body[5]);
      }
    }

    if (isFiltertron()) {
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : color);
      g2d.fill(body[0]);
    }
    g2d.setComposite(oldComposite);

    final Color finalBorderColor = tryBorderColor(outlineMode, color.darker());
    g2d.setColor(finalBorderColor);
    if (isFiltertron()) {
      g2d.draw(body[0]);
    }
    if (body[1] != null) {
      g2d.draw(body[1]);
    }

    if (!outlineMode) {
      g2d.setColor(getPoleColor());
      g2d.fill(body[3]);
      g2d.setColor(darkerOrLighter(getPoleColor()));
      g2d.draw(body[3]);
      if (body[6] != null) {
        g2d.draw(body[6]);
      }

      if (isFiltertron()) {
        if (body[4] != null) {
          g2d.setColor(getBobbinColor1().darker());
          g2d.draw(body[4]);
        }
        if (body[5] != null) {
          g2d.setColor(getBobbinColor2().darker());
          g2d.draw(body[5]);
        }
      }
    }

    if (isFiltertron()) {
      g2d.setColor(finalBorderColor);
      g2d.draw(body[0]);
    }

    drawMainLabel(g2d, project, outlineMode, componentState);

    drawTerminalLabels(g2d, finalBorderColor, project);
  }

  @Override
  protected OrientationHV getControlPointDirection() {
    return OrientationHV.VERTICAL;
  }

  @Override
  protected Shape[] getBody() {
    if (body == null) {
      body = new Shape[7];
      Point[] points = getControlPoints();
      int x = points[0].x;
      int y = points[0].y;
      int width = (int) getType().getWidth().convertToPixels();
      int length = (int) getType().getLength().convertToPixels();
      final int lipWidth = (int) LIP_WIDTH.convertToPixels();
      final int lipLength = (int) LIP_LENGTH.convertToPixels();
      final int edgeRadius = (int) EDGE_RADIUS.convertToPixels();
      final int pointMargin = (int) POINT_MARGIN.convertToPixels();
      final int pointSize = getClosestOdd(POINT_SIZE.convertToPixels());
      final int lipHoleSize = getClosestOdd(LIP_HOLE_SIZE.convertToPixels());
      final int poleSize = (int) getType().getPoleSize().convertToPixels();
      final int poleSpacing = (int) POLE_SPACING.convertToPixels();
      int coilSpacing = width / 2;
      int coilMargin = (width - coilSpacing) / 2;
      int poleMargin = (length - poleSpacing * 5) / 2;
      Area base = new Area(new RoundRectangle2D.Double(
          x /* + pointMargin */ - length,
          y - pointMargin,
          length,
          width,
          edgeRadius,
          edgeRadius));

      // base or cover
      body[0] = new Area(base);
      if (isFiltertron()) {
        int cutoutMargin = (int) FILTETRON_CUTOUT_MARGIN.convertToPixels();
        int cutoutHeight = poleSize + 2 * cutoutMargin;
        int cutoutWidth = 5 * poleSpacing + poleSize + 2 * cutoutMargin;
        Area cutout = new Area(new RoundRectangle2D.Double(
            x /* + pointMargin */ - length + poleMargin - poleSize / 2 - cutoutMargin,
            y - pointMargin + coilMargin - poleSize / 2 - cutoutMargin,
            cutoutWidth,
            cutoutHeight,
            cutoutHeight,
            cutoutHeight));
        ((Area) body[0]).subtract(cutout);
        cutout = new Area(new RoundRectangle2D.Double(
            x /* + pointMargin */ - length + poleMargin - poleSize / 2 - cutoutMargin,
            y - pointMargin + width - coilMargin - poleSize / 2 - cutoutMargin,
            cutoutWidth,
            cutoutHeight,
            cutoutHeight,
            cutoutHeight));
        ((Area) body[0]).subtract(cutout);
        int middleCutoutWidth = poleSpacing / 5;
        cutout = new Area(new Rectangle2D.Double(
            x /* + pointMargin */ - length / 2 - middleCutoutWidth / 2,
            y - pointMargin + width / 4,
            middleCutoutWidth,
            width / 2));
        ((Area) body[0]).subtract(cutout);
        if (!getLegs()) {
          cutout = new Area(new Ellipse2D.Double(
              x /* + pointMargin */ - length + lipHoleSize * 2,
              y - pointMargin + width / 2 - lipHoleSize * 0.75,
              lipHoleSize * 1.5,
              lipHoleSize * 1.5));
          ((Area) body[0]).subtract(cutout);
          cutout = new Area(new Ellipse2D.Double(
              x /* + pointMargin */ - lipHoleSize * 3,
              y - pointMargin + width / 2 - lipHoleSize * 0.75,
              lipHoleSize * 1.5,
              lipHoleSize * 1.5));
          ((Area) body[0]).subtract(cutout);
        }
      }

      // bobbins
      if (!getCover() || isFiltertron()) {
        int bobbinWidth = width / 2;
        int bobbinRadius = (int) (isFiltertron() ? edgeRadius * 1.1f : bobbinWidth);
        body[4] = new Area(new RoundRectangle2D.Double(
            x /* + pointMargin */ - length,
            y - pointMargin,
            length,
            bobbinWidth,
            bobbinRadius,
            bobbinRadius));
        body[5] = new Area(new RoundRectangle2D.Double(
            x /* + pointMargin */ - length,
            y - pointMargin + bobbinWidth,
            length,
            bobbinWidth,
            bobbinRadius,
            bobbinRadius));
      }

      // legs
      if (getLegs()) {
        Area legArea = new Area(new RoundRectangle2D.Double(
            x /* + pointMargin */ - length - lipLength,
            y - pointMargin + width / 2 - lipWidth / 2,
            length + 2 * lipLength,
            lipWidth,
            edgeRadius / 2,
            edgeRadius / 2));
        legArea.subtract((Area) (body[0]));
        legArea.subtract(new Area(new Ellipse2D.Double(
            x /* + pointMargin */ - length - lipLength / 2,
            y - pointMargin + width / 2 - lipHoleSize / 2,
            lipHoleSize,
            lipHoleSize)));
        legArea.subtract(new Area(new Ellipse2D.Double(
            x /* + pointMargin */ + lipLength / 2,
            y - pointMargin + width / 2 - lipHoleSize / 2,
            lipHoleSize,
            lipHoleSize)));
        legArea.subtract(base);
        body[1] = legArea;
      }

      // contact point
      body[2] = new Area(new Ellipse2D.Double(
          x - pointSize / 2, y - pointSize / 2, pointSize, pointSize));

      Area poleArea = new Area();
      Area poleDecorationArea = new Area();

      if (getCoilType1() == PolePieceType.Rail) {
        poleArea.add(new Area(new RoundRectangle2D.Double(
            x /* + pointMargin */ - length + poleMargin - poleSize / 2,
            y - pointMargin + coilMargin - poleSize / 2,
            poleSpacing * 5 + poleSize,
            poleSize,
            poleSize / 2,
            poleSize / 2)));
      } else if (getCoilType1() == PolePieceType.Rods || getCoilType1() == PolePieceType.Screws) {
        Path2D screwPath = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
          Ellipse2D pole = new Ellipse2D.Double(
              x /* + pointMargin */ - length + poleMargin + i * poleSpacing - poleSize / 2,
              y - pointMargin + coilMargin - poleSize / 2,
              poleSize,
              poleSize);
          poleArea.add(new Area(pole));
          if (getCoilType1() == PolePieceType.Screws) {
            if (i % 2 == 0) {
              screwPath.moveTo(
                  x /* + pointMargin */ - length + poleMargin + i * poleSpacing - poleSize / 2,
                  y - pointMargin + coilMargin - poleSize / 2);
              screwPath.lineTo(
                  x /* + pointMargin */
                  - length + poleMargin + i * poleSpacing - poleSize / 2 + poleSize,
                  y - pointMargin + coilMargin - poleSize / 2 + poleSize);
            } else {
              screwPath.moveTo(
                  x /* + pointMargin */ - length + poleMargin + i * poleSpacing - poleSize / 2,
                  y - pointMargin + coilMargin - poleSize / 2 + poleSize);
              screwPath.lineTo(
                  x /* + pointMargin */
                  - length + poleMargin + i * poleSpacing - poleSize / 2 + poleSize,
                  y - pointMargin + coilMargin - poleSize / 2);
            }
          }
        }
        if (getCoilType1() == PolePieceType.Screws) {
          Stroke pathStroke =
              ObjectCache.getInstance().fetchBasicStroke((float) SCREW_LINE.convertToPixels());
          Area screwArea = new Area(pathStroke.createStrokedShape(screwPath));
          screwArea.intersect(poleArea);
          poleDecorationArea.add(screwArea);
        }
      }

      if (getCoilType2() == PolePieceType.Rail) {
        poleArea.add(new Area(new RoundRectangle2D.Double(
            x /* + pointMargin */ - length + poleMargin - poleSize / 2,
            y - pointMargin + width - coilMargin - poleSize / 2,
            poleSpacing * 5 + poleSize,
            poleSize,
            poleSize / 2,
            poleSize / 2)));
      } else if (getCoilType2() == PolePieceType.Rods || getCoilType2() == PolePieceType.Screws) {
        Path2D screwPath = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
          poleArea.add(Area.circle(
              x /* + pointMargin */ - length + poleMargin + i * poleSpacing,
              y - pointMargin + width - coilMargin,
              poleSize));
          if (getCoilType1() == PolePieceType.Screws) {
            if (i % 2 != 0) {
              screwPath.moveTo(
                  x /* + pointMargin */ - length + poleMargin + i * poleSpacing - poleSize / 2,
                  y - pointMargin + width - coilMargin - poleSize / 2);
              screwPath.lineTo(
                  x /* + pointMargin */
                      - length
                      + poleMargin
                      + i * poleSpacing
                      - poleSize / 2
                      + poleSize,
                  y - pointMargin + width - coilMargin - poleSize / 2 + poleSize);
            } else {
              screwPath.moveTo(
                  x /* + pointMargin */ - length + poleMargin + i * poleSpacing - poleSize / 2,
                  y - pointMargin + width - coilMargin - poleSize / 2 + poleSize);
              screwPath.lineTo(
                  x /* + pointMargin */
                      - length
                      + poleMargin
                      + i * poleSpacing
                      - poleSize / 2
                      + poleSize,
                  y - pointMargin + width - coilMargin - poleSize / 2);
            }
          }
        }
        if (getCoilType2() == PolePieceType.Screws) {
          Stroke pathStroke =
              ObjectCache.getInstance().fetchBasicStroke((float) SCREW_LINE.convertToPixels());
          Area screwArea = new Area(pathStroke.createStrokedShape(screwPath));
          screwArea.intersect(poleArea);
          poleDecorationArea.add(screwArea);
        }
      }

      body[3] = poleArea;
      body[6] = poleDecorationArea;

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
    }
    return body;
  }

  @Override
  protected int getMainLabelYOffset() {
    return (int) (getType().getWidth().convertToPixels() / 2 - 20);
  }

  @Override
  public boolean isHumbucker() {
    return true;
  }

  public boolean isFiltertron() {
    return getType() == HumbuckerType.Filtertron;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    final int x = width / 2;
    final int y = height / 2;
    g2d.rotate(Math.PI / 4, x, y);

    int baseWidth = width / 2;
    int baseLength = 27 * width / 32;

    g2d.setColor(BASE_COLOR);
    g2d.fillRoundRect(
        x - baseWidth / 8,
        0,
        baseWidth / 4,
        height - 1,
        width / 16,
        width / 16);
    g2d.setColor(BASE_COLOR.darker());
    g2d.drawRoundRect(
        x - baseWidth / 8,
        0,
        baseWidth / 4,
        height - 1,
        width / 16,
        width / 16);

    g2d.setColor(BASE_COLOR);
    g2d.fillRoundRect(
        x - baseWidth / 2,
        y - baseLength / 2,
        baseWidth,
        baseLength,
        width / 8,
        width / 8);
    g2d.setColor(BASE_COLOR.darker());
    g2d.drawRoundRect(
        x - baseWidth / 2,
        y - baseLength / 2,
        baseWidth,
        baseLength,
        width / 8,
        width / 8);

    g2d.setColor(BOBBIN_COLOR1);
    g2d.fillRoundRect(
        x - baseWidth / 2,
        y - baseLength / 2,
        baseWidth / 2,
        baseLength,
        baseWidth / 2,
        baseWidth / 2);
    g2d.setColor(BOBBIN_COLOR2);
    g2d.fillRoundRect(
        x,
        y - baseLength / 2,
        baseWidth / 2,
        baseLength,
        baseWidth / 2,
        baseWidth / 2);

    g2d.setColor(METAL_COLOR);
    int poleSize = 2;
    int poleSpacing = baseLength / (DEFAULT_NUMBER_OF_STRINGS + 1);
    int poleY = y - poleSpacing * (DEFAULT_NUMBER_OF_STRINGS - 1) / 2;
    for (int i = 0; i < 6; i++) {
      g2d.fill(Area.circle(x - baseWidth / 4, poleY + i * poleSpacing, poleSize));
      g2d.fill(Area.circle(x + baseWidth / 4, poleY + i * poleSpacing, poleSize));
    }
  }

  @EditableProperty
  public HumbuckerType getType() {
    if (type == null) {
      type = HumbuckerType.PAF;
    }
    return type;
  }

  public void setType(HumbuckerType type) {
    this.type = type;
    // Invalidate the body
    body = null;
  }

  @EditableProperty(name = "Bobbin 1")
  public Color getBobbinColor1() {
    if (bobbinColor1 == null) {
      bobbinColor1 = BOBBIN_COLOR1;
    }
    return bobbinColor1;
  }

  public void setBobbinColor1(Color bobbinColor1) {
    this.bobbinColor1 = bobbinColor1;
  }

  @EditableProperty(name = "Bobbin 2")
  public Color getBobbinColor2() {
    if (bobbinColor2 == null) {
      bobbinColor2 = BOBBIN_COLOR2;
    }
    return bobbinColor2;
  }

  public void setBobbinColor2(Color bobbinColor2) {
    this.bobbinColor2 = bobbinColor2;
  }

  @EditableProperty
  public boolean getCover() {
    return cover;
  }

  public void setCover(boolean cover) {
    this.cover = cover;
    // Invalidate the body
    body = null;
  }

  @EditableProperty
  public Boolean getLegs() {
    if (legs == null) {
      legs = true;
    }
    return legs;
  }

  public void setLegs(Boolean legs) {
    this.legs = legs;
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

  @EditableProperty(name = "Pole Pieces 1")
  public PolePieceType getCoilType1() {
    if (coilType1 == null) {
      return PolePieceType.Screws;
    }
    return coilType1;
  }

  public void setCoilType1(PolePieceType coilType1) {
    this.coilType1 = coilType1;
    // Invalidate the body
    body = null;
  }

  @EditableProperty(name = "Pole Pieces 2")
  public PolePieceType getCoilType2() {
    if (coilType2 == null) {
      return PolePieceType.Rods;
    }
    return coilType2;
  }

  public void setCoilType2(PolePieceType coilType2) {
    this.coilType2 = coilType2;
    // Invalidate the body
    body = null;
  }

  @Override
  public Polarity getPolarity() {
    // just to disable editor
    return super.getPolarity();
  }

  public enum HumbuckerType {
    PAF(WIDTH, LENGTH, POLE_SIZE),
    Mini(WIDTH_MINI, LENGTH_MINI, POLE_SIZE),
    Filtertron(WIDTH_FILTERTRON, LENGTH_FILTERTRON, POLE_SIZE_FILTERTRON);

    private Size width;
    private Size length;
    private Size poleSize;

    HumbuckerType(Size width, Size length, Size poleSize) {
      this.width = width;
      this.length = length;
      this.poleSize = poleSize;
    }

    public Size getWidth() {
      return width;
    }

    public Size getLength() {
      return length;
    }

    public Size getPoleSize() {
      return poleSize;
    }

    @Override
    public String toString() {
      return name();
    }
  }

  public enum PolePieceType {
    Rods,
    Rail,
    Screws,
    None;
  }
}
