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

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.diylc.components.Area;
import org.diylc.core.measures.Size;

public class IEC60320 {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(IEC60320.class);
  private static final Map<String, IEC60320> couplers = new HashMap<>();
  private static final Point reference = new Point(0,0);

  enum CouplerType {
    CONNECTOR, // i.e. plug
    INLET // i.e. socket
  }

  static {
    couplers.put("C1", C1());
    couplers.put("C2", C2());
    //couplers.put("C13", C13());
    couplers.put("C14", C14());
  }

  /**
     Type of coupler (male connector/plug or female inlet/socket)
  */
  private final CouplerType couplerType;
  /**
     Coupler pins.
  */
  private final List<Pin> pins;
  /**
     Coupler main area (plug for connectors, opening for inlets);
  */
  private final Area coupler;
  /**
     Area surrounding the main area.
  */
  private final Area courtyard;
  /**
     Area of mounting ears (optional).
  */
  private final Area mount;

  private Area[] body;

  private IEC60320(CouplerType type, List<Pin> pins, Area coupler, Area courtyard, Area mount) {
    super();
    this.couplerType = type;
    this.pins = pins;
    this.coupler = coupler;
    this.courtyard = courtyard;
    this.mount = mount;
  }

  private IEC60320(CouplerType type, List<Pin> pins, Area coupler, Area courtyard) {
    this(type, pins, coupler, courtyard, null);
  }

  public int howManyPins() {
    return pins.size();
  }

