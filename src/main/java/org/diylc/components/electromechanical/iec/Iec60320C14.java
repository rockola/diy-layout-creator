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

package org.diylc.components.electromechanical.iec;

import java.awt.Point;
import org.diylc.components.Area;
import org.diylc.components.electromechanical.Pin;
import org.diylc.core.annotations.Variant;
import org.diylc.core.measures.Size;

/**
 * IEC60320 C14
 *
 * <p>10 A 250 V appliance coupler for class I equipment and cold conditions, inlet.
 */
public class Iec60320C14 extends Iec60320 {

  private static final long serialVersionUID = 1L;

  public Iec60320C14() {
    super(Iec60320.CouplerType.INLET);
    final Size pinHorizontalSpacing = Size.mm(7);
    final Size pinVerticalSpacing = Size.mm(4);
    final Size pinWidth = Size.mm(2);
    final Size pinHeight = Size.mm(4);
    final Point pinReference = Area.point(reference.x, reference.y - pinHeight.half().asPixels());
    pins.add(Pin.rectangular(pinReference, pinWidth, pinHeight));
    pins.add(
        Pin.rectangular(
            Area.point(
                pinReference.x - pinHorizontalSpacing.asPixels(),
                pinReference.y + pinVerticalSpacing.asPixels()),
            pinWidth,
            pinHeight));
    pins.add(
        Pin.rectangular(
            Area.point(
                pinReference.x + pinHorizontalSpacing.asPixels(),
                pinReference.y + pinVerticalSpacing.asPixels()),
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

    coupler =
        Area.roundedPolygon(
            new Point[] {
              Area.point(reference.x, reference.y - halfInletHeight),
              Area.point(
                  reference.x + pinHorizontalSpacing.asPixels(), reference.y - halfInletHeight),
              Area.point(
                  reference.x + halfInletWidth,
                  reference.y - inletUpperCornerVerticalOffset.asPixels()),
              Area.point(reference.x + halfInletWidth, reference.y + halfInletHeight),
              Area.point(reference.x - halfInletWidth, reference.y + halfInletHeight),
              Area.point(
                  reference.x - halfInletWidth,
                  reference.y - inletUpperCornerVerticalOffset.asPixels()),
              Area.point(
                  reference.x - pinHorizontalSpacing.asPixels(), reference.y - halfInletHeight)
            },
            new double[] {
              inletUpperRadius,
              inletUpperRadius,
              inletLowerRadius,
              inletLowerRadius,
              inletUpperRadius,
              inletUpperRadius
            });
    courtyard = Area.centeredRoundRect(reference, courtyardWidth, courtyardHeight, courtyardRadius);
    mount =
        Area.roundedPolygon(
                new Point[] {
                  Area.point(reference.x, reference.y - halfMountHeight),
                  Area.point(reference.x + halfMountWidth, reference.y - halfMountHeight),
                  Area.point(reference.x + lugWidth, reference.y),
                  Area.point(reference.x + halfMountWidth, reference.y + halfMountHeight),
                  Area.point(reference.x - halfMountWidth, reference.y + halfMountHeight),
                  Area.point(reference.x - lugWidth, reference.y),
                  Area.point(reference.x - halfMountWidth, reference.y - halfMountHeight),
                },
                new double[] {
                  inletUpperRadius,
                  lugRadius,
                  inletUpperRadius,
                  inletUpperRadius,
                  lugRadius,
                  inletUpperRadius
                })
            .subtract(Area.circle(reference.x + holeOffset, reference.y, mountHoleDiameter))
            .subtract(Area.circle(reference.x - holeOffset, reference.y, mountHoleDiameter));
  }

  @Variant
  public static IecSocket variant() {
    return new Iec60320C14();
  }
}
