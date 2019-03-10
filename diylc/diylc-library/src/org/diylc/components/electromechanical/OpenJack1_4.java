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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Open 1/4\" Jack", category = "Electro-Mechanical", author = "Branislav Stojkovic",
    description = "Switchcraft-style open panel mount 1/4\" phono jack, stereo and mono", stretchable = false,
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "J")
public class OpenJack1_4 extends AbstractMultiPartComponent<String> {

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

  private String value = "";
  private Point[] controlPoints = new Point[] {new Point(0, 0), new Point(0, 0), new Point(0, 0)};
  transient Area[] body;
  @Deprecated
  private Orientation orientation = Orientation.DEFAULT;
  private Integer angle = 0;
  private OpenJackType type = OpenJackType.MONO;
  private boolean showLabels = true;

  public OpenJack1_4() {
    super();
    updateControlPoints();
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
//    if (componentState != ComponentState.DRAGGING) {
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : WAFER_COLOR);
    g2d.fill(body[0]);

    g2d.setComposite(oldComposite);
//    }

    Color finalBorderColor;

    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor = theme.getOutlineColor();
    } else {
      finalBorderColor =  WAFER_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);

//    if (componentState != ComponentState.DRAGGING) {
      oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BASE_COLOR);
      
      drawingObserver.startTrackingContinuityArea(true);
      g2d.fill(body[1]);
      g2d.fill(body[2]);
      if (body[3] != null)
        g2d.fill(body[3]);
      drawingObserver.stopTrackingContinuityArea();

      g2d.setComposite(oldComposite);