  public List<Pin> getPins() {
    return pins;
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[3];
      body[0] = mount;
      body[1] = courtyard;
      body[2] = coupler;
    }
    return body;
  }

  public Point[] getControlPoints() {
    Point[] controlPoints = new Point[pins.size()];
    for (int i = 0; i < pins.size(); i++) {
      controlPoints[i] = pins.get(i).getOffset();
    }
    return controlPoints;
  }

  /**
   * IEC60320 C1
   *
   * <p>0.2A 250V connector for class II equipment and cold conditions.
   *
   * @return Instance of IEC60320 as per IEC60320-C1.
   */
  public static IEC60320 C1() {
    List<Pin> pins = new ArrayList<>();
    final Size pinHorizontalSpacing = Size.mm(6.6);
    final Size pinWidth = Size.mm(2.9);
    pins.add(Pin.circular(pinHorizontalSpacing.half().negative(), pinWidth));
    pins.add(Pin.circular(pinHorizontalSpacing.half(), pinWidth));
    final Size inletHeight = Size.mm(8.2);
    final Size inletWidth = Size.mm(14.5);
    final Size courtyardWidth = Size.mm(18.5);
    final Size courtyardHeight = Size.mm(13);
    final Size courtyardCornerRadius = Size.mm(0.5);
    return new IEC60320(
        CouplerType.CONNECTOR,
        pins,
        Area.centeredRoundRect(
            reference,
            inletWidth,
            inletHeight,
            inletHeight.half()),
         Area.centeredRoundRect(
            reference,
            courtyardWidth,
            courtyardHeight,
            courtyardCornerRadius));
  }

  /**
   * IEC60320 C2
   *
   * <p>0.2A 250V inlet for class II equipment and cold conditions.
   *
   * @return Instance of IEC60320 as per IEC60320-C2.
   */
  public static IEC60320 C2() {
    List<Pin> pins = new ArrayList<>();
    final Size pinHorizontalSpacing = Size.mm(6.6);
    final Size pinWidth = Size.mm(2.36);
    pins.add(Pin.circular(pinHorizontalSpacing.half().negative(), pinWidth));
    pins.add(Pin.circular(pinHorizontalSpacing.half(), pinWidth));
    final Size inletHeight = Size.mm(8.2);
    final Size inletWidth = Size.mm(14.5);
    final Size courtyardWidth = Size.mm(19);
    final Size courtyardHeight = Size.mm(13.5);
    final Size courtyardCornerRadius = Size.mm(2.5);
    return new IEC60320(
        CouplerType.INLET,
        pins,
        Area.centeredRoundRect(
            reference,
            inletWidth,
            inletHeight,
            inletHeight.half()),
        Area.centeredRoundRect(
            reference,
            courtyardWidth,
            courtyardHeight,
            courtyardCornerRadius));
  }

  /**
   * IEC60320 C14
   *
   * <p>10 A 250 V appliance coupler for class I equipment and cold
   * conditions, inlet.
   *
   * @return Instance of IEC60320 as per IEC60320-C14.
   */
  public static IEC60320 C14() {
    List<Pin> pins = new ArrayList<>();
    final Size pinHorizontalSpacing = Size.mm(7);
    final Size pinVerticalSpacing = Size.mm(4);
    final Size pinWidth = Size.mm(2);
    final Size pinHeight = Size.mm(4);
    final Point pinReference = Area.point(reference.x, reference.y - pinHeight.half().asPixels());
    pins.add(Pin.rectangular(pinReference, pinWidth, pinHeight));
    pins.add(Pin.rectangular(
        Area.point(
            pinReference.x - pinHorizontalSpacing.asPixels(),
            pinReference.y + pinHeight.asPixels()),
        pinWidth,
        pinHeight));
    pins.add(Pin.rectangular(
        Area.point(
            pinReference.x + pinHorizontalSpacing.asPixels(),
            pinReference.y + pinHeight.asPixels()),
        pinWidth,
        pinHeight));
    final Size inletWidth = Size.mm(24);
    final Size inletHeight = Size.mm(16);
    // opening lower corner radius, IEC spec says R = 3 max.
    final Size inletLowerCornerRadius = Size.mm(1.5);
    // opening upper corner radius, IEC spec says R = 2 max.
    final Size inletUpperCornerRadius = Size.mm(.75);
    // Opening upper corner vertical offset; distance of start of
    // slant from reference point.
    final Size inletUpperCornerVerticalOffset = Size.mm(3);
    final double halfInletHeight = inletHeight.half().asPixels();
    final double halfInletWidth = inletWidth.half().asPixels();
    final double inletUpperRadius = inletUpperCornerRadius.asPixels();
    final double inletLowerRadius = inletLowerCornerRadius.asPixels();
    final Size courtyardWidth = Size.mm(30.5);
    final Size courtyardHeight = Size.mm(22.5);
    final Size courtyardRadius = inletLowerCornerRadius;
    final Size mountWidth = courtyardWidth;
    final Size mountHeight = courtyardHeight;
    final double halfMountWidth = mountWidth.half().asPixels();
    final double halfMountHeight = mountHeight.half().asPixels();
    final double lugWidth = halfMountWidth + mountWidth.multiply(.3).asPixels();
    // TODO figure out less arbitrary value for the following offsets
    final double lugRadius = inletLowerRadius * 3.5;
    final double mountHoleDiameter = Size.mm(3.5).asPixels();
    final double holeOffset = lugWidth - 1.5 * mountHoleDiameter;
    return new IEC60320(
        CouplerType.INLET,
        pins,
        // inlet
        Area.roundedPolygon(new Point[] {
            Area.point(
                reference.x,
                reference.y - halfInletHeight),
            Area.point(
                reference.x + pinHorizontalSpacing.asPixels(),
                reference.y - halfInletHeight),
            Area.point(
                reference.x + halfInletWidth,
                reference.y - inletUpperCornerVerticalOffset.asPixels()),
            Area.point(
                reference.x + halfInletWidth,
                reference.y + halfInletHeight),
            Area.point(
                reference.x - halfInletWidth,
                reference.y + halfInletHeight),
            Area.point(
                reference.x - halfInletWidth,
                reference.y - inletUpperCornerVerticalOffset.asPixels()),
            Area.point(
                reference.x - pinHorizontalSpacing.asPixels(),
                reference.y - halfInletHeight)
          }, new double[] {
            inletUpperRadius,
            inletUpperRadius,
            inletLowerRadius,
            inletLowerRadius,
            inletUpperRadius,
            inletUpperRadius
          }),
        // courtyard
        Area.centeredRoundRect(reference, courtyardWidth, courtyardHeight, courtyardRadius),
        // mount
        Area.roundedPolygon(new Point[] {
            Area.point(reference.x, reference.y - halfMountHeight),
            Area.point(reference.x + halfMountWidth, reference.y - halfMountHeight),
            Area.point(reference.x + lugWidth, reference.y),
            Area.point(reference.x + halfMountWidth, reference.y + halfMountHeight),
            Area.point(reference.x - halfMountWidth, reference.y + halfMountHeight),
            Area.point(reference.x - lugWidth, reference.y),
            Area.point(reference.x - halfMountWidth, reference.y - halfMountHeight),
          }, new double[] {
            inletUpperRadius,
            lugRadius,
            inletUpperRadius,
            inletUpperRadius,
            lugRadius,
            inletUpperRadius
          })
        .subtract(Area.circle(reference.x + holeOffset, reference.y, mountHoleDiameter))
        .subtract(Area.circle(reference.x - holeOffset, reference.y, mountHoleDiameter)));
  }
}
