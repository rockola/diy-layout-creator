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
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractComponent;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.Area;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.StringValue;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

@StringValue
@ComponentDescriptor(
    name = "Toggle Switch",
    category = "Guitar",
    author = "Branislav Stojkovic",
    description = "3-position toggle switch, like the pickup switch on a Les Paul",
    zOrder = AbstractComponent.COMPONENT,
    instanceNamePrefix = "SW")
public class GuitarToggleSwitch extends AbstractTransparentComponent implements ISwitch {

  private static final long serialVersionUID = 1L;
  private static Color BASE_COLOR = Color.lightGray;
  private static Color WAFER_COLOR = Color.decode("#CD8500");
  private static Size LENGTH = Size.in(1.3);
  private static Size BASE_LENGTH = Size.mm(18);
  private static Size WAFER_THICKNESS = Size.in(0.05);
  private static Size TERMINAL_SPACING = Size.in(0.2);

  private Orientation orientation = Orientation.DEFAULT;
  private transient Area[] body;

  public GuitarToggleSwitch() {
    super();
    controlPoints = getFreshControlPoints(4);
    updateControlPoints();
  }

  @Override
  public void draw(
      Graphics2D g2d,
      ComponentState componentState,
      boolean outlineMode,
      Project project,
      IDrawingObserver drawingObserver) {
    Area[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (!componentState.isDragging()) {
      Composite oldComposite = setTransparency(g2d);
      body[1].fill(g2d, outlineMode ? Constants.TRANSPARENT_COLOR : WAFER_COLOR);
      body[0].fill(g2d, outlineMode ? Constants.TRANSPARENT_COLOR : BASE_COLOR);
      g2d.setComposite(oldComposite);
    }

    body[0].draw(g2d, tryBorderColor(outlineMode, WAFER_COLOR.darker()));
    body[1].draw(g2d, tryBorderColor(outlineMode, BASE_COLOR.darker()));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    body[2].draw(g2d, METAL_COLOR);
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[3];

      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int baseLength = (int) BASE_LENGTH.convertToPixels();
      int length = (int) LENGTH.convertToPixels();
      int waferThickness = getClosestOdd(WAFER_THICKNESS.convertToPixels());

      body[0] =
          Area.rect(
              x - waferThickness / 2,
              y,
              waferThickness - 1,
              baseLength + (length - baseLength) / 2);

      int bodyY = y + (length - baseLength) / 2;
      Area waferArea = new Area();
      waferArea.add(Area.rect(x + waferThickness / 2, bodyY, waferThickness * 3, baseLength));
      waferArea.add(Area.rect(x - waferThickness * 5 / 2, bodyY, waferThickness, baseLength));
      waferArea.add(Area.rect(x + waferThickness * 3 / 2, bodyY, waferThickness, baseLength));
      waferArea.add(Area.rect(x - waferThickness * 7 / 2, bodyY, waferThickness * 3, baseLength));
      body[1] = waferArea;

      int terminalSpacing = (int) TERMINAL_SPACING.convertToPixels();
      Path2D terminalPath = new Path2D.Double(Path2D.WIND_EVEN_ODD);
      terminalPath.moveTo(x - waferThickness * 3 / 2, bodyY + 1);
      terminalPath.lineTo(x - waferThickness * 3 / 2, bodyY + baseLength);
      terminalPath.lineTo(x, y + length);

      terminalPath.moveTo(x + waferThickness * 3 / 2, bodyY + 1);
      terminalPath.lineTo(x + waferThickness * 3 / 2, bodyY + baseLength);
      terminalPath.lineTo(x, y + length);

      terminalPath.moveTo(x - waferThickness * 5 / 2, bodyY + 1);
      terminalPath.lineTo(x - waferThickness * 5 / 2, bodyY + baseLength);
      terminalPath.lineTo(x - terminalSpacing, y + length);

      terminalPath.moveTo(x + waferThickness * 5 / 2, bodyY + 1);
      terminalPath.lineTo(x + waferThickness * 5 / 2, bodyY + baseLength);
      terminalPath.lineTo(x + terminalSpacing, y + length);
      body[2] = new Area(terminalPath);

      // Rotate if needed
      if (orientation.isRotated()) {
        AffineTransform rotation = orientation.getRotation(x, y);
        // TODO comment was "Skip the last one because it's already
        // rotated" - should this be so?
        for (int i = 0; i < body.length; i++) {
          body[i].transform(rotation);
        }
      }
    }
    return body;
  }

