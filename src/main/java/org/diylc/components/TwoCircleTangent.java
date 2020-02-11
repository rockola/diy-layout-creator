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

package org.diylc.components;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * Creates an area covered by the two circles and the area between them bordered by the two external
 * common tangents.
 *
 * @author Branislav Stojkovic
 */
public class TwoCircleTangent extends Area {

  public TwoCircleTangent(Point2D p1, Point2D p2, double r1, double r2) {
    double d = p1.distance(p2);
    double h = Math.sqrt(d * d - (r1 - r2) * (r1 - r2));
    double y = Math.hypot(h, r2);
    Area a = Area.circle(p1, r1 * 2);
    a.add(Area.circle(p2, r2 * 2));

    double acos = Math.acos((r1 * r1 + d * d - y * y) / (2 * r1 * d));
    double deltaX = p2.getX() - p1.getX();
    double deltaY = p2.getY() - p1.getY();
    double atan = Math.atan2(deltaY, deltaX);
    double theta1 = atan + acos;
    double theta2 = atan - acos;
    Path2D path = new Path2D.Double();
    path.moveTo(p1.getX() + r1 * Math.cos(theta1), p1.getY() + r1 * Math.sin(theta1));
    path.lineTo(p2.getX() + r2 * Math.cos(theta1), p2.getY() + r2 * Math.sin(theta1));
    path.lineTo(p2.getX() + r2 * Math.cos(theta2), p2.getY() + r2 * Math.sin(theta2));
    path.lineTo(p1.getX() + r1 * Math.cos(theta2), p1.getY() + r1 * Math.sin(theta2));
    path.closePath();

    a.add(new Area(path));
    add(a);
  }
}