//    }

    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor = theme.getOutlineColor();
    } else {
      finalBorderColor = BASE_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[1]);
    g2d.draw(body[2]);
    if (body[3] != null)
      g2d.draw(body[3]);

    // draw labels
    if (showLabels) {
      g2d.setColor(BASE_COLOR.darker());
      g2d.setFont(project.getFont().deriveFont(project.getFont().getSize2D() * 0.8f));
      int springLength = (int) SPRING_LENGTH.convertToPixels();
      int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();
      int centerY = controlPoints[0].y + springLength - holeToEdge;
      Point tipLabel = new Point(controlPoints[0].x, (int) (controlPoints[0].y + holeToEdge * 1.25));
      AffineTransform ringTransform = AffineTransform.getRotateInstance(getType() == OpenJackType.SWITCHED ? SWITCH_THETA : RING_THETA, controlPoints[0].x, centerY);
      AffineTransform sleeveTransform = AffineTransform.getRotateInstance(getType() == OpenJackType.SWITCHED ? SLEEVE_SWITCHED_THETA : SLEEVE_THETA, controlPoints[0].x, centerY);
      Point ringOrSwitchLabel = new Point(0, 0);
      Point sleeveLabel = new Point(0, 0);
      ringTransform.transform(tipLabel, ringOrSwitchLabel);
      sleeveTransform.transform(tipLabel, sleeveLabel);

      if (getTheta() != 0) {
        AffineTransform rotation = AffineTransform.getRotateInstance(getTheta(), controlPoints[0].x, controlPoints[0].y);
        rotation.transform(tipLabel, tipLabel);
        rotation.transform(ringOrSwitchLabel, ringOrSwitchLabel);
        rotation.transform(sleeveLabel, sleeveLabel);
      }
      drawCenteredText(g2d, "T", tipLabel.x, tipLabel.y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      drawCenteredText(g2d, "S", sleeveLabel.x, sleeveLabel.y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      if (getType() == OpenJackType.STEREO)
        drawCenteredText(g2d, "R", ringOrSwitchLabel.x, ringOrSwitchLabel.y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      if (getType() == OpenJackType.SWITCHED)
        drawCenteredText(g2d, "Sw", ringOrSwitchLabel.x, ringOrSwitchLabel.y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }
    
    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[4];

      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int outerDiameter = getClosestOdd(OUTER_DIAMETER.convertToPixels());
      int innerDiameter = getClosestOdd(INNER_DIAMETER.convertToPixels());
      int ringDiameter = getClosestOdd(RING_DIAMETER.convertToPixels());
      int springLength = (int) SPRING_LENGTH.convertToPixels();
      int springWidth = (int) SPRING_WIDTH.convertToPixels();
      int holeDiameter = getClosestOdd(HOLE_DIAMETER.convertToPixels());
      int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();

      int centerY = y + springLength - holeToEdge;

      Area wafer =
          new Area(new Ellipse2D.Double(x - outerDiameter / 2, centerY - outerDiameter / 2, outerDiameter,
              outerDiameter));
      wafer.subtract(new Area(new Ellipse2D.Double(x - ringDiameter / 2, centerY - ringDiameter / 2, ringDiameter,
          ringDiameter)));

      body[0] = wafer;

      Area tip =
          new Area(new RoundRectangle2D.Double(x - springWidth / 2, y - holeToEdge, springWidth, springLength
              - ringDiameter / 2, springWidth, springWidth));
      tip.subtract(new Area(
          new Ellipse2D.Double(x - holeDiameter / 2, y - holeDiameter / 2, holeDiameter, holeDiameter)));
      tip.subtract(wafer);

      body[1] = tip;

      Area sleeve =
          new Area(new RoundRectangle2D.Double(x - springWidth / 2, y - holeToEdge, springWidth, springLength,
              springWidth, springWidth));
      sleeve.subtract(new Area(new Ellipse2D.Double(x - holeDiameter / 2, y - holeDiameter / 2, holeDiameter,
          holeDiameter)));
      sleeve.transform(AffineTransform.getRotateInstance(getType() == OpenJackType.SWITCHED ? SLEEVE_SWITCHED_THETA : SLEEVE_THETA, x, centerY));
      sleeve.add(new Area(new Ellipse2D.Double(x - ringDiameter / 2, centerY - ringDiameter / 2, ringDiameter,
          ringDiameter)));
      sleeve.subtract(new Area(new Ellipse2D.Double(x - innerDiameter / 2, centerY - innerDiameter / 2, innerDiameter,
          innerDiameter)));

      body[2] = sleeve;

      if (getType() != OpenJackType.MONO) {
        Area ringOrSwitch =
            new Area(new RoundRectangle2D.Double(x - springWidth / 2, y - holeToEdge, springWidth, springLength,
                springWidth, springWidth));
        ringOrSwitch.subtract(new Area(new Ellipse2D.Double(x - holeDiameter / 2, y - holeDiameter / 2, holeDiameter,
            holeDiameter)));
        ringOrSwitch.transform(AffineTransform.getRotateInstance(getType() == OpenJackType.SWITCHED ? SWITCH_THETA : RING_THETA, x, centerY));
        ringOrSwitch.subtract(new Area(new Ellipse2D.Double(x - outerDiameter / 2, centerY - outerDiameter / 2, outerDiameter,
            outerDiameter)));

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

  private void updateControlPoints() {
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;

    int springLength = (int) SPRING_LENGTH.convertToPixels();
    int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();

    int centerY = y + springLength - holeToEdge;

    AffineTransform.getRotateInstance(getType() == OpenJackType.SWITCHED ? SLEEVE_SWITCHED_THETA : SLEEVE_THETA, x, centerY).transform(controlPoints[0], controlPoints[1]);
    AffineTransform.getRotateInstance(getType() == OpenJackType.SWITCHED ? SWITCH_THETA : RING_THETA, x, centerY).transform(controlPoints[0], controlPoints[2]);

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

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(6f * width / 32));
    g2d.setColor(BASE_COLOR);
    g2d.drawLine(width / 2, 4 * width / 32, width / 2, width / 4);

    g2d.rotate(RING_THETA, width / 2, height / 2);

    g2d.drawLine(width / 2, 4 * width / 32, width / 2, width / 4);

    g2d.setColor(WAFER_COLOR);
    g2d.draw(new Ellipse2D.Double(width / 2 - waferDiameter / 2, height / 2 - waferDiameter / 2, waferDiameter,
        waferDiameter));

    g2d.setColor(BASE_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f * width / 32));
    g2d.draw(new Ellipse2D.Double(width / 2 - sleeveDiameter / 2, height / 2 - sleeveDiameter / 2, sleeveDiameter,
        sleeveDiameter));

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(6f * width / 32));
    g2d.rotate(-Math.PI / 2, width / 2, height / 2);

    g2d.drawLine(width / 2, 4 * width / 32, width / 2, width / 3);
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return index < 2 || getType() == OpenJackType.STEREO;
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.controlPoints[index].setLocation(point);
    // Invalidate the body
    body = null;
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
  public Integer getAngle() {
    if (angle == null) {
      if (orientation != null && orientation != Orientation.DEFAULT)
        angle = Integer.parseInt(orientation.name().replace("_", ""));
      else 
        angle = 0;
    }
    return angle;
  }
  
  public void setAngle(Integer angle) {
    this.angle = angle;
    updateControlPoints();
    // Invalidate the body
    body = null;
  }
  
  protected double getTheta() {
    return Math.toRadians(getAngle());
  }

  @EditableProperty
  public OpenJackType getType() {
    return type;
  }

  public void setType(OpenJackType type) {
    this.type = type;
    updateControlPoints();
    // Invalidate the body
    body = null;
  }

  @EditableProperty(name = "Labels")
  public boolean getShowLabels() {
    return showLabels;
  }

  public void setShowLabels(boolean showLabels) {
    this.showLabels = showLabels;
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    if (index == 0)
      return "Tip";
    if (index == 1)
      return "Sleeve";
    if (index == 2 && getType() == OpenJackType.STEREO)
      return "Ring";
    if (index == 2 && getType() == OpenJackType.SWITCHED)
      return "Shunt";
    return null;
  }
  
  public enum OpenJackType {

    MONO, STEREO, SWITCHED;

    @Override
    public String toString() {
      return name().substring(0, 1) + name().substring(1).toLowerCase();
    }
  }
}
