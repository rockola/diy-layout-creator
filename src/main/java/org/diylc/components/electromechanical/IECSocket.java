/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2018 held jointly by the individual authors.

  This file is part of DIYLC.

  DIYLC is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  DIYLC is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.diylc.components.electromechanical;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.components.Area;
import org.diylc.components.RoundedPolygon;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "IEC Socket",
    category = "Electro-Mechanical",
    author = "Branislav Stojkovic",
    description = "Panel-mounted IEC 60320 C14 power inlet",
    zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = "IEC",
    autoEdit = false)
public class IECSocket extends AbstractMultiPartComponent<String> {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(IECSocket.class);

  private static Map<String, IEC60320> subtypes = new HashMap<>();

  static {
    subtypes.put("C1", IEC60320.C1());
    subtypes.put("C2", IEC60320.C2());
    subtypes.put("C13", IEC60320.C14());
  }

  private static Size IEC_60320_C14_LUG_HORIZONTAL_SPACING = Size.mm(7);
  private static Size IEC_60320_C14_LUG_VERTICAL_SPACING = Size.mm(4);
  private static Size IEC_60320_C14_LUG_WIDTH = Size.mm(2);
  private static Size IEC_60320_C14_LUG_HEIGHT = Size.mm(4);
  private static Size IEC_60320_C14_OPENING_WIDTH = Size.mm(24);
  private static Size IEC_60320_C14_OPENING_HEIGHT = Size.mm(16);
  private static Size IEC_60320_C14_OUTLINE_WIDTH = Size.mm(30.5);
  private static Size IEC_60320_C14_OUTLINE_HEIGHT = Size.mm(22.5);
  // IEC 60320 C14 spec says R = 3 max.
  private static Size IEC_60320_C14_OPENING_LOWER_CORNER_RADIUS = Size.mm(1.5);
  // IEC 60320 C14 spec says R = 2 max.
  private static Size IEC_60320_C14_OPENING_UPPER_CORNER_RADIUS = Size.mm(.75);
  private static Size IEC_60320_C14_OPENING_UPPER_CORNER_VERTICAL_OFFSET = Size.mm(3);

  // common
  private static Size HORIZONTAL_SPACING = new Size(0.3d, SizeUnit.in);
  private static Size VERTICAL_SPACING = new Size(0.2d, SizeUnit.in);
  private static Size LUG_WIDTH = new Size(4d, SizeUnit.mm);
  private static Size LUG_THICKNESS = new Size(0.8d, SizeUnit.mm);

  // simple
  private static Size SIMPLE_CUTOUT_LENGTH = new Size(27.5d, SizeUnit.mm);
  private static Size SIMPLE_CUTOUT_WIDTH = new Size(19.5d, SizeUnit.mm);
  private static Size SIMPLE_BASE_LENGTH = new Size(30.8d, SizeUnit.mm);
  private static Size SIMPLE_BASE_WIDTH = new Size(22.6d, SizeUnit.mm);
  private static Size SIMPLE_BASE_RADIUS = new Size(3d, SizeUnit.mm);
  private static Size SIMPLE_CUTOUT_SLANT = new Size(5d, SizeUnit.mm);
  private static Size SIMPLE_CUTOUT_RADIUS = new Size(1d, SizeUnit.mm);
  private static Size SIMPLE_LENGTH = new Size(50d, SizeUnit.mm);
  private static Size SIMPLE_OUTER_RADIUS = new Size(6d, SizeUnit.mm);
  private static Size HOLE_SPACING = new Size(40d, SizeUnit.mm);
  private static Size HOLE_DIAMETER = new Size(3d, SizeUnit.mm);

  private static Color BODY_COLOR = Color.decode("#555555");
  private static Color BORDER_COLOR = BODY_COLOR.darker();

  protected Point[] controlPoints = new Point[] {
    new Point(0, 0),
    new Point(0, 0),
    new Point(0, 0)
  };

  protected String value;
  private Orientation orientation = Orientation.DEFAULT;

  private IEC60320 iec;

  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;

  public IECSocket() {
    super();
    iec = IEC60320.C14();
    controlPoints = iec.getControlPoints();
    updateControlPoints();
  }

  private void updateControlPoints() {
    final Point firstPoint = controlPoints[0];
    final int horizontalSpacing = (int) HORIZONTAL_SPACING.convertToPixels();
    final int verticalSpacing = (int) VERTICAL_SPACING.convertToPixels();

    controlPoints[1].setLocation(firstPoint.x - horizontalSpacing, firstPoint.y + verticalSpacing);
    controlPoints[2].setLocation(firstPoint.x + horizontalSpacing, firstPoint.y + verticalSpacing);

    if (orientation != Orientation.DEFAULT) {
      double theta = orientation.getTheta();
      AffineTransform rotation =
          AffineTransform.getRotateInstance(theta, firstPoint.x, firstPoint.y);

      for (int i = 1; i < controlPoints.length; i++) {
        rotation.transform(controlPoints[i], controlPoints[i]);
      }
    }
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
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
  }

  @EditableProperty
  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public Orientation getOrientation() {
    if (orientation == null) {
      orientation = Orientation.DEFAULT;
    }
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
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

    final AffineTransform at = g2d.getTransform();
    Point displaced = controlPoints[0];
    g2d.translate(displaced.x, displaced.y);
    if (orientation != Orientation.DEFAULT) {
      g2d.rotate(orientation.getTheta());
    }

    // Draw body
    final Color fillColor = (outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
    final Color drawColor = tryBorderColor(outlineMode, getBorderColor());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    int i = 0;
    for (Area a : getBody()) {
      if (a != null) {
        a.fillDraw(g2d, fillColor, drawColor);
      }
    }
    drawingObserver.stopTracking();
    final Color pinFillColor = (outlineMode ? Constants.TRANSPARENT_COLOR : METAL_COLOR);
    for (Pin pin : iec.getPins()) {
      pin.getShape().fillDraw(g2d, pinFillColor, drawColor);
    }

    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);

    g2d.setTransform(at);
  }

  public Area[] getBody() {
    return iec.getBody();
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BODY_COLOR);
    int margin = (int) (2f * width / 32);
    int slant = (int) (5f * width / 32);
    int terminal = (int) (4f * width / 32);
    int terminalSpacingH = (int) (7f * width / 32);
    int terminalSpacingV = (int) (4f * width / 32);
    RoundedPolygon poly = new RoundedPolygon(
        new Point[] {
          new Point(width / 2, height / 5),
          new Point(width - margin - slant, height / 5),
          new Point(width - margin, height / 5 + slant),
          new Point(width - margin, height * 4 / 5),
          new Point(margin, height * 4 / 5),
          new Point(margin, height / 5 + slant),
          new Point(margin + slant, height / 5),
        },
        new double[] {2d});
    g2d.fill(poly);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(poly);
    g2d.setColor(METAL_COLOR);
    for (int i = -1; i <= 1; i++) {
      g2d.drawLine(
          width / 2 + terminalSpacingH * i - terminal / 2,
          height / 2 + terminalSpacingV * Math.abs(i) - terminalSpacingV / 2,
          width / 2 + terminalSpacingH * i + terminal / 2,
          height / 2 + terminalSpacingV * Math.abs(i) - terminalSpacingV / 2);
    }
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

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
