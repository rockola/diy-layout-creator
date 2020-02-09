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

package org.diylc.presenter;

import java.awt.geom.Point2D;

public class Connection {

  private Point2D p1;
  private Point2D p2;

  public Connection(Point2D p1, Point2D p2) {
    this.p1 = p1.getX() < p2.getX() || p1.getX() == p2.getX() && p1.getY() < p2.getY() ? p1 : p2;
    this.p2 = p1.getX() < p2.getX() || p1.getX() == p2.getX() && p1.getY() < p2.getY() ? p2 : p1;
  }

  public Point2D getP1() {
    return p1;
  }

  public Point2D getP2() {
    return p2;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((p1 == null) ? 0 : p1.hashCode());
    result = prime * result + ((p2 == null) ? 0 : p2.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this != obj) {
      if (obj == null
          || getClass() != obj.getClass()) {
        return false;
      }
      Connection other = (Connection) obj;
      if ((p1 == null && other.p1 != null)
          || (p1 != null && !p1.equals(other.p1))
          || (p2 == null && other.p2 != null)
          || (p2 != null && !p2.equals(other.p1))) {
        return false;
      }
    }
    return true;
  }
}