  private void updateControlPoints() {
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;
    int terminalSpacing = (int) TERMINAL_SPACING.convertToPixels();
    int length = (int) LENGTH.convertToPixels();

    controlPoints[1].setLocation(x - terminalSpacing, y + length);
    controlPoints[2].setLocation(x, y + length);
    controlPoints[3].setLocation(x + terminalSpacing, y + length);

    // Rotate if needed
    if (orientation != Orientation.DEFAULT) {
      double theta = orientation.getTheta();
      AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
      for (Point point : controlPoints) {
        rotation.transform(point, point);
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(Math.PI / 4, width / 2, height / 2);
    int baseLength = 20 * width / 32;

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3f));
    g2d.setColor(BASE_COLOR);
    g2d.drawLine(width / 2, 0, width / 2, baseLength + (width - baseLength) / 2);
    g2d.setColor(WAFER_COLOR);
    g2d.fillRect(8 * width / 32, (width - baseLength) / 2, 16 * width / 32, baseLength);
    g2d.setColor(METAL_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    g2d.drawLine(
        width / 2 - 2,
        (width - baseLength) / 2,
        width / 2 - 2,
        baseLength + (width - baseLength) / 2);
    g2d.drawLine(
        width / 2 + 2,
        (width - baseLength) / 2,
        width / 2 + 2,
        baseLength + (width - baseLength) / 2);
    g2d.drawLine(
        width / 2 - 5,
        (width - baseLength) / 2,
        width / 2 - 5,
        baseLength + (width - baseLength) / 2);
    g2d.drawLine(
        width / 2 + 5,
        (width - baseLength) / 2,
        width / 2 + 5,
        baseLength + (width - baseLength) / 2);
    int dx = 2 * width / 32;
    int dy = 4 * width / 32;
    g2d.drawLine(
        width / 2 - 2,
        baseLength + (width - baseLength) / 2,
        width / 2 - 2 + dx,
        baseLength + (width - baseLength) / 2 + dy);
    g2d.drawLine(
        width / 2 + 2,
        baseLength + (width - baseLength) / 2,
        width / 2 + 2 - dx,
        baseLength + (width - baseLength) / 2 + dy);

    g2d.drawLine(
        width / 2 - 5,
        baseLength + (width - baseLength) / 2,
        width / 2 - 5 - dx,
        baseLength + (width - baseLength) / 2 + dy);
    g2d.drawLine(
        width / 2 + 5,
        baseLength + (width - baseLength) / 2,
        width / 2 + 5 + dx,
        baseLength + (width - baseLength) / 2 + dy);
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.ALWAYS;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Invalidate the body
    body = null;
  }

  //  @Override
  //  public String getControlPointNodeName(int index) {
  //    // we don't want the switch to produce any nodes, it just makes connections
  //    return null;
  //  }
  //
  // switch stuff

  @Override
  public int getPositionCount() {
    return 3;
  }

  @Override
  public String getPositionName(int position) {
    switch (position) {
      case 0:
        return "Treble";
      case 1:
        return "Middle";
      case 2:
        return "Rhythm";
      default:
    }
    return null;
  }

  @Override
  public boolean arePointsConnected(int index1, int index2, int position) {
    switch (position) {
      case 0:
        return index1 == 1 && index2 == 2;
      case 1:
        return index1 > 0;
      case 2:
        return index1 == 2 && index2 == 3;
      default:
    }
    return false;
  }
}
