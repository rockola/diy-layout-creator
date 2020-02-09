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

package org.diylc.components.electromechanical;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.Area;
import org.diylc.components.transform.JackTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(
    name = "Open Jack",
    category = "Electro-Mechanical",
    author = "Branislav Stojkovic",
    description = "Switchcraft-style open panel mount jack, stereo and mono",
    zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = "J",
    xmlTag = "jack:open",
    transformer = JackTransformer.class)
public class OpenJack extends AbstractJack {

  private static final double RING_THETA = Math.PI * 0.795;
  private static final double SLEEVE_THETA = Math.PI * 0.29444444444;
  private static final double SLEEVE_SWITCHED_THETA = Math.PI * 4 / 3;
  private static final double SWITCH_THETA = Math.PI * 5 / 3;

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = Color.lightGray;
  private static Color WAFER_COLOR = Color.decode("#CD8500");

  private static Size OUTER_DIAMETER = new Size(0.75d, SizeUnit.in);
  private static Size INNER_DIAMETER = new Size(0.25d, SizeUnit.in);
  private static Size RING_DIAMETER = new Size(0.33d, SizeUnit.in);
  private static Size SPRING_LENGTH = new Size(0.563d, SizeUnit.in);
  private static Size SPRING_WIDTH = new Size(0.12d, SizeUnit.in);
  private static Size HOLE_DIAMETER = new Size(0.05d, SizeUnit.in);
  private static Size HOLE_TO_EDGE = new Size(0.063d, SizeUnit.in);

  {
    controlPoints = new Point[] {new Point(0, 0), new Point(0, 0), new Point(0, 0)};
    showLabels = true;
  }

  public OpenJack() {
    super();
    updateControlPoints();
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
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : WAFER_COLOR);
    g2d.fill(body[0]);
    g2d.setComposite(oldComposite);

    Color finalBorderColor = tryBorderColor(outlineMode, WAFER_COLOR.darker());
    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);

    drawingObserver.startTrackingContinuityArea(true);
    oldComposite = setTransparency(g2d);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BASE_COLOR);
    g2d.fill(body[1]);
    g2d.fill(body[2]);
    if (body[3] != null) {
      g2d.fill(body[3]);
    }
    g2d.setComposite(oldComposite);
    drawingObserver.stopTrackingContinuityArea();

    finalBorderColor = tryBorderColor(outlineMode, BASE_COLOR.darker());
    g2d.setColor(finalBorderColor);
    g2d.draw(body[1]);
    g2d.draw(body[2]);
    if (body[3] != null) {
      g2d.draw(body[3]);
    }

    // draw labels
    if (showLabels) {
      g2d.setColor(BASE_COLOR.darker());
      g2d.setFont(project.getFont().deriveFont(project.getFont().getSize2D() * 0.8f));
      int springLength = (int) SPRING_LENGTH.convertToPixels();
      int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();
      int centerY = controlPoints[0].y + springLength - holeToEdge;
      Point tipLabel =
          new Point(controlPoints[0].x, (int) (controlPoints[0].y + holeToEdge * 1.25));
      AffineTransform ringTransform =
          AffineTransform.getRotateInstance(
              getType().isSwitched() ? SWITCH_THETA : RING_THETA,
              controlPoints[0].x,
              centerY);
      AffineTransform sleeveTransform =
          AffineTransform.getRotateInstance(
              getType().isSwitched() ? SLEEVE_SWITCHED_THETA : SLEEVE_THETA,
              controlPoints[0].x,
              centerY);
      Point ringOrSwitchLabel = new Point(0, 0);
      Point sleeveLabel = new Point(0, 0);
      ringTransform.transform(tipLabel, ringOrSwitchLabel);
      sleeveTransform.transform(tipLabel, sleeveLabel);

      if (getTheta() != 0) {
        AffineTransform rotation =
            AffineTransform.getRotateInstance(getTheta(), controlPoints[0].x, controlPoints[0].y);
        rotation.transform(tipLabel, tipLabel);
        rotation.transform(ringOrSwitchLabel, ringOrSwitchLabel);
        rotation.transform(sleeveLabel, sleeveLabel);
      }
      drawJackConnectionLabel(g2d, "T", tipLabel);
      drawJackConnectionLabel(g2d, "S", sleeveLabel);
      if (getType().isStereo()) {
        drawJackConnectionLabel(g2d, "R", ringOrSwitchLabel);
      } else if (getType().isSwitched()) {
        // are there no stereo switched jacks?
        drawJackConnectionLabel(g2d, "Sw", ringOrSwitchLabel);
      }
    }

    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  private void drawJackConnectionLabel(Graphics2D g2d, String text, Point position) {
    StringUtils.drawCenteredText(
        g2d,
        text,
        position.x,
        position.y,
        HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[4];

      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      final int outerDiameter = getClosestOdd(OUTER_DIAMETER.convertToPixels());
      final int innerDiameter = getClosestOdd(INNER_DIAMETER.convertToPixels());
      final int ringDiameter = getClosestOdd(RING_DIAMETER.convertToPixels());
      final int springLength = (int) SPRING_LENGTH.convertToPixels();
      final int springWidth = (int) SPRING_WIDTH.convertToPixels();
      final int holeDiameter = getClosestOdd(HOLE_DIAMETER.convertToPixels());
      final int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();
      final int centerY = y + springLength - holeToEdge;

      Area wafer = new Area(new Ellipse2D.Double(
          x - outerDiameter / 2,
          centerY - outerDiameter / 2,
          outerDiameter,
          outerDiameter));
      wafer.subtract(new Area(new Ellipse2D.Double(
          x - ringDiameter / 2, centerY - ringDiameter / 2, ringDiameter, ringDiameter)));
      body[0] = wafer;

      Area tip = new Area(new RoundRectangle2D.Double(
          x - springWidth / 2,
          y - holeToEdge,
          springWidth,
          springLength - ringDiameter / 2,
          springWidth,
          springWidth));
      tip.subtract(new Area(new Ellipse2D.Double(
          x - holeDiameter / 2, y - holeDiameter / 2, holeDiameter, holeDiameter)));
      tip.subtract(wafer);
      body[1] = tip;

      Area sleeve = new Area(new RoundRectangle2D.Double(
          x - springWidth / 2,
          y - holeToEdge,
          springWidth,
          springLength,
          springWidth,
          springWidth));
      sleeve.subtract(new Area(new Ellipse2D.Double(
          x - holeDiameter / 2, y - holeDiameter / 2, holeDiameter, holeDiameter)));
      sleeve.transform(
          AffineTransform.getRotateInstance(
              getType().isSwitched() ? SLEEVE_SWITCHED_THETA : SLEEVE_THETA,
              x,
              centerY));
      sleeve.add(new Area(new Ellipse2D.Double(
          x - ringDiameter / 2, centerY - ringDiameter / 2, ringDiameter, ringDiameter)));
      sleeve.subtract(new Area(new Ellipse2D.Double(
          x - innerDiameter / 2,
          centerY - innerDiameter / 2,
          innerDiameter,
          innerDiameter)));
      body[2] = sleeve;

      if (!getType().isMono()) {
        Area ringOrSwitch = new Area(new RoundRectangle2D.Double(
            x - springWidth / 2,
            y - holeToEdge,
            springWidth,
            springLength,
            springWidth,
            springWidth));
        ringOrSwitch.subtract(Area.circle(x, y, holeDiameter));
        ringOrSwitch.transform(AffineTransform.getRotateInstance(
            getType().isSwitched() ? SWITCH_THETA : RING_THETA,
            x,
            centerY));
        ringOrSwitch.subtract(Area.circle(x, centerY, outerDiameter));
        body[3] = ringOrSwitch;
      }

      // Rotate if needed
      if (getTheta() != 0) {
        AffineTransform rotation = AffineTransform.getRotateInstance(getTheta(), x, y);
        // Skip the last one because it's already rotated
        for (int i = 0; i < body.length; i++) {
          Shape shape = body[i];
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
  protected void updateControlPoints() {
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;
    final int springLength = (int) SPRING_LENGTH.convertToPixels();
    final int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();
    final int centerY = y + springLength - holeToEdge;

    AffineTransform.getRotateInstance(
        getType().isSwitched() ? SLEEVE_SWITCHED_THETA : SLEEVE_THETA,
            x,
            centerY).transform(controlPoints[0], controlPoints[1]);
    AffineTransform.getRotateInstance(
        getType().isSwitched() ? SWITCH_THETA : RING_THETA,
            x,
            centerY).transform(controlPoints[0], controlPoints[2]);
    // Rotate if needed
    if (getTheta() != 0) {
      AffineTransform rotation = AffineTransform.getRotateInstance(getTheta(), x, y);
      for (Point point : controlPoints) {
        rotation.transform(point, point);
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int waferDiameter = 15 * width / 32;
    int sleeveDiameter = 9 * width / 32;
    int x = width / 2;
    int y = height / 2;
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(6f * width / 32));
    g2d.setColor(BASE_COLOR);
    g2d.drawLine(x, 4 * width / 32, x, width / 4);

    g2d.rotate(RING_THETA, width / 2, height / 2);
    g2d.drawLine(x, 4 * width / 32, x, width / 4);

    g2d.setColor(WAFER_COLOR);
    g2d.draw(Area.circle(x, y, waferDiameter));

    g2d.setColor(BASE_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f * width / 32));
    g2d.draw(Area.circle(x, y, sleeveDiameter));

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(6f * width / 32));
    g2d.rotate(-HALF_PI, x, y);

    g2d.drawLine(x, 4 * width / 32, x, width / 3);
  }
}
